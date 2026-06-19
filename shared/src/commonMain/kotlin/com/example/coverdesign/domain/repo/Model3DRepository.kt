package com.example.coverdesign.domain.repo

import com.example.coverdesign.domain.model.Model3D
import com.example.coverdesign.domain.model.ModelStatus
import com.example.coverdesign.domain.model.ProductConfig
import com.example.coverdesign.domain.model.Texture
import com.example.coverdesign.domain.util.Resource
import kotlinx.coroutines.flow.Flow

interface Model3DRepository {
    suspend fun generateModel(config: ProductConfig): Resource<Model3D>
    suspend fun getModelUrl(configId: String): Resource<String>
    fun getModelStatus(modelId: String): Flow<ModelStatus>
    suspend fun getTextures(): Resource<List<Texture>>
    suspend fun getBaseModelForProduct(productId: String): Resource<Model3D>
}