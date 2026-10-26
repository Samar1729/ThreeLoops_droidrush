package com.threeloops.mirrorarchive

import android.app.Application
import com.threeloops.mirrorarchive.work.IndexPipeline
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Main application class for Mirror Archive.
 *
 * Configured with Hilt dependency injection. Operates strictly on-device with zero internet permissions.
 */
@HiltAndroidApp
class MirrorArchiveApp : Application() {
    @Inject
    lateinit var indexPipeline: IndexPipeline

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        applicationScope.launch { indexPipeline.warmUp() }
    }
}
