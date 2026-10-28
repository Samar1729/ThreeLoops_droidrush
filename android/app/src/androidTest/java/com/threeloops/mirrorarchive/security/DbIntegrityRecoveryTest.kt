package com.threeloops.mirrorarchive.security

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.threeloops.mirrorarchive.contracts.ScanPhase
import com.threeloops.mirrorarchive.data.db.AppDatabase
import com.threeloops.mirrorarchive.data.db.ImageEntity
import com.threeloops.mirrorarchive.data.db.ScanStateEntity
import com.threeloops.mirrorarchive.di.DatabaseModule
import com.threeloops.mirrorarchive.work.ScanController
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import java.io.File
import java.security.KeyStore
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class DbIntegrityRecoveryTest {
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private lateinit var databaseName: String
    private lateinit var keyDirectory: File
    private lateinit var keyAlias: String
    private lateinit var keyProvider: KeyProvider
    private var database: AppDatabase? = null

    @Before
    fun setUp() {
        val suffix = UUID.randomUUID().toString().replace('-', '_')
        databaseName = "db-integrity-$suffix.db"
        keyAlias = "mirror_db_recovery_test_$suffix"
        keyDirectory = File(context.cacheDir, keyAlias).apply { mkdirs() }
        keyProvider = KeyProvider(
            keystoreProviderName = KEYSTORE_PROVIDER,
            keyFileDirectory = keyDirectory,
            alias = keyAlias,
            hasExistingDatabase = { context.getDatabasePath(databaseName).exists() }
        )
    }

    @After
    fun tearDown() {
        database?.close()
        database = null
        context.deleteDatabase(databaseName)
        keyProvider.resetForRecovery()
        keyDirectory.deleteRecursively()
        KeyStore.getInstance(KEYSTORE_PROVIDER).apply {
            load(null)
            if (containsAlias(keyAlias)) deleteEntry(keyAlias)
        }
    }

    @Test
    fun encryptedRoomFileIsNotReadableByPlatformSqlite() {
        database = DatabaseModule.build(context, databaseName, keyProvider)
        database!!.openHelper.writableDatabase

        assertTrue(DbIntegrity(context, databaseName).isEncrypted())
    }

    @Test
    fun missingWrappedKeyRebuildsEmptyDatabaseAndStartsAnotherScan() = runBlocking {
        val scanController = Mockito.mock(ScanController::class.java)
        database = DatabaseModule.build(context, databaseName, keyProvider) {
            scanController.start()
        }
        val db = database!!
        db.imageDao().saveBatchTx(
            images = listOf(
                ImageEntity(
                    mediaId = 11L,
                    uri = "content://test/11",
                    displayName = "11.jpg",
                    relativePath = "Pictures/",
                    dateAdded = 11L,
                    dateTaken = null,
                    generationModified = 11L,
                    width = 10,
                    height = 10,
                    sizeBytes = 100L,
                    dHash = 11L,
                    pHash = 11L,
                    blurScore = 10.0,
                    ocrText = null,
                    faceCount = 0,
                    indexedAt = 11L,
                    pipelineVersion = 1
                )
            ),
            labels = emptyList(),
            albums = emptyList(),
            faces = emptyList(),
            buckets = emptyList(),
            state = ScanStateEntity(
                lastProcessedId = 11L,
                lastGeneration = 11L,
                totalCount = 1L,
                processedCount = 1L,
                phase = ScanPhase.COMPLETE,
                updatedAt = 11L
            )
        )
        assertTrue(File(keyDirectory, KeyProvider.KEY_FILE_NAME).delete())

        DatabaseModule.recoverFromKeyLoss(db)

        assertEquals(0, db.imageDao().count().first())
        assertEquals(ScanPhase.IDLE, db.scanStateDao().observe().first()?.phase)
        assertTrue(File(keyDirectory, KeyProvider.KEY_FILE_NAME).isFile)
        assertTrue(DbIntegrity(context, databaseName).isEncrypted())
        Mockito.verify(scanController).start()
    }

    @Test
    fun missingDatabaseIsReportedAsNotCreated() {
        assertFalse(DbIntegrity(context, databaseName).isEncrypted())
    }

    private companion object { const val KEYSTORE_PROVIDER = "AndroidKeyStore" }
}
