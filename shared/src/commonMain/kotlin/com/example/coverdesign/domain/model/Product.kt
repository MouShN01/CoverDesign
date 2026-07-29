package com.example.coverdesign.domain.model

sealed class Product {
    abstract val id: String
    abstract val name: String
    abstract val description: String
    abstract val basePrice: Double
    abstract val category: OrderCategory
    abstract val availableMaterials: List<Material>
    abstract val availableTextures: List<Texture>
    abstract val imageUrl: String

    data class Generic(
        override val id: String,
        override val name: String,
        override val description: String,
        override val basePrice: Double,
        override val category: OrderCategory,
        override val availableMaterials: List<Material>,
        override val availableTextures: List<Texture>,
        override val imageUrl: String
    ) : Product()
}
