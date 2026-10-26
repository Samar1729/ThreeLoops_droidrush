package com.threeloops.mirrorarchive.ui.albums

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.threeloops.mirrorarchive.R
import com.threeloops.mirrorarchive.contracts.AlbumSummary
import com.threeloops.mirrorarchive.contracts.AlbumType
import com.threeloops.mirrorarchive.contracts.ScanPhase
import com.threeloops.mirrorarchive.contracts.ScanState
import com.threeloops.mirrorarchive.ui.theme.GlassScaffoldBackground
import com.threeloops.mirrorarchive.ui.theme.glass

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

    GlassScaffoldBackground(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(18.dp))

            // Glassmorphic Top Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Vault Logo Emblem
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .glass(
                                shape = RoundedCornerShape(14.dp),
                                backgroundColor = Color.White.copy(alpha = 0.08f),
                                borderWidth = 1.dp,
                                topBorderAlpha = 0.40f,
                                bottomBorderAlpha = 0.08f
                            )
                            .padding(6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.vault_logo),
                            contentDescription = "Vault",
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Vault",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = (-0.5).sp
                        )
                        Text(
                            text = "Encrypted Smart Albums",
                            fontSize = 12.sp,
                            color = Color(0xFFA1A1AA)
                        )
                    }
                }

                // Quick utility shortcuts inside Frosted Glass Discs
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .glass(
                                shape = CircleShape,
                                backgroundColor = Color.White.copy(alpha = 0.08f),
                                borderWidth = 1.dp,
                                topBorderAlpha = 0.35f,
                                bottomBorderAlpha = 0.06f
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { onNavigateDuplicates() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ContentCopy,
                            contentDescription = "Duplicates",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .glass(
                                shape = CircleShape,
                                backgroundColor = Color.White.copy(alpha = 0.08f),
                                borderWidth = 1.dp,
                                topBorderAlpha = 0.35f,
                                bottomBorderAlpha = 0.06f
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { onNavigateStorageSaver() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CleaningServices,
                            contentDescription = "Storage Saver",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .glass(
                                shape = CircleShape,
                                backgroundColor = Color.White.copy(alpha = 0.08f),
                                borderWidth = 1.dp,
                                topBorderAlpha = 0.35f,
                                bottomBorderAlpha = 0.06f
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { onNavigateSettings() },
                        contentAlignment = Alignment.Center
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

            Spacer(modifier = Modifier.height(16.dp))

            // Indexing Status Pill if scanning
            if (scanState.phase == ScanPhase.SCANNING) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp)
                        .glass(
                            shape = RoundedCornerShape(16.dp),
                            backgroundColor = Color.White.copy(alpha = 0.08f),
                            borderWidth = 1.dp,
                            topBorderAlpha = 0.30f,
                            bottomBorderAlpha = 0.05f
                        )
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(Color.White, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Indexing On-Device",
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

            // Albums Grid: Glassmorphic cards
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 100.dp),
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

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .glass(
                shape = RoundedCornerShape(22.dp),
                backgroundColor = Color.White.copy(alpha = 0.06f),
                borderWidth = 1.dp,
                topBorderAlpha = 0.30f,
                bottomBorderAlpha = 0.05f
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() }
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // Photo Canvas with rounded inner corners
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF101014))
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
                                    colors = listOf(Color.Transparent, Color(0xB3000000)),
                                    startY = 80f
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

                // Frosted Glass Icon Badge on Top-Left
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .glass(
                            shape = CircleShape,
                            backgroundColor = Color(0xB3000000),
                            borderWidth = 1.dp,
                            topBorderAlpha = 0.40f,
                            bottomBorderAlpha = 0.10f
                        )
                        .padding(6.dp)
                ) {
                    Icon(
                        imageVector = albumIcon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp)
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
                    color = Color(0xFFA1A1AA),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}
