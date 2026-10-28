package com.threeloops.mirrorarchive.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        ImageEntity::class,
        LabelEntity::class,
        AlbumEntryEntity::class,
        FaceEntity::class,
        HashBucketEntity::class,
        ScanStateEntity::class,
        SkippedImageEntity::class,
        PipelineStatEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(DatabaseTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun imageDao(): ImageDao
    abstract fun labelDao(): LabelDao
    abstract fun albumDao(): AlbumDao
    abstract fun faceDao(): FaceDao
    abstract fun scanStateDao(): ScanStateDao
    abstract fun skippedDao(): SkippedDao
    abstract fun pipelineStatDao(): PipelineStatDao

}
