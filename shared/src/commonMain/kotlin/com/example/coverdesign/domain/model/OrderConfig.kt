package com.example.coverdesign.domain.model

import com.example.coverdesign.domain.model.pricing.PriceEstimate

sealed interface OrderConfig {
    val orderId: String
    val category: OrderCategory
    val quantity: Int
    val priceEstimate: PriceEstimate
}