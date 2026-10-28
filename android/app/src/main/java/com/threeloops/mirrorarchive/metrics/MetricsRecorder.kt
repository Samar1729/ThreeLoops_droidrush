package com.threeloops.mirrorarchive.metrics

import com.threeloops.mirrorarchive.data.db.AppDatabase
import com.threeloops.mirrorarchive.data.db.PipelineStatEntity
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject
import javax.inject.Singleton

enum class Stage { DECODE, LABEL, FACE, OCR, HASH, DB }

data class StageMetric(val totalNanos: Long, val count: Long)

data class RunMetricSnapshot(val stages: Map<Stage, StageMetric>, val images: Long)

@Singleton
class MetricsRecorder @Inject constructor(
    private val database: AppDatabase,
    private val memoryProbe: MemoryProbe
) {
    private class RunMetrics(val startedAt: Long) {
        val totals = ConcurrentHashMap<Stage, AtomicLong>().apply {
            Stage.entries.forEach { put(it, AtomicLong()) }
        }
        val counts = ConcurrentHashMap<Stage, AtomicLong>().apply {
            Stage.entries.forEach { put(it, AtomicLong()) }
        }
        val images = AtomicLong(0L)
        val finishMutex = Mutex()
    }

    private val nextId = AtomicLong(System.currentTimeMillis())
    private val runs = ConcurrentHashMap<Long, RunMetrics>()
    private val activeRun = AtomicReference<Long?>(null)

    fun startRun(): Long {
        val runId = nextId.incrementAndGet()
        runs[runId] = RunMetrics(System.currentTimeMillis())
        activeRun.set(runId)
        memoryProbe.resetPeak()
        memoryProbe.sample()
        return runId
    }

    inline fun <T> time(stage: Stage, block: () -> T): T {
        val started = System.nanoTime()
        try {
            return block()
        } finally {
            record(stage, System.nanoTime() - started)
        }
    }

    suspend inline fun <T> timeSuspend(stage: Stage, crossinline block: suspend () -> T): T {
        val started = System.nanoTime()
        try {
            return block()
        } finally {
            record(stage, System.nanoTime() - started)
        }
    }

    fun recordImage(count: Long = 1L) {
        require(count >= 0L)
        activeRun.get()?.let { runs[it]?.images?.addAndGet(count) }
    }

    fun snapshot(runId: Long): RunMetricSnapshot? = runs[runId]?.let { run ->
        RunMetricSnapshot(
            stages = Stage.entries.associateWith { stage ->
                StageMetric(run.totals.getValue(stage).get(), run.counts.getValue(stage).get())
            },
            images = run.images.get()
        )
    }

    suspend fun finishRun(runId: Long) {
        val run = checkNotNull(runs[runId]) { "Unknown metrics run: $runId" }
        run.finishMutex.withLock {
            memoryProbe.sample()
            fun stageMs(stage: Stage) = run.totals.getValue(stage).get() / NANOS_PER_MILLI
            database.pipelineStatDao().insert(
                PipelineStatEntity(
                    runId = runId.toString(),
                    startedAt = run.startedAt,
                    images = run.images.get(),
                    decodeMs = stageMs(Stage.DECODE),
                    labelMs = stageMs(Stage.LABEL),
                    hashMs = stageMs(Stage.HASH),
                    ocrMs = stageMs(Stage.OCR),
                    faceMs = stageMs(Stage.FACE),
                    dbMs = stageMs(Stage.DB),
                    peakHeapMb = memoryProbe.peakMb().toDouble()
                )
            )
            runs.remove(runId, run)
            activeRun.compareAndSet(runId, null)
        }
    }

    @PublishedApi
    internal fun record(stage: Stage, elapsedNanos: Long) {
        activeRun.get()?.let { id ->
            runs[id]?.let { run ->
                run.totals.getValue(stage).addAndGet(elapsedNanos.coerceAtLeast(0L))
                run.counts.getValue(stage).incrementAndGet()
                memoryProbe.sample()
            }
        }
    }

    private companion object { const val NANOS_PER_MILLI = 1_000_000L }
}
