package com.threeloops.mirrorarchive.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.threeloops.mirrorarchive.search.SearchMode
import com.threeloops.mirrorarchive.search.SearchUiState
import com.threeloops.mirrorarchive.search.SearchViewModel
import com.threeloops.mirrorarchive.ui.theme.GlassScaffoldBackground
import com.threeloops.mirrorarchive.ui.theme.TextMuted
import com.threeloops.mirrorarchive.ui.theme.TextPrimary
import com.threeloops.mirrorarchive.ui.theme.TextSecondary
import com.threeloops.mirrorarchive.ui.theme.glass

@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    onImageClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val query by viewModel.query.collectAsStateWithLifecycle()
    val mode by viewModel.mode.collectAsStateWithLifecycle()
    val suggestions by viewModel.suggestions.collectAsStateWithLifecycle()
    val uiState by viewModel.results.collectAsStateWithLifecycle()

    GlassScaffoldBackground {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Title
            Text(
                text = "Search",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                letterSpacing = (-0.5).sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Frosted Glass Search Input Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .glass(
                        shape = RoundedCornerShape(22.dp),
                        backgroundColor = Color.White.copy(alpha = 0.08f),
                        topBorderAlpha = 0.35f,
                        bottomBorderAlpha = 0.08f
                    )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
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
                        onValueChange = { viewModel.query.value = it },
                        placeholder = {
                            Text(
                                text = if (mode == SearchMode.TAGS) "Search tags, categories, objects..." else "Search text inside photos (OCR)...",
                                fontSize = 14.sp,
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
                            cursorColor = TextPrimary
                        ),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    if (query.isNotEmpty()) {
                        IconButton(
                            onClick = { viewModel.query.value = "" },
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

            // Frosted Glass Mode Selector: Tags vs Text (OCR)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .glass(
                        shape = RoundedCornerShape(14.dp),
                        backgroundColor = Color.White.copy(alpha = 0.05f),
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
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Tags",
                        fontSize = 13.sp,
                        fontWeight = if (isTags) FontWeight.SemiBold else FontWeight.Medium,
                        color = if (isTags) TextPrimary else TextSecondary
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(11.dp))
                        .background(if (!isTags) Color.White.copy(alpha = 0.16f) else Color.Transparent)
                        .clickable { viewModel.setMode(SearchMode.TEXT) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Text (OCR)",
                        fontSize = 13.sp,
                        fontWeight = if (!isTags) FontWeight.SemiBold else FontWeight.Medium,
                        color = if (!isTags) TextPrimary else TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Frosted Glass Suggestion Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(suggestions) { tag ->
                    Box(
                        modifier = Modifier
                            .glass(
                                shape = RoundedCornerShape(16.dp),
                                backgroundColor = Color.White.copy(alpha = 0.06f),
                                topBorderAlpha = 0.25f,
                                bottomBorderAlpha = 0.06f
                            )
                            .clickable { viewModel.query.value = tag }
                            .padding(horizontal = 14.dp, vertical = 7.dp)
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

            Spacer(modifier = Modifier.height(14.dp))

            // Results Section
            when (val state = uiState) {
                is SearchUiState.Idle -> {
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
                                    imageVector = Icons.Outlined.Search,
                                    contentDescription = null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "Search by content or text",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Find receipts, foods, screenshots, or Latin text",
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                        }
                    }
                }

                is SearchUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = TextPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                is SearchUiState.Results -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${state.items.size} results",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextSecondary
                            )
                            Text(
                                text = "${state.latencyMs} ms",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }

                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            contentPadding = PaddingValues(bottom = 100.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(state.items, key = { it.id }) { image ->
                                Box(
                                    modifier = Modifier
                                        .aspectRatio(1f)
                                        .glass(
                                            shape = RoundedCornerShape(10.dp),
                                            backgroundColor = Color.White.copy(alpha = 0.05f),
                                            topBorderAlpha = 0.22f,
                                            bottomBorderAlpha = 0.05f
                                        )
                                        .clickable { onImageClick(image.id) }
                                ) {
                                    AsyncImage(
                                        model = image.uri,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                        }
                    }
                }

                is SearchUiState.Empty -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 70.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "No results found",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Try searching for a different keyword or toggle mode",
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                        }
                    }
                }
            }
        }
    }
}
