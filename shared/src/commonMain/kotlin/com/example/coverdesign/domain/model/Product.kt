package com.example.coverdesign.domain.model

// shared/commonMain/domain/model/Product.kt
data class Product(
    val id: String,
    val name: String,
    val description: String,
    val basePrice: Double,
    val category: ProductCategory,
    val availableMaterials: List<Material>,
    val availableTextures: List<Texture>,
    val imageUrl: String
)

enum class ProductCategory {
    COSMETIC_COVER
}
