package com.example.coverdesign.domain.model.pricing

data class PriceEstimate(
    val configId: String,
    val currency: String = "USD",
    val basePrice: Double,
    val materialSurcharges: Map<String, Double> = emptyMap(),
    val textureSurcharge: Double = 0.0,
    val inscriptionSurcharge: Double = 0.0,
    val elementSurcharges: Map<String, Double> = emptyMap(),
    val sizeMultiplier: Double = 1.0,
    val sizeMultiplierLabel: String = "",   // "XL +20%" or "Custom +35%"
    val quantity: Int = 1,
    // ── totals ───────────────────────────────────────────────────────────────
    // subtotal = (base + all surcharges) × sizeMultiplier
    val subtotal: Double,
    // total = subtotal × quantity
    val total: Double
) {
    /** Convenience sum of all material surcharges */
    val totalMaterialSurcharge: Double get() = materialSurcharges.values.sum()

    /** Convenience sum of all element surcharges */
    val totalElementSurcharge: Double get() = elementSurcharges.values.sum()
}