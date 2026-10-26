package com.threeloops.mirrorarchive.work

import android.graphics.Bitmap
import com.threeloops.mirrorarchive.albums.AlbumClassifier
import com.threeloops.mirrorarchive.contracts.IndexedImage
import com.threeloops.mirrorarchive.contracts.MediaRow
import com.threeloops.mirrorarchive.data.db.SkippedDao
import com.threeloops.mirrorarchive.data.mediastore.ThumbnailLoader
import com.threeloops.mirrorarchive.data.repo.PIPELINE_VERSION
import com.threeloops.mirrorarchive.hash.PerceptualHasher
import com.threeloops.mirrorarchive.ml.ImageAnalyzer
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import java.util.logging.Level
import java.util.logging.Logger

/** Orchestrates media loading, analysis, classification, and hashing for one image row. */
@Singleton
class IndexPipeline @Inject constructor(
    private val loader: ThumbnailLoader,
    private val analyzer: ImageAnalyzer,
    private val hasher: PerceptualHasher,
    private val skippedDao: SkippedDao
) {
    /** Process one MediaStore row, returning null and recording a skip on failure. */
    suspend fun processOne(row: MediaRow): IndexedImage? = withContext(Dispatchers.IO) {
        var bitmap: Bitmap? = null
        var ocrBitmap: Bitmap? = null
        try {
            bitmap = timed("thumbnail") { loader.load(row.uri, 512) }
            if (bitmap == null) {
                recordSkipped(row.mediaId, "Thumbnail load returned null")
                return@withContext null
            }

            val sourceBitmap = checkNotNull(bitmap)
            val labels = timed("labels") { analyzer.labels(sourceBitmap) }
            val faces = timed("faces") { analyzer.faces(sourceBitmap) }
            val ocr = if (analyzer.isTextLike(labels)) {
                ocrBitmap = timed("ocr_thumbnail") { loader.loadForOcr(row.uri) }
                ocrBitmap?.let { loaded -> timed("ocr") { analyzer.ocr(loaded) } }.orEmpty()
            } else {
                ""
            }
            val albums = timed("albums") {
                AlbumClassifier.classify(
                    labels = labels,
                    path = "${row.displayName} ${row.relativePath}",
                    ocr = ocr
                )
            }
            val hash = timed("hash") { hasher.hashAll(sourceBitmap) }

            IndexedImage(
                mediaId = row.mediaId,
                uri = row.uri.toString(),
                labels = labels,
                ocrText = ocr,
                dHash = hash.dHash,
                pHash = hash.pHash,
                blurScore = hash.blur,
                faceCount = faces.size,
                displayName = row.displayName,
                relativePath = row.relativePath,
                dateTaken = row.dateTaken,
                generationModified = row.generationModified,
                width = row.width,
                height = row.height,
                sizeBytes = row.sizeBytes,
                faces = faces,
                albums = albums,
                pipelineVersion = PIPELINE_VERSION
            )
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (failure: Exception) {
            LOGGER.log(Level.SEVERE, "Failed to index mediaId=${row.mediaId}", failure)
            recordSkipped(row.mediaId, failure.message ?: failure.javaClass.simpleName)
            null
        } finally {
            try {
                recycleSafely(ocrBitmap)
            } finally {
                recycleSafely(bitmap)
            }
        }
    }

    /** Warm up the configured analyzer. The Application invokes this on a background coroutine. */
    fun warmUp() {
        analyzer.warmUp()
    }

    private suspend fun recordSkipped(mediaId: Long, reason: String) {
        try {
            skippedDao.insert(mediaId, reason)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (failure: Exception) {
            LOGGER.log(Level.SEVERE, "Could not record skipped mediaId=$mediaId", failure)
        }
    }

    private fun recycleSafely(bitmap: Bitmap?) {
        try {
            loader.recycle(bitmap)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (failure: Exception) {
            LOGGER.log(Level.WARNING, "Could not recycle a temporary bitmap", failure)
        }
    }

    /** R-9 metrics hook. Currently pass-through; wrap [block] with MetricsRecorder later. */
    @Suppress("UNUSED_PARAMETER")
    private suspend fun <T> timed(stage: String, block: suspend () -> T): T = block()

    private companion object {
        val LOGGER: Logger = Logger.getLogger(IndexPipeline::class.java.name)
    }
}
