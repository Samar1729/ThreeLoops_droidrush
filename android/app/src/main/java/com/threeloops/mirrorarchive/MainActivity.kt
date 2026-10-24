package com.threeloops.mirrorarchive

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.threeloops.mirrorarchive.ui.navigation.AppNavigationState
import com.threeloops.mirrorarchive.ui.navigation.MainViewModel
import com.threeloops.mirrorarchive.ui.navigation.MirrorArchiveBottomBar
import com.threeloops.mirrorarchive.ui.navigation.Routes
import com.threeloops.mirrorarchive.ui.onboarding.OnboardingScreen
import com.threeloops.mirrorarchive.ui.permission.PermissionDeniedScreen
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
 * Root navigation container with bottom tabs, dynamic start destination, and NavHost.
 *
 * @param navController Navigation controller managing back stack.
 * @param viewModel MainViewModel providing reactive app gate state.
 */
@Composable
fun MainNavigationApp(
    navController: NavHostController = rememberNavController(),
    viewModel: MainViewModel = hiltViewModel()
) {
    val navState by viewModel.navigationState.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Show loading spinner while reading initial onboarding & permission states
    if (navState is AppNavigationState.Loading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }

    // Determine initial start destination based on gate rules:
    // onboarding not done -> onboarding;
    // else state not FULL/PARTIAL -> permission_denied;
    // else -> albums.
    val calculatedStartDestination = when (navState) {
        AppNavigationState.Onboarding -> Routes.ONBOARDING
        AppNavigationState.PermissionDenied -> Routes.PERMISSION_DENIED
        AppNavigationState.Ready, AppNavigationState.Loading -> Routes.ALBUMS
    }

    // Reactive automatic re-routing when stateFlow changes (e.g., returning from Settings)
    LaunchedEffect(navState) {
        when (navState) {
            AppNavigationState.Loading -> Unit
            AppNavigationState.Onboarding -> {
                if (currentRoute != null && currentRoute != Routes.ONBOARDING) {
                    navController.navigate(Routes.ONBOARDING) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }
            AppNavigationState.PermissionDenied -> {
                if (currentRoute != null && currentRoute != Routes.PERMISSION_DENIED) {
                    navController.navigate(Routes.PERMISSION_DENIED) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }
            AppNavigationState.Ready -> {
                if (currentRoute == Routes.ONBOARDING || currentRoute == Routes.PERMISSION_DENIED) {
                    navController.navigate(Routes.ALBUMS) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }
        }
    }

    val isGateScreen = currentRoute == Routes.ONBOARDING || currentRoute == Routes.PERMISSION_DENIED

    Scaffold(
        bottomBar = {
            if (!isGateScreen) {
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
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = calculatedStartDestination,
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
                OnboardingScreen(
                    onGranted = {
                        navController.navigate(Routes.ALBUMS) {
                            popUpTo(Routes.ONBOARDING) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onDenied = {
                        navController.navigate(Routes.PERMISSION_DENIED) {
                            popUpTo(Routes.ONBOARDING) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(Routes.PERMISSION_DENIED) {
                PermissionDeniedScreen(
                    onGranted = {
                        navController.navigate(Routes.ALBUMS) {
                            popUpTo(Routes.PERMISSION_DENIED) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onContinueWithPartial = {
                        navController.navigate(Routes.ALBUMS) {
                            popUpTo(Routes.PERMISSION_DENIED) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
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
