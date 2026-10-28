package com.threeloops.mirrorarchive.dupes

import com.threeloops.mirrorarchive.contracts.FaceInfo
import com.threeloops.mirrorarchive.contracts.HashRow
import com.threeloops.mirrorarchive.contracts.ImageItem
import com.threeloops.mirrorarchive.data.repo.ImageRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BurstSelector @Inject constructor(
    private val repository: ImageRepository,
    private val duplicateFinder: DuplicateFinder,
    private val purgePlanner: PurgePlanner
) {
    fun burstGroups(rows: List<HashRow>): List<List<Long>> =
        BurstGrouping.groups(rows, duplicateFinder.findGroups(rows))

    fun bestShot(group: List<Long>): Long {
        val rowsById = repository.duplicateCandidates().associateBy { it.id }
        val imagesById = repository.items(group).associateBy { it.id }
        val facesById = group.associateWith(repository::facesFor)
        return bestShot(group, rowsById, imagesById, facesById)
    }

    fun keepBestPurgeRest(group: List<Long>): PurgePlan {
        require(group.size > 1) { "A burst must contain at least two images." }
        val duplicateGroup = DuplicateGroup(
            id = 0L,
            imageIds = group,
            bestId = bestShot(group),
            maxDistance = 0
        )
        return purgePlanner.planForGroup(duplicateGroup)
    }

    internal fun bestShot(
        group: List<Long>,
        rowsById: Map<Long, HashRow>,
        imagesById: Map<Long, ImageItem>,
        facesById: Map<Long, List<FaceInfo>>
    ): Long = BurstScorer.bestShot(
        group.map { id ->
            val image = imagesById[id]
            val face = facesById[id]?.firstOrNull()
            BurstShotCandidate(
                id = id,
                blurScore = rowsById[id]?.blurScore ?: image?.blurScore ?: 0.0,
                eyesOpen = face?.eyesOpenProb ?: DEFAULT_FACE_SCORE,
                smile = face?.smileProb ?: DEFAULT_FACE_SCORE,
                resolution = (image?.width?.toLong() ?: 0L) *
                    (image?.height?.toLong() ?: 0L)
            )
        }
    )

    private companion object {
        const val DEFAULT_FACE_SCORE = 0.5f
    }
}

internal object BurstGrouping {
    private const val BURST_GAP_MILLIS = 3_000L

    fun groups(rows: List<HashRow>, duplicateGroups: List<List<Long>>): List<List<Long>> {
        val rowsById = rows.associateBy { it.id }
        return duplicateGroups.flatMap { duplicateGroup ->
            val timedRows = duplicateGroup.mapNotNull { id ->
                rowsById[id]?.takeIf { it.dateTaken != null }
            }.sortedWith(compareBy<HashRow> { it.dateTaken }.thenBy { it.id })

            val bursts = mutableListOf<List<Long>>()
            var currentBurst = mutableListOf<Long>()
            var previousCaptureTime: Long? = null

            for (row in timedRows) {
                val capturedAt = row.dateTaken ?: continue
                if (previousCaptureTime != null &&
                    capturedAt - previousCaptureTime > BURST_GAP_MILLIS
                ) {
                    if (currentBurst.size > 1) bursts.add(currentBurst)
                    currentBurst = mutableListOf()
                }
                currentBurst.add(row.id)
                previousCaptureTime = capturedAt
            }

            if (currentBurst.size > 1) bursts.add(currentBurst)
            bursts
        }.sortedBy { group -> rowsById[group.first()]?.dateTaken }
    }
}

internal data class BurstShotCandidate(
    val id: Long,
    val blurScore: Double,
    val eyesOpen: Float,
    val smile: Float,
    val resolution: Long
)

internal object BurstScorer {
    fun bestShot(candidates: List<BurstShotCandidate>): Long {
        require(candidates.isNotEmpty()) { "Cannot select a best shot from an empty burst." }
        val minimumBlur = candidates.minOf { it.blurScore }
        val blurRange = candidates.maxOf { it.blurScore } - minimumBlur

        return candidates.maxWithOrNull(
            compareBy<BurstShotCandidate> {
                val normalizedBlur = if (blurRange == 0.0) {
                    0.5
                } else {
                    (it.blurScore - minimumBlur) / blurRange
                }
                normalizedBlur +
                    0.15 * it.eyesOpen.coerceIn(0f, 1f) +
                    0.10 * it.smile.coerceIn(0f, 1f)
            }.thenBy { it.resolution }.thenByDescending { it.id }
        )!!.id
    }
}
