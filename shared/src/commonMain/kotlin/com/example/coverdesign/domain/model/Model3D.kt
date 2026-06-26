package com.example.coverdesign.domain.model
// shared/commonMain/domain/model/Model3D.kt

// base model — fixed, tied to a product, never changes
data class Model3D(
    val id: String,
    val productId: String,
    val modelUrl: String,
    val previewImageUrl: String,
    val status: ModelStatus,
    val generatedAt: Long
)

enum class ModelStatus { PENDING, GENERATING, READY, FAILED }