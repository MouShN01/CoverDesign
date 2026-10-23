package com.example.coverdesign.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.example.coverdesign.ui.screens.ProfileScreen
import com.example.coverdesign.ui.screens.config.ProductConfigScreen
import com.example.coverdesign.ui.screens.patient.ar.VirtualTryOnPlaceholderScreen
import com.example.coverdesign.ui.screens.patient.home.HomeScreen
import com.example.coverdesign.ui.screens.patient.order.OrderSummaryScreen
import com.example.coverdesign.ui.screens.patient.order.OrdersListScreen
import com.example.coverdesign.ui.screens.patient.preview.ModelTestScreen
import com.example.coverdesign.ui.screens.patient.preview.ModifiableModelPreviewScreen
import com.example.coverdesign.ui.screens.patient.rehab.RecommendationsScreen
import com.example.coverdesign.ui.screens.patient.rehab.RehabHistoryScreen
import com.example.coverdesign.ui.screens.patient.sensor.SensorCameraScreen
import com.example.coverdesign.ui.viewmodel.ConfigEntryMode
import com.example.coverdesign.ui.viewmodel.ModelPreviewAction
import com.example.coverdesign.ui.viewmodel.OrderType

fun NavGraphBuilder.patientGraph(
    navController: NavHostController
) {

    navigation(
        route = Screen.PatientMain.route,
        startDestination = Screen.Model3D.createRoute("test")
    ) {

        // ── test: local 3D model on launch ───────────────────────────
        composable(Screen.Model3D.route) {
            ModelTestScreen(onBack = { navController.navigate(Screen.Home.route) })
        }

        // ── bottom nav roots ──────────────────────────────────────────
        composable(Screen.Home.route) {
            HomeScreen(
                onNewOrder = { navController.navigate(Screen.NewOrder.route) },
                on3DVisualize = { navController.navigate(Screen.Model3D.route) },
                onVirtualTryOn = { navController.navigate(Screen.ArTryOn.createRoute("")) },
                onOrderClick = { orderId ->
                    navController.navigate(Screen.OrderDetail.createRoute(orderId))
                },
                onSeeAllOrders = { navController.navigate(Screen.Orders.route) },
                onOpenRehabHistory = { navController.navigate(Screen.RehabHistory.route) },
                onOpenRecommendations = { navController.navigate(Screen.PatientRecommendations.route) },
                onOrderInsoles = { navController.navigate(Screen.NewInsoleOrder.route) }
            )
        }
        composable(Screen.RehabHistory.route) {
            RehabHistoryScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.PatientRecommendations.route) {
            RecommendationsScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.SensorCamera.route) {
            SensorCameraScreen()
        }

        composable(Screen.ModelPreview.route) { entry ->
            val configId = entry.arguments?.getString("configId") ?: return@composable
            val taskId = entry.arguments?.getString("taskId") ?: return@composable
            println("loadAdaptedModel before ModifiableModelPreviewScreen: $configId")
            ModifiableModelPreviewScreen(
                configId = configId,
                taskId = taskId,
                onTryOn = { navController.navigate(Screen.ArTryOn.createRoute(configId)) },
                onOrder = {
                    navController.navigate(
                        Screen.OrderSummary.createRoute(configId)
                    )
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Orders.route) {
            OrdersListScreen(
                onOrderClick = { orderId ->
                    navController.navigate(Screen.OrderDetail.createRoute(orderId))
                }
            )
        }
        composable(Screen.Profile.route) {
            ProfileScreen()
        }

        composable(Screen.NewOrder.route) {
            ProductConfigScreen(
                entryMode = ConfigEntryMode.DirectNew(),
                onProceed = { taskId, configId ->
                    navController.navigate(Screen.ModelPreview.createRoute(taskId, configId))
                },
                onOrderSummary = { configId ->
                    navController.navigate(Screen.OrderSummary.createRoute(configId))
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.NewInsoleOrder.route) {
            ProductConfigScreen(
                entryMode = ConfigEntryMode.DirectNew(OrderType.INSOLE),
                onProceed = { taskId, configId ->
                    navController.navigate(Screen.ModelPreview.createRoute(taskId, configId))
                },
                onOrderSummary = { configId ->
                    navController.navigate(Screen.OrderSummary.createRoute(configId))
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.ProductConfigFromOrder.route) { entry ->
            val configId = entry.arguments?.getString("configId") ?: return@composable
            ProductConfigScreen(
                entryMode = ConfigEntryMode.FromExistingConfig(configId),
                onProceed = { taskId, newConfigId ->
                    navController.navigate(Screen.ModelPreview.createRoute(taskId, newConfigId))
                },
                onOrderSummary = { configId ->
                    navController.navigate(Screen.OrderSummary.createRoute(configId))
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.ArTryOn.route) { entry ->
            val configId = entry.arguments?.getString("configId").orEmpty()
            VirtualTryOnPlaceholderScreen(
                configId = configId,
                onOrder = { navController.navigate(Screen.OrderSummary.createRoute(configId)) },
                onEditConfig = { navController.navigate(Screen.ProductConfigFromOrder.createRoute(configId)) },
                onBack = { navController.popBackStack() },
            )
        }
        composable(Screen.OrderSummary.route) { entry ->
            val configId = entry.arguments?.getString("configId") ?: return@composable
            OrderSummaryScreen(
                orderId = configId,
                onOrderSubmitted = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onView3DModel = { _, adaptedModelId ->
                    if (adaptedModelId != null)
                        navController.navigate(Screen.ModelPreviewFromOrder.createRoute(adaptedModelId))
                },
                onBack = { navController.popBackStack() },
                onNavigateHome = {
                    navController.navigate(Screen.Home.route)
                }
            )
        }
        composable(Screen.OrderDetail.route) { entry ->
            val orderId = entry.arguments?.getString("orderId") ?: return@composable
            OrderSummaryScreen(
                orderId = orderId,
                onOrderSubmitted = { navController.popBackStack() },
                onView3DModel = { _, adaptedModelId ->
                    if (adaptedModelId != null)
                        navController.navigate(Screen.ModelPreviewFromOrder.createRoute(adaptedModelId))
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.ModelPreviewFromOrder.route) { entry ->
            val adaptedModelId = entry.arguments?.getString("adaptedModelId") ?: return@composable
            ModifiableModelPreviewScreen(
                configId = adaptedModelId,
                taskId   = null,
                onTryOn  = { navController.navigate(Screen.ArTryOn.createRoute(adaptedModelId)) },
                onOrder  = { navController.popBackStack() },
                onBack   = { navController.popBackStack() },
                loadAction = ModelPreviewAction.LoadFromOrder(adaptedModelId)
            )
        }
    }
}
