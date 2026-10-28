package com.threeloops.mirrorarchive.settings

import org.junit.Assert.assertEquals
import org.junit.Test

class SettingsTest {
    @Test
    fun updateValuesAreClampedToSupportedRanges() {
        val safe = Settings(
            dHashMax = -3,
            pHashMax = 90,
            blurThreshold = -1.0,
            labelConfidence = 2.0f
        ).clampedForStorage()

        assertEquals(0, safe.dHashMax)
        assertEquals(32, safe.pHashMax)
        assertEquals(100.0, safe.blurThreshold, 0.0)
        assertEquals(0.99f, safe.labelConfidence)
    }
}
