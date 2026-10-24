package com.threeloops.mirrorarchive.ui.permission

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.threeloops.mirrorarchive.permissions.PermissionManager
import com.threeloops.mirrorarchive.permissions.PermissionState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel managing the permission denial recovery screens (DENIED, PERMANENTLY_DENIED, PARTIAL).
 *
 * Exposes [uiState] mapping the reactive [PermissionManager.stateFlow] to [PermissionUiState].
 */
@HiltViewModel
class PermissionViewModel @Inject constructor(
    private val permissionManager: PermissionManager
) : ViewModel() {

    /**
     * UI state representing the current permission grant status.
     */
    val uiState: StateFlow<PermissionUiState> = permissionManager.stateFlow()
        .map { state ->
            when (state) {
                PermissionState.GRANTED_FULL -> PermissionUiState.Granted
                PermissionState.GRANTED_PARTIAL -> PermissionUiState.Partial
                PermissionState.DENIED -> PermissionUiState.Denied
                PermissionState.DENIED_PERMANENTLY -> PermissionUiState.PermanentlyDenied
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PermissionUiState.Loading
        )

    /**
     * Returns the array of platform permissions required on this device's API level.
     */
    fun requiredPermissions(): Array<String> = permissionManager.requiredPermissions()

    /**
     * Handles the result of a retry or "Select more photos" request.
     *
     * @param result Mapping of permission names to grant booleans.
     */
    fun onPermissionResult(result: Map<String, Boolean>) {
        viewModelScope.launch {
            permissionManager.markAsked()
            permissionManager.refresh()
        }
    }

    /**
     * Triggers an immediate refresh of the platform permission evaluation.
     */
    fun refresh() {
        permissionManager.refresh()
    }
}
