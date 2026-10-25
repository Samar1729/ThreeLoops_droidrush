package com.threeloops.mirrorarchive.dupes

import com.threeloops.mirrorarchive.contracts.HashRow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Configuration thresholds for duplicate detection.
 *
 * @property dHashMax Maximum allowed dHash Hamming distance for a confirmed pair.
 * @property pHashMax Maximum allowed pHash Hamming distance for a confirmed pair.
 */
data class DuplicateConfig(
    val dHashMax: Int = 10,
    val pHashMax: Int = 10
)

/**
 * A cluster of duplicate images grouped by perceptual similarity.
 *
 * @property id Unique group identifier.
 * @property imageIds List of image IDs belonging to this group.
 * @property bestId The image ID selected as the "best shot" (highest quality).
 * @property maxDistance Maximum Hamming distance between any pair in the group.
 */
data class DuplicateGroup(
    val id: Long,
    val imageIds: List<Long>,
    val bestId: Long,
    val maxDistance: Int
)

/**
 * Result of duplicate grouping, including groups and performance statistics.
 *
 * @property groups List of detected duplicate groups.
 * @property stats Comparison statistics for the grouping operation.
 */
data class GroupResult(
    val groups: List<DuplicateGroup>,
    val stats: ComparisonStats
)

/**
 * Performance statistics from the duplicate finding operation.
 *
 * @property naivePairs Total naive O(n²) pair count.
 * @property bucketedPairs Pairs after pigeonhole bucketing.
 * @property confirmedPairs Pairs confirmed within Hamming thresholds.
 * @property elapsedMs Wall-clock time for the grouping operation.
 */
data class ComparisonStats(
    val naivePairs: Long,
    val bucketedPairs: Long,
    val confirmedPairs: Long,
    val elapsedMs: Long
)

/**
 * Hamming distance pair between dHash and pHash of two images.
 *
 * @property dHash Hamming distance of difference hashes.
 * @property pHash Hamming distance of perceptual hashes.
 */
data class PairDistance(
    val dHash: Int,
    val pHash: Int
)

/**
 * Stub implementation of [DuplicateFinder] for Hilt DI binding.
 * Delegates to [RealDuplicateFinder] at runtime.
 */
@Singleton
class DuplicateFinderStub @Inject constructor() : DuplicateFinder {
    private val real = RealDuplicateFinder()

    override fun findGroups(rows: List<HashRow>): List<List<Long>> = real.findGroups(rows)
    override fun bestShot(group: List<Long>): Long = real.bestShot(group)
    override fun findGroupsWithStats(rows: List<HashRow>, config: DuplicateConfig): GroupResult =
        real.findGroupsWithStats(rows, config)
    override fun distance(a: HashRow, b: HashRow): PairDistance = real.distance(a, b)
}
