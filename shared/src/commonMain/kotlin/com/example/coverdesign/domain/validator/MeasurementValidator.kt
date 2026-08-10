package com.example.coverdesign.domain.validator

import com.example.coverdesign.domain.model.Measurement
import com.example.coverdesign.domain.model.OrderCategory

// ─────────────────────────────────────────────────────────────────────────────
// MeasurementValidator
//
// All dimension limits are in mm and grounded in real-world fabrication:
//
// JEWELRY       — ring/earring/bracelet must fit a human hand/wrist/ear
// ACCESSORY     — phone cases, bags, small wearables
// CLOTHING      — height = body height in cm (stored as cm, not mm here),
//                 width = weight in kg, depth = waist in cm
//                 (re-uses Measurement fields with semantic remapping per
//                  ClothingSizeSection — see ProductConfigScreen)
// FOOTWEAR      — foot length × width; depth re-used for EU size ordinal
// THREE_D_PRINT — bed size limited by typical consumer FDM printer volume
// INTERIOR_DECOR — must fit through a standard door, be liftable by one person
// OTHER         — permissive generic bounds
// ─────────────────────────────────────────────────────────────────────────────

class MeasurementValidator {

    // ── public entry point ────────────────────────────────────────────────────

    fun validate(category: OrderCategory, m: Measurement): String? =
        when (category) {
            OrderCategory.JEWELRY        -> validateJewelry(m)
            OrderCategory.ACCESSORY      -> validateAccessory(m)
            OrderCategory.CLOTHING       -> validateClothingBody(m)
            OrderCategory.FOOTWEAR       -> validateFootwear(m)
            OrderCategory.THREE_D_PRINT  -> validateThreeDPrint(m)
            OrderCategory.INTERIOR_DECOR -> validateInteriorDecor(m)
            OrderCategory.OTHER          -> validateGeneric(m)
            OrderCategory.PROSTHETIC_COVER -> validateProsthesisCover(m)
            OrderCategory.INSOLE         -> validateFootwear(m)
        }

    // ── JEWELRY ───────────────────────────────────────────────────────────────
    // height = band thickness or piece length
    // width  = band width or piece width
    // depth  = EU ring size ordinal (stored by AccessorySizeSection) — optional
    //
    // Real-world limits:
    //   Ring band thickness: 0.5–20 mm (fine wire to statement ring)
    //   Ring band width:     1–30 mm
    //   Earring length:      2–80 mm (stud to chandelier)
    //   Bracelet width:      2–50 mm

    private fun validateJewelry(m: Measurement): String? {
        if (m.height <= 0.0) return "Please enter the piece thickness or length"
        if (m.height < 0.5)  return "Minimum thickness is 0.5 mm — thinner pieces cannot be fabricated reliably"
        if (m.height > 80.0) return "Maximum jewellery length is 80 mm"
        if (m.width > 0.0 && m.width < 1.0)  return "Minimum width is 1 mm"
        if (m.width > 50.0)  return "Maximum jewellery width is 50 mm"
        return null
    }

    // ── ACCESSORY ─────────────────────────────────────────────────────────────
    // Covers phone cases, bags, small wearable accessories.
    // Real-world limits:
    //   Smallest meaningful accessory: ~10 × 5 × 1 mm (keyring tag)
    //   Largest practical accessory:   400 × 300 × 200 mm (large bag panel)

    private fun validateAccessory(m: Measurement): String? {
        if (m.height <= 0.0 && m.width <= 0.0)
            return "Enter at least one dimension"
        if (m.height > 0.0 && m.height < 5.0)
            return "Minimum accessory height is 5 mm"
        if (m.width > 0.0 && m.width < 5.0)
            return "Minimum accessory width is 5 mm"
        if (m.height > 400.0)
            return "Maximum accessory height is 400 mm"
        if (m.width > 300.0)
            return "Maximum accessory width is 300 mm"
        if (m.depth > 200.0)
            return "Maximum accessory depth is 200 mm"
        return null
    }

    // ── CLOTHING — body measurements ──────────────────────────────────────────
    // Field remapping (matches ClothingSizeSection in ProductConfigScreen):
    //   height → body height in cm  (required, 100–220 cm)
    //   width  → body weight in kg  (optional, 30–300 kg)
    //   depth  → waist circumference in cm (optional, 40–200 cm)
    //
    // Note: values are stored in cm here (not mm) — the screen passes them
    // directly without conversion because human body measurements are in cm.

    private fun validateClothingBody(m: Measurement): String? {
        // height is the only required field for 3D generation
        if (m.height <= 0.0)
            return "Body height is required to generate the clothing model"
        if (m.height < 100.0)
            return "Body height must be at least 100 cm"
        if (m.height > 220.0)
            return "Body height cannot exceed 220 cm"
        // weight — optional but must be realistic if provided
        if (m.width > 0.0 && m.width < 30.0)
            return "Body weight must be at least 30 kg if provided"
        if (m.width > 300.0)
            return "Body weight cannot exceed 300 kg"
        // waist — optional but must be realistic if provided
        if (m.depth > 0.0 && m.depth < 40.0)
            return "Waist circumference must be at least 40 cm if provided"
        if (m.depth > 200.0)
            return "Waist circumference cannot exceed 200 cm"
        return null
    }

    // ── FOOTWEAR ──────────────────────────────────────────────────────────────
    // Field remapping (matches FootwearSizeSection in ProductConfigScreen):
    //   height → foot length in mm  (EU 35 ≈ 222 mm … EU 46 ≈ 295 mm)
    //   width  → foot width in mm   (typical 70–110 mm)
    //   depth  → EU size ordinal (0 if not selected by grid)
    //
    // Real-world limits:
    //   Shortest adult foot: ~200 mm (EU 33/34 — included for edge cases)
    //   Longest adult foot:  ~320 mm (EU 48+)

    private fun validateFootwear(m: Measurement): String? {
        // at least one of foot length or EU size grid must be set
        val hasEuSize    = m.depth in 35.0..46.0
        val hasFootLength = m.height > 0.0

        if (!hasEuSize && !hasFootLength)
            return "Please select an EU size or enter your foot length"
        if (hasFootLength && m.height < 180.0)
            return "Foot length must be at least 180 mm (EU 33)"
        if (hasFootLength && m.height > 340.0)
            return "Foot length cannot exceed 340 mm (EU 50+)"
        if (m.width > 0.0 && m.width < 60.0)
            return "Foot width must be at least 60 mm if provided"
        if (m.width > 130.0)
            return "Foot width cannot exceed 130 mm"
        return null
    }

    // ── THREE_D_PRINT ─────────────────────────────────────────────────────────
    // Constrained by typical consumer FDM/resin printer build volumes:
    //   FDM (e.g. Bambu X1C):  256 × 256 × 256 mm
    //   Large FDM:              350 × 350 × 400 mm
    //   Minimum printable wall: 0.4 mm (one nozzle width)
    //
    // We use the larger FDM envelope as max to support pro setups.

    private fun validateThreeDPrint(m: Measurement): String? {
        if (m.height <= 0.0 && m.width <= 0.0 && m.depth <= 0.0)
            return "Enter at least one dimension"
        if (m.height > 0.0 && m.height < 0.4)
            return "Minimum printable height is 0.4 mm (one nozzle width)"
        if (m.width > 0.0 && m.width < 0.4)
            return "Minimum printable width is 0.4 mm"
        if (m.depth > 0.0 && m.depth < 0.4)
            return "Minimum printable depth is 0.4 mm"
        if (m.height > 400.0)
            return "Height exceeds maximum printer bed size (400 mm)"
        if (m.width > 350.0)
            return "Width exceeds maximum printer bed size (350 mm)"
        if (m.depth > 350.0)
            return "Depth exceeds maximum printer bed size (350 mm)"
        return null
    }

    // ── INTERIOR_DECOR ────────────────────────────────────────────────────────
    // Must fit through a standard door and be liftable by one person:
    //   Standard EU door opening: 860 × 2000 mm
    //   Max liftable weight (single person): roughly correlates to ~2 m³ at low density
    //   Minimum meaningful décor item: a small figurine ~20 mm
    //   Maximum practical: a large shelf/panel ~3000 mm in longest dimension

    private fun validateInteriorDecor(m: Measurement): String? {
        if (m.height <= 0.0 && m.width <= 0.0 && m.depth <= 0.0)
            return "Enter at least one dimension"
        if (m.height > 0.0 && m.height < 20.0)
            return "Minimum height for a décor item is 20 mm"
        if (m.width > 0.0 && m.width < 20.0)
            return "Minimum width for a décor item is 20 mm"
        if (m.depth > 0.0 && m.depth < 1.0)
            return "Minimum depth is 1 mm"
        // door constraint — at least two dimensions must fit through a standard door
        val dims = listOf(m.height, m.width, m.depth).filter { it > 0.0 }.sorted()
        if (dims.size >= 2 && dims[dims.size - 2] > 860.0)
            return "Two or more dimensions exceed a standard door width (860 mm) — the item cannot be moved indoors"
        if (m.height > 3000.0 || m.width > 3000.0 || m.depth > 3000.0)
            return "Maximum dimension for a décor item is 3000 mm"
        return null
    }

    // ── OTHER — permissive generic bounds ─────────────────────────────────────

    private fun validateGeneric(m: Measurement): String? {
        if (m.height < 0.0 || m.width < 0.0 || m.depth < 0.0)
            return "Dimensions cannot be negative"
        if (m.height > 10_000.0 || m.width > 10_000.0 || m.depth > 10_000.0)
            return "Dimension exceeds 10 000 mm — please check your input"
        return null
    }
    private fun validateProsthesisCover(m: Measurement): String? { ///TODO add validation for prosthesis cover
        if (m.height < 0.0 || m.width < 0.0 || m.depth < 0.0)
            return "Dimensions cannot be negative"
        if (m.height > 10_000.0 || m.width > 10_000.0 || m.depth > 10_000.0)
            return "Dimension exceeds 10 000 mm — please check your input"
        return null
    }

    // ── Other field validators (category-independent) ─────────────────────────

    fun validateInscription(text: String): String? =
        if (text.length > 50) "Inscription must be 50 characters or fewer" else null

    fun validateQuantity(qty: Int): String? = when {
        qty < 1   -> "Quantity must be at least 1"
        qty > 999 -> "Quantity cannot exceed 999"
        else      -> null
    }
}
//
//class MeasurementValidator {
//    fun validate(m: Measurement): String? = when {
//        m.height <= 0    -> "Height must be greater than 0"
//        m.width  <= 0    -> "Width must be greater than 0"
//        m.depth  <= 0    -> "Depth must be greater than 0"
//        m.height > 1000  -> "Height exceeds maximum (1000 mm)"
//        m.width  > 1000  -> "Width exceeds maximum (1000 mm)"
//        m.depth  > 1000  -> "Depth exceeds maximum (1000 mm)"
//        else -> null
//    }
//
//    fun validateInscription(text: String): String? = when {
//        text.length > 50 -> "Inscription must be 50 characters or fewer"
//        else -> null
//    }
//    fun validateClothing(text: String): String? = when {
//        text.length > 50 -> "Inscription must be 50 characters or fewer"
//        else -> null
//    }
//
//    fun validateQuantity(qty: Int): String? = when {
//        qty < 1  -> "Quantity must be at least 1"
//        qty > 999 -> "Quantity cannot exceed 999"
//        else -> null
//    }
//    fun validateInterior(
//        measurement: Measurement
//    ) {
//    }
//}