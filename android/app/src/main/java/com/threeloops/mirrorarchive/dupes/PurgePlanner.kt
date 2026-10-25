package com.threeloops.mirrorarchive.dupes

import android.net.Uri
import com.threeloops.mirrorarchive.data.mediastore.MediaStoreDataSource
import com.threeloops.mirrorarchive.data.repo.ImageRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PurgePlanner @Inject constructor(
    private val repository: ImageRepository,
    private val mediaStoreDataSource: MediaStoreDataSource
) {
    /**
     * Builds the purge plan.
     * Safety Rule: Refuses to produce a plan that deletes every photo in the group.
     */
    fun plan(group: DuplicateGroup, selected: Set<Long>): PurgePlan {
        // Prevent total deletion of a group
        if (selected.containsAll(group.imageIds)) {
            throw IllegalArgumentException("Safety violation: Cannot delete every photo in a group. Must keep at least one.")
        }

        // Use Rishav's R-6 repository route to get displayable items
        val items = repository.items(group.imageIds)

        val keepIds = mutableListOf<Long>()
        val deleteIds = mutableListOf<Long>()
        val deleteUris = mutableListOf<Uri>()
        var bytesFreed = 0L

        for (item in items) {
            if (selected.contains(item.id)) {
                deleteIds.add(item.mediaId)
                deleteUris.add(Uri.parse(item.uri))
                bytesFreed += item.sizeBytes
            } else {
                keepIds.add(item.mediaId)
            }
        }

        return PurgePlan(
            keepIds = keepIds,
            deleteIds = deleteIds,
            deleteUris = deleteUris,
            bytesFreed = bytesFreed
        )
    }

    /**
     * Creates default purge plan for a group: keep canonical, delete others.
     */
    fun planForGroup(group: DuplicateGroup): PurgePlan {
        val selected = group.imageIds.filter { it != group.bestId }.toSet()
        return plan(group, selected)
    }

    /**
     * Drops URIs that no longer exist on the device before requesting deletion.
     */
    fun verify(plan: PurgePlan): PurgePlan {
        val verifiedUris = mutableListOf<Uri>()
        val verifiedIds = mutableListOf<Long>()

        for (i in plan.deleteUris.indices) {
            val uri = plan.deleteUris[i]
            val mediaId = plan.deleteIds[i]
            
            // Depends on Rishav's R-3 contract
            if (mediaStoreDataSource.exists(uri)) {
                verifiedUris.add(uri)
                verifiedIds.add(mediaId)
            }
        }

        return plan.copy(
            deleteIds = verifiedIds,
            deleteUris = verifiedUris
        )
    }
}