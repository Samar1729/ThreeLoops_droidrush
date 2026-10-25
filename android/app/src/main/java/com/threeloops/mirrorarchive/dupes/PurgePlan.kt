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
)