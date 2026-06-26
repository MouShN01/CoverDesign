package com.example.coverdesign.domain.usecase

import com.example.coverdesign.domain.model.AdaptedModel3D
import com.example.coverdesign.domain.model.ProductConfig
import com.example.coverdesign.domain.repo.Model3DRepository
import com.example.coverdesign.domain.util.Resource

// shared/commonMain/domain/usecase/GenerateModelUseCase.kt     FR4
class GenerateModelUseCase(private val repo: Model3DRepository) {
    suspend operator fun invoke(config: ProductConfig): Resource<AdaptedModel3D> =
        repo.generateAdaptedModel(config)
}