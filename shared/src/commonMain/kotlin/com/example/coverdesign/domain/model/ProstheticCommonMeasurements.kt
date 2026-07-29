package com.example.coverdesign.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ProstheticCommonMeasurements(
    val circumferenceMm: Float = 0f,
    val kneeApexToFootshellTopMm: Float = 0f  // length: apex of knee unit to top of footshell
)