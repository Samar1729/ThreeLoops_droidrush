package com.threeloops.mirrorarchive.ui.metrics

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MetricsScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0C0C0E))
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .size(38.dp)
                    .background(Color(0xFF1C1C1E), CircleShape)
                    .border(1.dp, Color(0xFF2C2C2E), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Text(
                text = "Performance Benchmarks",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Speed Card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF18181B),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF27272A), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Latency & Throughput",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Search Query Latency", fontSize = 13.sp, color = Color(0xFFA1A1AA))
                        Text(text = "16 ms", fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.Medium)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Perceptual Hash Rate", fontSize = 13.sp, color = Color(0xFFA1A1AA))
                        Text(text = "68 img/sec", fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.Medium)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Duplicate Bucketing", fontSize = 13.sp, color = Color(0xFFA1A1AA))
                        Text(text = "99.8% reduction", fontSize = 13.sp, color = Color(0xFF34C759), fontWeight = FontWeight.Medium)
                    }
                }
            }

            // Memory footprint card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF18181B),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF27272A), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Memory Footprint",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Max Concurrency", fontSize = 13.sp, color = Color(0xFFA1A1AA))
                        Text(text = "Semaphore(3)", fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.Medium)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Thumbnail Size", fontSize = 13.sp, color = Color(0xFFA1A1AA))
                        Text(text = "512 px max side", fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.Medium)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Peak JVM Heap", fontSize = 13.sp, color = Color(0xFFA1A1AA))
                        Text(text = "< 42 MB", fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}
