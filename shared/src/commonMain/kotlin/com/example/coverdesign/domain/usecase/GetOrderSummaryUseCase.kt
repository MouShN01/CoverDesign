package com.example.coverdesign.domain.usecase

import com.example.coverdesign.domain.model.Order
import com.example.coverdesign.domain.repo.OrderRepository
import com.example.coverdesign.domain.util.Resource
import kotlinx.coroutines.flow.Flow

// shared/commonMain/domain/usecase/GetOrderSummaryUseCase.kt   FR10
class GetOrderSummaryUseCase(private val repo: OrderRepository) {
    fun observeHistory(): Flow<List<Order>> = repo.getOrderHistory()
    suspend operator fun invoke(orderId: String): Resource<Order> =
        repo.getOrderById(orderId)
}