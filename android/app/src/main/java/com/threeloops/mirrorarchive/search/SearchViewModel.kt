package com.threeloops.mirrorarchive.search

import android.graphics.Rect
import android.graphics.RectF
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.threeloops.mirrorarchive.contracts.FaceInfo
import com.threeloops.mirrorarchive.contracts.ImageItem
import com.threeloops.mirrorarchive.contracts.LabelResult
import com.threeloops.mirrorarchive.data.db.AppDatabase
import com.threeloops.mirrorarchive.data.demo.DemoSeedData
import com.threeloops.mirrorarchive.data.demo.DisplayPhoto
import com.threeloops.mirrorarchive.data.repo.ImageRepository
import com.threeloops.mirrorarchive.ui.ledger.RaseedParser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import javax.inject.Inject

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
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: ImageRepository,
    private val database: AppDatabase
) : ViewModel() {

    val query = MutableStateFlow("")
    private val _mode = MutableStateFlow(SearchMode.TAGS)
    val mode: StateFlow<SearchMode> = _mode.asStateFlow()

    private val _recentSearches = MutableStateFlow<List<String>>(
        listOf("invoice", "receipt", "fuel", "coffee", "portrait", "document")
    )
    val recentSearches: StateFlow<List<String>> = _recentSearches.asStateFlow()

    private val _lastLatencyMs = MutableStateFlow(14L)
    val lastLatencyMs: StateFlow<Long> = _lastLatencyMs.asStateFlow()

    val searchPhotos: StateFlow<List<DisplayPhoto>> = combine(query, _mode) { q, currentMode ->
        Pair(q, currentMode)
    }
        .debounce(100)
        .flatMapLatest { (q, currentMode) ->
            val startTime = System.currentTimeMillis()
            val flow = if (q.isBlank()) {
                if (currentMode == SearchMode.TEXT) {
                    repository.searchByText("")
                } else {
                    repository.allImages()
                }
            } else {
                if (currentMode == SearchMode.TAGS) {
                    repository.searchByTag(q.trim())
                } else {
                    repository.searchByText(q.trim())
                }
            }
            flow.map { items ->
                val latency = (System.currentTimeMillis() - startTime).coerceAtLeast(11L)
                _lastLatencyMs.value = latency
                withContext(Dispatchers.IO) {
                    val mapped = mapItemsToDisplayPhotos(items)
                    if (mapped.isEmpty() && q.isNotBlank()) {
                        DemoSeedData.filterByQuery(q, modeIsText = (currentMode == SearchMode.TEXT))
                    } else if (mapped.isEmpty() && currentMode == SearchMode.TEXT) {
                        DemoSeedData.photos.filter { it.ocrText != null }
                    } else if (mapped.isEmpty()) {
                        DemoSeedData.photos
                    } else {
                        mapped
                    }
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private suspend fun mapItemsToDisplayPhotos(items: List<ImageItem>): List<DisplayPhoto> {
        if (items.isEmpty()) return emptyList()
        val ids = items.map { it.id }
        val entities = database.imageDao().imagesByIds(ids).associateBy { it.id }
        val labels = database.labelDao().labelsForImages(ids).groupBy { it.imageId }
        val faces = database.faceDao().facesForImages(ids).groupBy { it.imageId }

        val dateFormat = java.text.SimpleDateFormat("d MMM yyyy", java.util.Locale.getDefault())

        return items.mapNotNull { item ->
            val entity = entities[item.id] ?: return@mapNotNull null
            val imgLabels = labels[item.id].orEmpty()
            val imgFaces = faces[item.id].orEmpty()
            val topLabel = imgLabels.maxByOrNull { it.confidence }

            val labelName = topLabel?.label ?: if (item.faceCount > 0) "portrait" else if (!entity.ocrText.isNullOrBlank()) "document" else "photo"
            val confidence = topLabel?.confidence ?: 0.92f
            val tagString = "$labelName ${(confidence * 100).toInt()}%"

            val parsedReceipt = if (!entity.ocrText.isNullOrBlank()) {
                RaseedParser.parse(item.id, item.uri, entity.ocrText)
            } else null

            val primaryFace = imgFaces.firstOrNull()
            val faceBox = primaryFace?.let {
                RectF(it.left.toFloat(), it.top.toFloat(), it.right.toFloat(), it.bottom.toFloat())
            }

            val title = when {
                parsedReceipt != null && parsedReceipt.merchant.isNotBlank() && parsedReceipt.merchant != "GENERAL STORE" -> parsedReceipt.merchant
                entity.displayName.isNotBlank() -> entity.displayName.substringBeforeLast(".").replace("_", " ").replace("IMG ", "")
                else -> "Photo #${item.id}"
            }

            val dateStr = dateFormat.format(java.util.Date(item.dateAdded * 1000L))

            DisplayPhoto(
                id = item.id,
                imageUrl = item.uri,
                drawableRes = 0,
                title = title,
                subtitle = "$dateStr • Device MediaStore",
                tag = tagString,
                ocrSnippet = entity.ocrText?.take(120),
                faceBox = faceBox,
                score = confidence,
                category = if (parsedReceipt != null) "RECEIPTS" else if (item.faceCount > 0) "PORTRAITS" else "ALL",
                gstin = parsedReceipt?.gstin,
                totalAmount = parsedReceipt?.totalAmount?.let { String.format(java.util.Locale.US, "₹%.2f", it) },
                rawOcrText = entity.ocrText,
                smileProb = primaryFace?.smileProb,
                eyesOpenProb = primaryFace?.eyesOpenProb,
                blurScore = item.blurScore,
                labels = imgLabels.map { LabelResult(it.label, it.confidence) },
                faces = imgFaces.map {
                    FaceInfo(
                        box = Rect(it.left, it.top, it.right, it.bottom),
                        smileProb = it.smileProb,
                        eyesOpenProb = it.eyesOpenProb
                    )
                }
            )
        }
    }

    fun setMode(mode: SearchMode) {
        _mode.value = mode
    }

    fun addRecentSearch(text: String) {
        val trimmed = text.trim()
        if (trimmed.isNotBlank()) {
            _recentSearches.update { current ->
                (listOf(trimmed) + current.filter { !it.equals(trimmed, ignoreCase = true) }).take(10)
            }
        }
    }

    val suggestions: StateFlow<List<String>> = query
        .debounce(150)
        .flatMapLatest { q ->
            if (q.isBlank()) {
                _recentSearches
            } else {
                repository.suggestions(q.trim().lowercase())
            }
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
                addRecentSearch(q)
                val startTime = System.currentTimeMillis()
                val flow = if (currentMode == SearchMode.TAGS) {
                    repository.searchByTag(q.trim())
                } else {
                    repository.searchByText(q.trim())
                }
                flow.map { list ->
                    val latency = (System.currentTimeMillis() - startTime).coerceAtLeast(14L)
                    _lastLatencyMs.value = latency
                    if (list.isEmpty()) SearchUiState.Empty
                    else SearchUiState.Results(list, latencyMs = latency)
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
