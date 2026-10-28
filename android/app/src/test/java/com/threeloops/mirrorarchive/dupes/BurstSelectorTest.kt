package com.threeloops.mirrorarchive.dupes

import com.threeloops.mirrorarchive.contracts.HashRow
import org.junit.Assert.assertEquals
import org.junit.Test

class BurstSelectorTest {
    @Test
    fun burstGroupsChainAdjacentCapturesAndSplitAfterThreeSeconds() {
        val rows = listOf(
            hashRow(id = 1, dateTaken = 1_000L),
            hashRow(id = 2, dateTaken = 4_000L),
            hashRow(id = 3, dateTaken = 7_000L),
            hashRow(id = 4, dateTaken = 10_001L),
            hashRow(id = 5, dateTaken = null),
            hashRow(id = 6, dateTaken = 13_001L)
        )

        assertEquals(
            listOf(listOf(1L, 2L, 3L), listOf(4L, 6L)),
            BurstGrouping.groups(rows, listOf(rows.map { it.id }))
        )
    }

    @Test
    fun burstGroupsDoNotCombineSeparateDuplicateGroups() {
        val rows = listOf(
            hashRow(id = 1, dateTaken = 1_000L),
            hashRow(id = 2, dateTaken = 2_000L),
            hashRow(id = 3, dateTaken = 2_100L),
            hashRow(id = 4, dateTaken = 2_200L)
        )

        assertEquals(
            listOf(listOf(1L, 2L), listOf(3L, 4L)),
            BurstGrouping.groups(rows, listOf(listOf(1L, 2L), listOf(3L, 4L)))
        )
    }

    @Test
    fun bestShotNormalizesBlurAndAddsFaceBonuses() {
        assertEquals(
            2L,
            BurstScorer.bestShot(
                listOf(
                    BurstShotCandidate(1L, 10.0, 0f, 0f, 4_000_000L),
                    BurstShotCandidate(2L, 9.0, 1f, 1f, 2_000_000L),
                    BurstShotCandidate(3L, 0.0, 0f, 0f, 1_000_000L)
                )
            )
        )
    }

    @Test
    fun bestShotUsesResolutionToBreakScoreTies() {
        assertEquals(
            2L,
            BurstScorer.bestShot(
                listOf(
                    BurstShotCandidate(1L, 10.0, 1f, 1f, 2_000_000L),
                    BurstShotCandidate(2L, 10.0, 1f, 1f, 4_000_000L)
                )
            )
        )
    }

    private fun hashRow(id: Long, dateTaken: Long?) = HashRow(
        id = id,
        dHash = 0L,
        pHash = 0L,
        blurScore = id.toDouble(),
        dateAdded = 1L,
        dateTaken = dateTaken
    )
}
