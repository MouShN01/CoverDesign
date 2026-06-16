package com.example.coverdesign.data.remote.service

// data/remote/service/Model3DApiService.kt
import com.example.coverdesign.data.dto.Model3DDto
import com.example.coverdesign.data.dto.ProductConfigDto
import com.example.coverdesign.data.dto.TextureDto
import com.example.coverdesign.data.remote.client.ApiConstants
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.*
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.*
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

class Model3DApiService(private val client: HttpClient) {

    suspend fun generateModel(config: ProductConfigDto): Model3DDto =
        client.post(ApiConstants.Endpoints.GENERATE_MODEL) {
            setBody(config)
        }.body()

    suspend fun getModelUrl(modelId: String): String =
        client.get(
            ApiConstants.Endpoints.MODEL_URL.replace("{id}", modelId)
        ).body<Map<String, String>>()["url"] ?: ""

    suspend fun getModelStatus(modelId: String): Model3DDto =
        client.get(
            ApiConstants.Endpoints.MODEL_STATUS.replace("{id}", modelId)
        ).body()

    suspend fun getTextures(): List<TextureDto> =
        client.get(ApiConstants.Endpoints.TEXTURES).body()

}