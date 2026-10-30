package com.threeloops.mirrorarchive.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ========================================================
// Glassmorphism Green to White Theme Constants
// ========================================================
val PureBlack = Color(0xFF0F172A)
val ObsidianBlack = Color(0xFF064E3B)
val GlassSurface = Color(0xF2FFFFFF) // 95% frosted white
val GlassSurfaceHover = Color(0xFFFFFFFF)
val GlassSurfaceSubtle = Color(0xE6F0FDF4) // Soft mint white
val GlassSurfaceElevated = Color(0xFFFFFFFF)

val GlassBorderLight = Color(0x3310B981) // 20% Emerald specular highlight
val GlassBorderSubtle = Color(0x1F10B981) // 12% Emerald border

val TextPrimary = Color(0xFF0F172A) // Dark slate for primary text
val TextSecondary = Color(0xFF4B5563) // Crisp secondary
val TextMuted = Color(0xFF9CA3AF)

/**
 * Standard glass border brush with top-down specular reflection.
 */
fun glassBorderBrush(
    topAlpha: Float = 0.28f,
    bottomAlpha: Float = 0.08f
): Brush = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF10B981).copy(alpha = topAlpha),
        Color(0xFF10B981).copy(alpha = bottomAlpha)
    )
)

/**
 * Modifier extension for applying frosted glass styling.
 */
fun Modifier.glass(
    shape: Shape = RoundedCornerShape(20.dp),
    backgroundColor: Color = Color.White.copy(alpha = 0.88f),
    borderWidth: Dp = 1.dp,
    topBorderAlpha: Float = 0.28f,
    bottomBorderAlpha: Float = 0.08f
): Modifier = this
    .clip(shape)
    .background(backgroundColor, shape)
    .border(
        width = borderWidth,
        brush = glassBorderBrush(topBorderAlpha, bottomBorderAlpha),
        shape = shape
    )

/**
 * Fullscreen ambient backdrop with Green to White gradient.
 */
@Composable
fun GlassScaffoldBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE8F5E9), // Soft Mint Top
                        Color(0xFFFFFFFF), // Crisp White Center
                        Color(0xFFF1F8F4)  // Subtle Green-White Base
                    )
                )
            )
            .drawBehind {
                // Subtle emerald radial glow top-center
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF10B981).copy(alpha = 0.12f),
                            Color(0xFF34D399).copy(alpha = 0.04f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.5f, size.height * 0.08f),
                        radius = size.width * 0.9f
                    )
                )
                // Subtle mint radial glow bottom-right
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFA7F3D0).copy(alpha = 0.10f),
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

/**
 * Frosted glass card with standard border and elevation.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    backgroundColor: Color = Color.White.copy(alpha = 0.90f),
    borderWidth: Dp = 1.dp,
    topBorderAlpha: Float = 0.28f,
    bottomBorderAlpha: Float = 0.08f,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier.glass(
            shape = shape,
            backgroundColor = backgroundColor,
            borderWidth = borderWidth,
            topBorderAlpha = topBorderAlpha,
            bottomBorderAlpha = bottomBorderAlpha
        ),
        content = content
    )
}

/**
 * Interactive glass pill button with tactile touch feedback.
 */
@Composable
fun GlassPillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isActive: Boolean = false,
    leadingIcon: (@Composable () -> Unit)? = null
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(
                if (isActive) Color(0xFF059669) else Color.White.copy(alpha = 0.80f),
                RoundedCornerShape(24.dp)
            )
            .border(
                1.dp,
                if (isActive) Color(0xFF047857) else Color(0x3310B981),
                RoundedCornerShape(24.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            leadingIcon?.let {
                it()
                Spacer(modifier = Modifier.width(6.dp))
            }
            androidx.compose.material3.Text(
                text = text,
                color = if (isActive) Color.White else Color(0xFF064E3B),
                fontSize = 13.sp,
                fontWeight = if (isActive) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Medium
            )
        }
    }
}
