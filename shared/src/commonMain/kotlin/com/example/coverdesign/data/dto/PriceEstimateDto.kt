package com.example.coverdesign.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PriceEstimateDto(

    @SerialName("config_id")
    val configId: String,

    @SerialName("currency")
    val currency: String = "USD",

    // ── base ─────────────────────────────────────────────────────────────────
    @SerialName("base_price")
    val basePrice: Double,

    // ── material surcharges — map of material name → surcharge amount ─────────
    // e.g. { "CARBON_FIBER": 28.0 }
    @SerialName("material_surcharges")
    val materialSurcharges: Map<String, Double> = emptyMap(),

    // ── texture surcharge — single value for the selected texture ─────────────
    @SerialName("texture_surcharge")
    val textureSurcharge: Double = 0.0,

    // ── personalisation surcharges ────────────────────────────────────────────
    @SerialName("inscription_surcharge")
    val inscriptionSurcharge: Double = 0.0,

    // map of element name → surcharge amount, e.g. { "LOGO": 8.0 }
    @SerialName("element_surcharges")
    val elementSurcharges: Map<String, Double> = emptyMap(),

    // ── size ──────────────────────────────────────────────────────────────────
    @SerialName("size_multiplier")
    val sizeMultiplier: Double = 1.0,

    @SerialName("size_multiplier_label")
    val sizeMultiplierLabel: String = "",

    // ── totals ────────────────────────────────────────────────────────────────
    @SerialName("quantity")
    val quantity: Int = 1,

    // subtotal = (base + all surcharges) × sizeMultiplier (per unit)
    @SerialName("subtotal")
    val subtotal: Double,

    // total = subtotal × quantity
    @SerialName("total")
    val total: Double
)