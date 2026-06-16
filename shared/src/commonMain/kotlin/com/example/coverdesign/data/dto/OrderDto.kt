package com.example.coverdesign.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// data/dto/OrderDto.kt
@kotlinx.serialization.Serializable
data class OrderDto(
    @SerialName("id")         val id: String,
    @SerialName("config")     val config: ProductConfigDto,
    @SerialName("price")      val price: PriceEstimateDto,
    @SerialName("status")     val status: String,
    @SerialName("created_at") val createdAt: Long
)

@kotlinx.serialization.Serializable
data class OrderIdDto(
    @SerialName("order_id") val orderId: String
)

@Serializable
data class SubmitOrderRequestDto(
    @SerialName("config_id") val configId: String,
    @SerialName("notes")     val notes: String
)