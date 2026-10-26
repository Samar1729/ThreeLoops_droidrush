package com.threeloops.mirrorarchive.data.mediastore

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Size
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Loads bounded MediaStore thumbnails for image analysis.
 *
 * Callers must be off the main thread. Always call [recycle] in a `finally` block when done
 * with a bitmap. Never keep a `List<Bitmap>`; keep only ids and hashes between operations.
 */
@Singleton
class BitmapLoader @Inject constructor(
    @ApplicationContext private val context: Context
) {

    /**
     * Load a MediaStore thumbnail with its longest side bounded by [maxSide].
     *
     * Never decodes the full-size image. The caller owns the returned bitmap and must call
     * [recycle] in a `finally` block. Call this method off the main thread.
     */
    fun load(uri: Uri, maxSide: Int = 512): Bitmap? {
        var bitmap: Bitmap? = null
        return try {
            require(maxSide > 0) { "maxSide must be positive" }
            bitmap = context.contentResolver.loadThumbnail(uri, Size(maxSide, maxSide), null)
            if (bitmap.config == Bitmap.Config.HARDWARE) {
                val softwareBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, false)
                    ?: return null
                recycle(bitmap)
                bitmap = null
                softwareBitmap
            } else {
                bitmap.also { bitmap = null }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: IOException) {
            null
        } catch (e: SecurityException) {
            null
        } catch (e: IllegalArgumentException) {
            null
        } catch (e: Exception) {
            null
        } finally {
            // If conversion failed, don't leak the hardware bitmap returned by the resolver.
            recycle(bitmap)
        }
    }

    /** Load a thumbnail sized for OCR. The caller must recycle the returned bitmap. */
    fun loadForOcr(uri: Uri): Bitmap? = load(uri, 1024)

    /**
     * Load a thumbnail, scale it to [side] x [side], and return row-major luminance values.
     *
     * The temporary bitmap is always recycled. Call this method off the main thread.
     */
    fun loadGray(uri: Uri, side: Int): IntArray? {
        var source: Bitmap? = null
        var scaled: Bitmap? = null
        return try {
            require(side > 0) { "side must be positive" }
            source = load(uri, side) ?: return null
            scaled = Bitmap.createScaledBitmap(source, side, side, true)
            val pixels = IntArray(side * side)
            scaled.getPixels(pixels, 0, side, 0, 0, side, side)
            IntArray(pixels.size) { index ->
                val color = pixels[index]
                val red = (color shr 16) and 0xFF
                val green = (color shr 8) and 0xFF
                val blue = color and 0xFF
                (0.299 * red + 0.587 * green + 0.114 * blue).toInt()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        } finally {
            if (scaled !== source) recycle(scaled)
            recycle(source)
        }
    }

    /** Recycle [bmp] if non-null and not already recycled. */
    fun recycle(bmp: Bitmap?) {
        if (bmp != null && !bmp.isRecycled) bmp.recycle()
    }
}
