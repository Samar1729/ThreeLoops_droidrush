package com.threeloops.mirrorarchive.metrics

import com.threeloops.mirrorarchive.data.db.AppDatabase
import com.threeloops.mirrorarchive.settings.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

data class MetricsSnapshot(
    val imagesIndexed: Long,
    val avgMsPerImage: Double,
    val perStageMs: Map<String, Double>,
    val throughputImgPerSec: Double,
    val comparisonsNaive: Long,
    val comparisonsBucketed: Long,
    val reclaimableMb: Double,
    val searchLatencyMsP50: Double,
    val peakHeapMb: Double,
    val dbSizeMb: Double,
    val networkPermission: String
)

@Singleton
class MetricsRepository @Inject constructor(
    private val database: AppDatabase,
    private val settingsRepository: SettingsRepository,
    private val memoryProbe: MemoryProbe,
    private val manifestAudit: ManifestAudit
) {
    private data class IndexedRun(val imageCount: Long, val stat: com.threeloops.mirrorarchive.data.db.PipelineStatEntity?)

    fun snapshot(): Flow<MetricsSnapshot> {
        val indexedRun = combine(
            database.imageDao().count(),
            database.pipelineStatDao().observeLatest()
        ) { imageCount, stat -> IndexedRun(imageCount.toLong(), stat) }

        return combine(indexedRun, MetricsInbox.snapshot) { run, inbox ->
            val stat = run.stat
            val stages = linkedMapOf(
                "DECODE" to (stat?.decodeMs ?: 0L).toDouble(),
                "LABEL" to (stat?.labelMs ?: 0L).toDouble(),
                "FACE" to (stat?.faceMs ?: 0L).toDouble(),
                "OCR" to (stat?.ocrMs ?: 0L).toDouble(),
                "HASH" to (stat?.hashMs ?: 0L).toDouble(),
                "DB" to (stat?.dbMs ?: 0L).toDouble()
            )
            val totalMs = stages.values.sum()
            val imageTotal = stat?.images ?: 0L
            val currentMemoryMb = memoryProbe.sample().let { it.javaHeapMb + it.nativeHeapMb }.toDouble()
            MetricsSnapshot(
                imagesIndexed = run.imageCount,
                avgMsPerImage = if (imageTotal > 0L) totalMs / imageTotal else 0.0,
                perStageMs = stages,
                throughputImgPerSec = if (totalMs > 0.0 && imageTotal > 0L) imageTotal * 1_000.0 / totalMs else 0.0,
                comparisonsNaive = inbox.comparisonsNaive,
                comparisonsBucketed = inbox.comparisonsBucketed,
                reclaimableMb = inbox.reclaimableBytes / (1024.0 * 1024.0),
                searchLatencyMsP50 = inbox.searchLatencyMsP50,
                peakHeapMb = maxOf(currentMemoryMb, stat?.peakHeapMb ?: 0.0, memoryProbe.peakMb().toDouble()),
                dbSizeMb = settingsRepository.dbSizeBytes() / (1024.0 * 1024.0),
                networkPermission = if (manifestAudit.hasInternetPermission()) "present" else "none"
            )
        }
    }
}
