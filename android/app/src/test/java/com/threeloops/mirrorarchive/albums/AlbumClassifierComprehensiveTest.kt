package com.threeloops.mirrorarchive.albums

import com.threeloops.mirrorarchive.contracts.AlbumType
import com.threeloops.mirrorarchive.contracts.LabelResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AlbumClassifierComprehensiveTest {

    @Test
    fun testScreenshotsClassification() {
        val labels = listOf(LabelResult("screenshot", 0.95f))
        val result = AlbumClassifier.classify(
            labels = labels,
            path = "Pictures/Screenshots/Screenshot_2026.png",
            ocr = null,
            faceCount = 0
        )
        assertTrue(result.contains(AlbumType.SCREENSHOTS))

        val explanation = AlbumClassifier.explain(
            labels = labels,
            path = "Pictures/Screenshots/Screenshot_2026.png",
            ocr = null,
            faceCount = 0
        )
        assertTrue(explanation.contains("SCREENSHOTS"))
    }

    @Test
    fun testReceiptsClassificationWithTwoKeywords() {
        val labels = listOf(LabelResult("paper", 0.92f), LabelResult("text", 0.88f))
        val ocr = "STARBUCKS INVOICE TOTAL 450 GSTIN 27AAACB2184A1Z5"
        val result = AlbumClassifier.classify(
            labels = labels,
            path = "DCIM/Camera/IMG_001.jpg",
            ocr = ocr,
            faceCount = 0
        )
        assertTrue(result.contains(AlbumType.RECEIPTS))

        val explanation = AlbumClassifier.explain(
            labels = labels,
            path = "DCIM/Camera/IMG_001.jpg",
            ocr = ocr,
            faceCount = 0
        )
        assertTrue(explanation.contains("RECEIPTS"))
        assertTrue(explanation.contains("invoice") || explanation.contains("total") || explanation.contains("gst"))
    }

    @Test
    fun testDocumentsClassificationLongTextNonReceipt() {
        val labels = listOf(LabelResult("document", 0.91f), LabelResult("paper", 0.85f))
        val ocr = "This is a long legal contract paragraph describing software engineering specifications, on-device machine learning pipelines, and database architecture exceeding eighty characters in total length."
        val result = AlbumClassifier.classify(
            labels = labels,
            path = "Download/Contract.pdf",
            ocr = ocr,
            faceCount = 0
        )
        assertTrue(result.contains(AlbumType.DOCUMENTS))

        val explanation = AlbumClassifier.explain(
            labels = labels,
            path = "Download/Contract.pdf",
            ocr = ocr,
            faceCount = 0
        )
        assertTrue(explanation.contains("DOCUMENTS"))
    }

    @Test
    fun testFoodClassification() {
        val labels = listOf(LabelResult("pizza", 0.96f), LabelResult("dish", 0.89f))
        val result = AlbumClassifier.classify(
            labels = labels,
            path = "DCIM/Camera/IMG_Food.jpg",
            ocr = null,
            faceCount = 0
        )
        assertTrue(result.contains(AlbumType.FOOD))

        val explanation = AlbumClassifier.explain(
            labels = labels,
            path = "DCIM/Camera/IMG_Food.jpg",
            ocr = null,
            faceCount = 0
        )
        assertTrue(explanation.contains("FOOD"))
    }

    @Test
    fun testPeopleClassification() {
        val labels = listOf(LabelResult("indoor", 0.80f))
        val result = AlbumClassifier.classify(
            labels = labels,
            path = "DCIM/Camera/IMG_Portrait.jpg",
            ocr = null,
            faceCount = 2
        )
        assertTrue(result.contains(AlbumType.PEOPLE))

        val explanation = AlbumClassifier.explain(
            labels = labels,
            path = "DCIM/Camera/IMG_Portrait.jpg",
            ocr = null,
            faceCount = 2
        )
        assertTrue(explanation.contains("PEOPLE"))
        assertTrue(explanation.contains("2 face(s)"))
    }
}
