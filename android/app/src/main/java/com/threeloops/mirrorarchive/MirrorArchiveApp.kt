package com.threeloops.mirrorarchive

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Main application class for Mirror Archive.
 *
 * Configured with Hilt dependency injection. Operates strictly on-device with zero internet permissions.
 */
@HiltAndroidApp
class MirrorArchiveApp : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}
