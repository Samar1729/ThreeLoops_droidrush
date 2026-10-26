package com.threeloops.mirrorarchive.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.threeloops.mirrorarchive.data.demo.DemoPhoto
import com.threeloops.mirrorarchive.ui.theme.glass

/**
 * Procedural offline Canvas renderer for DemoPhotos.
 * Renders rich, high-fidelity visual cards (Receipt with tear lines and barcodes,
 * Portrait with facial mesh and lighting, Bursts with motion streak overlays,
 * and Documents/Boarding passes) without external network dependencies.
 */
@Composable
fun DemoPhotoCanvas(
    photo: DemoPhoto,
    modifier: Modifier = Modifier,
    showLandmarks: Boolean = false,
    showBoundingBoxes: Boolean = false
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.linearGradient(photo.gradientColors))
    ) {
        when (photo.category) {
            "RECEIPTS" -> ReceiptCanvasVisual(photo = photo)
            "PEOPLE" -> PortraitCanvasVisual(photo = photo, showLandmarks = showLandmarks)
            "BURSTS" -> BurstCanvasVisual(photo = photo)
            "DOCUMENTS" -> DocumentCanvasVisual(photo = photo)
            else -> GenericPhotoCanvasVisual(photo = photo)
        }

        // Bounding Box Overlay if requested (e.g., in Drishti X-Ray)
        if (showBoundingBoxes) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                if (photo.category == "PEOPLE" || photo.faces.isNotEmpty()) {
                    // Draw emerald bounding box around face
                    val boxLeft = w * 0.28f
                    val boxTop = h * 0.22f
                    val boxSize = w * 0.44f

                    drawRoundRect(
                        color = Color(0xFF10B981),
                        topLeft = Offset(boxLeft, boxTop),
                        size = Size(boxSize, boxSize * 1.15f),
                        cornerRadius = CornerRadius(12f, 12f),
                        style = Stroke(width = 4f)
                    )
                }

                if (photo.category == "RECEIPTS" || photo.ocrText != null) {
                    // Draw amber bounding boxes around text zones
                    val lines = listOf(
                        Offset(w * 0.12f, h * 0.22f) to Size(w * 0.76f, h * 0.12f),
                        Offset(w * 0.12f, h * 0.42f) to Size(w * 0.76f, h * 0.18f),
                        Offset(w * 0.12f, h * 0.68f) to Size(w * 0.76f, h * 0.12f)
                    )
                    lines.forEach { (pos, boxDim) ->
                        drawRoundRect(
                            color = Color(0xFFF59E0B),
                            topLeft = pos,
                            size = boxDim,
                            cornerRadius = CornerRadius(8f, 8f),
                            style = Stroke(
                                width = 3f,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReceiptCanvasVisual(photo: DemoPhoto) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Header: Merchant & Icon
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(end = 28.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .glass(
                                shape = CircleShape,
                                backgroundColor = Color.White.copy(alpha = 0.12f),
                                borderWidth = 1.dp,
                                topBorderAlpha = 0.40f
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = photo.iconVector,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = photo.merchant ?: "RETAIL STORE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = 0.5.sp,
                        maxLines = 1,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Dotted divider canvas
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
            ) {
                drawLine(
                    color = Color.White.copy(alpha = 0.25f),
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 2f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Simulated line items
            Text(
                text = photo.title,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFFE4E4E7),
                maxLines = 1
            )
            Text(
                text = "DATE: ${photo.dateFormatted}",
                fontSize = 8.sp,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFFA1A1AA)
            )
        }

        // Bottom section: Total, GSTIN, and Barcode Strip
        Column {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
            ) {
                drawLine(
                    color = Color.White.copy(alpha = 0.25f),
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 2f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    if (photo.gstin != null) {
                        Text(
                            text = "GSTIN: ${photo.gstin.take(10)}...",
                            fontSize = 8.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF10B981)
                        )
                    }
                    Text(
                        text = "TOTAL PAYABLE",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFA1A1AA)
                    )
                }

                Text(
                    text = "₹ ${"%,.2f".format(photo.totalAmount ?: 0.0)}",
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Procedural 1D Barcode Strip
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(18.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color.White.copy(alpha = 0.08f))
            ) {
                val barWidths = floatArrayOf(2f, 4f, 1f, 3f, 5f, 2f, 4f, 1f, 3f, 2f, 4f, 1f, 5f, 2f, 3f, 1f, 4f, 2f, 3f)
                var currentX = 12f
                var i = 0
                while (currentX < size.width - 12f) {
                    val w = barWidths[i % barWidths.size]
                    drawLine(
                        color = Color.White.copy(alpha = 0.70f),
                        start = Offset(currentX, 2f),
                        end = Offset(currentX, size.height - 2f),
                        strokeWidth = w
                    )
                    currentX += w + 4f
                    i++
                }
            }
        }
    }
}

@Composable
private fun PortraitCanvasVisual(photo: DemoPhoto, showLandmarks: Boolean) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Ambient radial light spot in canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerOffset = Offset(size.width * 0.5f, size.height * 0.42f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White.copy(alpha = 0.15f), Color.Transparent),
                    center = centerOffset,
                    radius = size.width * 0.45f
                )
            )
        }

        // Silhouette & Facial Landmarks
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .glass(
                        shape = CircleShape,
                        backgroundColor = Color.White.copy(alpha = 0.10f),
                        borderWidth = 1.dp,
                        topBorderAlpha = 0.50f
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )

                // Face Landmarks Simulation
                if (showLandmarks) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val eyeY = size.height * 0.38f
                        // Left eye dot
                        drawCircle(color = Color(0xFF10B981), radius = 3f, center = Offset(size.width * 0.38f, eyeY))
                        // Right eye dot
                        drawCircle(color = Color(0xFF10B981), radius = 3f, center = Offset(size.width * 0.62f, eyeY))
                        // Nose dot
                        drawCircle(color = Color(0xFF38BDF8), radius = 2.5f, center = Offset(size.width * 0.50f, size.height * 0.50f))
                        // Smile arc
                        drawArc(
                            color = Color(0xFF10B981),
                            startAngle = 20f,
                            sweepAngle = 140f,
                            useCenter = false,
                            topLeft = Offset(size.width * 0.36f, size.height * 0.56f),
                            size = Size(size.width * 0.28f, size.height * 0.16f),
                            style = Stroke(width = 2.5f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = photo.title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1
            )

            val face = photo.faces.firstOrNull()
            val smilePct = ((face?.smileProb ?: 0.90f) * 100).toInt()
            val eyesPct = ((face?.eyesOpenProb ?: 0.95f) * 100).toInt()

            Text(
                text = "Eyes $eyesPct% • Smile $smilePct%",
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFF10B981),
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
private fun BurstCanvasVisual(photo: DemoPhoto) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Motion Streak / Blur Simulation
        if (photo.rejectionReason?.contains("Motion Blur") == true) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val step = 14f
                for (offset in -100..400 step 20) {
                    drawLine(
                        color = Color.White.copy(alpha = 0.08f),
                        start = Offset(offset.toFloat(), 0f),
                        end = Offset(offset.toFloat() + 80f, size.height),
                        strokeWidth = 3f
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .glass(
                            shape = RoundedCornerShape(8.dp),
                            backgroundColor = if (photo.isWinner) Color(0x3310B981) else Color(0x33EF4444),
                            borderWidth = 1.dp,
                            topBorderAlpha = 0.50f
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (photo.isWinner) "BEST SHOT" else "RUNNER UP",
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = if (photo.isWinner) Color(0xFF10B981) else Color(0xFFEF4444)
                    )
                }

                Text(
                    text = "${photo.dateFormatted}",
                    fontSize = 8.sp,
                    color = Color(0xFFA1A1AA)
                )
            }

            // Central Icon
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(48.dp)
                    .glass(
                        shape = CircleShape,
                        backgroundColor = Color.White.copy(alpha = 0.10f),
                        borderWidth = 1.dp,
                        topBorderAlpha = 0.40f
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = photo.iconVector,
                    contentDescription = null,
                    tint = if (photo.isWinner) Color.White else Color(0xFFA1A1AA),
                    modifier = Modifier.size(24.dp)
                )
            }

            // Bottom Math Metrics
            Column {
                Text(
                    text = photo.title,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    maxLines = 1
                )
                Text(
                    text = "Sharpness: ${"%.1f".format(photo.blurScore)}",
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace,
                    color = if (photo.isWinner) Color(0xFF10B981) else Color(0xFFA1A1AA)
                )
            }
        }
    }
}

@Composable
private fun DocumentCanvasVisual(photo: DemoPhoto) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = photo.iconVector,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "DOCUMENT",
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFA1A1AA)
                )
            }

            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(Color(0xFF38BDF8), CircleShape)
            )
        }

        // Visual document layout: lines of text / code simulation
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = photo.title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1
            )

            // Simulated document lines
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(4.dp)
                    .background(Color.White.copy(alpha = 0.20f), RoundedCornerShape(2.dp))
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(4.dp)
                    .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(2.dp))
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(4.dp)
                    .background(Color.White.copy(alpha = 0.20f), RoundedCornerShape(2.dp))
            )
        }

        // Bottom OCR Snippet Chip
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .glass(
                    shape = RoundedCornerShape(8.dp),
                    backgroundColor = Color.Black.copy(alpha = 0.40f),
                    borderWidth = 1.dp,
                    topBorderAlpha = 0.30f
                )
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = (photo.ocrText?.lines()?.firstOrNull() ?: "TEXT EXTRACTED").take(24),
                fontSize = 8.sp,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFF38BDF8),
                maxLines = 1
            )
        }
    }
}

@Composable
private fun GenericPhotoCanvasVisual(photo: DemoPhoto) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = photo.iconVector,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.5f),
            modifier = Modifier.size(42.dp)
        )
    }
}
