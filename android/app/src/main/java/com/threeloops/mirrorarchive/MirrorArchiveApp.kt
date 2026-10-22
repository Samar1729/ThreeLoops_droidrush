package com.threeloops.mirrorarchive

import android.app.Application

class MirrorArchiveApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // ML Kit clients warm-up and application-level singletons can be initialized here
    }
}
