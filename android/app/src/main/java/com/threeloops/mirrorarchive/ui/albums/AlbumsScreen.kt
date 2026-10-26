package com.threeloops.mirrorarchive.ui.albums

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.threeloops.mirrorarchive.contracts.AlbumSummary
import com.threeloops.mirrorarchive.contracts.AlbumType
import com.threeloops.mirrorarchive.data.demo.DemoPhoto
import com.threeloops.mirrorarchive.data.demo.DemoSeedData
import com.threeloops.mirrorarchive.ui.components.DemoPhotoCanvas
import com.threeloops.mirrorarchive.ui.sandbox.NitiSandboxDialog
import com.threeloops.mirrorarchive.ui.theme.AmbientMeshBox
import com.threeloops.mirrorarchive.ui.theme.glass
import com.threeloops.mirrorarchive.ui.xray.DrishtiXRayBottomSheet

data class SmartAlbumCarouselItem(
    val type: AlbumType,
    val title: String,
    val count: Int,
    val icon: ImageVector
)

@Composable
fun AlbumsScreen(
    viewModel: AlbumsViewModel,
    onAlbumClick: (AlbumType) -> Unit,
    onNavigateDuplicates: () -> Unit,
    onNavigateStorageSaver: () -> Unit,
    onNavigateSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showNitiSandbox by remember { mutableStateOf(false) }
    var selectedAlbumFilter by remember { mutableStateOf<AlbumType?>(null) }
    var inspectingPhoto by remember { mutableStateOf<DemoPhoto?>(null) }

    val carouselItems = remember {
        listOf(
            SmartAlbumCarouselItem(AlbumType.SCREENSHOTS, "Screenshots", 18, Icons.Outlined.Screenshot),
            SmartAlbumCarouselItem(AlbumType.RECEIPTS, "Receipts", 12, Icons.Outlined.Receipt),
            SmartAlbumCarouselItem(AlbumType.DOCUMENTS, "Documents", 9, Icons.Outlined.Description),
            SmartAlbumCarouselItem(AlbumType.PEOPLE, "People", 24, Icons.Outlined.Person),
            SmartAlbumCarouselItem(AlbumType.FOOD, "Food", 15, Icons.Outlined.Restaurant)
        )
    }

    val displayedPhotos = remember(selectedAlbumFilter) {
        if (selectedAlbumFilter == null) {
            DemoSeedData.photos
        } else {
            DemoSeedData.photos.filter { it.albumType == selectedAlbumFilter }
        }
    }

    AmbientMeshBox(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(18.dp))

            // ========================================================
            // Top Header: Title + Sleek Live Telemetry Pill + Niti Disc
            // ========================================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Vault",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "On-Device Neural Archive",
                        fontSize = 11.sp,
                        color = Color(0xFFA1A1AA)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Sleek Live Pipeline Telemetry Pill: ● 1,248 Indexed • 42 img/s • INTERNET: NONE
                    Box(
                        modifier = Modifier
                            .glass(
                                shape = RoundedCornerShape(20.dp),
                                backgroundColor = Color(0xDD121216),
                                borderWidth = 1.dp,
                                topBorderAlpha = 0.35f,
                                bottomBorderAlpha = 0.08f
                            )
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .background(Color(0xFF10B981), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "1,248 • 42 img/s • ",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFFE4E4E7)
                            )
                            Text(
                                text = "INTERNET: NONE",
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF10B981)
                            )
                        }
                    }

                    // Niti Sandbox Tuning Disc
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .glass(
                                shape = CircleShape,
                                backgroundColor = Color(0x3310B981),
                                borderWidth = 1.dp,
                                topBorderAlpha = 0.45f,
                                bottomBorderAlpha = 0.10f
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { showNitiSandbox = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Tune,
                            contentDescription = "Niti Sandbox",
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // ========================================================
            // Horizontal Smart Album Carousel (True Glass Cards)
            // Brush: White 0.10f to 0.03f with 1.dp border 0.25f to 0.04f
            // ========================================================
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(horizontal = 2.dp, vertical = 4.dp)
            ) {
                items(carouselItems, key = { it.type.name }) { item ->
                    val isSelected = selectedAlbumFilter == item.type

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        if (isSelected) Color(0x3310B981) else Color.White.copy(alpha = 0.10f),
                                        if (isSelected) Color(0x1810B981) else Color.White.copy(alpha = 0.03f)
                                    )
                                )
                            )
                            .border(
                                width = 1.dp,
                                brush = Brush.verticalGradient(
                                    listOf(
                                        if (isSelected) Color(0xFF10B981) else Color.White.copy(alpha = 0.25f),
                                        if (isSelected) Color(0x3310B981) else Color.White.copy(alpha = 0.04f)
                                    )
                                ),
                                shape = RoundedCornerShape(18.dp)
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                selectedAlbumFilter = if (isSelected) null else item.type
                            }
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title,
                                tint = if (isSelected) Color(0xFF10B981) else Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = item.title,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "(${item.count})",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = if (isSelected) Color(0xFF10B981) else Color(0xFFA1A1AA)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Filter indicator pill if active
            if (selectedAlbumFilter != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Filtered by: ${selectedAlbumFilter?.name}",
                        fontSize = 12.sp,
                        color = Color(0xFF10B981),
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Show All",
                        fontSize = 11.sp,
                        color = Color.White,
                        modifier = Modifier
                            .clickable { selectedAlbumFilter = null }
                            .padding(4.dp)
                    )
                }
            }

            // ========================================================
            // 2-Column Staggered Photo Grid of Demo Photos
            // Bottom Scrim + ML Label Chip + Tap to open Drishti X-Ray
            // ========================================================
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 110.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(displayedPhotos, key = { it.id }) { photo ->
                    PhotoGridCard(
                        photo = photo,
                        onClick = { inspectingPhoto = photo }
                    )
                }
            }
        }

        // Drishti X-Ray Inspector Bottom Sheet
        if (inspectingPhoto != null) {
            DrishtiXRayBottomSheet(
                photo = inspectingPhoto,
                onDismiss = { inspectingPhoto = null }
            )
        }

        // Niti Sandbox Dialog
        if (showNitiSandbox) {
            NitiSandboxDialog(onDismiss = { showNitiSandbox = false })
        }
    }
}

@Composable
fun PhotoGridCard(
    photo: DemoPhoto,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(175.dp)
            .clip(RoundedCornerShape(18.dp))
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(Color.White.copy(alpha = 0.22f), Color.White.copy(alpha = 0.04f))
                ),
                shape = RoundedCornerShape(18.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() }
    ) {
        // High-polish procedural Canvas visual
        DemoPhotoCanvas(
            photo = photo,
            modifier = Modifier.fillMaxSize()
        )

        // Bottom Gradient Scrim
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f)),
                        startY = 180f
                    )
                )
        )

        // Top-Right X-Ray Trigger Pill
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
                .glass(
                    shape = CircleShape,
                    backgroundColor = Color.Black.copy(alpha = 0.40f),
                    borderWidth = 1.dp,
                    topBorderAlpha = 0.35f
                )
                .padding(5.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Visibility,
                contentDescription = "Inspect ML",
                tint = Color.White,
                modifier = Modifier.size(11.dp)
            )
        }

        // Bottom Left: Primary ML Label Chip (e.g. "receipt 0.96")
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(8.dp)
                .glass(
                    shape = RoundedCornerShape(8.dp),
                    backgroundColor = Color(0xD0101014),
                    borderWidth = 1.dp,
                    topBorderAlpha = 0.35f,
                    bottomBorderAlpha = 0.08f
                )
                .padding(horizontal = 7.dp, vertical = 3.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = photo.primaryLabel,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${photo.primaryConfidence}",
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF10B981)
                )
            }
        }
    }
}
