package com.example.coverdesign.domain.model.pricing

data class PriceEstimate(
    val configId            : String,
    val currency            : String = "USD",

    // ── base ─────────────────────────────────────────────────────────────────
    val basePrice           : Double,       // from Product.basePrice in catalogue

    // ── material surcharge — one entry per material used ──────────────────────
    // key = Material.name, value = surcharge amount
    val materialSurcharges  : Map<String, Double> = emptyMap(),

    // ── texture surcharge — for the selected texture preset ───────────────────
    // key = Texture.id or Texture.name, value = surcharge amount
    val textureSurcharge    : Double = 0.0,

    // ── personalisation surcharges ────────────────────────────────────────────
    val inscriptionSurcharge : Double = 0.0,   // flat fee if inscription non-empty

    // key = AdditionalElement.name, value = surcharge per element
    val elementSurcharges   : Map<String, Double> = emptyMap(),

    // ── size multiplier ───────────────────────────────────────────────────────
    // 1.0 = standard M size; >1.0 = larger; <1.0 = smaller
    // Applied to (base + all surcharges) before quantity
    val sizeMultiplier      : Double = 1.0,
    val sizeMultiplierLabel : String = "",   // e.g. "XL +20%" or "Custom +35%"

    // ── quantity ──────────────────────────────────────────────────────────────
    val quantity            : Int = 1,

    // ── totals ───────────────────────────────────────────────────────────────
    // subtotal = (base + all surcharges) × sizeMultiplier
    val subtotal            : Double,
    // total = subtotal × quantity
    val total               : Double
) {
    /** Convenience sum of all material surcharges */
    val totalMaterialSurcharge: Double get() = materialSurcharges.values.sum()

    /** Convenience sum of all element surcharges */
    val totalElementSurcharge: Double get() = elementSurcharges.values.sum()
}