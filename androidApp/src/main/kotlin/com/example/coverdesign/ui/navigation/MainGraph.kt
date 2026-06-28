package com.example.coverdesign.ui.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.example.coverdesign.ui.screens.CatalogScreen
import com.example.coverdesign.ui.screens.OrderSummaryScreen
import com.example.coverdesign.ui.screens.OrdersListScreen
import com.example.coverdesign.ui.screens.ProfileScreen
import com.example.coverdesign.ui.screens.ar.ARScreen
import com.example.coverdesign.ui.screens.config.ProductConfigScreen
import com.example.coverdesign.ui.screens.home.HomeScreen
import com.example.coverdesign.ui.screens.preview.ModelPreviewScreen
import com.example.coverdesign.ui.viewmodel.ConfigEntryMode
import com.example.coverdesign.ui.viewmodel.ModelPreviewAction
import com.example.coverdesign.ui.viewmodel.ModelPreviewViewModel
import org.koin.compose.viewmodel.koinViewModel

fun NavGraphBuilder.mainGraph(
    navController: NavHostController
) {

    navigation(
        route = "main",
        startDestination = Screen.Home.route
    ) {

        // ── bottom nav roots ──────────────────────────────────────────
        composable(Screen.Home.route) {
            HomeScreen(
                onNewOrder      = { navController.navigate(Screen.Catalog.route) },
                on3DVisualize   = { navController.navigate(Screen.Model3D.route) },
                onVirtualTryOn  = { navController.navigate(Screen.ArTryOn.createRoute("")) },
                onOrderClick    = { orderId ->
                    navController.navigate(Screen.OrderDetail.createRoute(orderId))
                },
                onSeeAllOrders  = { navController.navigate(Screen.Orders.route) }
            )
        }
        // in MainScaffold NavHost
        composable(Screen.Model3D.route) {
            CatalogScreen(
                onProductSelected = { product ->
                    // navigate to preview loading base model — no config needed
                    navController.navigate(
                        Screen.ModelPreview.createRoute(product.id)
                    )
                }
            )
        }

        composable(Screen.ModelPreview.route) { entry ->
            val id = entry.arguments?.getString("configId") ?: return@composable

            // figure out if this is a productId (from 3D tab) or configId (from config flow)
            // convention: productIds start with "prod-", configIds are UUIDs
            val isProductId = id.startsWith("prod-")

            val viewModel: ModelPreviewViewModel = koinViewModel()

            LaunchedEffect(id) {
                if (isProductId) {
                    viewModel.onAction(ModelPreviewAction.LoadForProduct(id))
                }
                // LoadModel is triggered by ProductConfigScreen via SaveAndProceed
            }
            LaunchedEffect(id) {
                if (isProductId) viewModel.onAction(ModelPreviewAction.LoadForProduct(id))
                else viewModel.onAction(ModelPreviewAction.LoadForConfig(id))
            }

            ModelPreviewScreen(
                configId = id,
                onTryOn  = { navController.navigate(Screen.ArTryOn.createRoute(id)) },
                onOrder  = { navController.navigate(Screen.OrderSummary.createRoute(id)) },
                onBack   = { navController.popBackStack() }
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

        // ── catalog → config flow ─────────────────────────────────────
        composable(Screen.Catalog.route) {
            CatalogScreen(
                onProductSelected = { product ->
                    navController.navigate(Screen.ProductConfigFromCatalog.createRoute(product.id))
                    //navController.navigate(Screen.ProductConfig.createRoute(product.id))
                }
            )
        }
        composable(Screen.ProductConfigFromCatalog.route) { entry ->
            val productId = entry.arguments?.getString("productId") ?: return@composable
            ProductConfigScreen(
                entryMode = ConfigEntryMode.FromProduct(productId),
                onProceed = { id -> navController.navigate(Screen.ModelPreview.createRoute(id)) },
                onBack    = { navController.popBackStack() }
            )
        }
        composable(Screen.ProductConfigFromOrder.route) { entry ->
            val configId = entry.arguments?.getString("configId") ?: return@composable
            ProductConfigScreen(
                entryMode = ConfigEntryMode.FromExistingConfig(configId),
                onProceed = { id -> navController.navigate(Screen.ModelPreview.createRoute(id)) },
                onBack    = { navController.popBackStack() }
            )
        }
//        composable(Screen.ProductConfig.route) { entry ->
//            val productId = entry.arguments?.getString("productId") ?: return@composable
//            ProductConfigScreen(
//                productId = productId,
//                onProceed = { configId ->
//                    navController.navigate(Screen.ModelPreview.createRoute(configId))
//                },
//                onBack    = { navController.popBackStack() }
//            )
//        }
        composable(Screen.ArTryOn.route) { entry ->
            val configId = entry.arguments?.getString("configId") ?: return@composable
            //shared-assets/models/Astronaut.glb
            val modelUrl = "https://modelviewer.dev/shared-assets/models/glTF-Sample-Assets/Models/MaterialsVariantsShoe/glTF-Binary/MaterialsVariantsShoe.glb"
            val helmet = "test_models/damaged_helmet.glb"
            ARScreen(modelUrl = helmet)
//            ArTryOnScreen(
//                configId           = configId,
//                onProceedToOrder   = { navController.navigate(Screen.OrderSummary.createRoute(configId)) },
//                onUpdateParameters = { navController.navigate(Screen.ProductConfigFromOrder.createRoute(configId)) },
//                onBack             = { navController.popBackStack() }
//            )
        }
        composable(Screen.OrderSummary.route) { entry ->
            val configId = entry.arguments?.getString("configId") ?: return@composable
            OrderSummaryScreen(
                configId         = configId,
                onOrderSubmitted = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onBack           = { navController.popBackStack() }
            )
        }
        composable(Screen.OrderDetail.route) { entry ->
            val orderId = entry.arguments?.getString("orderId") ?: return@composable
            OrderSummaryScreen(
                configId         = orderId,
                onOrderSubmitted = { navController.popBackStack() },
                onBack           = { navController.popBackStack() }
            )
        }
    }
}