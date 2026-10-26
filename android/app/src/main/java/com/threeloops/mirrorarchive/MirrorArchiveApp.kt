package com.threeloops.mirrorarchive

import android.app.Application
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.work.Configuration
import androidx.hilt.work.HiltWorkerFactory
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.intercept.Interceptor
import coil.request.ErrorResult
import com.threeloops.mirrorarchive.work.IndexPipeline
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Main application class for Mirror Archive / Vault.
 *
 * Configured with Hilt dependency injection. Operates strictly on-device with zero internet permissions.
 */
@HiltAndroidApp
class MirrorArchiveApp : Application(), Configuration.Provider, ImageLoaderFactory {
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(workerFactory).build()

    @Inject
    lateinit var indexPipeline: IndexPipeline

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .components {
                add(Interceptor { chain ->
                    val data = chain.request.data
                    if (data is String && (data.startsWith("http://") || data.startsWith("https://"))) {
                        // Offline security safeguard: avoid network socket requests when running strictly offline
                        ErrorResult(
                            drawable = ColorDrawable(Color.argb(255, 20, 20, 22)),
                            request = chain.request,
                            throwable = SecurityException("Offline app: internet disabled")
                        )
                    } else {
                        try {
                            chain.proceed(chain.request)
                        } catch (e: Throwable) {
                            ErrorResult(
                                drawable = ColorDrawable(Color.argb(255, 20, 20, 22)),
                                request = chain.request,
                                throwable = e
                            )
                        }
                    }
                })
            }
            .respectCacheHeaders(false)
            .build()
    }

    override fun onCreate() {
        super.onCreate()
        applicationScope.launch {
            try {
                indexPipeline.warmUp()
            } catch (ignored: Throwable) {}
        }
    }
}
