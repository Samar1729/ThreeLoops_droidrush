package com.threeloops.mirrorarchive.storage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.threeloops.mirrorarchive.contracts.ImageItem
import com.threeloops.mirrorarchive.data.repo.ImageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class StorageSaverViewModel @Inject constructor(
    private val repository: ImageRepository
) : ViewModel() {

    // A value near 100 on a 256 px Laplacian is a starting guess for the threshold[cite: 55].
    private val _threshold = MutableStateFlow(100.0)
    val threshold: StateFlow<Double> = _threshold.asStateFlow()

    // Route: lowBlur(threshold: Double): Flow<List<ImageItem>>[cite: 55]
    // Blurry images listed blurriest first[cite: 47, 55]
    val lowBlurImages: StateFlow<List<ImageItem>> = _threshold
        .flatMapLatest { currentThreshold ->
            repository.lowBlur(currentThreshold)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Route: reclaimableBytes(threshold: Double): Flow<Long>[cite: 55]
    // SUM(sizeBytes) WHERE blurScore < :t[cite: 55]
    val reclaimableBytes: StateFlow<Long> = lowBlurImages
        .map { images ->
            images.sumOf { it.sizeBytes }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0L
        )

    fun updateThreshold(newThreshold: Double) {
        _threshold.value = newThreshold
    }

    /**
     * Purges the specified low blur images from the database index.
     */
    fun purgeLowBlur(images: List<ImageItem>, onComplete: () -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            val mediaIds = images.map { it.mediaId }
            if (mediaIds.isNotEmpty()) {
                repository.deleteRows(mediaIds)
            }
            withContext(Dispatchers.Main) {
                onComplete()
            }
        }
    }

    // Route: suggestedThreshold(): Double[cite: 55]
    // Helper to calculate the 10th percentile of blurScore across the index[cite: 55].
    fun suggestedThreshold(allScores: List<Double>): Double {
        if (allScores.isEmpty()) return 100.0
        val sorted = allScores.sorted()
        val index = (sorted.size * 0.10).toInt()
        return sorted[index]
    }
}