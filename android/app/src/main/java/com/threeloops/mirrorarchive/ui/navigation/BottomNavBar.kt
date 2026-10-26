package com.threeloops.mirrorarchive.ui.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.ReceiptLong
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
    BottomNavItem(Routes.ALBUMS, "Library", Icons.Outlined.PhotoLibrary),
    BottomNavItem(Routes.DUPLICATES, "Dupes", Icons.Outlined.ContentCopy),
    BottomNavItem(Routes.search(), "Search", Icons.Outlined.Search),
    BottomNavItem(Routes.PEOPLE, "People", Icons.Outlined.People),
    BottomNavItem(Routes.LEDGER, "Ledger", Icons.Outlined.ReceiptLong)
)

/**
 * Floating glassmorphism bottom navigation dock with Spotify-sleek styling.
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
            .padding(horizontal = 14.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .glass(
                    shape = RoundedCornerShape(32.dp),
                    backgroundColor = Color(0xF00E0E12),
                    borderWidth = 1.dp,
                    topBorderAlpha = 0.35f,
                    bottomBorderAlpha = 0.08f
                )
                .padding(horizontal = 6.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            bottomNavItems.forEach { item ->
                val isSelected = when (item.label) {
                    "Search" -> currentRoute?.startsWith("search") == true
                    "Library" -> currentRoute == Routes.ALBUMS || currentRoute?.startsWith("albums/") == true
                    "Dupes" -> currentRoute == Routes.DUPLICATES || currentRoute?.startsWith("duplicates") == true
                    "People" -> currentRoute == Routes.PEOPLE
                    "Ledger" -> currentRoute == Routes.LEDGER
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
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            tint = iconColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = item.label,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                            color = iconColor
                        )
                    }
                }
            }
        }
    }
}
