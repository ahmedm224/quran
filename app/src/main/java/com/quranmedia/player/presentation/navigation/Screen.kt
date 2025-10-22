package com.quranmedia.player.presentation.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Reciters : Screen("reciters")
    object Surahs : Screen("surahs/{reciterId}") {
        fun createRoute(reciterId: String) = "surahs/$reciterId"
    }
    object Player : Screen("player/{reciterId}/{surahNumber}?resume={resume}&startAyah={startAyah}") {
        fun createRoute(reciterId: String, surahNumber: Int, resume: Boolean = false, startAyah: Int? = null) =
            "player/$reciterId/$surahNumber?resume=$resume${startAyah?.let { "&startAyah=$it" } ?: ""}"
    }
    object Bookmarks : Screen("bookmarks")
    object Downloads : Screen("downloads")
    object Search : Screen("search")
    object Settings : Screen("settings")
    object About : Screen("about")
}
