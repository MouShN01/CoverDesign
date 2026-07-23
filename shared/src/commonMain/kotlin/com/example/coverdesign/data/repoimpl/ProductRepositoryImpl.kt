package com.example.coverdesign.data.repoimpl

// shared/commonMain/data/repoimpl/ProductRepositoryImpl.kt
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.example.coverdesign.data.dto.toDomain
import com.example.coverdesign.data.dto.toDto
import com.example.coverdesign.data.local.toDomain
import com.example.coverdesign.data.local.toEntity
import com.example.coverdesign.data.remote.service.ProductApiService
import com.example.coverdesign.db.AppDatabase
import com.example.coverdesign.domain.model.Product
import com.example.coverdesign.domain.model.ProductConfig
import com.example.coverdesign.domain.model.pricing.PriceEstimate
import com.example.coverdesign.domain.repo.ProductRepository
import com.example.coverdesign.domain.util.Resource
import com.example.coverdesign.domain.util.currentEpochMillis
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProductRepositoryImpl(
    private val api: ProductApiService,
    private val db: AppDatabase
) : ProductRepository {

    override suspend fun getCatalog(): Resource<List<Product>> =
        withContext(Dispatchers.Default) {
            try {
                // ── 1. try local DB first ─────────────────────────────────────────
                val cached = db.catalogQueries
                    .selectAllProducts()
                    .executeAsList()
                    .map { it.toDomain() }

                if (cached.isNotEmpty()) {
                    // return cache immediately, then refresh in background
                    backgroundRefreshCatalog()
                    return@withContext Resource.Success(cached)
                }

                // ── 2. nothing cached — try network ───────────────────────────────
                fetchCatalogFromNetwork()

            } catch (e: Exception) {
                // fall back to cache if network fails
                val cached = db.catalogQueries.selectAllProducts()
                    .executeAsList()
                    .map { it.toDomain() }

                if (cached.isNotEmpty()) Resource.Success(cached)
                else Resource.Error("Failed to load catalog: ${e.message}", e)
            }
        }

    private fun backgroundRefreshCatalog() {
        CoroutineScope(Dispatchers.Default).launch {
            fetchCatalogFromNetwork()
        }
    }

    private suspend fun fetchCatalogFromNetwork(): Resource<List<Product>> {
        println("fetchCatalogFromNetwork ENTERED")
        return try {
            val products = api.getCatalog().map { it.toDomain() }

            println("fetchCatalogFromNetwork products ${products.size}")
            println("fetchCatalogFromNetwork products by name ${products.forEach { it.name }}")

            db.transaction {
                db.catalogQueries.clearCatalog()
                products.forEach {
                    db.catalogQueries.insertOrReplaceProduct(
                        it.toEntity(cachedAt = currentEpochMillis())
                    )
                }
            }
            Resource.Success(products)
        } catch (e: Exception) {
            println("fetchCatalogFromNetwork erorr ${e.message} \n\r ${e.cause}")
            Resource.Error("Network unavailable: ${e.message}", e)
        }
    }

    /* OLD*/
    override suspend fun getProduct(id: String): Resource<Product> =
        withContext(Dispatchers.Default) {
            try {
                Resource.Success(api.getProduct(id).toDomain())
            } catch (e: Exception) {
                val cached = db.catalogQueries.selectProductById(id)
                    .executeAsOneOrNull()
                    ?.toDomain()

                if (cached != null) Resource.Success(cached)
                else Resource.Error("Product not found: ${e.message}", e)
            }
        }

    override suspend fun saveConfig(config: ProductConfig): Unit = //TODO remove Unit
        withContext(Dispatchers.IO) {
            db.productConfigQueries.insertOrReplaceConfig(config.toEntity())
        }

    override fun getSavedConfig(): Flow<ProductConfig?> =
        db.productConfigQueries.selectCurrentConfig()
            .asFlow()
            .mapToOneOrNull(Dispatchers.Default)
            .map { it?.toDomain() }

    override suspend fun calculatePrice(config: ProductConfig): Resource<PriceEstimate> =
        withContext(Dispatchers.Default) {
            try {
                Resource.Success(api.calculatePrice(config.toDto()).toDomain())
            } catch (e: Exception) {
                Resource.Error("Price calculation failed: ${e.message}", e)
            }
        }

    // ProductRepositoryImpl.kt
    override suspend fun getConfigsForProduct(productId: String): List<ProductConfig> =
        withContext(Dispatchers.Default) {
            db.productConfigQueries.selectConfigsByProductId(productId)
                .executeAsList()
                .map { it.toDomain() }
        }

    override suspend fun getConfigById(configId: String): ProductConfig? =
        withContext(Dispatchers.Default) {
            db.productConfigQueries.selectConfigById(configId)
                .executeAsOneOrNull()
                ?.toDomain()
        }
}