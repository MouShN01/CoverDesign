package com.example.coverdesign.domain.usecase

import com.example.coverdesign.domain.model.OrderDetail
import com.example.coverdesign.domain.repo.Model3DRepository
import com.example.coverdesign.domain.repo.OrderRepository
import com.example.coverdesign.domain.util.Resource

class GetOrderDetailUseCase(
    private val orderRepo  : OrderRepository,
    private val model3DRepo: Model3DRepository,
) {
    suspend operator fun invoke(id: String): Resource<OrderDetail> {
        val base = orderRepo.getOrderDetailById(id)
        if (base !is Resource.Success) return base

        return when (val detail = base.data) {
            is OrderDetail.ProstheticCoverOrder -> {
                val modelsResult = model3DRepo.getAllAdaptedModelsByConfigId(detail.config.orderId)
                val models = if (modelsResult is Resource.Success) modelsResult.data else emptyList()
                Resource.Success(detail.copy(adaptedModels = models))
            }
            else -> base
        }
    }
}