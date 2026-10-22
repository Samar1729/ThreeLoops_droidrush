package com.threeloops.mirrorarchive.data.repo

import com.threeloops.mirrorarchive.data.model.*
import kotlinx.coroutines.flow.Flow

/**
 * Section 3.5 ImageRepository (the shared contract)
 * Owner: Rishav (consumed by Aditya & Samar)
 */
interface ImageRepository {
    fun searchByTag(q: String): Flow<List<ImageItem>>
    fun album(type: AlbumType): Flow<List<ImageItem>>
    fun duplicateCandidates(): List<HashRow>
    fun albumSummaries(): Flow<List<AlbumSummary>>
    fun searchByText(q: String): Flow<List<ImageItem>>
    fun suggestions(prefix: String): Flow<List<String>>
    fun peopleImages(): Flow<List<ImageItem>>
    fun lowBlur(threshold: Double): Flow<List<ImageItem>>
    fun items(ids: List<Long>): List<ImageItem>
    fun facesFor(imageId: Long): List<FaceInfo>
    suspend fun deleteRows(mediaIds: List<Long>)
    fun scanState(): Flow<ScanProgress>
    suspend fun lastProcessedId(): Long
}
