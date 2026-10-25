package com.threeloops.mirrorarchive.ml

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.threeloops.mirrorarchive.contracts.FaceInfo
import com.threeloops.mirrorarchive.contracts.LabelResult
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Production on-device ML Kit image analyzer.
 * Uses bundled offline models with zero network permissions.
 */
@Singleton
class MlKitImageAnalyzer @Inject constructor() : ImageAnalyzer {

    private val labeler by lazy {
        ImageLabeling.getClient(
            ImageLabelerOptions.Builder()
                .setConfidenceThreshold(0.6f)
                .build()
        )
    }

    private val faceDetector by lazy {
        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .build()
        FaceDetection.getClient(options)
    }

    private val textRecognizer by lazy {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }

    private val textLikeKeywords = setOf(
        "text", "font", "document", "paper", "receipt", "invoice", "menu",
        "bill", "ticket", "newspaper", "book", "handwriting", "screenshot",
        "label", "poster", "presentation", "signage", "blackboard", "whiteboard"
    )

    override suspend fun labels(bmp: Bitmap): List<LabelResult> {
        val image = InputImage.fromBitmap(bmp, 0)
        val rawLabels = labeler.process(image).await()
        return rawLabels
            .filter { it.confidence >= 0.6f }
            .map { LabelResult(text = it.text.lowercase().trim(), confidence = it.confidence) }
            .distinctBy { it.text }
    }

    override suspend fun faces(bmp: Bitmap): List<FaceInfo> {
        val image = InputImage.fromBitmap(bmp, 0)
        val detected = faceDetector.process(image).await()
        return detected.map { face ->
            FaceInfo(
                box = face.boundingBox,
                smileProb = face.smilingProbability,
                eyesOpenProb = when {
                    face.leftEyeOpenProbability != null && face.rightEyeOpenProbability != null ->
                        (face.leftEyeOpenProbability!! + face.rightEyeOpenProbability!!) / 2f
                    face.leftEyeOpenProbability != null -> face.leftEyeOpenProbability
                    face.rightEyeOpenProbability != null -> face.rightEyeOpenProbability
                    else -> null
                }
            )
        }
    }

    override suspend fun ocr(bmp: Bitmap): String {
        val image = InputImage.fromBitmap(bmp, 0)
        val result = textRecognizer.process(image).await()
        return result.text.trim()
    }

    override fun isTextLike(labels: List<LabelResult>): Boolean {
        return labels.any { label ->
            val text = label.text.lowercase()
            textLikeKeywords.any { keyword -> text.contains(keyword) }
        }
    }

    override fun warmUp() {
        val dummy = Bitmap.createBitmap(32, 32, Bitmap.Config.ARGB_8888)
        val dummyImage = InputImage.fromBitmap(dummy, 0)
        labeler.process(dummyImage)
        faceDetector.process(dummyImage)
        textRecognizer.process(dummyImage)
    }

    override fun close() {
        labeler.close()
        faceDetector.close()
        textRecognizer.close()
    }
}
