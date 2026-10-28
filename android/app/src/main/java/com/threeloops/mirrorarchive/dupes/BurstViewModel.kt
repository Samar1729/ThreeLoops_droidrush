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

data class BurstSequence(
    val id: Long,
    val imageIds: List<Long>,
    val bestId: Long,
    val imageMap: Map<Long, ImageItem>
)

sealed interface BurstUiState {
    data object Loading : BurstUiState
    data class Results(val bursts: List<BurstSequence>) : BurstUiState
    data class Error(val message: String) : BurstUiState
}

@HiltViewModel
class BurstViewModel @Inject constructor(
    private val repository: ImageRepository,
    private val burstSelector: BurstSelector
) : ViewModel() {
    private val _state = MutableStateFlow<BurstUiState>(BurstUiState.Loading)
    val state: StateFlow<BurstUiState> = _state.asStateFlow()

    init {
        scan()
    }

    fun scan() {
        viewModelScope.launch(Dispatchers.IO) {
            _state.value = BurstUiState.Loading
            try {
                val rows = repository.duplicateCandidates()
                val groups = burstSelector.burstGroups(rows)
                val imageIds = groups.flatten().distinct()
                val images = repository.items(imageIds).associateBy { it.id }
                val rowsById = rows.associateBy { it.id }
                val facesById = imageIds.associateWith(repository::facesFor)
                val bursts = groups.mapIndexed { index, group ->
                    BurstSequence(
                        id = index.toLong() + 1,
                        imageIds = group,
                        bestId = burstSelector.bestShot(group, rowsById, images, facesById),
                        imageMap = images.filterKeys { it in group }
                    )
                }
                _state.value = BurstUiState.Results(bursts)
            } catch (exception: Exception) {
                _state.value = BurstUiState.Error(
                    exception.message ?: "Unable to load burst sequences."
                )
            }
        }
    }

    fun keepBestPurgeRest(group: List<Long>): PurgePlan =
        burstSelector.keepBestPurgeRest(group)
}
