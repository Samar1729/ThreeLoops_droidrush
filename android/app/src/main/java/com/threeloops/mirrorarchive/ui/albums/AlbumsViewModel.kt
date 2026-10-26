package com.threeloops.mirrorarchive.ui.albums

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.util.Log
import android.net.Uri
import com.threeloops.mirrorarchive.contracts.AlbumSummary
import com.threeloops.mirrorarchive.contracts.AlbumType
import com.threeloops.mirrorarchive.contracts.ImageItem
import com.threeloops.mirrorarchive.contracts.ScanPhase
import com.threeloops.mirrorarchive.contracts.ScanProgress
import com.threeloops.mirrorarchive.contracts.ScanState
import com.threeloops.mirrorarchive.data.repo.ImageRepository
import com.threeloops.mirrorarchive.data.mediastore.MediaStoreDataSource
import com.threeloops.mirrorarchive.data.mediastore.BitmapLoader
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

/**
 * ViewModel serving Albums tab and category details.
 * Owner: Aditya
 */
@HiltViewModel
class AlbumsViewModel @Inject constructor(
    private val repository: ImageRepository,
    private val mediaStore: MediaStoreDataSource,
    private val bitmapLoader: BitmapLoader
) : ViewModel() {

    private val _devicePhotoCount = MutableStateFlow<Int?>(null)
    val devicePhotoCount = _devicePhotoCount
    private val _loaderProof = MutableStateFlow<String?>(null)
    val loaderProof = _loaderProof

    init {
        viewModelScope.launch { _devicePhotoCount.value = mediaStore.count() }
    }

    fun logFirstTwoPages() {
        viewModelScope.launch {
            val first = mediaStore.query(afterId = 0L, limit = 50)
            val second = mediaStore.query(afterId = first.lastOrNull()?.mediaId ?: 0L, limit = 50)
            val firstIds = first.map { it.mediaId }
            val secondIds = second.map { it.mediaId }
            val all = firstIds + secondIds
            val ascending = all.zipWithNext().all { (a, b) -> a < b }
            val noOverlap = firstIds.toSet().intersect(secondIds.toSet()).isEmpty()
            Log.d("MirrorMediaStore", "page1 size=${first.size}, ids=$firstIds")
            Log.d("MirrorMediaStore", "page2 size=${second.size}, ids=$secondIds")
            Log.d("MirrorMediaStore", "strictlyAscending=$ascending, noOverlap=$noOverlap")
        }
    }

    fun testLoader() {
        viewModelScope.launch {
            val firstPhoto = mediaStore.query(afterId = 0L, limit = 1).firstOrNull()
            val photoResult = if (firstPhoto == null) {
                "No MediaStore photo available"
            } else {
                withContext(Dispatchers.IO) {
                    val bitmap = bitmapLoader.load(firstPhoto.uri)
                    try {
                        if (bitmap == null) "photo load returned null"
                        else {
                            val maxSide = maxOf(bitmap.width, bitmap.height)
                            "photo ${bitmap.width}×${bitmap.height}, max side=$maxSide (<=512: ${maxSide <= 512})"
                        }
                    } finally {
                        bitmapLoader.recycle(bitmap)
                    }
                }
            }
            val badUriReturnedNull = withContext(Dispatchers.IO) {
                bitmapLoader.load(Uri.parse("content://media/external/images/media/-1")) == null
            }
            _loaderProof.value = "$photoResult; bad URI returned null: $badUriReturnedNull"
        }
    }

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
