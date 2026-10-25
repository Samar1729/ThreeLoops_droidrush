package com.threeloops.mirrorarchive.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onNavigateMetrics: () -> Unit,
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
                text = "Settings",
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
            // Privacy Verification Card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF18181B),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF27272A), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Shield,
                            contentDescription = null,
                            tint = Color(0xFF34C759),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Privacy & Verification",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Network Permission: None (0 INTERNET)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFE4E4E7)
                    )
                    Text(
                        text = "Encrypted local SQLite with SQLCipher (AES-256). Every model and index runs 100% on-device.",
                        fontSize = 12.sp,
                        color = Color(0xFFA1A1AA),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            // Storage Card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF18181B),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF27272A), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Index Database",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Total Indexed", fontSize = 13.sp, color = Color(0xFFA1A1AA))
                        Text(text = "128 Photos", fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.Medium)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Index Size", fontSize = 13.sp, color = Color(0xFFA1A1AA))
                        Text(text = "3.8 MB", fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.Medium)
                    }
                }
            }

            // Telemetry & Benchmarks link
            Button(
                onClick = onNavigateMetrics,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF27272A)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "View Performance Benchmarks",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
            }
        }
    }
}
