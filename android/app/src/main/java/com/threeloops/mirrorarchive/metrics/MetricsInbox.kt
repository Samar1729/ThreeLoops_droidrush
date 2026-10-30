package com.threeloops.mirrorarchive.metrics

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

data class InboxSnapshot(
    val comparisonsNaive: Long = 0,
    val comparisonsBucketed: Long = 0,
    val reclaimableBytes: Long = 0,
    val searchLatenciesMs: List<Long> = emptyList(),
    val hashThroughputImgPerSec: Double = 0.0
) {
    val searchLatencyMsP50: Double
        get() {
            if (searchLatenciesMs.isEmpty()) return 0.0
            val sorted = searchLatenciesMs.sorted()
            val middle = sorted.size / 2
            return if (sorted.size % 2 == 0) {
                (sorted[middle - 1].toDouble() + sorted[middle].toDouble()) / 2.0
            } else sorted[middle].toDouble()
        }
}

object MetricsInbox {
    private val mutableSnapshot = MutableStateFlow(InboxSnapshot())
    val snapshot: StateFlow<InboxSnapshot> = mutableSnapshot

    fun setComparisonStats(naive: Long, bucketed: Long) {
        mutableSnapshot.update {
            it.copy(comparisonsNaive = naive.coerceAtLeast(0L), comparisonsBucketed = bucketed.coerceAtLeast(0L))
        }
    }

    fun setReclaimableBytes(bytes: Long) {
        mutableSnapshot.update { it.copy(reclaimableBytes = bytes.coerceAtLeast(0L)) }
    }

    fun recordSearchLatency(ms: Long) {
        if (ms < 0L) return
        mutableSnapshot.update { it.copy(searchLatenciesMs = (it.searchLatenciesMs + ms).takeLast(MAX_LATENCIES)) }
    }

    fun setHashThroughput(throughput: Double) {
        if (throughput < 0.0) return
        mutableSnapshot.update { it.copy(hashThroughputImgPerSec = throughput) }
    }

    private const val MAX_LATENCIES = 1_000
}
