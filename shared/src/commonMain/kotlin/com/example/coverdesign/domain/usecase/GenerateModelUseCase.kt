package com.example.coverdesign.domain.usecase

import com.example.coverdesign.domain.model.Model3D
import com.example.coverdesign.domain.model.ProductConfig
import com.example.coverdesign.domain.repo.Model3DRepository
import com.example.coverdesign.domain.util.Resource

// shared/commonMain/domain/usecase/GenerateModelUseCase.kt     FR4
class GenerateModelUseCase(private val repo: Model3DRepository) {
    suspend operator fun invoke(config: ProductConfig): Resource<Model3D> =
        repo.generateModel(config)
}