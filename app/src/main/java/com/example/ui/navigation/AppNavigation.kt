package com.example.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object ActiveFocus : Screen("active_focus")
    object SessionComplete : Screen("session_complete")
    object SessionFailed : Screen("session_failed")
    object Stats : Screen("stats")
    object Settings : Screen("settings")
}
