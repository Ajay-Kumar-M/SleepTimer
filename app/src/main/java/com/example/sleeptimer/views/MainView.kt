package com.example.sleeptimer.views

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.sleeptimer.components.Screen
import com.example.sleeptimer.ui.theme.SleepTimerTheme
import com.example.sleeptimer.viewModel.HomeScreenViewModel

@Composable
fun MainView() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.MainView.route) {
        composable(Screen.MainView.route) {
            HomeScreen(navController)
        }
        composable(Screen.SettingsView.route) {
            HomeScreen(navController)
        }
    }
}

@Composable
fun HomeScreen(
    navController: NavHostController,
    viewModel: HomeScreenViewModel = androidx.lifecycle.viewmodel.compose.viewModel( )
) {

}
@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SleepTimerTheme {
        MainView()
    }
}