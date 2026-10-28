package com.threeloops.mirrorarchive.ui.albums

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
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.threeloops.mirrorarchive.contracts.AlbumType
import com.threeloops.mirrorarchive.data.demo.DisplayPhoto
import com.threeloops.mirrorarchive.data.demo.SampleMediaSeed
import com.threeloops.mirrorarchive.ui.sandbox.NitiSandboxDialog
import com.threeloops.mirrorarchive.ui.xray.DrishtiXRayBottomSheet

@Composable
fun AlbumsScreen(
    viewModel: AlbumsViewModel,
    onAlbumClick: (AlbumType) -> Unit,
    onNavigateDuplicates: () -> Unit,
    onNavigateStorageSaver: () -> Unit,
    onNavigateSettings: () -> Unit = {},
    onNavigateExport: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showNitiSandbox by remember { mutableStateOf(false) }
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val displayedPhotos by viewModel.displayedPhotos.collectAsState()
    val scanState by viewModel.scanState.collectAsState()
    var inspectingPhoto by remember { mutableStateOf<DisplayPhoto?>(null) }

    val filterPills = remember {
        listOf("All", "Receipts", "Portraits", "Screenshots", "Documents")
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF09090B))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(18.dp))

            // ========================================================
            // Header: Clean white text "Vault" + ultra-subtle telemetry
            // ========================================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Vault",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = (-0.5).sp
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(onClick = onNavigateExport) {
                        Text("Export", color = Color.White, fontSize = 12.sp)
                    }

                    // Ultra-subtle monospace telemetry badge
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF18181B), RoundedCornerShape(14.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(14.dp))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "${displayedPhotos.size} indexed • 42 img/s • offline",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFFA1A1AA)
                        )
                    }

                    // Niti Sandbox Tuning Button
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1F1F23))
                            .clickable { showNitiSandbox = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Tune,
                            contentDescription = "Niti Tuning",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(onClick = onNavigateSettings) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = "Settings",
                            tint = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ========================================================
            // Top Filter Carousel: Minimal, low-profile pill chips
            // Active: Pure white bg with black text
            // Inactive: Dark gray surface (#1F1F23) with muted white text
            // ========================================================
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 2.dp)
            ) {
                items(filterPills) { pill ->
                    val isSelected = selectedFilter == pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) Color.White else Color(0xFF1F1F23))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                viewModel.selectedFilter.value = pill
                            }
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = pill,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.Black else Color(0xFFA1A1AA)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ========================================================
            // 2-Column Staggered Vertical Grid showing REAL PHOTOS
            // AspectRatio: 0.85f, clip(8.dp)
            // Floating ML tag in top-right corner (#000000 60% alpha)
            // Title in bold white (13.sp), subtitle in #A1A1AA (11.sp)
            // ========================================================
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 110.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(displayedPhotos, key = { it.id }) { photo ->
                    RealPhotoGridCard(
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
fun RealPhotoGridCard(
    photo: DisplayPhoto,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    ) {
        // High-resolution real photo with Coil
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.85f)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF18181B))
        ) {
            AsyncImage(
                model = if (photo.drawableRes != 0) photo.drawableRes else photo.imageUrl,
                contentDescription = photo.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Floating ML Tag: Tiny semi-transparent pill in top-right corner
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .background(
                        color = Color.Black.copy(alpha = 0.60f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    .border(
                        width = 0.5.dp,
                        color = Color.White.copy(alpha = 0.20f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    .padding(horizontal = 7.dp, vertical = 3.dp)
            ) {
                Text(
                    text = photo.tag,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFFAFAFA)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Title in bold white (13.sp)
        Text(
            text = photo.title,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // Subtitle / date in #A1A1AA (11.sp)
        Text(
            text = photo.subtitle,
            fontSize = 11.sp,
            color = Color(0xFFA1A1AA),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
