package com.threeloops.mirrorarchive.di

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import com.threeloops.mirrorarchive.data.db.AppDatabase
import com.threeloops.mirrorarchive.data.db.SkippedDao
import com.threeloops.mirrorarchive.data.db.ScanStateDao
import com.threeloops.mirrorarchive.security.KeyProvider
import com.threeloops.mirrorarchive.security.DbIntegrity
import com.threeloops.mirrorarchive.security.RecoveringOpenHelperFactory
import com.threeloops.mirrorarchive.security.RecoveringSupportSQLiteOpenHelper
import com.threeloops.mirrorarchive.work.ScanController
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Provider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    private const val DATABASE_NAME = "mirror_archive.db"
    private const val MIGRATION_PREFS = "database_migration"
    private const val LEGACY_DATABASE_REMOVED = "legacy_plaintext_database_removed"

    @Provides
    @Singleton
    fun provideKeyProvider(@ApplicationContext context: Context): KeyProvider = KeyProvider(
        keystoreProviderName = "AndroidKeyStore",
        keyFileDirectory = context.noBackupFilesDir,
        hasExistingDatabase = { context.getDatabasePath(DATABASE_NAME).exists() }
    )

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context,
        keyProvider: KeyProvider,
        scanControllerProvider: Provider<ScanController>
    ): AppDatabase = build(context, keyProvider) {
        scanControllerProvider.get().start()
    }

    fun build(ctx: Context): AppDatabase = build(
        ctx,
        createKeyProvider(ctx.applicationContext)
    )

    private fun build(ctx: Context, keyProvider: KeyProvider, onRecovered: () -> Unit = {}): AppDatabase {
        val context = ctx.applicationContext
        val migrationPrefs = context.getSharedPreferences(MIGRATION_PREFS, Context.MODE_PRIVATE)
        if (!migrationPrefs.getBoolean(LEGACY_DATABASE_REMOVED, false)) {
            // This index is derived data. Remove the pre-SQLCipher plaintext database once.
            context.deleteDatabase(DATABASE_NAME)
            check(migrationPrefs.edit().putBoolean(LEGACY_DATABASE_REMOVED, true).commit()) {
                "Could not save the legacy database migration marker"
            }
        }
        return buildWithRecovery(context, DATABASE_NAME, keyProvider, onRecovered)
    }

    /** Uses the same SQLCipher configuration for isolated Room instrumented-test databases. */
    fun build(ctx: Context, databaseName: String, onRecovered: () -> Unit = {}): AppDatabase {
        val context = ctx.applicationContext
        return buildWithRecovery(context, databaseName, createKeyProvider(context), onRecovered)
    }

    fun build(
        ctx: Context,
        databaseName: String,
        keyProvider: KeyProvider,
        onRecovered: () -> Unit = {}
    ): AppDatabase = buildWithRecovery(ctx.applicationContext, databaseName, keyProvider, onRecovered)

    private fun buildWithRecovery(
        ctx: Context,
        databaseName: String,
        keyProvider: KeyProvider,
        onRecovered: () -> Unit = {}
    ): AppDatabase {
        val context = ctx.applicationContext
        System.loadLibrary("sqlcipher")
        return Room.databaseBuilder(context, AppDatabase::class.java, databaseName)
            .openHelperFactory(RecoveringOpenHelperFactory(context, databaseName, keyProvider, onRecovered))
            .fallbackToDestructiveMigration()
            .addCallback(object : androidx.room.RoomDatabase.Callback() {
                override fun onOpen(db: SupportSQLiteDatabase) {
                    super.onOpen(db)
                    db.setForeignKeyConstraintsEnabled(true)
                }
            })
            .build()
    }

    fun recoverFromKeyLoss(database: AppDatabase) {
        val helper = database.openHelper as? RecoveringSupportSQLiteOpenHelper
            ?: error("AppDatabase was not built with the recoverable SQLCipher factory")
        // Close the underlying connection while keeping Room's wrapper reusable after rebuild.
        helper.close()
        helper.recoverFromKeyLoss()
    }

    private fun createKeyProvider(context: Context) = KeyProvider(
        keystoreProviderName = "AndroidKeyStore",
        keyFileDirectory = context.noBackupFilesDir,
        hasExistingDatabase = { context.getDatabasePath(DATABASE_NAME).exists() }
    )

    @Provides
    @Singleton
    fun provideDbIntegrity(@ApplicationContext context: Context): DbIntegrity = DbIntegrity(context)

    @Provides
    @Singleton
    fun provideSkippedDao(database: AppDatabase): SkippedDao = database.skippedDao()

    @Provides
    @Singleton
    fun provideScanStateDao(database: AppDatabase): ScanStateDao = database.scanStateDao()
}
