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
import com.example.coverdesign.domain.repo.OrderRepository
import com.example.coverdesign.domain.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

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

    override suspend fun linkOrderToAdaptedModel(configId: String, adaptedModelId: String): Resource<Unit> {
        // find the DRAFT order for this config
        try {
            val order = db.orderQueries.selectOrderByConfigId(configId).executeAsOneOrNull()
                ?: return Resource.Error(
                    "Order not found for config: $configId",
                    cause = Exception("Order not found for config: $configId")
                )
            db.orderQueries.updateAdaptedModelId(
                id = order.id,
                adaptedModelId = adaptedModelId
            )
            db.orderQueries.updateOrderStatus(
                id = order.id,
                status = OrderStatus.SUBMITTED.name
            )
            return Resource.Success(Unit)
        } catch (e: Exception) {
            return Resource.Error(
                message = e.message.toString(),
                cause = e.cause
            )
        }
    }

    override suspend fun saveOrderLocally(order: Order): Resource<Unit> = /// Resourse Order
        withContext(Dispatchers.IO) {
            try {
                db.orderQueries.insertOrder(order.toEntity())
                return@withContext Resource.Success(Unit)
            } catch (e: Exception) {
                return@withContext Resource.Error(e.message.toString(), e.cause)
            }
        }

    override fun getOrderHistory(): Flow<List<Order>> =
        db.orderQueries.selectAllOrders()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { entities -> entities.map { it.toDomain() } }

    override suspend fun getOrderById(id: String): Resource<Order> =
        withContext(Dispatchers.Default) {
            val byOrderId = db.orderQueries.selectOrderById(id).executeAsOneOrNull()
            if (byOrderId != null) return@withContext Resource.Success(byOrderId.toDomain())

            val byConfigId = db.orderQueries.selectOrderByConfigId(id).executeAsOneOrNull()
            if (byConfigId != null) return@withContext Resource.Success(byConfigId.toDomain())

            try {
                Resource.Success(api.getOrderById(id).toDomain())
            } catch (e: Exception) {
                Resource.Error("Order not found: ${e.message}", e)
            }
        }
}