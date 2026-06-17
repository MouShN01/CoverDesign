package com.example.coverdesign.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Measurement(
    val height: Double,   // mm
    val width: Double,    // mm
    val depth: Double,    // mm
    val isPreciseMode: Boolean = false,  // switches to μm when true
    val customSizeMm: Double? = null    // free numeric size if no label applies
) {
    fun toDisplayUnit(): Map<String, String> {
        val unit = if (isPreciseMode) "μm" else "mm"
        val factor = if (isPreciseMode) 1000.0 else 1.0
        return mapOf(
            "height" to "${height * factor} $unit",
            "width"  to "${width  * factor} $unit",
            "depth"  to "${depth  * factor} $unit"
        )
    }
}
