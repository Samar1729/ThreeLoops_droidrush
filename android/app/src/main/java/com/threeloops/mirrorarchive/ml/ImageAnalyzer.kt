package com.threeloops.mirrorarchive.ml

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.label.ImageLabeler
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.threeloops.mirrorarchive.albums.AlbumRules
import com.threeloops.mirrorarchive.data.model.FaceInfo
import com.threeloops.mirrorarchive.data.model.LabelResult
import kotlinx.coroutines.tasks.await

/**
 * 3.7 Image Analyzer (ML Kit)
 * Owner: Aditya
 *
 * Thin, suspend-friendly wrappers over three bundled ML Kit clients (image-labeling,
 * face-detection, text-recognition). Bundled artifacts work 100% offline and need
 * no INTERNET permission.
 */
class ImageAnalyzer {

    private val labeler: ImageLabeler by lazy {
        ImageLabeling.getClient(
            ImageLabelerOptions.Builder()
                .setConfidenceThreshold(0.6f)
                .build()
        )
    }

    private val faceDetector: FaceDetector by lazy {
        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .build()
        FaceDetection.getClient(options)
    }

    private val textRecognizer: TextRecognizer by lazy {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }

    /**
     * Labels: Confidence cut-off starts at 0.6; output lowercased and de-duplicated.
     */
    suspend fun labels(bmp: Bitmap): List<LabelResult> {
        val image = InputImage.fromBitmap(bmp, 0)
        val result = labeler.process(image).await()
        return result
            .filter { it.confidence >= 0.6f }
            .map { LabelResult(it.text.lowercase(), it.confidence) }
            .distinctBy { it.text }
    }

    /**
     * Faces: FAST mode with classification enabled (smile and eyes-open probabilities).
     */
    suspend fun faces(bmp: Bitmap): List<FaceInfo> {
        val image = InputImage.fromBitmap(bmp, 0)
        val result = faceDetector.process(image).await()
        return result.map { face ->
            FaceInfo(
                box = face.boundingBox,
                smileProb = face.smilingProbability,
                eyesOpenProb = when {
                    face.leftEyeOpenProbability != null && face.rightEyeOpenProbability != null ->
                        (face.leftEyeOpenProbability!! + face.rightEyeOpenProbability!!) / 2f
                    else -> face.leftEyeOpenProbability ?: face.rightEyeOpenProbability
                }
            )
        }
    }

    /**
     * OCR: Latin recognizer on the bitmap; empty string when nothing found.
     */
    suspend fun ocr(bmp: Bitmap): String {
        val image = InputImage.fromBitmap(bmp, 0)
        val result = textRecognizer.process(image).await()
        return result.text
    }

    /**
     * Gate: OCR runs only on images whose labels look like text or paper.
     */
    fun isTextLike(labels: List<LabelResult>): Boolean {
        val labelTexts = labels.map { it.text.lowercase() }
        return labelTexts.any { label ->
            AlbumRules.textLikeLabels.any { target -> label.contains(target) }
        }
    }

    /**
     * One dummy call per client at app start so the first real image is not slow.
     */
    suspend fun warmUp() {
        try {
            val dummyBmp = Bitmap.createBitmap(32, 32, Bitmap.Config.ARGB_8888)
            val dummyImage = InputImage.fromBitmap(dummyBmp, 0)
            labeler.process(dummyImage).await()
            faceDetector.process(dummyImage).await()
            textRecognizer.process(dummyImage).await()
            dummyBmp.recycle()
        } catch (_: Exception) {
            // Warm-up failure ignored
        }
    }

    fun close() {
        labeler.close()
        faceDetector.close()
        textRecognizer.close()
    }
}
