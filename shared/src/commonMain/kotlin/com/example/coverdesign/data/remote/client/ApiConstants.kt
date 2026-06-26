package com.example.coverdesign.data.remote.client

// data/remote/client/ApiConstants.kt
object ApiConstants {
    // swap via BuildConfig / xcconfig per environment
    const val BASE_URL     = "https://api.yourbackend.com/v1" //TODO API

    object Endpoints {
        const val BASE_MODEL = ""

        // products
        const val CATALOG        = "/products"
        const val PRODUCT        = "/products/{id}"
        const val PRICE          = "/products/price"

        // 3D model
        const val GENERATE_MODEL = "/model/generate"
        const val MODEL_STATUS   = "/model/{id}/status"
        const val MODEL_URL      = "/model/{id}/url"
        const val TRY_ON         = "/model/tryon"
        const val TEXTURES       = "/model/textures"

        // orders
        const val SUBMIT_ORDER   = "/orders"
        const val ORDER_BY_ID    = "/orders/{id}"

        // auth
        const val LOGIN          = "/auth/login"
        const val REFRESH        = "/auth/refresh"
        const val LOGOUT         = "/auth/logout"
    }
}