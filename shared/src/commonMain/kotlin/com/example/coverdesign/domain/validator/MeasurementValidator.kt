package com.example.coverdesign.domain.validator

import com.example.coverdesign.domain.model.Measurement

// shared/commonMain/domain/validator/MeasurementValidator.kt
class MeasurementValidator {
    fun validate(m: Measurement): String? = when {
        m.height <= 0  -> "Height must be greater than 0"
        m.width  <= 0  -> "Width must be greater than 0"
        m.depth  <= 0  -> "Depth must be greater than 0"
        m.height > 1000 -> "Height exceeds maximum allowed (1000 mm)"
        m.width  > 1000 -> "Width exceeds maximum allowed (1000 mm)"
        m.depth  > 1000 -> "Depth exceeds maximum allowed (1000 mm)"
        else -> null   // null = valid
    }
}