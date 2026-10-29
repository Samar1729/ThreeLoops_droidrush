package com.threeloops.mirrorarchive.ui.storage

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.hilt.navigation.compose.hiltViewModel
import com.threeloops.mirrorarchive.contracts.ImageItem
import com.threeloops.mirrorarchive.storage.StorageSaverViewModel
import com.threeloops.mirrorarchive.ui.theme.GlassCard
import com.threeloops.mirrorarchive.ui.theme.GlassScaffoldBackground
import com.threeloops.mirrorarchive.ui.theme.TextMuted
import com.threeloops.mirrorarchive.ui.theme.TextPrimary
import com.threeloops.mirrorarchive.ui.theme.TextSecondary
import com.threeloops.mirrorarchive.ui.theme.glass

@Composable
fun StorageSaverScreen(
    onBackClick: () -> Unit,
    onImageClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: StorageSaverViewModel = hiltViewModel()
) {
    val currentThreshold by viewModel.threshold.collectAsState()
    val lowBlurImages by viewModel.lowBlurImages.collectAsState()
    val reclaimableBytes by viewModel.reclaimableBytes.collectAsState()
    val context = LocalContext.current

    val sampleBlurry = remember {
        listOf(
            ImageItem(4L, 104L, "content://media/external/images/media/104", 1700003000L, 1700003000000L, 4_200_000L, 3840, 2160, 32.0, 0),
            ImageItem(2L, 102L, "content://media/external/images/media/102", 1700001000L, 1700001000000L, 3_120_000L, 1920, 1080, 58.0, 1)
        )
    }

    val displayImages = if (lowBlurImages.isNotEmpty()) lowBlurImages else sampleBlurry.filter { it.blurScore < currentThreshold }
    val totalBytes = if (lowBlurImages.isNotEmpty()) reclaimableBytes else displayImages.sumOf { it.sizeBytes }

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
                        tint = TextPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = "Storage Saver",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Blurry & Low Sharpness Photos",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                // Threshold Adjustment Glass Card
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    backgroundColor = Color.White.copy(alpha = 0.06f),
                    topBorderAlpha = 0.28f
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Sharpness Threshold",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary
                            )
                            Text(
                                text = "< ${currentThreshold.toInt()}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Slider(
                            value = currentThreshold.toFloat(),
                            onValueChange = { viewModel.updateThreshold(it.toDouble()) },
                            valueRange = 20f..150f,
                            colors = SliderDefaults.colors(
                                thumbColor = Color.White,
                                activeTrackColor = Color.White,
                                inactiveTrackColor = Color.White.copy(alpha = 0.15f)
                            )
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${displayImages.size} photos flagged",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                            Text(
                                text = "Free up ${"%.1f".format(totalBytes / (1024.0 * 1024.0))} MB",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (displayImages.isNotEmpty()) {
                    Button(
                        onClick = {
                            viewModel.purgeLowBlur(displayImages) {
                                Toast.makeText(context, "Cleaned ${displayImages.size} blurry photos", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text(
                            text = "Clean All Flagged (${"%.1f".format(totalBytes / (1024.0 * 1024.0))} MB)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color.Black
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Grid of Blurry Photos
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 100.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(displayImages, key = { it.id }) { image ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .glass(
                                    shape = RoundedCornerShape(16.dp),
                                    backgroundColor = Color.White.copy(alpha = 0.05f),
                                    topBorderAlpha = 0.25f,
                                    bottomBorderAlpha = 0.06f
                                )
                                .clickable { onImageClick(image.id) }
                        ) {
                            AsyncImage(
                                model = image.uri,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(16.dp))
                            )

                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(8.dp)
                                    .glass(
                                        shape = RoundedCornerShape(6.dp),
                                        backgroundColor = Color.Black.copy(alpha = 0.70f),
                                        topBorderAlpha = 0.35f
                                    )
                                    .padding(horizontal = 7.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "Sharpness: ${image.blurScore.toInt()}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
