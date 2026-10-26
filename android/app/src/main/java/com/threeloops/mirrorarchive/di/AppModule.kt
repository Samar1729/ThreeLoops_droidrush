package com.threeloops.mirrorarchive.di

import com.threeloops.mirrorarchive.data.repo.ImageRepository
import com.threeloops.mirrorarchive.data.repo.RoomImageRepository
import com.threeloops.mirrorarchive.dupes.DuplicateFinder
import com.threeloops.mirrorarchive.dupes.RealDuplicateFinder
import com.threeloops.mirrorarchive.hash.PerceptualHasher
import com.threeloops.mirrorarchive.hash.PerceptualHasherStub
import com.threeloops.mirrorarchive.ml.ImageAnalyzer
import com.threeloops.mirrorarchive.ml.MlKitImageAnalyzer
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt dependency injection module binding repository and engine components.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    /**
     * Binds the Room-backed repository as the application [ImageRepository].
     */
    @Binds
    @Singleton
    abstract fun bindImageRepository(impl: RoomImageRepository): ImageRepository

    /**
     * Binds the on-device ML Kit analyzer for label detection, OCR, and facial features.
     */
    @Binds
    @Singleton
    abstract fun bindImageAnalyzer(impl: MlKitImageAnalyzer): ImageAnalyzer

    /**
     * Binds [PerceptualHasherStub] as the [PerceptualHasher] component.
     */
    @Binds
    @Singleton
    abstract fun bindPerceptualHasher(impl: PerceptualHasherStub): PerceptualHasher

    /**
     * Binds [RealDuplicateFinder] as the [DuplicateFinder] component.
     */
    @Binds
    @Singleton
    abstract fun bindDuplicateFinder(impl: RealDuplicateFinder): DuplicateFinder
}
