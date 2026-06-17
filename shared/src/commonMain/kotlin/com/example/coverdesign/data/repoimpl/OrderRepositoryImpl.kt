package com.example.coverdesign.data.repoimpl

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.example.coverdesign.data.dto.SubmitOrderRequestDto
import com.example.coverdesign.data.dto.toDomain
import com.example.coverdesign.data.local.toDomain
import com.example.coverdesign.data.local.toEntity
import com.example.coverdesign.data.remote.service.OrderApiService
import com.example.coverdesign.db.AppDatabase
import com.example.coverdesign.domain.model.Order
import com.example.coverdesign.domain.model.OrderId
import com.example.coverdesign.domain.model.OrderStatus
import com.example.coverdesign.domain.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import com.example.coverdesign.domain.repo.OrderRepository
import kotlinx.coroutines.IO

class OrderRepositoryImpl(
    private val api: OrderApiService,
    private val db: AppDatabase
) : OrderRepository {

    override suspend fun submitOrder(order: Order): Resource<OrderId> =
        withContext(Dispatchers.Default) {
            try {
                val request = SubmitOrderRequestDto(
                    configId = order.config.id,
                    notes = order.config.additionalNotes
                )
                val response = api.submitOrder(request)

                // mark local order as submitted
                db.orderQueries.updateOrderStatus(
                    status = OrderStatus.SUBMITTED.name,
                    id = order.id
                )

                Resource.Success(OrderId(response.orderId))
            } catch (e: Exception) {
                // order stays DRAFT locally — user can retry later
                Resource.Error("Order submission failed: ${e.message}", e)
            }
        }

    override suspend fun saveOrderLocally(order: Order): Unit =
        withContext(Dispatchers.IO) { //TODO CHECK LATER plus Unit return type
            db.orderQueries.insertOrder(order.toEntity())
        }

    override fun getOrderHistory(): Flow<List<Order>> =
        db.orderQueries.selectAllOrders()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { entities -> entities.map { it.toDomain() } }

    override suspend fun getOrderById(id: String): Resource<Order> =
        withContext(Dispatchers.Default) {
            val local = db.orderQueries.selectOrderById(id).executeAsOneOrNull()
            if (local != null) Resource.Success(local.toDomain())
            else {
                try {
                    Resource.Success(api.getOrderById(id).toDomain())
                } catch (e: Exception) {
                    Resource.Error("Order not found: ${e.message}", e)
                }
            }
        }
}