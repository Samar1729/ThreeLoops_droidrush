package com.threeloops.mirrorarchive.albums

import com.threeloops.mirrorarchive.contracts.AlbumType
import com.threeloops.mirrorarchive.contracts.LabelResult

/**
 * Classifies images into smart album categories.
 */
object AlbumClassifier {

    private val RECEIPT_KEYWORDS = setOf(
        "total", "subtotal", "invoice", "gst", "gstin", "tax", "amount", "bill", "receipt", "qty", "upi", "card"
    )

    private val FOOD_KEYWORDS = setOf(
        "food", "dish", "cuisine", "meal", "fruit", "vegetable", "dessert", "beverage", "coffee", "pizza", "burger"
    )

    private val PEOPLE_LABELS = setOf(
        "person", "face", "smile", "selfie", "portrait", "human", "people"
    )

    private val TEXT_LIKE_LABELS = setOf(
        "text", "font", "document", "paper", "receipt", "invoice", "menu",
        "bill", "ticket", "newspaper", "book", "handwriting", "screenshot",
        "label", "poster", "presentation", "signage", "blackboard", "whiteboard",
        "diagram", "number", "writing"
    )

    /**
     * Checks if label results match text-like criteria.
     */
    fun isTextLike(labels: List<LabelResult>): Boolean {
        return labels.any { label ->
            val text = label.text.lowercase().trim()
            TEXT_LIKE_LABELS.any { text.contains(it) }
        }
    }

    /**
     * Classifies media into smart album categories based on 3 signals:
     * labels, path/filename, and OCR text content.
     */
    fun classify(
        labels: List<LabelResult>,
        path: String,
        ocr: String?,
        faceCount: Int = 0
    ): Set<AlbumType> {
        val result = mutableSetOf<AlbumType>()
        val lowerPath = path.lowercase().trim()
        val safeOcr = ocr?.lowercase()?.trim().orEmpty()
        val labelTexts = labels.map { it.text.lowercase().trim() }.toSet()

        // 1. SCREENSHOTS: Path contains 'Screenshots' or starts with 'Screenshot_'
        val isScreenshot = lowerPath.contains("screenshots") ||
                path.substringAfterLast("/").startsWith("Screenshot_", ignoreCase = true) ||
                labelTexts.any { it.contains("screenshot") }
        if (isScreenshot) {
            result.add(AlbumType.SCREENSHOTS)
        }

        // 2. RECEIPTS: isTextLike(labels) AND 2+ financial keywords
        val ocrTokens = safeOcr.split(Regex("[\\s,;:_\\-\n\r/]+")).filter { it.isNotEmpty() }.toSet()
        val matchedFinancialTokens = RECEIPT_KEYWORDS.filter { token ->
            ocrTokens.contains(token) || safeOcr.contains(token)
        }
        val isTextContent = isTextLike(labels) || safeOcr.isNotEmpty()
        val isReceipt = (isTextContent && matchedFinancialTokens.size >= 2) || labelTexts.contains("receipt")
        if (isReceipt) {
            result.add(AlbumType.RECEIPTS)
        }

        // 3. DOCUMENTS: isTextLike AND ocr length > 80 AND NOT receipts
        val isDocument = isTextContent && safeOcr.length > 80 && !isReceipt
        if (isDocument) {
            result.add(AlbumType.DOCUMENTS)
        }

        // 4. FOOD: labels intersect culinary keywords
        val matchedFood = labelTexts.intersect(FOOD_KEYWORDS)
        if (matchedFood.isNotEmpty()) {
            result.add(AlbumType.FOOD)
        }

        // 5. PEOPLE: faceCount > 0 OR labels intersect people labels
        val matchedPeople = labelTexts.intersect(PEOPLE_LABELS)
        if (faceCount > 0 || matchedPeople.isNotEmpty()) {
            result.add(AlbumType.PEOPLE)
        }

        return result
    }

    /**
     * Explains the classification decision with a human-readable audit trail.
     */
    fun explain(
        labels: List<LabelResult>,
        path: String,
        ocr: String?,
        faceCount: Int = 0
    ): String {
        val safeOcr = ocr?.lowercase()?.trim().orEmpty()
        val labelTexts = labels.map { it.text.lowercase().trim() }.toSet()
        val ocrTokens = safeOcr.split(Regex("[\\s,;:_\\-\n\r/]+")).filter { it.isNotEmpty() }.toSet()
        val explanations = mutableListOf<String>()

        // Check Screenshots
        if (path.lowercase().contains("screenshots") || path.substringAfterLast("/").startsWith("Screenshot_", true)) {
            explanations.add("SCREENSHOTS: Path matched '${path.substringAfterLast("/")}'")
        }

        // Check Receipts
        val matchedFinancial = RECEIPT_KEYWORDS.filter { ocrTokens.contains(it) || safeOcr.contains(it) }
        val textLabel = labels.firstOrNull { l -> TEXT_LIKE_LABELS.any { l.text.contains(it, true) } }
        if (matchedFinancial.size >= 2) {
            val labelStr = textLabel?.let { " + label '${it.text}' (${String.format(java.util.Locale.ROOT, "%.2f", it.confidence)})" }.orEmpty()
            explanations.add("RECEIPTS: OCR matched tokens [${matchedFinancial.take(3).joinToString(", ")}]$labelStr")
        }

        // Check Documents
        if (safeOcr.length > 80 && matchedFinancial.size < 2) {
            explanations.add("DOCUMENTS: Text-like content with OCR length ${safeOcr.length} chars (non-receipt)")
        }

        // Check Food
        val foodMatches = labelTexts.intersect(FOOD_KEYWORDS)
        if (foodMatches.isNotEmpty()) {
            val topFood = labels.firstOrNull { it.text.lowercase().trim() in foodMatches }
            val conf = topFood?.let { String.format(java.util.Locale.ROOT, "%.2f", it.confidence) } ?: "0.80"
            explanations.add("FOOD: Detected label '${foodMatches.first()}' ($conf) in culinary ontology")
        }

        // Check People
        if (faceCount > 0) {
            explanations.add("PEOPLE: Detected $faceCount face(s) via on-device facial landmark analyzer")
        } else if (labelTexts.intersect(PEOPLE_LABELS).isNotEmpty()) {
            explanations.add("PEOPLE: Label matched '${labelTexts.intersect(PEOPLE_LABELS).first()}'")
        }

        return if (explanations.isEmpty()) {
            "UNCLASSIFIED: Signals did not meet category confidence thresholds"
        } else {
            explanations.joinToString(" • ")
        }
    }
}
