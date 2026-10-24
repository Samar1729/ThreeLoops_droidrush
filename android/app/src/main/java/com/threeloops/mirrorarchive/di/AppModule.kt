package com.threeloops.mirrorarchive.di

import com.threeloops.mirrorarchive.data.repo.FakeImageRepository
import com.threeloops.mirrorarchive.data.repo.ImageRepository
import com.threeloops.mirrorarchive.dupes.DuplicateFinder
import com.threeloops.mirrorarchive.dupes.DuplicateFinderStub
import com.threeloops.mirrorarchive.hash.PerceptualHasher
import com.threeloops.mirrorarchive.hash.PerceptualHasherStub
import com.threeloops.mirrorarchive.ml.ImageAnalyzer
import com.threeloops.mirrorarchive.ml.ImageAnalyzerStub
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt dependency injection module binding repository and engine stubs.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    /**
     * Binds [FakeImageRepository] as the application [ImageRepository].
     */
    @Binds
    @Singleton
    abstract fun bindImageRepository(impl: FakeImageRepository): ImageRepository

    /**
     * Binds [ImageAnalyzerStub] as the [ImageAnalyzer] component.
     */
    @Binds
    @Singleton
    abstract fun bindImageAnalyzer(impl: ImageAnalyzerStub): ImageAnalyzer

    /**
     * Binds [PerceptualHasherStub] as the [PerceptualHasher] component.
     */
    @Binds
    @Singleton
    abstract fun bindPerceptualHasher(impl: PerceptualHasherStub): PerceptualHasher

    /**
     * Binds [DuplicateFinderStub] as the [DuplicateFinder] component.
     */
    @Binds
    @Singleton
    abstract fun bindDuplicateFinder(impl: DuplicateFinderStub): DuplicateFinder
}
