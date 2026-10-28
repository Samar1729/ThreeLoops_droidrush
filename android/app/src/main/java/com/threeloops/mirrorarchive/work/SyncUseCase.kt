package com.threeloops.mirrorarchive.work

import com.threeloops.mirrorarchive.data.mediastore.MediaStoreSource
import com.threeloops.mirrorarchive.data.repo.ImageRepository
import com.threeloops.mirrorarchive.permissions.PermissionManager
import com.threeloops.mirrorarchive.permissions.PermissionState
import kotlinx.coroutines.CancellationException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncUseCase @Inject constructor(
    private val mediaStore: MediaStoreSource,
    private val repository: ImageRepository,
    private val permissionManager: PermissionManager,
    private val scanController: ScanController
) {
    /** Reconciles removals only with a complete, successful MediaStore view. */
    suspend fun reconcileDeletions() {
        // Partial photo access omits unselected items. Deleting against that incomplete ID set
        // would erase valid indexed photos, so reconciliation is restricted to full permission.
        if (permissionManager.state() != PermissionState.GRANTED_FULL) return

        val allIds = try {
            mediaStore.allIds().toSet()
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            // A failed MediaStore query cannot distinguish missing files from a failed query.
            return
        }
        val dbIds = try {
            repository.allMediaIds()
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            return
        }
        dbIds.asSequence()
            .filterNot(allIds::contains)
            .toList()
            .chunked(DELETE_CHUNK_SIZE)
            .forEach { chunk -> repository.deleteRows(chunk) }
    }

    fun triggerNow() = scanController.startIncremental()

    private companion object { const val DELETE_CHUNK_SIZE = 500 }
}
