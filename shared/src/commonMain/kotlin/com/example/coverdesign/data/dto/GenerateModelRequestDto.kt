package com.example.coverdesign.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ─────────────────────────────────────────────────────────────────────────────
// GenerateModelRequestDto — POST body sent to /v1/generate
@Serializable
data class GenerateModelRequestDto(
    @SerialName("order_id") val configId: String,
    @SerialName("texture_url") val textureUrl: String?, // Firebase Storage URL
    @SerialName("material") val material: String,
    @SerialName("enable_pbr") val enablePbr: Boolean = true,
    @SerialName("category") val category: String,
    @SerialName("product_id") val productId: String? = null,
    @SerialName("parameters") val parameters: ProstheticGenerationParametersDto? = null // JSON string with additional parameters
)

@Serializable
data class ProstheticGenerationParametersDto(
    @SerialName("limb_side") val limbSide: String,
    @SerialName("prosthetic_type") val prostheticType: ProstheticTypeEnumDto,
    @SerialName("circumference_mm") val circumferenceMm: Float,
    @SerialName("knee_apex_to_footshell_top_mm") val kneeApexToFootshellTopMm: Float,
    @SerialName("type_specific_measurement_mm") val typeSpecificMeasurementMm: Float,
    @SerialName("second_type_specific_measurement_mm") val secondTypeSpecificMeasurementMm: Float,
    @SerialName("button_placement") val buttonPlacement: String?,
    @SerialName("suspension_type") val suspensionType: String,
    @SerialName("color_name") val colorName: String,
    @SerialName("color_hex") val colorHex: String,
    @SerialName("texture_name") val textureName: String?, // use if there wan no image for the texture was loaded from UI
    @SerialName("additional_notes") val additionalNotes: String,
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