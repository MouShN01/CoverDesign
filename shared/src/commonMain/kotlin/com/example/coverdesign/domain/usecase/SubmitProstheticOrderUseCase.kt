package com.example.coverdesign.domain.usecase

import com.example.coverdesign.data.dto.toGenerateModelRequest
import com.example.coverdesign.domain.model.ProstheticCoverOrderConfig
import com.example.coverdesign.domain.model.pricing.toPriceEstimate
import com.example.coverdesign.domain.repo.OrderRepository
import com.example.coverdesign.domain.util.Resource

class SubmitProstheticOrderUseCase(
    private val orderRepo: OrderRepository,
    private val generateModel: GenerateProstheticCoverModelUseCase
) {
    suspend operator fun invoke(config: ProstheticCoverOrderConfig): Resource<String> {

        val estimate = config.toPriceEstimate()
        val type = config.prostheticType
        val classification =
            "${config.limbSide.displayName} | ${config.limbLevel?.displayName ?: ""}: ${type?.displayName}"
        val additionalNotes = config.additionalNotes.ifBlank { classification }

        val saved =
            orderRepo.saveProstheticCoverOrderLocally(
                config.copy(
                    additionalNotes = additionalNotes,
                    priceEstimate = estimate
                )
            )
        if (saved is Resource.Error) return Resource.Error(saved.message, saved.cause)
        return generateModel(config.toGenerateModelRequest())
    }
}