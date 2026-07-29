package com.example.coverdesign.domain.model.pricing

import com.example.coverdesign.domain.model.AdditionalElement
import com.example.coverdesign.domain.model.InsoleFootConfig
import com.example.coverdesign.domain.model.InsoleOrderConfig
import com.example.coverdesign.domain.model.Material
import com.example.coverdesign.domain.model.Measurement
import com.example.coverdesign.domain.model.OrderCategory
import com.example.coverdesign.domain.model.ProductConfig
import com.example.coverdesign.domain.model.SizeLabel
import com.example.coverdesign.domain.model.Texture
import kotlin.math.roundToInt

// ─────────────────────────────────────────────────────────────────────────────
// Price tables  — all values in USD
// These are client-side estimates; the server applies the authoritative table.
// ─────────────────────────────────────────────────────────────────────────────

/** Surcharge per material, added once to the base price (before size × qty). */
val MATERIAL_SURCHARGES: Map<Material, Double> = mapOf(
    Material.PLASTIC to 0.00,   // standard — no surcharge
    Material.PLA to 2.00,   // eco 3D-print filament
    Material.ABS to 3.50,   // engineering-grade plastic
    Material.NYLON to 5.00,   // flexible / durable
    Material.CARBON_FIBER to 28.00,  // premium lightweight
    Material.SILICONE to 8.00,   // soft / skin-safe
    Material.LEATHER to 15.00,  // genuine leather finish
    Material.EPOXY_RESIN to 10.00,  // custom cast resin
    Material.TITANIUM to 45.00,  // medical-grade metal
    Material.STAINLESS_STEEL to 22.00 // brushed metal finish
)

/**
 * Surcharge per texture preset (by Texture.id or Texture.name fallback).
 * Custom image textures get a flat "image processing" fee.
 */
val TEXTURE_SURCHARGES: Map<String, Double> = mapOf(
    "tex-glossy" to 2.00,   // glossy UV coating
    "tex-rough" to 1.50,   // bead-blasted / matte
    "tex-transparent" to 4.00,   // clear coat, more complex process
    "tex-custom-image" to 6.00    // image wrap / UV print setup fee
)

/** Flat fee for adding an inscription (laser engraving / embossing setup). */
const val INSCRIPTION_SURCHARGE: Double = 5.00

/** Surcharge per additional element selected. */
val ELEMENT_SURCHARGES: Map<AdditionalElement, Double> = mapOf(
    AdditionalElement.LOGO to 8.00,   // logo print / emboss
    AdditionalElement.ENGRAVING to 6.00,   // laser engraving
    AdditionalElement.DECOR to 4.50,   // decorative finish
    AdditionalElement.MOUNTING to 3.00,   // mounting hardware
    AdditionalElement.PADDING to 2.50,   // inner padding layer
    AdditionalElement.STRAP to 5.00    // attachment strap
)

// ─────────────────────────────────────────────────────────────────────────────
// Reference dimensions for size-multiplier calculation.
// "M" / base dimensions for each category (mm). Deviations scale the price.
// ─────────────────────────────────────────────────────────────────────────────

/** Reference volume in mm³ for each category at size M / standard. */
private val REFERENCE_VOLUME_MM3: Map<OrderCategory, Double> = mapOf(
    OrderCategory.ACCESSORY to 8_000.0,   // ~20×20×20 mm
    OrderCategory.JEWELRY to 1_500.0,   // ~15×10×10 mm
    OrderCategory.CLOTHING to 200_000.0,  // ~500×200×2 mm panel
    OrderCategory.FOOTWEAR to 300_000.0,  // ~260×100×~11.5 mm
    OrderCategory.THREE_D_PRINT to 50_000.0,   // ~50×50×20 mm generic
    OrderCategory.INTERIOR_DECOR to 500_000.0,  // ~200×100×25 mm
    OrderCategory.OTHER to 50_000.0
)

// Standard SizeLabel multipliers (relative to M = 1.0)
private val SIZE_LABEL_MULTIPLIERS: Map<SizeLabel, Double> = mapOf(
    SizeLabel.XS to 0.75,
    SizeLabel.S to 0.88,
    SizeLabel.M to 1.00,
    SizeLabel.L to 1.15,
    SizeLabel.XL to 1.30,
    SizeLabel.XXL to 1.50
)

// ─────────────────────────────────────────────────────────────────────────────
// PricingEngine
// ─────────────────────────────────────────────────────────────────────────────

object PricingEngine {

    /**
     * Calculates a full [PriceEstimate] for the given [config] and [basePrice].
     *
     * Calculation order:
     *  1. Material surcharge
     *  2. Texture surcharge
     *  3. Inscription surcharge (flat, if inscription non-empty)
     *  4. Element surcharges (per element)
     *  5. Size multiplier (SizeLabel OR custom measurement volume ratio)
     *  6. × quantity
     */
    fun calculate(config: ProductConfig, basePrice: Double): PriceEstimate {

        // ── 1. Material surcharge ─────────────────────────────────────────
        val matSurcharge = MATERIAL_SURCHARGES[config.material] ?: 0.0
        val materialSurcharges = mapOf(config.material.name to matSurcharge)

        // ── 2. Texture surcharge ──────────────────────────────────────────
        val texSurcharge = textureKey(config.texture).let { TEXTURE_SURCHARGES[it] ?: 0.0 }

        // ── 3. Inscription ────────────────────────────────────────────────
        val inscSurcharge = if (config.inscription.isNotBlank()) INSCRIPTION_SURCHARGE else 0.0

        // ── 4. Additional elements ────────────────────────────────────────
        val elemSurcharges = config.additionalElements.associate { element ->
            element.name to (ELEMENT_SURCHARGES[element] ?: 0.0)
        }

        // ── 5. Size multiplier ────────────────────────────────────────────
        val (multiplier, multiplierLabel) = resolveSizeMultiplier(
            sizeLabel = config.sizeLabel,
            measurement = config.measurement,
            category = config.orderCategory
        )

        // ── 6. Assemble totals ────────────────────────────────────────────
        val preMultiplierSum =
            basePrice + matSurcharge + texSurcharge + inscSurcharge + elemSurcharges.values.sum()

        val subtotal = preMultiplierSum * multiplier
        val total = subtotal * config.quantity

        return PriceEstimate(
            configId = config.id,
            basePrice = basePrice,
            materialSurcharges = materialSurcharges,
            textureSurcharge = texSurcharge,
            inscriptionSurcharge = inscSurcharge,
            elementSurcharges = elemSurcharges,
            sizeMultiplier = multiplier,
            sizeMultiplierLabel = multiplierLabel,
            quantity = config.quantity,
            subtotal = subtotal.roundTo2(),
            total = total.roundTo2()
        )
    }

    /**
     * Calculates a full [PriceEstimate] for the given [config] and [basePrice].
     *
     * Calculation order:
     *  1. Material surcharge
     *  2. Texture surcharge
     *  3. Inscription surcharge (flat, if inscription non-empty)
     *  4. Element surcharges (per element)
     *  5. Size multiplier (SizeLabel OR custom measurement volume ratio)
     *  6. × quantity
     */
    fun calculateForInsole(config: InsoleOrderConfig, basePrice: Double): PriceEstimate {

        // ── 1. Material surcharge ─────────────────────────────────────────
        val matSurcharge = MATERIAL_SURCHARGES[config.material] ?: 0.0
        val materialSurcharges = mapOf(config.material.name to matSurcharge)

        // ── 5. Size multiplier ────────────────────────────────────────────
        val (multiplier, multiplierLabel) = resolveSizeMultiplierForInsole(
            rightFoot = config.rightFoot,
            leftFoot = config.leftFoot,
        )

        // ── 6. Assemble totals ────────────────────────────────────────────
        val preMultiplierSum = basePrice + matSurcharge

        val subtotal = preMultiplierSum * multiplier
        val total = subtotal * config.quantity

        return PriceEstimate(
            configId = config.orderId,
            basePrice = basePrice,
            materialSurcharges = materialSurcharges,
            sizeMultiplier = multiplier,
            sizeMultiplierLabel = multiplierLabel,
            quantity = config.quantity,
            subtotal = subtotal.roundTo2(),
            total = total.roundTo2()
        )
    }

    private fun resolveSizeMultiplierForInsole(
        rightFoot: InsoleFootConfig,
        leftFoot: InsoleFootConfig?,
    ): Pair<Double, String> { // Added the explicit return type

        // Renamed from 'volume' since length * width is a 2D Area
        var totalAreaMm2 = 0f

        // ── path 1: Calculate total surface area ──────────────────────────
        if (leftFoot != null) {
            // Removed the unused 'euSize' dead code here
            for (foot in listOf(rightFoot, leftFoot)) {
                totalAreaMm2 += foot.lengthMm * foot.widthMm
            }
        } else {
            totalAreaMm2 += rightFoot.lengthMm * rightFoot.widthMm
        }

        // ── path 2: Calculate multiplier against reference ────────────────
        if (totalAreaMm2 > 0.0) {
            // Note: You might want to rename REFERENCE_VOLUME_MM3 to REFERENCE_AREA_MM2 in your constants file
            val refArea =
                REFERENCE_VOLUME_MM3[OrderCategory.INSOLE] ?: REFERENCE_VOLUME_MM3[OrderCategory.OTHER]!!

            val ratio = (totalAreaMm2 / refArea).toDouble().coerceIn(0.60, 2.50)

            val roundedRatio = ((ratio * 100)).roundToInt() / 100.0// 2 dp
            val pct = ((ratio - 1.0) * 100).roundToInt()
            val sign = if (pct >= 0) "+$pct%" else "$pct%"

            return roundedRatio to "Custom $sign"
        }

        // ── path 3: no size info fallback ─────────────────────────────────
        return 1.0 to ""
    }

    // ── Size multiplier resolution ────────────────────────────────────────────

    /**
     * Returns (multiplier, humanLabel).
     *
     * Priority:
     *  1. If a [SizeLabel] is selected → use the fixed table.
     *  2. If custom measurements are provided → compare volume to category
     *     reference and derive a ratio.  Clamped to [0.60 … 2.50].
     *  3. Otherwise → 1.0 (no adjustment).
     */
    private fun resolveSizeMultiplier(
        sizeLabel: SizeLabel?, measurement: Measurement, category: OrderCategory
    ): Pair<Double, String> {

        // ── path 1: standard size label ───────────────────────────────────
        if (sizeLabel != null) {
            val m = SIZE_LABEL_MULTIPLIERS[sizeLabel] ?: 1.0
            val pct = ((m - 1.0) * 100).roundToInt()
            val sign = if (pct >= 0) "+$pct%" else "$pct%"
            return m to "${sizeLabel.name} $sign"
        }

        // ── path 2: custom measurement volume ─────────────────────────────
        val volume = measurement.height * measurement.width * measurement.depth
        if (volume > 0.0) {
            val refVolume =
                REFERENCE_VOLUME_MM3[category] ?: REFERENCE_VOLUME_MM3[OrderCategory.OTHER]!!
            val ratio = (volume / refVolume).coerceIn(0.60, 2.50)
            val roundedRatio = (ratio * 100).roundToInt() / 100.0   // 2 dp
            val pct = ((ratio - 1.0) * 100).roundToInt()
            val sign = if (pct >= 0) "+$pct%" else "$pct%"
            return roundedRatio to "Custom $sign"
        }

        // ── path 3: no size info ──────────────────────────────────────────
        return 1.0 to ""
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    /** Returns the key to look up in [TEXTURE_SURCHARGES]. */
    private fun textureKey(texture: Texture): String =
        // prefer stable id; fall back to name (for server-supplied textures)
        if (texture.id.isNotBlank()) texture.id else texture.name.lowercase()

    private fun Double.roundTo2(): Double = (this * 100.0).roundToInt() / 100.0
}