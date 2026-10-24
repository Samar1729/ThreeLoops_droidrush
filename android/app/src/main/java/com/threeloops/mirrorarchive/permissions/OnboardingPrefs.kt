package com.threeloops.mirrorarchive.permissions

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

/** Scoped DataStore instance for onboarding and permission tracking preferences. */
private val Context.onboardingDataStore by preferencesDataStore(name = "onboarding_prefs")

/**
 * DataStore-backed preferences for onboarding completion and permission-request history.
 *
 * @param context Application context providing the DataStore instance.
 */
@Singleton
class OnboardingPrefs @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private companion object {
        val KEY_ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
        val KEY_ASKED_BEFORE = booleanPreferencesKey("permission_asked_before")
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Volatile
    private var cachedAskedBefore = false

    init {
        scope.launch {
            try {
                context.onboardingDataStore.data.collect { prefs ->
                    cachedAskedBefore = prefs[KEY_ASKED_BEFORE] ?: false
                }
            } catch (_: Exception) {
                // Ignore initialization failures in testing environments
            }
        }
    }

    /**
     * Observes whether the user has completed the onboarding flow.
     */
    fun isDone(): Flow<Boolean> =
        context.onboardingDataStore.data.map { prefs ->
            prefs[KEY_ONBOARDING_DONE] ?: false
        }

    /**
     * Marks onboarding as completed.
     */
    suspend fun markDone() {
        context.onboardingDataStore.edit { prefs ->
            prefs[KEY_ONBOARDING_DONE] = true
        }
    }

    /**
     * Observes whether a permission request has been launched at least once.
     */
    fun askedBefore(): Flow<Boolean> =
        context.onboardingDataStore.data.map { prefs ->
            prefs[KEY_ASKED_BEFORE] ?: false
        }

    /**
     * Records that a permission request has been launched.
     */
    suspend fun markAsked() {
        cachedAskedBefore = true
        context.onboardingDataStore.edit { prefs ->
            prefs[KEY_ASKED_BEFORE] = true
        }
    }

    /**
     * Synchronous read of the "asked before" flag for use in non-suspend contexts.
     */
    fun askedBeforeSynchronous(): Boolean =
        try {
            runBlocking(Dispatchers.IO) {
                context.onboardingDataStore.data.first()[KEY_ASKED_BEFORE] ?: false
            }.also { cachedAskedBefore = it }
        } catch (_: Exception) {
            cachedAskedBefore
        }
}
