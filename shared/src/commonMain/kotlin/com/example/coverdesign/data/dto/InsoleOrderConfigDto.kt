package com.example.coverdesign.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class InsoleFootConfigDto(
    @SerialName("eu_size") val euSize: Int,
    @SerialName("length_mm") val lengthMm: Float,
    @SerialName("width_mm") val widthMm: Float,
    @SerialName("side") val side: String
)

@Serializable
data class InsoleOrderConfigDto(
    @SerialName("order_id") val orderId: String,
    @SerialName("category") val category: String,
    @SerialName("quantity") val quantity: Int,
    @SerialName("price_estimate") val priceEstimate: PriceEstimateDto,
    @SerialName("right_foot") val rightFoot: InsoleFootConfigDto,
    @SerialName("left_foot") val leftFoot: InsoleFootConfigDto?,
    @SerialName("material") val material: String,
    @SerialName("color_name") val colorName: String,
    @SerialName("color_hex") val colorHex: String
)