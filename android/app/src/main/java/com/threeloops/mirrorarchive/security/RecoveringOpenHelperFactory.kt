package com.threeloops.mirrorarchive.security

import android.content.Context
import android.database.sqlite.SQLiteException as FrameworkSQLiteException
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory

/** Rebuilds a derived index once if its wrapped SQLCipher key is lost or rejected. */
class RecoveringOpenHelperFactory(
    private val context: Context,
    private val databaseName: String,
    private val keyProvider: KeyProvider,
    private val onRecovered: () -> Unit = {}
) : SupportSQLiteOpenHelper.Factory {
    override fun create(configuration: SupportSQLiteOpenHelper.Configuration): SupportSQLiteOpenHelper =
        RecoveringSupportSQLiteOpenHelper(
            context.applicationContext,
            databaseName,
            keyProvider,
            configuration,
            onRecovered
        )
}

class RecoveringSupportSQLiteOpenHelper internal constructor(
    private val context: Context,
    private val dbName: String,
    private val keyProvider: KeyProvider,
    private val configuration: SupportSQLiteOpenHelper.Configuration,
    private val onRecovered: () -> Unit
) : SupportSQLiteOpenHelper {
    private val lock = Any()
    private var delegate: SupportSQLiteOpenHelper? = null
    private var passphraseBytes: ByteArray? = null
    private var writeAheadLoggingEnabled = false

    override val databaseName: String? get() = dbName

    override fun setWriteAheadLoggingEnabled(enabled: Boolean) {
        synchronized(lock) {
            writeAheadLoggingEnabled = enabled
            delegate?.setWriteAheadLoggingEnabled(enabled)
        }
    }

    override val writableDatabase: SupportSQLiteDatabase get() = open(readOnly = false)

    override val readableDatabase: SupportSQLiteDatabase get() = open(readOnly = true)

    override fun close() {
        synchronized(lock) { closeDelegate() }
    }

    fun recoverFromKeyLoss() {
        synchronized(lock) {
            closeDelegate()
            removeDatabaseFiles()
            keyProvider.resetForRecovery()
            val recovered = requireNotNull(createDelegate()).writableDatabase
            resetCheckpoint(recovered)
            markRecoveryNotice()
            runCatching(onRecovered)
        }
    }

    private fun open(readOnly: Boolean): SupportSQLiteDatabase = synchronized(lock) {
        try {
            val helper = createDelegate()
            val database = if (readOnly) helper.readableDatabase else helper.writableDatabase
            database
        } catch (failure: Exception) {
            if (!isKeyOrCipherOpenFailure(failure)) {
                throw failure
            }
            closeDelegate()
            removeDatabaseFiles()
            keyProvider.resetForRecovery()
            val recovered = requireNotNull(createDelegate()).writableDatabase
            resetCheckpoint(recovered)
            markRecoveryNotice()
            runCatching(onRecovered)
            recovered
        }
    }

    private fun createDelegate(): SupportSQLiteOpenHelper {
        delegate?.let { return it }
        val passphrase = keyProvider.getPassphrase()
        return try {
            val helper = SupportOpenHelperFactory(passphrase).create(configuration)
            helper.setWriteAheadLoggingEnabled(writeAheadLoggingEnabled)
            passphraseBytes = passphrase
            delegate = helper
            helper
        } catch (failure: Exception) {
            passphrase.fill(0)
            throw failure
        }
    }

    private fun closeDelegate() {
        runCatching { delegate?.close() }
        delegate = null
        clearPassphrase()
    }

    private fun clearPassphrase() {
        passphraseBytes?.fill(0)
        passphraseBytes = null
    }

    private fun removeDatabaseFiles() {
        context.deleteDatabase(dbName)
        val dbFile = context.getDatabasePath(dbName)
        listOf("-wal", "-shm", "-journal").forEach { suffix ->
            val sidecar = java.io.File(dbFile.path + suffix)
            if (sidecar.exists()) sidecar.delete()
        }
    }

    private fun resetCheckpoint(database: SupportSQLiteDatabase) {
        database.execSQL(
            """INSERT OR REPLACE INTO scan_state
                (id, lastProcessedId, lastGeneration, totalCount, processedCount, phase, updatedAt)
                VALUES (1, 0, 0, 0, 0, 'IDLE', ?)""",
            arrayOf<Any>(System.currentTimeMillis())
        )
    }

    private fun markRecoveryNotice() {
        DatabaseRecoveryNotifier.notifyReset(context)
    }

    private fun isKeyOrCipherOpenFailure(error: Throwable): Boolean {
        var current: Throwable? = error
        while (current != null) {
            if (current is KeyLossException) return true
            val name = current.javaClass.name
            val message = current.message.orEmpty().lowercase()
            val cipherError = message.contains("not a database") ||
                message.contains("file is encrypted") ||
                message.contains("decrypt") || message.contains("cipher") ||
                name.contains("NotADatabase")
            val sqliteError = current is FrameworkSQLiteException ||
                name.contains("SQLite")
            if (sqliteError && cipherError) return true
            current = current.cause
        }
        return false
    }

    companion object {
        const val RECOVERY_PREFS = "database_recovery"
        const val RECOVERY_NOTICE_PENDING = "index_reset_notice_pending"
    }
}
