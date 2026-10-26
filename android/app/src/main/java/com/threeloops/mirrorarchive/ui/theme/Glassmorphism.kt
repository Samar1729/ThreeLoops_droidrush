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

// Glassmorphism Black & White Theme Colors
val PureBlack = Color(0xFF000000)
val ObsidianBlack = Color(0xFF070709)
val GlassSurface = Color(0x1AFFFFFF) // 10% white
val GlassSurfaceHover = Color(0x26FFFFFF) // 15% white
val GlassSurfaceSubtle = Color(0x0FFFFFFF) // 6% white
val GlassSurfaceElevated = Color(0x2EFFFFFF) // 18% white

val GlassBorderLight = Color(0x40FFFFFF) // 25% white specular highlight
val GlassBorderSubtle = Color(0x14FFFFFF) // 8% white bottom edge

val TextPrimary = Color(0xFFFFFFFF)
val TextSecondary = Color(0xFFA1A1AA)
val TextMuted = Color(0xFF71717A)

/**
 * Standard glass border brush with top-down specular reflection.
 */
fun glassBorderBrush(
    topAlpha: Float = 0.28f,
    bottomAlpha: Float = 0.06f
): Brush = Brush.verticalGradient(
    colors = listOf(
        Color.White.copy(alpha = topAlpha),
        Color.White.copy(alpha = bottomAlpha)
    )
)

/**
 * Modifier extension for applying frosted glass styling.
 */
fun Modifier.glass(
    shape: Shape = RoundedCornerShape(20.dp),
    backgroundColor: Color = Color.White.copy(alpha = 0.07f),
    borderWidth: Dp = 1.dp,
    topBorderAlpha: Float = 0.28f,
    bottomBorderAlpha: Float = 0.06f
): Modifier = this
    .clip(shape)
    .background(backgroundColor, shape)
    .border(
        width = borderWidth,
        brush = glassBorderBrush(topBorderAlpha, bottomBorderAlpha),
        shape = shape
    )

/**
 * Fullscreen ambient backdrop with deep black and subtle ambient glass lighting.
 */
@Composable
fun GlassScaffoldBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PureBlack)
            .drawBehind {
                // Subtle ambient radial glow in top center for glass refraction
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0x18FFFFFF),
                            Color(0x06FFFFFF),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.5f, size.height * 0.15f),
                        radius = size.width * 0.85f
                    )
                )
                // Subtle ambient radial glow in bottom right
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0x10FFFFFF),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.85f, size.height * 0.85f),
                        radius = size.width * 0.6f
                    )
                )
            },
        content = content
    )
}

/**
 * Frosted Glass Card container.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    backgroundColor: Color = Color.White.copy(alpha = 0.07f),
    borderWidth: Dp = 1.dp,
    topBorderAlpha: Float = 0.25f,
    bottomBorderAlpha: Float = 0.05f,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val baseModifier = modifier.glass(
        shape = shape,
        backgroundColor = backgroundColor,
        borderWidth = borderWidth,
        topBorderAlpha = topBorderAlpha,
        bottomBorderAlpha = bottomBorderAlpha
    )

    if (onClick != null) {
        Box(
            modifier = baseModifier.clickable { onClick() }
        ) {
            content()
        }
    } else {
        Box(modifier = baseModifier) {
            content()
        }
    }
}

/**
 * Frosted Glass Badge / Chip.
 */
@Composable
fun GlassBadge(
    text: String,
    modifier: Modifier = Modifier,
    textColor: Color = TextPrimary,
    backgroundColor: Color = Color.White.copy(alpha = 0.12f)
) {
    Box(
        modifier = modifier
            .glass(
                shape = RoundedCornerShape(8.dp),
                backgroundColor = backgroundColor,
                borderWidth = 1.dp,
                topBorderAlpha = 0.35f,
                bottomBorderAlpha = 0.10f
            )
            .padding(horizontal = 9.dp, vertical = 4.dp)
    ) {
        androidx.compose.material3.Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
            color = textColor
        )
    }
}
