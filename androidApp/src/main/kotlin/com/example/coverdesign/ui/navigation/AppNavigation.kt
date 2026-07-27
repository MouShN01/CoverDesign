package com.example.coverdesign.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.coverdesign.domain.repo.FireAuthRepository
import org.koin.compose.koinInject

@Composable
fun AppScaffold() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val hideBottomBar = currentRoute in listOf(
        Screen.ModelPreview.route,
        Screen.ModelPreviewFromOrder.route,
        Screen.ArTryOn.route,
        Screen.OrderSummary.route,
        Screen.Login.route,
        Screen.Register.route,
        Screen.ProductConfigFromCatalog.route,
        Screen.ProductConfigFromOrder.route,
        Screen.SelectRole.route
    )

    Scaffold(
        bottomBar = {
            if (!hideBottomBar) {
                AppBottomNavBar(
                    currentRoute = currentRoute,
                    onItemSelected = { item ->
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    ) { padding ->
        val authRepo: FireAuthRepository = koinInject()

        // Compute once at composition time — never changes, so the NavHost
        // never resets when auth state toggles during a sign-in attempt.
        val startDestination = remember {
            if (authRepo.getCurrentUserId() != null) Screen.Main.route else Screen.Auth.route
        }

        // Only used to detect a real sign-out from the main graph and redirect
        // back to auth. We do NOT use this to change startDestination.
        val isAuthenticated by authRepo.isAuthenticated()
            .collectAsStateWithLifecycle(initialValue = authRepo.getCurrentUserId() != null)

        LaunchedEffect(isAuthenticated) {
            if (!isAuthenticated) {
                // If the user is already on an auth screen (e.g. role-check failure
                // signed them out mid-login), there is nothing to do — they stay put.
                val authScreens = setOf(
                    Screen.SelectRole.route,
                    Screen.Login.route,
                    Screen.Register.route,
                    Screen.Auth.route
                )
                val route = navController.currentBackStackEntry?.destination?.route
                if (route != null && route !in authScreens) {
                    navController.navigate(Screen.Auth.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
        }

        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(padding)
        ) {
            authGraph(navController)
            mainGraph(navController)
        }
    }
}
