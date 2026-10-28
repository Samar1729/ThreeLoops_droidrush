package com.threeloops.mirrorarchive

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.threeloops.mirrorarchive.contracts.ImageItem
import com.threeloops.mirrorarchive.contracts.ScanPhase
import com.threeloops.mirrorarchive.contracts.ScanProgress
import com.threeloops.mirrorarchive.ui.albums.AlbumDetailScreen
import com.threeloops.mirrorarchive.ui.albums.AlbumsScreen
import com.threeloops.mirrorarchive.ui.albums.AlbumsViewModel
import com.threeloops.mirrorarchive.ui.auth.AuthScreen
import com.threeloops.mirrorarchive.ui.dupes.DuplicateGroupDetailScreen
import com.threeloops.mirrorarchive.ui.dupes.DuplicatesScreen
import com.threeloops.mirrorarchive.ui.imagedetail.ImageDetailScreen
import com.threeloops.mirrorarchive.ui.metrics.MetricsScreen
import com.threeloops.mirrorarchive.ui.ledger.RaseedLedgerScreen
import com.threeloops.mirrorarchive.ui.navigation.NowIndexingBar
import com.threeloops.mirrorarchive.ui.navigation.AppNavigationState
import com.threeloops.mirrorarchive.ui.navigation.MainViewModel
import com.threeloops.mirrorarchive.ui.navigation.MirrorArchiveBottomBar
import com.threeloops.mirrorarchive.ui.navigation.Routes
import com.threeloops.mirrorarchive.ui.onboarding.OnboardingScreen
import com.threeloops.mirrorarchive.ui.people.PeopleScreen
import com.threeloops.mirrorarchive.ui.permission.PermissionDeniedScreen
import com.threeloops.mirrorarchive.ui.search.SearchScreen
import com.threeloops.mirrorarchive.ui.settings.SettingsScreen
import com.threeloops.mirrorarchive.ui.storage.StorageSaverScreen
import com.threeloops.mirrorarchive.ui.theme.MirrorArchiveTheme
import com.threeloops.mirrorarchive.security.DatabaseRecoveryNotifier
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main activity hosting the Navigation Compose shell.
 */
@AndroidEntryPoint
class MainActivity : FragmentActivity() {
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
    val navState by viewModel.navigationState.collectAsStateWithLifecycle()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val recoveryEvent by DatabaseRecoveryNotifier.events.collectAsStateWithLifecycle()

    var isVaultUnlocked by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(recoveryEvent, navState, isVaultUnlocked) {
        if (isVaultUnlocked && navState == AppNavigationState.Ready &&
            DatabaseRecoveryNotifier.consumePendingNotice(context)
        ) {
            snackbarHostState.showSnackbar("Index was reset, re-scanning")
        }
    }

    if (!isVaultUnlocked) {
        AuthScreen(
            onAuthenticated = { isVaultUnlocked = true }
        )
        return
    }

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
                viewModel.startInitialScanIfNeeded()
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

    val scanProgress by viewModel.scanProgress.collectAsStateWithLifecycle(
        initialValue = ScanProgress(ScanPhase.IDLE, 0, 0)
    )

    Scaffold(
        containerColor = Color(0xFFF7FCF9),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (!isGateScreen) {
                Column {
                    NowIndexingBar(progress = scanProgress)
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
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            NavHost(
                navController = navController,
                startDestination = calculatedStartDestination,
                modifier = Modifier.weight(1f)
            ) {
            // Main bottom tabs
            composable(Routes.ALBUMS) {
                AlbumsScreen(
                    viewModel = hiltViewModel(),
                    onAlbumClick = { albumType ->
                        navController.navigate(Routes.albumDetail(albumType.name))
                    },
                    onNavigateDuplicates = {
                        navController.navigate(Routes.DUPLICATES)
                    },
                    onNavigateStorageSaver = {
                        navController.navigate(Routes.STORAGE_SAVER)
                    },
                    onNavigateSettings = {
                        navController.navigate(Routes.SETTINGS)
                    }
                )
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
            ) {
                SearchScreen(
                    viewModel = hiltViewModel(),
                    onImageClick = { imageId ->
                        navController.navigate(Routes.imageDetail(imageId))
                    }
                )
            }

            composable(Routes.PEOPLE) {
                PeopleScreen(
                    viewModel = hiltViewModel(),
                    onImageClick = { imageId ->
                        navController.navigate(Routes.imageDetail(imageId))
                    }
                )
            }

            composable(Routes.LEDGER) {
                RaseedLedgerScreen(
                    viewModel = hiltViewModel(),
                    onReceiptClick = { imageId ->
                        navController.navigate(Routes.imageDetail(imageId))
                    }
                )
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

            // Detail destinations
            composable(
                route = Routes.ALBUM_DETAIL,
                arguments = listOf(
                    navArgument("albumType") {
                        type = NavType.StringType
                        defaultValue = "SCREENSHOTS"
                    }
                )
            ) { backStackEntry ->
                val albumType = backStackEntry.arguments?.getString("albumType") ?: "SCREENSHOTS"
                AlbumDetailScreen(
                    albumTypeStr = albumType,
                    viewModel = hiltViewModel(),
                    onBackClick = { navController.popBackStack() },
                    onImageClick = { imageId ->
                        navController.navigate(Routes.imageDetail(imageId))
                    }
                )
            }

            composable(
                route = Routes.IMAGE_DETAIL,
                arguments = listOf(
                    navArgument("imageId") {
                        type = NavType.StringType
                        defaultValue = "1"
                    }
                )
            ) { backStackEntry ->
                val imageIdStr = backStackEntry.arguments?.getString("imageId") ?: "1"
                val imageId = imageIdStr.toLongOrNull() ?: 1L
                val albumsViewModel: AlbumsViewModel = hiltViewModel()
                val imageItem = albumsViewModel.getImage(imageId) ?: ImageItem(
                    id = imageId,
                    mediaId = 100L + imageId,
                    uri = "content://media/external/images/media/${100L + imageId}",
                    dateAdded = System.currentTimeMillis() / 1000,
                    dateTaken = System.currentTimeMillis(),
                    sizeBytes = 2_800_000L,
                    width = 1920,
                    height = 1080,
                    blurScore = 120.0,
                    faceCount = 1
                )
                ImageDetailScreen(
                    image = imageItem,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(Routes.DUPLICATES) {
                DuplicatesScreen(
                    onBackClick = { navController.popBackStack() },
                    onImageClick = { imageId ->
                        navController.navigate(Routes.imageDetail(imageId))
                    }
                )
            }

            composable(
                route = Routes.DUPLICATE_GROUP,
                arguments = listOf(
                    navArgument("groupId") {
                        type = NavType.StringType
                        defaultValue = "1"
                    }
                )
            ) { backStackEntry ->
                val groupIdStr = backStackEntry.arguments?.getString("groupId") ?: "1"
                val groupId = groupIdStr.toLongOrNull() ?: 1L
                DuplicateGroupDetailScreen(
                    groupId = groupId,
                    duplicatesViewModel = hiltViewModel(),
                    purgeViewModel = hiltViewModel(),
                    onBackClick = { navController.popBackStack() },
                    onImageClick = { imageId ->
                        navController.navigate(Routes.imageDetail(imageId))
                    }
                )
            }

            composable(Routes.STORAGE_SAVER) {
                StorageSaverScreen(
                    onBackClick = { navController.popBackStack() },
                    onImageClick = { imageId ->
                        navController.navigate(Routes.imageDetail(imageId))
                    }
                )
            }

            composable(Routes.SETTINGS) {
                SettingsScreen(
                    onBackClick = { navController.popBackStack() },
                    onNavigateMetrics = { navController.navigate(Routes.METRICS) },
                    viewModel = hiltViewModel()
                )
            }

            composable(Routes.METRICS) {
                MetricsScreen(
                    onBackClick = { navController.popBackStack() },
                    viewModel = hiltViewModel()
                )
            }
            }
        }
    }
}

@Composable
private fun ScanProgressBanner(progress: ScanProgress) {
    if (progress.phase == ScanPhase.IDLE) return
    val label = when (progress.phase) {
        ScanPhase.IDLE -> return
        ScanPhase.QUEUED -> "Indexing queued"
        ScanPhase.SCANNING -> "Indexing ${progress.done} / ${progress.total}"
        ScanPhase.PAUSED -> "Indexing paused · ${progress.done} / ${progress.total}"
        ScanPhase.FAILED -> "Indexing failed · ${progress.done} / ${progress.total}"
        ScanPhase.COMPLETE -> "Indexing complete · ${progress.done} / ${progress.total}"
        ScanPhase.SYNCING -> "Syncing new photos..."
    }
    Surface(color = Color(0xFF18181B), modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp)) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            Text(text = label, color = Color.White)
            if (progress.phase == ScanPhase.SCANNING || progress.phase == ScanPhase.QUEUED) {
                if (progress.total > 0) {
                    LinearProgressIndicator(
                        progress = { (progress.done.toFloat() / progress.total).coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth().padding(top = 6.dp)
                    )
                } else {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(top = 6.dp))
                }
            }
        }
    }
}
