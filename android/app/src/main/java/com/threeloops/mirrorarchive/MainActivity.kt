package com.threeloops.mirrorarchive

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.threeloops.mirrorarchive.data.model.AlbumSummary
import com.threeloops.mirrorarchive.data.model.AlbumType
import com.threeloops.mirrorarchive.data.model.ImageItem
import com.threeloops.mirrorarchive.ui.albums.AlbumsScreen
import com.threeloops.mirrorarchive.ui.navigation.MirrorArchiveBottomBar
import com.threeloops.mirrorarchive.ui.navigation.Screen
import com.threeloops.mirrorarchive.ui.people.PeopleScreen
import com.threeloops.mirrorarchive.ui.theme.MirrorArchiveTheme
import kotlinx.coroutines.flow.MutableStateFlow

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val sampleSummaries = listOf(
            AlbumSummary(AlbumType.RECEIPTS, count = 14, coverUri = null),
            AlbumSummary(AlbumType.SCREENSHOTS, count = 28, coverUri = null),
            AlbumSummary(AlbumType.DOCUMENTS, count = 9, coverUri = null),
            AlbumSummary(AlbumType.FOOD, count = 42, coverUri = null),
            AlbumSummary(AlbumType.PEOPLE, count = 35, coverUri = null)
        )

        setContent {
            MirrorArchiveTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                Scaffold(
                    bottomBar = {
                        MirrorArchiveBottomBar(
                            currentRoute = currentRoute,
                            onNavigate = { route ->
                                navController.navigate(route) {
                                    popUpTo(Screen.Albums.route) { saveState = true }
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
                        startDestination = Screen.Albums.route,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(Screen.Albums.route) {
                            AlbumsScreen(
                                summariesFlow = MutableStateFlow(sampleSummaries),
                                onAlbumClick = { albumType ->
                                    navController.navigate(Screen.AlbumDetail.createRoute(albumType))
                                }
                            )
                        }

                        composable(Screen.People.route) {
                            PeopleScreen(
                                peopleFlow = MutableStateFlow(emptyList()),
                                onImageClick = { id ->
                                    navController.navigate(Screen.ImageDetail.createRoute(id))
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
