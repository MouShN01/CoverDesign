package com.example.coverdesign.domain.usecase

import com.example.coverdesign.domain.model.ArSessionState
import com.example.coverdesign.domain.model.ProductConfig
import com.example.coverdesign.domain.repo.ArSessionRepository
import com.example.coverdesign.domain.util.Resource
import com.example.coverdesign.domain.repo.ProductRepository
import com.example.coverdesign.domain.validator.MeasurementValidator
import kotlinx.coroutines.flow.firstOrNull

// shared/commonMain/domain/usecase/UpdateConfigUseCase.kt      FR7
class UpdateConfigUseCase(
    private val repo: ProductRepository,
    private val arRepo: ArSessionRepository,
    private val validator: MeasurementValidator
) {
    suspend operator fun invoke(updated: ProductConfig): Resource<ProductConfig> {
        val error = validator.validate(updated.measurement)
        if (error != null) return Resource.Error(error)

        repo.saveConfig(updated)

        // if an AR session is live, push the new params for instant re-render
        if (arRepo.observeSessionState().firstOrNull() is ArSessionState.Active) {
            arRepo.updateOverlayConfig(updated)
        }

        return Resource.Success(updated)
    }
}