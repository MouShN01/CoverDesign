package com.example.coverdesign.domain.usecase

import com.example.coverdesign.domain.repo.OrderRepository
import com.example.coverdesign.domain.util.Resource

class LinkOrderToAdaptedModelUseCase(
    private val orderRepo: OrderRepository
) {
    suspend operator fun invoke(
        configId: String,
        adaptedModelId: String
    ): Resource<Unit> {
        orderRepo.linkOrderToAdaptedModel(configId, adaptedModelId)
        return Resource.Success(Unit)
    }

}
