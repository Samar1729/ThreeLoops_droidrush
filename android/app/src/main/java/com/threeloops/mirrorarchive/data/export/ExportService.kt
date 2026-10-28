package com.threeloops.mirrorarchive.data.export

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.net.Uri
import android.provider.MediaStore
import android.provider.OpenableColumns
import com.threeloops.mirrorarchive.data.mediastore.ThumbnailLoader
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.IOException
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import javax.inject.Singleton

data class ExportProgress(
    val completedFiles: Int,
    val totalFiles: Int,
    val currentFile: String
)

@Singleton
class ExportService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val thumbnailLoader: ThumbnailLoader
) {
    private val _progress = MutableStateFlow<ExportProgress?>(null)
    val progress: StateFlow<ExportProgress?> = _progress.asStateFlow()

    suspend fun exportZip(uris: List<Uri>, name: String): Uri {
        require(uris.isNotEmpty()) { "Select at least one photo to export." }
        val resolver = context.contentResolver
        val archiveName = "${ExportFileNames.safeArchiveName(name)}.zip"
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, archiveName)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/zip")
            put(MediaStore.MediaColumns.RELATIVE_PATH, DOWNLOAD_DIRECTORY)
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }
        val archiveUri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
            ?: throw IOException("Unable to create the ZIP archive in Downloads.")
        var published = false
        try {
            val output = resolver.openOutputStream(archiveUri, "w")
                ?: throw IOException("Unable to write the ZIP archive.")
            val entryNames = mutableSetOf<String>()
            ZipOutputStream(BufferedOutputStream(output)).use { zip ->
                uris.forEachIndexed { index, uri ->
                    val displayName = sourceDisplayName(uri)
                    val entryName = ExportFileNames.uniqueEntryName(displayName, entryNames)
                    zip.putNextEntry(ZipEntry(entryName))
                    val input = resolver.openInputStream(uri)
                        ?: throw IOException("Unable to read photo: $displayName")
                    var copiedBytes = 0L
                    BufferedInputStream(input).use { source ->
                        val buffer = ByteArray(COPY_BUFFER_SIZE)
                        while (true) {
                            val count = source.read(buffer)
                            if (count < 0) break
                            zip.write(buffer, 0, count)
                            copiedBytes += count
                        }
                    }
                    zip.closeEntry()
                    _progress.value = ExportProgress(
                        completedFiles = index + 1,
                        totalFiles = uris.size,
                        currentFile = "$entryName (${formatBytes(copiedBytes)})"
                    )
                }
            }

            val publishValues = ContentValues().apply {
                put(MediaStore.MediaColumns.IS_PENDING, 0)
            }
            if (resolver.update(archiveUri, publishValues, null, null) != 1) {
                throw IOException("Unable to publish the ZIP archive.")
            }
            published = true
            return archiveUri
        } finally {
            if (!published) resolver.delete(archiveUri, null, null)
            _progress.value = null
        }
    }

    suspend fun makeCollage(uris: List<Uri>, grid: Int): Bitmap {
        require(grid == 2 || grid == 3) { "Collage grid must be 2x2 or 3x3." }
        require(uris.size >= grid * grid) {
            "Select at least ${grid * grid} photos for a ${grid}x$grid collage."
        }
        val outputSize = COLLAGE_SIZE
        val gutter = COLLAGE_GUTTER
        val cellSize = (outputSize - gutter * (grid + 1)) / grid
        val bitmap = Bitmap.createBitmap(outputSize, outputSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap).apply { drawColor(Color.BLACK) }
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        var drawn = 0
        try {
            for (uri in uris.take(grid * grid)) {
                val thumbnail = thumbnailLoader.load(uri, COLLAGE_THUMBNAIL_SIZE)
                    ?: throw IOException("Unable to load photo for collage: $uri")
                try {
                    val left = gutter + (drawn % grid) * (cellSize + gutter)
                    val top = gutter + (drawn / grid) * (cellSize + gutter)
                    canvas.drawBitmap(
                        thumbnail,
                        centerCropSource(thumbnail.width, thumbnail.height),
                        RectF(
                            left.toFloat(),
                            top.toFloat(),
                            (left + cellSize).toFloat(),
                            (top + cellSize).toFloat()
                        ),
                        paint
                    )
                } finally {
                    thumbnailLoader.recycle(thumbnail)
                }
                drawn++
            }
            return bitmap
        } catch (exception: Exception) {
            bitmap.recycle()
            throw exception
        }
    }

    suspend fun saveCollage(bmp: Bitmap): Uri {
        require(!bmp.isRecycled) { "Cannot save a recycled collage bitmap." }
        val resolver = context.contentResolver
        val values = ContentValues().apply {
            put(
                MediaStore.MediaColumns.DISPLAY_NAME,
                "MirrorArchive_Collage_${System.currentTimeMillis()}.jpg"
            )
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.MediaColumns.RELATIVE_PATH, PICTURES_DIRECTORY)
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }
        val collageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            ?: throw IOException("Unable to create the collage in Pictures/MirrorArchive.")
        var published = false
        try {
            val output = resolver.openOutputStream(collageUri, "w")
                ?: throw IOException("Unable to write the collage.")
            output.use {
                if (!bmp.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, it)) {
                    throw IOException("Unable to encode the collage as JPEG.")
                }
            }
            val publishValues = ContentValues().apply {
                put(MediaStore.MediaColumns.IS_PENDING, 0)
            }
            if (resolver.update(collageUri, publishValues, null, null) != 1) {
                throw IOException("Unable to publish the collage.")
            }
            published = true
            return collageUri
        } finally {
            if (!published) resolver.delete(collageUri, null, null)
        }
    }

    private fun sourceDisplayName(uri: Uri): String {
        val resolver = context.contentResolver
        resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index >= 0) cursor.getString(index)?.takeIf(String::isNotBlank)?.let { return it }
            }
        }
        return uri.lastPathSegment?.substringAfterLast('/')?.takeIf(String::isNotBlank)
            ?: "photo"
    }

    private fun centerCropSource(width: Int, height: Int): Rect {
        val side = minOf(width, height)
        val left = (width - side) / 2
        val top = (height - side) / 2
        return Rect(left, top, left + side, top + side)
    }

    private fun formatBytes(bytes: Long): String =
        String.format(Locale.ROOT, "%.1f MB", bytes / (1024.0 * 1024.0))

    private companion object {
        const val COPY_BUFFER_SIZE = 8 * 1024
        const val COLLAGE_SIZE = 1_024
        const val COLLAGE_THUMBNAIL_SIZE = 512
        const val COLLAGE_GUTTER = 8
        const val JPEG_QUALITY = 90
        const val DOWNLOAD_DIRECTORY = "Download/MirrorArchive"
        const val PICTURES_DIRECTORY = "Pictures/MirrorArchive"
    }
}

internal object ExportFileNames {
    private const val MAX_ARCHIVE_NAME_LENGTH = 80
    private val unsafeNameCharacters = Regex("[^A-Za-z0-9._ -]")

    fun safeArchiveName(name: String): String {
        val safeName = name.trim()
            .replace(unsafeNameCharacters, "_")
            .trim('.', '_', ' ')
            .take(MAX_ARCHIVE_NAME_LENGTH)
        return safeName.ifBlank { "MirrorArchive_Export" }
    }

    fun uniqueEntryName(name: String, usedNames: MutableSet<String>): String {
        val safeName = name.substringAfterLast('/').substringAfterLast('\\')
            .replace(unsafeNameCharacters, "_")
            .trim('.', ' ')
            .ifBlank { "photo" }
        val extensionIndex = safeName.lastIndexOf('.')
        val base = if (extensionIndex > 0) safeName.substring(0, extensionIndex) else safeName
        val extension = if (extensionIndex > 0) safeName.substring(extensionIndex) else ""
        var candidate = "$base$extension"
        var suffix = 2
        while (!usedNames.add(candidate.lowercase(Locale.ROOT))) {
            candidate = "$base ($suffix)$extension"
            suffix++
        }
        return candidate
    }
}
