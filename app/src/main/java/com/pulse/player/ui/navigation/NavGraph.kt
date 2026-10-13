package com.pulse.player.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.pulse.player.data.model.VideoSourceType
import com.pulse.player.data.repository.DownloadRepository
import com.pulse.player.data.repository.MediaRepository
import com.pulse.player.player.Media3PlayerManager
import com.pulse.player.ui.components.BottomNavigationBar
import com.pulse.player.ui.screens.detail.VideoDetailScreen
import com.pulse.player.ui.screens.detail.VideoDetailViewModel
import com.pulse.player.ui.screens.downloads.DownloadsScreen
import com.pulse.player.ui.screens.home.HomeScreen
import com.pulse.player.ui.screens.home.HomeViewModel
import com.pulse.player.ui.screens.library.LibraryScreen
import com.pulse.player.ui.screens.player.CustomPlayerScreen
import com.pulse.player.ui.screens.search.SearchScreen
import com.pulse.player.ui.screens.search.SearchViewModel
import com.pulse.player.ui.screens.settings.SettingsScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavGraph(
    mediaRepository: MediaRepository,
    downloadRepository: DownloadRepository,
    playerManager: Media3PlayerManager,
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route

    // Hide bottom bar on full screen player
    val showBottomBar = currentRoute in listOf(
        Screen.Home.route,
        Screen.Search.route,
        Screen.Library.route,
        Screen.Downloads.route,
        Screen.Settings.route
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavigationBar(navController = navController)
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            // 1. Home Screen
            composable(Screen.Home.route) {
                val homeViewModel = remember { HomeViewModel(mediaRepository, downloadRepository) }
                HomeScreen(
                    viewModel = homeViewModel,
                    onVideoClick = { video ->
                        navController.navigate(Screen.VideoDetail.createRoute(video.id, video.sourceType.name))
                    },
                    onSearchClick = {
                        navController.navigate(Screen.Search.route)
                    }
                )
            }

            // 2. Search Screen
            composable(Screen.Search.route) {
                val searchViewModel = remember { SearchViewModel(mediaRepository) }
                SearchScreen(
                    viewModel = searchViewModel,
                    onVideoClick = { video ->
                        navController.navigate(Screen.VideoDetail.createRoute(video.id, video.sourceType.name))
                    }
                )
            }

            // 3. Library Screen
            composable(Screen.Library.route) {
                LibraryScreen(
                    mediaRepository = mediaRepository,
                    onVideoClick = { video ->
                        navController.navigate(Screen.VideoDetail.createRoute(video.id, video.sourceType.name))
                    }
                )
            }

            // 4. Downloads Screen
            composable(Screen.Downloads.route) {
                DownloadsScreen(
                    downloadRepository = downloadRepository,
                    onPlayOfflineMedia = { video ->
                        navController.navigate(Screen.VideoDetail.createRoute(video.id, video.sourceType.name))
                    }
                )
            }

            // 5. Settings Screen
            composable(Screen.Settings.route) {
                SettingsScreen(mediaRepository = mediaRepository)
            }

            // 6. Video Detail Screen
            composable(
                route = Screen.VideoDetail.route,
                arguments = listOf(
                    navArgument("videoId") { type = NavType.StringType },
                    navArgument("sourceType") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val videoId = backStackEntry.arguments?.getString("videoId") ?: ""
                val sourceTypeName = backStackEntry.arguments?.getString("sourceType") ?: VideoSourceType.YOUTUBE.name
                val sourceType = VideoSourceType.valueOf(sourceTypeName)

                val detailViewModel = remember(videoId) {
                    VideoDetailViewModel(
                        videoId = videoId,
                        sourceType = sourceType,
                        mediaRepository = mediaRepository,
                        downloadRepository = downloadRepository,
                        playerManager = playerManager
                    )
                }

                VideoDetailScreen(
                    viewModel = detailViewModel,
                    onBackClick = { navController.popBackStack() },
                    onVideoClick = { related ->
                        navController.navigate(Screen.VideoDetail.createRoute(related.id, related.sourceType.name))
                    },
                    onFullscreenClick = { id, uri ->
                        navController.navigate(Screen.CustomPlayer.createRoute(id, uri))
                    }
                )
            }

            // 7. Custom Player Fullscreen Screen
            composable(
                route = Screen.CustomPlayer.route,
                arguments = listOf(
                    navArgument("videoId") { type = NavType.StringType },
                    navArgument("streamUri") { type = NavType.StringType }
                )
            ) {
                CustomPlayerScreen(
                    playerManager = playerManager,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}
