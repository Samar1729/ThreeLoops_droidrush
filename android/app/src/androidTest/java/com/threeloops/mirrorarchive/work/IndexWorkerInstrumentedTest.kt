package com.threeloops.mirrorarchive.work

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.Configuration
import androidx.work.ExistingWorkPolicy
import androidx.work.ListenableWorker
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.testing.WorkManagerTestInitHelper
import com.threeloops.mirrorarchive.contracts.AlbumSummary
import com.threeloops.mirrorarchive.contracts.AlbumType
import com.threeloops.mirrorarchive.contracts.FaceInfo
import com.threeloops.mirrorarchive.contracts.HashBundle
import com.threeloops.mirrorarchive.contracts.ImageItem
import com.threeloops.mirrorarchive.contracts.IndexedImage
import com.threeloops.mirrorarchive.contracts.LabelResult
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
import com.threeloops.mirrorarchive.data.repo.RoomImageRepository
import com.threeloops.mirrorarchive.di.DatabaseModule
import com.threeloops.mirrorarchive.data.repo.FakeImageRepository
import com.threeloops.mirrorarchive.data.repo.ImageRepository
import com.threeloops.mirrorarchive.hash.PerceptualHasher
import com.threeloops.mirrorarchive.hash.PerceptualHasherStub
import com.threeloops.mirrorarchive.ml.ImageAnalyzerStub
import com.threeloops.mirrorarchive.metrics.MemoryProbe
import com.threeloops.mirrorarchive.metrics.MetricsRecorder
import com.threeloops.mirrorarchive.permissions.PermissionManager
import com.threeloops.mirrorarchive.permissions.PermissionState
import com.threeloops.mirrorarchive.work.ScanController
import com.threeloops.mirrorarchive.work.SyncUseCase
import com.threeloops.mirrorarchive.work.WORK_FULL
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
import org.mockito.Mockito
import java.util.concurrent.TimeUnit
import java.util.UUID

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

    @Test
    fun incrementalWorkerIndexesNewRowAndAdvancesCheckpoint() = runBlocking {
        fixture.source = FakeMediaStore(11L..11L)
        fixture.scanState.upsert(scanState(lastId = 10L, generation = 200L))

        val completed = awaitFinished(enqueueIncrementalWorker())

        assertEquals(androidx.work.WorkInfo.State.SUCCEEDED, completed.state)
        assertEquals(listOf(11L), fixture.repository.savedMediaIds)
        assertTrue(fixture.repository.indexedRows.containsKey(11L))
        assertEquals(11L, fixture.scanState.state.value?.lastProcessedId)
    }

    @Test
    fun incrementalWorkerReindexesEditedRowWithoutRegressingCheckpoint() = runBlocking {
        fixture.source = FakeMediaStore.withRows(listOf(mediaRow(id = 8L, generation = 301L)))
        fixture.scanState.upsert(scanState(lastId = 10L, generation = 300L))

        val completed = awaitFinished(enqueueIncrementalWorker())

        assertEquals(androidx.work.WorkInfo.State.SUCCEEDED, completed.state)
        assertEquals(listOf(8L), fixture.repository.savedMediaIds)
        assertEquals(1, fixture.repository.indexedRows.size)
        assertEquals(301L, fixture.repository.indexedRows.getValue(8L).generationModified)
        assertEquals(10L, fixture.scanState.state.value?.lastProcessedId)
    }

    @Test
    fun reconcileDeletionsDeletesRoomRowAndCascadesChildren() = runBlocking {
        val databaseName = "incremental-reconcile-${UUID.randomUUID()}.db"
        val database = DatabaseModule.build(context, databaseName)
        try {
            val repository = RoomImageRepository(database)
            repository.saveBatch(
                listOf(
                    IndexedImage(
                        mediaId = 44L,
                        uri = "content://test/44",
                        labels = listOf(LabelResult("receipt", 0.95f)),
                        ocrText = null,
                        dHash = 12L,
                        pHash = 34L,
                        blurScore = 5.0,
                        faceCount = 1,
                        displayName = "receipt.jpg",
                        relativePath = "Pictures/",
                        dateTaken = 44_000L,
                        generationModified = 50L,
                        width = 100,
                        height = 100,
                        sizeBytes = 100L,
                        faces = listOf(FaceInfo(Rect(1, 2, 30, 40), null, null)),
                        albums = setOf(AlbumType.RECEIPTS),
                        pipelineVersion = 1
                    )
                ),
                checkpointId = 44L
            )
            val image = database.imageDao().allImages().first().single()
            assertEquals(1, database.labelDao().labelsFor(image.id).size)
            assertEquals(1, database.faceDao().facesFor(image.id).size)
            assertEquals(1, database.albumDao().album(AlbumType.RECEIPTS).first().size)
            assertEquals(8, database.openHelper.readableDatabase.query(
                "SELECT COUNT(*) FROM hash_buckets WHERE imageId = ?",
                arrayOf(image.id)
            ).use { cursor -> cursor.moveToFirst(); cursor.getInt(0) })

            val sync = SyncUseCase(
                mediaStore = FakeMediaStore(44L..44L).apply { remove(44L) },
                repository = repository,
                permissionManager = grantedPermission(PermissionState.GRANTED_FULL),
                scanController = Mockito.mock(ScanController::class.java)
            )
            sync.reconcileDeletions()

            assertTrue(repository.allMediaIds().isEmpty())
            assertTrue(database.labelDao().labelsFor(image.id).isEmpty())
            assertTrue(database.faceDao().facesFor(image.id).isEmpty())
            assertTrue(database.albumDao().album(AlbumType.RECEIPTS).first().isEmpty())
            assertEquals(0, database.openHelper.readableDatabase.query(
                "SELECT COUNT(*) FROM hash_buckets WHERE imageId = ?",
                arrayOf(image.id)
            ).use { cursor -> cursor.moveToFirst(); cursor.getInt(0) })
        } finally {
            database.close()
            context.deleteDatabase(databaseName)
        }
    }

    @Test
    fun reconcileDeletionsDoesNothingWithPartialPermission() = runBlocking {
        fixture.repository.seed(9L)
        val sync = SyncUseCase(
            mediaStore = FakeMediaStore(emptyList()),
            repository = fixture.repository,
            permissionManager = grantedPermission(PermissionState.GRANTED_PARTIAL),
            scanController = Mockito.mock(ScanController::class.java)
        )

        sync.reconcileDeletions()

        assertTrue(fixture.repository.deletedMediaIds.isEmpty())
        assertEquals(listOf(9L), fixture.repository.allMediaIds())
    }

    @Test
    fun incrementalWorkerDoesNotRunWhileFullScanIsActive() = runBlocking {
        fixture.source = FakeMediaStore(1L..100L)
        fixture.repository.pauseAfterFirstBatch = true
        val fullRequest = OneTimeWorkRequest.Builder(IndexWorker::class.java).build()
        workManager.enqueueUniqueWork(WORK_FULL, ExistingWorkPolicy.REPLACE, fullRequest)
            .result.get(TEST_TIMEOUT_MS, TimeUnit.MILLISECONDS)
        withTimeout(TEST_TIMEOUT_MS) { fixture.repository.firstBatchSaved.await() }

        try {
            val completed = awaitFinished(enqueueIncrementalWorker())
            assertEquals(androidx.work.WorkInfo.State.SUCCEEDED, completed.state)
            assertEquals((1L..50L).toList(), fixture.repository.savedMediaIds)
            assertEquals(50L, fixture.repository.lastProcessedId())
        } finally {
            workManager.cancelWorkById(fullRequest.id).result.get(TEST_TIMEOUT_MS, TimeUnit.MILLISECONDS)
            awaitFinished(fullRequest)
        }
    }

    private fun enqueueWorker(): OneTimeWorkRequest =
        OneTimeWorkRequest.Builder(IndexWorker::class.java).build().also { request ->
            workManager.enqueue(request).result.get(TEST_TIMEOUT_MS, TimeUnit.MILLISECONDS)
        }

    private fun enqueueIncrementalWorker(): OneTimeWorkRequest =
        OneTimeWorkRequest.Builder(IncrementalSyncWorker::class.java).build().also { request ->
            workManager.enqueue(request).result.get(TEST_TIMEOUT_MS, TimeUnit.MILLISECONDS)
        }

    private fun scanState(lastId: Long, generation: Long) = ScanStateEntity(
        lastProcessedId = lastId,
        lastGeneration = generation,
        totalCount = lastId,
        processedCount = lastId,
        phase = ScanPhase.COMPLETE,
        updatedAt = System.currentTimeMillis()
    )

    private fun grantedPermission(state: PermissionState): PermissionManager =
        Mockito.mock(PermissionManager::class.java).also { manager ->
            Mockito.`when`(manager.state()).thenReturn(state)
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
    ): ListenableWorker? {
        val fixture = WorkerFixtureHolder.current
        return when (workerClassName) {
            IndexWorker::class.java.name -> IndexWorker(
                appContext,
                workerParameters,
                fixture.repository,
                fixture.source,
                fixture.pipeline,
                fixture.scanState,
                fixture.metricsRecorder,
                fixture.memoryProbe
            )
            IncrementalSyncWorker::class.java.name -> IncrementalSyncWorker(
                appContext,
                workerParameters,
                fixture.repository,
                fixture.source,
                fixture.pipeline,
                fixture.scanState,
                fixture.permissionManager,
                fixture.syncUseCase
            )
            else -> null
        }
    }
}

private class WorkerFixture {
    val scanState = FakeScanStateDao()
    val skipped = FakeSkippedDao()
    val loader = FakeThumbnailLoader()
    val metricsRecorder = Mockito.mock(MetricsRecorder::class.java)
    val memoryProbe = Mockito.mock(MemoryProbe::class.java)
    val permissionManager = Mockito.mock(PermissionManager::class.java).also {
        Mockito.`when`(it.state()).thenReturn(PermissionState.GRANTED_FULL)
    }
    val syncUseCase = Mockito.mock(SyncUseCase::class.java)
    val pipeline = IndexPipeline(loader, ImageAnalyzerStub(), FastHasher(), skipped, metricsRecorder)
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
    }.toMutableList()

    constructor(ids: List<Long>) : this(ids.takeIf { it.isNotEmpty() }?.let { it.first()..it.last() } ?: LongRange.EMPTY)

    override suspend fun query(afterId: Long, limit: Int): List<MediaRow> =
        rows.asSequence().filter { it.mediaId > afterId }.take(limit).toList()

    override suspend fun count(): Int = rows.size

    override fun currentGeneration(): Long = rows.maxOfOrNull { it.generationModified } ?: 0L

    override suspend fun changedSince(generation: Long): List<MediaRow> =
        rows.filter { it.generationModified > generation }

    override suspend fun allIds(): LongArray = rows.map { it.mediaId }.toLongArray()

    fun remove(mediaId: Long) { rows.removeAll { it.mediaId == mediaId } }

    companion object {
        fun withRows(rows: List<MediaRow>): FakeMediaStore =
            FakeMediaStore(rows.map { it.mediaId }).also { fake ->
                fake.rows.clear()
                fake.rows.addAll(rows)
            }
    }
}

private fun mediaRow(id: Long, generation: Long) = MediaRow(
    mediaId = id,
    uri = Uri.parse("content://media/external/images/media/$id"),
    displayName = "$id.jpg",
    relativePath = "DCIM/Camera/",
    dateAdded = id,
    dateTaken = null,
    generationModified = generation,
    width = 4,
    height = 4,
    sizeBytes = 16L,
    mimeType = "image/jpeg"
)

private class RecordingImageRepository(
    private val scanState: FakeScanStateDao
) : ImageRepository by FakeImageRepository() {
    private var checkpoint = 0L
    val savedMediaIds = mutableListOf<Long>()
    val indexedRows = linkedMapOf<Long, IndexedImage>()
    val deletedMediaIds = mutableListOf<Long>()
    var pauseAfterFirstBatch = false
    val firstBatchSaved = CompletableDeferred<Unit>()

    override suspend fun lastProcessedId(): Long = checkpoint

    override suspend fun saveBatch(items: List<IndexedImage>, checkpointId: Long) {
        checkpoint = checkpointId
        savedMediaIds += items.map { it.mediaId }
        items.forEach { indexedRows[it.mediaId] = it }
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

    fun seed(mediaId: Long) {
        indexedRows[mediaId] = IndexedImage(
            mediaId = mediaId,
            uri = "content://test/$mediaId",
            labels = emptyList(),
            ocrText = null,
            dHash = 1L,
            pHash = 2L,
            blurScore = 3.0,
            faceCount = 0
        )
    }

    override suspend fun allMediaIds(): List<Long> = indexedRows.keys.toList()

    override suspend fun deleteRows(mediaIds: List<Long>) {
        deletedMediaIds += mediaIds
        mediaIds.forEach(indexedRows::remove)
    }
}

private class FakeScanStateDao : ScanStateDao {
    val state = MutableStateFlow<ScanStateEntity?>(null)
    override fun observe(): Flow<ScanStateEntity?> = state
    override suspend fun upsert(state: ScanStateEntity) { this.state.value = state }
    override suspend fun deletePipelineStats() = Unit
}

private class FakeSkippedDao : SkippedDao {
    val rows = linkedMapOf<Long, SkippedImageEntity>()
    private val count = MutableStateFlow(0)
    override suspend fun insert(entity: SkippedImageEntity) {
        rows.putIfAbsent(entity.mediaId, entity)
        count.value = rows.size
    }
    override fun count(): Flow<Int> = count
    override suspend fun deleteAll() {
        rows.clear()
        count.value = 0
    }
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
