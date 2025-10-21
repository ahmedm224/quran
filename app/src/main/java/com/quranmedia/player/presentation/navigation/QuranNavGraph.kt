package com.quranmedia.player.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.quranmedia.player.presentation.screens.home.HomeScreenNew
import com.quranmedia.player.presentation.screens.player.PlayerScreenNew
import com.quranmedia.player.presentation.screens.reciters.RecitersScreenNew
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
                    // Navigate to reciters first - user must select a reciter before browsing surahs
                    navController.navigate(Screen.Reciters.route)
                },
                onNavigateToPlayer = { reciterId, surahNumber, resume ->
                    navController.navigate(Screen.Player.createRoute(reciterId, surahNumber, resume))
                }
            )
        }

        composable(Screen.Reciters.route) {
            RecitersScreenNew(
                onReciterClick = { reciter ->
                    navController.navigate(Screen.Surahs.createRoute(reciter.id))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Surahs.route,
            arguments = listOf(
                navArgument("reciterId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val reciterId = backStackEntry.arguments?.getString("reciterId") ?: return@composable

            SurahsScreenNew(
                onSurahClick = { surah ->
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
                }
            )
        ) { backStackEntry ->
            val reciterId = backStackEntry.arguments?.getString("reciterId") ?: return@composable
            val surahNumber = backStackEntry.arguments?.getInt("surahNumber") ?: return@composable
            val resume = backStackEntry.arguments?.getBoolean("resume") ?: false

            PlayerScreenNew(
                reciterId = reciterId,
                surahNumber = surahNumber,
                resumeFromSaved = resume,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
