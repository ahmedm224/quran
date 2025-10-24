package com.quranmedia.player.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.quranmedia.player.presentation.screens.about.AboutScreen
import com.quranmedia.player.presentation.screens.bookmarks.BookmarksScreen
import com.quranmedia.player.presentation.screens.downloads.DownloadsScreen
import com.quranmedia.player.presentation.screens.home.HomeScreenNew
import com.quranmedia.player.presentation.screens.player.PlayerScreenNew
import com.quranmedia.player.presentation.screens.reciters.RecitersScreenNew
import com.quranmedia.player.presentation.screens.search.SearchScreen
import com.quranmedia.player.presentation.screens.surahs.SurahsScreenNew

@Composable
fun QuranNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreenNew(
                onNavigateToReciters = { navController.navigate(Screen.Reciters.route) },
                onNavigateToSurahs = {
                    // Navigate directly to Surahs screen with reciter dropdown
                    navController.navigate(Screen.Surahs.route)
                },
                onNavigateToPlayer = { reciterId, surahNumber, resume ->
                    navController.navigate(Screen.Player.createRoute(reciterId, surahNumber, resume))
                },
                onNavigateToBookmarks = { navController.navigate(Screen.Bookmarks.route) },
                onNavigateToSearch = { navController.navigate(Screen.Search.route) },
                onNavigateToAbout = { navController.navigate(Screen.About.route) },
                onNavigateToDownloads = { navController.navigate(Screen.Downloads.route) }
            )
        }

        composable(Screen.Reciters.route) {
            RecitersScreenNew(
                onReciterClick = { reciter ->
                    // Navigate to unified Surahs screen (reciter selection handled in that screen)
                    navController.navigate(Screen.Surahs.route)
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Surahs.route) {
            SurahsScreenNew(
                onSurahClick = { reciterId, surah ->
                    navController.navigate(Screen.Player.createRoute(reciterId, surah.number))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Player.route,
            arguments = listOf(
                navArgument("reciterId") { type = NavType.StringType },
                navArgument("surahNumber") { type = NavType.IntType },
                navArgument("resume") {
                    type = NavType.BoolType
                    defaultValue = false
                },
                navArgument("startAyah") {
                    type = NavType.IntType
                    defaultValue = -1
                }
            )
        ) { backStackEntry ->
            val reciterId = backStackEntry.arguments?.getString("reciterId") ?: return@composable
            val surahNumber = backStackEntry.arguments?.getInt("surahNumber") ?: return@composable
            val resume = backStackEntry.arguments?.getBoolean("resume") ?: false
            val startAyah = backStackEntry.arguments?.getInt("startAyah")?.takeIf { it > 0 }

            PlayerScreenNew(
                reciterId = reciterId,
                surahNumber = surahNumber,
                resumeFromSaved = resume,
                startFromAyah = startAyah,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Bookmarks.route) {
            BookmarksScreen(
                onNavigateBack = { navController.popBackStack() },
                onBookmarkClick = { reciterId, surahNumber, ayahNumber, positionMs ->
                    // Navigate to player with the bookmarked ayah
                    navController.navigate(Screen.Player.createRoute(reciterId, surahNumber, resume = false, startAyah = ayahNumber))
                }
            )
        }

        composable(Screen.Search.route) {
            SearchScreen(
                onNavigateBack = { navController.popBackStack() },
                onResultClick = { surahNumber, ayahNumber ->
                    // Navigate to player with the found ayah
                    // Use a default reciter - ar.abdulbasitmurattal
                    navController.navigate(Screen.Player.createRoute("ar.abdulbasitmurattal", surahNumber, resume = false, startAyah = ayahNumber))
                }
            )
        }

        composable(Screen.About.route) {
            AboutScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Downloads.route) {
            DownloadsScreen(
                onNavigateBack = { navController.popBackStack() },
                onDownloadClick = { reciterId, surahNumber ->
                    navController.navigate(Screen.Player.createRoute(reciterId, surahNumber))
                }
            )
        }
    }
}
