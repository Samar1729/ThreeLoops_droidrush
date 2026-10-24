package com.threeloops.mirrorarchive.permissions

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * JVM unit tests for [resolvePermissionState].
 *
 * Tests all combinations across:
 * - API 30-32 (Legacy storage permission)
 * - API 33 (Granular media images permission)
 * - API 34+ (Partial photo picker visual-user-selected permission)
 *
 * Scenarios verified for each tier:
 * 1. Full grant
 * 2. Partial grant (API 34+ only; ignored / not partial on 30-33)
 * 3. Denied with rationale (re-requestable)
 * 4. Permanently denied (asked before + no rationale + not granted)
 * 5. Never asked (initial launch state)
 */
class PermissionStateTest {

    // ==========================================
    // API 30 - 32: READ_EXTERNAL_STORAGE era
    // ==========================================

    @Test
    fun testApi30To32_FullGrant() {
        val state30 = resolvePermissionState(
            apiLevel = 30,
            hasFull = true,
            hasPartial = false,
            shouldShowRationale = false,
            askedBefore = true
        )
        assertEquals(PermissionState.GRANTED_FULL, state30)

        val state31 = resolvePermissionState(
            apiLevel = 31,
            hasFull = true,
            hasPartial = false,
            shouldShowRationale = false,
            askedBefore = false
        )
        assertEquals(PermissionState.GRANTED_FULL, state31)

        val state32 = resolvePermissionState(
            apiLevel = 32,
            hasFull = true,
            hasPartial = true, // Partial flag has no effect on <= 32
            shouldShowRationale = false,
            askedBefore = true
        )
        assertEquals(PermissionState.GRANTED_FULL, state32)
    }

    @Test
    fun testApi30To32_NeverAsked() {
        val state = resolvePermissionState(
            apiLevel = 30,
            hasFull = false,
            hasPartial = false,
            shouldShowRationale = false,
            askedBefore = false
        )
        assertEquals(PermissionState.DENIED, state)
    }

    @Test
    fun testApi30To32_DeniedWithRationale() {
        val state = resolvePermissionState(
            apiLevel = 31,
            hasFull = false,
            hasPartial = false,
            shouldShowRationale = true,
            askedBefore = true
        )
        assertEquals(PermissionState.DENIED, state)
    }

    @Test
    fun testApi30To32_PermanentlyDenied() {
        val state = resolvePermissionState(
            apiLevel = 32,
            hasFull = false,
            hasPartial = false,
            shouldShowRationale = false,
            askedBefore = true
        )
        assertEquals(PermissionState.DENIED_PERMANENTLY, state)
    }

    @Test
    fun testApi30To32_PartialFlagDoesNotGrantPartial() {
        // Even if hasPartial was somehow true, on API < 34 it must not result in GRANTED_PARTIAL
        val state = resolvePermissionState(
            apiLevel = 32,
            hasFull = false,
            hasPartial = true,
            shouldShowRationale = false,
            askedBefore = true
        )
        assertEquals(PermissionState.DENIED_PERMANENTLY, state)
    }

    // ==========================================
    // API 33: READ_MEDIA_IMAGES era (Tiramisu)
    // ==========================================

    @Test
    fun testApi33_FullGrant() {
        val state = resolvePermissionState(
            apiLevel = 33,
            hasFull = true,
            hasPartial = false,
            shouldShowRationale = false,
            askedBefore = true
        )
        assertEquals(PermissionState.GRANTED_FULL, state)
    }

    @Test
    fun testApi33_NeverAsked() {
        val state = resolvePermissionState(
            apiLevel = 33,
            hasFull = false,
            hasPartial = false,
            shouldShowRationale = false,
            askedBefore = false
        )
        assertEquals(PermissionState.DENIED, state)
    }

    @Test
    fun testApi33_DeniedWithRationale() {
        val state = resolvePermissionState(
            apiLevel = 33,
            hasFull = false,
            hasPartial = false,
            shouldShowRationale = true,
            askedBefore = true
        )
        assertEquals(PermissionState.DENIED, state)
    }

    @Test
    fun testApi33_PermanentlyDenied() {
        val state = resolvePermissionState(
            apiLevel = 33,
            hasFull = false,
            hasPartial = false,
            shouldShowRationale = false,
            askedBefore = true
        )
        assertEquals(PermissionState.DENIED_PERMANENTLY, state)
    }

    @Test
    fun testApi33_PartialFlagDoesNotGrantPartial() {
        // API 33 does not have VISUAL_USER_SELECTED
        val state = resolvePermissionState(
            apiLevel = 33,
            hasFull = false,
            hasPartial = true,
            shouldShowRationale = false,
            askedBefore = true
        )
        assertEquals(PermissionState.DENIED_PERMANENTLY, state)
    }

    // ==========================================
    // API 34+: READ_MEDIA_VISUAL_USER_SELECTED era (UpsideDownCake+)
    // ==========================================

    @Test
    fun testApi34Plus_FullGrant() {
        val state34 = resolvePermissionState(
            apiLevel = 34,
            hasFull = true,
            hasPartial = false,
            shouldShowRationale = false,
            askedBefore = true
        )
        assertEquals(PermissionState.GRANTED_FULL, state34)

        // If both full and partial are set, it is still GRANTED_FULL
        val stateBoth = resolvePermissionState(
            apiLevel = 35,
            hasFull = true,
            hasPartial = true,
            shouldShowRationale = false,
            askedBefore = true
        )
        assertEquals(PermissionState.GRANTED_FULL, stateBoth)
    }

    @Test
    fun testApi34Plus_PartialGrant() {
        // Selected Photos access only: hasPartial = true, hasFull = false
        val state34 = resolvePermissionState(
            apiLevel = 34,
            hasFull = false,
            hasPartial = true,
            shouldShowRationale = false,
            askedBefore = true
        )
        assertEquals(PermissionState.GRANTED_PARTIAL, state34)

        val state35 = resolvePermissionState(
            apiLevel = 35,
            hasFull = false,
            hasPartial = true,
            shouldShowRationale = true,
            askedBefore = true
        )
        assertEquals(PermissionState.GRANTED_PARTIAL, state35)
    }

    @Test
    fun testApi34Plus_NeverAsked() {
        val state = resolvePermissionState(
            apiLevel = 34,
            hasFull = false,
            hasPartial = false,
            shouldShowRationale = false,
            askedBefore = false
        )
        assertEquals(PermissionState.DENIED, state)
    }

    @Test
    fun testApi34Plus_DeniedWithRationale() {
        val state = resolvePermissionState(
            apiLevel = 34,
            hasFull = false,
            hasPartial = false,
            shouldShowRationale = true,
            askedBefore = true
        )
        assertEquals(PermissionState.DENIED, state)
    }

    @Test
    fun testApi34Plus_PermanentlyDenied() {
        val state = resolvePermissionState(
            apiLevel = 34,
            hasFull = false,
            hasPartial = false,
            shouldShowRationale = false,
            askedBefore = true
        )
        assertEquals(PermissionState.DENIED_PERMANENTLY, state)
    }
}
