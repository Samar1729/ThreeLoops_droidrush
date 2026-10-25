package com.threeloops.mirrorarchive.data.repo

import android.graphics.Rect
import com.threeloops.mirrorarchive.contracts.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * In-memory fake implementation of [ImageRepository] with live Flows for UI prototyping and testing.
 */
@Singleton
class FakeImageRepository @Inject constructor() : ImageRepository {

    private data class Metadata(
        val labels: List<String>,
        val ocr: String,
        val albums: Set<AlbumType>,
        val faces: List<FaceInfo> = emptyList()
    )

    private val fakeItems = listOf(
        ImageItem(
            id = 1L,
            mediaId = 101L,
            uri = "https://images.unsplash.com/photo-1554415707-9e4c018a482d?w=800",
            dateAdded = 1700000000L,
            dateTaken = 1700000000000L,
            sizeBytes = 2_450_000L,
            width = 1920,
            height = 1080,
            blurScore = 145.0,
            faceCount = 0
        ),
        ImageItem(
            id = 2L,
            mediaId = 102L,
            uri = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=800",
            dateAdded = 1700001000L,
            dateTaken = 1700001000000L,
            sizeBytes = 3_120_000L,
            width = 1920,
            height = 1080,
            blurScore = 88.0,
            faceCount = 0
        ),
        ImageItem(
            id = 3L,
            mediaId = 103L,
            uri = "https://images.unsplash.com/photo-1450133064473-71024230f91b?w=800",
            dateAdded = 1700002000L,
            dateTaken = 1700002000000L,
            sizeBytes = 1_850_000L,
            width = 1080,
            height = 1920,
            blurScore = 160.0,
            faceCount = 0
        ),
        ImageItem(
            id = 4L,
            mediaId = 104L,
            uri = "https://images.unsplash.com/photo-1565299624946-b28f40a0ae38?w=800",
            dateAdded = 1700003000L,
            dateTaken = 1700003000000L,
            sizeBytes = 4_200_000L,
            width = 3840,
            height = 2160,
            blurScore = 175.0,
            faceCount = 0
        ),
        ImageItem(
            id = 5L,
            mediaId = 105L,
            uri = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=800",
            dateAdded = 1700004000L,
            dateTaken = 1700004000000L,
            sizeBytes = 2_890_000L,
            width = 1920,
            height = 1080,
            blurScore = 110.0,
            faceCount = 1
        ),
        ImageItem(
            id = 6L,
            mediaId = 106L,
            uri = "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=800",
            dateAdded = 1700005000L,
            dateTaken = 1700005000000L,
            sizeBytes = 2_100_000L,
            width = 1080,
            height = 1920,
            blurScore = 135.0,
            faceCount = 1
        ),
        ImageItem(
            id = 7L,
            mediaId = 107L,
            uri = "https://images.unsplash.com/photo-1504674900247-0877df9cc836?w=800",
            dateAdded = 1700006000L,
            dateTaken = 1700006000000L,
            sizeBytes = 3_450_000L,
            width = 2560,
            height = 1440,
            blurScore = 190.0,
            faceCount = 0
        )
    )

    private val itemMetadata = mapOf(
        1L to Metadata(
            labels = listOf("receipt", "invoice", "paper", "text", "bill", "tax"),
            ocr = "Coffee Shop Receipt. Subtotal: $14.50. Tax: $1.20. Total: $15.70. Paid via Card.",
            albums = setOf(AlbumType.RECEIPTS)
        ),
        2L to Metadata(
            labels = listOf("screenshot", "display", "text", "software", "ui", "capture"),
            ocr = "Settings > Display & Brightness > Dark Mode Scheduled",
            albums = setOf(AlbumType.SCREENSHOTS)
        ),
        3L to Metadata(
            labels = listOf("document", "paper", "contract", "text", "page", "agreement"),
            ocr = "Independent Contractor Agreement. Section 4: Intellectual Property and Zero Cloud Retention.",
            albums = setOf(AlbumType.DOCUMENTS)
        ),
        4L to Metadata(
            labels = listOf("food", "dish", "pizza", "meal", "dinner", "cuisine", "lunch"),
            ocr = "",
            albums = setOf(AlbumType.FOOD)
        ),
        5L to Metadata(
            labels = listOf("person", "portrait", "face", "people", "human", "smile"),
            ocr = "",
            albums = setOf(AlbumType.PEOPLE),
            faces = listOf(FaceInfo(Rect(400, 200, 800, 600), smileProb = 0.95f, eyesOpenProb = 0.98f))
        ),
        6L to Metadata(
            labels = listOf("person", "portrait", "face", "people", "selfie"),
            ocr = "",
            albums = setOf(AlbumType.PEOPLE),
            faces = listOf(FaceInfo(Rect(350, 150, 750, 550), smileProb = 0.88f, eyesOpenProb = 0.92f))
        ),
        7L to Metadata(
            labels = listOf("food", "dish", "cuisine", "dinner", "salad", "meal"),
            ocr = "Chef's Table Special Menu",
            albums = setOf(AlbumType.FOOD)
        )
    )

    private val _itemsFlow = MutableStateFlow(fakeItems)

    private val _summariesFlow = MutableStateFlow(
        listOf(
            AlbumSummary(type = AlbumType.RECEIPTS, count = 1, coverUri = "https://images.unsplash.com/photo-1554415707-9e4c018a482d?w=800"),
            AlbumSummary(type = AlbumType.SCREENSHOTS, count = 1, coverUri = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=800"),
            AlbumSummary(type = AlbumType.DOCUMENTS, count = 1, coverUri = "https://images.unsplash.com/photo-1450133064473-71024230f91b?w=800"),
            AlbumSummary(type = AlbumType.FOOD, count = 2, coverUri = "https://images.unsplash.com/photo-1565299624946-b28f40a0ae38?w=800"),
            AlbumSummary(type = AlbumType.PEOPLE, count = 2, coverUri = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=800")
        )
    )

    private val _scanStateFlow = MutableStateFlow<ScanState>(
        ScanProgress(phase = ScanPhase.COMPLETE, done = 128, total = 128)
    )

    private var lastId: Long = 107L

    override suspend fun saveBatch(items: List<IndexedImage>, checkpointId: Long) {
        if (checkpointId > 0) {
            lastId = checkpointId
        }
    }

    override fun searchByTag(q: String): Flow<List<ImageItem>> {
        val query = q.trim().lowercase()
        return _itemsFlow.map { list ->
            if (query.isBlank()) list
            else list.filter { item ->
                val meta = itemMetadata[item.id]
                val labelMatch = meta?.labels?.any { it.contains(query) } == true
                val albumMatch = meta?.albums?.any { it.name.lowercase().contains(query) } == true
                labelMatch || albumMatch || item.uri.lowercase().contains(query)
            }
        }
    }

    override fun album(type: AlbumType): Flow<List<ImageItem>> {
        return _itemsFlow.map { list ->
            list.filter { item ->
                val meta = itemMetadata[item.id]
                meta?.albums?.contains(type) == true
            }
        }
    }

    override fun duplicateCandidates(): List<HashRow> {
        return _itemsFlow.value.map {
            HashRow(
                id = it.id,
                dHash = 0x1234567890ABCDEFL xor it.id,
                pHash = 0x7EDCBA0987654321L xor it.id,
                blurScore = it.blurScore,
                dateAdded = it.dateAdded,
                dateTaken = it.dateTaken
            )
        }
    }

    override fun albumSummaries(): Flow<List<AlbumSummary>> = _summariesFlow.asStateFlow()

    override fun searchByText(q: String): Flow<List<ImageItem>> {
        val query = q.trim().lowercase()
        return _itemsFlow.map { list ->
            if (query.isBlank()) list
            else list.filter { item ->
                val meta = itemMetadata[item.id]
                meta?.ocr?.lowercase()?.contains(query) == true
            }
        }
    }

    override fun suggestions(prefix: String): Flow<List<String>> {
        val sampleTags = listOf("receipt", "invoice", "screenshot", "food", "dinner", "people", "portrait", "document", "contract")
        return MutableStateFlow(
            if (prefix.isBlank()) sampleTags
            else sampleTags.filter { it.startsWith(prefix, ignoreCase = true) }
        )
    }

    override fun peopleImages(): Flow<List<ImageItem>> {
        return _itemsFlow.map { list -> list.filter { it.faceCount > 0 } }
    }

    override fun lowBlur(threshold: Double): Flow<List<ImageItem>> {
        return _itemsFlow.map { list -> list.filter { it.blurScore < threshold } }
    }

    override fun items(ids: List<Long>): List<ImageItem> {
        val idSet = ids.toSet()
        return _itemsFlow.value.filter { it.id in idSet }
    }

    override suspend fun deleteRows(mediaIds: List<Long>) {
        val mediaIdSet = mediaIds.toSet()
        _itemsFlow.value = _itemsFlow.value.filterNot { it.mediaId in mediaIdSet }
    }

    override fun scanState(): Flow<ScanState> = _scanStateFlow.asStateFlow()

    override suspend fun lastProcessedId(): Long = lastId

    override fun facesFor(imageId: Long): List<FaceInfo> {
        return itemMetadata[imageId]?.faces ?: emptyList()
    }
}
