package com.threeloops.mirrorarchive.di

import com.threeloops.mirrorarchive.data.mediastore.BitmapLoader
import com.threeloops.mirrorarchive.data.mediastore.ThumbnailLoader
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class BitmapLoaderModule {
    @Binds
    @Singleton
    abstract fun bindThumbnailLoader(implementation: BitmapLoader): ThumbnailLoader
}
