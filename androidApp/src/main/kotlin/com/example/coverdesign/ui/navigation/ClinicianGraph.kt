package com.example.coverdesign.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.example.coverdesign.ui.screens.ProfileScreen
import com.example.coverdesign.ui.screens.clinician.home.ClinicianHomeScreen

fun NavGraphBuilder.clinicianGraph(
    navController: NavHostController
) {
    navigation(
        route = Screen.ClinicianMain.route,
        startDestination = Screen.ClinicianHome.route
    ) {
        composable(Screen.ClinicianHome.route) {
            ClinicianHomeScreen(
                onPatientClick = { /* TODO: navigate to patient detail */ }
            )
        }
        composable(Screen.Profile.route) {
            ProfileScreen()
        }
    }
}
