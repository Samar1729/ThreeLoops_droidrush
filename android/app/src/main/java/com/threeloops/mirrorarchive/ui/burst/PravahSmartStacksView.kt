package com.threeloops.mirrorarchive.ui.burst

import android.content.ActivityNotFoundException
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.threeloops.mirrorarchive.contracts.ImageItem
import com.threeloops.mirrorarchive.contracts.displayName
import com.threeloops.mirrorarchive.dupes.BurstSequence
import com.threeloops.mirrorarchive.dupes.BurstUiState
import com.threeloops.mirrorarchive.dupes.BurstViewModel
import com.threeloops.mirrorarchive.dupes.PurgeUiState
import com.threeloops.mirrorarchive.dupes.PurgeViewModel
import com.threeloops.mirrorarchive.ui.theme.glass
import java.text.DateFormat
import java.util.Date

@Composable
fun PravahSmartStacksView(
    onImageClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BurstViewModel = hiltViewModel(),
    purgeViewModel: PurgeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val purgeState by purgeViewModel.state.collectAsState()
    var actionError by remember { mutableStateOf<String?>(null) }
    val deleteRequestLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        purgeViewModel.onResult(result.resultCode)
    }

    LaunchedEffect(purgeState) {
        if (purgeState is PurgeUiState.Done) viewModel.scan()
    }

    Column(modifier = modifier.fillMaxSize()) {
        actionError?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
        when (val currentState = state) {
            BurstUiState.Loading -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }

            is BurstUiState.Error -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(currentState.message, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(12.dp))
                Button(onClick = viewModel::scan) { Text("Retry") }
            }

            is BurstUiState.Results -> if (currentState.bursts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No burst sequences found.", color = Color(0xFFA1A1AA))
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 100.dp)
                ) {
                    items(currentState.bursts, key = { it.id }) { burst ->
                        BurstSequenceCard(
                            burst = burst,
                            isPurging = purgeState is PurgeUiState.Purging,
                            onImageClick = onImageClick,
                            onKeepBest = {
                                actionError = null
                                try {
                                    val plan = viewModel.keepBestPurgeRest(burst.imageIds)
                                    val intentSender = purgeViewModel.createRequest(plan)
                                    deleteRequestLauncher.launch(
                                        IntentSenderRequest.Builder(intentSender).build()
                                    )
                                } catch (exception: IllegalArgumentException) {
                                    actionError = exception.message
                                        ?: "Unable to request photo deletion."
                                } catch (exception: SecurityException) {
                                    actionError = exception.message
                                        ?: "Photo deletion permission is unavailable."
                                } catch (exception: ActivityNotFoundException) {
                                    actionError = exception.message
                                        ?: "Unable to request photo deletion."
                                }
                            }
                        )
                    }
                }
            }
        }
        when (val result = purgeState) {
            is PurgeUiState.Done -> Text(
                text = "Deleted ${result.deletedCount} photos · Freed ${formatSize(result.freedBytes)}",
                color = Color(0xFF10B981),
                modifier = Modifier.padding(16.dp)
            )
            is PurgeUiState.Error -> Text(
                text = result.message,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(16.dp)
            )
            else -> Unit
        }
    }
}

@Composable
private fun BurstSequenceCard(
    burst: BurstSequence,
    isPurging: Boolean,
    onImageClick: (Long) -> Unit,
    onKeepBest: () -> Unit
) {
    val captureTime = burst.imageIds.firstNotNullOfOrNull { burst.imageMap[it]?.dateTaken }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Color.White)
            .border(1.dp, Color(0x2210B981), RoundedCornerShape(22.dp))
            .padding(16.dp)
    ) {
        Text(
            text = "Burst sequence · ${burst.imageIds.size} photos",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF064E3B)
        )
        captureTime?.let {
            Text(
                text = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
                    .format(Date(it)),
                fontSize = 11.sp,
                color = Color(0xFF64748B)
            )
        }
        Spacer(Modifier.height(12.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(burst.imageIds, key = { it }) { id ->
                burst.imageMap[id]?.let { image ->
                    BurstPhoto(
                        image = image,
                        isWinner = id == burst.bestId,
                        onClick = { onImageClick(id) }
                    )
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = onKeepBest,
            enabled = !isPurging,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF059669),
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isPurging) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = Color.White
                )
                Spacer(Modifier.width(8.dp))
            } else {
                Icon(Icons.Outlined.DeleteSweep, contentDescription = null)
                Spacer(Modifier.width(8.dp))
            }
            Text(
                text = "Keep Best & Purge ${burst.imageIds.size - 1} Others",
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun BurstPhoto(
    image: ImageItem,
    isWinner: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(132.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (isWinner) Color(0xFFD1FAE5) else Color(0xFFF1F5F9))
            .border(
                width = if (isWinner) 2.dp else 1.dp,
                color = if (isWinner) Color(0xFF059669) else Color(0x2210B981),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
            .padding(6.dp)
    ) {
        AsyncImage(
            model = image.uri,
            contentDescription = if (isWinner) "Best shot" else image.displayName,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(112.dp)
                .clip(RoundedCornerShape(10.dp))
        )
        Text(
            text = if (isWinner) "BEST SHOT" else "Blur ${image.blurScore.toInt()}",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = if (isWinner) Color(0xFF065F46) else Color(0xFF64748B),
            modifier = Modifier.padding(top = 6.dp, start = 2.dp, bottom = 2.dp)
        )
    }
}

private fun formatSize(bytes: Long): String =
    "%.1f MB".format(bytes / (1024.0 * 1024.0))
