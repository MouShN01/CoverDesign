package com.example.coverdesign.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ─────────────────────────────────────────────────────────────────────────────
// GenerateModelRequestDto — POST body sent to /v1/generate
// Built from ProductConfigDto — prompt is constructed server-side or here
// ─────────────────────────────────────────────────────────────────────────────

@Serializable
data class GenerateModelRequestDto(
    @SerialName("config_id") val configId: String,
    @SerialName("prompt") val prompt: String,  // built from config fields
    @SerialName("texture_url") val textureUrl: String?, // Firebase Storage URL
    @SerialName("material") val material: String,
    @SerialName("enable_pbr") val enablePbr: Boolean = true,
    @SerialName("category") val category: String,
    @SerialName("product_id") val productId: String? = null,

)

// ─────────────────────────────────────────────────────────────────────────────
// GenerateModelResponseDto — POST response — only contains the task ID
// ─────────────────────────────────────────────────────────────────────────────

@Serializable
data class GenerateModelResponseDto(
    @SerialName("task_id") val taskId: String,
    @SerialName("status") val status: String,
    @SerialName("estimated_wait_seconds") val estimatedWaitSeconds: Int? = null
)