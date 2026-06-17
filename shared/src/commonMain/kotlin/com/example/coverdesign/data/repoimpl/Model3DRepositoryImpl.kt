package com.example.coverdesign.data.repoimpl

import com.example.coverdesign.data.dto.toDomain
import com.example.coverdesign.data.dto.toDto
import com.example.coverdesign.data.remote.service.Model3DApiService
import com.example.coverdesign.domain.model.Model3D
import com.example.coverdesign.domain.model.ModelStatus
import com.example.coverdesign.domain.model.ProductConfig
import com.example.coverdesign.domain.model.Texture
import com.example.coverdesign.domain.repo.Model3DRepository
import com.example.coverdesign.domain.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class Model3DRepositoryImpl(
    private val api: Model3DApiService
) : Model3DRepository {

    override suspend fun generateModel(config: ProductConfig): Resource<Model3D> =
        withContext(Dispatchers.Default) {
            try {
                Resource.Success(api.generateModel(config.toDto()).toDomain())
            } catch (e: Exception) {
                Resource.Error("Model generation failed: ${e.message}", e)
            }
        }

    override suspend fun getModelUrl(configId: String): Resource<String> =
        withContext(Dispatchers.Default) {
            try {
                Resource.Success(api.getModelUrl(configId))
            } catch (e: Exception) {
                Resource.Error("Could not fetch model URL: ${e.message}", e)
            }
        }

    override fun getModelStatus(modelId: String): Flow<ModelStatus> = flow {
        // poll backend until model is READY or FAILED (FR5)
        while (true) {
            val status = try {
                api.getModelStatus(modelId).toDomain().status
            } catch (e: Exception) {
                ModelStatus.FAILED
            }
            emit(status)
            if (status == ModelStatus.READY || status == ModelStatus.FAILED) break
            delay(2_000L)
        }
    }

    override suspend fun getTextures(): Resource<List<Texture>> =
        withContext(Dispatchers.Default) {
            try {
                Resource.Success(api.getTextures().map { it.toDomain() })
            } catch (e: Exception) {
                Resource.Error("Could not load textures: ${e.message}", e)
            }
        }
}