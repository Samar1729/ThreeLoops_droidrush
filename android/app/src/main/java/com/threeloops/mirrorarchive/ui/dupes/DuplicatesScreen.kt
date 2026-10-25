package com.threeloops.mirrorarchive.ui.dupes

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
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.threeloops.mirrorarchive.contracts.ImageItem

data class DuplicateCluster(
    val id: Long,
    val title: String,
    val items: List<ImageItem>,
    val bestItemId: Long,
    val similarityPct: Int
)

@Composable
fun DuplicatesScreen(
    onBackClick: () -> Unit,
    onImageClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    // Sample duplicate clusters based on perceptual hash
    val clusters = remember {
        listOf(
            DuplicateCluster(
                id = 1L,
                title = "Burst Sequence • 3 photos",
                items = listOf(
                    ImageItem(1L, 101L, "content://media/external/images/media/101", 1700000000L, 1700000000000L, 2_450_000L, 1920, 1080, 145.0, 0),
                    ImageItem(2L, 102L, "content://media/external/images/media/102", 1700000002L, 1700000002000L, 2_440_000L, 1920, 1080, 88.0, 0),
                    ImageItem(3L, 103L, "content://media/external/images/media/103", 1700000004L, 1700000004000L, 2_460_000L, 1920, 1080, 92.0, 0)
                ),
                bestItemId = 1L,
                similarityPct = 98
            ),
            DuplicateCluster(
                id = 2L,
                title = "Near Duplicate • 2 photos",
                items = listOf(
                    ImageItem(4L, 104L, "content://media/external/images/media/104", 1700003000L, 1700003000000L, 4_200_000L, 3840, 2160, 160.0, 1),
                    ImageItem(5L, 105L, "content://media/external/images/media/105", 1700003005L, 1700003005000L, 4_180_000L, 3840, 2160, 110.0, 1)
                ),
                bestItemId = 4L,
                similarityPct = 94
            )
        )
    }

    var cleaned by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0C0C0E))
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .size(38.dp)
                    .background(Color(0xFF1C1C1E), CircleShape)
                    .border(1.dp, Color(0xFF2C2C2E), CircleShape)
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
                    text = if (cleaned) "All duplicates cleaned" else "${clusters.size} clusters • ~13.2 MB reclaimable",
                    fontSize = 12.sp,
                    color = Color(0xFFA1A1AA)
                )
            }
        }

        if (cleaned) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = null,
                        tint = Color(0xFF34C759),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Library Optimized",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Zero redundant duplicates found",
                        fontSize = 13.sp,
                        color = Color(0xFFA1A1AA)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Summary Card
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF18181B),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFF27272A), RoundedCornerShape(16.dp))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Reclaimable Storage",
                                    fontSize = 12.sp,
                                    color = Color(0xFFA1A1AA)
                                )
                                Text(
                                    text = "13.2 MB",
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            Button(
                                onClick = { cleaned = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Text(
                                    text = "Keep Best",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.Black
                                )
                            }
                        }
                    }
                }

                // Cluster Items
                items(clusters, key = { it.id }) { cluster ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF18181B),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFF27272A), RoundedCornerShape(16.dp))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = cluster.title,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                                Text(
                                    text = "${cluster.similarityPct}% match",
                                    fontSize = 12.sp,
                                    color = Color(0xFF34C759)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                items(cluster.items) { item ->
                                    val isBest = item.id == cluster.bestItemId
                                    Box(
                                        modifier = Modifier
                                            .size(110.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0xFF27272A))
                                            .border(
                                                width = if (isBest) 2.dp else 0.dp,
                                                color = if (isBest) Color.White else Color.Transparent,
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .clickable { onImageClick(item.id) }
                                    ) {
                                        AsyncImage(
                                            model = item.uri,
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )

                                        if (isBest) {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = Color.White,
                                                modifier = Modifier
                                                    .align(Alignment.BottomStart)
                                                    .padding(6.dp)
                                            ) {
                                                Text(
                                                    text = "Best",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.Black,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
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
