package com.threeloops.mirrorarchive.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.threeloops.mirrorarchive.contracts.ScanPhase

/**
 * WorkManager entry point for indexing. The indexing pipeline is supplied in the worker stage;
 * until it is wired, report a failure rather than marking an empty run as successfully indexed.
 */
class IndexWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result = Result.failure(
        workDataOf(KEY_PHASE to ScanPhase.FAILED.name)
    )
}
