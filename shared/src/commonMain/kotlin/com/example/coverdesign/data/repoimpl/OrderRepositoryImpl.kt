package com.example.coverdesign.data.repoimpl

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.example.coverdesign.data.dto.SubmitInsoleOrderRequestDto
import com.example.coverdesign.data.dto.SubmitOrderRequestDto
import com.example.coverdesign.data.dto.SubmitProstheticCoverOrderRequestDto
import com.example.coverdesign.data.dto.toDomain
import com.example.coverdesign.data.dto.toDto
import com.example.coverdesign.data.local.toDomain
import com.example.coverdesign.data.local.toEntity
import com.example.coverdesign.data.remote.service.OrderApiService
import com.example.coverdesign.db.AppDatabase
import com.example.coverdesign.domain.model.InsoleOrderConfig
import com.example.coverdesign.domain.model.Order
import com.example.coverdesign.domain.model.OrderCategory
import com.example.coverdesign.domain.model.OrderDetail
import com.example.coverdesign.domain.model.OrderId
import com.example.coverdesign.domain.model.OrderListItem
import com.example.coverdesign.domain.model.OrderStatus
import com.example.coverdesign.domain.model.ProstheticCoverOrderConfig
import com.example.coverdesign.domain.repo.OrderRepository
import com.example.coverdesign.domain.util.Resource
import com.example.coverdesign.domain.util.currentEpochMillis
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class OrderRepositoryImpl(
    private val api: OrderApiService,
    private val db: AppDatabase
) : OrderRepository {

    // ── Catalog-based order flow ───────────────────────────────────────────────

    override suspend fun submitOrder(order: Order): Resource<OrderId> =
        withContext(Dispatchers.Default) {
            try {
                val request = SubmitOrderRequestDto(
                    configId = order.config.id,
                    notes = order.config.additionalNotes
                )
                val response = api.submitOrder(request)
                db.orderQueries.updateOrderStatus(
                    status = OrderStatus.SUBMITTED.name,
                    id = order.id
                )
                Resource.Success(OrderId(response.orderId))
            } catch (e: Exception) {
                Resource.Error("Order submission failed: ${e.message}", e)
            }
        }

    override suspend fun saveOrderLocally(order: Order): Resource<Unit> =
        withContext(Dispatchers.IO) {
            try {
                db.orderQueries.insertOrder(order.toEntity())
                Resource.Success(Unit)
            } catch (e: Exception) {
                Resource.Error(e.message.toString(), e.cause)
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

    override suspend fun linkOrderToAdaptedModel(
        configId: String,
        adaptedModelId: String
    ): Resource<Unit> {
        return try {
            val order = db.orderQueries.selectOrderByConfigId(configId).executeAsOneOrNull()
                ?: return Resource.Error(
                    "Order not found for config: $configId",
                    cause = Exception("Order not found for config: $configId")
                )
            db.orderQueries.updateAdaptedModelId(id = order.id, adaptedModelId = adaptedModelId)
            db.orderQueries.updateOrderStatus(id = order.id, status = OrderStatus.SUBMITTED.name)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message.toString(), e.cause)
        }
    }

    // ── Insole direct-order flow ───────────────────────────────────────────────

    override suspend fun submitInsoleOrder(config: InsoleOrderConfig): Resource<OrderId> =
        withContext(Dispatchers.Default) {
            try {
                val request = SubmitInsoleOrderRequestDto(config = config.toDto())
                val response = api.submitInsoleOrder(request)
                db.insoleOrderConfigQueries.updateInsoleOrderStatus(
                    status = OrderStatus.SUBMITTED.name,
                    orderId = config.orderId
                )
                Resource.Success(OrderId(response.orderId))
            } catch (e: Exception) {
                Resource.Error("Insole order submission failed: ${e.message}", e)
            }
        }

    override suspend fun saveInsoleOrderLocally(config: InsoleOrderConfig): Resource<Unit> =
        withContext(Dispatchers.IO) {
            try {
                db.insoleOrderConfigQueries.insertInsoleOrder(
                    config.toEntity(createdAt = currentEpochMillis())
                )
                Resource.Success(Unit)
            } catch (e: Exception) {
                Resource.Error(e.message.toString(), e.cause)
            }
        }

    override fun getInsoleOrderHistory(): Flow<List<InsoleOrderConfig>> =
        db.insoleOrderConfigQueries.selectAllInsoleOrders()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { entities -> entities.map { it.toDomain() } }

    override suspend fun getInsoleOrderById(orderId: String): Resource<InsoleOrderConfig> =
        withContext(Dispatchers.Default) {
            val entity = db.insoleOrderConfigQueries
                .selectInsoleOrderById(orderId)
                .executeAsOneOrNull()
            if (entity != null) Resource.Success(entity.toDomain())
            else Resource.Error("Insole order not found: $orderId")
        }

    // ── Prosthetic Cover direct-order flow ────────────────────────────────────

    override suspend fun submitProstheticCoverOrder(
        config: ProstheticCoverOrderConfig
    ): Resource<OrderId> =
        withContext(Dispatchers.Default) {
            try {
                val request = SubmitProstheticCoverOrderRequestDto(config = config.toDto())
                val response = api.submitProstheticCoverOrder(request)
                db.prostheticCoverOrderConfigQueries.updateProstheticCoverOrderStatus(
                    status = OrderStatus.SUBMITTED.name,
                    orderId = config.orderId
                )
                Resource.Success(OrderId(response.orderId))
            } catch (e: Exception) {
                Resource.Error("Prosthetic cover order submission failed: ${e.message}", e)
            }
        }

    override suspend fun saveProstheticCoverOrderLocally(
        config: ProstheticCoverOrderConfig
    ): Resource<Unit> =
        withContext(Dispatchers.IO) {
            try {
                db.prostheticCoverOrderConfigQueries.insertProstheticCoverOrder(config.toEntity())
                Resource.Success(Unit)
            } catch (e: Exception) {
                Resource.Error(e.message.toString(), e.cause)
            }
        }

    override fun getProstheticCoverOrderHistory(): Flow<List<ProstheticCoverOrderConfig>> =
        db.prostheticCoverOrderConfigQueries.selectAllProstheticCoverOrders()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { entities -> entities.map { it.toDomain() } }

    override suspend fun getProstheticCoverOrderById(
        orderId: String
    ): Resource<ProstheticCoverOrderConfig> =
        withContext(Dispatchers.Default) {
            val entity = db.prostheticCoverOrderConfigQueries
                .selectProstheticCoverOrderById(orderId)
                .executeAsOneOrNull()
            if (entity != null) Resource.Success(entity.toDomain())
            else Resource.Error("Prosthetic cover order not found: $orderId")
        }

    // ── Unified detail lookup ─────────────────────────────────────────────────

    override suspend fun getOrderDetailById(id: String): Resource<OrderDetail> =
        withContext(Dispatchers.Default) {
            // 1. catalog order — try by orderId then by configId (post-config flow passes configId)
            val catalogEntity = db.orderQueries.selectOrderById(id).executeAsOneOrNull()
                ?: db.orderQueries.selectOrderByConfigId(id).executeAsOneOrNull()
            if (catalogEntity != null)
                return@withContext Resource.Success(OrderDetail.CatalogOrder(catalogEntity.toDomain()))

            // 2. insole order
            val insoleEntity = db.insoleOrderConfigQueries
                .selectInsoleOrderById(id).executeAsOneOrNull()
            if (insoleEntity != null)
                return@withContext Resource.Success(
                    OrderDetail.InsoleOrder(
                        config = insoleEntity.toDomain(),
                        status = OrderStatus.valueOf(insoleEntity.status)
                    )
                )

            // 3. prosthetic cover order (adapted models loaded by GetOrderDetailUseCase)
            val coverEntity = db.prostheticCoverOrderConfigQueries
                .selectProstheticCoverOrderById(id).executeAsOneOrNull()
            if (coverEntity != null)
                return@withContext Resource.Success(
                    OrderDetail.ProstheticCoverOrder(
                        config = coverEntity.toDomain(),
                        status = OrderStatus.valueOf(coverEntity.status)
                    )
                )

            // 4. fallback — fetch catalog order from API
            try {
                Resource.Success(OrderDetail.CatalogOrder(api.getOrderById(id).toDomain()))
            } catch (e: Exception) {
                Resource.Error("Order not found: $id")
            }
        }

    // ── Combined list ─────────────────────────────────────────────────────────

    override fun getOrderListItems(): Flow<List<OrderListItem>> = combine(
        db.insoleOrderConfigQueries.selectAllInsoleOrders()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { list ->
                list.map { e ->
                    OrderListItem(
                        orderId = e.orderId,
                        category = OrderCategory.INSOLE,
                        createdAt = e.createdAt,
                        totalPrice = e.totalPrice,
                        status = OrderStatus.valueOf(e.status)
                    )
                }
            },
        db.prostheticCoverOrderConfigQueries.selectAllProstheticCoverOrders()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { list ->
                list.map { e ->
                    OrderListItem(
                        orderId = e.orderId,
                        category = OrderCategory.COSMETIC_COVER,
                        createdAt = e.createdAt,
                        totalPrice = e.totalPrice,
                        status = OrderStatus.valueOf(e.status)
                    )
                }
            }
    ) { insoles, covers -> (insoles + covers).sortedByDescending { it.createdAt } }
}