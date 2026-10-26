package com.threeloops.mirrorarchive.ui.people

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Face
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.threeloops.mirrorarchive.people.PeopleViewModel
import com.threeloops.mirrorarchive.ui.burst.PravahSmartStacksView
import com.threeloops.mirrorarchive.ui.theme.AmbientMeshBox
import com.threeloops.mirrorarchive.ui.theme.TextMuted
import com.threeloops.mirrorarchive.ui.theme.TextPrimary
import com.threeloops.mirrorarchive.ui.theme.TextSecondary
import com.threeloops.mirrorarchive.ui.theme.glass

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
    val images by viewModel.peopleImages.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableStateOf(PeopleTab.CLUSTERS) }

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
                color = TextPrimary,
                letterSpacing = (-0.5).sp
            )

            Text(
                text = "Facial geometry and Pravah burst best-shot scoring",
                fontSize = 12.sp,
                color = TextSecondary,
                modifier = Modifier.padding(top = 2.dp, bottom = 14.dp)
            )

            // Segmented Frosted Glass Pill Switcher
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .glass(
                        shape = RoundedCornerShape(22.dp),
                        backgroundColor = Color.White.copy(alpha = 0.05f),
                        borderWidth = 1.dp,
                        topBorderAlpha = 0.25f
                    )
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Tab 1: Face Clusters
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(18.dp))
                        .then(
                            if (selectedTab == PeopleTab.CLUSTERS) {
                                Modifier.glass(
                                    shape = RoundedCornerShape(18.dp),
                                    backgroundColor = Color.White.copy(alpha = 0.12f),
                                    borderWidth = 1.dp,
                                    topBorderAlpha = 0.35f
                                )
                            } else {
                                Modifier
                            }
                        )
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
                            tint = if (selectedTab == PeopleTab.CLUSTERS) Color.White else Color(0xFFA1A1AA),
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Portraits (${images.size})",
                            fontSize = 12.sp,
                            fontWeight = if (selectedTab == PeopleTab.CLUSTERS) FontWeight.SemiBold else FontWeight.Medium,
                            color = if (selectedTab == PeopleTab.CLUSTERS) Color.White else Color(0xFFA1A1AA)
                        )
                    }
                }

                // Tab 2: Pravah Burst Arena
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(18.dp))
                        .then(
                            if (selectedTab == PeopleTab.BURST_ARENA) {
                                Modifier.glass(
                                    shape = RoundedCornerShape(18.dp),
                                    backgroundColor = Color(0x3310B981),
                                    borderWidth = 1.dp,
                                    topBorderAlpha = 0.45f
                                )
                            } else {
                                Modifier
                            }
                        )
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
                            tint = if (selectedTab == PeopleTab.BURST_ARENA) Color(0xFF10B981) else Color(0xFFA1A1AA),
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Pravah Bursts",
                            fontSize = 12.sp,
                            fontWeight = if (selectedTab == PeopleTab.BURST_ARENA) FontWeight.Bold else FontWeight.Medium,
                            color = if (selectedTab == PeopleTab.BURST_ARENA) Color(0xFF10B981) else Color(0xFFA1A1AA)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            AnimatedContent(targetState = selectedTab, label = "peopleTabTransition") { tab ->
                when (tab) {
                    PeopleTab.BURST_ARENA -> {
                        PravahSmartStacksView(onImageClick = onImageClick)
                    }
                    PeopleTab.CLUSTERS -> {
                        if (images.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(bottom = 70.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .size(58.dp)
                                            .glass(
                                                shape = RoundedCornerShape(18.dp),
                                                backgroundColor = Color.White.copy(alpha = 0.05f),
                                                topBorderAlpha = 0.20f
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Face,
                                            contentDescription = null,
                                            tint = TextSecondary,
                                            modifier = Modifier.size(30.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Text(
                                        text = "No portrait photos yet",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = TextPrimary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Photos with detected faces will be clustered here",
                                        fontSize = 12.sp,
                                        color = TextMuted
                                    )
                                }
                            }
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(bottom = 100.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(images, key = { it.id }) { image ->
                                    val quality = viewModel.faceQuality(image.id)
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .aspectRatio(0.85f)
                                            .glass(
                                                shape = RoundedCornerShape(20.dp),
                                                backgroundColor = Color.White.copy(alpha = 0.06f),
                                                topBorderAlpha = 0.30f,
                                                bottomBorderAlpha = 0.08f
                                            )
                                            .clickable { onImageClick(image.id) }
                                    ) {
                                        AsyncImage(
                                            model = image.uri,
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(RoundedCornerShape(20.dp))
                                        )

                                        // Floating Glass Badges
                                        Row(
                                            modifier = Modifier
                                                .align(Alignment.BottomStart)
                                                .padding(10.dp),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .glass(
                                                        shape = RoundedCornerShape(8.dp),
                                                        backgroundColor = Color.Black.copy(alpha = 0.65f),
                                                        borderWidth = 1.dp,
                                                        topBorderAlpha = 0.35f,
                                                        bottomBorderAlpha = 0.10f
                                                    )
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    text = "${image.faceCount} ${if (image.faceCount == 1) "face" else "faces"}",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = TextPrimary
                                                )
                                            }

                                            if (quality > 0.65f) {
                                                Box(
                                                    modifier = Modifier
                                                        .glass(
                                                            shape = RoundedCornerShape(8.dp),
                                                            backgroundColor = Color.Black.copy(alpha = 0.65f),
                                                            borderWidth = 1.dp,
                                                            topBorderAlpha = 0.40f,
                                                            bottomBorderAlpha = 0.10f
                                                        )
                                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                                ) {
                                                    Text(
                                                        text = "Sharp",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = Color(0xFF10B981)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
