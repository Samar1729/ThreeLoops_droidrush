package com.threeloops.mirrorarchive.contracts

import android.graphics.Rect
import android.net.Uri

/**
 * Smart album classification categories.
 */
enum class AlbumType {
    RECEIPTS,
    SCREENSHOTS,
    DOCUMENTS,
    FOOD,
    PEOPLE
}

/**
 * Current execution phase of the background media indexing engine.
 */
enum class ScanPhase {
    IDLE,
    QUEUED,
    SCANNING,
    PAUSED,
    COMPLETE,
    SYNCING,
    FAILED
}

/**
 * Machine learning image labeling prediction result.
 *
 * @property text Descriptive classification label text.
 * @property confidence Confidence score in range [0.0, 1.0].
 */
data class LabelResult(
    val text: String,
    val confidence: Float
)

/**
 * Bounding box and facial attribute probabilities for a detected face.
 *
 * @property box Bounding rectangle of the face within the image coordinate space.
 * @property smileProb Probability that the face is smiling, or null if uncomputed.
 * @property eyesOpenProb Probability that eyes are open, or null if uncomputed.
 */
data class FaceInfo(
    val box: Rect,
    val smileProb: Float?,
    val eyesOpenProb: Float?
)

/**
 * Perceptual hash values and blur estimation computed for an image.
 *
 * @property dHash Difference hash 64-bit value.
 * @property pHash Perceptual hash 64-bit value.
 * @property blur Laplacian variance blur score.
 */
data class HashBundle(
    val dHash: Long,
    val pHash: Long,
    val blur: Double
)

/**
 * Minimal hash representation for fast in-memory duplicate search.
 *
 * @property id Unique record identifier.
 * @property dHash Difference hash 64-bit value.
 * @property pHash Perceptual hash 64-bit value.
 * @property blurScore Laplacian variance blur score.
 * @property dateAdded Epoch timestamp when media was added (seconds).
 * @property dateTaken Epoch timestamp when media was captured (milliseconds), or null.
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
 * Read-only image presentation model.
 *
 * @property id Internal database identifier.
 * @property mediaId System MediaStore row identifier.
 * @property uri Content URI string representing the image.
 * @property dateAdded Epoch timestamp when media was added.
 * @property dateTaken Epoch timestamp when media was captured, or null.
 * @property sizeBytes File size in bytes.
 * @property width Image width in pixels.
 * @property height Image height in pixels.
 * @property blurScore Blur score (higher indicates sharper image).
 * @property faceCount Number of detected faces.
 */
data class ImageItem(
    val id: Long,
    val mediaId: Long,
    val uri: String,
    val dateAdded: Long,
    val dateTaken: Long?,
    val sizeBytes: Long,
    val width: Int,
    val height: Int,
    val blurScore: Double,
    val faceCount: Int
)

/**
 * Display name extracted or derived from the URI string.
 */
val ImageItem.displayName: String
    get() = uri.substringAfterLast('/')

/**
 * Aggregated summary for a smart album category.
 *
 * @property type The smart album category.
 * @property count Total count of images classified into this album.
 * @property coverUri Content URI string of the cover image, or null if album is empty.
 */
data class AlbumSummary(
    val type: AlbumType,
    val count: Int,
    val coverUri: String?
)

/**
 * Progress status of an ongoing or completed media scan.
 *
 * @property phase Current scan phase.
 * @property done Count of items processed so far.
 * @property total Total count of items scheduled for processing.
 */
data class ScanProgress(
    val phase: ScanPhase,
    val done: Int,
    val total: Int
)

/**
 * Typealias representing the scan progress state.
 */
typealias ScanState = ScanProgress

/**
 * Raw media item queried directly from MediaStore.
 *
 * @property mediaId System MediaStore row ID (_ID).
 * @property uri Parsed content URI for the media file.
 * @property displayName File name including extension.
 * @property relativePath Relative storage path (e.g., DCIM/Camera/).
 * @property dateAdded Timestamp when media was added, in seconds.
 * @property dateTaken Timestamp when media was captured, in milliseconds, or null.
 * @property generationModified System generation number for change tracking.
 * @property width Image width in pixels.
 * @property height Image height in pixels.
 * @property sizeBytes File size in bytes.
 * @property mimeType MIME type string (e.g., image/jpeg).
 */
data class MediaRow(
    val mediaId: Long,
    val uri: Uri,
    val displayName: String,
    val relativePath: String,
    val dateAdded: Long,
    val dateTaken: Long?,
    val generationModified: Long,
    val width: Int,
    val height: Int,
    val sizeBytes: Long,
    val mimeType: String
)

/**
 * Fully processed image metadata record ready for persistence and query.
 *
 * @property mediaId System MediaStore row ID.
 * @property uri Content URI string.
 * @property labels Classification labels detected by ML Kit.
 * @property ocrText Extracted text from OCR, or null if no text found.
 * @property dHash Difference hash 64-bit value.
 * @property pHash Perceptual hash 64-bit value.
 * @property blurScore Blur score (Laplacian variance).
 * @property faceCount Number of detected faces.
 * @property displayName File name including extension.
 * @property relativePath Relative directory path.
 * @property dateTaken Timestamp when taken (ms), or null.
 * @property generationModified MediaStore generation marker.
 * @property width Image width in pixels.
 * @property height Image height in pixels.
 * @property sizeBytes File size in bytes.
 * @property faces List of detected face details.
 * @property albums Set of smart album categories matched.
 * @property pipelineVersion Indexing pipeline schema version.
 */
data class IndexedImage(
    val mediaId: Long,
    val uri: String,
    val labels: List<LabelResult>,
    val ocrText: String?,
    val dHash: Long,
    val pHash: Long,
    val blurScore: Double,
    val faceCount: Int,
    val displayName: String = "",
    val relativePath: String = "",
    val dateTaken: Long? = null,
    val generationModified: Long = 0,
    val width: Int = 0,
    val height: Int = 0,
    val sizeBytes: Long = 0L,
    val faces: List<FaceInfo> = emptyList(),
    val albums: Set<AlbumType> = emptySet(),
    val pipelineVersion: Int = 1
)
