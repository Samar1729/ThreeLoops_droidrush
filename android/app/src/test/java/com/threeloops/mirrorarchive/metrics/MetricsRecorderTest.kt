package com.threeloops.mirrorarchive.metrics

import com.threeloops.mirrorarchive.data.db.AppDatabase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito

class MetricsRecorderTest {
    private fun recorder() = MetricsRecorder(
        Mockito.mock(AppDatabase::class.java),
        Mockito.mock(MemoryProbe::class.java)
    )

    @Test
    fun stageTotalsAndCountsAccumulateForCurrentRun() {
        val recorder = recorder()
        val runId = recorder.startRun()

        recorder.time(Stage.DECODE) { 10 }
        recorder.time(Stage.DECODE) { "decoded" }
        recorder.time(Stage.LABEL) { true }
        recorder.recordImage(3)

        val snapshot = recorder.snapshot(runId)!!
        assertEquals(2L, snapshot.stages.getValue(Stage.DECODE).count)
        assertEquals(1L, snapshot.stages.getValue(Stage.LABEL).count)
        assertEquals(0L, snapshot.stages.getValue(Stage.FACE).count)
        assertEquals(3L, snapshot.images)
        assertTrue(snapshot.stages.getValue(Stage.DECODE).totalNanos >= 0L)
        assertTrue(snapshot.stages.getValue(Stage.LABEL).totalNanos >= 0L)
    }

    @Test
    fun failedBlockStillRecordsStageTiming() {
        val recorder = recorder()
        val runId = recorder.startRun()

        try {
            recorder.time(Stage.HASH) { error("expected failure") }
        } catch (expected: IllegalStateException) {
            assertEquals("expected failure", expected.message)
        }

        val metrics = recorder.snapshot(runId)!!.stages.getValue(Stage.HASH)
        assertEquals(1L, metrics.count)
        assertTrue(metrics.totalNanos >= 0L)
    }
}
