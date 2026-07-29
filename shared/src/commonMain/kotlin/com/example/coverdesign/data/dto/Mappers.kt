package com.example.coverdesign.data.dto

import com.example.coverdesign.domain.model.AdaptedModel3D
import com.example.coverdesign.domain.model.AdditionalElement
import com.example.coverdesign.domain.model.ButtonPlacement
import com.example.coverdesign.domain.model.FasteningType
import com.example.coverdesign.domain.model.InsoleFootConfig
import com.example.coverdesign.domain.model.InsoleOrderConfig
import com.example.coverdesign.domain.model.LegSide
import com.example.coverdesign.domain.model.LimbLevel
import com.example.coverdesign.domain.model.Material
import com.example.coverdesign.domain.model.Measurement
import com.example.coverdesign.domain.model.ModelStatus
import com.example.coverdesign.domain.model.Order
import com.example.coverdesign.domain.model.OrderCategory
import com.example.coverdesign.domain.model.OrderStatus
import com.example.coverdesign.domain.model.Product
import com.example.coverdesign.domain.model.ProductColor
import com.example.coverdesign.domain.model.ProductConfig
import com.example.coverdesign.domain.model.ProstheticCommonMeasurements
import com.example.coverdesign.domain.model.ProstheticCoverOrderConfig
import com.example.coverdesign.domain.model.ProstheticTypeEnum
import com.example.coverdesign.domain.model.SizeLabel
import com.example.coverdesign.domain.model.Texture
import com.example.coverdesign.domain.model.Token
import com.example.coverdesign.domain.model.pricing.PriceEstimate

// ═══════════════════════════════════════════════════════════════════
// DTO → Domain  (incoming from backend)
// ═══════════════════════════════════════════════════════════════════

fun ProductDto.toDomain() = Product.Generic(
    id = id,
    name = name,
    description = description,
    basePrice = basePrice,
    category = OrderCategory.valueOf(category.uppercase()),
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
    orderCategory = OrderCategory.valueOf(productCategory.uppercase()),
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
    textureSurcharge = textureSurcharge!!,
    inscriptionSurcharge = inscriptionSurcharge!!,
    elementSurcharges = elementSurcharges!!,
    sizeMultiplier = sizeMultiplier,
    sizeMultiplierLabel = sizeMultiplierLabel!!,
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
    productCategory = orderCategory.name.lowercase(),
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

// ═══════════════════════════════════════════════════════════════════
// InsoleOrderConfig  ↔  InsoleOrderConfigDto
// ═══════════════════════════════════════════════════════════════════

fun InsoleFootConfigDto.toDomain() = InsoleFootConfig(
    euSize = euSize,
    lengthMm = lengthMm,
    widthMm = widthMm,
    side = LegSide.valueOf(side.uppercase())
)

fun InsoleFootConfig.toDto() = InsoleFootConfigDto(
    euSize = euSize,
    lengthMm = lengthMm,
    widthMm = widthMm,
    side = side.name.lowercase()
)

fun InsoleOrderConfigDto.toDomain() = InsoleOrderConfig(
    orderId = orderId,
    category = OrderCategory.valueOf(category.uppercase()),
    quantity = quantity,
    priceEstimate = priceEstimate.toDomain(),
    rightFoot = rightFoot.toDomain(),
    leftFoot = leftFoot?.toDomain(),
    material = Material.valueOf(material.uppercase()),
    color = ProductColor(name = colorName, hex = colorHex)
)

fun InsoleOrderConfig.toDto() = InsoleOrderConfigDto(
    orderId = orderId,
    category = category.name.lowercase(),
    quantity = quantity,
    priceEstimate = priceEstimate.toDto(),
    rightFoot = rightFoot.toDto(),
    leftFoot = leftFoot?.toDto(),
    material = material.name.lowercase(),
    colorName = color.name,
    colorHex = color.hex
)

// ═══════════════════════════════════════════════════════════════════
// ProstheticCoverOrderConfig  ↔  ProstheticCoverOrderConfigDto
// ═══════════════════════════════════════════════════════════════════

fun ProstheticCoverOrderConfigDto.toDomain() = ProstheticCoverOrderConfig(
    orderId = orderId,
    category = OrderCategory.valueOf(category.uppercase()),
    quantity = quantity,
    priceEstimate = priceEstimate.toDomain(),
    limbSide = LegSide.valueOf(limbSide.uppercase()),
    limbLevel = LimbLevel.valueOf(limbLevel.uppercase()),
    prostheticType = prostheticType?.let { runCatching { ProstheticTypeEnum.valueOf(it) }.getOrNull() },
    commonMeasurements = ProstheticCommonMeasurements(
        circumferenceMm = circumferenceMm,
        kneeApexToFootshellTopMm = kneeApexToFootshellTopMm
    ),
    typeSpecificMeasurementMm = typeSpecificMeasurementMm,
    secondTypeSpecificMeasurementMm = secondTypeSpecificMeasurementMm,
    buttonPlacement = buttonPlacement?.let { runCatching { ButtonPlacement.valueOf(it.uppercase()) }.getOrNull() },
    fasteningType = runCatching { FasteningType.valueOf(fasteningType.uppercase()) }.getOrDefault(FasteningType.STRAP),
    color = ProductColor(name = colorName, hex = colorHex),
    material = Material.valueOf(material.uppercase()),
    texture = Texture(
        id = textureId,
        name = textureName,
        previewUrl = texturePreviewUrl,
        description = textureDescription
    ),
    inscription = inscription,
    additionalElements = additionalElements.map { AdditionalElement.valueOf(it.uppercase()) },
    additionalNotes = additionalNotes,
    createdAt = createdAt
)

fun ProstheticCoverOrderConfig.toDto() = ProstheticCoverOrderConfigDto(
    orderId = orderId,
    category = category.name.lowercase(),
    quantity = quantity,
    priceEstimate = priceEstimate.toDto(),
    limbSide = limbSide.name.lowercase(),
    limbLevel = limbLevel.name.lowercase(),
    prostheticType = prostheticType?.name,
    circumferenceMm = commonMeasurements.circumferenceMm,
    kneeApexToFootshellTopMm = commonMeasurements.kneeApexToFootshellTopMm,
    typeSpecificMeasurementMm = typeSpecificMeasurementMm,
    secondTypeSpecificMeasurementMm = secondTypeSpecificMeasurementMm,
    buttonPlacement = buttonPlacement?.name?.lowercase(),
    fasteningType = fasteningType.name.lowercase(),
    colorName = color.name,
    colorHex = color.hex,
    material = material.name.lowercase(),
    textureId = texture.id,
    textureName = texture.name,
    texturePreviewUrl = texture.previewUrl,
    textureDescription = texture.description,
    inscription = inscription,
    additionalElements = additionalElements.map { it.name.lowercase() },
    additionalNotes = additionalNotes,
    createdAt = createdAt
)