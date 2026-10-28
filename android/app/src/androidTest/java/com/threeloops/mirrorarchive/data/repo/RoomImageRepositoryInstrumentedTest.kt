package com.threeloops.mirrorarchive.data.repo

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.threeloops.mirrorarchive.contracts.ImageItem
import com.threeloops.mirrorarchive.contracts.IndexedImage
import com.threeloops.mirrorarchive.contracts.LabelResult
import com.threeloops.mirrorarchive.data.db.AppDatabase
import com.threeloops.mirrorarchive.di.DatabaseModule
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class RoomImageRepositoryInstrumentedTest {
    private val context: Context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private lateinit var databaseName: String
    private lateinit var database: AppDatabase
    private lateinit var repository: RoomImageRepository

    @Before
    fun setUp() {
        databaseName = "room-repository-test-${UUID.randomUUID()}.db"
        database = DatabaseModule.build(context, databaseName)
        repository = RoomImageRepository(database)
    }

    @After
    fun tearDown() {
        database.close()
        context.deleteDatabase(databaseName)
    }

    @Test
    fun tagSearchRequiresEveryTokenAndMatchesPrefixes() = runBlocking {
        repository.saveBatch(
            items = listOf(
                indexedImage(1, "puppy", "outdoors"),
                indexedImage(2, "dog"),
                indexedImage(3, "outdoor")
            ),
            checkpointId = 3
        )

        val matches = repository.searchByTag(" DOG out ").first().map { it.mediaId }
        assertEquals(listOf(1L), matches)
    }

    @Test
    fun tagSearchExpandsAlbumRuleSynonyms() = runBlocking {
        repository.saveBatch(
            items = listOf(
                indexedImage(10, "canine"),
                indexedImage(11, "puppy"),
                indexedImage(12, "doghouse")
            ),
            checkpointId = 12
        )

        val matches = repository.searchByTag("dog").first().map { it.mediaId }
        assertEquals(listOf(12L, 11L, 10L), matches)
    }

    @Test
    fun tagSearchFlowEmitsWhenMatchingImageIsSaved() = runBlocking {
        val emissions = Channel<List<Long>>(Channel.UNLIMITED)
        val collector = launch {
            repository.searchByTag("bird").collect { rows -> emissions.send(rows.map { it.mediaId }) }
        }
        try {
            assertTrue(withTimeout(5_000) { emissions.receive() }.isEmpty())
            repository.saveBatch(listOf(indexedImage(20, "birdsong")), checkpointId = 20)
            assertEquals(listOf(20L), withTimeout(5_000) { emissions.receive() })
        } finally {
            collector.cancelAndJoin()
            emissions.close()
        }
    }

    private fun indexedImage(mediaId: Long, vararg labels: String) = IndexedImage(
        mediaId = mediaId,
        uri = "content://test/$mediaId",
        labels = labels.map { LabelResult(it, 0.9f) },
        ocrText = null,
        dHash = 0x0102030405060708L + mediaId,
        pHash = 0x0807060504030201L + mediaId,
        blurScore = 10.0,
        faceCount = 0,
        displayName = "$mediaId.jpg",
        relativePath = "Pictures/Test/",
        dateTaken = mediaId * 1_000L,
        generationModified = mediaId,
        width = 100,
        height = 80,
        sizeBytes = 500L,
        pipelineVersion = 999
    )
}
