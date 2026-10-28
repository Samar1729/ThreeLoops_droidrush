package com.threeloops.mirrorarchive.ui.search

import android.widget.Toast
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
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.threeloops.mirrorarchive.data.demo.DisplayPhoto
import com.threeloops.mirrorarchive.search.SearchMode
import com.threeloops.mirrorarchive.search.SearchViewModel
import com.threeloops.mirrorarchive.ui.theme.AmbientMeshBox
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

    var activeChip by remember { mutableStateOf("all") }
    var inspectingPhoto by remember { mutableStateOf<DisplayPhoto?>(null) }

    // Quick filter chips
    val filterChips = remember {
        listOf("all", "receipt", "invoice", "coffee", "fuel", "portraits", "documents")
    }

    val matchedPhotos by viewModel.searchPhotos.collectAsStateWithLifecycle()

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
                        color = Color(0xFF064E3B),
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "Offline Tokenized Tags & Latin OCR",
                        fontSize = 11.sp,
                        color = Color(0xFF059669)
                    )
                }

                // Monospace Latency Badge
                Box(
                    modifier = Modifier
                        .background(Color(0xFFD1FAE5), RoundedCornerShape(10.dp))
                        .border(1.dp, Color(0x3310B981), RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "${latencyMs.coerceAtLeast(8L)}ms",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF065F46)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Green & White Search Input Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0x3310B981), RoundedCornerShape(22.dp))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = "Search",
                        tint = Color(0xFF059669),
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
                                color = Color(0xFF94A3B8)
                            )
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = Color(0xFF0F172A),
                            unfocusedTextColor = Color(0xFF0F172A),
                            cursorColor = Color(0xFF059669)
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
                                tint = Color(0xFF64748B),
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
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFE8F5E9))
                    .border(1.dp, Color(0x3310B981), RoundedCornerShape(14.dp))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val isTags = mode == SearchMode.TAGS
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(11.dp))
                        .background(if (isTags) Color(0xFF059669) else Color.Transparent)
                        .clickable { viewModel.setMode(SearchMode.TAGS) }
                        .padding(vertical = 7.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Tags (${matchedPhotos.size})",
                        fontSize = 12.sp,
                        fontWeight = if (isTags) FontWeight.Bold else FontWeight.Medium,
                        color = if (isTags) Color.White else Color(0xFF475569)
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(11.dp))
                        .background(if (!isTags) Color(0xFF059669) else Color.Transparent)
                        .clickable { viewModel.setMode(SearchMode.TEXT) }
                        .padding(vertical = 7.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Text OCR (${matchedPhotos.filter { it.rawOcrText != null }.size})",
                        fontSize = 12.sp,
                        fontWeight = if (!isTags) FontWeight.Bold else FontWeight.Medium,
                        color = if (!isTags) Color.White else Color(0xFF475569)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Green & White Quick Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 2.dp)
            ) {
                items(filterChips) { chip ->
                    val isSelected = activeChip == chip
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSelected) Color(0xFF059669) else Color.White)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) Color(0xFF059669) else Color(0x3310B981),
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable {
                                activeChip = chip
                                viewModel.query.value = if (chip == "all") "" else chip
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = chip,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else Color(0xFF334155)
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
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF064E3B)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Try 'receipt', 'coffee', 'shell', or 'portrait'",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            } else if (mode == SearchMode.TEXT) {
                // OCR Mode: 2-Column Grid with Copyable OCR Text Cards
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
                            query = if (query.isNotEmpty()) query else if (activeChip != "all") activeChip else "",
                            onClick = { inspectingPhoto = photo }
                        )
                    }
                }
            } else {
                // Tags Mode: 2-Column Grid
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
    photo: DisplayPhoto,
    query: String,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val rawText = photo.rawOcrText ?: photo.ocrSnippet ?: ""

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
    ) {
        // Real Photo Visual
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .background(Color(0xFFE2E8F0))
        ) {
            AsyncImage(
                model = if (photo.drawableRes != 0) photo.drawableRes else photo.imageUrl,
                contentDescription = photo.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Top-right X-Ray Pill
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
        }

        // OCR Extracted Content with Copy-Paste Support
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "OCR EXTRACTED",
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF059669)
                )

                // Dedicated "Copy Text" button
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFD1FAE5))
                        .clickable {
                            if (rawText.isNotEmpty()) {
                                clipboardManager.setText(AnnotatedString(rawText))
                                Toast.makeText(context, "Copied OCR text to clipboard!", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ContentCopy,
                        contentDescription = "Copy OCR",
                        tint = Color(0xFF065F46),
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "Copy",
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF065F46)
                    )
                }
            }

            if (photo.totalAmount != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Total: ${photo.totalAmount}",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF064E3B)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Highlighted & Selectable OCR Text
            val snippet = remember(photo, query) {
                val lines = rawText.lines().filter { it.isNotBlank() }
                val matchLine = if (query.isNotEmpty()) {
                    lines.firstOrNull { it.contains(query, ignoreCase = true) }
                } else null
                (matchLine
                    ?: lines.firstOrNull { it.contains("GSTIN", ignoreCase = true) }
                    ?: lines.firstOrNull { it.contains("TOTAL", ignoreCase = true) }
                    ?: lines.firstOrNull { it.contains("RS.", ignoreCase = true) }
                    ?: lines.firstOrNull() ?: rawText).trim()
            }

            val highlightTarget = remember(snippet, query) {
                if (query.isNotEmpty() && snippet.contains(query, ignoreCase = true)) {
                    query
                } else {
                    listOf("GSTIN", "TOTAL", "RS.", "INR", "BELLMAN-FORD", "CONSENSUS", "COROUTINES", "TECH KRITI")
                        .firstOrNull { snippet.contains(it, ignoreCase = true) } ?: ""
                }
            }

            // SelectionContainer enables cursor selection and direct copy/paste
            SelectionContainer {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF1F8F4), RoundedCornerShape(6.dp))
                        .padding(6.dp)
                ) {
                    Text(
                        text = buildAnnotatedString {
                            val lowerSnippet = snippet.lowercase()
                            val lowerTarget = highlightTarget.lowercase().trim()
                            val startIndex = if (lowerTarget.isNotEmpty()) lowerSnippet.indexOf(lowerTarget) else -1
                            if (startIndex != -1) {
                                append(snippet.substring(0, startIndex))
                                withStyle(style = SpanStyle(color = Color(0xFF059669), fontWeight = FontWeight.Bold)) {
                                    append(snippet.substring(startIndex, startIndex + lowerTarget.length))
                                }
                                append(snippet.substring(startIndex + lowerTarget.length))
                            } else {
                                withStyle(style = SpanStyle(color = Color(0xFF334155))) {
                                    append(snippet)
                                }
                            }
                        },
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 3,
                        lineHeight = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun TagResultCard(
    photo: DisplayPhoto,
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

            // Primary Tag Pill in top-right
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .background(Color(0xF0059669), RoundedCornerShape(10.dp))
                    .padding(horizontal = 7.dp, vertical = 3.dp)
            ) {
                Text(
                    text = photo.tag,
                    fontSize = 10.sp,
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