package com.threeloops.mirrorarchive.data.db

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.threeloops.mirrorarchive.contracts.AlbumType
import com.threeloops.mirrorarchive.contracts.ScanPhase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class RoomDaoInstrumentedTest {
    private val context: Context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private lateinit var databaseName: String
    private var openedDatabase: AppDatabase? = null
    private val db: AppDatabase get() = checkNotNull(openedDatabase)

    @Before
    fun setUp() {
        databaseName = "room-dao-test-${UUID.randomUUID()}.db"
        openedDatabase = openDatabase()
    }

    @After
    fun tearDown() {
        openedDatabase?.close()
        openedDatabase = null
        context.deleteDatabase(databaseName)
    }

    @Test
    fun batchWritesPersistAcrossCloseAndReopen() = runBlocking {
        val imageDao = db.imageDao()
        val total = 1_000L
        for (start in 1L..total step 50L) {
            val end = minOf(start + 49L, total)
            val batch = (start..end).map { mediaId -> image(mediaId) }
            val batchLastId = batch.last().mediaId
            imageDao.saveBatchTx(
                images = batch,
                labels = batch.map { LabelEntity(it.mediaId, "label-${it.mediaId}", 0.9f) },
                albums = emptyList(),
                faces = emptyList(),
                buckets = emptyList(),
                state = scanState(lastProcessedId = batchLastId)
            )
        }

        db.close()
        openedDatabase = openDatabase()
        assertEquals(1_000, db.imageDao().count().first())
        assertEquals(1_000L, db.imageDao().lastProcessedId())
    }

    @Test
    fun prefixLabelSearchMatchesFromStartOnly() = runBlocking {
        val image = image(101)
        db.imageDao().saveBatchTx(
            images = listOf(image),
            labels = listOf(LabelEntity(image.mediaId, "dog", 0.95f)),
            albums = emptyList(),
            faces = emptyList(),
            buckets = emptyList(),
            state = scanState(image.mediaId)
        )

        assertEquals(listOf(image.mediaId), db.labelDao().searchPrefix("do").first().map { it.mediaId })
        assertEquals(listOf(image.mediaId), db.labelDao().searchPrefix("dog").first().map { it.mediaId })
        assertTrue(db.labelDao().searchPrefix("og").first().isEmpty())
    }

    @Test
    fun deletingImageCascadesToAllChildTables() = runBlocking {
        val image = image(202, faceCount = 1)
        db.imageDao().saveBatchTx(
            images = listOf(image),
            labels = listOf(LabelEntity(image.mediaId, "dog", 0.8f)),
            albums = listOf(AlbumEntryEntity(image.mediaId, AlbumType.PEOPLE)),
            faces = listOf(FaceEntity(
                imageId = image.mediaId,
                left = 1,
                top = 2,
                right = 30,
                bottom = 40,
                smileProb = 0.7f,
                eyesOpenProb = 0.9f,
                clusterId = 4L
            )),
            buckets = listOf(HashBucketEntity(image.mediaId, chunkIdx = 0, chunkVal = 42)),
            state = scanState(image.mediaId)
        )

        db.imageDao().deleteByMediaIds(listOf(image.mediaId))

        assertEquals(0, db.imageDao().count().first())
        assertEquals(0, tableCount("labels"))
        assertEquals(0, tableCount("album_entries"))
        assertEquals(0, tableCount("faces"))
        assertEquals(0, tableCount("hash_buckets"))
    }

    @Test
    fun albumSummariesHaveCountsAndNewestCover() = runBlocking {
        val newestFood = image(301, dateAdded = 300)
        val olderFood = image(302, dateAdded = 200)
        val receipt = image(303, dateAdded = 100)
        db.imageDao().saveBatchTx(
            images = listOf(newestFood, olderFood, receipt),
            labels = emptyList(),
            albums = listOf(
                AlbumEntryEntity(newestFood.mediaId, AlbumType.FOOD),
                AlbumEntryEntity(olderFood.mediaId, AlbumType.FOOD),
                AlbumEntryEntity(receipt.mediaId, AlbumType.RECEIPTS)
            ),
            faces = emptyList(),
            buckets = emptyList(),
            state = scanState(receipt.mediaId)
        )

        val summaries = db.albumDao().albumSummaries().first().associateBy { it.type }
        assertEquals(2, summaries[AlbumType.FOOD]?.count)
        assertEquals(newestFood.uri, summaries[AlbumType.FOOD]?.coverUri)
        assertEquals(1, summaries[AlbumType.RECEIPTS]?.count)
        assertEquals(receipt.uri, summaries[AlbumType.RECEIPTS]?.coverUri)
    }

    @Test
    fun savingExistingMediaIdUpdatesInsteadOfDuplicating() = runBlocking {
        val original = image(404, uri = "content://test/original", displayName = "original.jpg")
        db.imageDao().saveBatchTx(
            images = listOf(original),
            labels = listOf(LabelEntity(original.mediaId, "old", 0.4f)),
            albums = emptyList(),
            faces = emptyList(),
            buckets = emptyList(),
            state = scanState(original.mediaId)
        )
        val updated = image(404, uri = "content://test/updated", displayName = "updated.jpg", dHash = 999L)
        db.imageDao().saveBatchTx(
            images = listOf(updated),
            labels = listOf(LabelEntity(updated.mediaId, "new", 0.9f)),
            albums = emptyList(),
            faces = emptyList(),
            buckets = emptyList(),
            state = scanState(updated.mediaId)
        )

        assertEquals(1, db.imageDao().count().first())
        assertEquals("content://test/updated", db.imageDao().lowBlur(1.0).first().single().uri)
        assertEquals(999L, db.imageDao().hashRows().single().dHash)
        assertEquals(1, tableCount("labels"))
        assertEquals("new", scalarString("SELECT label FROM labels"))
    }

    @Test
    fun skippedImageInsertIsIdempotent() = runBlocking {
        db.skippedDao().insert(mediaId = 505, reason = "unsupported format")
        db.skippedDao().insert(mediaId = 505, reason = "retry failed")

        assertEquals(1, db.skippedDao().count().first())
        assertNotNull(scalarString("SELECT reason FROM skipped_images WHERE mediaId = 505"))
    }

    private fun openDatabase(): AppDatabase = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        databaseName
    )
        .fallbackToDestructiveMigration()
        .addCallback(object : androidx.room.RoomDatabase.Callback() {
            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                db.setForeignKeyConstraintsEnabled(true)
            }
        })
        .build()

    private fun image(
        mediaId: Long,
        uri: String = "content://test/$mediaId",
        displayName: String = "$mediaId.jpg",
        dateAdded: Long = mediaId,
        dHash: Long = mediaId,
        faceCount: Int = 0
    ) = ImageEntity(
        mediaId = mediaId,
        uri = uri,
        displayName = displayName,
        relativePath = "Pictures/Test/",
        dateAdded = dateAdded,
        dateTaken = null,
        generationModified = mediaId,
        width = 640,
        height = 480,
        sizeBytes = 12_345L,
        dHash = dHash,
        pHash = mediaId * 3,
        blurScore = 0.2,
        ocrText = null,
        faceCount = faceCount,
        indexedAt = 1_700_000_000_000L,
        pipelineVersion = 1
    )

    private fun scanState(lastProcessedId: Long) = ScanStateEntity(
        lastProcessedId = lastProcessedId,
        lastGeneration = lastProcessedId,
        totalCount = lastProcessedId,
        processedCount = lastProcessedId,
        phase = ScanPhase.SCANNING,
        updatedAt = 1_700_000_000_000L
    )

    private fun tableCount(table: String): Int = db.openHelper.readableDatabase
        .query("SELECT COUNT(*) FROM $table")
        .use { cursor -> cursor.moveToFirst(); cursor.getInt(0) }

    private fun scalarString(sql: String): String? = db.openHelper.readableDatabase
        .query(sql)
        .use { cursor -> if (cursor.moveToFirst()) cursor.getString(0) else null }
}
