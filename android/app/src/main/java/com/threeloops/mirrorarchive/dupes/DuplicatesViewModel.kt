package com.threeloops.mirrorarchive.dupes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.threeloops.mirrorarchive.contracts.ImageItem
import com.threeloops.mirrorarchive.data.repo.ImageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    private val purgePlanner: PurgePlanner
) : ViewModel() {

    private val _state = MutableStateFlow<DuplicatesUiState>(DuplicatesUiState.Idle)
    val state: StateFlow<DuplicatesUiState> = _state.asStateFlow()

    init {
        scan()
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
