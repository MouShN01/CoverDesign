package com.example.coverdesign.domain.model

// shared/commonMain/domain/model/Model3D.kt
data class Model3D(
    val id: String,
    val configId: String,
    val modelUrl: String,       // URL to .glb / .usdz file
    val status: ModelStatus,
    val generatedAt: Long,
    val estimatedWaitSeconds: Int? = null,
//    val previewImageUrl: String,

    )

enum class ModelStatus {
    PENDING, GENERATING, READY, FAILED
}
