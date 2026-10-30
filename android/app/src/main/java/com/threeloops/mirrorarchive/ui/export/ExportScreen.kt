package com.threeloops.mirrorarchive.ui.export

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.threeloops.mirrorarchive.contracts.ImageItem
import com.threeloops.mirrorarchive.contracts.displayName
import com.threeloops.mirrorarchive.data.export.ExportOperation
import com.threeloops.mirrorarchive.data.export.ExportViewModel
import com.threeloops.mirrorarchive.ui.theme.GlassScaffoldBackground
import com.threeloops.mirrorarchive.ui.theme.TextMuted
import com.threeloops.mirrorarchive.ui.theme.TextPrimary
import com.threeloops.mirrorarchive.ui.theme.TextSecondary
import com.threeloops.mirrorarchive.ui.theme.glass

@Composable
fun ExportScreen(
    initialIds: List<Long>,
    onBackClick: () -> Unit,
    viewModel: ExportViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val images by viewModel.images.collectAsStateWithLifecycle()
    val selectedIds by viewModel.selectedIds.collectAsStateWithLifecycle()
    val operation by viewModel.operation.collectAsStateWithLifecycle()
    val progress by viewModel.progress.collectAsStateWithLifecycle()
    val collage by viewModel.collage.collectAsStateWithLifecycle()
    var archiveName by rememberSaveable { mutableStateOf("MirrorArchive_Export") }
    var grid by rememberSaveable { mutableFloatStateOf(2f) }

    LaunchedEffect(images, initialIds) {
        if (images.isNotEmpty()) {
            viewModel.initializeSelection(initialIds)
        }
    }

    GlassScaffoldBackground {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                androidx.compose.material3.IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .size(40.dp)
                        .glass(
                            shape = RoundedCornerShape(50),
                            backgroundColor = Color.White.copy(alpha = 0.08f),
                            topBorderAlpha = 0.35f
                        )
                ) {
                    Icon(
                        Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }
                Column(Modifier.padding(start = 14.dp)) {
                    Text(
                        "Export photos",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        "${selectedIds.size} selected · ${images.size} indexed",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Choose photos", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                Row {
                    TextButton(onClick = viewModel::selectAll) { Text("Select all") }
                    TextButton(onClick = viewModel::clearSelection) { Text("Clear") }
                }
            }

            if (images.isEmpty()) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No indexed photos available to export.", color = TextMuted)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(images, key = ImageItem::id) { image ->
                        ExportPhoto(
                            image = image,
                            selected = image.id in selectedIds,
                            onClick = { viewModel.toggleSelection(image.id) }
                        )
                    }
                }
            }

            if (progress != null) {
                Column(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                    LinearProgressIndicator(
                        progress = {
                            val current = progress
                            if (current == null || current.totalFiles == 0) 0f
                            else current.completedFiles.toFloat() / current.totalFiles
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        "Exporting ${progress?.completedFiles ?: 0}/${progress?.totalFiles ?: 0}: ${progress?.currentFile.orEmpty()}",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }

            TextField(
                value = archiveName,
                onValueChange = { archiveName = it },
                label = { Text("ZIP file name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedLabelColor = TextSecondary,
                    unfocusedLabelColor = TextMuted,
                    focusedContainerColor = Color.White.copy(alpha = 0.06f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.04f)
                )
            )
            Button(
                onClick = { viewModel.exportZip(archiveName) },
                enabled = selectedIds.isNotEmpty() && operation !is ExportOperation.Working,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                )
            ) {
                if (operation is ExportOperation.Working) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = Color.Black
                    )
                    Spacer(Modifier.size(8.dp))
                } else {
                    Icon(Icons.Outlined.Archive, contentDescription = null)
                    Spacer(Modifier.size(8.dp))
                }
                Text("Export ZIP to Downloads")
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Collage layout", color = TextSecondary, fontSize = 13.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    (2..3).forEach { size ->
                        val selected = grid.toInt() == size
                        OutlinedButton(
                            onClick = { grid = size.toFloat() },
                            border = BorderStroke(
                                1.dp,
                                if (selected) Color.White else Color.White.copy(alpha = 0.25f)
                            ),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp)
                        ) {
                            Text(
                                "${size}×$size",
                                color = if (selected) TextPrimary else TextMuted
                            )
                        }
                    }
                }
            }
            Button(
                onClick = { viewModel.makeCollage(grid.toInt()) },
                enabled = selectedIds.size >= grid.toInt() * grid.toInt() &&
                    operation !is ExportOperation.Working,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White.copy(alpha = 0.12f),
                    contentColor = TextPrimary
                )
            ) {
                Text(
                    "Create ${grid.toInt()}×${grid.toInt()} collage " +
                        "(${grid.toInt() * grid.toInt()} photos required)"
                )
            }
            collage?.let { bitmap ->
                androidx.compose.foundation.Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Collage preview",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxWidth().height(160.dp).padding(vertical = 6.dp)
                )
                OutlinedButton(
                    onClick = viewModel::saveCollage,
                    enabled = operation !is ExportOperation.Working,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save JPEG to Pictures/MirrorArchive", color = TextPrimary)
                }
            }
            when (val result = operation) {
                is ExportOperation.Completed -> Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.Check, contentDescription = null, tint = Color(0xFF10B981))
                    Text(
                        result.message,
                        modifier = Modifier.weight(1f).padding(start = 8.dp),
                        color = Color(0xFF10B981),
                        fontSize = 12.sp
                    )
                    TextButton(onClick = viewModel::dismissResult) { Text("Dismiss") }
                }
                is ExportOperation.Failed -> Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        result.message,
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.error
                    )
                    TextButton(onClick = viewModel::dismissResult) { Text("Dismiss") }
                }
                else -> Unit
            }
        }
    }
}

@Composable
private fun ExportPhoto(
    image: ImageItem,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF18181B))
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = image.uri,
            contentDescription = image.displayName,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(6.dp)
                .size(24.dp)
                .clip(RoundedCornerShape(50))
                .background(if (selected) Color.White else Color.Black.copy(alpha = 0.6f)),
            contentAlignment = Alignment.Center
        ) {
            if (selected) {
                Icon(Icons.Outlined.Check, contentDescription = "Selected", tint = Color.Black)
            }
        }
    }
}
