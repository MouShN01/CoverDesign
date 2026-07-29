package com.example.coverdesign.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class LimbLevel(val displayName: String) {
    ABOVE_KNEE("Above Knee"),
    BELOW_KNEE("Below Knee")
}

@Serializable
enum class LegSide(val displayName: String) {
    LEFT("Left"),
    RIGHT("Right")
}
