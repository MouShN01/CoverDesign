package com.example.coverdesign.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProstheticCoverOrderConfigDto(
    @SerialName("order_id") val orderId: String,
    @SerialName("category") val category: String,
    @SerialName("quantity") val quantity: Int,
    @SerialName("price_estimate") val priceEstimate: PriceEstimateDto,
    @SerialName("limb_side") val limbSide: String,
    @SerialName("limb_level") val limbLevel: String,
    @SerialName("prosthetic_type") val prostheticType: String?,
    @SerialName("circumference_mm") val circumferenceMm: Float,
    @SerialName("knee_apex_to_footshell_top_mm") val kneeApexToFootshellTopMm: Float,
    @SerialName("type_specific_measurement_mm") val typeSpecificMeasurementMm: Float = 0f,
    @SerialName("second_type_specific_measurement_mm") val secondTypeSpecificMeasurementMm: Float = 0f,
    @SerialName("button_placement") val buttonPlacement: String? = null,
    @SerialName("fastening_type") val fasteningType: String = "strap",
    @SerialName("color_name") val colorName: String,
    @SerialName("color_hex") val colorHex: String,
    @SerialName("material") val material: String,
    @SerialName("texture_id") val textureId: String,
    @SerialName("texture_name") val textureName: String,
    @SerialName("texture_preview_url") val texturePreviewUrl: String?,
    @SerialName("texture_description") val textureDescription: String?,
    @SerialName("inscription") val inscription: String,
    @SerialName("additional_elements") val additionalElements: List<String>,
    @SerialName("additional_notes") val additionalNotes: String,
    @SerialName("created_at") val createdAt: Long
)