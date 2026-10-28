package com.threeloops.mirrorarchive.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.WorkInfo
import androidx.work.workDataOf
import com.threeloops.mirrorarchive.contracts.MediaRow
import com.threeloops.mirrorarchive.contracts.ScanPhase
import com.threeloops.mirrorarchive.data.db.ScanStateDao
import com.threeloops.mirrorarchive.data.db.ScanStateEntity
import com.threeloops.mirrorarchive.data.mediastore.MediaStoreSource
import com.threeloops.mirrorarchive.data.repo.ImageRepository
import com.threeloops.mirrorarchive.permissions.PermissionManager
import com.threeloops.mirrorarchive.permissions.PermissionState
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

@HiltWorker
class IncrementalSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val repository: ImageRepository,
    private val mediaStore: MediaStoreSource,
    private val pipeline: IndexPipeline,
    private val scanStateDao: ScanStateDao,
    private val permissionManager: PermissionManager,
    private val syncUseCase: SyncUseCase
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        if (fullScanIsActive()) return Result.success()

        val original = scanStateDao.observe().first() ?: ScanStateEntity(
            lastProcessedId = 0L,
            lastGeneration = 0L,
            totalCount = 0L,
            processedCount = 0L,
            phase = ScanPhase.IDLE,
            updatedAt = System.currentTimeMillis()
        )
        var checkpoint = original.lastProcessedId
        var done = 0L
        var total = 0L

        return try {
            ensureMediaPermission()
            writeState(original, checkpoint, ScanPhase.SYNCING, done, total)
            publish(ScanPhase.SYNCING, done, total)

            val rowsById = linkedMapOf<Long, MediaRow>()
            mediaStore.changedSince(original.lastGeneration)
                .asSequence()
                .filter { it.mediaId <= original.lastProcessedId }
                .forEach { rowsById[it.mediaId] = it }

            var pageAfterId = original.lastProcessedId
            while (!isStopped) {
                ensureMediaPermission()
                val page = mediaStore.query(afterId = pageAfterId, limit = PAGE_SIZE)
                ensureMediaPermission()
                if (page.isEmpty()) break
                page.forEach { rowsById[it.mediaId] = it }
                pageAfterId = maxOf(pageAfterId, page.maxOf { it.mediaId })
            }
            if (isStopped) {
                writeState(original, checkpoint, ScanPhase.PAUSED, done, total)
                return Result.retry()
            }

            val orderedRows = rowsById.values.sortedBy(MediaRow::mediaId)
            total = orderedRows.size.toLong()
            writeState(original, checkpoint, ScanPhase.SYNCING, done, total)
            publish(ScanPhase.SYNCING, done, total)

            for (batch in orderedRows.chunked(PAGE_SIZE)) {
                if (isStopped) {
                    writeState(original, checkpoint, ScanPhase.PAUSED, done, total)
                    return Result.retry()
                }
                ensureMediaPermission()
                val semaphore = Semaphore(MAX_CONCURRENT_IMAGES)
                val indexed = coroutineScope {
                    batch.map { row ->
                        async(Dispatchers.Default) {
                            semaphore.withPermit { pipeline.processOne(row) }
                        }
                    }.awaitAll().filterNotNull()
                }

                val pageCheckpoint = batch.asSequence()
                    .filter { it.mediaId > original.lastProcessedId }
                    .maxOfOrNull(MediaRow::mediaId)
                checkpoint = maxOf(checkpoint, pageCheckpoint ?: original.lastProcessedId)
                repository.saveIncrementalBatch(
                    items = indexed,
                    checkpointId = checkpoint,
                    generationBaseline = original.lastGeneration
                )
                done += batch.size
                writeState(original, checkpoint, ScanPhase.SYNCING, done, total)
                publish(ScanPhase.SYNCING, done, total)
            }

            ensureMediaPermission()
            // Commit the generation only after every changed/new row has been saved.
            val committedGeneration = mediaStore.currentGeneration()
            writeState(
                original.copy(lastGeneration = committedGeneration),
                checkpoint,
                ScanPhase.SYNCING,
                done,
                total
            )
            syncUseCase.reconcileDeletions()
            writeState(
                original.copy(lastGeneration = committedGeneration),
                checkpoint,
                ScanPhase.COMPLETE,
                done,
                total
            )
            publish(ScanPhase.COMPLETE, done, total)
            Result.success()
        } catch (cancelled: CancellationException) {
            withContext(NonCancellable + Dispatchers.IO) {
                writeState(original, checkpoint, ScanPhase.PAUSED, done, total)
            }
            if (isStopped) Result.retry() else throw cancelled
        } catch (security: SecurityException) {
            writeState(original, checkpoint, ScanPhase.FAILED, done, total)
            publish(ScanPhase.FAILED, done, total)
            Result.failure(workDataOf(KEY_PHASE to ScanPhase.FAILED.name))
        } catch (failure: Exception) {
            writeState(original, checkpoint, ScanPhase.PAUSED, done, total)
            Result.retry()
        }
    }

    private suspend fun fullScanIsActive(): Boolean {
        val workManager = WorkManager.getInstance(applicationContext)
        return listOf(WORK_FULL, WORK_NOW).any { name ->
            workManager.getWorkInfosForUniqueWorkFlow(name).first().any { info ->
                info.state == WorkInfo.State.ENQUEUED ||
                    info.state == WorkInfo.State.RUNNING ||
                    info.state == WorkInfo.State.BLOCKED
            }
        }
    }

    private fun ensureMediaPermission() {
        val permission = permissionManager.state()
        if (permission == PermissionState.DENIED || permission == PermissionState.DENIED_PERMANENTLY) {
            throw SecurityException("Media access permission was revoked")
        }
    }

    private suspend fun writeState(
        baseline: ScanStateEntity,
        checkpoint: Long,
        phase: ScanPhase,
        done: Long,
        total: Long
    ) {
        scanStateDao.upsert(
            baseline.copy(
                lastProcessedId = maxOf(baseline.lastProcessedId, checkpoint),
                totalCount = total,
                processedCount = done,
                phase = phase,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    private suspend fun publish(phase: ScanPhase, done: Long, total: Long) {
        setProgress(
            workDataOf(
                KEY_DONE to done.coerceAtMost(Int.MAX_VALUE.toLong()).toInt(),
                KEY_TOTAL to total.coerceAtMost(Int.MAX_VALUE.toLong()).toInt(),
                KEY_PHASE to phase.name
            )
        )
    }

    private companion object {
        const val PAGE_SIZE = 50
        const val MAX_CONCURRENT_IMAGES = 3
    }
}
