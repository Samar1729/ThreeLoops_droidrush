package com.threeloops.mirrorarchive.ui.albums

import android.content.Context
import android.content.ContentValues
import android.content.ContentUris
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.util.Log
import android.net.Uri
import com.threeloops.mirrorarchive.contracts.AlbumSummary
import com.threeloops.mirrorarchive.contracts.AlbumType
import com.threeloops.mirrorarchive.contracts.ImageItem
import com.threeloops.mirrorarchive.contracts.MediaRow
import com.threeloops.mirrorarchive.contracts.ScanPhase
import com.threeloops.mirrorarchive.contracts.ScanProgress
import com.threeloops.mirrorarchive.contracts.ScanState
import com.threeloops.mirrorarchive.data.repo.ImageRepository
import com.threeloops.mirrorarchive.data.mediastore.MediaStoreDataSource
import com.threeloops.mirrorarchive.data.mediastore.BitmapLoader
import com.threeloops.mirrorarchive.work.IndexPipeline
import com.threeloops.mirrorarchive.work.ScanController
import com.threeloops.mirrorarchive.work.SyncUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

import android.graphics.Rect
import android.graphics.RectF
import com.threeloops.mirrorarchive.contracts.FaceInfo
import com.threeloops.mirrorarchive.contracts.LabelResult
import com.threeloops.mirrorarchive.data.db.AppDatabase
import com.threeloops.mirrorarchive.data.db.FaceEntity
import com.threeloops.mirrorarchive.data.db.ImageEntity
import com.threeloops.mirrorarchive.data.db.LabelEntity
import com.threeloops.mirrorarchive.data.demo.DisplayPhoto
import com.threeloops.mirrorarchive.data.demo.SampleMediaSeed
import com.threeloops.mirrorarchive.ui.ledger.RaseedParser
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

/**
 * ViewModel serving Albums tab and category details.
 * Owner: Aditya
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AlbumsViewModel @Inject constructor(
    private val repository: ImageRepository,
    private val mediaStore: MediaStoreDataSource,
    private val bitmapLoader: BitmapLoader,
    private val scanController: ScanController,
    private val syncUseCase: SyncUseCase,
    private val database: AppDatabase,
    private val indexPipeline: IndexPipeline
) : ViewModel() {

    private val _devicePhotoCount = MutableStateFlow<Int?>(null)
    val devicePhotoCount = _devicePhotoCount
    private val _loaderProof = MutableStateFlow<String?>(null)
    val loaderProof = _loaderProof

    val isImporting = MutableStateFlow(false)
    val importMessage = MutableStateFlow<String?>(null)

    val selectedFilter = MutableStateFlow("All")

    fun importPhoto(context: Context, sourceUri: Uri) {
        viewModelScope.launch {
            isImporting.value = true
            importMessage.value = "Analyzing photo with on-device ML & OCR..."
            try {
                val mediaRow = withContext(Dispatchers.IO) {
                    val time = System.currentTimeMillis()
                    val fileName = "imported_photo_${time}.jpg"

                    val values = ContentValues().apply {
                        put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                        put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM/Camera")
                        put(MediaStore.Images.Media.DATE_ADDED, time / 1000L)
                        put(MediaStore.Images.Media.DATE_TAKEN, time)
                    }

                    val destUri = try {
                        context.contentResolver.insert(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            values
                        ) ?: sourceUri
                    } catch (e: Exception) {
                        sourceUri
                    }

                    if (destUri != sourceUri) {
                        try {
                            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                                context.contentResolver.openOutputStream(destUri)?.use { output ->
                                    input.copyTo(output)
                                }
                            }
                        } catch (e: Exception) {
                            Log.w("AlbumsViewModel", "Could not copy to MediaStore URI, using sourceUri", e)
                        }
                    }

                    val mediaId = try {
                        ContentUris.parseId(destUri)
                    } catch (e: Exception) {
                        time
                    }

                    MediaRow(
                        mediaId = mediaId,
                        uri = destUri,
                        displayName = fileName,
                        relativePath = "DCIM/Camera/",
                        dateAdded = time / 1000L,
                        dateTaken = time,
                        generationModified = 1L,
                        width = 1920,
                        height = 1080,
                        sizeBytes = 1024L * 1024L,
                        mimeType = "image/jpeg"
                    )
                }

                val indexed = indexPipeline.processOne(mediaRow)
                if (indexed != null) {
                    repository.saveBatch(listOf(indexed), checkpointId = mediaRow.mediaId)
                    _devicePhotoCount.value = (_devicePhotoCount.value ?: 0) + 1
                    val ocrLen = indexed.ocrText?.length ?: 0
                    val faceCount = indexed.faceCount
                    val labelCount = indexed.labels.size
                    importMessage.value = "Analyzed! $labelCount tags, $faceCount faces, OCR: $ocrLen chars"
                } else {
                    importMessage.value = "Processing completed."
                }
            } catch (e: Exception) {
                Log.e("AlbumsViewModel", "Failed to import photo", e)
                importMessage.value = "Import error: ${e.message}"
            } finally {
                isImporting.value = false
            }
        }
    }

    init {
        viewModelScope.launch {
            _devicePhotoCount.value = mediaStore.count()
        }
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

    fun startScanNow() = scanController.start(ignoreConstraints = true)

    fun syncNewPhotos() = syncUseCase.triggerNow()

    fun cancelScan() = scanController.cancel()

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

    val displayedPhotos: StateFlow<List<DisplayPhoto>> = selectedFilter
        .flatMapLatest { filter ->
            val flow = when (filter.lowercase()) {
                "receipts" -> repository.album(AlbumType.RECEIPTS)
                "portraits" -> repository.peopleImages()
                "screenshots" -> repository.album(AlbumType.SCREENSHOTS)
                "documents" -> repository.album(AlbumType.DOCUMENTS)
                else -> repository.allImages()
            }
            flow.map { items ->
                withContext(Dispatchers.IO) {
                    val mapped = mapItemsToDisplayPhotos(items)
                    if (mapped.isEmpty()) {
                        SampleMediaSeed.filterByCategory(filter)
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

    fun getImage(imageId: Long): ImageItem? {
        return repository.items(listOf(imageId)).firstOrNull()
    }
}
