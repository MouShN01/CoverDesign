package com.example.coverdesign.data.dto

import com.example.coverdesign.domain.model.OrderStatus
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// data/dto/OrderDto.kt
@kotlinx.serialization.Serializable
data class OrderDto(
    @SerialName("id") val id: String,
    @SerialName("adapted_model_id") val adaptedModelId: String,
    @SerialName("config") val config: ProductConfigDto,
    @SerialName("price") val price: PriceEstimateDto,
    @SerialName("status") val status: String,
    @SerialName("created_at") val createdAt: Long
)

@Serializable
data class OrderIdDto(
    @SerialName("order_id") val orderId: String,
    @SerialName("status") val status: OrderStatus
)

@Serializable
data class SubmitOrderRequestDto(
    @SerialName("config_id") val configId: String,
    @SerialName("notes") val notes: String
)

@Serializable
data class SubmitInsoleOrderRequestDto(
    @SerialName("config") val config: InsoleOrderConfigDto,
    @SerialName("notes") val notes: String = ""
)

@Serializable
data class SubmitProstheticCoverOrderRequestDto(
    @SerialName("config") val config: ProstheticCoverOrderConfigDto,
    @SerialName("notes") val notes: String = ""
)