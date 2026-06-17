package com.example.coverdesign.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProductConfigDto(
    @SerialName("id") val id: String,
    @SerialName("product_id") val productId: String,
    @SerialName("product_type") val productType: String,
    @SerialName("height_mm") val heightMm: Double,
    @SerialName("width_mm") val widthMm: Double,
    @SerialName("depth_mm") val depthMm: Double,
    @SerialName("material") val material: String,
    @SerialName("size_label") val sizeLabel: String?,
    @SerialName("texture_id") val textureId: String,
    @SerialName("quantity") val quantity: Int,
    @SerialName("textureName") val textureName: String,
    @SerialName("texture_description") val textureDescription: String?,
    //@SerialName("custom_texture_description") val customTextureDescription: String = "None",
    @SerialName("inscription") val inscription: String,
    @SerialName("color_name") val colorName: String,
    @SerialName("color_hex") val colorHex: String,
    @SerialName("created_at") val createdAt: Long,
    @SerialName("additionalNotes") val additionalNotes: String,
    @SerialName("additional_elements") val additionalElements: List<String>,
    @SerialName("custom_size_mm") val customSizeMm: Double?,
    )