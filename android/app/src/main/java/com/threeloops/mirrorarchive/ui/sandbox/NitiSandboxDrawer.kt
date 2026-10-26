package com.threeloops.mirrorarchive.ui.sandbox

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.threeloops.mirrorarchive.albums.AlbumClassifier
import com.threeloops.mirrorarchive.albums.AlbumRules
import com.threeloops.mirrorarchive.contracts.AlbumType
import com.threeloops.mirrorarchive.contracts.LabelResult
import com.threeloops.mirrorarchive.ui.theme.GlassCard
import com.threeloops.mirrorarchive.ui.theme.glass

/**
 * Feature D: "Niti Sandbox" — Live Synonym & Confidence Tuning Drawer.
 * Allows mentors and judges to simulate and verify the deterministic ML rules engine live.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NitiSandboxDialog(
    onDismiss: () -> Unit
) {
    var confidenceCutoff by remember { mutableFloatStateOf(0.60f) }
    var inputLabels by remember { mutableStateOf("paper, receipt, document") }
    var inputOcrText by remember { mutableStateOf("Total ₹ 450 GSTIN 27AAAAA0000A1Z5") }
    var inputPath by remember { mutableStateOf("DCIM/Camera/IMG_2026.jpg") }
    var faceCount by remember { mutableIntStateOf(0) }

    var evaluatedAlbum by remember { mutableStateOf<AlbumType?>(null) }
    var auditTrace by remember { mutableStateOf("") }
    var expandedSynonyms by remember { mutableStateOf<List<String>>(emptyList()) }

    // Run initial evaluation
    LaunchedEffect(confidenceCutoff, inputLabels, inputOcrText, inputPath, faceCount) {
        val labelList = inputLabels.split(",")
            .map { it.trim().lowercase() }
            .filter { it.isNotEmpty() }
            .map { LabelResult(it, 0.85f) }

        val detected = AlbumClassifier.classify(
            labels = labelList,
            path = inputPath,
            ocr = inputOcrText,
            faceCount = faceCount
        )
        evaluatedAlbum = detected.firstOrNull()

        auditTrace = AlbumClassifier.explain(
            labels = labelList,
            path = inputPath,
            ocr = inputOcrText,
            faceCount = faceCount
        )

        // Synonym expansion check for first label
        val firstToken = labelList.firstOrNull()?.text ?: ""
        expandedSynonyms = AlbumRules.synonyms[firstToken] ?: emptyList()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 680.dp),
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        content = {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.94f)
                    .glass(
                        shape = RoundedCornerShape(24.dp),
                        backgroundColor = Color(0xF5101014),
                        borderWidth = 1.dp,
                        topBorderAlpha = 0.40f,
                        bottomBorderAlpha = 0.10f
                    )
                    .padding(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .glass(
                                        shape = CircleShape,
                                        backgroundColor = Color(0x3310B981),
                                        borderWidth = 1.dp,
                                        topBorderAlpha = 0.40f
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Tune,
                                    contentDescription = null,
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Niti Sandbox",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Live ML Rules & Synonym Tuning",
                                    fontSize = 11.sp,
                                    color = Color(0xFFA1A1AA)
                                )
                            }
                        }

                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = "Close",
                                tint = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Live Evaluation Result Hero Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .glass(
                                shape = RoundedCornerShape(16.dp),
                                backgroundColor = if (evaluatedAlbum != null) Color(0x2210B981) else Color.White.copy(alpha = 0.05f),
                                borderWidth = 1.dp,
                                topBorderAlpha = 0.50f
                            )
                            .padding(14.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "CLASSIFIED ALBUM",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFA1A1AA)
                                )
                                Text(
                                    text = evaluatedAlbum?.name ?: "NONE (Unclassified)",
                                    fontSize = 13.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = if (evaluatedAlbum != null) Color(0xFF10B981) else Color.White
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = auditTrace,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFFE4E4E7)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Slider: Confidence Cutoff
                    Text(
                        text = "ML Kit Confidence Threshold: ${"%.2f".format(confidenceCutoff)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Slider(
                        value = confidenceCutoff,
                        onValueChange = { confidenceCutoff = it },
                        valueRange = 0.40f..0.95f,
                        steps = 11,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF10B981),
                            activeTrackColor = Color(0xFF10B981),
                            inactiveTrackColor = Color(0xFF27272A)
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Input: Labels
                    Text(text = "Simulated ML Kit Labels (comma-separated)", fontSize = 11.sp, color = Color(0xFFA1A1AA))
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = inputLabels,
                        onValueChange = { inputLabels = it },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = LocalTextStyle.current.copy(fontSize = 12.sp, color = Color.White),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF10B981),
                            unfocusedBorderColor = Color(0x33FFFFFF)
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Input: OCR Text
                    Text(text = "Simulated OCR Extracted Text", fontSize = 11.sp, color = Color(0xFFA1A1AA))
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = inputOcrText,
                        onValueChange = { inputOcrText = it },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = LocalTextStyle.current.copy(fontSize = 12.sp, color = Color.White),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF10B981),
                            unfocusedBorderColor = Color(0x33FFFFFF)
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Input: Relative Path
                    Text(text = "File Path / Name Simulation", fontSize = 11.sp, color = Color(0xFFA1A1AA))
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = inputPath,
                        onValueChange = { inputPath = it },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = LocalTextStyle.current.copy(fontSize = 12.sp, color = Color.White),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF10B981),
                            unfocusedBorderColor = Color(0x33FFFFFF)
                        )
                    )

                    if (expandedSynonyms.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Synonym Expansion: ${expandedSynonyms.joinToString(", ")}",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF38BDF8)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(text = "Done Testing", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    )
}
