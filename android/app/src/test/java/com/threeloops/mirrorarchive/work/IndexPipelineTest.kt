package com.threeloops.mirrorarchive.work

import android.graphics.Bitmap
import android.net.Uri
import com.threeloops.mirrorarchive.contracts.AlbumType
import com.threeloops.mirrorarchive.contracts.FaceInfo
import com.threeloops.mirrorarchive.contracts.HashBundle
import com.threeloops.mirrorarchive.contracts.LabelResult
import com.threeloops.mirrorarchive.contracts.MediaRow
import com.threeloops.mirrorarchive.data.db.SkippedDao
import com.threeloops.mirrorarchive.data.db.SkippedImageEntity
import com.threeloops.mirrorarchive.data.mediastore.ThumbnailLoader
import com.threeloops.mirrorarchive.data.repo.PIPELINE_VERSION
import com.threeloops.mirrorarchive.hash.PerceptualHasher
import com.threeloops.mirrorarchive.ml.ImageAnalyzer
import com.threeloops.mirrorarchive.metrics.MetricsRecorder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito

class IndexPipelineTest {
    @Test
    fun nullBitmapReturnsNullAndRecordsSkippedRow() = runBlocking {
        val loader = FakeLoader(mainBitmap = null)
        val skipped = RecordingSkippedDao()
        val result = pipeline(loader, FakeAnalyzer(), FakeHasher(), skipped)
            .processOne(mediaRow())

        assertNull(result)
        assertEquals(1, skipped.rows.size)
        assertEquals(42L, skipped.rows.single().mediaId)
        assertEquals("Thumbnail load returned null", skipped.rows.single().reason)
        assertTrue(loader.recycled.isEmpty())
    }

    @Test
    fun analysisExceptionReturnsNullRecordsSkipAndRecyclesBitmap() = runBlocking {
        val bitmap = Mockito.mock(Bitmap::class.java)
        val loader = FakeLoader(mainBitmap = bitmap)
        val analyzer = FakeAnalyzer().apply { labelsFailure = IllegalStateException("labels failed") }
        val skipped = RecordingSkippedDao()

        val result = pipeline(loader, analyzer, FakeHasher(), skipped).processOne(mediaRow())

        assertNull(result)
        assertEquals("labels failed", skipped.rows.single().reason)
        assertEquals(listOf(bitmap), loader.recycled)
    }

    @Test
    fun normalImageProducesFullyPopulatedIndexedImageAndRecyclesBitmaps() = runBlocking {
        val bitmap = Mockito.mock(Bitmap::class.java)
        val ocrBitmap = Mockito.mock(Bitmap::class.java)
        val loader = FakeLoader(mainBitmap = bitmap, ocrBitmap = ocrBitmap)
        val face = Mockito.mock(FaceInfo::class.java)
        val analyzer = FakeAnalyzer().apply {
            detectedLabels = listOf(LabelResult("food", 0.93f))
            detectedFaces = listOf(face)
            textLike = true
            ocrText = "fresh ingredients"
        }
        val skipped = RecordingSkippedDao()
        val hash = HashBundle(dHash = 101L, pHash = 202L, blur = 3.5)

        val result = pipeline(loader, analyzer, FakeHasher(hash), skipped).processOne(mediaRow())

        assertEquals(42L, result?.mediaId)
        assertEquals("content://media/external/images/media/42", result?.uri)
        assertEquals("IMG_0042.jpg", result?.displayName)
        assertEquals("DCIM/Camera/", result?.relativePath)
        assertEquals(1_700_000_000_000L, result?.dateTaken)
        assertEquals(17L, result?.generationModified)
        assertEquals(2048, result?.width)
        assertEquals(1536, result?.height)
        assertEquals(9_876_543L, result?.sizeBytes)
        assertEquals(analyzer.detectedLabels, result?.labels)
        assertEquals("fresh ingredients", result?.ocrText)
        assertEquals(101L, result?.dHash)
        assertEquals(202L, result?.pHash)
        assertEquals(3.5, result?.blurScore ?: 0.0, 0.0)
        assertEquals(1, result?.faceCount)
        assertSame(face, result?.faces?.single())
        assertEquals(setOf(AlbumType.FOOD, AlbumType.PEOPLE), result?.albums)
        assertEquals(PIPELINE_VERSION, result?.pipelineVersion)
        assertTrue(skipped.rows.isEmpty())
        assertEquals(listOf(ocrBitmap, bitmap), loader.recycled)
    }

    private fun pipeline(
        loader: ThumbnailLoader,
        analyzer: ImageAnalyzer,
        hasher: PerceptualHasher,
        skipped: SkippedDao
    ) = IndexPipeline(loader, analyzer, hasher, skipped, Mockito.mock(MetricsRecorder::class.java))

    private fun mediaRow(): MediaRow {
        val uri = Mockito.mock(Uri::class.java)
        Mockito.`when`(uri.toString()).thenReturn("content://media/external/images/media/42")
        return MediaRow(
            mediaId = 42L,
            uri = uri,
            displayName = "IMG_0042.jpg",
            relativePath = "DCIM/Camera/",
            dateAdded = 1_700_000_100L,
            dateTaken = 1_700_000_000_000L,
            generationModified = 17L,
            width = 2048,
            height = 1536,
            sizeBytes = 9_876_543L,
            mimeType = "image/jpeg"
        )
    }

    private class FakeLoader(
        private val mainBitmap: Bitmap?,
        private val ocrBitmap: Bitmap? = null
    ) : ThumbnailLoader {
        val recycled = mutableListOf<Bitmap?>()
        override fun load(uri: Uri, maxSide: Int): Bitmap? {
            assertEquals(512, maxSide)
            return mainBitmap
        }
        override fun loadForOcr(uri: Uri): Bitmap? = ocrBitmap
        override fun recycle(bmp: Bitmap?) {
            if (bmp != null) recycled += bmp
        }
    }

    private class FakeAnalyzer : ImageAnalyzer {
        var detectedLabels = emptyList<LabelResult>()
        var detectedFaces = emptyList<FaceInfo>()
        var textLike = false
        var ocrText = ""
        var labelsFailure: Exception? = null

        override suspend fun labels(bmp: Bitmap): List<LabelResult> {
            labelsFailure?.let { throw it }
            return detectedLabels
        }
        override suspend fun faces(bmp: Bitmap): List<FaceInfo> = detectedFaces
        override suspend fun ocr(bmp: Bitmap): String = ocrText
        override fun isTextLike(labels: List<LabelResult>): Boolean = textLike
        override fun warmUp() = Unit
        override fun close() = Unit
    }

    private class FakeHasher(
        private val hash: HashBundle = HashBundle(1L, 2L, 0.5)
    ) : PerceptualHasher {
        override fun dHash(bmp: Bitmap): Long = hash.dHash
        override fun pHash(bmp: Bitmap): Long = hash.pHash
        override fun hamming(a: Long, b: Long): Int = java.lang.Long.bitCount(a xor b)
        override fun blurScore(bmp: Bitmap): Double = hash.blur
        override fun hashAll(bmp: Bitmap): HashBundle = hash
        override fun toGray(bmp: Bitmap, w: Int, h: Int): IntArray = IntArray(w * h)
        override fun bucketKeys(hash: Long): IntArray = IntArray(8)
    }

    private class RecordingSkippedDao : SkippedDao {
        val rows = mutableListOf<SkippedImageEntity>()
        override suspend fun insert(entity: SkippedImageEntity) {
            rows += entity
        }
        override fun count(): Flow<Int> = flowOf(rows.size)
        override suspend fun deleteAll() { rows.clear() }
    }
}
