package com.example.coverdesign.data.dto

// data/dto/ProductDto.kt
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

//TODO mirror backend JSON
@Serializable
data class ProductDto(
    @SerialName("id")          val id: String,
    @SerialName("name")        val name: String,
    @SerialName("description") val description: String,
    @SerialName("base_price")  val basePrice: Double,
    @SerialName("category")    val category: String,
    @SerialName("materials")   val materials: List<String>,
    @SerialName("textures")    val textures: List<TextureDto>,
    @SerialName("image_url")   val imageUrl: String
)

@Serializable
data class TextureDto(
    @SerialName("id")          val id: String,
    @SerialName("name")        val name: String,
    @SerialName("preview_url") val previewUrl: String,
    @SerialName("description") val description: String
)