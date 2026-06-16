package com.example.coverdesign.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// data/dto/ProductConfigDto.kt
@Serializable
data class ProductConfigDto(
    @SerialName("id")                  val id: String,
    @SerialName("product_id")          val productId: String,
    @SerialName("height_mm")           val heightMm: Double,
    @SerialName("width_mm")            val widthMm: Double,
    @SerialName("depth_mm")            val depthMm: Double,
    @SerialName("material")            val material: String,
    @SerialName("texture_id")          val textureId: String?,
    @SerialName("texture_description") val textureDescription: String,
    @SerialName("color")               val color: String,
    @SerialName("notes")               val notes: String,
    @SerialName("created_at")          val createdAt: Long
)