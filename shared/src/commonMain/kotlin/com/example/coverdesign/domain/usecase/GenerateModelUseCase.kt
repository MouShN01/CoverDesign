package com.example.coverdesign.domain.usecase

import com.example.coverdesign.data.dto.GenerateModelRequestDto
import com.example.coverdesign.data.dto.ProductConfigDto
import com.example.coverdesign.domain.repo.Model3DRepository
import com.example.coverdesign.domain.util.Resource

// ─────────────────────────────────────────────────────────────────────────────
// GenerateModelUseCase
//
// Step 1: builds GenerateModelRequestDto from ProductConfigDto
// Step 2: submits POST → receives taskId
// Step 3: returns taskId immediately so the UI can navigate to the
//         waiting/preview screen which starts polling independently
// ─────────────────────────────────────────────────────────────────────────────
class GenerateModelUseCase(
    private val model3dRepo: Model3DRepository
) {
    suspend operator fun invoke(config: ProductConfigDto): Resource<String> {
        println("submitGeneration GenerateModelUseCase invoke")
        val request = config.toGenerationRequest()
        return model3dRepo.submitGeneration(request)
    }
}
//class GenerateModelUseCase(
//    private val model3dRepo: Model3DRepository
//) {
//    suspend operator fun invoke(config: ProductConfigDto): Resource<String> {
//        val request = config.toGenerationRequest()
//        return model3dRepo.submitGeneration(request)
//    }
//}

// ─────────────────────────────────────────────────────────────────────────────
// PollModelStatusUseCase
//
// Polls GET /status/{taskId} until status is READY or FAILED.
// Emits each intermediate Model3D so the UI can show live progress.
//
// Polling interval:
//   - uses estimatedWaitSeconds from the server response as a hint
//   - falls back to DEFAULT_POLL_INTERVAL_MS
//   - backs off to MAX_POLL_INTERVAL_MS after many retries
// ─────────────────────────────────────────────────────────────────────────────

// ─────────────────────────────────────────────────────────────────────────────
// Prompt builder — constructs a text prompt from the config for the 3D API
// ─────────────────────────────────────────────────────────────────────────────

private fun ProductConfigDto.toGenerationRequest(): GenerateModelRequestDto { //todo falls under "Product"
    val prompt = buildString {
        append("A personalised $productCategory item")
        append(" made of $material")
        if (colorName.isNotBlank()) append(", color: $colorName ($colorHex)")
        if (textureName.isNotBlank()) append(", texture: $textureName")
        if (inscription.isNotBlank()) append(", with inscription: \"$inscription\"")
        if (additionalElements.isNotEmpty())
            append(", with: ${additionalElements.joinToString()}")
    }

    return GenerateModelRequestDto(
        configId = id,
        textureUrl = texturePreviewUrl,   // Firebase Storage URL or null
        material = material,
        enablePbr = true,
        category = productCategory,
        productId = productId

    )
}