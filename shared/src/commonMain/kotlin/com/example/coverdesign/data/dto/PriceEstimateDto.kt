package com.example.coverdesign.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// data/dto/PriceEstimateDto.kt
@Serializable
data class PriceEstimateDto(
    @SerialName("config_id")           val configId: String,
    @SerialName("base_price")          val basePrice: Double,
    @SerialName("material_surcharge")  val materialSurcharge: Double,
    @SerialName("texture_surcharge")   val textureSurcharge: Double,
    @SerialName("total")               val total: Double,
    @SerialName("currency")            val currency: String
)