package com.threeloops.mirrorarchive.ui.metrics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.threeloops.mirrorarchive.metrics.MetricsRepository
import com.threeloops.mirrorarchive.metrics.MetricsSnapshot
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

sealed interface MetricsUiState {
    data object Loading : MetricsUiState
    data class Data(val snapshot: MetricsSnapshot) : MetricsUiState
    data class Error(val message: String) : MetricsUiState
}

@HiltViewModel
class MetricsViewModel @Inject constructor(repository: MetricsRepository) : ViewModel() {
    val uiState = repository.snapshot()
        .map<MetricsSnapshot, MetricsUiState> { MetricsUiState.Data(it) }
        .catch { emit(MetricsUiState.Error(it.message ?: "Unable to load metrics")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MetricsUiState.Loading)
}
