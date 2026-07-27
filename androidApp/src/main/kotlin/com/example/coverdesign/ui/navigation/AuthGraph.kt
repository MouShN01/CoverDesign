package com.example.coverdesign.ui.navigation

import androidx.compose.runtime.remember
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.example.coverdesign.ui.screens.auth.LoginScreen
import com.example.coverdesign.ui.screens.auth.RegisterScreen
import com.example.coverdesign.ui.screens.auth.SelectRoleScreen
import com.example.coverdesign.ui.viewmodel.AuthViewModel
import org.koin.compose.viewmodel.koinViewModel

fun NavGraphBuilder.authGraph(
    navController: NavHostController
) {
    navigation(
        route = Screen.Auth.route,
        startDestination = Screen.SelectRole.route
    ) {
        composable(Screen.SelectRole.route) { backStackEntry ->
            val authEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Screen.Auth.route)
            }
            val sharedViewModel: AuthViewModel = koinViewModel(viewModelStoreOwner = authEntry)
            SelectRoleScreen(
                onContinue = { navController.navigate(Screen.Login.route) },
                viewModel = sharedViewModel
            )
        }
        composable(Screen.Login.route) { backStackEntry ->
            val authEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Screen.Auth.route)
            }
            val sharedViewModel: AuthViewModel = koinViewModel(viewModelStoreOwner = authEntry)

            LoginScreen(
                viewModel = sharedViewModel,
                onLoginSuccess = {
                    println("Login success with ROLE ${sharedViewModel.uiState.value.role}")
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.SelectRole.route) { inclusive = true }
                        //popUpTo(Screen.Auth.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }
        composable(Screen.Register.route) { backStackEntry ->
            val authEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Screen.Auth.route)
            }
            val sharedViewModel: AuthViewModel = koinViewModel(viewModelStoreOwner = authEntry)
            RegisterScreen(
                viewModel = sharedViewModel,
                onRegisterSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.SelectRole.route) { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
