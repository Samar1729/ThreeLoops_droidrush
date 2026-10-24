package com.threeloops.mirrorarchive.albums

import com.threeloops.mirrorarchive.contracts.AlbumType
import com.threeloops.mirrorarchive.contracts.LabelResult

/**
 * Classifies images into smart album categories.
 */
object AlbumClassifier {

    private val FINANCIAL_TOKENS = setOf(
        "total", "amount", "subtotal", "tax", "gst", "vat", "invoice", "receipt",
        "balance", "payment", "due", "price", "$", "€", "£", "₹", "checkout"
    )

    private val FOOD_KEYWORDS = setOf(
        "food", "dish", "meal", "cuisine", "snack", "dessert", "pasta", "pizza",
        "burger", "salad", "beverage", "drink", "coffee", "breakfast", "lunch", "dinner"
    )

    private val DOCUMENT_KEYWORDS = setOf(
        "document", "text", "paper", "agreement", "contract", "certificate", "resume",
        "form", "letter", "sheet", "page"
    )

    /**
     * Classifies media into smart album categories.
     *
     * @param labels Detected image labels.
     * @param path File path or display name.
     * @param ocr Extracted OCR text string.
     * @param faceCount Detected face count.
     * @return Set of matching [AlbumType] categories.
     */
    fun classify(
        labels: List<LabelResult>,
        path: String,
        ocr: String,
        faceCount: Int = 0
    ): Set<AlbumType> {
        val result = mutableSetOf<AlbumType>()
        val lowerPath = path.lowercase()
        val lowerOcr = ocr.lowercase()
        val labelTexts = labels.map { it.text.lowercase() }.toSet()

        // 1. Screenshots
        val isScreenshot = lowerPath.contains("screenshot") ||
                labelTexts.any { it.contains("screenshot") }
        if (isScreenshot) {
            result.add(AlbumType.SCREENSHOTS)
        }

        // 2. Receipts
        val ocrWords = lowerOcr.split(Regex("[\\s,;:\n\r]+"))
        val financialCount = FINANCIAL_TOKENS.count { token ->
            if (token.length == 1) lowerOcr.contains(token) else ocrWords.contains(token)
        }
        val isReceipt = labelTexts.contains("receipt") || financialCount >= 2
        if (isReceipt) {
            result.add(AlbumType.RECEIPTS)
        }

        // 3. Documents
        val hasDocumentLabel = labelTexts.any { it in DOCUMENT_KEYWORDS }
        val isDocumentPath = lowerPath.endsWith(".pdf") || lowerPath.contains("/documents/") || lowerPath.contains("document")
        val isLongTextDoc = lowerOcr.length > 50 && !isReceipt
        if ((hasDocumentLabel || isDocumentPath || isLongTextDoc) && !isReceipt) {
            result.add(AlbumType.DOCUMENTS)
        }

        // 4. Food
        val isFood = labelTexts.any { it in FOOD_KEYWORDS }
        if (isFood) {
            result.add(AlbumType.FOOD)
        }

        // 5. People
        val isPeople = faceCount > 0 || labelTexts.any { it in setOf("person", "human", "people", "face", "portrait", "selfie") && faceCount > 0 }
        if (faceCount > 0) {
            result.add(AlbumType.PEOPLE)
        }

        return result
    }

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
    ): String {
        val albums = classify(labels, path, ocr, faceCount)
        return if (albums.isEmpty()) {
            "No album category matched"
        } else {
            "Matched albums: ${albums.joinToString { it.name }}"
        }
    }
}
