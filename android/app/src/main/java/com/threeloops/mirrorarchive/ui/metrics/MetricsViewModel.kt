package com.threeloops.mirrorarchive.ui.metrics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.threeloops.mirrorarchive.metrics.Benchmark
import com.threeloops.mirrorarchive.metrics.MetricsInbox
import com.threeloops.mirrorarchive.metrics.MetricsRepository
import com.threeloops.mirrorarchive.metrics.MetricsSnapshot
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

sealed interface MetricsUiState {
    data object Loading : MetricsUiState
    data class Data(val snapshot: MetricsSnapshot) : MetricsUiState
    data class Error(val message: String) : MetricsUiState
}

@HiltViewModel
class MetricsViewModel @Inject constructor(repository: MetricsRepository) : ViewModel() {
    private val _isBenchmarking = MutableStateFlow(false)
    val isBenchmarking: StateFlow<Boolean> = _isBenchmarking.asStateFlow()

    val uiState = repository.snapshot()
        .map<MetricsSnapshot, MetricsUiState> { MetricsUiState.Data(it) }
        .catch { emit(MetricsUiState.Error(it.message ?: "Unable to load metrics")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MetricsUiState.Loading)

    init {
        runHashBenchmark(n = 50)
    }

    /**
     * Executes the on-device perceptual hash benchmark (Module 3.19, Samar S-7).
     * Measures hashAll images/sec across [n] thumbnail images on Dispatchers.Default.
     */
    fun runHashBenchmark(n: Int = 100) {
        if (_isBenchmarking.value) return
        viewModelScope.launch {
            _isBenchmarking.value = true
            try {
                val throughput = withContext(Dispatchers.Default) {
                    Benchmark.hashThroughput(n)
                }
                MetricsInbox.setHashThroughput(throughput)
            } finally {
                _isBenchmarking.value = false
            }
        }
    }
}

