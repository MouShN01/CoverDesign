package com.example.coverdesign.domain.model
// shared/commonMain/domain/model/Model3D.kt

// base model — fixed, tied to a product, never changes
data class Model3D(
    val configId: String,
    val productId: String,
    val modelUrl: String,
    val previewImageUrl: String,
    val status: ModelStatus,
    val generatedAt: Long,
    //val estimatedWaitSeconds: Int? = null,
)

enum class ModelStatus { PENDING, GENERATING, READY, FAILED }
