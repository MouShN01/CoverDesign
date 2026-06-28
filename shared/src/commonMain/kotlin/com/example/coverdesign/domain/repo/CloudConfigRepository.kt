package com.example.coverdesign.domain.repo

import com.example.coverdesign.domain.model.Order
import com.example.coverdesign.domain.model.ProductConfig
import com.example.coverdesign.domain.util.Resource
import kotlinx.coroutines.flow.Flow

// ─────────────────────────────────────────────────────────────────────────────
// CloudConfigRepository — Firestore-backed storage for configs and orders.
//
// Firestore structure:
//   /users/{uid}/
//       configs/{configId}   → ProductConfig snapshot (all params, no local URIs)
//       orders/{orderId}     → Order snapshot with PriceEstimate breakdown
//
// This is separate from the local SQLite cache (SQLDelight).
// SQLite = fast offline read; Firestore = cross-device sync + order history.
// ─────────────────────────────────────────────────────────────────────────────

interface CloudConfigRepository {

    // ── Configs ───────────────────────────────────────────────────────────────

    /** Save or update a config snapshot in Firestore. */
    suspend fun saveConfig(config: ProductConfig, userId: String): Resource<Unit>

    /** Observe all configs for a user in real time. */
    fun observeConfigs(userId: String): Flow<Resource<List<ProductConfig>>>

    /** Fetch a single config by id. */
    suspend fun getConfig(configId: String, userId: String): Resource<ProductConfig>

    /** Delete a config (e.g. when user discards a draft). */
    suspend fun deleteConfig(configId: String, userId: String): Resource<Unit>

    // ── Orders ────────────────────────────────────────────────────────────────

    /** Save a completed order to Firestore. */
    suspend fun saveOrder(order: Order, userId: String): Resource<Unit>

    /** Observe all orders for a user in real time (for order history screen). */
    fun observeOrders(userId: String): Flow<Resource<List<Order>>>

    /** Update only the status field of an order (webhook / push update). */
    suspend fun updateOrderStatus(
        orderId : String,
        status  : String,
        userId  : String
    ): Resource<Unit>
}