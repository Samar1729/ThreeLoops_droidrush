package com.threeloops.mirrorarchive.ui.navigation

import com.threeloops.mirrorarchive.permissions.PermissionState
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests verifying start-destination gate logic:
 * - onboarding not done -> onboarding;
 * - else state not FULL/PARTIAL -> permission_denied;
 * - else -> albums (Ready).
 */
class NavigationGateTest {

    @Test
    fun testOnboardingNotDone_AlwaysRoutesToOnboarding() {
        assertEquals(
            AppNavigationState.Onboarding,
            resolveGateDestination(isOnboardingDone = false, permissionState = PermissionState.DENIED)
        )
        assertEquals(
            AppNavigationState.Onboarding,
            resolveGateDestination(isOnboardingDone = false, permissionState = PermissionState.DENIED_PERMANENTLY)
        )
        assertEquals(
            AppNavigationState.Onboarding,
            resolveGateDestination(isOnboardingDone = false, permissionState = PermissionState.GRANTED_FULL)
        )
        assertEquals(
            AppNavigationState.Onboarding,
            resolveGateDestination(isOnboardingDone = false, permissionState = PermissionState.GRANTED_PARTIAL)
        )
    }

    @Test
    fun testOnboardingDone_DeniedRoutesToPermissionDenied() {
        assertEquals(
            AppNavigationState.PermissionDenied,
            resolveGateDestination(isOnboardingDone = true, permissionState = PermissionState.DENIED)
        )
        assertEquals(
            AppNavigationState.PermissionDenied,
            resolveGateDestination(isOnboardingDone = true, permissionState = PermissionState.DENIED_PERMANENTLY)
        )
    }

    @Test
    fun testOnboardingDone_GrantedFullRoutesToReady() {
        assertEquals(
            AppNavigationState.Ready,
            resolveGateDestination(isOnboardingDone = true, permissionState = PermissionState.GRANTED_FULL)
        )
    }

    @Test
    fun testOnboardingDone_GrantedPartialRoutesToReady() {
        assertEquals(
            AppNavigationState.Ready,
            resolveGateDestination(isOnboardingDone = true, permissionState = PermissionState.GRANTED_PARTIAL)
        )
    }
}
