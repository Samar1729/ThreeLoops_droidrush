package com.threeloops.mirrorarchive.dupes

import com.threeloops.mirrorarchive.contracts.HashRow
import com.threeloops.mirrorarchive.hash.PerceptualHasher
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max
import kotlin.system.measureTimeMillis

/**
 * Interface for grouping duplicate images and selecting a representative.
 */
interface DuplicateFinder {
    fun findGroups(rows: List<HashRow>): List<List<Long>>
    fun bestShot(group: List<Long>): Long
    fun findGroupsWithStats(
        rows: List<HashRow>,
        config: DuplicateConfig = DuplicateConfig()
    ): GroupResult
    fun distance(a: HashRow, b: HashRow): PairDistance
}

/**
 * Real production implementation of [DuplicateFinder] using 8-chunk Pigeonhole Bucketing
 * and Union-Find (DSU).
 */
@Singleton
class RealDuplicateFinder @Inject constructor() : DuplicateFinder {

    override fun distance(a: HashRow, b: HashRow): PairDistance {
        val dDist = PerceptualHasher.hamming(a.dHash, b.dHash)
        val pDist = PerceptualHasher.hamming(a.pHash, b.pHash)
        return PairDistance(dDist, pDist)
    }

    override fun findGroups(rows: List<HashRow>): List<List<Long>> {
        return findGroupsWithStats(rows).groups.map { it.imageIds }
    }

    override fun bestShot(group: List<Long>): Long {
        return group.firstOrNull() ?: 0L
    }

    fun bestShot(group: List<Long>, rowMap: Map<Long, HashRow>): Long {
        return group.maxByOrNull { rowMap[it]?.blurScore ?: 0.0 } ?: group.firstOrNull() ?: 0L
    }

    override fun findGroupsWithStats(
        rows: List<HashRow>,
        config: DuplicateConfig
    ): GroupResult {
        val n = rows.size.toLong()
        val naivePairs = (n * (n - 1)) / 2

        var bucketedPairsCount = 0L
        var confirmedPairsCount = 0L

        val groups = mutableListOf<DuplicateGroup>()

        val elapsedMs = measureTimeMillis {
            if (rows.size < 2) return@measureTimeMillis

            // 1. Pigeonhole Bucketing (8 chunks x 8 bits)
            val buckets = HashMap<Pair<Int, Int>, MutableList<HashRow>>()
            for (row in rows) {
                val chunks = PerceptualHasher.bucketKeys(row.dHash)
                for (chunkIdx in chunks.indices) {
                    val key = Pair(chunkIdx, chunks[chunkIdx])
                    buckets.getOrPut(key) { mutableListOf() }.add(row)
                }
            }

            // 2. Candidate Pair Extraction
            val candidatePairs = HashSet<Pair<Long, Long>>()
            for (list in buckets.values) {
                if (list.size > 1) {
                    for (i in 0 until list.size) {
                        for (j in i + 1 until list.size) {
                            val id1 = list[i].id
                            val id2 = list[j].id
                            val pair = if (id1 < id2) Pair(id1, id2) else Pair(id2, id1)
                            candidatePairs.add(pair)
                        }
                    }
                }
            }
            bucketedPairsCount = candidatePairs.size.toLong()

            val rowMap = rows.associateBy { it.id }
            val dsu = UnionFind(rows.map { it.id })
            val maxDistances = mutableMapOf<Pair<Long, Long>, Int>()

            // 3. Confirm candidates with exact Hamming thresholds
            for ((id1, id2) in candidatePairs) {
                val row1 = rowMap[id1] ?: continue
                val row2 = rowMap[id2] ?: continue

                val dist = distance(row1, row2)
                if (dist.dHash <= config.dHashMax && dist.pHash <= config.pHashMax) {
                    confirmedPairsCount++
                    dsu.union(id1, id2)
                    val maxD = max(dist.dHash, dist.pHash)
                    maxDistances[Pair(id1, id2)] = maxD
                }
            }

            // 4. Component Grouping
            val components = HashMap<Long, MutableList<Long>>()
            for (row in rows) {
                val root = dsu.find(row.id)
                components.getOrPut(root) { mutableListOf() }.add(row.id)
            }

            // 5. Final Group Assembly
            var groupIdCounter = 1L
            for ((_, cluster) in components) {
                if (cluster.size > 1) {
                    val bestId = bestShot(cluster, rowMap)
                    var groupMaxDist = 0

                    for (i in 0 until cluster.size) {
                        for (j in i + 1 until cluster.size) {
                            val u = cluster[i]
                            val v = cluster[j]
                            val p = if (u < v) Pair(u, v) else Pair(v, u)
                            maxDistances[p]?.let { groupMaxDist = max(groupMaxDist, it) }
                        }
                    }

                    groups.add(
                        DuplicateGroup(
                            id = groupIdCounter++,
                            imageIds = cluster,
                            bestId = bestId,
                            maxDistance = groupMaxDist
                        )
                    )
                }
            }
        }

        return GroupResult(
            groups = groups,
            stats = ComparisonStats(
                naivePairs = naivePairs,
                bucketedPairs = bucketedPairsCount,
                confirmedPairs = confirmedPairsCount,
                elapsedMs = elapsedMs
            )
        )
    }

    private class UnionFind(ids: List<Long>) {
        private val parent = HashMap<Long, Long>()

        init {
            for (id in ids) {
                parent[id] = id
            }
        }

        fun find(i: Long): Long {
            var root = i
            while (root != parent[root]) {
                root = parent[root]!!
            }
            var curr = i
            while (curr != root) {
                val nxt = parent[curr]!!
                parent[curr] = root
                curr = nxt
            }
            return root
        }

        fun union(i: Long, j: Long) {
            val rootI = find(i)
            val rootJ = find(j)
            if (rootI != rootJ) {
                parent[rootI] = rootJ
            }
        }
    }
}