package com.example.coverdesign.domain.repo

import com.example.coverdesign.domain.model.PriceEstimate
import com.example.coverdesign.domain.model.Product
import com.example.coverdesign.domain.model.ProductConfig
import com.example.coverdesign.domain.util.Resource
import kotlinx.coroutines.flow.Flow

// shared/commonMain/domain/repo/ProductRepository.kt
interface ProductRepository {
    suspend fun getCatalog(): Resource<List<Product>>
    suspend fun getProduct(id: String): Resource<Product>
    suspend fun saveConfig(config: ProductConfig)
    fun getSavedConfig(): Flow<ProductConfig?>
    suspend fun calculatePrice(config: ProductConfig): Resource<PriceEstimate>
}