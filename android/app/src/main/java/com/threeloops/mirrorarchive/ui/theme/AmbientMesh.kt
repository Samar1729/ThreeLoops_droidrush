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
 * Spotify-inspired ambient mesh glow.
 * Renders a subtle radial mesh gradient at 12-16% opacity to give a rich,
 * deep frosted-glass aesthetic without neon "AI slop".
 */
@Composable
fun AmbientMeshBox(
    modifier: Modifier = Modifier,
    meshColor: Color = Color(0xFF1E1E24), // Subtle dark ambient warm/charcoal
    secondaryColor: Color = Color(0xFF121217),
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF09090B)) // Obsidian pitch base
            .drawBehind {
                // Top center warm radial bloom
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            meshColor.copy(alpha = 0.22f),
                            secondaryColor.copy(alpha = 0.08f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.5f, size.height * 0.10f),
                        radius = size.width * 0.95f
                    )
                )

                // Bottom right subtle ambient glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.04f),
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
