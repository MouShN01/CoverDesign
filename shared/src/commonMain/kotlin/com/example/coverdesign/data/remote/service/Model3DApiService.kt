package com.example.coverdesign.data.remote.service

// data/remote/service/Model3DApiService.kt
import com.example.coverdesign.data.dto.GenerationTaskDto
import com.example.coverdesign.data.dto.GenerateModelRequestDto
import com.example.coverdesign.data.dto.GenerateModelResponseDto
import com.example.coverdesign.data.dto.Model3DDto
import com.example.coverdesign.data.dto.TextureDto
import com.example.coverdesign.data.remote.client.ApiConstants
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody

class Model3DApiService(private val client: HttpClient) {

    suspend fun getBaseModel(productId: String): Model3DDto = client.get(
        ApiConstants.Endpoints.BASE_MODEL.replace("{productId}", productId)
    ).body()

    suspend fun submitGeneration(request: GenerateModelRequestDto): GenerateModelResponseDto =
        client.post(ApiConstants.Endpoints.GENERATE_COVER_MODEL) {
            println("submitGeneration beginning of the post")
            setBody(request)
        }.body()

    // ── Step 2: poll status by taskId ────────────────────────────────────────
    // Returns AdaptedModel3DDto with current status field
    // Call repeatedly until status == "ready" or "failed"
    suspend fun getGenerationStatus(taskId: String): GenerationTaskDto = client.get(
        ApiConstants.Endpoints.MODEL_STATUS.replace("{taskId}", taskId)
    ).body()

    // ── Textures ──────────────────────────────────────────────────────────────
    suspend fun getTextures(): List<TextureDto> = client.get(ApiConstants.Endpoints.TEXTURES).body()
}

