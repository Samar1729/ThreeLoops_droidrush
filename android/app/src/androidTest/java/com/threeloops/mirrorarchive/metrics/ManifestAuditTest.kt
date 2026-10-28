package com.threeloops.mirrorarchive.metrics

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ManifestAuditTest {
    @Test
    fun appDoesNotRequestInternetPermission() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        assertFalse(ManifestAudit(context).hasInternetPermission())
    }
}
