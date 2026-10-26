package com.threeloops.mirrorarchive.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.threeloops.mirrorarchive.permissions.OnboardingPrefs
import com.threeloops.mirrorarchive.permissions.PermissionManager
import com.threeloops.mirrorarchive.permissions.PermissionState
import com.threeloops.mirrorarchive.contracts.ScanPhase
import com.threeloops.mirrorarchive.work.ScanController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
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
    permissionManager: PermissionManager,
    private val scanController: ScanController
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

    val scanProgress = scanController.progress()

    /** Start the first scan once media access is ready, or resume an interrupted first scan. */
    fun startInitialScanIfNeeded() {
        viewModelScope.launch {
            val progress = scanProgress.first()
            if (progress.phase == ScanPhase.IDLE ||
                progress.phase == ScanPhase.PAUSED ||
                progress.phase == ScanPhase.FAILED
            ) {
                scanController.start()
            }
        }
    }

    fun startScanNow() = scanController.start(ignoreConstraints = true)

    fun cancelScan() = scanController.cancel()
}
