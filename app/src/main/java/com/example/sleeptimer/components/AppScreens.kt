package com.example.sleeptimer.components

sealed class Screen(val route: String) {
    object MainView: Screen("MainView")
    object SettingsView: Screen("SettingsView")

}