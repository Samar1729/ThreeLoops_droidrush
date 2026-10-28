package com.threeloops.mirrorarchive.metrics

import android.os.Debug
import javax.inject.Inject
import javax.inject.Singleton
import java.util.concurrent.atomic.AtomicLong

data class MemorySample(val javaHeapMb: Long, val nativeHeapMb: Long)

@Singleton
class MemoryProbe @Inject constructor() {
    private val peakMb = AtomicLong(0L)

    fun sample(): MemorySample {
        val runtime = Runtime.getRuntime()
        val javaMb = (runtime.totalMemory() - runtime.freeMemory()).coerceAtLeast(0L) / BYTES_PER_MB
        val nativeMb = Debug.getNativeHeapAllocatedSize().coerceAtLeast(0L) / BYTES_PER_MB
        peakMb.accumulateAndGet(javaMb + nativeMb, ::maxOf)
        return MemorySample(javaMb, nativeMb)
    }

    fun peakMb(): Long = peakMb.get()

    internal fun resetPeak() { peakMb.set(0L) }

    private companion object { const val BYTES_PER_MB = 1024L * 1024L }
}
