package com.threeloops.mirrorarchive.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Green to White ambient mesh glow.
 * Renders an ethereal, premium gradient from mint/emerald to crisp white.
 */
@Composable
fun AmbientMeshBox(
    modifier: Modifier = Modifier,
    meshColor: Color = Color(0xFF10B981), // Emerald glow
    secondaryColor: Color = Color(0xFFA7F3D0), // Soft Mint
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE8F5E9),
                        Color(0xFFFFFFFF),
                        Color(0xFFF7FCF9)
                    )
                )
            )
            .drawBehind {
                // Top center warm emerald radial bloom
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            meshColor.copy(alpha = 0.14f),
                            secondaryColor.copy(alpha = 0.05f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.5f, size.height * 0.08f),
                        radius = size.width * 0.95f
                    )
                )

                // Bottom right subtle mint bloom
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            secondaryColor.copy(alpha = 0.12f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.9f, size.height * 0.85f),
                        radius = size.width * 0.7f
                    )
                )
            },
        content = content
    )
}
