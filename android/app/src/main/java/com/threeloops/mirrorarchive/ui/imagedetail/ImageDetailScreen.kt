package com.threeloops.mirrorarchive.ui.imagedetail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.threeloops.mirrorarchive.contracts.displayName
import com.threeloops.mirrorarchive.ui.theme.GlassCard
import com.threeloops.mirrorarchive.ui.theme.GlassScaffoldBackground
import com.threeloops.mirrorarchive.ui.theme.TextMuted
import com.threeloops.mirrorarchive.ui.theme.TextPrimary
import com.threeloops.mirrorarchive.ui.theme.TextSecondary
import com.threeloops.mirrorarchive.ui.theme.glass

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ImageDetailScreen(
    image: ImageItem,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

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
                        text = image.displayName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        maxLines = 1
                    )
                    Text(
                        text = "${image.width} × ${image.height} • ${"%.1f".format(image.sizeBytes / (1024.0 * 1024.0))} MB",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Main Photo Canvas with Specular Glass Frame
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 260.dp, max = 420.dp)
                        .glass(
                            shape = RoundedCornerShape(20.dp),
                            backgroundColor = Color.White.copy(alpha = 0.05f),
                            topBorderAlpha = 0.30f,
                            bottomBorderAlpha = 0.08f
                        )
                ) {
                    AsyncImage(
                        model = image.uri,
                        contentDescription = image.displayName,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(20.dp))
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Metadata Glass Card
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    backgroundColor = Color.White.copy(alpha = 0.05f),
                    topBorderAlpha = 0.25f
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Properties",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(text = "Resolution", fontSize = 12.sp, color = TextMuted)
                                Text(text = "${image.width} × ${image.height}", fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                            }
                            Column {
                                Text(text = "Sharpness", fontSize = 12.sp, color = TextMuted)
                                Text(text = "${image.blurScore.toInt()}", fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                            }
                            Column {
                                Text(text = "Faces", fontSize = 12.sp, color = TextMuted)
                                Text(text = "${image.faceCount}", fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                            }
                            Column {
                                Text(text = "Storage", fontSize = 12.sp, color = TextMuted)
                                Text(text = "${"%.1f".format(image.sizeBytes / (1024.0 * 1024.0))} MB", fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Smart Tags Glass Card
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    backgroundColor = Color.White.copy(alpha = 0.05f),
                    topBorderAlpha = 0.25f
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Tags",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val tags = mutableListOf("On-device Indexed", "Sharp Focus")
                            if (image.faceCount > 0) {
                                tags.add("People (${image.faceCount} ${if (image.faceCount == 1) "Face" else "Faces"})")
                                tags.add("Facial Geometry")
                            } else {
                                tags.add("Auto-categorized")
                            }
                            tags.forEach { tag ->
                                Box(
                                    modifier = Modifier
                                        .glass(
                                            shape = RoundedCornerShape(10.dp),
                                            backgroundColor = Color.White.copy(alpha = 0.08f),
                                            topBorderAlpha = 0.30f,
                                            bottomBorderAlpha = 0.08f
                                        )
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = tag,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = TextPrimary
                                    )
                                }
                            }
                        }
                    }
                }

                if (image.faceCount > 0) {
                    Spacer(modifier = Modifier.height(14.dp))

                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        backgroundColor = Color.White.copy(alpha = 0.05f),
                        topBorderAlpha = 0.25f
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                text = "Facial Analysis",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Faces Detected", fontSize = 13.sp, color = TextSecondary)
                                Text(text = "${image.faceCount}", fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Detection Precision", fontSize = 13.sp, color = TextSecondary)
                                Text(text = "High Accuracy", fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Landmarks & Attributes", fontSize = 13.sp, color = TextSecondary)
                                Text(text = "Contours & Alignment", fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
