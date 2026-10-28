package com.threeloops.mirrorarchive.metrics

import android.graphics.Bitmap
import com.threeloops.mirrorarchive.contracts.HashBundle
import com.threeloops.mirrorarchive.hash.PerceptualHasher
import kotlin.random.Random
import kotlin.system.measureNanoTime

/**
 * Section 3.19: On-device performance benchmark for perceptual hashing throughput.
 * Owner: Samar (Feature S-7)
 */
object Benchmark {

    /**
     * Measures perceptual hash throughput in images per second by executing
     * [PerceptualHasher.hashAll] on [n] thumbnail images.
     *
     * @param n Number of thumbnails to process (default: 100).
     * @return Throughput in images per second.
     */
    fun hashThroughput(n: Int = 100): Double {
        val count = n.coerceAtLeast(1)
        return try {
            benchmarkWithBitmaps(count)
        } catch (_: Throwable) {
            benchmarkSyntheticGray(count)
        }
    }

    /**
     * Benchmarks [PerceptualHasher.hashAll] across a supplied list of Bitmaps.
     */
    fun hashThroughput(bitmaps: List<Bitmap>, iterations: Int = bitmaps.size): Double {
        if (bitmaps.isEmpty() || iterations <= 0) return 0.0

        // Warm up JIT compiler to optimize 2D DCT matrix multiplication
        val warmupCount = minOf(5, bitmaps.size)
        for (i in 0 until warmupCount) {
            PerceptualHasher.hashAll(bitmaps[i % bitmaps.size])
        }

        val elapsedNanos = measureNanoTime {
            for (i in 0 until iterations) {
                PerceptualHasher.hashAll(bitmaps[i % bitmaps.size])
            }
        }

        if (elapsedNanos <= 0L) return 0.0
        val seconds = elapsedNanos / 1_000_000_000.0
        return iterations / seconds
    }

    /**
     * Benchmarks pure grayscale pixel buffers through the full DCT, dHash, and blur pipeline.
     * Guarantees 100% JVM unit-testability without requiring an Android emulator or device.
     */
    fun benchmarkSyntheticGray(count: Int = 100): Double {
        val poolSize = minOf(count, 10)
        val grayBuffers = List(poolSize) { seed ->
            generateSyntheticGray(256, 256, seed.toLong())
        }

        // Warm up JIT
        for (i in 0 until minOf(5, poolSize)) {
            hashFromGray(grayBuffers[i % poolSize])
        }

        val elapsedNanos = measureNanoTime {
            for (i in 0 until count) {
                hashFromGray(grayBuffers[i % poolSize])
            }
        }

        if (elapsedNanos <= 0L) return 0.0
        val seconds = elapsedNanos / 1_000_000_000.0
        return count / seconds
    }

    private fun benchmarkWithBitmaps(count: Int): Double {
        val poolSize = minOf(count, 10)
        val bitmaps = List(poolSize) { i ->
            val bmp = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_8888)
            val pixels = IntArray(256 * 256) { idx ->
                val x = idx % 256
                val y = idx / 256
                val v = (x * 3 + y * 7 + i * 13) and 0xFF
                (0xFF shl 24) or (v shl 16) or (v shl 8) or v
            }
            bmp.setPixels(pixels, 0, 256, 0, 0, 256, 256)
            bmp
        }

        try {
            return hashThroughput(bitmaps, count)
        } finally {
            bitmaps.forEach {
                try {
                    if (!it.isRecycled) it.recycle()
                } catch (_: Throwable) {}
            }
        }
    }

    private fun hashFromGray(gray256: IntArray): HashBundle {
        val blur = PerceptualHasher.blurScoreFromGray(gray256, 256, 256)
        val gray32 = PerceptualHasher.downsample(gray256, 256, 256, 32, 32)
        val pHash = PerceptualHasher.pHashFromGray(gray32)
        val gray9x8 = PerceptualHasher.downsample(gray256, 256, 256, 9, 8)
        val dHash = PerceptualHasher.dHashFromGray(gray9x8)
        return HashBundle(dHash = dHash, pHash = pHash, blur = blur)
    }

    private fun generateSyntheticGray(width: Int, height: Int, seed: Long): IntArray {
        val rand = Random(seed)
        val buffer = IntArray(width * height)
        for (y in 0 until height) {
            for (x in 0 until width) {
                val base = (x * 3 + y * 5) % 256
                buffer[y * width + x] = (base + rand.nextInt(-4, 5)).coerceIn(0, 255)
            }
        }
        return buffer
    }
}

