package com.example.coverdesign.data.dto

import com.example.coverdesign.domain.model.AdaptedModel3D
import com.example.coverdesign.domain.model.AdditionalElement
import com.example.coverdesign.domain.model.Material
import com.example.coverdesign.domain.model.Measurement
import com.example.coverdesign.domain.model.ModelStatus
import com.example.coverdesign.domain.model.Order
import com.example.coverdesign.domain.model.OrderStatus
import com.example.coverdesign.domain.model.Product
import com.example.coverdesign.domain.model.ProductCategory
import com.example.coverdesign.domain.model.ProductColor
import com.example.coverdesign.domain.model.ProductConfig
import com.example.coverdesign.domain.model.SizeLabel
import com.example.coverdesign.domain.model.Texture
import com.example.coverdesign.domain.model.Token
import com.example.coverdesign.domain.model.pricing.PriceEstimate

// ═══════════════════════════════════════════════════════════════════
// DTO → Domain  (incoming from backend)
// ═══════════════════════════════════════════════════════════════════

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

fun ProductConfigDto.toDomain() = ProductConfig(
    id = id,
    productId = productId,
    productCategory = ProductCategory.valueOf(productCategory.uppercase()),
    measurement = Measurement(
        height = heightMm,
        width = widthMm,
        depth = depthMm,
        isPreciseMode = false,      // not stored on backend, device-only
        customSizeMm = customSizeMm
    ),
    sizeLabel = sizeLabel?.let { SizeLabel.valueOf(it.uppercase()) },
    color = ProductColor(name = colorName, hex = colorHex),
    material = Material.valueOf(material.uppercase()),
    texture = Texture(
        id = textureId,
        name = textureName,
        previewUrl = texturePreviewUrl,
        description = textureDescription.orEmpty()
    ),
    inscription = inscription,
    userPhotoUri = null,  // never comes from backend — local device only
    additionalElements = additionalElements
        .map { AdditionalElement.valueOf(it.uppercase()) },
    quantity = quantity,
    additionalNotes = additionalNotes,
    createdAt = createdAt,
)


// ─────────────────────────────────────────────────────────────────────────────
// PriceEstimateDto  ↔  PriceEstimate (domain)
// ─────────────────────────────────────────────────────────────────────────────

fun PriceEstimateDto.toDomain() = PriceEstimate(
    configId = configId,
    currency = currency,
    basePrice = basePrice,
    materialSurcharges = materialSurcharges,
    textureSurcharge = textureSurcharge,
    inscriptionSurcharge = inscriptionSurcharge,
    elementSurcharges = elementSurcharges,
    sizeMultiplier = sizeMultiplier,
    sizeMultiplierLabel = sizeMultiplierLabel,
    quantity = quantity,
    subtotal = subtotal,
    total = total
)

fun PriceEstimate.toDto() = PriceEstimateDto(
    configId = configId,
    currency = currency,
    basePrice = basePrice,
    materialSurcharges = materialSurcharges,
    textureSurcharge = textureSurcharge,
    inscriptionSurcharge = inscriptionSurcharge,
    elementSurcharges = elementSurcharges,
    sizeMultiplier = sizeMultiplier,
    sizeMultiplierLabel = sizeMultiplierLabel,
    quantity = quantity,
    subtotal = subtotal,
    total = total
)

fun TokenDto.toDomain() = Token(
    accessToken = accessToken,
    refreshToken = refreshToken,
    //expiresAt    = expiresAt
)

fun OrderDto.toDomain() = Order(
    id = id,
    config = config.toDomain(),
    priceEstimate = price.toDomain(),
    status = OrderStatus.valueOf(status.uppercase()),
    createdAt = createdAt,
    adaptedModelId = adaptedModelId
)

fun Order.toDto() = Order(
    id = id,
    config = config,
    priceEstimate = priceEstimate,
    status = status,
    createdAt = createdAt,
    adaptedModelId = adaptedModelId
)

// ═══════════════════════════════════════════════════════════════════
// Domain → DTO  (outgoing to backend)
// ═══════════════════════════════════════════════════════════════════

fun ProductConfig.toDto() = ProductConfigDto(
    id = id,
    productId = productId,
    productCategory = productCategory.name.lowercase(),
    heightMm = measurement.height,
    widthMm = measurement.width,
    depthMm = measurement.depth,
    customSizeMm = measurement.customSizeMm,
    sizeLabel = sizeLabel?.name?.lowercase(),
    colorName = color.name,
    colorHex = color.hex,
    material = material.name.lowercase(),
    textureId = texture.id,
    textureName = texture.name,
    texturePreviewUrl = texture.previewUrl,
    textureDescription = texture.description ?: "None",
    inscription = inscription,
    // userPhotoUri intentionally excluded — never sent to backend
    additionalElements = additionalElements.map { it.name.lowercase() },
    quantity = quantity,
    createdAt = createdAt,
    additionalNotes = additionalNotes,
)

fun AdaptedModel3DDto.toDomain() = AdaptedModel3D(
    id = taskId,
    configId = configId,
    baseModelId = baseModelId,
    modelUrl = modelUrl.toString(),
    previewImageUrl = previewImageUrl.toString(),
    status = ModelStatus.valueOf(status.uppercase()),
    generatedAt = generatedAt,
    estimatedWaitSeconds = estimatedWaitSeconds,
    progress = progress.toLong(),
    error = error
)