package com.quranmedia.player.presentation.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Reciters : Screen("reciters")
    object Surahs : Screen("surahs/{reciterId}") {
        fun createRoute(reciterId: String) = "surahs/$reciterId"
    }
    object Player : Screen("player/{reciterId}/{surahNumber}?resume={resume}") {
        fun createRoute(reciterId: String, surahNumber: Int, resume: Boolean = false) =
            "player/$reciterId/$surahNumber?resume=$resume"
    }
    object Bookmarks : Screen("bookmarks")
    object Downloads : Screen("downloads")
    object Settings : Screen("settings")
}
