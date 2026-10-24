package com.threeloops.mirrorarchive

import com.threeloops.mirrorarchive.albums.AlbumClassifier
import com.threeloops.mirrorarchive.contracts.AlbumType
import com.threeloops.mirrorarchive.contracts.LabelResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Section 10 Test Plan: JVM Unit tests for AlbumClassifier
 * Proves hand-written label lists, paths and OCR strings for each album,
 * including multi-album and negative cases.
 */
class AlbumClassifierTest {

    @Test
    fun testReceiptClassification() {
        val labels = listOf(LabelResult("receipt", 0.95f), LabelResult("paper", 0.90f))
        val path = "DCIM/Camera/IMG_001.jpg"
        val ocr = "STARBUCKS COFFEE\nTotal Amount: $5.40\nGST Tax: $0.50\nInvoice #9821"

        val albums = AlbumClassifier.classify(labels, path, ocr)
        assertTrue("Receipt with 2+ financial tokens must classify as RECEIPTS", albums.contains(AlbumType.RECEIPTS))
    }

    @Test
    fun testScreenshotClassification() {
        val labels = listOf(LabelResult("screenshot", 0.99f))
        val path = "Pictures/Screenshots/Screenshot_20261021.png"
        val ocr = "Settings Wi-Fi Bluetooth"

        val albums = AlbumClassifier.classify(labels, path, ocr)
        assertTrue("Path with Screenshots must classify as SCREENSHOTS", albums.contains(AlbumType.SCREENSHOTS))
    }

    @Test
    fun testDocumentClassification() {
        val labels = listOf(LabelResult("document", 0.95f), LabelResult("text", 0.92f))
        val path = "Documents/Contract.pdf"
        val ocr = "THIS NON-DISCLOSURE AGREEMENT is made between Company A and Company B regarding confidential information. Neither party shall disclose proprietary algorithms or structural designs without prior written approval."

        val albums = AlbumClassifier.classify(labels, path, ocr)
        assertTrue("Long text document without receipt keywords must classify as DOCUMENTS", albums.contains(AlbumType.DOCUMENTS))
        assertTrue("Should not be classified as RECEIPTS", !albums.contains(AlbumType.RECEIPTS))
    }

    @Test
    fun testFoodClassification() {
        val labels = listOf(LabelResult("food", 0.98f), LabelResult("dish", 0.95f), LabelResult("pasta", 0.90f))
        val path = "DCIM/Camera/Dinner.jpg"
        val ocr = ""

        val albums = AlbumClassifier.classify(labels, path, ocr)
        assertTrue("Food labels must classify as FOOD", albums.contains(AlbumType.FOOD))
    }

    @Test
    fun testPeopleClassification() {
        val labels = listOf(LabelResult("portrait", 0.95f))
        val path = "DCIM/Camera/Portrait.jpg"
        val ocr = ""

        val albums = AlbumClassifier.classify(labels, path, ocr, faceCount = 1)
        assertTrue("Face count > 0 must classify as PEOPLE", albums.contains(AlbumType.PEOPLE))
    }

    @Test
    fun testMultiAlbumClassification() {
        // Screenshot of a receipt (tests multi-album contract from Section 3.8!)
        val labels = listOf(LabelResult("screenshot", 0.98f), LabelResult("receipt", 0.95f))
        val path = "Pictures/Screenshots/Uber_Receipt.png"
        val ocr = "Uber Technologies\nTotal: $24.50\nSubtotal: $20.00\nTax: $4.50"

        val albums = AlbumClassifier.classify(labels, path, ocr)
        assertTrue("Must classify as SCREENSHOTS", albums.contains(AlbumType.SCREENSHOTS))
        assertTrue("Must classify as RECEIPTS", albums.contains(AlbumType.RECEIPTS))
        assertEquals(2, albums.size)
    }

    @Test
    fun testNegativeCase() {
        // Nature photo without text or faces
        val labels = listOf(LabelResult("sky", 0.95f), LabelResult("mountain", 0.92f))
        val path = "DCIM/Camera/Mountain.jpg"
        val ocr = ""

        val albums = AlbumClassifier.classify(labels, path, ocr)
        assertTrue("Landscape photo should not classify into any of the 5 albums", albums.isEmpty())
    }
}
