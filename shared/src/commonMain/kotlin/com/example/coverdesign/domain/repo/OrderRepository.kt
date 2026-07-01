package com.example.coverdesign.domain.repo

import com.example.coverdesign.domain.model.Order
import com.example.coverdesign.domain.model.OrderId
import com.example.coverdesign.domain.util.Resource
import kotlinx.coroutines.flow.Flow

// shared/commonMain/domain/repo/OrderRepository.kt
interface OrderRepository {
    suspend fun submitOrder(order: Order): Resource<OrderId>
    suspend fun saveOrderLocally(order: Order)  : Resource<Unit>
    fun getOrderHistory(): Flow<List<Order>>
    suspend fun getOrderById(id: String): Resource<Order>
    suspend fun linkOrderToAdaptedModel(configId: String, adaptedModelId: String) :Resource<Unit>
}