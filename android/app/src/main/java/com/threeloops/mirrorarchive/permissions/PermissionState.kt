package com.threeloops.mirrorarchive.permissions

/**
 * Represents the current state of media read permission grants.
 */
enum class PermissionState {
    /** All requested permissions are granted; full MediaStore access available. */
    GRANTED_FULL,

    /** API 34+ partial photo-picker access granted (READ_MEDIA_VISUAL_USER_SELECTED but not READ_MEDIA_IMAGES). */
    GRANTED_PARTIAL,

    /** Permission was denied but can still be requested again (rationale can be shown). */
    DENIED,

    /** Permission was denied and should not be requested again (user tapped "Don't ask again" or never-asked path exhausted). */
    DENIED_PERMANENTLY
}

/**
 * Pure function that computes [PermissionState] from platform signals.
 *
 * This function is free of Android framework types so it can be tested on the JVM.
 *
 * @param apiLevel The device SDK version (e.g., [android.os.Build.VERSION.SDK_INT]).
 * @param hasFull True if the primary read permission is granted (READ_MEDIA_IMAGES on 33+, READ_EXTERNAL_STORAGE on 30-32).
 * @param hasPartial True if READ_MEDIA_VISUAL_USER_SELECTED is granted (only meaningful on API 34+).
 * @param shouldShowRationale True if the system indicates rationale should be shown (permission denied but re-requestable).
 * @param askedBefore True if the app has previously launched a permission request at least once.
 * @return The computed [PermissionState].
 */
fun resolvePermissionState(
    apiLevel: Int,
    hasFull: Boolean,
    hasPartial: Boolean,
    shouldShowRationale: Boolean,
    askedBefore: Boolean
): PermissionState = when {
    // Full access granted on any API level
    hasFull -> PermissionState.GRANTED_FULL

    // API 34+: partial photo picker access
    apiLevel >= 34 && hasPartial -> PermissionState.GRANTED_PARTIAL

    // System says we can show rationale → user denied once but didn't check "Don't ask again"
    shouldShowRationale -> PermissionState.DENIED

    // Never asked before → treat as initial DENIED (not permanent)
    !askedBefore -> PermissionState.DENIED

    // Asked before, no rationale, no grant → permanently denied
    else -> PermissionState.DENIED_PERMANENTLY
}
