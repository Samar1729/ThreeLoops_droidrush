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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PurgeViewModel @Inject constructor(
    private val repository: ImageRepository,
    private val planner: PurgePlanner,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private var pendingPlan: PurgePlan? = null

    /**
     * Generates the IntentSender for MediaStore deletion dialog.
     * Use MediaStore.createTrashRequest for 30-day undo if preferred.
     */
    fun createRequest(plan: PurgePlan): IntentSender {
        val verifiedPlan = planner.verify(plan)
        this.pendingPlan = verifiedPlan
        
        // minSdk 30 guarantees createDeleteRequest exists
        val pendingIntent = MediaStore.createDeleteRequest(context.contentResolver, verifiedPlan.deleteUris)
        return pendingIntent.intentSender
    }

    /**
     * Called from the UI layer when ActivityResultLauncher returns.
     * RESULT_OK -> Deletes rows from Room. Canceling changes nothing.
     */
    fun onResult(resultCode: Int) {
        if (resultCode == RESULT_OK) {
            pendingPlan?.let { plan ->
                viewModelScope.launch {
                    repository.deleteRows(plan.deleteIds)
                    // The UI will automatically update if it observes repository Flows
                }
            }
        }
        // Always clear the plan after resolution
        pendingPlan = null
    }

    /**
     * S-3 contract: Returns total bytes freeable across ALL groups' non-best photos.
     * Feeds the 'Space you can free' UI counter.
     */
    fun spaceFreeable(allGroups: List<DuplicateGroup>): Flow<Long> {
        // Collect all candidate IDs that are NOT the best shot in their respective groups
        val nonBestIds = allGroups.flatMap { group ->
            group.imageIds.filter { it != group.bestId }
        }
        
        // This is a simplified Flow conversion. In a real repository, 
        // you might want a direct DAO sum query, but doing it in memory is fine here.
        return repository.duplicateCandidatesFlow().map { rows ->
            val sizeMap = repository.items(nonBestIds).associate { it.id to it.sizeBytes }
            nonBestIds.sumOf { sizeMap[it] ?: 0L }
        }
    }
}