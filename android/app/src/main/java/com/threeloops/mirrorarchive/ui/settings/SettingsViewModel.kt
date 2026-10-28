package com.threeloops.mirrorarchive.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.threeloops.mirrorarchive.contracts.ScanProgress
import com.threeloops.mirrorarchive.data.db.AppDatabase
import com.threeloops.mirrorarchive.settings.AppInfo
import com.threeloops.mirrorarchive.settings.Settings
import com.threeloops.mirrorarchive.settings.SettingsRepository
import com.threeloops.mirrorarchive.work.ScanController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
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
        val appInfo: AppInfo
    ) : SettingsUiState
    data class Error(val message: String) : SettingsUiState
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: SettingsRepository,
    database: AppDatabase,
    private val scanController: ScanController
) : ViewModel() {
    val uiState = combine(
        repository.settings(),
        database.imageDao().count(),
        database.scanStateDao().observe(),
        scanController.progress()
    ) { settings, count, _, progress ->
        SettingsUiState.Data(
            settings = settings,
            indexedCount = count,
            dbSizeBytes = repository.dbSizeBytes(),
            scanProgress = progress,
            appInfo = repository.appInfo()
        ) as SettingsUiState
    }.catch { error ->
        emit(SettingsUiState.Error(error.message ?: "Unable to load settings"))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState.Loading)

    fun update(settings: Settings) = viewModelScope.launch { repository.update(settings) }
    fun resetDefaults() = update(Settings())
    fun rescanNewPhotos() = scanController.start()
    fun fullRescan() = scanController.rescan(full = true)
    fun clearIndex() = viewModelScope.launch { repository.clearIndex() }
}
