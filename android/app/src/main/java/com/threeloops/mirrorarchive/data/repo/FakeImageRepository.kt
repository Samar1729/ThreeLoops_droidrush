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
 * In-memory repository with a rich, realistic photo archive for demonstrating all ML Kit models,
 * smart album categorization, OCR text search, and facial analysis.
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
        // RECEIPTS & INVOICES (5 items)
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
            uri = "https://images.unsplash.com/photo-1556742049-0a67c5574f73?w=800",
            dateAdded = 1700000500L,
            dateTaken = 1700000500000L,
            sizeBytes = 1_980_000L,
            width = 1920,
            height = 1080,
            blurScore = 160.0,
            faceCount = 0
        ),
        ImageItem(
            id = 3L,
            mediaId = 103L,
            uri = "https://images.unsplash.com/photo-1554224155-8d04cb21cd6c?w=800",
            dateAdded = 1700001000L,
            dateTaken = 1700001000000L,
            sizeBytes = 3_120_000L,
            width = 1080,
            height = 1920,
            blurScore = 138.0,
            faceCount = 0
        ),
        ImageItem(
            id = 4L,
            mediaId = 104L,
            uri = "https://images.unsplash.com/photo-1586528116311-ad8dd3c8310d?w=800",
            dateAdded = 1700001500L,
            dateTaken = 1700001500000L,
            sizeBytes = 2_200_000L,
            width = 1920,
            height = 1080,
            blurScore = 152.0,
            faceCount = 0
        ),
        ImageItem(
            id = 5L,
            mediaId = 105L,
            uri = "https://images.unsplash.com/photo-1554224154-26032ffc0d07?w=800",
            dateAdded = 1700002000L,
            dateTaken = 1700002000000L,
            sizeBytes = 2_750_000L,
            width = 1920,
            height = 1080,
            blurScore = 140.0,
            faceCount = 0
        ),

        // SCREENSHOTS (5 items)
        ImageItem(
            id = 6L,
            mediaId = 106L,
            uri = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=800",
            dateAdded = 1700002500L,
            dateTaken = 1700002500000L,
            sizeBytes = 1_850_000L,
            width = 1080,
            height = 1920,
            blurScore = 195.0,
            faceCount = 0
        ),
        ImageItem(
            id = 7L,
            mediaId = 107L,
            uri = "https://images.unsplash.com/photo-1607604276583-eef5d076aa5f?w=800",
            dateAdded = 1700003000L,
            dateTaken = 1700003000000L,
            sizeBytes = 1_920_000L,
            width = 1080,
            height = 1920,
            blurScore = 188.0,
            faceCount = 0
        ),
        ImageItem(
            id = 8L,
            mediaId = 108L,
            uri = "https://images.unsplash.com/photo-1551288049-bebda4e38f71?w=800",
            dateAdded = 1700003500L,
            dateTaken = 1700003500000L,
            sizeBytes = 2_150_000L,
            width = 1920,
            height = 1080,
            blurScore = 178.0,
            faceCount = 0
        ),
        ImageItem(
            id = 9L,
            mediaId = 109L,
            uri = "https://images.unsplash.com/photo-1526374965328-7f61d4dc18c5?w=800",
            dateAdded = 1700004000L,
            dateTaken = 1700004000000L,
            sizeBytes = 2_400_000L,
            width = 1920,
            height = 1080,
            blurScore = 192.0,
            faceCount = 0
        ),
        ImageItem(
            id = 10L,
            mediaId = 110L,
            uri = "https://images.unsplash.com/photo-1555421689-491a97ff2040?w=800",
            dateAdded = 1700004500L,
            dateTaken = 1700004500000L,
            sizeBytes = 1_700_000L,
            width = 1080,
            height = 1920,
            blurScore = 185.0,
            faceCount = 0
        ),

        // DOCUMENTS (5 items)
        ImageItem(
            id = 11L,
            mediaId = 111L,
            uri = "https://images.unsplash.com/photo-1450133064473-71024230f91b?w=800",
            dateAdded = 1700005000L,
            dateTaken = 1700005000000L,
            sizeBytes = 3_100_000L,
            width = 1920,
            height = 1080,
            blurScore = 165.0,
            faceCount = 0
        ),
        ImageItem(
            id = 12L,
            mediaId = 112L,
            uri = "https://images.unsplash.com/photo-1589829545856-d10d557cf95f?w=800",
            dateAdded = 1700005500L,
            dateTaken = 1700005500000L,
            sizeBytes = 2_890_000L,
            width = 1920,
            height = 1080,
            blurScore = 158.0,
            faceCount = 0
        ),
        ImageItem(
            id = 13L,
            mediaId = 113L,
            uri = "https://images.unsplash.com/photo-1455390582262-044cdead277a?w=800",
            dateAdded = 1700006000L,
            dateTaken = 1700006000000L,
            sizeBytes = 2_600_000L,
            width = 1920,
            height = 1080,
            blurScore = 142.0,
            faceCount = 0
        ),
        ImageItem(
            id = 14L,
            mediaId = 114L,
            uri = "https://images.unsplash.com/photo-1568667256549-094345857637?w=800",
            dateAdded = 1700006500L,
            dateTaken = 1700006500000L,
            sizeBytes = 3_400_000L,
            width = 1920,
            height = 1080,
            blurScore = 170.0,
            faceCount = 0
        ),
        ImageItem(
            id = 15L,
            mediaId = 115L,
            uri = "https://images.unsplash.com/photo-1517842645767-c639042777db?w=800",
            dateAdded = 1700007000L,
            dateTaken = 1700007000000L,
            sizeBytes = 2_300_000L,
            width = 1920,
            height = 1080,
            blurScore = 155.0,
            faceCount = 0
        ),

        // FOOD & CUISINE (5 items)
        ImageItem(
            id = 16L,
            mediaId = 116L,
            uri = "https://images.unsplash.com/photo-1565299624946-b28f40a0ae38?w=800",
            dateAdded = 1700007500L,
            dateTaken = 1700007500000L,
            sizeBytes = 4_200_000L,
            width = 3840,
            height = 2160,
            blurScore = 180.0,
            faceCount = 0
        ),
        ImageItem(
            id = 17L,
            mediaId = 117L,
            uri = "https://images.unsplash.com/photo-1504674900247-0877df9cc836?w=800",
            dateAdded = 1700008000L,
            dateTaken = 1700008000000L,
            sizeBytes = 3_450_000L,
            width = 2560,
            height = 1440,
            blurScore = 190.0,
            faceCount = 0
        ),
        ImageItem(
            id = 18L,
            mediaId = 118L,
            uri = "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=800",
            dateAdded = 1700008500L,
            dateTaken = 1700008500000L,
            sizeBytes = 3_800_000L,
            width = 1920,
            height = 1080,
            blurScore = 175.0,
            faceCount = 0
        ),
        ImageItem(
            id = 19L,
            mediaId = 119L,
            uri = "https://images.unsplash.com/photo-1569718212165-3a8278d5f624?w=800",
            dateAdded = 1700009000L,
            dateTaken = 1700009000000L,
            sizeBytes = 3_600_000L,
            width = 1920,
            height = 1080,
            blurScore = 168.0,
            faceCount = 0
        ),
        ImageItem(
            id = 20L,
            mediaId = 120L,
            uri = "https://images.unsplash.com/photo-1514432324607-a09d9b4aefdd?w=800",
            dateAdded = 1700009500L,
            dateTaken = 1700009500000L,
            sizeBytes = 2_800_000L,
            width = 1920,
            height = 1080,
            blurScore = 182.0,
            faceCount = 0
        ),

        // PEOPLE & FACES (6 items with varied facial metrics & smile/eyes)
        ImageItem(
            id = 21L,
            mediaId = 121L,
            uri = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=800",
            dateAdded = 1700010000L,
            dateTaken = 1700010000000L,
            sizeBytes = 2_890_000L,
            width = 1920,
            height = 1080,
            blurScore = 160.0,
            faceCount = 1
        ),
        ImageItem(
            id = 22L,
            mediaId = 122L,
            uri = "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=800",
            dateAdded = 1700010500L,
            dateTaken = 1700010500000L,
            sizeBytes = 2_100_000L,
            width = 1080,
            height = 1920,
            blurScore = 172.0,
            faceCount = 1
        ),
        ImageItem(
            id = 23L,
            mediaId = 123L,
            uri = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=800",
            dateAdded = 1700011000L,
            dateTaken = 1700011000000L,
            sizeBytes = 2_650_000L,
            width = 1920,
            height = 1080,
            blurScore = 168.0,
            faceCount = 1
        ),
        ImageItem(
            id = 24L,
            mediaId = 124L,
            uri = "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=800",
            dateAdded = 1700011500L,
            dateTaken = 1700011500000L,
            sizeBytes = 2_450_000L,
            width = 1080,
            height = 1920,
            blurScore = 155.0,
            faceCount = 1
        ),
        ImageItem(
            id = 25L,
            mediaId = 125L,
            uri = "https://images.unsplash.com/photo-1492562080023-ab3db95bfbce?w=800",
            dateAdded = 1700012000L,
            dateTaken = 1700012000000L,
            sizeBytes = 2_900_000L,
            width = 1920,
            height = 1080,
            blurScore = 164.0,
            faceCount = 1
        ),
        ImageItem(
            id = 26L,
            mediaId = 126L,
            uri = "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?w=800",
            dateAdded = 1700012500L,
            dateTaken = 1700012500000L,
            sizeBytes = 3_200_000L,
            width = 1920,
            height = 1080,
            blurScore = 178.0,
            faceCount = 1
        )
    )

    private val itemMetadata = mapOf(
        // Receipts
        1L to Metadata(
            labels = listOf("receipt", "invoice", "paper", "text", "bill", "tax"),
            ocr = "Coffee Shop Receipt. Subtotal: $14.50. Tax: $1.20. Total: $15.70. Paid via Card.",
            albums = setOf(AlbumType.RECEIPTS)
        ),
        2L to Metadata(
            labels = listOf("receipt", "store", "paper", "bill", "payment"),
            ocr = "Retail Store Invoice #4092. Total: $89.00. Tax: $7.12. Card Approved.",
            albums = setOf(AlbumType.RECEIPTS)
        ),
        3L to Metadata(
            labels = listOf("receipt", "tax", "accounting", "paper", "total"),
            ocr = "Supermarket Grocery Bill. Subtotal: $45.20. GST: $2.26. Total Amount: $47.46.",
            albums = setOf(AlbumType.RECEIPTS)
        ),
        4L to Metadata(
            labels = listOf("receipt", "fuel", "gas", "bill", "invoice"),
            ocr = "Gasoline Station Receipt. Gallons: 12.4. Price: $3.89. Total: $48.24.",
            albums = setOf(AlbumType.RECEIPTS)
        ),
        5L to Metadata(
            labels = listOf("receipt", "bill", "paper", "payment", "subtotal"),
            ocr = "Bistro Dinner. Subtotal: $64.00. Service Tax: $5.12. Total: $69.12. Thank you!",
            albums = setOf(AlbumType.RECEIPTS)
        ),

        // Screenshots
        6L to Metadata(
            labels = listOf("screenshot", "display", "text", "software", "ui", "capture"),
            ocr = "Settings > Display & Brightness > Dark Mode Scheduled",
            albums = setOf(AlbumType.SCREENSHOTS)
        ),
        7L to Metadata(
            labels = listOf("screenshot", "chat", "messenger", "conversation", "text"),
            ocr = "Hey Aditya, did you commit the on-device ML vision pipeline?",
            albums = setOf(AlbumType.SCREENSHOTS)
        ),
        8L to Metadata(
            labels = listOf("screenshot", "dashboard", "analytics", "graph", "data"),
            ocr = "Weekly Analytics: 99.8% on-device efficiency, 0 cloud requests",
            albums = setOf(AlbumType.SCREENSHOTS)
        ),
        9L to Metadata(
            labels = listOf("screenshot", "code", "terminal", "bash", "developer"),
            ocr = "git commit -m 'feat: clean apple monochrome UI' && git push",
            albums = setOf(AlbumType.SCREENSHOTS)
        ),
        10L to Metadata(
            labels = listOf("screenshot", "banking", "finance", "account", "balance"),
            ocr = "Current Account Balance: $4,850.20. Transfer Complete.",
            albums = setOf(AlbumType.SCREENSHOTS)
        ),

        // Documents
        11L to Metadata(
            labels = listOf("document", "paper", "contract", "text", "page", "agreement"),
            ocr = "Independent Contractor Agreement. Section 4: Zero Cloud Retention and Privacy Guarantees.",
            albums = setOf(AlbumType.DOCUMENTS)
        ),
        12L to Metadata(
            labels = listOf("document", "legal", "certificate", "paper", "form"),
            ocr = "Certificate of Authorization. Droidrush 2026 Problem Statement 06.",
            albums = setOf(AlbumType.DOCUMENTS)
        ),
        13L to Metadata(
            labels = listOf("document", "letter", "page", "text", "handwriting"),
            ocr = "Research Summary: On-Device Perceptual Hashing and Substring OCR Indexing.",
            albums = setOf(AlbumType.DOCUMENTS)
        ),
        14L to Metadata(
            labels = listOf("document", "paper", "contract", "page", "signature"),
            ocr = "Non-Disclosure Agreement signed by Team ThreeLoops on October 2026.",
            albums = setOf(AlbumType.DOCUMENTS)
        ),
        15L to Metadata(
            labels = listOf("document", "form", "report", "paper", "text"),
            ocr = "Medical Laboratory Diagnostic Report. All parameters within nominal reference ranges.",
            albums = setOf(AlbumType.DOCUMENTS)
        ),

        // Food
        16L to Metadata(
            labels = listOf("food", "dish", "pizza", "meal", "dinner", "cuisine", "lunch"),
            ocr = "",
            albums = setOf(AlbumType.FOOD)
        ),
        17L to Metadata(
            labels = listOf("food", "dish", "cuisine", "dinner", "salad", "meal", "healthy"),
            ocr = "Chef's Table Special Menu",
            albums = setOf(AlbumType.FOOD)
        ),
        18L to Metadata(
            labels = listOf("food", "burger", "fries", "fast food", "meal", "snack"),
            ocr = "Classic Cheeseburger with Garlic Aioli",
            albums = setOf(AlbumType.FOOD)
        ),
        19L to Metadata(
            labels = listOf("food", "ramen", "soup", "noodles", "asian cuisine", "dish"),
            ocr = "Tonkotsu Ramen Bowl",
            albums = setOf(AlbumType.FOOD)
        ),
        20L to Metadata(
            labels = listOf("food", "coffee", "cappuccino", "breakfast", "drink", "cup"),
            ocr = "Espresso Roast Coffee",
            albums = setOf(AlbumType.FOOD)
        ),

        // People & Faces
        21L to Metadata(
            labels = listOf("person", "portrait", "face", "people", "human", "smile"),
            ocr = "",
            albums = setOf(AlbumType.PEOPLE),
            faces = listOf(FaceInfo(Rect(400, 200, 800, 600), smileProb = 0.96f, eyesOpenProb = 0.98f))
        ),
        22L to Metadata(
            labels = listOf("person", "portrait", "face", "people", "selfie"),
            ocr = "",
            albums = setOf(AlbumType.PEOPLE),
            faces = listOf(FaceInfo(Rect(350, 150, 750, 550), smileProb = 0.88f, eyesOpenProb = 0.94f))
        ),
        23L to Metadata(
            labels = listOf("person", "human", "man", "portrait", "smile", "face"),
            ocr = "",
            albums = setOf(AlbumType.PEOPLE),
            faces = listOf(FaceInfo(Rect(420, 180, 820, 580), smileProb = 0.92f, eyesOpenProb = 0.97f))
        ),
        24L to Metadata(
            labels = listOf("person", "woman", "portrait", "smile", "happy"),
            ocr = "",
            albums = setOf(AlbumType.PEOPLE),
            faces = listOf(FaceInfo(Rect(380, 160, 760, 540), smileProb = 0.94f, eyesOpenProb = 0.96f))
        ),
        25L to Metadata(
            labels = listOf("person", "man", "face", "portrait", "candid"),
            ocr = "",
            albums = setOf(AlbumType.PEOPLE),
            faces = listOf(FaceInfo(Rect(400, 200, 800, 600), smileProb = 0.45f, eyesOpenProb = 0.95f))
        ),
        26L to Metadata(
            labels = listOf("person", "model", "portrait", "face", "fashion"),
            ocr = "",
            albums = setOf(AlbumType.PEOPLE),
            faces = listOf(FaceInfo(Rect(360, 170, 780, 590), smileProb = 0.85f, eyesOpenProb = 0.99f))
        )
    )

    private val _itemsFlow = MutableStateFlow(fakeItems)

    private val _summariesFlow = MutableStateFlow(
        listOf(
            AlbumSummary(type = AlbumType.RECEIPTS, count = 5, coverUri = "https://images.unsplash.com/photo-1554415707-9e4c018a482d?w=800"),
            AlbumSummary(type = AlbumType.SCREENSHOTS, count = 5, coverUri = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=800"),
            AlbumSummary(type = AlbumType.DOCUMENTS, count = 5, coverUri = "https://images.unsplash.com/photo-1450133064473-71024230f91b?w=800"),
            AlbumSummary(type = AlbumType.FOOD, count = 5, coverUri = "https://images.unsplash.com/photo-1565299624946-b28f40a0ae38?w=800"),
            AlbumSummary(type = AlbumType.PEOPLE, count = 6, coverUri = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=800")
        )
    )

    private val _scanStateFlow = MutableStateFlow<ScanState>(
        ScanProgress(phase = ScanPhase.COMPLETE, done = 128, total = 128)
    )

    private var lastId: Long = 126L

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
        val sampleTags = listOf(
            "receipt", "invoice", "screenshot", "food", "dinner", "people",
            "portrait", "document", "contract", "pizza", "coffee", "chat", "terminal"
        )
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
