package com.example.coverdesign.domain.usecase

import com.example.coverdesign.domain.model.ModelStatus
import com.example.coverdesign.domain.repo.Model3DRepository
import com.example.coverdesign.domain.util.Resource
import kotlinx.coroutines.flow.Flow

// shared/commonMain/domain/usecase/GetModelPreviewUseCase.kt   FR5
class GetModelPreviewUseCase(private val repo: Model3DRepository) {
    suspend operator fun invoke(configId: String): Resource<String> =
        repo.getModelUrl(configId)

    fun observeStatus(modelId: String): Flow<ModelStatus> =
        repo.getModelStatus(modelId)
}