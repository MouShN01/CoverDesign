package com.example.coverdesign.domain.model

// shared/commonMain/domain/model/TryOnResult.kt
data class TryOnResult(
    val id: String,
    val overlayImageUrl: String,    // AR overlay returned from backend
    val configId: String
)