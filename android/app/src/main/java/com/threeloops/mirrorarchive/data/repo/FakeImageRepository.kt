package com.threeloops.mirrorarchive.data.repo

import com.threeloops.mirrorarchive.contracts.AlbumSummary
import com.threeloops.mirrorarchive.contracts.AlbumType
import com.threeloops.mirrorarchive.contracts.FaceInfo
import com.threeloops.mirrorarchive.contracts.HashRow
import com.threeloops.mirrorarchive.contracts.ImageItem
import com.threeloops.mirrorarchive.contracts.IndexedImage
import com.threeloops.mirrorarchive.contracts.ScanPhase
import com.threeloops.mirrorarchive.contracts.ScanProgress
import com.threeloops.mirrorarchive.contracts.ScanState
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

    private val fakeItems = listOf(
        ImageItem(
            id = 1L,
            mediaId = 101L,
            uri = "content://media/external/images/media/101",
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
            uri = "content://media/external/images/media/102",
            dateAdded = 1700001000L,
            dateTaken = 1700001000000L,
            sizeBytes = 3_120_000L,
            width = 1920,
            height = 1080,
            blurScore = 88.0,
            faceCount = 1
        ),
        ImageItem(
            id = 3L,
            mediaId = 103L,
            uri = "content://media/external/images/media/103",
            dateAdded = 1700002000L,
            dateTaken = 1700002000000L,
            sizeBytes = 1_850_000L,
            width = 1080,
            height = 1920,
            blurScore = 160.0,
            faceCount = 2
        ),
        ImageItem(
            id = 4L,
            mediaId = 104L,
            uri = "content://media/external/images/media/104",
            dateAdded = 1700003000L,
            dateTaken = 1700003000000L,
            sizeBytes = 4_200_000L,
            width = 3840,
            height = 2160,
            blurScore = 32.0,
            faceCount = 0
        ),
        ImageItem(
            id = 5L,
            mediaId = 105L,
            uri = "content://media/external/images/media/105",
            dateAdded = 1700004000L,
            dateTaken = 1700004000000L,
            sizeBytes = 2_890_000L,
            width = 1920,
            height = 1080,
            blurScore = 110.0,
            faceCount = 3
        )
    )

    private val _itemsFlow = MutableStateFlow(fakeItems)

    private val _summariesFlow = MutableStateFlow(
        listOf(
            AlbumSummary(type = AlbumType.RECEIPTS, count = 14, coverUri = "content://media/external/images/media/101"),
            AlbumSummary(type = AlbumType.SCREENSHOTS, count = 28, coverUri = "content://media/external/images/media/102"),
            AlbumSummary(type = AlbumType.DOCUMENTS, count = 9, coverUri = "content://media/external/images/media/103"),
            AlbumSummary(type = AlbumType.FOOD, count = 42, coverUri = "content://media/external/images/media/104"),
            AlbumSummary(type = AlbumType.PEOPLE, count = 35, coverUri = "content://media/external/images/media/105")
        )
    )

    private val _scanStateFlow = MutableStateFlow<ScanState>(
        ScanProgress(phase = ScanPhase.COMPLETE, done = 128, total = 128)
    )

    private var lastId: Long = 105L

    override suspend fun saveBatch(items: List<IndexedImage>, checkpointId: Long) {
        if (checkpointId > 0) {
            lastId = checkpointId
        }
    }

    override fun searchByTag(q: String): Flow<List<ImageItem>> {
        return _itemsFlow.map { list ->
            if (q.isBlank()) list
            else list.filter { it.uri.contains(q, ignoreCase = true) }
        }
    }

    override fun album(type: AlbumType): Flow<List<ImageItem>> {
        return _itemsFlow
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
        return _itemsFlow.map { list ->
            if (q.isBlank()) list
            else list.filter { it.uri.contains(q, ignoreCase = true) }
        }
    }

    override fun suggestions(prefix: String): Flow<List<String>> {
        val sampleTags = listOf("receipt", "invoice", "screenshot", "food", "dinner", "people", "portrait")
        return MutableStateFlow(sampleTags.filter { it.startsWith(prefix, ignoreCase = true) })
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

    override fun facesFor(imageId: Long): List<FaceInfo> = emptyList()
}
