package com.threeloops.mirrorarchive.work

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.Configuration
import androidx.work.ListenableWorker
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.testing.WorkManagerTestInitHelper
import com.threeloops.mirrorarchive.contracts.AlbumSummary
import com.threeloops.mirrorarchive.contracts.AlbumType
import com.threeloops.mirrorarchive.contracts.HashBundle
import com.threeloops.mirrorarchive.contracts.ImageItem
import com.threeloops.mirrorarchive.contracts.IndexedImage
import com.threeloops.mirrorarchive.contracts.MediaRow
import com.threeloops.mirrorarchive.contracts.ScanPhase
import com.threeloops.mirrorarchive.contracts.ScanProgress
import com.threeloops.mirrorarchive.contracts.ScanState
import com.threeloops.mirrorarchive.data.db.ScanStateDao
import com.threeloops.mirrorarchive.data.db.ScanStateEntity
import com.threeloops.mirrorarchive.data.db.SkippedDao
import com.threeloops.mirrorarchive.data.db.SkippedImageEntity
import com.threeloops.mirrorarchive.data.mediastore.MediaStoreSource
import com.threeloops.mirrorarchive.data.mediastore.ThumbnailLoader
import com.threeloops.mirrorarchive.data.repo.FakeImageRepository
import com.threeloops.mirrorarchive.data.repo.ImageRepository
import com.threeloops.mirrorarchive.hash.PerceptualHasher
import com.threeloops.mirrorarchive.hash.PerceptualHasherStub
import com.threeloops.mirrorarchive.ml.ImageAnalyzerStub
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import java.util.concurrent.TimeUnit

/** Runs the real CoroutineWorker with a deterministic MediaStore and thumbnail source. */
class IndexWorkerInstrumentedTest {
    private lateinit var fixture: WorkerFixture
    private lateinit var context: Context
    private lateinit var workManager: WorkManager

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        grantMediaReadPermission()
        fixture = WorkerFixture()
        WorkerFixtureHolder.current = fixture
        workManager = WorkManager.getInstance(context)
    }

    @Test
    fun workerScansFakeMediaStoreThroughCompletion() = runBlocking {
        fixture.source = FakeMediaStore(1L..1_000L)
        val request = enqueueWorker()
        val completed = awaitFinished(request)

        assertEquals(androidx.work.WorkInfo.State.SUCCEEDED, completed.state)
        assertEquals(1_000L, fixture.repository.lastProcessedId())
        assertEquals((1L..1_000L).toList(), fixture.repository.savedMediaIds)
        assertEquals(ScanPhase.COMPLETE, fixture.scanState.state.value?.phase)
    }

    @Test
    fun cancelledWorkerResumesFromCheckpointWithoutRepeatingSavedImages() = runBlocking {
        fixture.source = FakeMediaStore(1L..121L)
        fixture.repository.pauseAfterFirstBatch = true

        val firstRequest = enqueueWorker()
        withTimeout(TEST_TIMEOUT_MS) { fixture.repository.firstBatchSaved.await() }
        workManager.cancelWorkById(firstRequest.id).result.get(TEST_TIMEOUT_MS, TimeUnit.MILLISECONDS)
        awaitFinished(firstRequest)

        assertEquals(50L, fixture.repository.lastProcessedId())
        fixture.repository.pauseAfterFirstBatch = false

        val resumedRequest = enqueueWorker()
        val completed = awaitFinished(resumedRequest)

        assertEquals(androidx.work.WorkInfo.State.SUCCEEDED, completed.state)
        assertEquals(121L, fixture.repository.lastProcessedId())
        assertEquals((1L..121L).toList(), fixture.repository.savedMediaIds)
        assertEquals(fixture.repository.savedMediaIds.size, fixture.repository.savedMediaIds.distinct().size)
    }

    @Test
    fun corruptImageIsSkippedAndStillAdvancesCheckpoint() = runBlocking {
        fixture.source = FakeMediaStore(1L..3L)
        fixture.loader.corruptMediaIds += 2L

        val completed = awaitFinished(enqueueWorker())

        assertEquals(androidx.work.WorkInfo.State.SUCCEEDED, completed.state)
        assertEquals(3L, fixture.repository.lastProcessedId())
        assertEquals(listOf(1L, 3L), fixture.repository.savedMediaIds)
        assertTrue(fixture.skipped.rows.containsKey(2L))
        assertEquals(3L, fixture.scanState.state.value?.processedCount)
    }

    private fun enqueueWorker(): OneTimeWorkRequest =
        OneTimeWorkRequest.Builder(IndexWorker::class.java).build().also { request ->
            workManager.enqueue(request).result.get(TEST_TIMEOUT_MS, TimeUnit.MILLISECONDS)
        }

    private suspend fun awaitFinished(request: OneTimeWorkRequest) = withTimeout(TEST_TIMEOUT_MS) {
        workManager.getWorkInfoByIdFlow(request.id)
            .filterNotNull()
            .first { it.state.isFinished }
    }

    private fun grantMediaReadPermission() {
        val permission = if (Build.VERSION.SDK_INT >= 33) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        InstrumentationRegistry.getInstrumentation().uiAutomation
            .grantRuntimePermission(context.packageName, permission)
    }

    companion object {
        private const val TEST_TIMEOUT_MS = 60_000L

        @BeforeClass
        @JvmStatic
        fun initializeWorkManager() {
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            WorkManagerTestInitHelper.initializeTestWorkManager(
                context,
                Configuration.Builder()
                    .setWorkerFactory(WorkerFixtureFactory)
                    .build()
            )
        }
    }
}

private object WorkerFixtureHolder {
    lateinit var current: WorkerFixture
}

private object WorkerFixtureFactory : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? = if (workerClassName == IndexWorker::class.java.name) {
        val fixture = WorkerFixtureHolder.current
        IndexWorker(
            appContext,
            workerParameters,
            fixture.repository,
            fixture.source,
            fixture.pipeline,
            fixture.scanState
        )
    } else {
        null
    }
}

private class WorkerFixture {
    val scanState = FakeScanStateDao()
    val skipped = FakeSkippedDao()
    val loader = FakeThumbnailLoader()
    val pipeline = IndexPipeline(loader, ImageAnalyzerStub(), FastHasher(), skipped)
    val repository = RecordingImageRepository(scanState)
    var source: MediaStoreSource = FakeMediaStore(emptyList())
}

private class FakeMediaStore(ids: LongRange) : MediaStoreSource {
    private val rows = ids.map { id ->
        MediaRow(
            mediaId = id,
            uri = Uri.parse("content://media/external/images/media/$id"),
            displayName = "$id.jpg",
            relativePath = "DCIM/Camera/",
            dateAdded = id,
            dateTaken = null,
            generationModified = id,
            width = 4,
            height = 4,
            sizeBytes = 16L,
            mimeType = "image/jpeg"
        )
    }

    constructor(ids: List<Long>) : this(ids.takeIf { it.isNotEmpty() }?.let { it.first()..it.last() } ?: LongRange.EMPTY)

    override suspend fun query(afterId: Long, limit: Int): List<MediaRow> =
        rows.asSequence().filter { it.mediaId > afterId }.take(limit).toList()

    override suspend fun count(): Int = rows.size

    override fun currentGeneration(): Long = rows.maxOfOrNull { it.generationModified } ?: 0L
}

private class RecordingImageRepository(
    private val scanState: FakeScanStateDao
) : ImageRepository by FakeImageRepository() {
    private var checkpoint = 0L
    val savedMediaIds = mutableListOf<Long>()
    var pauseAfterFirstBatch = false
    val firstBatchSaved = CompletableDeferred<Unit>()

    override suspend fun lastProcessedId(): Long = checkpoint

    override suspend fun saveBatch(items: List<IndexedImage>, checkpointId: Long) {
        checkpoint = checkpointId
        savedMediaIds += items.map { it.mediaId }
        val previous = scanState.state.value
        scanState.upsert(
            ScanStateEntity(
                lastProcessedId = checkpointId,
                lastGeneration = items.maxOfOrNull { it.generationModified } ?: previous?.lastGeneration ?: 0L,
                totalCount = previous?.totalCount ?: 0L,
                processedCount = (previous?.processedCount ?: 0L) + items.size,
                phase = ScanPhase.SCANNING,
                updatedAt = System.currentTimeMillis()
            )
        )
        if (savedMediaIds.size >= 50) {
            firstBatchSaved.complete(Unit)
            if (pauseAfterFirstBatch && checkpointId == 50L) CompletableDeferred<Unit>().await()
        }
    }
}

private class FakeScanStateDao : ScanStateDao {
    val state = MutableStateFlow<ScanStateEntity?>(null)
    override fun observe(): Flow<ScanStateEntity?> = state
    override suspend fun upsert(state: ScanStateEntity) { this.state.value = state }
}

private class FakeSkippedDao : SkippedDao {
    val rows = linkedMapOf<Long, SkippedImageEntity>()
    private val count = MutableStateFlow(0)
    override suspend fun insert(entity: SkippedImageEntity) {
        rows.putIfAbsent(entity.mediaId, entity)
        count.value = rows.size
    }
    override fun count(): Flow<Int> = count
}

private class FakeThumbnailLoader : ThumbnailLoader {
    val corruptMediaIds = mutableSetOf<Long>()
    override fun load(uri: Uri, maxSide: Int): Bitmap? {
        val id = uri.lastPathSegment?.toLongOrNull()
        if (id in corruptMediaIds) return null
        return Bitmap.createBitmap(4, 4, Bitmap.Config.ARGB_8888)
    }
    override fun loadForOcr(uri: Uri): Bitmap? = load(uri, 1024)
    override fun recycle(bmp: Bitmap?) { if (bmp != null && !bmp.isRecycled) bmp.recycle() }
}

private class FastHasher : PerceptualHasher by PerceptualHasherStub() {
    override fun hashAll(bmp: Bitmap): HashBundle = HashBundle(1L, 2L, 3.0)
}
