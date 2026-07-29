package com.example.coverdesign.domain.validator

import com.example.coverdesign.domain.model.Measurement
import com.example.coverdesign.domain.model.OrderCategory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class MeasurementValidatorTest {

    private val validator = MeasurementValidator()

    private fun m(h: Double = 0.0, w: Double = 0.0, d: Double = 0.0) = Measurement(h, w, d)

    // ── JEWELRY ───────────────────────────────────────────────────────────────

    @Test
    fun jewelry_noHeight_returnsRequired() {
        val err = validator.validate(OrderCategory.JEWELRY, m(h = 0.0))
        assertEquals("Please enter the piece thickness or length", err)
    }

    @Test
    fun jewelry_heightBelowMinThickness_returnsError() {
        val err = validator.validate(OrderCategory.JEWELRY, m(h = 0.3))
        assertEquals(
            "Minimum thickness is 0.5 mm — thinner pieces cannot be fabricated reliably", err
        )
    }

    @Test
    fun jewelry_heightAtMin_isValid() {
        assertNull(validator.validate(OrderCategory.JEWELRY, m(h = 0.5)))
    }

    @Test
    fun jewelry_heightAtMax_isValid() {
        assertNull(validator.validate(OrderCategory.JEWELRY, m(h = 80.0)))
    }

    @Test
    fun jewelry_heightExceedsMax_returnsError() {
        val err = validator.validate(OrderCategory.JEWELRY, m(h = 81.0))
        assertEquals("Maximum jewellery length is 80 mm", err)
    }

    @Test
    fun jewelry_widthBelowMin_returnsError() {
        val err = validator.validate(OrderCategory.JEWELRY, m(h = 10.0, w = 0.5))
        assertEquals("Minimum width is 1 mm", err)
    }

    @Test
    fun jewelry_widthAtMin_isValid() {
        assertNull(validator.validate(OrderCategory.JEWELRY, m(h = 10.0, w = 1.0)))
    }

    @Test
    fun jewelry_widthExceedsMax_returnsError() {
        val err = validator.validate(OrderCategory.JEWELRY, m(h = 10.0, w = 51.0))
        assertEquals("Maximum jewellery width is 50 mm", err)
    }

    @Test
    fun jewelry_noWidth_isValid() {
        assertNull(validator.validate(OrderCategory.JEWELRY, m(h = 10.0, w = 0.0)))
    }

    // ── ACCESSORY ─────────────────────────────────────────────────────────────

    @Test
    fun accessory_allZero_returnsRequired() {
        val err = validator.validate(OrderCategory.ACCESSORY, m())
        assertEquals("Enter at least one dimension", err)
    }

    @Test
    fun accessory_heightBelowMin_returnsError() {
        val err = validator.validate(OrderCategory.ACCESSORY, m(h = 3.0))
        assertEquals("Minimum accessory height is 5 mm", err)
    }

    @Test
    fun accessory_heightAtMin_isValid() {
        assertNull(validator.validate(OrderCategory.ACCESSORY, m(h = 5.0)))
    }

    @Test
    fun accessory_heightExceedsMax_returnsError() {
        val err = validator.validate(OrderCategory.ACCESSORY, m(h = 401.0))
        assertEquals("Maximum accessory height is 400 mm", err)
    }

    @Test
    fun accessory_widthBelowMin_returnsError() {
        val err = validator.validate(OrderCategory.ACCESSORY, m(w = 3.0))
        assertEquals("Minimum accessory width is 5 mm", err)
    }

    @Test
    fun accessory_widthExceedsMax_returnsError() {
        val err = validator.validate(OrderCategory.ACCESSORY, m(h = 10.0, w = 301.0))
        assertEquals("Maximum accessory width is 300 mm", err)
    }

    @Test
    fun accessory_depthExceedsMax_returnsError() {
        val err = validator.validate(OrderCategory.ACCESSORY, m(h = 10.0, d = 201.0))
        assertEquals("Maximum accessory depth is 200 mm", err)
    }

    @Test
    fun accessory_validDimensions_isValid() {
        assertNull(validator.validate(OrderCategory.ACCESSORY, m(h = 10.0, w = 10.0, d = 10.0)))
    }

    // ── CLOTHING ──────────────────────────────────────────────────────────────

    @Test
    fun clothing_noHeight_returnsRequired() {
        val err = validator.validate(OrderCategory.CLOTHING, m(h = 0.0))
        assertEquals("Body height is required to generate the clothing model", err)
    }

    @Test
    fun clothing_heightBelowMin_returnsError() {
        val err = validator.validate(OrderCategory.CLOTHING, m(h = 99.0))
        assertEquals("Body height must be at least 100 cm", err)
    }

    @Test
    fun clothing_heightAtMin_isValid() {
        assertNull(validator.validate(OrderCategory.CLOTHING, m(h = 100.0)))
    }

    @Test
    fun clothing_heightExceedsMax_returnsError() {
        val err = validator.validate(OrderCategory.CLOTHING, m(h = 221.0))
        assertEquals("Body height cannot exceed 220 cm", err)
    }

    @Test
    fun clothing_weightBelowMin_returnsError() {
        val err = validator.validate(OrderCategory.CLOTHING, m(h = 170.0, w = 20.0))
        assertEquals("Body weight must be at least 30 kg if provided", err)
    }

    @Test
    fun clothing_weightExceedsMax_returnsError() {
        val err = validator.validate(OrderCategory.CLOTHING, m(h = 170.0, w = 301.0))
        assertEquals("Body weight cannot exceed 300 kg", err)
    }

    @Test
    fun clothing_waistBelowMin_returnsError() {
        val err = validator.validate(OrderCategory.CLOTHING, m(h = 170.0, d = 30.0))
        assertEquals("Waist circumference must be at least 40 cm if provided", err)
    }

    @Test
    fun clothing_waistExceedsMax_returnsError() {
        val err = validator.validate(OrderCategory.CLOTHING, m(h = 170.0, d = 201.0))
        assertEquals("Waist circumference cannot exceed 200 cm", err)
    }

    @Test
    fun clothing_validFullMeasurements_isValid() {
        assertNull(validator.validate(OrderCategory.CLOTHING, m(h = 175.0, w = 70.0, d = 80.0)))
    }

    // ── FOOTWEAR ──────────────────────────────────────────────────────────────

    @Test
    fun footwear_noSizeNoLength_returnsRequired() {
        val err = validator.validate(OrderCategory.FOOTWEAR, m())
        assertEquals("Please select an EU size or enter your foot length", err)
    }

    @Test
    fun footwear_validEuSize_isValid() {
        assertNull(validator.validate(OrderCategory.FOOTWEAR, m(d = 40.0)))
    }

    @Test
    fun footwear_validFootLength_isValid() {
        assertNull(validator.validate(OrderCategory.FOOTWEAR, m(h = 260.0)))
    }

    @Test
    fun footwear_footLengthBelowMin_returnsError() {
        val err = validator.validate(OrderCategory.FOOTWEAR, m(h = 150.0))
        assertEquals("Foot length must be at least 180 mm (EU 33)", err)
    }

    @Test
    fun footwear_footLengthExceedsMax_returnsError() {
        val err = validator.validate(OrderCategory.FOOTWEAR, m(h = 341.0))
        assertEquals("Foot length cannot exceed 340 mm (EU 50+)", err)
    }

    @Test
    fun footwear_widthBelowMin_returnsError() {
        val err = validator.validate(OrderCategory.FOOTWEAR, m(h = 260.0, w = 50.0))
        assertEquals("Foot width must be at least 60 mm if provided", err)
    }

    @Test
    fun footwear_widthExceedsMax_returnsError() {
        val err = validator.validate(OrderCategory.FOOTWEAR, m(h = 260.0, w = 131.0))
        assertEquals("Foot width cannot exceed 130 mm", err)
    }

    // ── THREE_D_PRINT ─────────────────────────────────────────────────────────

    @Test
    fun threeDPrint_allZero_returnsRequired() {
        val err = validator.validate(OrderCategory.THREE_D_PRINT, m())
        assertEquals("Enter at least one dimension", err)
    }

    @Test
    fun threeDPrint_heightBelowNozzle_returnsError() {
        val err = validator.validate(OrderCategory.THREE_D_PRINT, m(h = 0.3))
        assertEquals("Minimum printable height is 0.4 mm (one nozzle width)", err)
    }

    @Test
    fun threeDPrint_heightAtMin_isValid() {
        assertNull(validator.validate(OrderCategory.THREE_D_PRINT, m(h = 0.4)))
    }

    @Test
    fun threeDPrint_heightExceedsBed_returnsError() {
        val err = validator.validate(OrderCategory.THREE_D_PRINT, m(h = 401.0))
        assertEquals("Height exceeds maximum printer bed size (400 mm)", err)
    }

    @Test
    fun threeDPrint_widthBelowNozzle_returnsError() {
        val err = validator.validate(OrderCategory.THREE_D_PRINT, m(w = 0.3))
        assertEquals("Minimum printable width is 0.4 mm", err)
    }

    @Test
    fun threeDPrint_widthExceedsBed_returnsError() {
        val err = validator.validate(OrderCategory.THREE_D_PRINT, m(h = 10.0, w = 351.0))
        assertEquals("Width exceeds maximum printer bed size (350 mm)", err)
    }

    @Test
    fun threeDPrint_depthBelowNozzle_returnsError() {
        val err = validator.validate(OrderCategory.THREE_D_PRINT, m(d = 0.3))
        assertEquals("Minimum printable depth is 0.4 mm", err)
    }

    @Test
    fun threeDPrint_depthExceedsBed_returnsError() {
        val err = validator.validate(OrderCategory.THREE_D_PRINT, m(h = 10.0, d = 351.0))
        assertEquals("Depth exceeds maximum printer bed size (350 mm)", err)
    }

    // ── INTERIOR_DECOR ────────────────────────────────────────────────────────

    @Test
    fun interiorDecor_allZero_returnsRequired() {
        val err = validator.validate(OrderCategory.INTERIOR_DECOR, m())
        assertEquals("Enter at least one dimension", err)
    }

    @Test
    fun interiorDecor_heightBelowMin_returnsError() {
        val err = validator.validate(OrderCategory.INTERIOR_DECOR, m(h = 10.0))
        assertEquals("Minimum height for a décor item is 20 mm", err)
    }

    @Test
    fun interiorDecor_widthBelowMin_returnsError() {
        val err = validator.validate(OrderCategory.INTERIOR_DECOR, m(w = 10.0))
        assertEquals("Minimum width for a décor item is 20 mm", err)
    }

    @Test
    fun interiorDecor_depthBelowMin_returnsError() {
        val err = validator.validate(OrderCategory.INTERIOR_DECOR, m(h = 100.0, d = 0.5))
        assertEquals("Minimum depth is 1 mm", err)
    }

    @Test
    fun interiorDecor_twoDimensionsExceedDoor_returnsError() {
        // second-largest dim = 870 > 860 → door constraint triggered
        val err =
            validator.validate(OrderCategory.INTERIOR_DECOR, m(h = 900.0, w = 870.0, d = 10.0))
        assertEquals(
            "Two or more dimensions exceed a standard door width (860 mm) — the item cannot be moved indoors",
            err
        )
    }

    @Test
    fun interiorDecor_onlyOneDimensionExceedsDoor_isValid() {
        // second-largest dim = 850 ≤ 860 → no constraint violated
        assertNull(
            validator.validate(
                OrderCategory.INTERIOR_DECOR, m(h = 900.0, w = 850.0, d = 10.0)
            )
        )
    }

    @Test
    fun interiorDecor_dimensionExceedsAbsoluteMax_returnsError() {
        val err = validator.validate(OrderCategory.INTERIOR_DECOR, m(h = 3001.0))
        assertEquals("Maximum dimension for a décor item is 3000 mm", err)
    }

    @Test
    fun interiorDecor_validDimensions_isValid() {
        assertNull(
            validator.validate(
                OrderCategory.INTERIOR_DECOR, m(h = 500.0, w = 300.0, d = 50.0)
            )
        )
    }

    // ── OTHER ─────────────────────────────────────────────────────────────────

    @Test
    fun other_negativeDimension_returnsError() {
        val err = validator.validate(OrderCategory.OTHER, m(h = -1.0))
        assertEquals("Dimensions cannot be negative", err)
    }

    @Test
    fun other_dimensionExceedsMax_returnsError() {
        val err = validator.validate(OrderCategory.OTHER, m(h = 10001.0))
        assertEquals("Dimension exceeds 10 000 mm — please check your input", err)
    }

    @Test
    fun other_allZero_isValid() {
        assertNull(validator.validate(OrderCategory.OTHER, m()))
    }

    @Test
    fun other_validDimensions_isValid() {
        assertNull(validator.validate(OrderCategory.OTHER, m(h = 5000.0, w = 5000.0, d = 5000.0)))
    }

    // ── validateInscription ───────────────────────────────────────────────────

    @Test
    fun inscription_empty_isValid() {
        assertNull(validator.validateInscription(""))
    }

    @Test
    fun inscription_exactly50chars_isValid() {
        assertNull(validator.validateInscription("a".repeat(50)))
    }

    @Test
    fun inscription_51chars_returnsError() {
        val err = validator.validateInscription("a".repeat(51))
        assertEquals("Inscription must be 50 characters or fewer", err)
    }

    // ── validateQuantity ──────────────────────────────────────────────────────

    @Test
    fun quantity_zero_returnsError() {
        val err = validator.validateQuantity(0)
        assertEquals("Quantity must be at least 1", err)
    }

    @Test
    fun quantity_atMin_isValid() {
        assertNull(validator.validateQuantity(1))
    }

    @Test
    fun quantity_atMax_isValid() {
        assertNull(validator.validateQuantity(999))
    }

    @Test
    fun quantity_exceedsMax_returnsError() {
        val err = validator.validateQuantity(1000)
        assertEquals("Quantity cannot exceed 999", err)
    }

    @Test
    fun quantity_negative_returnsError() {
        val err = validator.validateQuantity(-1)
        assertEquals("Quantity must be at least 1", err)
    }
}
