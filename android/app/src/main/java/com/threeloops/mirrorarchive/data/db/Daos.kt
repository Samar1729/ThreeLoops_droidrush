package com.threeloops.mirrorarchive.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.Upsert
import com.threeloops.mirrorarchive.contracts.AlbumSummary
import com.threeloops.mirrorarchive.contracts.AlbumType
import com.threeloops.mirrorarchive.contracts.HashRow
import androidx.sqlite.db.SupportSQLiteQuery
import kotlinx.coroutines.flow.Flow

@Dao
abstract class ImageDao {
    /**
     * Atomically upsert a batch by MediaStore mediaId and replace child data for updated images.
     * Child imageIds refer to the input ImageEntity.id when nonzero, otherwise the input mediaId;
     * callers may also supply the current database id for an existing image. All references are
     * remapped to the actual generated database id before insertion.
     */
    @Transaction
    open suspend fun saveBatchTx(
        images: List<ImageEntity>,
        labels: List<LabelEntity>,
        albums: List<AlbumEntryEntity>,
        faces: List<FaceEntity>,
        buckets: List<HashBucketEntity>,
        state: ScanStateEntity
    ) {
        require(images.map { it.mediaId }.distinct().size == images.size) {
            "A batch cannot contain duplicate mediaIds"
        }

        val imageIdsByReference = mutableMapOf<Long, Long>()
        fun registerReference(reference: Long, databaseId: Long) {
            val previous = imageIdsByReference.put(reference, databaseId)
            require(previous == null || previous == databaseId) {
                "Ambiguous child imageId reference: $reference"
            }
        }

        for (image in images) {
            val existingId = imageIdByMediaId(image.mediaId)
            val databaseId = if (existingId == null) {
                insertImage(image.copy(id = 0L))
            } else {
                deleteChildren(existingId)
                updateImage(image.copy(id = existingId))
                existingId
            }

            registerReference(image.mediaId, databaseId)
            if (image.id != 0L) registerReference(image.id, databaseId)
            if (existingId != null) registerReference(existingId, databaseId)
            // A single-image batch can conveniently use 0 as the generated-id placeholder.
            if (images.size == 1 && image.id == 0L) registerReference(0L, databaseId)
        }

        fun databaseImageId(reference: Long): Long = checkNotNull(imageIdsByReference[reference]) {
            "Child row references imageId $reference outside this batch"
        }

        if (labels.isNotEmpty()) {
            insertLabels(labels.map { it.copy(imageId = databaseImageId(it.imageId)) })
        }
        if (albums.isNotEmpty()) {
            insertAlbums(albums.map { it.copy(imageId = databaseImageId(it.imageId)) })
        }
        if (faces.isNotEmpty()) {
            insertFaces(faces.map {
                it.copy(id = 0L, imageId = databaseImageId(it.imageId))
            })
        }
        if (buckets.isNotEmpty()) {
            insertBuckets(buckets.map { it.copy(imageId = databaseImageId(it.imageId)) })
        }
        upsertScanState(state)
    }

    @Query("SELECT id FROM images WHERE mediaId = :mediaId LIMIT 1")
    protected abstract suspend fun imageIdByMediaId(mediaId: Long): Long?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    protected abstract suspend fun insertImage(image: ImageEntity): Long

    @Update
    protected abstract suspend fun updateImage(image: ImageEntity)

    @Query("DELETE FROM labels WHERE imageId = :imageId")
    protected abstract suspend fun deleteLabels(imageId: Long)

    @Query("DELETE FROM album_entries WHERE imageId = :imageId")
    protected abstract suspend fun deleteAlbums(imageId: Long)

    @Query("DELETE FROM faces WHERE imageId = :imageId")
    protected abstract suspend fun deleteFaces(imageId: Long)

    @Query("DELETE FROM hash_buckets WHERE imageId = :imageId")
    protected abstract suspend fun deleteBuckets(imageId: Long)

    protected suspend fun deleteChildren(imageId: Long) {
        deleteLabels(imageId)
        deleteAlbums(imageId)
        deleteFaces(imageId)
        deleteBuckets(imageId)
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun insertLabels(labels: List<LabelEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun insertAlbums(albums: List<AlbumEntryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun insertFaces(faces: List<FaceEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun insertBuckets(buckets: List<HashBucketEntity>)

    @Upsert
    protected abstract suspend fun upsertScanState(state: ScanStateEntity)

    @Query("SELECT COALESCE(MAX(lastProcessedId), 0) FROM scan_state")
    abstract suspend fun lastProcessedId(): Long

    @Query("SELECT COUNT(*) FROM images")
    abstract fun count(): Flow<Int>

    @Query("SELECT COUNT(*) FROM images WHERE mediaId IN (:mediaIds)")
    abstract suspend fun countExistingMediaIds(mediaIds: List<Long>): Int

    @Query("SELECT id, dHash, pHash, blurScore, dateAdded, dateTaken FROM images")
    abstract suspend fun hashRows(): List<HashRow>

    @Query("SELECT * FROM images WHERE blurScore < :threshold ORDER BY blurScore ASC")
    abstract fun lowBlur(threshold: Double): Flow<List<ImageEntity>>

    @Query("SELECT * FROM images WHERE ocrText LIKE '%' || :q || '%'")
    abstract fun searchOcr(q: String): Flow<List<ImageEntity>>

    @RawQuery(observedEntities = [ImageEntity::class, LabelEntity::class])
    abstract fun searchByTag(query: SupportSQLiteQuery): Flow<List<ImageEntity>>

    @Query("SELECT * FROM images WHERE id IN (:ids)")
    abstract suspend fun imagesByIds(ids: List<Long>): List<ImageEntity>

    @Query("SELECT * FROM images WHERE id = :id LIMIT 1")
    abstract suspend fun imageById(id: Long): ImageEntity?

    @Query("SELECT * FROM images ORDER BY dateAdded DESC")
    abstract fun allImages(): Flow<List<ImageEntity>>

    @Query("DELETE FROM images WHERE mediaId IN (:ids)")
    abstract suspend fun deleteByMediaIds(ids: List<Long>)

    @Query("DELETE FROM images")
    abstract suspend fun deleteAll()

    @Query("SELECT mediaId FROM images ORDER BY mediaId ASC")
    abstract suspend fun allMediaIds(): List<Long>
}

@Dao
interface LabelDao {
    @Query(
        """SELECT DISTINCT images.* FROM images
            INNER JOIN labels ON labels.imageId = images.id
            WHERE labels.label LIKE :q || '%'
            ORDER BY images.dateAdded DESC"""
    )
    fun searchPrefix(q: String): Flow<List<ImageEntity>>

    @Query("SELECT DISTINCT label FROM labels WHERE label LIKE :prefix || '%' ORDER BY label ASC LIMIT 8")
    fun suggestions(prefix: String): Flow<List<String>>

    @Query("SELECT * FROM labels WHERE imageId = :imageId ORDER BY confidence DESC")
    suspend fun labelsFor(imageId: Long): List<LabelEntity>

    @Query("SELECT * FROM labels WHERE imageId IN (:imageIds) ORDER BY confidence DESC")
    suspend fun labelsForImages(imageIds: List<Long>): List<LabelEntity>
}

@Dao
interface AlbumDao {
    @Query(
        """SELECT images.* FROM images
            INNER JOIN album_entries ON album_entries.imageId = images.id
            WHERE album_entries.album = :type
            ORDER BY images.dateAdded DESC"""
    )
    fun album(type: AlbumType): Flow<List<ImageEntity>>

    @Query(
        """SELECT
                album_entries.album AS type,
                COUNT(*) AS count,
                (
                    SELECT images.uri FROM images
                    INNER JOIN album_entries AS cover_entries
                        ON cover_entries.imageId = images.id
                    WHERE cover_entries.album = album_entries.album
                    ORDER BY images.dateAdded DESC
                    LIMIT 1
                ) AS coverUri
            FROM album_entries
            GROUP BY album_entries.album"""
    )
    fun albumSummaries(): Flow<List<AlbumSummary>>
}

@Dao
interface FaceDao {
    @Query("SELECT * FROM images WHERE faceCount > 0 ORDER BY dateAdded DESC")
    fun imagesWithFaces(): Flow<List<ImageEntity>>

    @Query("SELECT * FROM faces WHERE imageId = :imageId ORDER BY id ASC")
    suspend fun facesFor(imageId: Long): List<FaceEntity>

    @Query("SELECT * FROM faces WHERE imageId IN (:imageIds) ORDER BY id ASC")
    suspend fun facesForImages(imageIds: List<Long>): List<FaceEntity>
}

@Dao
interface ScanStateDao {
    @Query("SELECT * FROM scan_state LIMIT 1")
    fun observe(): Flow<ScanStateEntity?>

    @Upsert
    suspend fun upsert(state: ScanStateEntity)

    @Query("DELETE FROM pipeline_stats")
    suspend fun deletePipelineStats()
}

@Dao
interface SkippedDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: SkippedImageEntity)

    suspend fun insert(mediaId: Long, reason: String) = insert(
        SkippedImageEntity(mediaId = mediaId, reason = reason, at = System.currentTimeMillis())
    )

    @Query("SELECT COUNT(*) FROM skipped_images")
    fun count(): Flow<Int>

    @Query("DELETE FROM skipped_images")
    suspend fun deleteAll()
}
