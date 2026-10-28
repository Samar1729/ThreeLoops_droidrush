package com.threeloops.mirrorarchive.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.withTransaction
import com.threeloops.mirrorarchive.BuildConfig
import com.threeloops.mirrorarchive.contracts.ScanPhase
import com.threeloops.mirrorarchive.data.db.AppDatabase
import com.threeloops.mirrorarchive.data.db.ScanStateEntity
import com.threeloops.mirrorarchive.work.ScanController
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.settingsDataStore by preferencesDataStore(name = "settings")

data class Settings(
    val dHashMax: Int = 6,
    val pHashMax: Int = 8,
    val blurThreshold: Double = 100.0,
    val labelConfidence: Float = 0.6f,
    val scanIgnoreConstraints: Boolean = false
)

data class AppInfo(
    val versionName: String,
    val versionCode: Long,
    val libraries: List<String>,
    val privacyStatement: String
)

internal fun Settings.clampedForStorage() = copy(
    dHashMax = dHashMax.coerceIn(0, 32),
    pHashMax = pHashMax.coerceIn(0, 32),
    blurThreshold = blurThreshold.takeIf { it.isFinite() && it > 0.0 } ?: Settings().blurThreshold,
    labelConfidence = labelConfidence.takeIf { it.isFinite() }?.coerceIn(0.1f, 0.99f)
        ?: Settings().labelConfidence
)

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: AppDatabase,
    private val scanController: ScanController
) {
    private object Keys {
        val dHashMax = intPreferencesKey("d_hash_max")
        val pHashMax = intPreferencesKey("p_hash_max")
        val blurThreshold = doublePreferencesKey("blur_threshold")
        val labelConfidence = floatPreferencesKey("label_confidence")
        val scanIgnoreConstraints = booleanPreferencesKey("scan_ignore_constraints")
    }

    fun settings(): Flow<Settings> = context.settingsDataStore.data.map { prefs ->
        Settings(
            dHashMax = prefs[Keys.dHashMax] ?: Settings().dHashMax,
            pHashMax = prefs[Keys.pHashMax] ?: Settings().pHashMax,
            blurThreshold = prefs[Keys.blurThreshold] ?: Settings().blurThreshold,
            labelConfidence = prefs[Keys.labelConfidence] ?: Settings().labelConfidence,
            scanIgnoreConstraints = prefs[Keys.scanIgnoreConstraints] ?: Settings().scanIgnoreConstraints
        )
    }

    suspend fun update(settings: Settings) {
        val safe = settings.clampedForStorage()
        context.settingsDataStore.edit { prefs ->
            prefs[Keys.dHashMax] = safe.dHashMax
            prefs[Keys.pHashMax] = safe.pHashMax
            prefs[Keys.blurThreshold] = safe.blurThreshold
            prefs[Keys.labelConfidence] = safe.labelConfidence
            prefs[Keys.scanIgnoreConstraints] = safe.scanIgnoreConstraints
        }
    }

    suspend fun clearIndex() {
        scanController.cancel()
        database.withTransaction {
            database.imageDao().deleteAll()
            database.skippedDao().deleteAll()
            database.scanStateDao().deletePipelineStats()
            database.scanStateDao().upsert(
                ScanStateEntity(
                    lastProcessedId = 0L,
                    lastGeneration = 0L,
                    totalCount = 0L,
                    processedCount = 0L,
                    phase = ScanPhase.IDLE,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun appInfo(): AppInfo = AppInfo(
        versionName = BuildConfig.VERSION_NAME,
        versionCode = BuildConfig.VERSION_CODE.toLong(),
        libraries = listOf(
            "Kotlin", "Jetpack Compose", "Room", "SQLCipher", "WorkManager",
            "ML Kit (bundled)", "DataStore", "Hilt", "Coil",
            "AI coding assistants were used"
        ),
        privacyStatement = "All processing is on-device. The app has no INTERNET permission and uses no analytics."
    )

    fun dbSizeBytes(): Long {
        val dbFile = context.getDatabasePath(DATABASE_NAME)
        return listOf(dbFile, java.io.File(dbFile.path + "-wal"), java.io.File(dbFile.path + "-shm"))
            .sumOf { file -> if (file.isFile) file.length() else 0L }
    }

    private companion object {
        const val DATABASE_NAME = "mirror_archive.db"
    }
}
