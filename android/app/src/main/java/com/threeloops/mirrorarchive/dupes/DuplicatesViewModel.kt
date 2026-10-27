package com.threeloops.mirrorarchive.dupes

import android.graphics.Rect
import android.graphics.RectF
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.threeloops.mirrorarchive.contracts.FaceInfo
import com.threeloops.mirrorarchive.contracts.ImageItem
import com.threeloops.mirrorarchive.contracts.LabelResult
import com.threeloops.mirrorarchive.data.db.AppDatabase
import com.threeloops.mirrorarchive.data.demo.DisplayPhoto
import com.threeloops.mirrorarchive.data.demo.SampleMediaSeed
import com.threeloops.mirrorarchive.data.repo.ImageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for the duplicates overview screen.
 */
sealed interface DuplicatesUiState {
    /** Initial state, scanning not started. */
    data object Idle : DuplicatesUiState

    /** Scanning for duplicates in progress. */
    data object Scanning : DuplicatesUiState

    /** Scan complete with results. */
    data class Results(
        val groups: List<DuplicateGroup>,
        val stats: ComparisonStats,
        val imageMap: Map<Long, ImageItem>
    ) : DuplicatesUiState

    /** No duplicates found. */
    data object Empty : DuplicatesUiState

    /** Error during scan. */
    data class Error(val message: String) : DuplicatesUiState
}

/**
 * ViewModel powering the Duplicates screen.
 * Runs the [DuplicateFinder] against repository candidates and exposes groups + stats.
 */
@HiltViewModel
class DuplicatesViewModel @Inject constructor(
    private val repository: ImageRepository,
    private val duplicateFinder: DuplicateFinder,
    private val purgePlanner: PurgePlanner,
    private val database: AppDatabase
) : ViewModel() {

    private val _state = MutableStateFlow<DuplicatesUiState>(DuplicatesUiState.Idle)
    val state: StateFlow<DuplicatesUiState> = _state.asStateFlow()

    val burstPhotos: StateFlow<List<DisplayPhoto>> = _state
        .map { s ->
            if (s is DuplicatesUiState.Results && s.groups.isNotEmpty()) {
                val group = s.groups.first()
                val ids = group.imageIds
                val entities = database.imageDao().imagesByIds(ids).associateBy { it.id }
                val faces = database.faceDao().facesForImages(ids).groupBy { it.imageId }
                val labels = database.labelDao().labelsForImages(ids).groupBy { it.imageId }

                group.imageIds.mapNotNull { id ->
                    val entity = entities[id] ?: return@mapNotNull null
                    val isWinner = (id == group.bestId)
                    val imgFaces = faces[id].orEmpty()
                    val imgLabels = labels[id].orEmpty()
                    val primaryFace = imgFaces.firstOrNull()

                    val reason = if (isWinner) null else if ((primaryFace?.eyesOpenProb ?: 1f) < 0.25f) "Drop • Eyes Closed" else "Drop • Blur: ${entity.blurScore.toInt()}"
                    val tag = if (isWinner) "Best Shot" else reason ?: "Drop"

                    DisplayPhoto(
                        id = entity.id,
                        imageUrl = entity.uri,
                        drawableRes = 0,
                        title = entity.displayName.substringBeforeLast(".").replace("_", " "),
                        subtitle = "pHash visual match • ${entity.blurScore.toInt()} sharpness",
                        tag = tag,
                        ocrSnippet = null,
                        faceBox = primaryFace?.let { RectF(it.left.toFloat(), it.top.toFloat(), it.right.toFloat(), it.bottom.toFloat()) },
                        score = if (isWinner) 0.98f else 0.40f,
                        category = "BURSTS",
                        blurScore = entity.blurScore,
                        isWinner = isWinner,
                        rejectionReason = reason,
                        smileProb = primaryFace?.smileProb,
                        eyesOpenProb = primaryFace?.eyesOpenProb,
                        labels = imgLabels.map { LabelResult(it.label, it.confidence) },
                        faces = imgFaces.map { FaceInfo(Rect(it.left, it.top, it.right, it.bottom), it.smileProb, it.eyesOpenProb) }
                    )
                }
            } else {
                SampleMediaSeed.burstPhotos
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SampleMediaSeed.burstPhotos)

    init {
        scan()
    }

    fun purgeRedundant() {
        viewModelScope.launch(Dispatchers.IO) {
            val s = _state.value
            if (s is DuplicatesUiState.Results) {
                for (group in s.groups) {
                    val toDelete = group.imageIds.filter { it != group.bestId }
                    repository.deleteRows(toDelete)
                }
                scan()
            }
        }
    }

    /**
     * Triggers a duplicate scan using hash rows from the repository.
     */
    fun scan() {
        viewModelScope.launch(Dispatchers.Default) {
            _state.value = DuplicatesUiState.Scanning
            try {
                val candidates = repository.duplicateCandidates()
                if (candidates.size < 2) {
                    _state.value = DuplicatesUiState.Empty
                    return@launch
                }

                val result = duplicateFinder.findGroupsWithStats(candidates)
                if (result.groups.isEmpty()) {
                    _state.value = DuplicatesUiState.Empty
                    return@launch
                }

                // Collect all image IDs from all groups to fetch their details
                val allIds = result.groups.flatMap { it.imageIds }.distinct()
                val items = repository.items(allIds)
                val imageMap = items.associateBy { it.id }

                _state.value = DuplicatesUiState.Results(
                    groups = result.groups,
                    stats = result.stats,
                    imageMap = imageMap
                )
            } catch (e: Exception) {
                _state.value = DuplicatesUiState.Error(
                    message = e.message ?: "Duplicate scan failed"
                )
            }
        }
    }

    /**
     * Creates a purge plan for the specified group.
     */
    fun planPurge(group: DuplicateGroup): PurgePlan {
        return purgePlanner.planForGroup(group)
    }
}
