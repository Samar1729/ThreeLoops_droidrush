package com.threeloops.mirrorarchive.ui.permission

/**
 * Sealed UI state representing media permission status across onboarding and permission screens.
 */
sealed interface PermissionUiState {
    /** Evaluating initial state or preferences. */
    data object Loading : PermissionUiState

    /** Full media access granted. */
    data object Granted : PermissionUiState

    /** Partial media access granted (API 34+ photo picker selection). */
    data object Partial : PermissionUiState

    /** Permission denied but re-requestable (system can display rationale dialog). */
    data object Denied : PermissionUiState

    /** Permission permanently denied (requires opening system Settings to grant). */
    data object PermanentlyDenied : PermissionUiState
}
