package com.threeloops.mirrorarchive.data.export

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.threeloops.mirrorarchive.contracts.ImageItem
import com.threeloops.mirrorarchive.data.repo.ImageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface ExportOperation {
    data object Idle : ExportOperation
    data object Working : ExportOperation
    data class Completed(val uri: Uri, val message: String) : ExportOperation
    data class Failed(val message: String) : ExportOperation
}

@HiltViewModel
class ExportViewModel @Inject constructor(
    private val repository: ImageRepository,
    private val exportService: ExportService
) : ViewModel() {
    val images: StateFlow<List<ImageItem>> = repository.allImages()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val progress = exportService.progress

    private val _selectedIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedIds: StateFlow<Set<Long>> = _selectedIds.asStateFlow()
    private val _operation = MutableStateFlow<ExportOperation>(ExportOperation.Idle)
    val operation: StateFlow<ExportOperation> = _operation.asStateFlow()
    private val _collage = MutableStateFlow<Bitmap?>(null)
    val collage: StateFlow<Bitmap?> = _collage.asStateFlow()
    private var selectionInitialized = false

    fun initializeSelection(ids: List<Long>) {
        if (selectionInitialized) return
        _selectedIds.value = if (ids.isEmpty()) {
            images.value.mapTo(mutableSetOf()) { it.id }
        } else {
            images.value.filter { it.mediaId in ids }.mapTo(mutableSetOf()) { it.id }
        }
        selectionInitialized = true
    }

    fun toggleSelection(id: Long) {
        _selectedIds.value = _selectedIds.value.toMutableSet().apply {
            if (!add(id)) remove(id)
        }
    }

    fun selectAll() {
        _selectedIds.value = images.value.mapTo(mutableSetOf()) { it.id }
    }

    fun clearSelection() {
        _selectedIds.value = emptySet()
    }

    fun exportZip(name: String) {
        val selected = selectedImages()
        if (selected.isEmpty()) {
            _operation.value = ExportOperation.Failed("Select at least one photo to export.")
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            _operation.value = ExportOperation.Working
            try {
                val uri = exportService.exportZip(selected.map { Uri.parse(it.uri) }, name)
                _operation.value = ExportOperation.Completed(uri, "ZIP saved to Downloads/MirrorArchive.")
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _operation.value = ExportOperation.Failed(
                    exception.message ?: "Unable to export the selected photos."
                )
            }
        }
    }

    fun makeCollage(grid: Int) {
        val selected = selectedImages()
        if (selected.isEmpty()) {
            _operation.value = ExportOperation.Failed("Select photos to create a collage.")
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            _operation.value = ExportOperation.Working
            try {
                val bitmap = exportService.makeCollage(
                    selected.map { Uri.parse(it.uri) },
                    grid
                )
                _collage.value?.takeUnless(Bitmap::isRecycled)?.recycle()
                _collage.value = bitmap
                _operation.value = ExportOperation.Idle
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _operation.value = ExportOperation.Failed(
                    exception.message ?: "Unable to create the collage."
                )
            }
        }
    }

    fun saveCollage() {
        val bitmap = _collage.value
        if (bitmap == null || bitmap.isRecycled) {
            _operation.value = ExportOperation.Failed("Create a collage before saving it.")
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            _operation.value = ExportOperation.Working
            try {
                val uri = exportService.saveCollage(bitmap)
                _collage.value = null
                bitmap.recycle()
                _operation.value = ExportOperation.Completed(
                    uri,
                    "Collage saved to Pictures/MirrorArchive."
                )
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _operation.value = ExportOperation.Failed(
                    exception.message ?: "Unable to save the collage."
                )
            }
        }
    }

    fun dismissResult() {
        if (_operation.value is ExportOperation.Completed ||
            _operation.value is ExportOperation.Failed
        ) {
            _operation.value = ExportOperation.Idle
        }
    }

    private fun selectedImages(): List<ImageItem> {
        val ids = _selectedIds.value
        return images.value.filter { it.id in ids }
    }

    override fun onCleared() {
        _collage.value?.takeUnless(Bitmap::isRecycled)?.recycle()
        super.onCleared()
    }
}
