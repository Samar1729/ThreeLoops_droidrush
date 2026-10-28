package com.threeloops.mirrorarchive.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.content.Context
import com.threeloops.mirrorarchive.di.DatabaseModule
import com.threeloops.mirrorarchive.security.DbIntegrity
import com.threeloops.mirrorarchive.security.KeyProvider
import com.threeloops.mirrorarchive.contracts.ScanProgress
import com.threeloops.mirrorarchive.data.db.AppDatabase
import com.threeloops.mirrorarchive.settings.AppInfo
import com.threeloops.mirrorarchive.settings.Settings
import com.threeloops.mirrorarchive.settings.SettingsRepository
import com.threeloops.mirrorarchive.metrics.ManifestAudit
import com.threeloops.mirrorarchive.work.ScanController
import com.threeloops.mirrorarchive.work.SyncUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface SettingsUiState {
    data object Loading : SettingsUiState
    data class Data(
        val settings: Settings,
        val indexedCount: Int,
        val dbSizeBytes: Long,
        val scanProgress: ScanProgress,
        val appInfo: AppInfo,
        val networkPermission: String,
        val databaseEncrypted: Boolean
    ) : SettingsUiState
    data class Error(val message: String) : SettingsUiState
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: SettingsRepository,
    private val database: AppDatabase,
    private val scanController: ScanController,
    private val syncUseCase: SyncUseCase,
    private val manifestAudit: ManifestAudit,
    private val dbIntegrity: DbIntegrity,
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val databaseEncrypted = MutableStateFlow(false)

    init {
        viewModelScope.launch(Dispatchers.IO) {
            databaseEncrypted.value = dbIntegrity.isEncrypted()
        }
    }

    val uiState = combine(
        repository.settings(),
        database.imageDao().count(),
        database.scanStateDao().observe(),
        scanController.progress(),
        databaseEncrypted
    ) { settings, count, _, progress, isEncrypted ->
        SettingsUiState.Data(
            settings = settings,
            indexedCount = count,
            dbSizeBytes = repository.dbSizeBytes(),
            scanProgress = progress,
            appInfo = repository.appInfo(),
            networkPermission = if (manifestAudit.hasInternetPermission()) "present" else "none",
            databaseEncrypted = isEncrypted
        ) as SettingsUiState
    }.flowOn(Dispatchers.IO).catch { error ->
        emit(SettingsUiState.Error(error.message ?: "Unable to load settings"))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState.Loading)

    fun update(settings: Settings) = viewModelScope.launch { repository.update(settings) }
    fun resetDefaults() = update(Settings())
    fun rescanNewPhotos() = syncUseCase.triggerNow()
    fun fullRescan() = scanController.rescan(full = true)
    fun clearIndex() = viewModelScope.launch { repository.clearIndex() }

    fun simulateKeyLoss() = viewModelScope.launch(Dispatchers.IO) {
        java.io.File(context.noBackupFilesDir, KeyProvider.KEY_FILE_NAME).delete()
        DatabaseModule.recoverFromKeyLoss(database)
        databaseEncrypted.value = dbIntegrity.isEncrypted()
    }
}
