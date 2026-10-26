package com.threeloops.mirrorarchive.data.demo

import com.threeloops.mirrorarchive.contracts.AlbumType

/**
 * Backward-compatible bridge to SampleMediaSeed.
 * All real photography items with Unsplash URLs and local webp drawables
 * are defined in SampleMediaSeed.kt.
 */
object DemoSeedData {
    val photos: List<DisplayPhoto> get() = SampleMediaSeed.photos
    val receiptPhotos: List<DisplayPhoto> get() = SampleMediaSeed.receiptPhotos
    val peoplePhotos: List<DisplayPhoto> get() = SampleMediaSeed.peoplePhotos
    val burstPhotos: List<DisplayPhoto> get() = SampleMediaSeed.burstPhotos
    val documentPhotos: List<DisplayPhoto> get() = SampleMediaSeed.documentPhotos

    fun getPhotoById(id: Long): DisplayPhoto? = SampleMediaSeed.getPhotoById(id)
    fun filterByCategory(category: String): List<DisplayPhoto> = SampleMediaSeed.filterByCategory(category)
    fun filterByQuery(query: String, modeIsText: Boolean): List<DisplayPhoto> = SampleMediaSeed.filterByQuery(query, modeIsText)
    fun filterByTag(tag: String): List<DisplayPhoto> = SampleMediaSeed.filterByCategory(tag)
    fun getClusterGroups(cosineThreshold: Float): Map<Int, List<DisplayPhoto>> = SampleMediaSeed.getClusterGroups(cosineThreshold)
}
