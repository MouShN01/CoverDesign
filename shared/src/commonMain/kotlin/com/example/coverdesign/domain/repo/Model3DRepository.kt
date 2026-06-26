package com.example.coverdesign.domain.repo

import com.example.coverdesign.domain.model.AdaptedModel3D
import com.example.coverdesign.domain.model.Model3D
import com.example.coverdesign.domain.model.ModelStatus
import com.example.coverdesign.domain.model.ProductConfig
import com.example.coverdesign.domain.model.Texture
import com.example.coverdesign.domain.util.Resource
import kotlinx.coroutines.flow.Flow

interface Model3DRepository {
    // base model — for 3D browse tab
    suspend fun getBaseModelForProduct(productId: String): Resource<Model3D>

    // adapted model — for config pipeline (FR4) and what ModelPreview/AR actually display
    suspend fun generateAdaptedModel(config: ProductConfig): Resource<AdaptedModel3D>
    suspend fun getAdaptedModel(configId: String): Resource<AdaptedModel3D>
    fun getAdaptedModelStatus(adaptedModelId: String): Flow<ModelStatus>

    suspend fun getTextures(): Resource<List<Texture>>
}