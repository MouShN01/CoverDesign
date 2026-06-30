package com.example.coverdesign.ui.navigation

sealed class Screen(val route: String) {
    // auth
    object Auth : Screen("auth")
    object Login : Screen("login")
    object Register : Screen("register")
    object Main : Screen("main")

    // bottom nav roots
    object Home : Screen("home")
    object Model3D : Screen("3d/{productId}") {
        fun createRoute(productId: String) =
            "3d/$productId"
    }

    object Orders : Screen("orders")
    object Profile : Screen("profile")

    // detail screens
    object Catalog : Screen("catalog")

    //    object ProductConfig : Screen("config/{productId}") {
//        fun createRoute(productId: String) = "config/$productId"
//    }
    object ModelPreview : Screen("preview/{taskId}/{configId}") {
        fun createRoute(taskId: String, configId: String) =
            "preview/$taskId/$configId"
    }

    object ProductConfigFromCatalog : Screen("config/product/{productId}") {
        fun createRoute(productId: String) = "config/product/$productId"
    }

    object ProductConfigFromOrder : Screen("config/existing/{configId}") {
        fun createRoute(configId: String) = "config/existing/$configId"
    }

    object ArTryOn : Screen("ar/{configId}") {
        fun createRoute(configId: String) = "ar/$configId"
    }

    object OrderSummary : Screen("order/{configId}") {
        fun createRoute(configId: String) = "order/$configId"
    }

    object OrderDetail : Screen("order/detail/{orderId}") {
        fun createRoute(orderId: String) = "order/detail/$orderId"
    }

}
