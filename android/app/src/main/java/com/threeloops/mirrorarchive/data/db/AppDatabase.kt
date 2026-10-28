package com.threeloops.mirrorarchive.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase

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

    companion object {
        private const val DATABASE_NAME = "mirror_archive.db"

        /**
         * Build the development database. Kept behind one factory so R-10 can switch the
         * underlying open helper to SQLCipher without changing Hilt consumers.
         */
        fun build(ctx: Context): AppDatabase = Room.databaseBuilder(
            ctx.applicationContext,
            AppDatabase::class.java,
            DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .addCallback(object : RoomDatabase.Callback() {
                override fun onOpen(db: SupportSQLiteDatabase) {
                    super.onOpen(db)
                    db.setForeignKeyConstraintsEnabled(true)
                }
            })
            .build()
    }
}
