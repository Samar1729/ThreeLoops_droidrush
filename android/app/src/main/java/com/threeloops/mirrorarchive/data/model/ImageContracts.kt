package com.threeloops.mirrorarchive.data.model

import android.net.Uri

/**
 * Section 3.5 ImageItem (read DTO)
 */
data class ImageItem(
    val id: Long,
    val mediaId: Long,
    val uri: Uri,
    val displayName: String = "",
    val relativePath: String = "",
    val dateAdded: Long,
    val dateTaken: Long? = null,
    val sizeBytes: Long = 0L,
    val width: Int = 0,
    val height: Int = 0,
    val blurScore: Double = 0.0,
    val faceCount: Int = 0,
    val ocrText: String? = null,
    val labels: List<LabelResult> = emptyList(),
    val faces: List<FaceInfo> = emptyList(),
    val albums: Set<AlbumType> = emptySet(),
    val dHash: Long = 0L,
    val pHash: Long = 0L
)

/**
 * Section 3.5 AlbumSummary
 */
data class AlbumSummary(
    val type: AlbumType,
    val count: Int,
    val coverUri: Uri?
)

/**
 * Section 3.11 HashRow
 */
data class HashRow(
    val id: Long,
    val dHash: Long,
    val pHash: Long,
    val blurScore: Double,
    val dateAdded: Long,
    val dateTaken: Long? = null
)

/**
 * Section 3.5 ScanState / ScanProgress
 */
data class ScanProgress(
    val phase: String,
    val done: Int,
    val total: Int
)
