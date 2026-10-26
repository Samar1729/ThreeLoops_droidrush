package com.threeloops.mirrorarchive.data.repo

import android.graphics.Rect
import androidx.sqlite.db.SimpleSQLiteQuery
import com.threeloops.mirrorarchive.albums.AlbumRules
import com.threeloops.mirrorarchive.contracts.AlbumSummary
import com.threeloops.mirrorarchive.contracts.AlbumType
import com.threeloops.mirrorarchive.contracts.FaceInfo
import com.threeloops.mirrorarchive.contracts.HashRow
import com.threeloops.mirrorarchive.contracts.ImageItem
import com.threeloops.mirrorarchive.contracts.IndexedImage
import com.threeloops.mirrorarchive.contracts.ScanPhase
import com.threeloops.mirrorarchive.contracts.ScanProgress
import com.threeloops.mirrorarchive.contracts.ScanState
import com.threeloops.mirrorarchive.data.db.AlbumEntryEntity
import com.threeloops.mirrorarchive.data.db.AppDatabase
import com.threeloops.mirrorarchive.data.db.FaceEntity
import com.threeloops.mirrorarchive.data.db.HashBucketEntity
import com.threeloops.mirrorarchive.data.db.ImageEntity
import com.threeloops.mirrorarchive.data.db.LabelEntity
import com.threeloops.mirrorarchive.data.db.ScanStateEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject

const val PIPELINE_VERSION = 1

/** Room-backed implementation of the image repository contract. */
class RoomImageRepository @Inject constructor(
    private val database: AppDatabase
) : ImageRepository {
    private val imageDao = database.imageDao()
    private val labelDao = database.labelDao()
    private val albumDao = database.albumDao()
    private val faceDao = database.faceDao()
    private val scanStateDao = database.scanStateDao()

    override suspend fun saveBatch(items: List<IndexedImage>, checkpointId: Long) =
        withContext(Dispatchers.IO) {
            val now = System.currentTimeMillis()
            val previousState = scanStateDao.observe().first()
            val storedImageCount = imageDao.count().first().toLong()
            val existingBatchRows = items.map { it.mediaId }.distinct().chunked(SQLITE_ID_CHUNK_SIZE)
                .sumOf { chunk -> if (chunk.isEmpty()) 0 else imageDao.countExistingMediaIds(chunk) }
            val addedRows = (items.size - existingBatchRows).coerceAtLeast(0).toLong()
            val totalCount = maxOf(previousState?.totalCount ?: 0L, storedImageCount + addedRows)
            val processedCount = ((previousState?.processedCount ?: 0L) + items.size)
                .coerceAtMost(totalCount)
            val lastGeneration = maxOf(
                previousState?.lastGeneration ?: 0L,
                items.maxOfOrNull { it.generationModified } ?: 0L
            )

            val images = items.map { it.toImageEntity(indexedAt = now) }
            val labels = items.flatMap { item ->
                item.labels.asSequence()
                    .map { it.text.trim().lowercase(Locale.ROOT) to it.confidence }
                    .filter { (label, _) -> label.isNotEmpty() }
                    .groupBy({ it.first }, { it.second })
                    .map { (label, confidences) ->
                        LabelEntity(
                            imageId = item.mediaId,
                            label = label,
                            confidence = confidences.maxOrNull() ?: 0f
                        )
                    }
            }
            val albums = items.flatMap { item ->
                item.albums.map { album -> AlbumEntryEntity(item.mediaId, album) }
            }
            val faces = items.flatMap { item ->
                item.faces.map { face ->
                    FaceEntity(
                        imageId = item.mediaId,
                        left = face.box.left,
                        top = face.box.top,
                        right = face.box.right,
                        bottom = face.box.bottom,
                        smileProb = face.smileProb,
                        eyesOpenProb = face.eyesOpenProb,
                        clusterId = null
                    )
                }
            }
            val buckets = items.flatMap { item ->
                (0..7).map { chunkIdx ->
                    HashBucketEntity(
                        imageId = item.mediaId,
                        chunkIdx = chunkIdx,
                        chunkVal = ((item.dHash ushr (chunkIdx * 8)) and 0xFFL).toInt()
                    )
                }
            }
            val state = ScanStateEntity(
                lastProcessedId = checkpointId,
                lastGeneration = lastGeneration,
                totalCount = totalCount,
                processedCount = processedCount,
                phase = previousState?.phase ?: ScanPhase.SCANNING,
                updatedAt = now
            )

            imageDao.saveBatchTx(images, labels, albums, faces, buckets, state)
        }

    override fun searchByTag(q: String): Flow<List<ImageItem>> {
        val tokens = q.trim().lowercase(Locale.ROOT).split(WHITESPACE).filter(String::isNotEmpty)
        if (tokens.isEmpty()) return flowOf(emptyList())

        val args = mutableListOf<Any>()
        val conditions = tokens.map { token ->
            val expansions = (listOf(token) + AlbumRules.synonyms[token].orEmpty())
                .map { it.lowercase(Locale.ROOT) }
                .distinct()
            val alternatives = expansions.joinToString(" OR ") {
                args += "${it.escapeLike()}%"
                "labels.label LIKE ? ESCAPE '!'"
            }
            "EXISTS (SELECT 1 FROM labels WHERE labels.imageId = images.id AND ($alternatives))"
        }
        val sql = "SELECT images.* FROM images WHERE ${conditions.joinToString(" AND ")} " +
            "ORDER BY images.dateAdded DESC"
        return imageDao.searchByTag(SimpleSQLiteQuery(sql, args.toTypedArray()))
            .map { rows -> rows.map(ImageEntity::toImageItem) }
            .flowOn(Dispatchers.IO)
    }

    override fun album(type: AlbumType): Flow<List<ImageItem>> = albumDao.album(type)
        .map { rows -> rows.map(ImageEntity::toImageItem) }
        .flowOn(Dispatchers.IO)

    override fun duplicateCandidates(): List<HashRow> = runBlocking(Dispatchers.IO) {
        imageDao.hashRows()
    }

    override fun albumSummaries(): Flow<List<AlbumSummary>> = albumDao.albumSummaries()
        .flowOn(Dispatchers.IO)

    override fun searchByText(q: String): Flow<List<ImageItem>> = imageDao.searchOcr(q)
        .map { rows -> rows.map(ImageEntity::toImageItem) }
        .flowOn(Dispatchers.IO)

    override fun suggestions(prefix: String): Flow<List<String>> = labelDao
        .suggestions(prefix.trim().lowercase(Locale.ROOT))
        .flowOn(Dispatchers.IO)

    override fun peopleImages(): Flow<List<ImageItem>> = faceDao.imagesWithFaces()
        .map { rows -> rows.map(ImageEntity::toImageItem) }
        .flowOn(Dispatchers.IO)

    override fun lowBlur(threshold: Double): Flow<List<ImageItem>> = imageDao.lowBlur(threshold)
        .map { rows -> rows.map(ImageEntity::toImageItem) }
        .flowOn(Dispatchers.IO)

    override fun items(ids: List<Long>): List<ImageItem> = runBlocking(Dispatchers.IO) {
        if (ids.isEmpty()) return@runBlocking emptyList()
        val byId = ids.distinct().chunked(SQLITE_ID_CHUNK_SIZE)
            .flatMap { imageDao.imagesByIds(it) }
            .associateBy { it.id }
        ids.mapNotNull { id -> byId[id]?.toImageItem() }
    }

    override suspend fun deleteRows(mediaIds: List<Long>) = withContext(Dispatchers.IO) {
        mediaIds.distinct().chunked(SQLITE_ID_CHUNK_SIZE).forEach { chunk ->
            if (chunk.isNotEmpty()) imageDao.deleteByMediaIds(chunk)
        }
    }

    override fun scanState(): Flow<ScanState> = scanStateDao.observe()
        .map { state ->
            ScanProgress(
                phase = state?.phase ?: ScanPhase.IDLE,
                done = (state?.processedCount ?: 0L).coerceIn(0L, Int.MAX_VALUE.toLong()).toInt(),
                total = (state?.totalCount ?: 0L).coerceIn(0L, Int.MAX_VALUE.toLong()).toInt()
            )
        }
        .flowOn(Dispatchers.IO)

    override suspend fun lastProcessedId(): Long = withContext(Dispatchers.IO) {
        imageDao.lastProcessedId()
    }

    override fun facesFor(imageId: Long): List<FaceInfo> = runBlocking(Dispatchers.IO) {
        faceDao.facesFor(imageId).map { face ->
            FaceInfo(
                box = Rect(face.left, face.top, face.right, face.bottom),
                smileProb = face.smileProb,
                eyesOpenProb = face.eyesOpenProb
            )
        }
    }

    private fun IndexedImage.toImageEntity(indexedAt: Long) = ImageEntity(
        mediaId = mediaId,
        uri = uri,
        displayName = displayName,
        relativePath = relativePath,
        // IndexedImage has no dateAdded field in the frozen contract. Use capture time in
        // seconds when present, and otherwise the time this row was indexed.
        dateAdded = dateTaken?.div(1_000L) ?: (indexedAt / 1_000L),
        dateTaken = dateTaken,
        generationModified = generationModified,
        width = width,
        height = height,
        sizeBytes = sizeBytes,
        dHash = dHash,
        pHash = pHash,
        blurScore = blurScore,
        ocrText = ocrText,
        faceCount = faceCount,
        indexedAt = indexedAt,
        pipelineVersion = PIPELINE_VERSION
    )

    private fun String.escapeLike(): String = replace("!", "!!")
        .replace("%", "!%")
        .replace("_", "!_")

    private companion object {
        const val SQLITE_ID_CHUNK_SIZE = 500
        val WHITESPACE = Regex("\\s+")
    }
}

/** The single ImageEntity -> repository DTO conversion point. */
private fun ImageEntity.toImageItem() = ImageItem(
    id = id,
    mediaId = mediaId,
    uri = uri,
    dateAdded = dateAdded,
    dateTaken = dateTaken,
    sizeBytes = sizeBytes,
    width = width,
    height = height,
    blurScore = blurScore,
    faceCount = faceCount
)
