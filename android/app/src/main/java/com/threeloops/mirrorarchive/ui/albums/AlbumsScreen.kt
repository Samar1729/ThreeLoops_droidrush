package com.threeloops.mirrorarchive.ui.albums

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.threeloops.mirrorarchive.contracts.AlbumSummary
import com.threeloops.mirrorarchive.contracts.AlbumType
import com.threeloops.mirrorarchive.contracts.ScanPhase
import com.threeloops.mirrorarchive.contracts.ScanState

@Composable
fun AlbumsScreen(
    viewModel: AlbumsViewModel,
    onAlbumClick: (AlbumType) -> Unit,
    onNavigateDuplicates: () -> Unit,
    onNavigateStorageSaver: () -> Unit,
    onNavigateSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val summaries by viewModel.albumSummaries.collectAsState()
    val scanState by viewModel.scanState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0C0C0E))
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(18.dp))

        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Library",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = "Smart Categorized Albums",
                    fontSize = 13.sp,
                    color = Color(0xFFA1A1AA)
                )
            }

            // Quick utility shortcuts (Duplicates & Storage Saver)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(
                    onClick = onNavigateDuplicates,
                    modifier = Modifier
                        .size(38.dp)
                        .background(Color(0xFF1C1C1E), CircleShape)
                        .border(1.dp, Color(0xFF2C2C2E), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ContentCopy,
                        contentDescription = "Duplicates",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = onNavigateStorageSaver,
                    modifier = Modifier
                        .size(38.dp)
                        .background(Color(0xFF1C1C1E), CircleShape)
                        .border(1.dp, Color(0xFF2C2C2E), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CleaningServices,
                        contentDescription = "Storage Saver",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = onNavigateSettings,
                    modifier = Modifier
                        .size(38.dp)
                        .background(Color(0xFF1C1C1E), CircleShape)
                        .border(1.dp, Color(0xFF2C2C2E), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = "Settings",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Indexing Status Pill if scanning
        if (scanState.phase == ScanPhase.SCANNING) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF18181B),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp)
                    .border(1.dp, Color(0xFF27272A), RoundedCornerShape(12.dp))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(Color(0xFF34C759), CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Analyzing Library",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }
                    Text(
                        text = "${scanState.done} / ${scanState.total}",
                        fontSize = 12.sp,
                        color = Color(0xFFA1A1AA)
                    )
                }
            }
        }

        // Albums Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 24.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(summaries, key = { it.type.name }) { summary ->
                AlbumCard(
                    summary = summary,
                    onClick = { onAlbumClick(summary.type) }
                )
            }
        }
    }
}

@Composable
fun AlbumCard(
    summary: AlbumSummary,
    onClick: () -> Unit
) {
    val albumIcon = when (summary.type) {
        AlbumType.RECEIPTS -> Icons.Outlined.Receipt
        AlbumType.SCREENSHOTS -> Icons.Outlined.Screenshot
        AlbumType.DOCUMENTS -> Icons.Outlined.Description
        AlbumType.FOOD -> Icons.Outlined.Restaurant
        AlbumType.PEOPLE -> Icons.Outlined.Person
    }

    val albumTitle = when (summary.type) {
        AlbumType.RECEIPTS -> "Receipts"
        AlbumType.SCREENSHOTS -> "Screenshots"
        AlbumType.DOCUMENTS -> "Documents"
        AlbumType.FOOD -> "Food"
        AlbumType.PEOPLE -> "People"
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF18181B)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF27272A), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .background(Color(0xFF121214))
            ) {
                if (summary.coverUri != null) {
                    AsyncImage(
                        model = summary.coverUri,
                        contentDescription = albumTitle,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color(0x99000000)),
                                    startY = 100f
                                )
                            )
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = albumIcon,
                            contentDescription = albumTitle,
                            tint = Color(0xFF52525B),
                            modifier = Modifier.size(38.dp)
                        )
                    }
                }

                // Category pill on top-left of card
                Box(
                    modifier = Modifier
                        .padding(10.dp)
                        .background(Color(0xCC000000), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = albumIcon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Text(
                    text = albumTitle,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Text(
                    text = "${summary.count} photos",
                    fontSize = 12.sp,
                    color = Color(0xFFA1A1AA)
                )
            }
        }
    }
}
