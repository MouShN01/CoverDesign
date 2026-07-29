package com.example.coverdesign.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ProductConfig(
    val id: String,
    val productId: String,
    val orderCategory: OrderCategory,        // Тип товару
    val measurement: Measurement,        // Розмір (numeric)
    val sizeLabel: SizeLabel?,           // Розмір (enum S/M/L)
    val color: ProductColor,             // Колір
    val material: Material,              // Матеріал
    val texture: Texture,               // Текстура
    val inscription: String,             // Напис (max 50 chars)
    val userPhotoUri: String?,           // Фото користувача ( local-only URI)
    val additionalElements: List<AdditionalElement>,  // Додаткові елементи
    val quantity: Int,                     // Кількість (min 1)
    val additionalNotes: String = "",
    //val textureImageUri: String? = null,
    val createdAt: Long
)

//enum class ProductType(val displayName: String) {
//    COVER("Cosmetics"),
//    ACCESSORY("Accessory"),
//    CLOTHING_ELEMENT("Clothing"),
//    UPPER_LIMB("Верхня кінцівка"),
//    LOWER_LIMB("Нижня кінцівка"),
//    COSMETIC_COVER("Косметичний чохол"),
//    CUSTOM("Personalised option")
//}

enum class SizeLabel { XS, S, M, L, XL, XXL }

@Serializable
data class ProductColor(
    val name: String,       // "чорний", "білий"
    val hex: String         // "#000000"
)

enum class AdditionalElement(val displayName: String) {
    LOGO("Logo"),
    DECOR("Decor"),
    FASTENER("Fastener"),
    ENGRAVING("Engraving"),
    MOUNTING("Mounting"),
    PADDING("Padding"),
    STRAP("Strap"),
}