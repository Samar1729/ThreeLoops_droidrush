package com.threeloops.mirrorarchive.ui.ledger

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Storefront
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.threeloops.mirrorarchive.ui.theme.AmbientMeshBox
import com.threeloops.mirrorarchive.ui.theme.glass

@Composable
fun RaseedLedgerScreen(
    viewModel: RaseedLedgerViewModel,
    onReceiptClick: (Long) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val summary by viewModel.summary.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showExportDialog by remember { mutableStateOf(false) }

    AmbientMeshBox(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Top Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Raseed Ledger",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "Zero-Cloud GST & Expense Extractor",
                        fontSize = 12.sp,
                        color = Color(0xFFA1A1AA)
                    )
                }

                // Export CSV Button
                Box(
                    modifier = Modifier
                        .glass(
                            shape = RoundedCornerShape(14.dp),
                            backgroundColor = Color.White.copy(alpha = 0.08f),
                            borderWidth = 1.dp,
                            topBorderAlpha = 0.35f,
                            bottomBorderAlpha = 0.08f
                        )
                        .clickable {
                            showExportDialog = true
                        }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Download,
                            contentDescription = "Export CSV",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Export",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Spotify-Wrapped FinTech Summary Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .glass(
                        shape = RoundedCornerShape(22.dp),
                        backgroundColor = Color(0xDD141418),
                        borderWidth = 1.dp,
                        topBorderAlpha = 0.40f,
                        bottomBorderAlpha = 0.08f
                    )
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TOTAL TRACKED EXPENSES",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.sp,
                            color = Color(0xFFA1A1AA)
                        )

                        Box(
                            modifier = Modifier
                                .glass(
                                    shape = RoundedCornerShape(8.dp),
                                    backgroundColor = Color(0x3310B981),
                                    borderWidth = 1.dp,
                                    topBorderAlpha = 0.45f,
                                    bottomBorderAlpha = 0.10f
                                )
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "100% OFFLINE",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF10B981)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "₹ ${"%,.2f".format(summary.totalSpent)}",
                        fontSize = 32.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "Invoices", fontSize = 11.sp, color = Color(0xFFA1A1AA))
                            Text(
                                text = "${summary.receiptCount}",
                                fontSize = 14.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }

                        Column {
                            Text(text = "Est. GST Credit (ITC)", fontSize = 11.sp, color = Color(0xFFA1A1AA))
                            Text(
                                text = "₹ ${"%,.2f".format(summary.totalGstClaimable)}",
                                fontSize = 14.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF10B981)
                            )
                        }

                        Column {
                            Text(text = "Security", fontSize = 11.sp, color = Color(0xFFA1A1AA))
                            Text(
                                text = "Encrypted DB",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Recent Invoices & Receipts",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Invoices List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                items(summary.items, key = { it.id }) { raseed ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .glass(
                                shape = RoundedCornerShape(16.dp),
                                backgroundColor = Color.White.copy(alpha = 0.05f),
                                borderWidth = 1.dp,
                                topBorderAlpha = 0.25f,
                                bottomBorderAlpha = 0.05f
                            )
                            .clickable {
                                onReceiptClick(raseed.id)
                            }
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .glass(
                                            shape = CircleShape,
                                            backgroundColor = Color.White.copy(alpha = 0.08f),
                                            borderWidth = 1.dp,
                                            topBorderAlpha = 0.30f
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Storefront,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(
                                        text = raseed.merchant,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White,
                                        maxLines = 1
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = raseed.date,
                                            fontSize = 11.sp,
                                            color = Color(0xFFA1A1AA)
                                        )
                                        if (raseed.gstin != null) {
                                            Text(
                                                text = " • ",
                                                fontSize = 11.sp,
                                                color = Color(0xFFA1A1AA)
                                            )
                                            Text(
                                                text = raseed.gstin,
                                                fontSize = 10.sp,
                                                fontFamily = FontFamily.Monospace,
                                                color = Color(0xFF10B981)
                                            )
                                        }
                                    }
                                }
                            }

                            // Amount
                            Text(
                                text = "₹ ${"%,.2f".format(raseed.totalAmount)}",
                                fontSize = 15.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }

    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = {
                Text(text = "Offline CSV Export", fontWeight = FontWeight.Bold, color = Color.White)
            },
            text = {
                Text(
                    text = "The ledger will be exported locally to your device storage.\n\nSummary:\n• ${summary.receiptCount} receipts\n• Total: ₹ ${"%,.2f".format(summary.totalSpent)}\n• 0 bytes uploaded to external servers.",
                    color = Color(0xFFA1A1AA)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val csv = viewModel.exportCsv()
                        Toast.makeText(context, "Exported ${summary.receiptCount} rows to local storage", Toast.LENGTH_SHORT).show()
                        showExportDialog = false
                    }
                ) {
                    Text(text = "Confirm Export", color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text(text = "Cancel", color = Color.White)
                }
            },
            containerColor = Color(0xFF18181B)
        )
    }
}
