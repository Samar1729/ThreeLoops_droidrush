package com.threeloops.mirrorarchive.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.threeloops.mirrorarchive.permissions.OnboardingPrefs
import com.threeloops.mirrorarchive.permissions.PermissionManager
import com.threeloops.mirrorarchive.permissions.PermissionState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Gate state determining the top-level active destination based on onboarding and permission status.
 */
sealed interface AppNavigationState {
    /** Loading initial DataStore and platform permission values. */
    data object Loading : AppNavigationState

    /** Onboarding has not been completed. */
    data object Onboarding : AppNavigationState

    /** Onboarding completed, but permission state is neither FULL nor PARTIAL. */
    data object PermissionDenied : AppNavigationState

    /** Onboarding completed and permission is GRANTED_FULL or GRANTED_PARTIAL. */
    data object Ready : AppNavigationState
}

/**
 * Computes top-level [AppNavigationState] from onboarding completion and permission status.
 *
 * Rules:
 * - Onboarding not done -> [AppNavigationState.Onboarding]
 * - Else if permission state is not FULL and not PARTIAL -> [AppNavigationState.PermissionDenied]
 * - Else -> [AppNavigationState.Ready] (navigate to albums)
 */
fun resolveGateDestination(
    isOnboardingDone: Boolean,
    permissionState: PermissionState
): AppNavigationState = when {
    !isOnboardingDone -> AppNavigationState.Onboarding
    permissionState != PermissionState.GRANTED_FULL &&
            permissionState != PermissionState.GRANTED_PARTIAL -> AppNavigationState.PermissionDenied
    else -> AppNavigationState.Ready
}

/**
 * Root ViewModel coordinating app gate navigation and reactive re-routing.
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    onboardingPrefs: OnboardingPrefs,
    permissionManager: PermissionManager
) : ViewModel() {

    /**
     * Reactive state observing both onboarding completion and media permission changes.
     */
    val navigationState: StateFlow<AppNavigationState> = combine(
        onboardingPrefs.isDone(),
        permissionManager.stateFlow()
    ) { isDone, permState ->
        resolveGateDestination(isDone, permState)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AppNavigationState.Loading
    )
}
