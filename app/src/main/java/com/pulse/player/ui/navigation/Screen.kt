package com.pulse.player.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Search : Screen("search")
    object Library : Screen("library")
    object Downloads : Screen("downloads")
    object Settings : Screen("settings")

    object VideoDetail : Screen("video_detail/{videoId}/{sourceType}") {
        fun createRoute(videoId: String, sourceType: String): String = "video_detail/$videoId/$sourceType"
    }

    object CustomPlayer : Screen("custom_player/{videoId}/{streamUri}") {
        fun createRoute(videoId: String, streamUri: String): String = "custom_player/$videoId/${android.net.Uri.encode(streamUri)}"
    }
}
