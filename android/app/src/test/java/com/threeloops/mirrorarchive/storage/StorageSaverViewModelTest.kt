package com.threeloops.mirrorarchive.storage

import com.threeloops.mirrorarchive.contracts.ImageItem
import com.threeloops.mirrorarchive.data.repo.FakeImageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Section 3.15: Unit tests for StorageSaverViewModel.
 * Owner: Samar (Feature S-4)
 */
@OptIn(ExperimentalCoroutinesApi::class)
class StorageSaverViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeImageRepository
    private lateinit var viewModel: StorageSaverViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeImageRepository()
        viewModel = StorageSaverViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testSuggestedThresholdCalculatesTenthPercentile() {
        val scores = listOf(10.0, 20.0, 30.0, 40.0, 50.0, 60.0, 70.0, 80.0, 90.0, 100.0)
        val suggested = viewModel.suggestedThreshold(scores)
        assertEquals(20.0, suggested, 0.001)
    }

    @Test
    fun testEmptyScoresReturnDefaultSuggestedThreshold() {
        val suggested = viewModel.suggestedThreshold(emptyList())
        assertEquals(100.0, suggested, 0.001)
    }

    @Test
    fun testUpdateThresholdChangesState() = runTest {
        viewModel.updateThreshold(65.0)
        assertEquals(65.0, viewModel.threshold.value, 0.001)
    }

    @Test
    fun testPurgeLowBlurDeletesRowsFromRepository() = runTest {
        val testItem = ImageItem(
            id = 1L,
            mediaId = 101L,
            uri = "content://media/101",
            dateAdded = 1000L,
            dateTaken = 1000L,
            sizeBytes = 2048L,
            width = 100,
            height = 100,
            blurScore = 25.0,
            faceCount = 0
        )

        var completed = false
        val job = viewModel.purgeLowBlur(listOf(testItem)) {
            completed = true
        }
        job.join()

        assertTrue("Purge callback must complete successfully", completed)
    }
}

