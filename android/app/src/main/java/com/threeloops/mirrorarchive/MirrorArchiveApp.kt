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
import com.threeloops.mirrorarchive.data.db.AppDatabase
import com.threeloops.mirrorarchive.contracts.ScanPhase
import com.threeloops.mirrorarchive.permissions.PermissionManager
import com.threeloops.mirrorarchive.permissions.PermissionState
import com.threeloops.mirrorarchive.work.IndexPipeline
import com.threeloops.mirrorarchive.work.MediaObserver
import com.threeloops.mirrorarchive.work.SyncUseCase
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onStart
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

    @Inject
    lateinit var appDatabase: AppDatabase

    @Inject
    lateinit var permissionManager: PermissionManager

    @Inject
    lateinit var mediaObserver: MediaObserver

    @Inject
    lateinit var syncUseCase: SyncUseCase

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
                appDatabase.openHelper.writableDatabase
            } catch (ignored: Throwable) {
                // Database recovery is handled by the SQLCipher open helper when the error
                // indicates a missing key or a rejected database passphrase.
            }
            try {
                indexPipeline.warmUp()
            } catch (ignored: Throwable) {}

            var startupSyncTriggered = false
            combine(
                permissionManager.stateFlow().onStart { emit(permissionManager.state()) },
                appDatabase.scanStateDao().observe()
            ) { permission, scanState -> permission to scanState }
                .collect { (permission, scanState) ->
                    val ready = permission == PermissionState.GRANTED_FULL &&
                        scanState?.phase == ScanPhase.COMPLETE
                    if (ready) {
                        mediaObserver.register()
                        if (!startupSyncTriggered) {
                            startupSyncTriggered = true
                            syncUseCase.triggerNow()
                        }
                    } else {
                        mediaObserver.unregister()
                    }
                }
        }
    }
}
