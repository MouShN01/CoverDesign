package com.example.coverdesign.data.remote.service

// data/remote/service/ProductApiService.kt
import com.example.coverdesign.data.dto.PriceEstimateDto
import com.example.coverdesign.data.dto.ProductConfigDto
import com.example.coverdesign.data.dto.ProductDto
import com.example.coverdesign.data.remote.client.ApiConstants
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*

class ProductApiService(private val client: HttpClient) {

    suspend fun getCatalog(): List<ProductDto> =
        client.get(ApiConstants.Endpoints.CATALOG).body()

    suspend fun getProduct(id: String): ProductDto =
        client.get(ApiConstants.Endpoints.PRODUCT.replace("{id}", id)).body()

    suspend fun calculatePrice(config: ProductConfigDto): PriceEstimateDto =
        client.post(ApiConstants.Endpoints.PRICE) {
            setBody(config)
        }.body()
}