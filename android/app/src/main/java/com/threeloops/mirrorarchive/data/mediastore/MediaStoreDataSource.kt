package com.threeloops.mirrorarchive.data.mediastore

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import com.threeloops.mirrorarchive.contracts.MediaRow
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Data source for querying images from the Android MediaStore.
 *
 * This is the ONLY class in the application allowed to communicate with MediaStore directly.
 * All MediaStore queries run on [Dispatchers.IO] and gracefully handle [SecurityException]
 * without crashing.
 *
 * @param context Application context used to obtain the [ContentResolver].
 */
@Singleton
class MediaStoreDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private companion object {
        const val TAG = "MediaStoreDataSource"

        val PROJECTION = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.RELATIVE_PATH,
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media.DATE_TAKEN,
            MediaStore.Images.Media.GENERATION_MODIFIED,
            MediaStore.Images.Media.WIDTH,
            MediaStore.Images.Media.HEIGHT,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.MIME_TYPE
        )
    }

    /**
     * Queries a page of media rows from [MediaStore.Images.Media.EXTERNAL_CONTENT_URI]
     * where `_id > [afterId]`, ordered by `_id ASC`.
     *
     * Uses the Bundle-based [ContentResolver.query] with explicit query arguments
     * ([ContentResolver.QUERY_ARG_SQL_SELECTION], [ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS],
     * [ContentResolver.QUERY_ARG_SQL_SORT_ORDER], and [ContentResolver.QUERY_ARG_LIMIT])
     * to guarantee that LIMIT is properly honored on Android 11+ (API 30+).
     *
     * @param afterId MediaStore row ID checkpoint; only rows with `_id > afterId` are returned.
     * @param limit Maximum number of records to return (defaults to 50).
     * @return List of matching [MediaRow] items, or empty list on denial/exception.
     */
    suspend fun query(afterId: Long, limit: Int = 50): List<MediaRow> = withContext(Dispatchers.IO) {
        val queryArgs = Bundle().apply {
            putString(ContentResolver.QUERY_ARG_SQL_SELECTION, "${MediaStore.Images.Media._ID} > ?")
            putStringArray(ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS, arrayOf(afterId.toString()))
            putString(ContentResolver.QUERY_ARG_SQL_SORT_ORDER, "${MediaStore.Images.Media._ID} ASC")
            putInt(ContentResolver.QUERY_ARG_LIMIT, limit)
        }

        try {
            context.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                PROJECTION,
                queryArgs,
                null
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                val pathCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.RELATIVE_PATH)
                val addedCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
                val takenCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)
                val genCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.GENERATION_MODIFIED)
                val widthCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH)
                val heightCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT)
                val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
                val mimeCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE)

                val results = ArrayList<MediaRow>(cursor.count)
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idCol)
                    val displayName = cursor.getString(nameCol) ?: ""
                    val relativePath = cursor.getString(pathCol) ?: ""
                    val dateAdded = cursor.getLong(addedCol)
                    val dateTaken = if (cursor.isNull(takenCol)) null else cursor.getLong(takenCol)
                    val generationModified = cursor.getLong(genCol)
                    val width = cursor.getInt(widthCol)
                    val height = cursor.getInt(heightCol)
                    val sizeBytes = cursor.getLong(sizeCol)
                    val mimeType = cursor.getString(mimeCol) ?: "image/jpeg"

                    results.add(
                        MediaRow(
                            mediaId = id,
                            uri = uriFor(id),
                            displayName = displayName,
                            relativePath = relativePath,
                            dateAdded = dateAdded,
                            dateTaken = dateTaken,
                            generationModified = generationModified,
                            width = width,
                            height = height,
                            sizeBytes = sizeBytes,
                            mimeType = mimeType
                        )
                    )
                }
                results
            } ?: emptyList()
        } catch (e: SecurityException) {
            Log.w(TAG, "SecurityException while querying MediaStore: ${e.message}")
            emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error querying MediaStore", e)
            emptyList()
        }
    }

    /**
     * Returns the total count of images currently recorded in MediaStore.
     *
     * @return Total image count, or 0 if cursor is null or query fails.
     */
    suspend fun count(): Int = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                arrayOf(MediaStore.Images.Media._ID),
                null,
                null
            )?.use { cursor ->
                cursor.count
            } ?: 0
        } catch (e: SecurityException) {
            Log.w(TAG, "SecurityException while counting MediaStore items: ${e.message}")
            0
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error counting MediaStore items", e)
            0
        }
    }

    /**
     * Checks whether an image at the specified [uri] exists and is readable.
     *
     * Returns false on any [SecurityException], [java.io.FileNotFoundException], or other exception.
     *
     * @param uri The content URI to check.
     * @return True if the URI can be opened for reading, false otherwise.
     */
    fun exists(uri: Uri): Boolean {
        return try {
            context.contentResolver.openAssetFileDescriptor(uri, "r")?.use { true } ?: false
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Generates a fully-qualified content [Uri] for a given MediaStore [_ID].
     *
     * @param mediaId System MediaStore row identifier.
     * @return Content URI pointing to the image record.
     */
    fun uriFor(mediaId: Long): Uri =
        ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, mediaId)
}
