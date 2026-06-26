package com.example.coverdesign.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// data/dto/Model3DDto.kt
//@Serializable
//data class Model3DDto(
//    @SerialName("id")           val id: String,
//    @SerialName("config_id")    val configId: String,
//    @SerialName("model_url")    val modelUrl: String,
//    @SerialName("status")       val status: String,
//    @SerialName("generated_at") val generatedAt: Long,
//    @SerialName("estimatedWaitSeconds") val estimatedWaitSeconds: Int?
//)

@Serializable
data class Model3DDto(
    @SerialName("id")                val id: String,
    @SerialName("product_id")        val productId: String,
    @SerialName("model_url")         val modelUrl: String,
    @SerialName("preview_image_url") val previewImageUrl: String = "",
    @SerialName("status")            val status: String,
    @SerialName("generated_at")      val generatedAt: Long
)

data class AdaptedModel3DDto(
    @SerialName("id")                      val id: String,
    @SerialName("config_id")               val configId: String,
    @SerialName("base_model_id")           val baseModelId: String,
    @SerialName("model_url")               val modelUrl: String,
    @SerialName("preview_image_url")       val previewImageUrl: String = "",
    @SerialName("status")                  val status: String,
    @SerialName("generated_at")            val generatedAt: Long,
    @SerialName("estimated_wait_seconds")  val estimatedWaitSeconds: Int? = null
)