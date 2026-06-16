package com.example.coverdesign.domain.usecase

import com.example.coverdesign.domain.model.Order
import com.example.coverdesign.domain.model.OrderId
import com.example.coverdesign.domain.util.Resource
import com.example.coverdesign.domain.repo.OrderRepository

// shared/commonMain/domain/usecase/SubmitOrderUseCase.kt       FR9
class SubmitOrderUseCase(private val repo: OrderRepository) {
    suspend operator fun invoke(order: Order): Resource<OrderId> {
        repo.saveOrderLocally(order)          // local snapshot first
        return repo.submitOrder(order)        // then send to backend
    }
}