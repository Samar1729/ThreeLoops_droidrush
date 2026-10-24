package com.threeloops.mirrorarchive.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/**
 * Data representation of a bottom navigation bar item.
 *
 * @property route Target route destination.
 * @property label User-facing tab label.
 * @property icon Tab icon vector.
 */
data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

/**
 * Three bottom navigation tabs: Albums, Search, People.
 */
val bottomNavItems = listOf(
    BottomNavItem(Routes.ALBUMS, "Albums", Icons.Outlined.Folder),
    BottomNavItem(Routes.search(), "Search", Icons.Outlined.Search),
    BottomNavItem(Routes.PEOPLE, "People", Icons.Outlined.People)
)

/**
 * Material 3 NavigationBar component for the three main tabs.
 *
 * @param currentRoute Currently selected route string.
 * @param onNavigate Callback invoked when a tab is clicked.
 * @param modifier Layout modifier.
 */
@Composable
fun MirrorArchiveBottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        modifier = modifier
    ) {
        bottomNavItems.forEach { item ->
            val isSelected = when (item.label) {
                "Search" -> currentRoute?.startsWith("search") == true
                else -> currentRoute == item.route
            }
            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(item.route) },
                icon = {
                    Icon(imageVector = item.icon, contentDescription = item.label)
                },
                label = { Text(text = item.label) },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = MaterialTheme.colorScheme.primary,
                    selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                    selectedTextColor = MaterialTheme.colorScheme.onSurface,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            )
        }
    }
}
