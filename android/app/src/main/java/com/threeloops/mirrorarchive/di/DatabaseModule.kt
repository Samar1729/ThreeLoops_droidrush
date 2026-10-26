package com.threeloops.mirrorarchive.di

import android.content.Context
import com.threeloops.mirrorarchive.data.db.AppDatabase
import com.threeloops.mirrorarchive.data.db.SkippedDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        AppDatabase.build(context)

    @Provides
    @Singleton
    fun provideSkippedDao(database: AppDatabase): SkippedDao = database.skippedDao()
}
