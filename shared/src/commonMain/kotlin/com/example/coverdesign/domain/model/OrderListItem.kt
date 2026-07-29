package com.example.coverdesign.domain.model

data class OrderListItem(
    val orderId: String,
    val category: OrderCategory,
    val createdAt: Long,
    val totalPrice: Double,
    val status: OrderStatus,
)