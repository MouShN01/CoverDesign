package com.example.coverdesign.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.example.coverdesign.ui.screens.CatalogScreen
import com.example.coverdesign.ui.screens.OrderSummaryScreen
import com.example.coverdesign.ui.screens.OrdersListScreen
import com.example.coverdesign.ui.screens.ProfileScreen
import com.example.coverdesign.ui.screens.ar.VirtualTryOnPlaceholderScreen
import com.example.coverdesign.ui.screens.config.ProductConfigScreen
import com.example.coverdesign.ui.screens.home.HomeScreen
import com.example.coverdesign.ui.screens.preview.ModifiableModelPreviewScreen
import com.example.coverdesign.ui.viewmodel.ConfigEntryMode
import com.example.coverdesign.ui.viewmodel.ModelPreviewAction

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
                onNewOrder = { navController.navigate(Screen.Catalog.route) },
                on3DVisualize = { navController.navigate(Screen.Model3D.route) },
                onVirtualTryOn = { navController.navigate(Screen.ArTryOn.createRoute("")) },
                onOrderClick = { orderId ->
                    navController.navigate(Screen.OrderDetail.createRoute(orderId))
                },
                onSeeAllOrders = { navController.navigate(Screen.Orders.route) }
            )
        }
        // in MainScaffold NavHost
//        composable(Screen.Model3D.route) {
//            CatalogScreen(
//                onProductSelected = { product ->
//                    // navigate to preview loading base model — no config needed
//                    navController.navigate(
//                        Screen.Model3D.createRoute(productId = product.id)
//                    )
//                }
//            )
//        }

        composable(Screen.ModelPreview.route) { entry ->
            val configId = entry.arguments?.getString("configId") ?: return@composable
            val taskId = entry.arguments?.getString("taskId") ?: return@composable

            // figure out if this is a productId (from 3D tab) or configId (from config flow)
            // convention: productIds start with "prod-", configIds are UUIDs
//            val isProductId = id.startsWith("prod-")
            println("loadAdaptedModel before ModifiableModelPreviewScreen: $configId")
            ModifiableModelPreviewScreen(
                configId = configId,
                taskId = taskId,
                onTryOn = { navController.navigate(Screen.ArTryOn.createRoute(configId)) },
                onOrder = {
                    navController.navigate(
                        Screen.OrderSummary
                            .createRoute(configId)
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
                onProceed = { taskId, configId ->
                    navController.navigate(
                        Screen.ModelPreview.createRoute(
                            taskId, configId
                        )
                    )
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
                onBack = { navController.popBackStack() }
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
            val configId = entry.arguments?.getString("configId").orEmpty()
//            ArSceneViewComposable(
//                configId     = configId,
//                onOrder      = { navController.navigate(Screen.OrderSummary.createRoute(configId)) },
//                onEditConfig = { navController.navigate(Screen.ProductConfigFromOrder.createRoute(configId)) },
//                onBack       = { navController.popBackStack() }
//            )
            VirtualTryOnPlaceholderScreen(
                configId = configId,
                onOrder = {  navController.navigate(Screen.OrderSummary.createRoute(configId)) },
                onEditConfig = { navController.navigate(Screen.ProductConfigFromOrder.createRoute(configId)) },
                onBack = { navController.popBackStack() },
            )

        }
//        composable(Screen.ArTryOn.route) { entry ->
//            val configId = entry.arguments?.getString("configId").orEmpty()
//
//            // ArMode is resolved from the ProductConfig stored locally.
//            // We read it here so navigation can decide which composable to show.
//            val config by productRepo
//                .getConfigById(configId)
//                .collectAsStateWithLifecycle(initialValue = null)
//
//            config?.let { cfg ->
//                val arMode = cfg.productCategory.resolveArMode(
//                    photoUri     = cfg.userPhotoUri,
//                    measurements = cfg.measurement
//                )
//
//                when (arMode) {
//                    // ── JEWELRY / ACCESSORY — overlay on user photo ───────────────
//                    is ArMode.OnUserPhoto -> ArSceneViewComposable(
//                        configId     = configId,
//                        photoUri     = arMode.photoUri,
//                        onOrder      = { navController.navigate(Screen.OrderSummary.createRoute(configId)) },
//                        onEditConfig = { navController.navigate(Screen.ProductConfigFromOrder.createRoute(configId)) },
//                        onBack       = { navController.popBackStack() }
//                    )
//
//                    // ── CLOTHING / FOOTWEAR / THREE_D_PRINT — body silhouette ─────
//                    is ArMode.OnBodySilhouette -> VirtualTryOnPlaceholderScreen(
//                        configId     = configId,
//                        measurements = arMode.measurements,
//                        category     = cfg.productCategory,
//                        onOrder      = { navController.navigate(Screen.OrderSummary.createRoute(configId)) },
//                        onEditConfig = { navController.navigate(Screen.ProductConfigFromOrder.createRoute(configId)) },
//                        onBack       = { navController.popBackStack() }
//                    )
//
//                    // ── INTERIOR_DECOR / OTHER — room space ───────────────────────
//                    is ArMode.InRoomSpace -> ArSceneViewComposable(
//                        configId     = configId,
//                        photoUri     = null,   // no reference photo — ARCore uses live camera
//                        onOrder      = { navController.navigate(Screen.OrderSummary.createRoute(configId)) },
//                        onEditConfig = { navController.navigate(Screen.ProductConfigFromOrder.createRoute(configId)) },
//                        onBack       = { navController.popBackStack() }
//                    )
//                }
//            } ?: LoadingScreen()   // brief moment while config loads from local DB
//        }
        composable(Screen.OrderSummary.route) { entry ->
            val configId = entry.arguments?.getString("configId") ?: return@composable
            OrderSummaryScreen(
                configId = configId,
                onOrderSubmitted = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onView3DModel = { _, adaptedModelId ->
                    if (adaptedModelId != null)
                        navController.navigate(Screen.ModelPreviewFromOrder.createRoute(adaptedModelId))
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.OrderDetail.route) { entry ->
            val orderId = entry.arguments?.getString("orderId") ?: return@composable
            OrderSummaryScreen(
                configId = orderId,
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