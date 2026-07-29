package com.example.coverdesign.domain.usecase

import com.example.coverdesign.domain.model.ProductConfig
import com.example.coverdesign.domain.repo.ProductRepository
import com.example.coverdesign.domain.util.Resource
import com.example.coverdesign.domain.validator.MeasurementValidator

// shared/commonMain/domain/usecase/ConfigureProductUseCase.kt  FR2, FR3
class ConfigureProductUseCase(
    private val repo: ProductRepository,
    private val validator: MeasurementValidator
) {
    suspend operator fun invoke(config: ProductConfig): Resource<ProductConfig> {
        val validationError = validator.validate(config.orderCategory,config.measurement)
        if (validationError != null) return Resource.Error(validationError)
        repo.saveConfig(config)
        return Resource.Success(config)
    }
}