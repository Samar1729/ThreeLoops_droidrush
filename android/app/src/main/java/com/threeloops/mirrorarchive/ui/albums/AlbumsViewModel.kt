package com.threeloops.mirrorarchive.ui.albums

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.threeloops.mirrorarchive.contracts.AlbumSummary
import com.threeloops.mirrorarchive.contracts.AlbumType
import com.threeloops.mirrorarchive.contracts.ImageItem
import com.threeloops.mirrorarchive.contracts.ScanPhase
import com.threeloops.mirrorarchive.contracts.ScanProgress
import com.threeloops.mirrorarchive.contracts.ScanState
import com.threeloops.mirrorarchive.data.repo.ImageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * ViewModel serving Albums tab and category details.
 * Owner: Aditya
 */
@HiltViewModel
class AlbumsViewModel @Inject constructor(
    private val repository: ImageRepository
) : ViewModel() {

    val albumSummaries: StateFlow<List<AlbumSummary>> = repository.albumSummaries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val scanState: StateFlow<ScanState> = repository.scanState()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            ScanProgress(ScanPhase.COMPLETE, 128, 128)
        )

    fun getAlbumImages(type: AlbumType): StateFlow<List<ImageItem>> {
        return repository.album(type)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    fun getImage(imageId: Long): ImageItem? {
        return repository.items(listOf(imageId)).firstOrNull()
    }
}
