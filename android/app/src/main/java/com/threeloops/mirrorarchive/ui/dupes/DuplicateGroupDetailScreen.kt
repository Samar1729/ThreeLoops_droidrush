package com.threeloops.mirrorarchive.ui.dupes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.PhotoSizeSelectLarge
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.threeloops.mirrorarchive.contracts.ImageItem
import com.threeloops.mirrorarchive.contracts.displayName
import com.threeloops.mirrorarchive.dupes.DuplicateGroup
import com.threeloops.mirrorarchive.dupes.DuplicatesUiState
import com.threeloops.mirrorarchive.dupes.DuplicatesViewModel
import com.threeloops.mirrorarchive.dupes.PurgePlan
import com.threeloops.mirrorarchive.dupes.PurgeUiState
import com.threeloops.mirrorarchive.dupes.PurgeViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Detail screen for a single duplicate group, showing all member images
 * with metadata comparisons and a purge action.
 *
 * @param groupId The ID of the group to display.
 * @param duplicatesViewModel ViewModel providing groups and image data.
 * @param purgeViewModel ViewModel managing the purge execution.
 * @param onBackClick Callback for back navigation.
 * @param onImageClick Callback when an image is tapped for detail.
 * @param modifier Layout modifier.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DuplicateGroupDetailScreen(
    groupId: Long,
    duplicatesViewModel: DuplicatesViewModel,
    purgeViewModel: PurgeViewModel,
    onBackClick: () -> Unit,
    onImageClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by duplicatesViewModel.state.collectAsState()
    val purgeState by purgeViewModel.state.collectAsState()

    // Find the group and build context
    val (group, imageMap) = remember(uiState, groupId) {
        when (val state = uiState) {
            is DuplicatesUiState.Results -> {
                val g = state.groups.find { it.id == groupId }
                Pair(g, state.imageMap)
            }
            else -> Pair(null, emptyMap())
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Group #$groupId",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        if (group == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Group not found",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
            }
            return@Scaffold
        }

        val groupImages = group.imageIds.mapNotNull { imageMap[it] }
        val plan = remember(group) { duplicatesViewModel.planPurge(group) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Group info card
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${groupImages.size} photos in group",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Max distance: ${group.maxDistance}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Reclaimable: ${String.format("%.1f", plan.reclaimableBytes / (1024.0 * 1024.0))} MB",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Image grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                items(groupImages) { image ->
                    val isBest = image.id == group.bestId
                    val isPurge = image.id in plan.purgeIds

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(0.75f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF262626))
                            .then(
                                if (isBest) {
                                    Modifier.border(
                                        width = 2.dp,
                                        color = Color(0xFF4ADE80),
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                } else {
                                    Modifier.border(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                }
                            )
                            .clickable { onImageClick(image.id) }
                    ) {
                        AsyncImage(
                            model = image.uri,
                            contentDescription = image.displayName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Gradient overlay
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp)
                                .align(Alignment.BottomCenter)
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color.Black.copy(alpha = 0.7f)
                                        )
                                    )
                                )
                        )

                        // Status badge
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isBest) Color(0xFF4ADE80).copy(alpha = 0.9f)
                            else Color.Red.copy(alpha = 0.8f),
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(8.dp)
                        ) {
                            Text(
                                text = if (isBest) "KEEP" else "PURGE",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isBest) Color.Black else Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        // Image metadata
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(8.dp)
                        ) {
                            Text(
                                text = image.displayName,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Blur: ${image.blurScore.toInt()} · ${formatSize(image.sizeBytes)}",
                                fontSize = 9.sp,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Purge action
            when (purgeState) {
                is PurgeUiState.Idle -> {
                    Button(
                        onClick = { purgeViewModel.executePurge(plan) },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DeleteSweep,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Keep Best & Delete ${plan.purgeIds.size} (${String.format("%.1f", plan.reclaimableBytes / (1024.0 * 1024.0))} MB)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                is PurgeUiState.Purging -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                }

                is PurgeUiState.Done -> {
                    val done = purgeState as PurgeUiState.Done
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF4ADE80).copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF4ADE80),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Deleted ${done.deletedCount} photos · Freed ${String.format("%.1f", done.freedBytes / (1024.0 * 1024.0))} MB",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }

                is PurgeUiState.Error -> {
                    val error = purgeState as PurgeUiState.Error
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = error.message,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * Formats byte size to a human-readable string.
 */
private fun formatSize(bytes: Long): String {
    return when {
        bytes >= 1024 * 1024 -> "${String.format("%.1f", bytes / (1024.0 * 1024.0))} MB"
        bytes >= 1024 -> "${bytes / 1024} KB"
        else -> "$bytes B"
    }
}
