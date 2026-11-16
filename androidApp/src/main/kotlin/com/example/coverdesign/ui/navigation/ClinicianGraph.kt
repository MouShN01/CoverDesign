package com.example.coverdesign.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import com.example.coverdesign.ui.screens.ProfileScreen
import com.example.coverdesign.ui.screens.clinician.home.ClinicianHomeScreen
import com.example.coverdesign.ui.screens.clinician.patients.ClinicianPatientsScreen
import com.example.coverdesign.ui.screens.clinician.recommendation.PatientRecommendationData
import com.example.coverdesign.ui.screens.clinician.recommendation.WriteRecommendationScreen

fun NavGraphBuilder.clinicianGraph(
    navController: NavHostController
) {
    navigation(
        route = Screen.ClinicianMain.route,
        startDestination = Screen.ClinicianHome.route
    ) {
        composable(Screen.ClinicianHome.route) {
            ClinicianHomeScreen(
                onPatientClick = { patientId ->
                    navController.navigate(Screen.WriteRecommendation.createRoute(patientId))
                },
                onNotificationsClick = {
                    TODO("Clinician notifications screen not yet implemented")
                },
                onViewAllPatientsClick = {
                    navController.navigate(ClinicianNavItem.Patients.route)
                }
            )
        }
        composable(Screen.Profile.route) {
            ProfileScreen()
        }
        composable(ClinicianNavItem.Patients.route) {
            ClinicianPatientsScreen(
                onBack = { navController.popBackStack() },
                onPatientClick = { patientId ->
                    navController.navigate(Screen.WriteRecommendation.createRoute(patientId))
                }
            )
        }
        // TODO: implement reports/analytics screen
        composable(ClinicianNavItem.Reports.route) {
            TODO("Clinician reports screen not yet implemented")
        }
        // TODO: implement clinician settings screen
        composable(ClinicianNavItem.Settings.route) {
            TODO("Clinician settings screen not yet implemented")
        }
        composable(
            route = Screen.WriteRecommendation.route,
            arguments = listOf(navArgument("patientId") { type = NavType.StringType })
        ) { backStackEntry ->
            val patientId = backStackEntry.arguments?.getString("patientId").orEmpty()
            // TODO: replace with ViewModel-driven data fetched by patientId
            WriteRecommendationScreen(
                patientData = PatientRecommendationData(
                    patientName = "Maria L.",
                    treatmentType = "Post-operative Rehab",
                    lastUpdated = "Updated 2h ago",
                    kyphosisCoeff = 12.4f,
                    asymmetryCoeff = 0.82f,
                    pressureNote = "Left foot imbalance",
                    gaitPattern = "Irregular"
                ),
                onBack = { navController.popBackStack() },
                onSend = { _, _ -> navController.popBackStack() }
            )
        }
    }
}