package com.threeloops.mirrorarchive.ui.ledger

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RaseedParserTest {

    @Test
    fun testParseMerchantAndGstinAndAmount() {
        val ocr = """
            STARBUCKS COFFEE
            STORE #4812, INDIRANAGAR, BENGALURU
            GSTIN: 29AAACB2184A1Z5
            DATE: 24/10/2026
            
            1  CAPPUCCINO TALL       280.00
            1  BUTTER CROISSANT      170.00
            
            SUBTOTAL:                450.00
            CGST 2.5%:                11.25
            SGST 2.5%:                11.25
            TOTAL PAYABLE:           472.50
        """.trimIndent()

        val item = RaseedParser.parse(
            id = 1,
            imageUri = "content://media/external/images/media/1",
            ocrText = ocr
        )

        assertEquals("STARBUCKS", item.merchant)
        assertEquals("29AAACB2184A1Z5", item.gstin)
        assertEquals("24/10/2026", item.date)
        assertEquals(472.50, item.totalAmount, 0.01)
        assertEquals("Dining & Cafe", item.category)
    }

    @Test
    fun testParseShellFuelInvoice() {
        val ocr = """
            SHELL INDIA MARKETS PVT LTD
            HIGHWAY PETROL PUMP #4
            GSTIN: 27AAECS1490F1ZM
            DATE: 19-10-2026
            
            PUMP 04 V-POWER PETROL
            RATE: 104.50 / L
            VOL: 23.92 L
            
            GRAND TOTAL: ₹ 2,500.00
        """.trimIndent()

        val item = RaseedParser.parse(
            id = 2,
            imageUri = "content://media/external/images/media/2",
            ocrText = ocr
        )

        assertEquals("SHELL", item.merchant)
        assertEquals("27AAECS1490F1ZM", item.gstin)
        assertEquals("19-10-2026", item.date)
        assertEquals(2500.00, item.totalAmount, 0.01)
        assertEquals("Fuel & Auto", item.category)
    }
}
