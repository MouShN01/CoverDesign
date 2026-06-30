package com.example.coverdesign.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.coverdesign.domain.repo.FireAuthRepository
import org.koin.compose.koinInject

// androidApp/ui/navigation/AppNavigation.kt


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

        val authRepo: FireAuthRepository = koinInject()
        val isAuthenticated by authRepo.isAuthenticated()
            .collectAsStateWithLifecycle(initialValue = false)

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