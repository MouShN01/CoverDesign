package com.example.coverdesign.data.dto

import com.example.coverdesign.domain.model.Material
import com.example.coverdesign.domain.model.Model3D
import com.example.coverdesign.domain.model.ModelStatus
import com.example.coverdesign.domain.model.PriceEstimate
import com.example.coverdesign.domain.model.Product
import com.example.coverdesign.domain.model.ProductCategory
import com.example.coverdesign.domain.model.ProductConfig
import com.example.coverdesign.domain.model.Texture
import com.example.coverdesign.domain.model.Token
import com.example.coverdesign.domain.model.TryOnResult

// data/dto/Mappers.kt
fun ProductDto.toDomain() = Product(
    id = id,
    name = name,
    description = description,
    basePrice = basePrice,
    category = ProductCategory.valueOf(category.uppercase()),
    availableMaterials = materials.map { Material.valueOf(it.uppercase()) },
    availableTextures = textures.map { it.toDomain() },
    imageUrl = imageUrl
)

fun TextureDto.toDomain() = Texture(
    id = id,
    name = name,
    previewUrl = previewUrl,
    description = description
)

fun PriceEstimateDto.toDomain() = PriceEstimate(
    configId = configId,
    basePrice = basePrice,
    materialSurcharge = materialSurcharge,
    textureSurcharge = textureSurcharge,
    total = total,
    currency = currency
)

fun Model3DDto.toDomain() = Model3D(
    id = id,
    configId = configId,
    modelUrl = modelUrl,
    status = ModelStatus.valueOf(status.uppercase()),
    generatedAt = generatedAt
)

fun TokenDto.toDomain() = Token(
    accessToken = accessToken,
    refreshToken = refreshToken,
    expiresAt = expiresAt
)

// Domain → DTO (for sending to backend)
fun ProductConfig.toDto() = ProductConfigDto(
    id                 = id,
    productId          = productId,
    heightMm           = measurement.height,
    widthMm            = measurement.width,
    depthMm            = measurement.depth,
    material           = material.name.lowercase(),
    textureId          = texture?.id,
    textureDescription = textureDescription,
    color              = color,
    notes              = additionalNotes,
    createdAt          = createdAt
)