package com.threeloops.mirrorarchive.albums

import com.threeloops.mirrorarchive.data.model.AlbumType
import com.threeloops.mirrorarchive.data.model.LabelResult

/**
 * 3.8 Album Classifier
 * Owner: Aditya
 *
 * Decides album memberships from three combined signals: labels, file path / name,
 * and OCR keywords. Pure Kotlin function (no Android types) for fast JVM testing.
 */
object AlbumClassifier {

    /**
     * Frozen signature: The worker passes displayName + relativePath as path
     * and runs OCR before calling it (for text-like images).
     */
    fun classify(
        labels: List<LabelResult>,
        path: String,
        ocr: String,
        faceCount: Int = 0
    ): Set<AlbumType> {
        val albums = mutableSetOf<AlbumType>()
        val lowerPath = path.lowercase()
        val lowerOcr = ocr.lowercase()
        val lowerLabels = labels.map { it.text.lowercase() }

        // 1. SCREENSHOTS: relativePath contains 'Screenshots' OR displayName starts with 'Screenshot_'
        if (lowerPath.contains("screenshots") || lowerPath.contains("screenshot_") || lowerPath.startsWith("screenshot")) {
            albums.add(AlbumType.SCREENSHOTS)
        }

        // Check text-like gate
        val hasTextLabel = lowerLabels.any { l ->
            AlbumRules.textLikeLabels.any { l.contains(it) }
        }

        // Count financial tokens
        val receiptKeywordHits = AlbumRules.receiptKeywords.count { lowerOcr.contains(it) }

        // 2. RECEIPTS: text-like labels AND OCR contains 2+ of [total, subtotal, invoice, gst, tax, amount, bill, receipt, qty]
        val isReceipt = (hasTextLabel || lowerOcr.length > 30) && receiptKeywordHits >= 2
        if (isReceipt) {
            albums.add(AlbumType.RECEIPTS)
        }

        // 3. DOCUMENTS: text-like labels AND OCR length above threshold AND not RECEIPTS
        if ((hasTextLabel || lowerOcr.length > 50) && lowerOcr.length >= AlbumRules.documentMinOcrLength && !isReceipt) {
            albums.add(AlbumType.DOCUMENTS)
        }

        // 4. FOOD: label in [food, dish, cuisine, meal, fruit, vegetable, dessert, ...]
        if (lowerLabels.any { l -> AlbumRules.foodLabels.any { l.contains(it) } }) {
            albums.add(AlbumType.FOOD)
        }

        // 5. PEOPLE: label in [person, face, smile, ...] OR faceCount above 0
        if (lowerLabels.any { l -> AlbumRules.peopleLabels.any { l.contains(it) } } || faceCount > 0) {
            albums.add(AlbumType.PEOPLE)
        }

        return albums
    }

    /**
     * NEW: Human-readable reason, e.g. 'RECEIPTS: ocr hit total, gst + label text'.
     * Great for the demo and for debugging album mistakes.
     */
    fun explain(
        labels: List<LabelResult>,
        path: String,
        ocr: String,
        faceCount: Int = 0
    ): String {
        val lowerPath = path.lowercase()
        val lowerOcr = ocr.lowercase()
        val lowerLabels = labels.map { it.text.lowercase() }
        val reasons = mutableListOf<String>()

        if (lowerPath.contains("screenshots") || lowerPath.contains("screenshot_")) {
            reasons.add("SCREENSHOTS: path matched Screenshots folder or Screenshot_ prefix")
        }

        val matchedReceiptWords = AlbumRules.receiptKeywords.filter { lowerOcr.contains(it) }
        if (matchedReceiptWords.size >= 2) {
            reasons.add("RECEIPTS: OCR hit ${matchedReceiptWords.size} financial tokens [${matchedReceiptWords.take(3).joinToString(", ")}]")
        } else if (lowerOcr.length >= AlbumRules.documentMinOcrLength) {
            reasons.add("DOCUMENTS: text length (${lowerOcr.length} chars) >= threshold (80 chars)")
        }

        val matchedFood = lowerLabels.filter { l -> AlbumRules.foodLabels.any { l.contains(it) } }
        if (matchedFood.isNotEmpty()) {
            reasons.add("FOOD: detected labels [${matchedFood.take(2).joinToString(", ")}]")
        }

        if (faceCount > 0) {
            reasons.add("PEOPLE: ML Kit face detection found $faceCount face(s)")
        } else {
            val matchedPeople = lowerLabels.filter { l -> AlbumRules.peopleLabels.any { l.contains(it) } }
            if (matchedPeople.isNotEmpty()) {
                reasons.add("PEOPLE: detected person label [${matchedPeople.take(2).joinToString(", ")}]")
            }
        }

        return if (reasons.isNotEmpty()) reasons.joinToString(" • ") else "Uncategorized"
    }
}
