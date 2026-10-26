package com.threeloops.mirrorarchive.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.threeloops.mirrorarchive.contracts.AlbumType
import com.threeloops.mirrorarchive.contracts.ScanPhase

@Entity(
    tableName = "images",
    indices = [Index(value = ["mediaId"], unique = true)]
)
data class ImageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val mediaId: Long,
    val uri: String,
    val displayName: String,
    val relativePath: String,
    val dateAdded: Long,
    val dateTaken: Long?,
    val generationModified: Long,
    val width: Int,
    val height: Int,
    val sizeBytes: Long,
    val dHash: Long,
    val pHash: Long,
    val blurScore: Double,
    val ocrText: String?,
    val faceCount: Int,
    val indexedAt: Long,
    val pipelineVersion: Int
)

@Entity(
    tableName = "labels",
    primaryKeys = ["imageId", "label"],
    foreignKeys = [
        ForeignKey(
            entity = ImageEntity::class,
            parentColumns = ["id"],
            childColumns = ["imageId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["label"]), Index(value = ["imageId"])]
)
data class LabelEntity(
    val imageId: Long,
    /** Must be normalized before persistence. */
    val label: String,
    val confidence: Float
) {
    init {
        require(label == label.lowercase()) { "LabelEntity.label must be lowercase" }
    }
}

@Entity(
    tableName = "album_entries",
    primaryKeys = ["imageId", "album"],
    foreignKeys = [
        ForeignKey(
            entity = ImageEntity::class,
            parentColumns = ["id"],
            childColumns = ["imageId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["album"])]
)
data class AlbumEntryEntity(
    val imageId: Long,
    val album: AlbumType
)

@Entity(
    tableName = "faces",
    foreignKeys = [
        ForeignKey(
            entity = ImageEntity::class,
            parentColumns = ["id"],
            childColumns = ["imageId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["imageId"])]
)
data class FaceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val imageId: Long,
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int,
    val smileProb: Float?,
    val eyesOpenProb: Float?,
    val clusterId: Long?
)

@Entity(
    tableName = "hash_buckets",
    primaryKeys = ["imageId", "chunkIdx"],
    foreignKeys = [
        ForeignKey(
            entity = ImageEntity::class,
            parentColumns = ["id"],
            childColumns = ["imageId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["chunkIdx", "chunkVal"]), Index(value = ["imageId"])]
)
data class HashBucketEntity(
    val imageId: Long,
    /** Chunk offset in the 64-bit hash, from 0 through 7. */
    val chunkIdx: Int,
    /** Unsigned byte value, from 0 through 255. */
    val chunkVal: Int
) {
    init {
        require(chunkIdx in 0..7) { "chunkIdx must be in 0..7" }
        require(chunkVal in 0..255) { "chunkVal must be in 0..255" }
    }
}

@Entity(tableName = "scan_state")
data class ScanStateEntity(
    @PrimaryKey val id: Int = SINGLETON_ID,
    val lastProcessedId: Long,
    val lastGeneration: Long,
    val totalCount: Long,
    val processedCount: Long,
    val phase: ScanPhase,
    val updatedAt: Long
) {
    init {
        require(id == SINGLETON_ID) { "ScanStateEntity id is fixed at $SINGLETON_ID" }
    }

    companion object {
        const val SINGLETON_ID = 1
    }
}

@Entity(tableName = "skipped_images")
data class SkippedImageEntity(
    @PrimaryKey val mediaId: Long,
    val reason: String,
    val at: Long
)

@Entity(tableName = "pipeline_stats")
data class PipelineStatEntity(
    @PrimaryKey val runId: String,
    val startedAt: Long,
    val images: Long,
    val decodeMs: Long,
    val labelMs: Long,
    val hashMs: Long,
    val ocrMs: Long,
    val faceMs: Long,
    val dbMs: Long,
    val peakHeapMb: Double
)
