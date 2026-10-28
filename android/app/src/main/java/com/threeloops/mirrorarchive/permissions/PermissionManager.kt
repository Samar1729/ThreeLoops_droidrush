package com.threeloops.mirrorarchive.permissions

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ProcessLifecycleOwner
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages runtime permission state for media access.
 *
 * Evaluates the current grant status using the pure [resolvePermissionState] function and
 * exposes a reactive [Flow] that re-evaluates on every lifecycle ON_RESUME event (so returning
 * from system Settings auto-updates the state without restarting).
 *
 * @param context Application context for permission checks.
 * @param onboardingPrefs DataStore-backed preferences for tracking whether permission was asked.
 */
@Singleton
class PermissionManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val onboardingPrefs: OnboardingPrefs
) {

    /** Manual trigger for re-evaluation (e.g. after a permission callback). */
    private val refreshTrigger = MutableStateFlow(0L)

    /**
     * Returns the set of runtime permissions that should be requested on the current API level.
     *
     * - API 34+: READ_MEDIA_IMAGES + READ_MEDIA_VISUAL_USER_SELECTED + optional notifications
     * - API 33:  READ_MEDIA_IMAGES + optional notifications
     * - API 30-32: READ_EXTERNAL_STORAGE
     */
    fun requiredPermissions(): Array<String> = when {
        Build.VERSION.SDK_INT >= 34 -> arrayOf(
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED,
            Manifest.permission.POST_NOTIFICATIONS
        )
        Build.VERSION.SDK_INT >= 33 -> arrayOf(
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.POST_NOTIFICATIONS
        )
        else -> arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
    }

    /**
     * Computes the current [PermissionState] synchronously.
     *
     * Uses the pure [resolvePermissionState] mapping with live system values.
     */
    fun state(): PermissionState = state(shouldShowRationale = false)

    /**
     * Computes the current [PermissionState] synchronously with an explicit rationale indicator.
     *
     * @param shouldShowRationale True if an Activity indicates rationale should be shown.
     */
    fun state(shouldShowRationale: Boolean): PermissionState {
        val apiLevel = Build.VERSION.SDK_INT

        val hasFull = when {
            apiLevel >= 33 -> isGranted(Manifest.permission.READ_MEDIA_IMAGES)
            else -> isGranted(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        val hasPartial = if (apiLevel >= 34) {
            isGranted(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
        } else {
            false
        }

        val askedBefore = onboardingPrefs.askedBeforeSynchronous()

        return resolvePermissionState(
            apiLevel = apiLevel,
            hasFull = hasFull,
            hasPartial = hasPartial,
            shouldShowRationale = shouldShowRationale,
            askedBefore = askedBefore
        )
    }

    /**
     * Records that permission has been requested from the user.
     *
     * Persists the flag in DataStore so subsequent denials are recognized as permanent.
     */
    suspend fun markAsked() {
        onboardingPrefs.markAsked()
        refresh()
    }

    /**
     * Reactive [Flow] of [PermissionState] that re-evaluates on:
     * - Every lifecycle ON_RESUME event (e.g. returning from system Settings)
     * - Explicit calls to [refresh]
     *
     * The flow never completes normally and emits only when the state actually changes.
     */
    fun stateFlow(): Flow<PermissionState> {
        val lifecycleFlow: Flow<Unit> = callbackFlow {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    trySend(Unit)
                }
            }
            withContext(Dispatchers.Main.immediate) {
                ProcessLifecycleOwner.get().lifecycle.addObserver(observer)
            }
            awaitClose {
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    ProcessLifecycleOwner.get().lifecycle.removeObserver(observer)
                }
            }
        }

        return combine(
            lifecycleFlow.onStart { emit(Unit) },
            refreshTrigger
        ) { _, _ -> state() }
            .distinctUntilChanged()
    }

    /**
     * Forces an immediate re-evaluation and emission of [PermissionState] in [stateFlow].
     */
    fun refresh() {
        refreshTrigger.value++
    }

    private fun isGranted(permission: String): Boolean =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
}

/**
 * Creates an [Intent] that navigates to this application's system Settings page.
 *
 * Useful for directing users to manually grant permissions after permanent denial.
 */
fun Context.appSettingsIntent(): Intent =
    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", packageName, null)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
