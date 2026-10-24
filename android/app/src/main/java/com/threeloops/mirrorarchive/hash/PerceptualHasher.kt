package com.threeloops.mirrorarchive.hash

import android.graphics.Bitmap
import com.threeloops.mirrorarchive.contracts.HashBundle
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Interface for computing difference hashes, perceptual hashes, and blur scores.
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
     * Computes dHash, pHash, and blur score in a single bundle.
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
}

/**
 * Stub implementation of [PerceptualHasher].
 */
@Singleton
class PerceptualHasherStub @Inject constructor() : PerceptualHasher {
    override fun dHash(bmp: Bitmap): Long = 0L
    override fun pHash(bmp: Bitmap): Long = 0L
    override fun hamming(a: Long, b: Long): Int = (a xor b).countOneBits()
    override fun blurScore(bmp: Bitmap): Double = 0.0
    override fun hashAll(bmp: Bitmap): HashBundle = HashBundle(0L, 0L, 0.0)
    override fun toGray(bmp: Bitmap, w: Int, h: Int): IntArray = IntArray(w * h)
    override fun bucketKeys(hash: Long): IntArray = IntArray(0)
}
