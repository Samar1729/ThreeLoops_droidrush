package com.threeloops.mirrorarchive.di

import com.threeloops.mirrorarchive.data.mediastore.MediaStoreDataSource
import com.threeloops.mirrorarchive.data.mediastore.MediaStoreSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class MediaStoreModule {
    @Binds
    @Singleton
    abstract fun bindMediaStoreSource(source: MediaStoreDataSource): MediaStoreSource
}
