package com.threeloops.mirrorarchive.work

import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/** Debounces MediaStore changes before asking the incremental worker to reconcile the gallery. */
@Singleton
class MediaObserver @Inject constructor(
    @ApplicationContext private val context: Context,
    private val syncUseCase: SyncUseCase
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val lock = Any()
    private var observer: ContentObserver? = null
    private var debounceJob: Job? = null

    fun register() = synchronized(lock) {
        if (observer != null) return@synchronized
        val contentObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) = scheduleSync()

            override fun onChange(selfChange: Boolean, uri: Uri?) {
                scheduleSync()
            }

            private fun scheduleSync() {
                synchronized(lock) {
                    debounceJob?.cancel()
                    debounceJob = scope.launch {
                        delay(DEBOUNCE_MILLIS)
                        syncUseCase.triggerNow()
                    }
                }
            }
        }
        context.contentResolver.registerContentObserver(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            true,
            contentObserver
        )
        observer = contentObserver
    }

    fun unregister() = synchronized(lock) {
        debounceJob?.cancel()
        debounceJob = null
        observer?.let(context.contentResolver::unregisterContentObserver)
        observer = null
    }

    private companion object { const val DEBOUNCE_MILLIS = 2_000L }
}
