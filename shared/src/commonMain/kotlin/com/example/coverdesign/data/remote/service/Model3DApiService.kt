package com.example.coverdesign.data.remote.service

// data/remote/service/Model3DApiService.kt
import com.example.coverdesign.data.dto.AdaptedModel3DDto
import com.example.coverdesign.data.dto.Model3DDto
import com.example.coverdesign.data.dto.ProductConfigDto
import com.example.coverdesign.data.dto.TextureDto
import com.example.coverdesign.data.remote.client.ApiConstants
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody

class Model3DApiService(private val client: HttpClient) {

    suspend fun getBaseModel(productId: String): Model3DDto =
        client.get(ApiConstants.Endpoints.BASE_MODEL.replace("{productId}", productId)).body()

    suspend fun generateAdaptedModel(config: ProductConfigDto): AdaptedModel3DDto =
        client.post(ApiConstants.Endpoints.GENERATE_MODEL) { setBody(config) }.body()

    suspend fun getAdaptedModelStatus(adaptedModelId: String): AdaptedModel3DDto =
        client.get(ApiConstants.Endpoints.MODEL_STATUS.replace("{id}", adaptedModelId)).body()

    suspend fun getTextures(): List<TextureDto> =
        client.get(ApiConstants.Endpoints.TEXTURES).body()

}