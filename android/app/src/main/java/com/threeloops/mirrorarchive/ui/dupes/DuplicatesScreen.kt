package com.threeloops.mirrorarchive.ui.dupes

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.threeloops.mirrorarchive.data.demo.DemoPhoto
import com.threeloops.mirrorarchive.data.demo.DemoSeedData
import com.threeloops.mirrorarchive.ui.components.DemoPhotoCanvas
import com.threeloops.mirrorarchive.ui.theme.GlassScaffoldBackground
import com.threeloops.mirrorarchive.ui.theme.glass
import com.threeloops.mirrorarchive.ui.xray.DrishtiXRayBottomSheet

@Composable
fun DuplicatesScreen(
    onBackClick: () -> Unit,
    onImageClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isPurged by remember { mutableStateOf(false) }
    var reclaimableMb by remember { mutableStateOf("13.2 MB") }
    var inspectingPhoto by remember { mutableStateOf<DemoPhoto?>(null) }

    val burstItems = remember { DemoSeedData.burstPhotos }
    val displayedBurstItems = remember(isPurged) {
        if (isPurged) burstItems.filter { it.isWinner } else burstItems
    }

    GlassScaffoldBackground {
        Column(
            modifier = modifier.fillMaxSize()
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .size(40.dp)
                        .glass(
                            shape = CircleShape,
                            backgroundColor = Color.White.copy(alpha = 0.08f),
                            topBorderAlpha = 0.35f
                        )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = "Duplicates & Bursts",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = if (isPurged) "Library Optimized • 0 redundant duplicates" else "1 burst sequence • ~13.2 MB reclaimable",
                        fontSize = 12.sp,
                        color = if (isPurged) Color(0xFF10B981) else Color(0xFFA1A1AA)
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 40.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Reclaimable Storage Summary Glass Card
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .glass(
                                shape = RoundedCornerShape(20.dp),
                                backgroundColor = Color(0xDD141418),
                                topBorderAlpha = 0.35f,
                                bottomBorderAlpha = 0.08f
                            )
                    ) {
                        Row(
                            modifier = Modifier.padding(18.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "RECLAIMABLE STORAGE",
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = 1.sp,
                                    color = Color(0xFFA1A1AA)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = reclaimableMb,
                                    fontSize = 28.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isPurged) Color(0xFF10B981) else Color.White
                                )
                            }

                            if (!isPurged) {
                                Button(
                                    onClick = {
                                        isPurged = true
                                        reclaimableMb = "0.0 MB"
                                        Toast.makeText(
                                            context,
                                            "Purged 3 redundant shots • Saved 13.2 MB",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.White,
                                        contentColor = Color.Black
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.AutoAwesome,
                                        contentDescription = null,
                                        tint = Color.Black,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Keep Best",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .glass(
                                            shape = RoundedCornerShape(12.dp),
                                            backgroundColor = Color(0x2210B981),
                                            borderWidth = 1.dp,
                                            topBorderAlpha = 0.40f
                                        )
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Outlined.Check,
                                            contentDescription = null,
                                            tint = Color(0xFF10B981),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "OPTIMIZED",
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF10B981)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Burst Sequence Cluster Card
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .glass(
                                shape = RoundedCornerShape(20.dp),
                                backgroundColor = Color(0xDD141418),
                                topBorderAlpha = 0.30f,
                                bottomBorderAlpha = 0.06f
                            )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Action Burst Sequence (4 shots)",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = if (isPurged) "Purged: Kept Best Shot • 3 deleted" else "98% visual similarity • 1 Best Shot candidate",
                                        fontSize = 11.sp,
                                        color = if (isPurged) Color(0xFF10B981) else Color(0xFFA1A1AA)
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .glass(
                                            shape = RoundedCornerShape(8.dp),
                                            backgroundColor = Color.White.copy(alpha = 0.08f),
                                            borderWidth = 1.dp,
                                            topBorderAlpha = 0.30f
                                        )
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "pHash: 98%",
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFFE4E4E7)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Horizontal list of burst candidates with dynamic math badges
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(vertical = 4.dp)
                            ) {
                                items(displayedBurstItems, key = { it.id }) { photo ->
                                    BurstPhotoItemCard(
                                        photo = photo,
                                        onClick = { inspectingPhoto = photo }
                                    )
                                }
                            }
                        }
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
    }
}

@Composable
private fun BurstPhotoItemCard(
    photo: DemoPhoto,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(170.dp)
            .height(200.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = if (photo.isWinner) 2.dp else 1.dp,
                color = if (photo.isWinner) Color(0xFF10B981) else Color.White.copy(alpha = 0.15f),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
    ) {
        // Real Procedural Photo Visual
        DemoPhotoCanvas(
            photo = photo,
            modifier = Modifier.fillMaxSize(),
            showLandmarks = photo.faces.isNotEmpty()
        )

        // Gradient Scrim
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.90f)),
                        startY = 180f
                    )
                )
        )

        // Overlaid Exact Math Badges as specified
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            if (photo.isWinner) {
                // Winner Card: Emerald badge
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .glass(
                            shape = RoundedCornerShape(8.dp),
                            backgroundColor = Color(0xE0103826),
                            borderWidth = 1.dp,
                            topBorderAlpha = 0.60f
                        )
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Column {
                        Text(
                            text = "★ BEST • Score 0.92",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF10B981)
                        )
                        Text(
                            text = "(Sharpness 91 | Face 0.94)",
                            fontSize = 8.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFFA7F3D0)
                        )
                    }
                }
            } else {
                // Loser Card: Red/Zinc badge
                val badgeText = photo.rejectionReason ?: "Drop • Low Quality"
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .glass(
                            shape = RoundedCornerShape(8.dp),
                            backgroundColor = Color(0xE027272A),
                            borderWidth = 1.dp,
                            topBorderAlpha = 0.40f
                        )
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = badgeText,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFEF4444),
                        lineHeight = 12.sp
                    )
                }
            }
        }
    }
}
