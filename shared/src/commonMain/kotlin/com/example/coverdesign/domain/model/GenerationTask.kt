package com.example.coverdesign.domain.model

/**
 * adapted model — result of GenAI pipeline for a specific configuration
  */
data class GenerationTask(
    val id: String,
    val configId: String, //OrderId
    val baseModelId: String, //excluding this for now
    val modelUrl: String?,
    val previewImageUrl: String?,
    val status: ModelStatus,
    val generatedAt: Long,
    val error: String?,
    val estimatedWaitSeconds: Int? = null,
    val progress: Long
)