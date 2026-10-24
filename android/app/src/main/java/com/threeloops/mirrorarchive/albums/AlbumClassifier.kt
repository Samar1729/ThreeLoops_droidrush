package com.threeloops.mirrorarchive.albums

import com.threeloops.mirrorarchive.contracts.AlbumType
import com.threeloops.mirrorarchive.contracts.LabelResult

/**
 * Classifies images into smart album categories.
 */
object AlbumClassifier {
    /**
     * Classifies media into smart album categories.
     *
     * @param labels Detected image labels.
     * @param path File path or display name.
     * @param ocr Extracted OCR text string.
     * @return Set of matching [AlbumType] categories.
     */
    fun classify(
        labels: List<LabelResult>,
        path: String,
        ocr: String,
        faceCount: Int = 0
    ): Set<AlbumType> = emptySet()

    /**
     * Explains the classification decision in human-readable format.
     *
     * @param labels Detected image labels.
     * @param path File path or display name.
     * @param ocr Extracted OCR text string.
     * @param faceCount Detected face count.
     * @return Human-readable explanation string.
     */
    fun explain(
        labels: List<LabelResult>,
        path: String,
        ocr: String,
        faceCount: Int = 0
    ): String = ""
}
