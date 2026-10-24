package com.threeloops.mirrorarchive.ml

import android.graphics.Bitmap
import com.threeloops.mirrorarchive.contracts.FaceInfo
import com.threeloops.mirrorarchive.contracts.LabelResult
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ImageAnalyzer contract for on-device machine learning operations.
 */
interface ImageAnalyzer {
    /**
     * Extracts descriptive labels with confidence scores from the given bitmap.
     */
    suspend fun labels(bmp: Bitmap): List<LabelResult>

    /**
     * Detects human faces and their attribute probabilities from the given bitmap.
     */
    suspend fun faces(bmp: Bitmap): List<FaceInfo>

    /**
     * Extracts Latin text via on-device OCR from the given bitmap.
     */
    suspend fun ocr(bmp: Bitmap): String

    /**
     * Evaluates whether the detected labels indicate text, document, or paper content.
     */
    fun isTextLike(labels: List<LabelResult>): Boolean

    /**
     * Warms up ML Kit inference pipelines asynchronously.
     */
    fun warmUp()

    /**
     * Closes underlying ML Kit client resources.
     */
    fun close()
}

/**
 * Stub implementation of [ImageAnalyzer] returning empty values.
 */
@Singleton
class ImageAnalyzerStub @Inject constructor() : ImageAnalyzer {
    override suspend fun labels(bmp: Bitmap): List<LabelResult> = emptyList()
    override suspend fun faces(bmp: Bitmap): List<FaceInfo> = emptyList()
    override suspend fun ocr(bmp: Bitmap): String = ""
    override fun isTextLike(labels: List<LabelResult>): Boolean = false
    override fun warmUp() {}
    override fun close() {}
}
