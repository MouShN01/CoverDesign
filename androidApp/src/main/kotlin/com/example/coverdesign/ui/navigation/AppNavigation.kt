package com.example.coverdesign.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.ViewInAr
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.coverdesign.domain.repo.AuthRepository
import com.example.coverdesign.ui.screens.CatalogScreen
import com.example.coverdesign.ui.screens.HomeScreen
import com.example.coverdesign.ui.screens.OrderSummaryScreen
import com.example.coverdesign.ui.screens.OrdersListScreen
import com.example.coverdesign.ui.screens.ProductConfigScreen
import com.example.coverdesign.ui.screens.ProfileScreen
import com.example.coverdesign.ui.screens.ar.ArTryOnScreen
import com.example.coverdesign.ui.screens.auth.LoginScreen
import com.example.coverdesign.ui.screens.auth.RegisterScreen
import com.example.coverdesign.ui.screens.preview.ModelPreviewScreen
import com.example.coverdesign.ui.viewmodel.ModelPreviewAction
import com.example.coverdesign.ui.viewmodel.ModelPreviewViewModel
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

// androidApp/ui/navigation/AppNavigation.kt

// ── Bottom nav items ──────────────────────────────────────────────────────────
sealed class BottomNavItem(
    val route    : String,
    val label    : String,
    val icon     : ImageVector,
    val iconSelected: ImageVector = icon
) {
    object Home    : BottomNavItem("home",    "Home",    Icons.Outlined.Home,
        Icons.Filled.Home)
    object Model3D : BottomNavItem("3d",      "3D",      Icons.Outlined.ViewInAr,
        Icons.Filled.ViewInAr)
    object Orders  : BottomNavItem("orders",  "Orders",  Icons.Outlined.ShoppingBag,
        Icons.Filled.ShoppingBag)
    object Profile : BottomNavItem("profile", "Profile", Icons.Outlined.Person,
        Icons.Filled.Person)
}

val bottomNavItems = listOf(
    BottomNavItem.Home,
    BottomNavItem.Model3D,
    BottomNavItem.Orders,
    BottomNavItem.Profile
)

// ── All screens ───────────────────────────────────────────────────────────────
sealed class Screen(val route: String) {
    // auth
    object Login         : Screen("login")
    object Register      : Screen("register")
    // bottom nav roots
    object Home          : Screen("home")
    object Model3D       : Screen("3d")
    object Orders        : Screen("orders")
    object Profile       : Screen("profile")
    // detail screens
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
    object OrderDetail   : Screen("order/detail/{orderId}") {
        fun createRoute(orderId: String) = "order/detail/$orderId"
    }
}

// ── Root navigation ───────────────────────────────────────────────────────────
@Composable
fun AppNavigation() {

    val navController = rememberNavController()
    val authRepo: AuthRepository = koinInject()
    val isAuthenticated by authRepo.isAuthenticated()
        .collectAsStateWithLifecycle(initialValue = false)

    if (!isAuthenticated) {
        // auth flow — no bottom bar
        NavHost(
            navController    = navController,
            startDestination = Screen.Login.route
        ) {
            composable(Screen.Login.route) {
                LoginScreen(
                    onLoginSuccess       = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = {
                        navController.navigate(Screen.Register.route)
                    }
                )
            }
            composable(Screen.Register.route) {
                RegisterScreen(
                    onRegisterSuccess  = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToLogin  = { navController.popBackStack() }
                )
            }
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
        }
    } else {
        // main app — with bottom bar
        MainScaffold(navController)
    }
}

// ── Main scaffold with persistent bottom bar ──────────────────────────────────
@Composable
fun MainScaffold(navController: NavHostController) {
    //val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // screens where bottom bar is hidden
    val hideBottomBar = currentRoute in listOf(
        Screen.ProductConfig.route,
        Screen.ModelPreview.route,
        Screen.ArTryOn.route,
        Screen.OrderSummary.route
    )

    Scaffold(
        bottomBar = {
            if (!hideBottomBar) {
                AppBottomNavBar(
                    currentRoute  = currentRoute,
                    onItemSelected = { item ->
                        navController.navigate(item.route) {
                            popUpTo(Screen.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState    = true
                        }
                    }
                )
            }
        }
    ) { padding ->
        NavHost(
            navController    = navController,
            startDestination = Screen.Home.route,
            modifier         = Modifier.padding(padding)
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
                        navController.navigate(Screen.ProductConfig.createRoute(product.id))
                    }
                )
            }
            composable(Screen.ProductConfig.route) { entry ->
                val productId = entry.arguments?.getString("productId") ?: return@composable
                ProductConfigScreen(
                    productId = productId,
                    onProceed = { configId ->
                        navController.navigate(Screen.ModelPreview.createRoute(configId))
                    },
                    onBack    = { navController.popBackStack() }
                )
            }
            composable(Screen.ModelPreview.route) { entry ->
                val configId = entry.arguments?.getString("configId") ?: return@composable
                ModelPreviewScreen(
                    configId = configId,
                    onTryOn  = { navController.navigate(Screen.ArTryOn.createRoute(configId)) },
                    onOrder  = { navController.navigate(Screen.OrderSummary.createRoute(configId)) },
                    onBack   = { navController.popBackStack() }
                )
            }
            composable(Screen.ArTryOn.route) { entry ->
                val configId = entry.arguments?.getString("configId") ?: return@composable
                ArTryOnScreen(
                    configId         = configId,
                    onProceedToOrder = {
                        navController.navigate(Screen.OrderSummary.createRoute(configId))
                    },
                    onBack           = { navController.popBackStack() }
                )
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
}

// ── Bottom nav bar ────────────────────────────────────────────────────────────
@Composable
fun AppBottomNavBar(
    currentRoute   : String?,
    onItemSelected : (BottomNavItem) -> Unit
) {
    NavigationBar {
        bottomNavItems.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                selected = selected,
                onClick  = { onItemSelected(item) },
                icon     = {
                    Icon(
                        imageVector        = if (selected) item.iconSelected else item.icon,
                        contentDescription = item.label
                    )
                },
                label    = { Text(item.label) }
            )
        }
    }
}