package com.example.coverdesign.data.dto

import com.example.coverdesign.domain.model.LimbLevel
import kotlinx.serialization.Serializable

/**
 * Serializable counterpart to [com.example.coverdesign.domain.model.ProstheticType]. Replaces the sealed-class hierarchy in order
 * configs and DTOs. Type-specific measurement values are stored as flat fields on
 * [com.example.coverdesign.domain.model.ProstheticCoverOrderConfig] rather than inside the type object.
 */
@Serializable
enum class ProstheticTypeEnumDto(val displayName: String) {
    // ── Above Knee ────────────────────────────────────────────────────────────
    ABOVE_KNEE_POLYCENTRIC_KNEE("Polycentric Knee"),
    ABOVE_KNEE_TOTAL_KNEE("Total Knee"),
    ABOVE_KNEE_THROUGH_KNEE("Through Knee"),
    ABOVE_KNEE_HIGH_PROFILE("High Profile"),
    ABOVE_KNEE_BLADE("Blade"),
    ABOVE_KNEE_MICROPROCESSOR("Microprocessor"),
    ABOVE_KNEE_HYDRAULIC_FOOT("Hydraulic Foot"),

    // ── Below Knee ────────────────────────────────────────────────────────────
    BELOW_KNEE_RELEASE_BUTTON("Release Button"),
    BELOW_KNEE_RELEASE_VALVE("Release Valve"),
    BELOW_KNEE_PINLOCK_BUTTON("Pinlock Button"),
    BELOW_KNEE_HIGH_PROFILE("High Profile"),
    BELOW_KNEE_BLADE("Blade"),
    BELOW_KNEE_MICROPROCESSOR("Microprocessor"),
    BELOW_KNEE_HYDRAULIC_FOOT("Hydraulic Foot"),
    BELOW_KNEE_ROTATIONPLASTY("Rotationplasty Prosthesis"),
    BELOW_KNEE_SYMES_SOCKET("Symes Socket");

    val limbLevel: LimbLevel get() = if (name.startsWith("ABOVE_KNEE")) LimbLevel.ABOVE_KNEE else LimbLevel.BELOW_KNEE

    /** True for types whose type-specific measurement is footshell-to-button-centre. */
    val hasButtonMeasurement: Boolean get() = this in setOf(
        BELOW_KNEE_RELEASE_BUTTON, BELOW_KNEE_RELEASE_VALVE, BELOW_KNEE_PINLOCK_BUTTON
    )

    /** True for types with a secondary measurement (Rotationplasty only). */
    val hasSecondaryMeasurement: Boolean get() = this == BELOW_KNEE_ROTATIONPLASTY
}