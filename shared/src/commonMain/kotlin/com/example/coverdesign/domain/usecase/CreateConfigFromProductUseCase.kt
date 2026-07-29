package com.example.coverdesign.domain.usecase

import com.example.coverdesign.domain.model.Material
import com.example.coverdesign.domain.model.Measurement
import com.example.coverdesign.domain.model.Product
import com.example.coverdesign.domain.model.ProductColor
import com.example.coverdesign.domain.model.ProductConfig
import com.example.coverdesign.domain.repo.ProductRepository
import com.example.coverdesign.domain.util.Resource
import com.example.coverdesign.domain.util.currentEpochMillis
import com.example.coverdesign.domain.util.generateUuid

// shared/commonMain/domain/usecase/CreateConfigFromProductUseCase.kt   FR1 → FR2 handoff
class CreateConfigFromProductUseCase(
    private val productRepo: ProductRepository
) {
    suspend operator fun invoke(product: Product): Resource<ProductConfig> {
        val config = ProductConfig(
            id = generateUuid(),
            productId = product.id,
            orderCategory = product.category,
            measurement = Measurement(0.0, 0.0, 0.0),
            sizeLabel = null,
            color = ProductColor("", "#000000"),
            material = product.availableMaterials.firstOrNull() ?: Material.PLA,
            texture = product.availableTextures.first(),
            inscription = "Custom inscription",
            userPhotoUri = null,
            additionalElements = emptyList(),
            quantity = 1,
            createdAt = currentEpochMillis()
        )
        productRepo.saveConfig(config)
        return Resource.Success(config)
    }
}