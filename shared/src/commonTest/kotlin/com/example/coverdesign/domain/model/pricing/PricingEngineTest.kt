package com.example.coverdesign.domain.model.pricing

import com.example.coverdesign.domain.model.AdditionalElement
import com.example.coverdesign.domain.model.Material
import com.example.coverdesign.domain.model.Measurement
import com.example.coverdesign.domain.model.ProductCategory
import com.example.coverdesign.domain.model.ProductColor
import com.example.coverdesign.domain.model.ProductConfig
import com.example.coverdesign.domain.model.SizeLabel
import com.example.coverdesign.domain.model.Texture
import kotlin.test.Test
import kotlin.test.assertEquals

class PricingEngineTest {

    private fun makeConfig(
        material: Material = Material.PLASTIC,
        texture: Texture = Texture("unknown-id", "None", null, null),
        inscription: String = "",
        additionalElements: List<AdditionalElement> = emptyList(),
        sizeLabel: SizeLabel? = SizeLabel.M,
        measurement: Measurement = Measurement(0.0, 0.0, 0.0),
        quantity: Int = 1,
        category: ProductCategory = ProductCategory.ACCESSORY
    ) = ProductConfig(
        id = "cfg",
        productId = "prod",
        productCategory = category,
        measurement = measurement,
        sizeLabel = sizeLabel,
        color = ProductColor("White", "#FFFFFF"),
        material = material,
        texture = texture,
        inscription = inscription,
        userPhotoUri = null,
        additionalElements = additionalElements,
        quantity = quantity,
        createdAt = 0L
    )

    // ── base price ────────────────────────────────────────────────────────────

    @Test
    fun noSurcharges_sizeM_quantity1_equalsBasePrice() {
        val result = PricingEngine.calculate(makeConfig(), basePrice = 100.0)
        assertEquals(100.0, result.total)
        assertEquals(100.0, result.subtotal)
    }

    // ── material surcharges ───────────────────────────────────────────────────

    @Test
    fun materialPla_addsSurchargeToBase() {
        val result = PricingEngine.calculate(makeConfig(material = Material.PLA), basePrice = 100.0)
        assertEquals(102.0, result.total)  // 100 + 2.0
    }

    @Test
    fun materialCarbonFiber_addsSurchargeToBase() {
        val result = PricingEngine.calculate(makeConfig(material = Material.CARBON_FIBER), basePrice = 100.0)
        assertEquals(128.0, result.total)  // 100 + 28.0
    }

    @Test
    fun materialNotInTable_noSurcharge() {
        // METAL is in the Material enum but not in MATERIAL_SURCHARGES → 0.0 surcharge
        val result = PricingEngine.calculate(makeConfig(material = Material.METAL), basePrice = 100.0)
        assertEquals(100.0, result.total)
    }

    // ── texture surcharges ────────────────────────────────────────────────────

    @Test
    fun texGlossy_addsSurchargeToBase() {
        val result = PricingEngine.calculate(
            makeConfig(texture = Texture("tex-glossy", "Glossy", null, null)),
            basePrice = 100.0
        )
        assertEquals(102.0, result.total)  // 100 + 2.0
    }

    @Test
    fun texTransparent_addsSurchargeToBase() {
        val result = PricingEngine.calculate(
            makeConfig(texture = Texture("tex-transparent", "Transparent", null, null)),
            basePrice = 100.0
        )
        assertEquals(104.0, result.total)  // 100 + 4.0
    }

    @Test
    fun unknownTextureId_noSurcharge() {
        val result = PricingEngine.calculate(
            makeConfig(texture = Texture("custom-xyz", "Custom", null, null)),
            basePrice = 100.0
        )
        assertEquals(100.0, result.total)
    }

    // ── inscription surcharge ─────────────────────────────────────────────────

    @Test
    fun nonBlankInscription_addsInscriptionFee() {
        val result = PricingEngine.calculate(
            makeConfig(inscription = "Hello"),
            basePrice = 100.0
        )
        assertEquals(105.0, result.total)  // 100 + 5.0 flat fee
    }

    @Test
    fun blankInscription_noInscriptionFee() {
        val result = PricingEngine.calculate(makeConfig(inscription = ""), basePrice = 100.0)
        assertEquals(100.0, result.total)
    }

    @Test
    fun whitespaceInscription_noInscriptionFee() {
        val result = PricingEngine.calculate(makeConfig(inscription = "   "), basePrice = 100.0)
        assertEquals(100.0, result.total)
    }

    // ── additional element surcharges ─────────────────────────────────────────

    @Test
    fun logoElement_addsCorrectSurcharge() {
        val result = PricingEngine.calculate(
            makeConfig(additionalElements = listOf(AdditionalElement.LOGO)),
            basePrice = 100.0
        )
        assertEquals(108.0, result.total)  // 100 + 8.0
    }

    @Test
    fun multipleElements_sumAllSurcharges() {
        val result = PricingEngine.calculate(
            makeConfig(additionalElements = listOf(AdditionalElement.LOGO, AdditionalElement.PADDING)),
            basePrice = 100.0
        )
        assertEquals(110.5, result.total)  // 100 + 8.0 + 2.5
    }

    @Test
    fun elementNotInTable_noSurcharge() {
        // FASTENER is in AdditionalElement enum but not in ELEMENT_SURCHARGES
        val result = PricingEngine.calculate(
            makeConfig(additionalElements = listOf(AdditionalElement.FASTENER)),
            basePrice = 100.0
        )
        assertEquals(100.0, result.total)
    }

    // ── size label multipliers ────────────────────────────────────────────────

    @Test
    fun sizeLabelXS_appliesCorrectMultiplier() {
        val result = PricingEngine.calculate(makeConfig(sizeLabel = SizeLabel.XS), basePrice = 100.0)
        assertEquals(75.0, result.subtotal)  // 100 * 0.75
        assertEquals(75.0, result.total)
        assertEquals(0.75, result.sizeMultiplier)
    }

    @Test
    fun sizeLabelM_multiplierIsOne() {
        val result = PricingEngine.calculate(makeConfig(sizeLabel = SizeLabel.M), basePrice = 100.0)
        assertEquals(1.0, result.sizeMultiplier)
        assertEquals(100.0, result.total)
    }

    @Test
    fun sizeLabelXXL_appliesCorrectMultiplier() {
        val result = PricingEngine.calculate(makeConfig(sizeLabel = SizeLabel.XXL), basePrice = 100.0)
        assertEquals(1.5, result.sizeMultiplier)
        assertEquals(150.0, result.total)
    }

    // ── custom measurement multiplier ─────────────────────────────────────────

    @Test
    fun customVolume_doubleReference_multiplierIsTwo() {
        // ref for ACCESSORY = 8000 mm³ (~20×20×20), volume = 40×20×20 = 16000 → ratio 2.0
        val result = PricingEngine.calculate(
            makeConfig(
                sizeLabel = null,
                measurement = Measurement(40.0, 20.0, 20.0),
                category = ProductCategory.ACCESSORY
            ),
            basePrice = 100.0
        )
        assertEquals(2.0, result.sizeMultiplier)
        assertEquals(200.0, result.total)
    }

    @Test
    fun customVolume_tinyItem_multiplierClampedAt0_60() {
        // volume = 1×1×1 = 1 mm³, far below ref → clamped to 0.60
        val result = PricingEngine.calculate(
            makeConfig(
                sizeLabel = null,
                measurement = Measurement(1.0, 1.0, 1.0),
                category = ProductCategory.ACCESSORY
            ),
            basePrice = 100.0
        )
        assertEquals(0.6, result.sizeMultiplier)
    }

    @Test
    fun customVolume_hugeItem_multiplierClampedAt2_50() {
        // volume = 100×100×100 = 1_000_000 mm³, far above ref → clamped to 2.50
        val result = PricingEngine.calculate(
            makeConfig(
                sizeLabel = null,
                measurement = Measurement(100.0, 100.0, 100.0),
                category = ProductCategory.ACCESSORY
            ),
            basePrice = 100.0
        )
        assertEquals(2.5, result.sizeMultiplier)
    }

    @Test
    fun noSizeInfo_multiplierIsOneWithEmptyLabel() {
        val result = PricingEngine.calculate(
            makeConfig(sizeLabel = null, measurement = Measurement(0.0, 0.0, 0.0)),
            basePrice = 100.0
        )
        assertEquals(1.0, result.sizeMultiplier)
        assertEquals("", result.sizeMultiplierLabel)
    }

    // ── quantity ──────────────────────────────────────────────────────────────

    @Test
    fun quantity_multipliesTotal() {
        val result = PricingEngine.calculate(makeConfig(quantity = 3), basePrice = 100.0)
        assertEquals(100.0, result.subtotal)   // per-unit price
        assertEquals(300.0, result.total)      // × 3
        assertEquals(3, result.quantity)
    }

    // ── combination ───────────────────────────────────────────────────────────

    @Test
    fun allSurchargesWithXsAndQuantity2() {
        // PLA (+2) + tex-glossy (+2) + inscription (+5) + LOGO (+8) = 17 total surcharge
        // preSum = 100 + 17 = 117, × XS (0.75) = 87.75, × 2 = 175.5
        val result = PricingEngine.calculate(
            makeConfig(
                material = Material.PLA,
                texture = Texture("tex-glossy", "Glossy", null, null),
                inscription = "Hi",
                additionalElements = listOf(AdditionalElement.LOGO),
                sizeLabel = SizeLabel.XS,
                quantity = 2
            ),
            basePrice = 100.0
        )
        assertEquals(87.75, result.subtotal)
        assertEquals(175.5, result.total)
    }

    // ── rounding ──────────────────────────────────────────────────────────────

    @Test
    fun resultRoundsToTwoDecimalPlaces() {
        // tex-rough (+1.50) → preSum 101.5, × M (1.0) = 101.5, × 1 = 101.5
        val result = PricingEngine.calculate(
            makeConfig(texture = Texture("tex-rough", "Rough", null, null)),
            basePrice = 100.0
        )
        assertEquals(101.5, result.subtotal)
        assertEquals(101.5, result.total)
    }
}
