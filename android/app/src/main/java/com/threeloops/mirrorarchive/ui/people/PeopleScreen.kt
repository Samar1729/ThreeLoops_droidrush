package com.threeloops.mirrorarchive.ui.people

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Face
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import coil.compose.AsyncImage
import com.threeloops.mirrorarchive.data.demo.DemoPhoto
import com.threeloops.mirrorarchive.data.demo.DemoSeedData
import com.threeloops.mirrorarchive.people.PeopleViewModel
import com.threeloops.mirrorarchive.ui.burst.PravahSmartStacksView
import com.threeloops.mirrorarchive.ui.theme.AmbientMeshBox
import com.threeloops.mirrorarchive.ui.theme.TextPrimary
import com.threeloops.mirrorarchive.ui.theme.TextSecondary
import com.threeloops.mirrorarchive.ui.theme.glass
import com.threeloops.mirrorarchive.ui.xray.DrishtiXRayBottomSheet

import androidx.lifecycle.compose.collectAsStateWithLifecycle

enum class PeopleTab {
    CLUSTERS,
    BURST_ARENA
}

@Composable
fun PeopleScreen(
    viewModel: PeopleViewModel,
    onImageClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(PeopleTab.CLUSTERS) }
    var cosineThreshold by remember { mutableFloatStateOf(0.85f) }
    var selectedClusterId by remember { mutableStateOf<Int?>(null) }
    var inspectingPhoto by remember { mutableStateOf<DemoPhoto?>(null) }

    // Dynamic cluster groups calculated from cosine similarity threshold slider
    val clusterGroups = remember(cosineThreshold) {
        DemoSeedData.getClusterGroups(cosineThreshold)
    }

    val peoplePhotos by viewModel.portraitPhotos.collectAsStateWithLifecycle()

    val displayedPhotos = remember(selectedClusterId, clusterGroups, peoplePhotos) {
        if (selectedClusterId == null) {
            peoplePhotos
        } else {
            clusterGroups[selectedClusterId] ?: peoplePhotos
        }
    }

    AmbientMeshBox {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "People & Bursts",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF064E3B),
                letterSpacing = (-0.5).sp
            )

            Text(
                text = "Offline Face Geometry Clustering & Pravah Burst Arena",
                fontSize = 11.sp,
                color = Color(0xFF059669),
                modifier = Modifier.padding(top = 2.dp, bottom = 14.dp)
            )

            // Segmented Frosted Glass Pill Switcher
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color(0xFFE8F5E9))
                    .border(1.dp, Color(0x3310B981), RoundedCornerShape(22.dp))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Tab 1: Face Clusters
                val isPortraits = selectedTab == PeopleTab.CLUSTERS
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(18.dp))
                        .background(if (isPortraits) Color(0xFF059669) else Color.Transparent)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { selectedTab = PeopleTab.CLUSTERS },
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Face,
                            contentDescription = null,
                            tint = if (isPortraits) Color.White else Color(0xFF475569),
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Portraits (${displayedPhotos.size})",
                            fontSize = 12.sp,
                            fontWeight = if (isPortraits) FontWeight.SemiBold else FontWeight.Medium,
                            color = if (isPortraits) Color.White else Color(0xFF475569)
                        )
                    }
                }

                // Tab 2: Pravah Burst Arena
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(18.dp))
                        .background(if (!isPortraits) Color(0xFF059669) else Color.Transparent)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { selectedTab = PeopleTab.BURST_ARENA },
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.AutoAwesome,
                            contentDescription = null,
                            tint = if (!isPortraits) Color.White else Color(0xFF475569),
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Pravah Bursts",
                            fontSize = 12.sp,
                            fontWeight = if (!isPortraits) FontWeight.Bold else FontWeight.Medium,
                            color = if (!isPortraits) Color.White else Color(0xFF475569)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            AnimatedContent(targetState = selectedTab, label = "peopleTabTransition") { tab ->
                when (tab) {
                    PeopleTab.BURST_ARENA -> {
                        PravahSmartStacksView(onImageClick = onImageClick)
                    }

                    PeopleTab.CLUSTERS -> {
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Cosine Similarity Threshold Slider Control Card
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(Color.White)
                                    .border(1.dp, Color(0x2210B981), RoundedCornerShape(18.dp))
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Cosine Similarity Threshold",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF064E3B)
                                        )

                                        Text(
                                            text = "${"%.2f".format(cosineThreshold)} (${clusterGroups.size} clusters)",
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF059669)
                                        )
                                    }

                                    Slider(
                                        value = cosineThreshold,
                                        onValueChange = {
                                            cosineThreshold = it
                                            selectedClusterId = null
                                        },
                                        valueRange = 0.75f..0.95f,
                                        colors = SliderDefaults.colors(
                                            thumbColor = Color(0xFF059669),
                                            activeTrackColor = Color(0xFF059669),
                                            inactiveTrackColor = Color(0xFFD1FAE5)
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Circular Face Cluster Row: Person 1 (6), Person 2 (5), Person 3 (3)
                            Text(
                                text = "Identified Face Clusters",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFA1A1AA),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(horizontal = 2.dp, vertical = 2.dp)
                            ) {
                                // "All" Avatar
                                item {
                                    val isAllSelected = selectedClusterId == null
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.clickable { selectedClusterId = null }
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(54.dp)
                                                .clip(CircleShape)
                                                .background(if (isAllSelected) Color(0x3310B981) else Color.White.copy(alpha = 0.08f))
                                                .border(
                                                    width = if (isAllSelected) 2.dp else 1.dp,
                                                    color = if (isAllSelected) Color(0xFF10B981) else Color.White.copy(alpha = 0.20f),
                                                    shape = CircleShape
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.Person,
                                                contentDescription = null,
                                                tint = if (isAllSelected) Color(0xFF10B981) else Color.White,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "All (14)",
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = if (isAllSelected) Color(0xFF10B981) else Color(0xFFA1A1AA)
                                        )
                                    }
                                }

                                items(clusterGroups.keys.sorted().toList()) { clusterId ->
                                    val count = clusterGroups[clusterId]?.size ?: 0
                                    val isSelected = selectedClusterId == clusterId
                                    val repPhoto = clusterGroups[clusterId]?.firstOrNull()

                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.clickable {
                                            selectedClusterId = if (isSelected) null else clusterId
                                        }
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(54.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (isSelected) Color(0x3310B981) else Color(0xFF18181B)
                                                )
                                                .border(
                                                    width = if (isSelected) 2.dp else 1.dp,
                                                    color = if (isSelected) Color(0xFF10B981) else Color.White.copy(alpha = 0.20f),
                                                    shape = CircleShape
                                                )
                                        ) {
                                            if (repPhoto != null) {
                                                AsyncImage(
                                                    model = if (repPhoto.clusterAvatarRes != 0) repPhoto.clusterAvatarRes else (if (repPhoto.drawableRes != 0) repPhoto.drawableRes else repPhoto.imageUrl),
                                                    contentDescription = null,
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.fillMaxSize().clip(CircleShape)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Text(
                                            text = "Person $clusterId ($count)",
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) Color(0xFF10B981) else Color(0xFFA1A1AA)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // 2-Column Portraits Grid with Face Landmarks
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(bottom = 110.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(displayedPhotos, key = { it.id }) { photo ->
                                    PortraitCard(
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
private fun PortraitCard(
    photo: DemoPhoto,
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

            // Top-Right X-Ray Pill
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .background(Color(0xD9064E3B), CircleShape)
                    .padding(5.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Visibility,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(11.dp)
                )
            }

            // Bottom Left Quality & Smile Pill
            val smileProb = ((photo.smileProb ?: 0.90f) * 100).toInt()
            val eyesProb = ((photo.eyesOpenProb ?: 0.95f) * 100).toInt()

            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(6.dp)
                    .background(Color(0xF0059669), RoundedCornerShape(8.dp))
                    .padding(horizontal = 6.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "Eyes $eyesProb% • Smile $smileProb%",
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = photo.title,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF064E3B),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = photo.subtitle,
            fontSize = 11.sp,
            color = Color(0xFF64748B),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
