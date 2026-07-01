package com.example.coverdesign.data.repoimpl

import android.util.Log
import com.example.coverdesign.data.dto.ProductConfigDto
import com.example.coverdesign.data.dto.toDomain
import com.example.coverdesign.data.dto.toDto
import com.example.coverdesign.domain.model.Order
import com.example.coverdesign.domain.model.OrderStatus
import com.example.coverdesign.domain.model.ProductConfig
import com.example.coverdesign.domain.model.pricing.PriceEstimate
import com.example.coverdesign.domain.repo.CloudConfigRepository
import com.example.coverdesign.domain.util.Resource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.tasks.await


// ─────────────────────────────────────────────────────────────────────────────
// FirebaseCloudConfigRepositoryImpl
//
// Firestore structure:
//   users/{uid}/configs/{configId}  → ProductConfigDto fields (flat map)
//   users/{uid}/orders/{orderId}    → Order fields (flat map)
//
// Why flat maps and not nested objects?
// Firestore charges per field read in a document.  Flat maps let us
// updateOrderStatus() with a single field write without fetching the whole doc.
// ─────────────────────────────────────────────────────────────────────────────

class FirebaseCloudConfigRepositoryImpl(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : CloudConfigRepository {

    // ── collection helpers ────────────────────────────────────────────────────
    private fun configsRef(userId: String) =
        firestore.collection("users").document(userId).collection("configs")

    private fun ordersRef(userId: String) =
        firestore.collection("users").document(userId).collection("orders")

    // ── Configs ───────────────────────────────────────────────────────────────
    fun addOrderData(userId: String, order: Order) {

        firestore.collection("users").document("${userId}/${order.id}")
            .set(order)
            .addOnSuccessListener { documentReference ->
                Log.d("TAG", "DocumentSnapshot added with ID: $documentReference")
            }
            .addOnFailureListener { e ->
                Log.w("TAG", "Error adding document", e)
            }
    }

    override suspend fun saveConfig(
        config: ProductConfig,
        userId: String
    ): Resource<Unit> = try {
        // Convert to DTO first — this strips local content:// URIs automatically
        val dto = config.toDto()
        val data = dto.toFirestoreMap()
        configsRef(userId)
            .document(config.id)
            .set(data, SetOptions.merge())
            .await()
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error("Failed to save config: ${e.message}", e)
    }

    override fun observeConfigs(userId: String): Flow<Resource<List<ProductConfig>>> =
        callbackFlow {
            val listener = configsRef(userId)
                .orderBy("created_at", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(Resource.Error("Firestore error: ${error.message}", error))
                        return@addSnapshotListener
                    }
                    val configs = snapshot?.documents?.mapNotNull { doc ->
                        runCatching { doc.data?.toProductConfigDto()?.toDomain() }.getOrNull()
                    } ?: emptyList()
                    trySend(Resource.Success(configs))
                }
            awaitClose { listener.remove() }
        }.catch { emit(Resource.Error("Stream error: ${it.message}", it)) }

    override suspend fun getConfig(
        configId: String,
        userId: String
    ): Resource<ProductConfig> = try {
        val doc = configsRef(userId).document(configId).get().await()
        val dto = doc.data?.toProductConfigDto()
            ?: return Resource.Error("Config not found: $configId")
        Resource.Success(dto.toDomain())
    } catch (e: Exception) {
        Resource.Error("Failed to fetch config: ${e.message}", e)
    }

    override suspend fun deleteConfig(
        configId: String,
        userId: String
    ): Resource<Unit> = try {
        configsRef(userId).document(configId).delete().await()
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error("Failed to delete config: ${e.message}", e)
    }

    // ── Orders ────────────────────────────────────────────────────────────────

    override suspend fun saveOrder(
        order: Order,
        userId: String
    ): Resource<Unit> = try {
        val data = order.toFirestoreMap()
        ordersRef(userId)
            .document(order.id)
            .set(data, SetOptions.merge())
            .await()
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error("Failed to save order: ${e.message}", e)
    }

    override fun observeOrders(userId: String): Flow<Resource<List<Order>>> =
        callbackFlow {
            val listener = ordersRef(userId)
                .orderBy("created_at", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(Resource.Error("Firestore error: ${error.message}", error))
                        return@addSnapshotListener
                    }
                    val orders = snapshot?.documents?.mapNotNull { doc ->
                        runCatching { doc.data?.toOrder() }.getOrNull()
                    } ?: emptyList()
                    trySend(Resource.Success(orders))
                }
            awaitClose { listener.remove() }
        }.catch { emit(Resource.Error("Stream error: ${it.message}", it)) }

    override suspend fun updateOrderStatus(
        orderId: String,
        status: String,
        userId: String
    ): Resource<Unit> = try {
        ordersRef(userId)
            .document(orderId)
            .update("status", status)
            .await()
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error("Failed to update status: ${e.message}", e)
    }

    // ── Firestore serialisation helpers ───────────────────────────────────────

    private fun ProductConfigDto.toFirestoreMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "product_id" to productId,
        "product_category" to productCategory,
        "height" to heightMm,
        "width" to widthMm,
        "depth" to depthMm,
        "size_label" to sizeLabel,
        "color_name" to colorName,
        "color_hex" to colorHex,
        "material" to material,
        "texture_id" to textureId,
        "texture_name" to textureName,
        "texture_preview_url" to texturePreviewUrl,
        "texture_description" to textureDescription,
        "inscription" to inscription,
        "additional_elements" to additionalElements,
        "quantity" to quantity,
        "created_at" to createdAt
    )

    private fun Map<String, Any?>.toProductConfigDto(): ProductConfigDto = ProductConfigDto(
        id = get("id") as String,
        productId = get("product_id") as String,
        productCategory = get("product_category") as String,
        heightMm = (get("height") as Number).toDouble(),
        widthMm = (get("width") as Number).toDouble(),
        depthMm = (get("depth") as Number).toDouble(),
        sizeLabel = get("size_label") as? String,
        colorName = get("color_name") as? String ?: "",
        colorHex = get("color_hex") as? String ?: "#000000",
        material = get("material") as? String ?: "PLA",
        textureId = get("texture_id") as? String ?: "",
        textureName = get("texture_name") as? String ?: "",
        texturePreviewUrl = get("texture_preview_url") as? String,
        textureDescription = get("texture_description") as? String,
        inscription = get("inscription") as? String ?: "",
        additionalElements = (get("additional_elements") as? List<*>)
            ?.filterIsInstance<String>() ?: emptyList(),
        quantity = (get("quantity") as? Number)?.toInt() ?: 1,
        createdAt = (get("created_at") as? Number)?.toLong() ?: 0L,
        additionalNotes = get("additionalNotes") as String,
        customSizeMm = (get("customSizeMm") as Number).toDouble(),
    )

    private fun Order.toFirestoreMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "config_id" to config.id,
        "config_snapshot" to config.toDto().toFirestoreMap(),
        "base_price" to priceEstimate.basePrice,
        "material_surcharges" to priceEstimate.materialSurcharges,
        "texture_surcharge" to priceEstimate.textureSurcharge,
        "inscription_surcharge" to priceEstimate.inscriptionSurcharge,
        "element_surcharges" to priceEstimate.elementSurcharges,
        "size_multiplier" to priceEstimate.sizeMultiplier,
        "size_multiplier_label" to priceEstimate.sizeMultiplierLabel,
        "quantity" to priceEstimate.quantity,
        "subtotal" to priceEstimate.subtotal,
        "total" to priceEstimate.total,
        "currency" to priceEstimate.currency,
        "status" to status.name,
        "created_at" to createdAt,
        "adapted_model_id" to adaptedModelId
    )

    @Suppress("UNCHECKED_CAST")
    private fun Map<String, Any?>.toOrder(): Order {
        val configSnapshot = get("config_snapshot") as? Map<String, Any?>
        val config = configSnapshot?.toProductConfigDto()?.toDomain()
            ?: throw IllegalStateException("Missing config_snapshot in order document")

        return Order(
            id = get("id") as String,
            config = config,
            priceEstimate = PriceEstimate(
                configId = config.id,
                currency = get("currency") as? String ?: "USD",
                basePrice = (get("base_price") as Number).toDouble(),
                materialSurcharges = (get("material_surcharges") as? Map<String, Any?>)
                    ?.mapValues { (it.value as Number).toDouble() }
                    ?: emptyMap(),
                textureSurcharge = (get("texture_surcharge") as? Number)?.toDouble() ?: 0.0,
                inscriptionSurcharge = (get("inscription_surcharge") as? Number)?.toDouble() ?: 0.0,
                elementSurcharges = (get("element_surcharges") as? Map<String, Any?>)
                    ?.mapValues { (it.value as Number).toDouble() }
                    ?: emptyMap(),
                sizeMultiplier = (get("size_multiplier") as? Number)?.toDouble() ?: 1.0,
                sizeMultiplierLabel = get("size_multiplier_label") as? String ?: "",
                quantity = (get("quantity") as? Number)?.toInt() ?: 1,
                subtotal = (get("subtotal") as Number).toDouble(),
                total = (get("total") as Number).toDouble()
            ),
            status = OrderStatus.valueOf(get("status") as String),
            createdAt = (get("created_at") as Number).toLong(),
            adaptedModelId = get("adapted_model_id") as? String ?: "",
        )
    }
}