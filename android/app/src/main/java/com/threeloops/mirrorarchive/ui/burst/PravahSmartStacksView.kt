package com.threeloops.mirrorarchive.ui.burst

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.DeleteSweep
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
import com.threeloops.mirrorarchive.ui.theme.glass
import com.threeloops.mirrorarchive.ui.xray.DrishtiXRayBottomSheet

data class BurstCandidate(
    val demoPhoto: DemoPhoto,
    val compositeScore: Float,
    val sharpnessScore: Float,
    val faceQualityScore: Float,
    val isWinner: Boolean,
    val rejectionReason: String?
)

data class BurstStack(
    val stackId: Long,
    val title: String,
    val captureTimestamp: String,
    val candidates: List<BurstCandidate>
)

/**
 * Feature C: "Pravah Smart Stacks" — Interactive Burst Best-Shot Arena.
 * Evaluates Composite Score = 0.55 * Sharpness + 0.45 * FaceQuality.
 */
@Composable
fun PravahSmartStacksView(
    onImageClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var inspectingPhoto by remember { mutableStateOf<DemoPhoto?>(null) }

    val burstDemos = remember { DemoSeedData.burstPhotos }

    var stacks by remember {
        mutableStateOf(
            listOf(
                BurstStack(
                    stackId = 1,
                    title = "Action Burst Sequence (4 shots)",
                    captureTimestamp = "Today, 17:42:10",
                    candidates = listOf(
                        BurstCandidate(
                            demoPhoto = burstDemos[0], // Winner (Peak Jump)
                            compositeScore = 94.6f,
                            sharpnessScore = 96.0f,
                            faceQualityScore = 93.0f,
                            isWinner = true,
                            rejectionReason = null
                        ),
                        BurstCandidate(
                            demoPhoto = burstDemos[1], // Motion Blur
                            compositeScore = 48.2f,
                            sharpnessScore = 38.2f,
                            faceQualityScore = 60.5f,
                            isWinner = false,
                            rejectionReason = "Drop • Blur 38.2"
                        ),
                        BurstCandidate(
                            demoPhoto = burstDemos[2], // Eyes Blinked
                            compositeScore = 42.1f,
                            sharpnessScore = 72.0f,
                            faceQualityScore = 14.0f,
                            isWinner = false,
                            rejectionReason = "Drop • Eyes 0.14"
                        ),
                        BurstCandidate(
                            demoPhoto = burstDemos[3], // Sub-optimal Angle
                            compositeScore = 58.4f,
                            sharpnessScore = 64.0f,
                            faceQualityScore = 52.0f,
                            isWinner = false,
                            rejectionReason = "Drop • Low Score"
                        )
                    )
                )
            )
        )
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        items(stacks, key = { it.stackId }) { stack ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .glass(
                        shape = RoundedCornerShape(22.dp),
                        backgroundColor = Color(0xDD141418),
                        borderWidth = 1.dp,
                        topBorderAlpha = 0.35f,
                        bottomBorderAlpha = 0.08f
                    )
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stack.title,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "${stack.captureTimestamp} • hamming(dHash) <= 8",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFFA1A1AA)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // 1-Click "Keep Best & Purge Rest"
                        Box(
                            modifier = Modifier
                                .glass(
                                    shape = RoundedCornerShape(12.dp),
                                    backgroundColor = Color(0x3310B981),
                                    borderWidth = 1.dp,
                                    topBorderAlpha = 0.40f
                                )
                                .clickable {
                                    val winner = stack.candidates.first { it.isWinner }
                                    Toast.makeText(
                                        context,
                                        "Kept Best Shot (${winner.demoPhoto.title}) and purged runner-ups",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    stacks = stacks.map {
                                        if (it.stackId == stack.stackId) {
                                            it.copy(candidates = listOf(winner))
                                        } else it
                                    }
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.DeleteSweep,
                                    contentDescription = null,
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Purge Rest",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF10B981),
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Horizontal Cards for Burst Candidates
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        stack.candidates.forEach { candidate ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .glass(
                                        shape = RoundedCornerShape(14.dp),
                                        backgroundColor = if (candidate.isWinner) Color(0x1F10B981) else Color.White.copy(alpha = 0.04f),
                                        borderWidth = if (candidate.isWinner) 2.dp else 1.dp,
                                        topBorderAlpha = if (candidate.isWinner) 0.60f else 0.20f
                                    )
                                    .clickable { inspectingPhoto = candidate.demoPhoto }
                                    .padding(6.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    // Visual Canvas Card
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(84.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                    ) {
                                        DemoPhotoCanvas(
                                            photo = candidate.demoPhoto,
                                            modifier = Modifier.fillMaxSize()
                                        )

                                        if (candidate.isWinner) {
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .padding(4.dp)
                                                    .background(Color(0xFF10B981), CircleShape)
                                                    .padding(3.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Outlined.Check,
                                                    contentDescription = "Best",
                                                    tint = Color.Black,
                                                    modifier = Modifier.size(10.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    // Math Badge
                                    Text(
                                        text = if (candidate.isWinner) "★ 95 pts" else "${candidate.compositeScore.toInt()} pts",
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        color = if (candidate.isWinner) Color(0xFF10B981) else Color.White
                                    )

                                    Text(
                                        text = candidate.rejectionReason ?: "Best Shot",
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = if (candidate.isWinner) Color(0xFF10B981) else Color(0xFFEF4444),
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (inspectingPhoto != null) {
        DrishtiXRayBottomSheet(
            photo = inspectingPhoto,
            onDismiss = { inspectingPhoto = null }
        )
    }
}
