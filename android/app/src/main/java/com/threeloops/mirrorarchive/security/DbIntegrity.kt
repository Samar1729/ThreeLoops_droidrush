package com.threeloops.mirrorarchive.security

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import javax.inject.Singleton

@Singleton
class DbIntegrity(private val context: Context, private val databaseName: String = DATABASE_NAME) {
    fun isEncrypted(): Boolean {
        val databaseFile = context.getDatabasePath(databaseName)
        if (!databaseFile.isFile) return false
        return try {
            SQLiteDatabase.openDatabase(
                databaseFile.absolutePath,
                null,
                SQLiteDatabase.OPEN_READONLY
            ).use { database ->
                database.rawQuery("SELECT name FROM sqlite_master LIMIT 1", null).use { cursor ->
                    while (cursor.moveToNext()) Unit
                }
                false
            }
        } catch (_: SQLiteException) {
            true
        } catch (_: Exception) {
            false
        }
    }

    private companion object { const val DATABASE_NAME = "mirror_archive.db" }
}
