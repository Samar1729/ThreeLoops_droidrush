package com.threeloops.mirrorarchive.people

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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 3.10 Faces & People
 * Owner: Aditya
 */
@HiltViewModel
class PeopleViewModel @Inject constructor(
    private val repository: ImageRepository,
    private val database: AppDatabase
) : ViewModel() {

    val peopleImages: StateFlow<List<ImageItem>> = repository.peopleImages()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val portraitPhotos: StateFlow<List<DisplayPhoto>> = repository.peopleImages()
        .map { items ->
            withContext(Dispatchers.IO) {
                if (items.isEmpty()) {
                    DemoSeedData.peoplePhotos
                } else {
                    val ids = items.map { it.id }
                    val entities = database.imageDao().imagesByIds(ids).associateBy { it.id }
                    val faces = database.faceDao().facesForImages(ids).groupBy { it.imageId }
                    val labels = database.labelDao().labelsForImages(ids).groupBy { it.imageId }

                    items.mapNotNull { item ->
                        val entity = entities[item.id] ?: return@mapNotNull null
                        val imgFaces = faces[item.id].orEmpty()
                        val imgLabels = labels[item.id].orEmpty()
                        val primaryFace = imgFaces.firstOrNull()

                        val smile = primaryFace?.smileProb ?: 0.5f
                        val eyes = primaryFace?.eyesOpenProb ?: 0.5f
                        val quality = (smile * 0.4f + eyes * 0.6f).coerceIn(0f, 1f)

                        DisplayPhoto(
                            id = entity.id,
                            imageUrl = entity.uri,
                            drawableRes = 0,
                            title = entity.displayName.substringBeforeLast(".").replace("_", " "),
                            subtitle = "Smile ${(smile * 100).toInt()}% • Eyes ${(eyes * 100).toInt()}%",
                            tag = "Quality ${(quality * 100).toInt()}%",
                            ocrSnippet = null,
                            faceBox = primaryFace?.let { RectF(it.left.toFloat(), it.top.toFloat(), it.right.toFloat(), it.bottom.toFloat()) },
                            score = quality,
                            category = "PORTRAITS",
                            blurScore = entity.blurScore,
                            smileProb = smile,
                            eyesOpenProb = eyes,
                            labels = imgLabels.map { LabelResult(it.label, it.confidence) },
                            faces = imgFaces.map { FaceInfo(Rect(it.left, it.top, it.right, it.bottom), it.smileProb, it.eyesOpenProb) }
                        )
                    }.sortedByDescending { it.score }
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DemoSeedData.peoplePhotos)

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
