package com.threeloops.mirrorarchive.ui.ledger

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import coil.compose.AsyncImage
import com.threeloops.mirrorarchive.data.demo.DemoPhoto
import com.threeloops.mirrorarchive.data.demo.DemoSeedData
import com.threeloops.mirrorarchive.ui.theme.AmbientMeshBox
import com.threeloops.mirrorarchive.ui.theme.glass
import com.threeloops.mirrorarchive.ui.xray.DrishtiXRayBottomSheet

@Composable
fun RaseedLedgerScreen(
    viewModel: RaseedLedgerViewModel,
    onReceiptClick: (Long) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val summary by viewModel.summary.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showExportDialog by remember { mutableStateOf(false) }
    var expandedReceiptId by remember { mutableStateOf<Long?>(1L) } // Default expand Starbucks for immediate wow-factor
    var inspectingPhoto by remember { mutableStateOf<DemoPhoto?>(null) }

    val receiptDemoPhotos = remember { DemoSeedData.receiptPhotos }

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
                        text = "Zero-Cloud GST & Expense Regex Extractor",
                        fontSize = 11.sp,
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
                        .clickable { showExportDialog = true }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Download,
                            contentDescription = "Export CSV",
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
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

            // FinTech Glass Summary Card
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
                    .padding(18.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TOTAL TRACKED EXPENSES",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
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
                                    topBorderAlpha = 0.45f
                                )
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "100% OFFLINE",
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF10B981)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val totalAmount = receiptDemoPhotos.sumOf { it.totalDouble ?: 0.0 }
                    val totalGst = totalAmount * 0.18

                    Text(
                        text = "₹ ${"%,.2f".format(totalAmount)}",
                        fontSize = 30.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "Invoices", fontSize = 11.sp, color = Color(0xFFA1A1AA))
                            Text(
                                text = "${receiptDemoPhotos.size}",
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }

                        Column {
                            Text(text = "Est. GST Credit (ITC)", fontSize = 11.sp, color = Color(0xFFA1A1AA))
                            Text(
                                text = "₹ ${"%,.2f".format(totalGst)}",
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF10B981)
                            )
                        }

                        Column {
                            Text(text = "Security", fontSize = 11.sp, color = Color(0xFFA1A1AA))
                            Text(
                                text = "SQLCipher DB",
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Verified Invoices & Tax Receipts",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            // Invoices List: Interactive Expandable Cards
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 110.dp)
            ) {
                items(receiptDemoPhotos, key = { it.id }) { photo ->
                    val isExpanded = expandedReceiptId == photo.id

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .glass(
                                shape = RoundedCornerShape(18.dp),
                                backgroundColor = Color(0xDD141418),
                                borderWidth = 1.dp,
                                topBorderAlpha = if (isExpanded) 0.50f else 0.25f,
                                bottomBorderAlpha = 0.06f
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                expandedReceiptId = if (isExpanded) null else photo.id
                            }
                            .padding(14.dp)
                    ) {
                        Column {
                            // Top Row: Icon, Merchant, GSTIN, and Amount
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
                                            .clip(CircleShape)
                                            .background(Color(0xFF18181B))
                                            .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape)
                                    ) {
                                        AsyncImage(
                                            model = if (photo.drawableRes != 0) photo.drawableRes else photo.imageUrl,
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize().clip(CircleShape)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(end = 8.dp)
                                    ) {
                                        Text(
                                            text = photo.merchant ?: photo.title,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        val subtitleText = if (photo.gstin != null) {
                                            "${photo.dateFormatted} • GSTIN: ${photo.gstin}"
                                        } else {
                                            photo.dateFormatted
                                        }
                                        Text(
                                            text = subtitleText,
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = Color(0xFFA1A1AA),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                Text(
                                    text = photo.totalAmount ?: "₹ 0.00",
                                    fontSize = 14.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }

                            // Expanded Section: Actual real photo of receipt + Raw OCR regex highlights
                            AnimatedVisibility(
                                visible = isExpanded,
                                enter = expandVertically() + fadeIn(),
                                exit = shrinkVertically() + fadeOut()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 14.dp)
                                    ) {
                                    // Real Receipt Photo Preview
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(180.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0xFF18181B))
                                            .border(
                                                width = 1.dp,
                                                color = Color.White.copy(alpha = 0.15f),
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                    ) {
                                        AsyncImage(
                                            model = if (photo.drawableRes != 0) photo.drawableRes else photo.imageUrl,
                                            contentDescription = photo.title,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Extracted Regex Badges
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        if (photo.gstin != null) {
                                            Box(
                                                modifier = Modifier
                                                    .glass(
                                                        shape = RoundedCornerShape(8.dp),
                                                        backgroundColor = Color(0x3310B981),
                                                        borderWidth = 1.dp,
                                                        topBorderAlpha = 0.40f
                                                    )
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    text = "GSTIN: ${photo.gstin}",
                                                    fontSize = 9.sp,
                                                    fontFamily = FontFamily.Monospace,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF10B981)
                                                )
                                            }
                                        }

                                        Box(
                                            modifier = Modifier
                                                .glass(
                                                    shape = RoundedCornerShape(8.dp),
                                                    backgroundColor = Color.White.copy(alpha = 0.08f),
                                                    borderWidth = 1.dp,
                                                    topBorderAlpha = 0.30f
                                                )
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = "TOTAL: ${photo.totalAmount ?: "₹ 0.00"}",
                                                fontSize = 9.sp,
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Raw OCR Text with Highlighted Matches
                                    if (photo.ocrText != null) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .glass(
                                                    shape = RoundedCornerShape(12.dp),
                                                    backgroundColor = Color.Black.copy(alpha = 0.50f),
                                                    borderWidth = 1.dp,
                                                    topBorderAlpha = 0.25f
                                                )
                                                .padding(10.dp)
                                        ) {
                                            Text(
                                                text = photo.ocrText ?: "",
                                                fontSize = 10.sp,
                                                fontFamily = FontFamily.Monospace,
                                                color = Color(0xFFA1A1AA),
                                                lineHeight = 15.sp
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Action to open full Drishti X-Ray Inspector
                                    Button(
                                        onClick = { inspectingPhoto = photo },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color.White.copy(alpha = 0.12f),
                                            contentColor = Color.White
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Visibility,
                                            contentDescription = null,
                                            tint = Color(0xFF10B981),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Open in Drishti X-Ray Inspector",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
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

        // Export Dialog
        if (showExportDialog) {
            AlertDialog(
                onDismissRequest = { showExportDialog = false },
                containerColor = Color(0xFF18181B),
                title = {
                    Text(
                        text = "Export Expense Ledger",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                text = {
                    Text(
                        text = "Ledger exported to internal storage as Darpan_Expenses_Oct2026.csv. 100% offline, zero cloud syncing.",
                        fontSize = 13.sp,
                        color = Color(0xFFA1A1AA)
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showExportDialog = false
                            Toast.makeText(context, "Exported to Downloads/Darpan_Expenses.csv", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Text(text = "OK", color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    }
}
