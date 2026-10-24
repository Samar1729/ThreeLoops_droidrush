package com.threeloops.mirrorarchive.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Albums.route, "Albums", Icons.Outlined.Folder),
    BottomNavItem(Screen.Search.route, "Search", Icons.Outlined.Search),
    BottomNavItem(Screen.People.route, "People", Icons.Outlined.People),
    BottomNavItem(Screen.Duplicates.route, "Duplicates", Icons.Outlined.CopyAll),
    BottomNavItem(Screen.Metrics.route, "Metrics", Icons.Outlined.Analytics)
)

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
            val isSelected = currentRoute == item.route
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
