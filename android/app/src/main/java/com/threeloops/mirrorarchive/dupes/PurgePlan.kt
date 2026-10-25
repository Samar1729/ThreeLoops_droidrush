package com.threeloops.mirrorarchive.dupes

import android.net.Uri

/**
 * Manifest for an upcoming deletion.
 * Maps to S-3: PurgePlan(keepIds, deleteIds, deleteUris, bytesFreed)
 */
data class PurgePlan(
    val keepIds: List<Long>,
    val deleteIds: List<Long>,   // MediaStore IDs
    val deleteUris: List<Uri>,   // MediaStore URIs
    val bytesFreed: Long
) {
    val purgeIds: List<Long> get() = deleteIds
    val reclaimableBytes: Long get() = bytesFreed
}

sealed interface PurgeUiState {
    data object Idle : PurgeUiState
    data object Purging : PurgeUiState
    data class Done(val deletedCount: Int, val freedBytes: Long) : PurgeUiState
    data class Error(val message: String) : PurgeUiState
}