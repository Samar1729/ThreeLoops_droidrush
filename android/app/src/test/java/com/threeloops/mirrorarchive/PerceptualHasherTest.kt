package com.threeloops.mirrorarchive

import com.threeloops.mirrorarchive.hash.PerceptualHasher
import org.junit.Assert.*
import org.junit.Test
import kotlin.random.Random

/**
 * Section 10 Test Plan: JVM Unit Tests for PerceptualHasher (Feature S-1)
 * Owner: Samar
 *
 * Proves:
 * 1. Identical images -> distance = 0
 * 2. Recompressed / noisy / slight brightness variation -> distance is small (<= 6)
 * 3. Unrelated images -> distance is large (>= 18)
 * 4. Blur score -> sharp images have high variance, smooth/blurry have near-zero variance
 * 5. Bucket keys -> correctly splits 64-bit hash into 8 chunks of 8 bits
 * 6. Rotated images -> documented as a known limit (distance is large)
 */
class PerceptualHasherTest {

    private fun generateSyntheticImage(width: Int, height: Int, seed: Long = 42L): IntArray {
        val rand = Random(seed)
        val img = IntArray(width * height)
        for (y in 0 until height) {
            for (x in 0 until width) {
                // Natural pattern with multiple harmonic frequencies
                val wave1 = Math.sin(x * 0.35) * 45.0
                val wave2 = Math.cos(y * 0.35) * 45.0
                val wave3 = Math.sin((x + y) * 0.15) * 30.0
                val base = (128.0 + wave1 + wave2 + wave3).toInt().coerceIn(20, 235)
                val noise = rand.nextInt(-3, 4)
                img[y * width + x] = (base + noise).coerceIn(0, 255)
            }
        }
        return img
    }

    @Test
    fun testIdenticalImagesHaveZeroHammingDistance() {
        val img9x8 = generateSyntheticImage(9, 8, seed = 101L)
        val dHash1 = PerceptualHasher.dHashFromGray(img9x8)
        val dHash2 = PerceptualHasher.dHashFromGray(img9x8)
        assertEquals("Identical images must have dHash hamming distance of 0", 0, PerceptualHasher.hamming(dHash1, dHash2))

        val img32x32 = generateSyntheticImage(32, 32, seed = 202L)
        val pHash1 = PerceptualHasher.pHashFromGray(img32x32)
        val pHash2 = PerceptualHasher.pHashFromGray(img32x32)
        assertEquals("Identical images must have pHash hamming distance of 0", 0, PerceptualHasher.hamming(pHash1, pHash2))
    }

    @Test
    fun testRecompressedOrNoisyImagesHaveSmallDistance() {
        // Base image with harmonic content
        val base32x32 = generateSyntheticImage(32, 32, seed = 303L)
        val basePHash = PerceptualHasher.pHashFromGray(base32x32)

        // Slightly modified image (simulating compression artifact noise: +/- 3 intensity shifts)
        val noisy32x32 = base32x32.clone()
        val rand = Random(999L)
        for (i in noisy32x32.indices) {
            noisy32x32[i] = (noisy32x32[i] + rand.nextInt(-3, 4)).coerceIn(0, 255)
        }
        val noisyPHash = PerceptualHasher.pHashFromGray(noisy32x32)
        val distPHash = PerceptualHasher.hamming(basePHash, noisyPHash)

        assertTrue(
            "Recompressed/noisy image must have small pHash distance (got $distPHash, expected <= 8 as per pHashMax spec)",
            distPHash <= 8
        )

        // dHash test with slight noise
        val base9x8 = generateSyntheticImage(9, 8, seed = 404L)
        val baseDHash = PerceptualHasher.dHashFromGray(base9x8)
        val noisy9x8 = base9x8.clone()
        for (i in noisy9x8.indices) {
            noisy9x8[i] = (noisy9x8[i] + rand.nextInt(-2, 3)).coerceIn(0, 255)
        }
        val noisyDHash = PerceptualHasher.dHashFromGray(noisy9x8)
        val distDHash = PerceptualHasher.hamming(baseDHash, noisyDHash)

        assertTrue(
            "Recompressed/noisy image must have small dHash distance (got $distDHash, expected <= 6 as per dHashMax spec)",
            distDHash <= 6
        )
    }

    @Test
    fun testUnrelatedImagesHaveLargeDistance() {
        // Image A: Concentric circular waves (radial pattern)
        val imgA = IntArray(32 * 32) { i ->
            val x = (i % 32) - 16.0
            val y = (i / 32) - 16.0
            val r = Math.sqrt(x * x + y * y)
            ((Math.sin(r * 0.6) + 1.0) * 120.0).toInt().coerceIn(0, 255)
        }

        // Image B: High frequency diagonal checkerboard pattern
        val imgB = IntArray(32 * 32) { i ->
            val x = i % 32
            val y = i / 32
            ((Math.cos(x * 1.5) * Math.sin(y * 1.5) + 1.0) * 120.0).toInt().coerceIn(0, 255)
        }

        val pHashA = PerceptualHasher.pHashFromGray(imgA)
        val pHashB = PerceptualHasher.pHashFromGray(imgB)
        val distP = PerceptualHasher.hamming(pHashA, pHashB)

        assertTrue(
            "Completely unrelated images must have large pHash distance (got $distP, expected >= 18)",
            distP >= 18
        )

        val dHashA = PerceptualHasher.dHashFromGray(PerceptualHasher.downsample(imgA, 32, 32, 9, 8))
        val dHashB = PerceptualHasher.dHashFromGray(PerceptualHasher.downsample(imgB, 32, 32, 9, 8))
        val distD = PerceptualHasher.hamming(dHashA, dHashB)
        assertTrue(
            "Completely unrelated images must have large dHash distance (got $distD, expected >= 18)",
            distD >= 18
        )
    }

    @Test
    fun testBlurScoreDistinguishesSharpAndBlurry() {
        val size = 64
        // Flat/blurry image (all uniform gray or very smooth)
        val flatImage = IntArray(size * size) { 128 }
        val flatBlur = PerceptualHasher.blurScoreFromGray(flatImage, size, size)
        assertEquals("Uniform image must have near zero blur score (variance)", 0.0, flatBlur, 0.001)

        // Sharp image: high frequency checkerboard edges
        val sharpImage = IntArray(size * size) { i ->
            val x = i % size
            val y = i / size
            if ((x / 4 + y / 4) % 2 == 0) 250 else 10
        }
        val sharpBlur = PerceptualHasher.blurScoreFromGray(sharpImage, size, size)

        assertTrue(
            "Sharp image should have high blur score (got $sharpBlur, expected > 1000)",
            sharpBlur > 1000.0
        )
    }

    @Test
    fun testBucketKeysSplits64BitHashInto8Bytes() {
        // Hash: 0x0123456789ABCDEF
        val hash = 0x0123456789ABCDEFL
        val keys = PerceptualHasher.bucketKeys(hash)

        assertEquals("Must produce exactly 8 chunks", 8, keys.size)
        assertEquals(0x01, keys[0])
        assertEquals(0x23, keys[1])
        assertEquals(0x45, keys[2])
        assertEquals(0x67, keys[3])
        assertEquals(0x89, keys[4])
        assertEquals(0xAB, keys[5])
        assertEquals(0xCD, keys[6])
        assertEquals(0xEF, keys[7])
    }

    @Test
    fun testRotatedImageDocumentedAsKnownLimit() {
        // Spec Section 10: "rotated is documented as a known limit"
        // 90-degree rotation changes spatial frequency and horizontal gradient directions,
        // so standard dHash and pHash produce a large distance.
        val img32x32 = generateSyntheticImage(32, 32, seed = 505L)
        val rotated32x32 = IntArray(32 * 32)
        for (y in 0 until 32) {
            for (x in 0 until 32) {
                // 90 degree clockwise rotation: (x, y) -> (31 - y, x)
                rotated32x32[x * 32 + (31 - y)] = img32x32[y * 32 + x]
            }
        }

        val pHashOrig = PerceptualHasher.pHashFromGray(img32x32)
        val pHashRotated = PerceptualHasher.pHashFromGray(rotated32x32)
        val dist = PerceptualHasher.hamming(pHashOrig, pHashRotated)

        assertTrue(
            "Known limit: 90-degree rotation produces large distance without pre-alignment (got $dist)",
            dist > 10
        )
    }
}
