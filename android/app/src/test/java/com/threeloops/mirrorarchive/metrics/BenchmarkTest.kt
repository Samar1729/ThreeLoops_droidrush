package com.threeloops.mirrorarchive.metrics

import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * JVM unit test for [Benchmark.hashThroughput].
 * Owner: Samar (Feature S-7)
 */
class BenchmarkTest {

    @Test
    fun hashThroughputReturnsPositiveNumber() {
        val throughput = Benchmark.hashThroughput(30)
        assertTrue("Throughput must be positive and greater than 0, got $throughput img/s", throughput > 0.0)
    }

    @Test
    fun benchmarkSyntheticGrayExecutesReliably() {
        val throughput = Benchmark.benchmarkSyntheticGray(50)
        assertTrue("Synthetic grayscale benchmark must return positive rate, got $throughput img/s", throughput > 0.0)
    }
}

