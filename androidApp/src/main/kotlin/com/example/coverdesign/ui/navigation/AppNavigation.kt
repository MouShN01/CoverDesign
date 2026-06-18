package com.example.coverdesign.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.coverdesign.ui.screens.CatalogScreen
import com.example.coverdesign.ui.screens.ModelPreviewScreen
import com.example.coverdesign.ui.screens.OrderSummaryScreen
import com.example.coverdesign.ui.screens.ProductConfigScreen
import com.example.coverdesign.ui.screens.ar.ArTryOnScreen

sealed class Screen(val route: String) {
    object Catalog       : Screen("catalog")
    object ProductConfig : Screen("config/{productId}") {
        fun createRoute(productId: String) = "config/$productId"
    }
    object ModelPreview  : Screen("preview/{configId}") {
        fun createRoute(configId: String) = "preview/$configId"
    }
    object ArTryOn       : Screen("ar/{configId}") {
        fun createRoute(configId: String) = "ar/$configId"
    }
    object OrderSummary  : Screen("order/{configId}") {
        fun createRoute(configId: String) = "order/$configId"
    }
}

@Composable
fun AppNavigation(navController: NavHostController = rememberNavController()) {
    NavHost(
        navController    = navController,
        startDestination = Screen.Catalog.route
    ) {
        composable(Screen.Catalog.route) {
            CatalogScreen(
                onProductSelected = { product ->
                    navController.navigate(Screen.ProductConfig.createRoute(product.id))
                }
            )
        }
        composable(Screen.ProductConfig.route) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId") ?: return@composable
            ProductConfigScreen(
                productId     = productId,
                onProceed     = { configId ->
                    navController.navigate(Screen.ModelPreview.createRoute(configId))
                },
                onBack        = { navController.popBackStack() }
            )
        }
        composable(Screen.ModelPreview.route) { backStackEntry ->
            val configId = backStackEntry.arguments?.getString("configId") ?: return@composable
            ModelPreviewScreen(
                configId      = configId,
                onTryOn       = { navController.navigate(Screen.ArTryOn.createRoute(configId)) },
                onOrder       = { navController.navigate(Screen.OrderSummary.createRoute(configId)) },
                onBack        = { navController.popBackStack() }
            )
        }
        composable(Screen.ArTryOn.route) { backStackEntry ->
            val configId = backStackEntry.arguments?.getString("configId") ?: return@composable
            ArTryOnScreen(
                configId      = configId,
                onProceedToOrder = { navController.navigate(Screen.OrderSummary.createRoute(configId)) },
                onBack        = { navController.popBackStack() }
            )
        }
        composable(Screen.OrderSummary.route) { backStackEntry ->
            val configId = backStackEntry.arguments?.getString("configId") ?: return@composable
            OrderSummaryScreen(
                configId      = configId,
                onOrderSubmitted = { navController.popBackStack(Screen.Catalog.route, false) },
                onBack        = { navController.popBackStack() }
            )
        }
    }
}