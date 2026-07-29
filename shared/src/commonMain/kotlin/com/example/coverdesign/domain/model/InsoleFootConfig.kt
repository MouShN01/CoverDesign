package com.example.coverdesign.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class InsoleFootConfig(
    val euSize: Int = 0,
    val lengthMm: Float = 0f,
    val widthMm: Float = 0f,
    val side: LegSide
)