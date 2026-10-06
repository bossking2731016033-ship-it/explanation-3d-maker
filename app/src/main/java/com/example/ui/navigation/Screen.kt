package com.example.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Home : Screen("home")
    object Loading : Screen("loading")
    object Result : Screen("result")
    object History : Screen("history")
    object Settings : Screen("settings")
}
