package com.example.coverdesign.domain.model

import com.example.coverdesign.domain.model.pricing.PriceEstimate
import kotlin.jvm.JvmInline

data class Order(
    val id: String,
    val adaptedModelId: String?,
    val config: ProductConfig,
    val priceEstimate: PriceEstimate,
    val status: OrderStatus,
    val createdAt: Long,
    )

enum class OrderStatus {
    DRAFT, SUBMITTED, CONFIRMED, IN_PRODUCTION, SHIPPED, DELIVERED, CANCELLED
}

@JvmInline
value class OrderId(val value: String)
