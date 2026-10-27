package com.threeloops.mirrorarchive.data.repo

import com.threeloops.mirrorarchive.contracts.AlbumSummary
import com.threeloops.mirrorarchive.contracts.AlbumType
import com.threeloops.mirrorarchive.contracts.FaceInfo
import com.threeloops.mirrorarchive.contracts.HashRow
import com.threeloops.mirrorarchive.contracts.ImageItem
import com.threeloops.mirrorarchive.contracts.IndexedImage
import com.threeloops.mirrorarchive.contracts.ScanState
import kotlinx.coroutines.flow.Flow

/**
 * Image repository interface managing indexed images, queries, and background indexing state.
 */
interface ImageRepository {
    /**
     * Persists a batch of indexed images and optionally updates the checkpoint media ID.
     */
    suspend fun saveBatch(items: List<IndexedImage>, checkpointId: Long = 0L)

    /**
     * Observes images matching the given tag query.
     */
    fun searchByTag(q: String): Flow<List<ImageItem>>

    /**
     * Observes images belonging to the specified smart album.
     */
    fun album(type: AlbumType): Flow<List<ImageItem>>

    /**
     * Observes all images indexed in the repository.
     */
    fun allImages(): Flow<List<ImageItem>>

    /**
     * Returns candidate hash rows for duplicate clustering.
     */
    fun duplicateCandidates(): List<HashRow>

    /**
     * Observes album summaries across all smart album categories.
     */
    fun albumSummaries(): Flow<List<AlbumSummary>>

    /**
     * Observes images whose OCR text matches the query string.
     */
    fun searchByText(q: String): Flow<List<ImageItem>>

    /**
     * Observes search suggestion terms matching prefix.
     */
    fun suggestions(prefix: String): Flow<List<String>>

    /**
     * Observes all images containing detected faces.
     */
    fun peopleImages(): Flow<List<ImageItem>>

    /**
     * Observes images with blur score below the given threshold.
     */
    fun lowBlur(threshold: Double): Flow<List<ImageItem>>

    /**
     * Retrieves specific image items by their IDs.
     */
    fun items(ids: List<Long>): List<ImageItem>

    /**
     * Deletes rows by MediaStore IDs.
     */
    suspend fun deleteRows(mediaIds: List<Long>)

    /**
     * Observes media scanning progress state.
     */
    fun scanState(): Flow<ScanState>

    /**
     * Retrieves the last processed MediaStore ID checkpoint.
     */
    suspend fun lastProcessedId(): Long

    /**
     * Retrieves detected faces for the specified image ID.
     */
    fun facesFor(imageId: Long): List<FaceInfo> = emptyList()
}
