package com.example.coverdesign.domain.model

data class PriceEstimate(
    val configId: String,
    val basePrice: Double,
    val materialSurcharge: Double,
    val textureSurcharge: Double = 0.0,
    val total: Double,
    val currency: String = "USD"
)