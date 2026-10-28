package com.threeloops.mirrorarchive.ui.settings

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.threeloops.mirrorarchive.contracts.ScanPhase
import com.threeloops.mirrorarchive.settings.Settings
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showClearConfirmation by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text("Settings", style = MaterialTheme.typography.headlineSmall, color = Color.White)
        }
        when (val current = state) {
            SettingsUiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            is SettingsUiState.Error -> Text(current.message, color = MaterialTheme.colorScheme.error)
            is SettingsUiState.Data -> {
                ThresholdsSection(current.settings, viewModel::update, viewModel::resetDefaults)
                Spacer(Modifier.height(14.dp))
                IndexSection(
                    indexedCount = current.indexedCount,
                    dbSizeBytes = current.dbSizeBytes,
                    phase = current.scanProgress.phase,
                    onRescan = viewModel::rescanNewPhotos,
                    onFullRescan = viewModel::fullRescan,
                    onClear = { showClearConfirmation = true }
                )
                Spacer(Modifier.height(14.dp))
                AboutSection(current.appInfo.versionName, current.appInfo.libraries, current.appInfo.privacyStatement)
                Spacer(Modifier.height(24.dp))
            }
        }
    }

    if (showClearConfirmation) {
        AlertDialog(
            onDismissRequest = { showClearConfirmation = false },
            title = { Text("Clear indexed data?") },
            text = { Text("This removes the local photo index and scan checkpoint. Your photos on the device are not deleted.") },
            confirmButton = {
                TextButton(onClick = {
                    showClearConfirmation = false
                    viewModel.clearIndex()
                }) { Text("Clear index") }
            },
            dismissButton = { TextButton(onClick = { showClearConfirmation = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun ThresholdsSection(settings: Settings, update: (Settings) -> Unit, reset: () -> Unit) {
    SectionCard("Thresholds") {
        IntegerSlider("dHash maximum distance", settings.dHashMax, 0..32) { update(settings.copy(dHashMax = it)) }
        IntegerSlider("pHash maximum distance", settings.pHashMax, 0..32) { update(settings.copy(pHashMax = it)) }
        FloatSlider("Blur threshold", settings.blurThreshold.toFloat(), 1f..5000f, "%.0f") {
            update(settings.copy(blurThreshold = it.toDouble()))
        }
        FloatSlider("Label confidence", settings.labelConfidence, 0.1f..0.99f, "%.2f") {
            update(settings.copy(labelConfidence = it))
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()) {
            Text("Ignore scan constraints", color = Color.White)
            Switch(checked = settings.scanIgnoreConstraints, onCheckedChange = {
                update(settings.copy(scanIgnoreConstraints = it))
            })
        }
        OutlinedButton(onClick = reset, modifier = Modifier.fillMaxWidth()) { Text("Reset to defaults") }
    }
}

@Composable
private fun IntegerSlider(label: String, value: Int, range: IntRange, onValue: (Int) -> Unit) {
    Text("$label: $value", color = Color.White)
    Slider(value = value.toFloat(), onValueChange = { onValue(it.toInt()) }, valueRange = range.first.toFloat()..range.last.toFloat(), steps = range.last - range.first - 1)
}

@Composable
private fun FloatSlider(label: String, value: Float, range: ClosedFloatingPointRange<Float>, format: String, onValue: (Float) -> Unit) {
    Text("$label: ${String.format(Locale.getDefault(), format, value)}", color = Color.White)
    Slider(value = value.coerceIn(range), onValueChange = onValue, valueRange = range)
}

@Composable
private fun IndexSection(
    indexedCount: Int,
    dbSizeBytes: Long,
    phase: ScanPhase,
    onRescan: () -> Unit,
    onFullRescan: () -> Unit,
    onClear: () -> Unit
) {
    SectionCard("Index") {
        InfoRow("Indexed images", indexedCount.toString())
        InfoRow("Database size", String.format(Locale.getDefault(), "%.2f MB", dbSizeBytes / (1024.0 * 1024.0)))
        InfoRow("Last scan state", phase.name.lowercase().replaceFirstChar { it.uppercase() })
        Button(onClick = onRescan, modifier = Modifier.fillMaxWidth()) { Text("Rescan new photos") }
        OutlinedButton(onClick = onFullRescan, modifier = Modifier.fillMaxWidth()) { Text("Full rescan") }
        TextButton(onClick = onClear, modifier = Modifier.fillMaxWidth()) { Text("Clear index") }
    }
}

@Composable
private fun AboutSection(versionName: String, libraries: List<String>, privacy: String) {
    SectionCard("Privacy / About") {
        InfoRow("Version", versionName)
        InfoRow("Network permission", "None")
        InfoRow("Database encrypted", "Pending")
        Text("Libraries and disclosures", color = Color.White)
        libraries.forEach { Text("• $it", color = Color.LightGray, style = MaterialTheme.typography.bodySmall) }
        Text(privacy, color = Color.LightGray, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            content()
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Color.LightGray)
        Text(value, color = Color.White)
    }
}
