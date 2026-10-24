package com.threeloops.mirrorarchive.hash

import android.graphics.Bitmap
import com.threeloops.mirrorarchive.contracts.HashBundle
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.cos
import kotlin.math.sqrt

/**
 * Section 3.11 Perceptual Hasher
 * Interface for computing difference hashes, perceptual hashes, and blur scores.
 * Owner: Samar (Feature S-1)
 */
interface PerceptualHasher {
    /**
     * Computes 64-bit difference hash (dHash).
     */
    fun dHash(bmp: Bitmap): Long

    /**
     * Computes 64-bit perceptual hash (pHash).
     */
    fun pHash(bmp: Bitmap): Long

    /**
     * Computes Hamming distance between two 64-bit hashes.
     */
    fun hamming(a: Long, b: Long): Int

    /**
     * Computes blur score (Laplacian variance) on the given bitmap.
     */
    fun blurScore(bmp: Bitmap): Double

    /**
     * Computes dHash, pHash, and blur score in a single bundle from a shared buffer.
     */
    fun hashAll(bmp: Bitmap): HashBundle

    /**
     * Converts bitmap into an 8-bit grayscale array of size [w] x [h].
     */
    fun toGray(bmp: Bitmap, w: Int, h: Int): IntArray

    /**
     * Computes candidate hash bucket keys for duplicate clustering.
     */
    fun bucketKeys(hash: Long): IntArray

    companion object : PerceptualHasher {
        /**
         * Precomputed 32x32 1D DCT cosine matrix (Orthonormal DCT-II)
         */
        val DCT_COSINE_MATRIX: Array<DoubleArray> = Array(32) { u ->
            val s = if (u == 0) sqrt(1.0 / 32.0) else sqrt(2.0 / 32.0)
            DoubleArray(32) { x ->
                s * cos((2.0 * x + 1.0) * u * Math.PI / 64.0)
            }
        }

        override fun dHash(bmp: Bitmap): Long {
            val gray = toGray(bmp, 9, 8)
            return dHashFromGray(gray)
        }

        override fun pHash(bmp: Bitmap): Long {
            val gray = toGray(bmp, 32, 32)
            return pHashFromGray(gray)
        }

        override fun hamming(a: Long, b: Long): Int {
            return java.lang.Long.bitCount(a xor b)
        }

        override fun blurScore(bmp: Bitmap): Double {
            val size = 256
            val gray = toGray(bmp, size, size)
            return blurScoreFromGray(gray, size, size)
        }

        override fun hashAll(bmp: Bitmap): HashBundle {
            val gray256 = toGray(bmp, 256, 256)
            val blur = blurScoreFromGray(gray256, 256, 256)
            val gray32 = downsample(gray256, 256, 256, 32, 32)
            val pHash = pHashFromGray(gray32)
            val gray9x8 = downsample(gray256, 256, 256, 9, 8)
            val dHash = dHashFromGray(gray9x8)
            return HashBundle(dHash = dHash, pHash = pHash, blur = blur)
        }

        override fun toGray(bmp: Bitmap, w: Int, h: Int): IntArray {
            val scaled = if (bmp.width == w && bmp.height == h) {
                bmp
            } else {
                Bitmap.createScaledBitmap(bmp, w, h, true)
            }
            val pixels = IntArray(w * h)
            scaled.getPixels(pixels, 0, w, 0, 0, w, h)
            if (scaled != bmp) {
                scaled.recycle()
            }
            val gray = IntArray(w * h)
            for (i in pixels.indices) {
                val c = pixels[i]
                val r = (c ushr 16) and 0xFF
                val g = (c ushr 8) and 0xFF
                val b = c and 0xFF
                gray[i] = (299 * r + 587 * g + 114 * b + 500) / 1000
            }
            return gray
        }

        override fun bucketKeys(hash: Long): IntArray {
            val keys = IntArray(8)
            for (i in 0 until 8) {
                val shift = (7 - i) * 8
                keys[i] = ((hash ushr shift) and 0xFFL).toInt()
            }
            return keys
        }

        fun dHashFromGray(gray9x8: IntArray): Long {
            require(gray9x8.size >= 72) { "dHash input buffer must contain at least 72 pixels (9x8)" }
            var hash = 0L
            var bitIndex = 0
            for (y in 0 until 8) {
                val rowOffset = y * 9
                for (x in 0 until 8) {
                    val left = gray9x8[rowOffset + x]
                    val right = gray9x8[rowOffset + x + 1]
                    if (left > right) {
                        hash = hash or (1L shl bitIndex)
                    }
                    bitIndex++
                }
            }
            return hash
        }

        fun pHashFromGray(gray32x32: IntArray): Long {
            require(gray32x32.size >= 1024) { "pHash input buffer must contain at least 1024 pixels (32x32)" }
            val rowDct = Array(8) { DoubleArray(32) }
            for (u in 0 until 8) {
                val cRow = DCT_COSINE_MATRIX[u]
                val uTarget = rowDct[u]
                for (y in 0 until 32) {
                    val yOffset = y * 32
                    var sum = 0.0
                    for (x in 0 until 32) {
                        sum += cRow[x] * gray32x32[yOffset + x]
                    }
                    uTarget[y] = sum
                }
            }
            val dct8x8 = DoubleArray(64)
            for (u in 0 until 8) {
                val rowValues = rowDct[u]
                for (v in 0 until 8) {
                    val cCol = DCT_COSINE_MATRIX[v]
                    var sum = 0.0
                    for (y in 0 until 32) {
                        sum += cCol[y] * rowValues[y]
                    }
                    dct8x8[u * 8 + v] = sum
                }
            }
            val acCoeffs = DoubleArray(63)
            for (i in 1 until 64) {
                acCoeffs[i - 1] = dct8x8[i]
            }
            acCoeffs.sort()
            val median = acCoeffs[31]
            var hash = 0L
            for (i in 0 until 64) {
                if (dct8x8[i] > median) {
                    hash = hash or (1L shl i)
                }
            }
            return hash
        }

        fun blurScoreFromGray(gray: IntArray, width: Int, height: Int): Double {
            if (width < 3 || height < 3 || gray.size < width * height) return 0.0
            var sum = 0.0
            var sumSq = 0.0
            var count = 0
            for (y in 1 until height - 1) {
                val row = y * width
                val rowAbove = (y - 1) * width
                val rowBelow = (y + 1) * width
                for (x in 1 until width - 1) {
                    val lap = gray[rowAbove + x] +
                            gray[rowBelow + x] +
                            gray[row + (x - 1)] +
                            gray[row + (x + 1)] -
                            4 * gray[row + x]
                    sum += lap
                    sumSq += (lap.toDouble() * lap.toDouble())
                    count++
                }
            }
            if (count == 0) return 0.0
            val mean = sum / count
            val variance = (sumSq / count) - (mean * mean)
            return if (variance > 0.0) variance else 0.0
        }

        fun downsample(src: IntArray, srcW: Int, srcH: Int, dstW: Int, dstH: Int): IntArray {
            val dst = IntArray(dstW * dstH)
            val scaleX = srcW.toDouble() / dstW
            val scaleY = srcH.toDouble() / dstH
            for (dy in 0 until dstH) {
                val syStart = (dy * scaleY).toInt()
                val syEnd = ((dy + 1) * scaleY).toInt().coerceAtMost(srcH)
                for (dx in 0 until dstW) {
                    val sxStart = (dx * scaleX).toInt()
                    val sxEnd = ((dx + 1) * scaleX).toInt().coerceAtMost(srcW)
                    var sum = 0
                    var count = 0
                    for (sy in syStart until syEnd) {
                        val row = sy * srcW
                        for (sx in sxStart until sxEnd) {
                            sum += src[row + sx]
                            count++
                        }
                    }
                    dst[dy * dstW + dx] = if (count > 0) sum / count else 0
                }
            }
            return dst
        }
    }
}

/**
 * Implementation of [PerceptualHasher] injected across the application.
 */
@Singleton
class PerceptualHasherStub @Inject constructor() : PerceptualHasher by PerceptualHasher.Companion
