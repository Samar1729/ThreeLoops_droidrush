package com.threeloops.mirrorarchive.dupes

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.IntentSender
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.threeloops.mirrorarchive.data.repo.ImageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PurgeViewModel @Inject constructor(
    private val repository: ImageRepository,
    private val planner: PurgePlanner,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow<PurgeUiState>(PurgeUiState.Idle)
    val state: StateFlow<PurgeUiState> = _state.asStateFlow()

    private var pendingPlan: PurgePlan? = null

    /**
     * Generates the IntentSender for MediaStore deletion dialog.
     */
    fun createRequest(plan: PurgePlan): IntentSender {
        val verifiedPlan = planner.verify(plan)
        this.pendingPlan = verifiedPlan
        
        val pendingIntent = MediaStore.createDeleteRequest(context.contentResolver, verifiedPlan.deleteUris)
        return pendingIntent.intentSender
    }

    /**
     * Direct execution of purge without MediaStore prompt (for in-memory or already authorized).
     */
    fun executePurge(plan: PurgePlan) {
        viewModelScope.launch {
            _state.value = PurgeUiState.Purging
            try {
                repository.deleteRows(plan.deleteIds)
                _state.value = PurgeUiState.Done(
                    deletedCount = plan.deleteIds.size,
                    freedBytes = plan.bytesFreed
                )
            } catch (e: Exception) {
                _state.value = PurgeUiState.Error(e.message ?: "Purge failed")
            }
        }
    }

    /**
     * Called from the UI layer when ActivityResultLauncher returns.
     */
    fun onResult(resultCode: Int) {
        if (resultCode == RESULT_OK) {
            pendingPlan?.let { plan ->
                viewModelScope.launch {
                    repository.deleteRows(plan.deleteIds)
                    _state.value = PurgeUiState.Done(
                        deletedCount = plan.deleteIds.size,
                        freedBytes = plan.bytesFreed
                    )
                }
            }
        }
        pendingPlan = null
    }

    fun spaceFreeable(allGroups: List<DuplicateGroup>): Long {
        val nonBestIds = allGroups.flatMap { group ->
            group.imageIds.filter { it != group.bestId }
        }
        val sizeMap = repository.items(nonBestIds).associate { it.id to it.sizeBytes }
        return nonBestIds.sumOf { sizeMap[it] ?: 0L }
    }
}