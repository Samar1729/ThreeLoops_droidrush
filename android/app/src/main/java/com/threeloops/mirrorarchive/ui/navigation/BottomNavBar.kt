package com.threeloops.mirrorarchive.ui.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.threeloops.mirrorarchive.ui.theme.glass

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(Routes.ALBUMS, "Albums", Icons.Outlined.Folder),
    BottomNavItem(Routes.search(), "Search", Icons.Outlined.Search),
    BottomNavItem(Routes.PEOPLE, "People", Icons.Outlined.People)
)

/**
 * Floating glassmorphism bottom navigation pill bar with Black & White styling.
 */
@Composable
fun MirrorArchiveBottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .glass(
                    shape = RoundedCornerShape(32.dp),
                    backgroundColor = Color(0xE60C0C10),
                    borderWidth = 1.dp,
                    topBorderAlpha = 0.35f,
                    bottomBorderAlpha = 0.08f
                )
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            bottomNavItems.forEach { item ->
                val isSelected = when (item.label) {
                    "Search" -> currentRoute?.startsWith("search") == true
                    else -> currentRoute == item.route
                }

                val iconColor by animateColorAsState(
                    targetValue = if (isSelected) Color.White else Color(0xFF71717A),
                    label = "iconColor"
                )
                val bgTabColor by animateColorAsState(
                    targetValue = if (isSelected) Color.White.copy(alpha = 0.12f) else Color.Transparent,
                    label = "bgTabColor"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(26.dp))
                        .then(
                            if (isSelected) {
                                Modifier.glass(
                                    shape = RoundedCornerShape(26.dp),
                                    backgroundColor = bgTabColor,
                                    borderWidth = 1.dp,
                                    topBorderAlpha = 0.30f,
                                    bottomBorderAlpha = 0.05f
                                )
                            } else {
                                Modifier
                            }
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            onNavigate(item.route)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            tint = iconColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = item.label,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                            color = iconColor
                        )
                    }
                }
            }
        }
    }
}
