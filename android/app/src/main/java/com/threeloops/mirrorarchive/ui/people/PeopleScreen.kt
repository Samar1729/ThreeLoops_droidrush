package com.threeloops.mirrorarchive.ui.people

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Face
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import com.threeloops.mirrorarchive.people.PeopleViewModel
import com.threeloops.mirrorarchive.ui.theme.GlassScaffoldBackground
import com.threeloops.mirrorarchive.ui.theme.TextMuted
import com.threeloops.mirrorarchive.ui.theme.TextPrimary
import com.threeloops.mirrorarchive.ui.theme.TextSecondary
import com.threeloops.mirrorarchive.ui.theme.glass

@Composable
fun PeopleScreen(
    viewModel: PeopleViewModel,
    onImageClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val images by viewModel.peopleImages.collectAsStateWithLifecycle()

    GlassScaffoldBackground {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "People",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                letterSpacing = (-0.5).sp
            )

            Text(
                text = "Portraits and detected facial geometry",
                fontSize = 13.sp,
                color = TextSecondary,
                modifier = Modifier.padding(top = 2.dp, bottom = 18.dp)
            )

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
    }
}
