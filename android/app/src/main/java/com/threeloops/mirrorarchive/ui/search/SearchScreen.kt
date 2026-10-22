package com.threeloops.mirrorarchive.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
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
import com.threeloops.mirrorarchive.search.SearchMode
import com.threeloops.mirrorarchive.search.SearchUiState
import com.threeloops.mirrorarchive.search.SearchViewModel

@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    onImageClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val query by viewModel.query.collectAsState()
    val mode by viewModel.mode.collectAsState()
    val suggestions by viewModel.suggestions.collectAsState()
    val uiState by viewModel.results.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Search",
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        // Segmented Control: Tags vs Text
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val isTags = mode == SearchMode.TAGS
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isTags) MaterialTheme.colorScheme.primary else Color.Transparent,
                modifier = Modifier
                    .weight(1f)
                    .clickable { viewModel.setMode(SearchMode.TAGS) }
            ) {
                Text(
                    text = "Tags",
                    fontSize = 12.sp,
                    fontWeight = if (isTags) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isTags) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(vertical = 7.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (!isTags) MaterialTheme.colorScheme.primary else Color.Transparent,
                modifier = Modifier
                    .weight(1f)
                    .clickable { viewModel.setMode(SearchMode.TEXT) }
            ) {
                Text(
                    text = "Text in Photos",
                    fontSize = 12.sp,
                    fontWeight = if (!isTags) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (!isTags) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(vertical = 7.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }

        // Apple Search Field
        OutlinedTextField(
            value = query,
            onValueChange = { viewModel.query.value = it },
            placeholder = {
                Text(
                    text = if (mode == SearchMode.TAGS) "Search by tag ('food', 'coffee', 'pet')..."
                    else "Search text in photos ('total', 'invoice')...",
                    fontSize = 14.sp
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedBorderColor = Color.Transparent
            ),
            modifier = Modifier.fillMaxWidth()
        )

        // Suggestion Chips
        if (suggestions.isNotEmpty()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                items(suggestions) { tag ->
                    SuggestionChip(
                        onClick = { viewModel.query.value = tag },
                        label = { Text(text = tag, fontSize = 11.sp) },
                        shape = CircleShape
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Results Section
        when (val state = uiState) {
            is SearchUiState.Idle -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Search photos, text, and labels",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                    )
                }
            }
            is SearchUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                }
            }
            is SearchUiState.Empty -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No results found for \"$query\"",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                    )
                }
            }
            is SearchUiState.Results -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(state.items) { item ->
                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .background(Color(0xFF262626))
                                .clickable { onImageClick(item.id) }
                        ) {
                            AsyncImage(
                                model = item.uri,
                                contentDescription = item.displayName,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }
    }
}
