package com.threeloops.mirrorarchive.work

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.threeloops.mirrorarchive.R
import com.threeloops.mirrorarchive.contracts.ScanPhase
import com.threeloops.mirrorarchive.data.db.ScanStateDao
import com.threeloops.mirrorarchive.data.db.ScanStateEntity
import com.threeloops.mirrorarchive.data.mediastore.MediaStoreSource
import com.threeloops.mirrorarchive.data.repo.ImageRepository
import com.threeloops.mirrorarchive.metrics.MemoryProbe
import com.threeloops.mirrorarchive.metrics.MetricsRecorder
import com.threeloops.mirrorarchive.metrics.Stage
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Resumable, bounded-concurrency MediaStore indexer.
 *
 * Checkpoints are advanced after every source page, including pages whose items were skipped.
 * Individual decode and analysis failures are recorded by [IndexPipeline] and do not fail a run.
 */
@HiltWorker
class IndexWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val repository: ImageRepository,
    private val mediaStore: MediaStoreSource,
    private val pipeline: IndexPipeline,
    private val scanStateDao: ScanStateDao,
    private val metricsRecorder: MetricsRecorder,
    private val memoryProbe: MemoryProbe
) : CoroutineWorker(appContext, params) {

    private var currentCheckpoint = 0L
    private var currentDone = 0L
    private var currentTotal = 0L
    private val foregroundStarted = AtomicBoolean(false)

    override suspend fun doWork(): Result {
        val metricsRunId = metricsRecorder.startRun()
        return try {
            ensureMediaPermission()
            currentCheckpoint = repository.lastProcessedId()
            currentTotal = mediaStore.count().toLong()
            val previous = scanStateDao.observe().first()
            currentDone = previous?.processedCount?.coerceAtMost(currentTotal) ?: 0L
            writeState(phase = ScanPhase.SCANNING)
            publishProgress(ScanPhase.SCANNING)
            setForeground(foregroundInfo(currentDone, currentTotal))
            foregroundStarted.set(true)

            var exhausted = false
            while (!isStopped) {
                ensureMediaPermission()
                val rows = mediaStore.query(afterId = currentCheckpoint, limit = PAGE_SIZE)
                ensureMediaPermission()
                if (rows.isEmpty()) {
                    exhausted = true
                    break
                }

                val semaphore = Semaphore(MAX_CONCURRENT_IMAGES)
                val results = coroutineScope {
                    rows.map { row ->
                        async(Dispatchers.Default) {
                            semaphore.withPermit { pipeline.processOne(row) }
                        }
                    }.awaitAll()
                }

                currentCheckpoint = rows.last().mediaId
                val indexed = results.filterNotNull()
                metricsRecorder.timeSuspend(Stage.DB) {
                    repository.saveBatch(indexed, checkpointId = currentCheckpoint)
                }
                metricsRecorder.recordImage(indexed.size.toLong())
                memoryProbe.sample()
                currentDone += rows.size
                // Repository increments this for stored rows; worker owns source-row progress,
                // which also includes images intentionally skipped by the pipeline.
                writeState(phase = ScanPhase.SCANNING)
                publishProgress(ScanPhase.SCANNING)
                if (foregroundStarted.get()) {
                    setForeground(foregroundInfo(currentDone, currentTotal))
                }
            }

            if (isStopped || !exhausted) {
                writeState(phase = ScanPhase.PAUSED)
                publishProgress(ScanPhase.PAUSED)
                Result.retry()
            } else {
                ensureMediaPermission()
                // Count again so newly-added rows after the initial count are reflected on the
                // next scheduled scan, while COMPLETE means the query was exhausted.
                writeState(
                    phase = ScanPhase.COMPLETE,
                    total = mediaStore.count().toLong(),
                    generation = mediaStore.currentGeneration()
                )
                publishProgress(ScanPhase.COMPLETE)
                Result.success()
            }
        } catch (e: SecurityException) {
            persistTerminalState(ScanPhase.FAILED)
            publishProgress(ScanPhase.FAILED)
            Result.failure(workDataOf(KEY_PHASE to ScanPhase.FAILED.name))
        } catch (e: CancellationException) {
            withContext(NonCancellable + Dispatchers.IO) {
                persistTerminalState(ScanPhase.PAUSED)
            }
            if (isStopped) Result.retry() else throw e
        } finally {
            withContext(NonCancellable + Dispatchers.IO) {
                metricsRecorder.finishRun(metricsRunId)
            }
        }
    }

    private suspend fun ensureMediaPermission() {
        val fullAccess = when {
            Build.VERSION.SDK_INT >= 33 -> granted(Manifest.permission.READ_MEDIA_IMAGES)
            else -> granted(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        val partialAccess = Build.VERSION.SDK_INT >= 34 &&
            granted(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
        if (!fullAccess && !partialAccess) {
            throw SecurityException("Media access permission was revoked")
        }
    }

    private fun granted(permission: String): Boolean =
        ContextCompat.checkSelfPermission(applicationContext, permission) == PackageManager.PERMISSION_GRANTED

    private suspend fun writeState(
        phase: ScanPhase,
        total: Long = currentTotal,
        generation: Long? = null
    ) {
        val old = scanStateDao.observe().first()
        scanStateDao.upsert(
            ScanStateEntity(
                lastProcessedId = currentCheckpoint,
                lastGeneration = generation ?: old?.lastGeneration ?: 0L,
                totalCount = total,
                processedCount = currentDone,
                phase = phase,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    private suspend fun persistTerminalState(phase: ScanPhase) {
        val old = scanStateDao.observe().first()
        currentCheckpoint = maxOf(currentCheckpoint, old?.lastProcessedId ?: 0L)
        currentDone = maxOf(currentDone, old?.processedCount ?: 0L)
        currentTotal = maxOf(currentTotal, old?.totalCount ?: 0L)
        writeState(phase)
    }

    private suspend fun publishProgress(phase: ScanPhase) {
        setProgress(
            workDataOf(
                KEY_DONE to currentDone.coerceAtMost(Int.MAX_VALUE.toLong()).toInt(),
                KEY_TOTAL to currentTotal.coerceAtMost(Int.MAX_VALUE.toLong()).toInt(),
                KEY_PHASE to phase.name
            )
        )
    }

    private fun foregroundInfo(done: Long, total: Long): ForegroundInfo {
        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "Photo indexing", NotificationManager.IMPORTANCE_LOW)
            )
        }
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_indexing)
            .setContentTitle("Indexing photos ($done/$total)")
            .setContentText("Scanning photos on this device")
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setProgress(total.toInt().coerceAtLeast(0), done.toInt().coerceAtLeast(0), total == 0L)
            .build()

        // Android allows foreground services to run when notification permission is denied;
        // the system may hide the notification from the drawer, but the scan must continue.
        if (Build.VERSION.SDK_INT >= 33 && !granted(Manifest.permission.POST_NOTIFICATIONS)) {
            android.util.Log.i(TAG, "POST_NOTIFICATIONS denied; continuing foreground scan")
        }
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            ForegroundInfo(NOTIFICATION_ID, notification)
        }
    }

    private companion object {
        const val PAGE_SIZE = 50
        const val MAX_CONCURRENT_IMAGES = 3
        const val CHANNEL_ID = "photo_indexing"
        const val NOTIFICATION_ID = 4107
        const val TAG = "MirrorMediaStore"
    }
}
