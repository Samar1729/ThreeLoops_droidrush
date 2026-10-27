package com.threeloops.mirrorarchive.work

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.threeloops.mirrorarchive.contracts.ScanPhase
import com.threeloops.mirrorarchive.contracts.ScanProgress
import com.threeloops.mirrorarchive.data.db.AppDatabase
import com.threeloops.mirrorarchive.data.db.ScanStateEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Schedules and observes scans.
 *
 * State machine: IDLE means no scan has started; QUEUED means WorkManager accepted or is waiting
 * to start work; SCANNING means the worker is actively processing; PAUSED means work was stopped,
 * cancelled, blocked, or the process died before its checkpoint reached the total; SYNCING means
 * the worker is reconciling its MediaStore generation checkpoint; COMPLETE means the checkpoint
 * reached the current total or WorkManager succeeded; FAILED means WorkManager reported failure.
 */
@Singleton
class ScanController @Inject constructor(
    @ApplicationContext context: Context,
    database: AppDatabase
) {
    private val workManager = WorkManager.getInstance(context)
    private val scanStateDao = database.scanStateDao()
    private val controllerScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /** Enqueue a unique full scan, or an unconstrained immediate run when requested. */
    fun start(ignoreConstraints: Boolean = false) {
        val requestBuilder = OneTimeWorkRequestBuilder<IndexWorker>()

        val uniqueName = if (ignoreConstraints) WORK_NOW else WORK_FULL
        if (!ignoreConstraints) {
            requestBuilder
                .setConstraints(
                    Constraints.Builder()
                        .setRequiresBatteryNotLow(true)
                        .setRequiresStorageNotLow(true)
                        .build()
                )
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
        }

        workManager.enqueueUniqueWork(
            uniqueName,
            ExistingWorkPolicy.REPLACE,
            requestBuilder.build()
        )
    }

    /** Live progress from both unique work names, backed by the persisted scan checkpoint. */
    fun progress(): Flow<ScanProgress> = combine(
        workManager.getWorkInfosForUniqueWorkFlow(WORK_FULL),
        workManager.getWorkInfosForUniqueWorkFlow(WORK_NOW),
        scanStateDao.observe()
    ) { fullWork, immediateWork, checkpoint ->
        val workInfo = selectWorkInfo(fullWork + immediateWork)
        if (workInfo == null) progressFromCheckpoint(checkpoint)
        else progressFromWork(workInfo, checkpoint)
    }.flowOn(Dispatchers.IO)

    /** Cancel both unique scan names without changing the persisted checkpoint. */
    fun cancel() {
        workManager.cancelUniqueWork(WORK_FULL)
        workManager.cancelUniqueWork(WORK_NOW)
    }

    /** Reset the checkpoint for a full rescan, retaining indexed rows until they are replaced. */
    fun rescan(full: Boolean) {
        if (!full) {
            start()
            return
        }

        controllerScope.launch {
            val previous = scanStateDao.observe().first()
            scanStateDao.upsert(
                ScanStateEntity(
                    lastProcessedId = 0L,
                    lastGeneration = 0L,
                    totalCount = previous?.totalCount ?: 0L,
                    processedCount = 0L,
                    phase = ScanPhase.IDLE,
                    updatedAt = System.currentTimeMillis()
                )
            )
            start()
        }
    }

    private fun progressFromWork(work: WorkInfo, checkpoint: ScanStateEntity?): ScanProgress {
        val fallbackDone = checkpoint?.processedCount ?: 0L
        val fallbackTotal = checkpoint?.totalCount ?: 0L
        val done = work.progress.getInt(KEY_DONE, fallbackDone.toIntSaturated())
        val total = work.progress.getInt(KEY_TOTAL, fallbackTotal.toIntSaturated())
        val workerPhase = work.progress.getString(KEY_PHASE)
            ?.let { phase -> runCatching { ScanPhase.valueOf(phase) }.getOrNull() }

        val phase = when (work.state) {
            WorkInfo.State.ENQUEUED ->
                if (work.runAttemptCount > 0) ScanPhase.PAUSED else ScanPhase.QUEUED
            WorkInfo.State.RUNNING -> workerPhase ?: ScanPhase.SCANNING
            WorkInfo.State.BLOCKED, WorkInfo.State.CANCELLED -> ScanPhase.PAUSED
            WorkInfo.State.SUCCEEDED -> ScanPhase.COMPLETE
            WorkInfo.State.FAILED -> ScanPhase.FAILED
        }
        return ScanProgress(phase = phase, done = done, total = total)
    }

    private fun progressFromCheckpoint(state: ScanStateEntity?): ScanProgress {
        if (state == null) return ScanProgress(ScanPhase.IDLE, 0, 0)
        val phase = when {
            state.phase == ScanPhase.SYNCING -> ScanPhase.SYNCING
            state.phase == ScanPhase.COMPLETE -> ScanPhase.COMPLETE
            state.phase == ScanPhase.FAILED -> ScanPhase.FAILED
            state.totalCount == 0L && state.processedCount == 0L -> ScanPhase.IDLE
            state.processedCount >= state.totalCount -> ScanPhase.COMPLETE
            else -> ScanPhase.PAUSED
        }
        return ScanProgress(
            phase = phase,
            done = state.processedCount.toIntSaturated(),
            total = state.totalCount.toIntSaturated()
        )
    }

    private fun selectWorkInfo(workInfos: List<WorkInfo>): WorkInfo? {
        return workInfos.firstOrNull { it.state == WorkInfo.State.RUNNING }
            ?: workInfos.firstOrNull { it.state == WorkInfo.State.ENQUEUED }
            ?: workInfos.firstOrNull { it.state == WorkInfo.State.BLOCKED }
            ?: workInfos.lastOrNull()
    }

    private fun Long.toIntSaturated(): Int = coerceIn(0L, Int.MAX_VALUE.toLong()).toInt()
}
