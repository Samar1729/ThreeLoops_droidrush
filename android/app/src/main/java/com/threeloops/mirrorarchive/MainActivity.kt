package com.threeloops.mirrorarchive

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.threeloops.mirrorarchive.ui.navigation.MirrorArchiveBottomBar
import com.threeloops.mirrorarchive.ui.navigation.Routes
import com.threeloops.mirrorarchive.ui.theme.MirrorArchiveTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main activity hosting the Navigation Compose shell.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MirrorArchiveTheme {
                MainNavigationApp()
            }
        }
    }
}

/**
 * Root navigation container with bottom tabs and NavHost.
 *
 * @param navController Navigation controller managing back stack.
 */
@Composable
fun MainNavigationApp(navController: NavHostController = rememberNavController()) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            MirrorArchiveBottomBar(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(Routes.ALBUMS) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.ALBUMS,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Three main bottom tabs
            composable(Routes.ALBUMS) {
                PlaceholderScreen(name = "albums")
            }

            composable(
                route = Routes.SEARCH,
                arguments = listOf(
                    navArgument("mode") {
                        type = NavType.StringType
                        defaultValue = "TAGS"
                    },
                    navArgument("q") {
                        type = NavType.StringType
                        defaultValue = ""
                    }
                )
            ) { backStackEntry ->
                val mode = backStackEntry.arguments?.getString("mode") ?: "TAGS"
                val q = backStackEntry.arguments?.getString("q") ?: ""
                PlaceholderScreen(name = "search?mode=$mode&q=$q")
            }

            composable(Routes.PEOPLE) {
                PlaceholderScreen(name = "people")
            }

            // Onboarding & Permission destinations
            composable(Routes.ONBOARDING) {
                PlaceholderScreen(name = "onboarding")
            }

            composable(Routes.PERMISSION_DENIED) {
                PlaceholderScreen(name = "permission_denied")
            }

            // Detail destinations (arguments are IDs only)
            composable(
                route = Routes.ALBUM_DETAIL,
                arguments = listOf(
                    navArgument("albumType") {
                        type = NavType.StringType
                        defaultValue = "{albumType}"
                    }
                )
            ) { backStackEntry ->
                val albumType = backStackEntry.arguments?.getString("albumType") ?: "{albumType}"
                PlaceholderScreen(name = "albums/$albumType")
            }

            composable(
                route = Routes.IMAGE_DETAIL,
                arguments = listOf(
                    navArgument("imageId") {
                        type = NavType.StringType
                        defaultValue = "{imageId}"
                    }
                )
            ) { backStackEntry ->
                val imageId = backStackEntry.arguments?.getString("imageId") ?: "{imageId}"
                PlaceholderScreen(name = "image/$imageId")
            }

            composable(Routes.DUPLICATES) {
                PlaceholderScreen(name = "duplicates")
            }

            composable(
                route = Routes.DUPLICATE_GROUP,
                arguments = listOf(
                    navArgument("groupId") {
                        type = NavType.StringType
                        defaultValue = "{groupId}"
                    }
                )
            ) { backStackEntry ->
                val groupId = backStackEntry.arguments?.getString("groupId") ?: "{groupId}"
                PlaceholderScreen(name = "duplicates/group/$groupId")
            }

            composable(Routes.STORAGE_SAVER) {
                PlaceholderScreen(name = "storage_saver")
            }

            composable(
                route = Routes.EXPORT,
                arguments = listOf(
                    navArgument("ids") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = ""
                    }
                )
            ) { backStackEntry ->
                val ids = backStackEntry.arguments?.getString("ids") ?: ""
                PlaceholderScreen(name = "export?ids=$ids")
            }

            composable(Routes.SETTINGS) {
                PlaceholderScreen(name = "settings")
            }

            composable(Routes.METRICS) {
                PlaceholderScreen(name = "metrics")
            }
        }
    }
}

/**
 * Placeholder screen displaying a centered Text with the route name.
 *
 * @param name The destination route name.
 * @param modifier The layout modifier.
 */
@Composable
fun PlaceholderScreen(
    name: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.headlineMedium
        )
    }
}
