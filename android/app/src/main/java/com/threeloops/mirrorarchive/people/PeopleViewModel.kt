package com.threeloops.mirrorarchive.people

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.threeloops.mirrorarchive.contracts.FaceInfo
import com.threeloops.mirrorarchive.contracts.ImageItem
import com.threeloops.mirrorarchive.data.repo.ImageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * 3.10 Faces & People
 * Owner: Aditya
 */
@HiltViewModel
class PeopleViewModel @Inject constructor(
    private val repository: ImageRepository
) : ViewModel() {

    val peopleImages: StateFlow<List<ImageItem>> = repository.peopleImages()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun facesFor(imageId: Long): List<FaceInfo> {
        return repository.facesFor(imageId)
    }

    /**
     * Combines smile and eyes-open into 0..1 for the best-shot scoring in 3.14
     */
    fun faceQuality(imageId: Long): Float {
        val faces = facesFor(imageId)
        if (faces.isEmpty()) return 0.5f

        val primary = faces.first()
        val smile = primary.smileProb ?: 0.5f
        val eyes = primary.eyesOpenProb ?: 0.5f
        return (smile * 0.4f + eyes * 0.6f).coerceIn(0f, 1f)
    }
}
