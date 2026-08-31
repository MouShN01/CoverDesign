package com.example.coverdesign.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import com.example.coverdesign.ui.screens.ProfileScreen
import com.example.coverdesign.ui.screens.clinician.home.ClinicianHomeScreen
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
                }
            )
        }
        composable(Screen.Profile.route) {
            ProfileScreen()
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