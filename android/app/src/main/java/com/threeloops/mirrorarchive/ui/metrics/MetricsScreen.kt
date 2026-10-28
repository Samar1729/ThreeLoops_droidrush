package com.threeloops.mirrorarchive.ui.metrics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.threeloops.mirrorarchive.metrics.MetricsSnapshot
import java.util.Locale

@Composable
fun MetricsScreen(
    onBackClick: () -> Unit,
    viewModel: MetricsViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    Column(
        modifier = modifier.fillMaxSize().background(Color(0xFF0C0C0E))
            .verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text("Performance metrics", style = MaterialTheme.typography.headlineSmall, color = Color.White)
        }
        when (val current = state) {
            MetricsUiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            is MetricsUiState.Error -> Text(current.message, color = MaterialTheme.colorScheme.error)
            is MetricsUiState.Data -> MetricsCards(current.snapshot)
        }
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun MetricsCards(metrics: MetricsSnapshot) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        MetricsCard("Index and performance") {
            MetricRow("Images indexed", metrics.imagesIndexed.takeIf { it > 0 }?.toString() ?: "—")
            MetricRow("Average processing time", metrics.avgMsPerImage.formatMs())
            MetricRow("Throughput", metrics.throughputImgPerSec.formatRate())
            MetricRow("Database size", metrics.dbSizeMb.formatMb())
            MetricRow("Peak heap", metrics.peakHeapMb.formatMb())
            MetricRow("Network permission", metrics.networkPermission)
        }
        MetricsCard("Pipeline time by stage") {
            val maxMs = metrics.perStageMs.values.maxOrNull() ?: 0.0
            metrics.perStageMs.forEach { (stage, ms) ->
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    MetricRow(stage, ms.formatMs())
                    LinearProgressIndicator(
                        progress = { if (maxMs > 0.0) (ms / maxMs).toFloat().coerceIn(0f, 1f) else 0f },
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = Color(0xFF303036)
                    )
                }
            }
        }
        MetricsCard("Duplicate analysis") {
            MetricRow("Naive comparisons", metrics.comparisonsNaive.takeIf { it > 0 }?.toString() ?: "—")
            MetricRow("Bucketed comparisons", metrics.comparisonsBucketed.takeIf { it > 0 }?.toString() ?: "—")
            MetricRow("Reclaimable storage", metrics.reclaimableMb.formatMb())
        }
        MetricsCard("Search") {
            MetricRow("Search latency (p50)", metrics.searchLatencyMsP50.formatMs())
        }
    }
}

@Composable
private fun MetricsCard(title: String, content: @Composable () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            content()
        }
    }
}

@Composable
private fun MetricRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Color.LightGray)
        Text(value, color = Color.White)
    }
}

private fun Double.formatMs(): String = if (this <= 0.0) "—" else String.format(Locale.getDefault(), "%.2f ms", this)
private fun Double.formatRate(): String = if (this <= 0.0) "—" else String.format(Locale.getDefault(), "%.2f img/s", this)
private fun Double.formatMb(): String = if (this <= 0.0) "—" else String.format(Locale.getDefault(), "%.2f MB", this)
