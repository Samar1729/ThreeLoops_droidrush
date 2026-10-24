package com.threeloops.mirrorarchive.dupes

import com.threeloops.mirrorarchive.contracts.HashRow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Interface for grouping duplicate images and selecting a representative best shot.
 */
interface DuplicateFinder {
    /**
     * Groups duplicate image rows into clusters of IDs.
     */
    fun findGroups(rows: List<HashRow>): List<List<Long>>

    /**
     * Selects the best image ID from a duplicate cluster.
     */
    fun bestShot(group: List<Long>): Long
}

/**
 * Stub implementation of [DuplicateFinder].
 */
@Singleton
class DuplicateFinderStub @Inject constructor() : DuplicateFinder {
    override fun findGroups(rows: List<HashRow>): List<List<Long>> = emptyList()
    override fun bestShot(group: List<Long>): Long = group.firstOrNull() ?: 0L
}
