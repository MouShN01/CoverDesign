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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.coverdesign.domain.repo.AuthRepository
import org.koin.compose.koinInject

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

@Composable
fun AppScaffold() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // screens where bottom bar is hidden
    val hideBottomBar = currentRoute in listOf(
        //Screen.ProductConfig.route,
        Screen.ModelPreview.route,
        Screen.ArTryOn.route,
        Screen.OrderSummary.route,
        Screen.Login.route,
        Screen.Register.route,
        Screen.ProductConfigFromCatalog.route,
        Screen.ProductConfigFromOrder.route
    )

    Scaffold(
        bottomBar = {
            if (!hideBottomBar) {
                AppBottomNavBar(
                    currentRoute  = currentRoute,
                    onItemSelected = { item ->
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                        }
                    }
//                    onItemSelected = { item ->
//                        navController.navigate(item.route) {
//                            popUpTo(Screen.Home.route) { saveState = true }
//                            launchSingleTop = true
//                            restoreState    = true
//                        }
//                    }
                )
            }
        }
    ) { padding ->

        val authRepo: AuthRepository = koinInject()
        val isAuthenticated by authRepo.isAuthenticated()
            .collectAsStateWithLifecycle(initialValue = false)
        // auth flow — no bottom bar

        NavHost(
            navController = navController,
            startDestination = if (isAuthenticated) Screen.Main.route else Screen.Auth.route,
            modifier = Modifier.padding(padding)
        ) {
            authGraph(navController)
            mainGraph(navController)
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