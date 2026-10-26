package com.threeloops.mirrorarchive.ui.search

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
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.threeloops.mirrorarchive.data.demo.DemoPhoto
import com.threeloops.mirrorarchive.data.demo.DemoSeedData
import com.threeloops.mirrorarchive.search.SearchMode
import com.threeloops.mirrorarchive.search.SearchViewModel
import com.threeloops.mirrorarchive.ui.components.DemoPhotoCanvas
import com.threeloops.mirrorarchive.ui.theme.AmbientMeshBox
import com.threeloops.mirrorarchive.ui.theme.TextMuted
import com.threeloops.mirrorarchive.ui.theme.TextPrimary
import com.threeloops.mirrorarchive.ui.theme.TextSecondary
import com.threeloops.mirrorarchive.ui.theme.glass
import com.threeloops.mirrorarchive.ui.xray.DrishtiXRayBottomSheet

@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    onImageClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val query by viewModel.query.collectAsStateWithLifecycle()
    val mode by viewModel.mode.collectAsStateWithLifecycle()
    val latencyMs by viewModel.lastLatencyMs.collectAsStateWithLifecycle()

    var activeChip by remember { mutableStateOf("receipt") }
    var inspectingPhoto by remember { mutableStateOf<DemoPhoto?>(null) }

    // Quick filter chips
    val filterChips = remember {
        listOf("all", "receipt", "invoice", "coffee", "fuel", "food", "portrait", "screenshot")
    }

    // Filter demo photos deterministically
    val matchedPhotos = remember(query, mode, activeChip) {
        val effectiveQuery = if (query.isNotEmpty()) query else if (activeChip != "all") activeChip else ""
        if (effectiveQuery.isEmpty()) {
            if (mode == SearchMode.TEXT) {
                DemoSeedData.photos.filter { it.ocrText != null }
            } else {
                DemoSeedData.photos
            }
        } else {
            DemoSeedData.filterByQuery(effectiveQuery, modeIsText = (mode == SearchMode.TEXT))
        }
    }

    AmbientMeshBox {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Title & SearchTimer Latency Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Runic Search",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "Offline Tokenized Tags & Latin OCR",
                        fontSize = 11.sp,
                        color = Color(0xFFA1A1AA)
                    )
                }

                // Latency Badge
                Box(
                    modifier = Modifier
                        .glass(
                            shape = RoundedCornerShape(10.dp),
                            backgroundColor = Color(0x3310B981),
                            borderWidth = 1.dp,
                            topBorderAlpha = 0.45f
                        )
                        .padding(horizontal = 9.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${latencyMs.coerceAtLeast(8L)}ms",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF10B981)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Frosted Glass Search Input Bar
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
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = "Search",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    TextField(
                        value = query,
                        onValueChange = {
                            viewModel.query.value = it
                            if (it.isNotEmpty()) activeChip = ""
                        },
                        placeholder = {
                            Text(
                                text = if (mode == SearchMode.TAGS) "Search tags, objects, coffee..." else "Search OCR text, GSTIN, amount...",
                                fontSize = 13.sp,
                                color = TextMuted
                            )
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            cursorColor = Color(0xFF10B981)
                        ),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    if (query.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                viewModel.query.value = ""
                                activeChip = "all"
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = "Clear",
                                tint = TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Segmented Mode Selector: Tags vs Text (OCR)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .glass(
                        shape = RoundedCornerShape(14.dp),
                        backgroundColor = Color.White.copy(alpha = 0.05f),
                        borderWidth = 1.dp,
                        topBorderAlpha = 0.20f,
                        bottomBorderAlpha = 0.05f
                    )
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val isTags = mode == SearchMode.TAGS
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(11.dp))
                        .background(if (isTags) Color.White.copy(alpha = 0.16f) else Color.Transparent)
                        .clickable { viewModel.setMode(SearchMode.TAGS) }
                        .padding(vertical = 7.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Tags (${matchedPhotos.size})",
                        fontSize = 12.sp,
                        fontWeight = if (isTags) FontWeight.SemiBold else FontWeight.Medium,
                        color = if (isTags) TextPrimary else TextSecondary
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(11.dp))
                        .background(if (!isTags) Color(0x3310B981) else Color.Transparent)
                        .clickable { viewModel.setMode(SearchMode.TEXT) }
                        .padding(vertical = 7.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Text OCR (${matchedPhotos.filter { it.ocrText != null }.size})",
                        fontSize = 12.sp,
                        fontWeight = if (!isTags) FontWeight.Bold else FontWeight.Medium,
                        color = if (!isTags) Color(0xFF10B981) else TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Frosted Glass Quick Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 2.dp)
            ) {
                items(filterChips) { chip ->
                    val isSelected = activeChip == chip
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (isSelected) Color(0x3310B981) else Color.White.copy(alpha = 0.06f)
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSelected) Color(0xFF10B981) else Color.White.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable {
                                activeChip = chip
                                viewModel.query.value = ""
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = chip,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color(0xFF10B981) else TextPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Results Section
            if (matchedPhotos.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 70.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No matching photos found",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Try 'receipt', 'coffee', 'shell', or 'portrait'",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }
                }
            } else if (mode == SearchMode.TEXT) {
                // OCR Mode: 2-Column Grid with OCR Snippet Preview Cards
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 110.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(matchedPhotos, key = { it.id }) { photo ->
                        OcrResultCard(
                            photo = photo,
                            query = if (query.isNotEmpty()) query else activeChip,
                            onClick = { inspectingPhoto = photo }
                        )
                    }
                }
            } else {
                // Tags Mode: 2-Column Grid with Category Chips
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 110.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(matchedPhotos, key = { it.id }) { photo ->
                        TagResultCard(
                            photo = photo,
                            onClick = { inspectingPhoto = photo }
                        )
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
private fun OcrResultCard(
    photo: DemoPhoto,
    query: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .glass(
                shape = RoundedCornerShape(16.dp),
                backgroundColor = Color(0xDD141418),
                borderWidth = 1.dp,
                topBorderAlpha = 0.35f,
                bottomBorderAlpha = 0.08f
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() }
    ) {
        // Photo Visual
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
        ) {
            DemoPhotoCanvas(
                photo = photo,
                modifier = Modifier.fillMaxSize()
            )

            // Top-right X-Ray Pill
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .glass(
                        shape = CircleShape,
                        backgroundColor = Color.Black.copy(alpha = 0.40f),
                        borderWidth = 1.dp,
                        topBorderAlpha = 0.35f
                    )
                    .padding(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Visibility,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(10.dp)
                )
            }
        }

        // OCR Snippet Preview Card with Highlighted Match Spans
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "OCR MATCH",
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF10B981)
                )

                if (photo.totalAmount != null) {
                    Text(
                        text = "₹ ${"%,.2f".format(photo.totalAmount)}",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Highlighted OCR Snippet Text
            val snippet = remember(photo, query) {
                val lines = photo.ocrText?.lines() ?: emptyList()
                val matchLine = lines.firstOrNull { it.contains(query, ignoreCase = true) }
                    ?: lines.firstOrNull { it.contains("GSTIN", ignoreCase = true) }
                    ?: lines.firstOrNull { it.contains("TOTAL", ignoreCase = true) }
                    ?: lines.firstOrNull() ?: "MATCHED OCR"
                matchLine.trim()
            }

            Text(
                text = buildAnnotatedString {
                    val lowerSnippet = snippet.lowercase()
                    val lowerQ = query.lowercase().trim()
                    val startIndex = if (lowerQ.isNotEmpty()) lowerSnippet.indexOf(lowerQ) else -1
                    if (startIndex != -1) {
                        append(snippet.substring(0, startIndex))
                        withStyle(style = SpanStyle(color = Color(0xFF10B981), fontWeight = FontWeight.Bold)) {
                            append(snippet.substring(startIndex, startIndex + lowerQ.length))
                        }
                        append(snippet.substring(startIndex + lowerQ.length))
                    } else {
                        withStyle(style = SpanStyle(color = Color(0xFFE4E4E7))) {
                            append(snippet)
                        }
                    }
                },
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                maxLines = 2,
                lineHeight = 13.sp
            )
        }
    }
}

@Composable
private fun TagResultCard(
    photo: DemoPhoto,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(170.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(Color.White.copy(alpha = 0.22f), Color.White.copy(alpha = 0.04f))
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() }
    ) {
        DemoPhotoCanvas(
            photo = photo,
            modifier = Modifier.fillMaxSize()
        )

        // Gradient Scrim
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.80f)),
                        startY = 180f
                    )
                )
        )

        // Primary Tag Pill
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(8.dp)
                .glass(
                    shape = RoundedCornerShape(8.dp),
                    backgroundColor = Color(0xD0101014),
                    borderWidth = 1.dp,
                    topBorderAlpha = 0.35f
                )
                .padding(horizontal = 7.dp, vertical = 3.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = photo.primaryLabel,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${photo.primaryConfidence}",
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFF10B981)
                )
            }
        }
    }
}
