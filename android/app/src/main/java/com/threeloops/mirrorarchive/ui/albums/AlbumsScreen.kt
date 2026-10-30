package com.threeloops.mirrorarchive.ui.albums

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.threeloops.mirrorarchive.contracts.AlbumType
import com.threeloops.mirrorarchive.data.demo.DisplayPhoto
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
    val context = LocalContext.current
    var showNitiSandbox by remember { mutableStateOf(false) }
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val displayedPhotos by viewModel.displayedPhotos.collectAsState()
    val scanState by viewModel.scanState.collectAsState()
    val isImporting by viewModel.isImporting.collectAsState()
    val importMessage by viewModel.importMessage.collectAsState()
    var inspectingPhoto by remember { mutableStateOf<DisplayPhoto?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            Toast.makeText(context, "Processing photo with on-device ML & OCR...", Toast.LENGTH_SHORT).show()
            viewModel.importPhoto(context, uri)
        }
    }

    val filterPills = remember {
        listOf("All", "Receipts", "Portraits", "Screenshots", "Documents")
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF7FCF9))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(18.dp))

            // ========================================================
            // Header: Forest green "Vault" + Add Photo + telemetry
            // ========================================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Vault",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF064E3B),
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "Private Offline Archive",
                        fontSize = 11.sp,
                        color = Color(0xFF059669)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Modern "+ Add Photo" button to test local ML & OCR
                    Button(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF059669),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AddPhotoAlternate,
                            contentDescription = "Add Photo",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "+ Add Pic",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Sync button
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE8F5E9))
                            .border(1.dp, Color(0x3310B981), CircleShape)
                            .clickable { viewModel.syncNewPhotos() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Sync,
                            contentDescription = "Sync new photos",
                            tint = Color(0xFF065F46),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Monospace telemetry badge
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFD1FAE5), RoundedCornerShape(14.dp))
                            .border(1.dp, Color(0x3310B981), RoundedCornerShape(14.dp))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "${displayedPhotos.size} indexed",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF065F46)
                        )
                    }

                    // Niti Tuning Button
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE8F5E9))
                            .border(1.dp, Color(0x3310B981), CircleShape)
                            .clickable { showNitiSandbox = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Tune,
                            contentDescription = "Niti Tuning",
                            tint = Color(0xFF065F46),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Settings Button
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE8F5E9))
                            .border(1.dp, Color(0x3310B981), CircleShape)
                            .clickable(onClick = onNavigateSettings),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = "Settings",
                            tint = Color(0xFF065F46),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Real-time import / indexing progress banner
            AnimatedVisibility(
                visible = isImporting || importMessage != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .background(Color(0xFFE6F4EA), RoundedCornerShape(12.dp))
                        .border(1.dp, Color(0xFF34A853).copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    if (isImporting) {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = Color(0xFF059669),
                            trackColor = Color(0xFFD1FAE5)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                    Text(
                        text = importMessage ?: "Analyzing photo...",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF064E3B),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ========================================================
            // Top Filter Carousel: Green-to-White Pill Chips
            // Active: Radiant emerald background with white text
            // Inactive: White surface with emerald border and slate text
            // ========================================================
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 2.dp)
            ) {
                items(filterPills) { pill ->
                    val isSelected = selectedFilter.equals(pill, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) Color(0xFF059669) else Color.White)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) Color(0xFF059669) else Color(0x3310B981),
                                shape = RoundedCornerShape(16.dp)
                            )
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
                            color = if (isSelected) Color.White else Color(0xFF334155)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ========================================================
            // 2-Column Staggered Grid showing REAL PHOTOS
            // Cards on crisp white surfaces with emerald accents
            // ========================================================
            if (displayedPhotos.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AddPhotoAlternate,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No photos in this view yet",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF064E3B)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Tap '+ Add Pic' above to import any photo and test on-device ML & OCR instantly!",
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            } else {
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
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .border(1.dp, Color(0x2210B981), RoundedCornerShape(14.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(8.dp)
    ) {
        // Real photo preview
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.92f)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFE2E8F0))
        ) {
            AsyncImage(
                model = if (photo.drawableRes != 0) photo.drawableRes else photo.imageUrl,
                contentDescription = photo.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Floating ML Tag: Emerald pill in top-right corner
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .background(
                        color = Color(0xF0059669),
                        shape = RoundedCornerShape(10.dp)
                    )
                    .padding(horizontal = 7.dp, vertical = 3.dp)
            ) {
                Text(
                    text = photo.tag,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Title in deep forest green (13.sp)
        Text(
            text = photo.title,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF064E3B),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // Subtitle / date in muted slate (11.sp)
        Text(
            text = photo.subtitle,
            fontSize = 11.sp,
            color = Color(0xFF64748B),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}