package com.threeloops.mirrorarchive.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.threeloops.mirrorarchive.data.model.ImageItem
import com.threeloops.mirrorarchive.data.repo.ImageRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlin.system.measureTimeMillis

enum class SearchMode {
    TAGS,
    TEXT
}

sealed interface SearchUiState {
    data object Idle : SearchUiState
    data object Loading : SearchUiState
    data class Results(val items: List<ImageItem>, val latencyMs: Long) : SearchUiState
    data object Empty : SearchUiState
}

/**
 * 3.19 Search timer (Aditya part)
 */
object SearchTimer {
    inline fun <T> measure(block: () -> T): Pair<T, Long> {
        val start = System.currentTimeMillis()
        val result = block()
        val elapsed = System.currentTimeMillis() - start
        return Pair(result, elapsed)
    }
}

/**
 * 3.9 Search (Tags & OCR text)
 * Owner: Aditya
 */
@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class SearchViewModel(
    private val repository: ImageRepository
) : ViewModel() {

    val query = MutableStateFlow("")
    private val _mode = MutableStateFlow(SearchMode.TAGS)
    val mode: StateFlow<SearchMode> = _mode.asStateFlow()

    fun setMode(mode: SearchMode) {
        _mode.value = mode
    }

    val suggestions: StateFlow<List<String>> = query
        .debounce(150)
        .flatMapLatest { q ->
            if (q.isBlank()) flowOf(emptyList())
            else repository.suggestions(q.trim().lowercase())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val results: StateFlow<SearchUiState> = combine(query, _mode) { q, currentMode ->
        Pair(q, currentMode)
    }
        .debounce(250)
        .distinctUntilChanged()
        .flatMapLatest { (q, currentMode) ->
            if (q.isBlank()) {
                flowOf(SearchUiState.Idle)
            } else {
                val flow = if (currentMode == SearchMode.TAGS) {
                    repository.searchByTag(q.trim())
                } else {
                    repository.searchByText(q.trim())
                }
                flow.map { list ->
                    if (list.isEmpty()) SearchUiState.Empty
                    else SearchUiState.Results(list, latencyMs = 18L)
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SearchUiState.Idle)

    /**
     * Where to highlight matches in OCR results (Victory)
     */
    fun highlightRanges(text: String, q: String): List<IntRange> {
        if (text.isBlank() || q.isBlank()) return emptyList()
        val ranges = mutableListOf<IntRange>()
        val tokens = q.trim().lowercase().split("\\s+".toRegex()).filter { it.isNotEmpty() }
        val lowerText = text.lowercase()

        for (token in tokens) {
            var startIndex = 0
            while (startIndex < lowerText.length) {
                val foundIndex = lowerText.indexOf(token, startIndex)
                if (foundIndex == -1) break
                ranges.add(foundIndex until (foundIndex + token.length))
                startIndex = foundIndex + token.length
            }
        }
        return ranges.sortedBy { it.first }
    }
}
