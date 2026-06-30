package com.example.coverdesign.data.local

// shared/commonMain/data/local/LocalMappers.kt
import com.example.coverdesign.db.AdaptedModel3DEntity
import com.example.coverdesign.db.Model3DEntity
import com.example.coverdesign.db.OrderEntity
import com.example.coverdesign.db.ProductConfigEntity
import com.example.coverdesign.db.ProductEntity
import com.example.coverdesign.domain.model.AdaptedModel3D
import com.example.coverdesign.domain.model.Material
import com.example.coverdesign.domain.model.Measurement
import com.example.coverdesign.domain.model.Model3D
import com.example.coverdesign.domain.model.ModelStatus
import com.example.coverdesign.domain.model.Order
import com.example.coverdesign.domain.model.OrderStatus
import com.example.coverdesign.domain.model.Product
import com.example.coverdesign.domain.model.ProductCategory
import com.example.coverdesign.domain.model.ProductColor
import com.example.coverdesign.domain.model.ProductConfig
import com.example.coverdesign.domain.model.SizeLabel
import com.example.coverdesign.domain.model.Texture
import com.example.coverdesign.domain.model.pricing.PriceEstimate
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

private val json = Json { ignoreUnknownKeys = true }

fun ProductConfigEntity.toDomain(): ProductConfig = ProductConfig(
    id = id,
    productId = productId,
    productCategory = ProductCategory.valueOf(productType),
    measurement = Measurement(
        height = heightMm,
        width = widthMm,
        depth = depthMm,
        isPreciseMode = isPreciseMode == 1L,
        customSizeMm = customSizeMm
    ),
    sizeLabel = sizeLabel?.let { SizeLabel.valueOf(it) },
    color = ProductColor(name = colorName, hex = colorHex),
    material = Material.valueOf(material),
    texture = Texture(
        id = textureId.orEmpty(),
        name = textureName.orEmpty(),
        previewUrl = texturePreviewUrl.orEmpty(),
        description = textureDescription.orEmpty()
    ),
    inscription = inscription,
    userPhotoUri = userPhotoUri,
    additionalElements = json.decodeFromString(additionalElementsJson),
    quantity = quantity.toInt(),
    additionalNotes = additionalNotes,
    createdAt = createdAt,
)

fun ProductConfig.toEntity(): ProductConfigEntity = ProductConfigEntity(
    id = id,
    productId = productId,
    productType = productCategory.name,
    heightMm = measurement.height,
    widthMm = measurement.width,
    depthMm = measurement.depth,
    isPreciseMode = if (measurement.isPreciseMode) 1L else 0L,
    sizeLabel = sizeLabel?.name,
    customSizeMm = measurement.customSizeMm,
    colorName = color.name,
    colorHex = color.hex,
    material = material.name,
    textureId = texture.id,
    textureName = texture.name,
    texturePreviewUrl = texture.previewUrl.orEmpty(),
    textureDescription = texture.description.orEmpty(),
    inscription = inscription,
    userPhotoUri = userPhotoUri, // stored locally only, never synced
    additionalElementsJson = json.encodeToString(additionalElements),
    quantity = quantity.toLong(),
    additionalNotes = additionalNotes,
    createdAt = createdAt
)

// ─────────────────────────────────────────────────────────────────────────────
// OrderEntity  →  Order (domain)
// ─────────────────────────────────────────────────────────────────────────────

fun OrderEntity.toDomain(): Order = Order(
    id = id,
    config = json.decodeFromString<ProductConfig>(configJson),
    priceEstimate = PriceEstimate(
        configId = configId,
        currency = currency,
        basePrice = basePrice,
        materialSurcharges = json.decodeFromString(mapSerializer, materialSurchargesJson),
        textureSurcharge = textureSurcharge,
        inscriptionSurcharge = inscriptionSurcharge,
        elementSurcharges = json.decodeFromString(mapSerializer, elementSurchargesJson),
        sizeMultiplier = sizeMultiplier,
        sizeMultiplierLabel = sizeMultiplierLabel,
        quantity = quantity.toInt(),
        subtotal = subtotal,
        total = totalPrice
    ),
    status = OrderStatus.valueOf(status),
    createdAt = createdAt
)

// ─────────────────────────────────────────────────────────────────────────────
// Order (domain)  →  OrderEntity
// ─────────────────────────────────────────────────────────────────────────────
private val mapSerializer = MapSerializer(String.serializer(), Double.serializer())

fun Order.toEntity(): OrderEntity = OrderEntity(
    id = id,
    configId = config.id,
    configJson = json.encodeToString(ProductConfig.serializer(), config),
    currency = priceEstimate.currency,
    basePrice = priceEstimate.basePrice,
    materialSurchargesJson = json.encodeToString(mapSerializer, priceEstimate.materialSurcharges),
    textureSurcharge = priceEstimate.textureSurcharge,
    inscriptionSurcharge = priceEstimate.inscriptionSurcharge,
    elementSurchargesJson = json.encodeToString(mapSerializer, priceEstimate.elementSurcharges),
    sizeMultiplier = priceEstimate.sizeMultiplier,
    sizeMultiplierLabel = priceEstimate.sizeMultiplierLabel,
    quantity = priceEstimate.quantity.toLong(),
    subtotal = priceEstimate.subtotal,
    totalPrice = priceEstimate.total,
    status = status.name,
    createdAt = createdAt
)

// ── Product (catalog cache) ───────────────────────────────────────────────
fun ProductEntity.toDomain(): Product = Product(
    id = id,
    name = name,
    description = description,
    basePrice = basePrice,
    category = ProductCategory.valueOf(category),
    availableMaterials = json.decodeFromString<List<String>>(materialsJson)
        .map { Material.valueOf(it) },
    availableTextures = json.decodeFromString(texturesJson),
    imageUrl = imageUrl
)

fun Product.toEntity(cachedAt: Long): ProductEntity = ProductEntity(
    id = id,
    name = name,
    description = description,
    basePrice = basePrice,
    category = category.name,
    materialsJson = json.encodeToString(availableMaterials.map { it.name }),
    texturesJson = json.encodeToString(availableTextures),
    imageUrl = imageUrl,
    cachedAt = cachedAt
)

fun Model3DEntity.toDomain() = Model3D(
    configId = id,
    modelUrl = modelUrl,
    previewImageUrl = previewImageUrl,
    status = ModelStatus.valueOf(status),
    productId = productId,
    generatedAt = generatedAt,
)

fun Model3D.toEntity(productId: String) = Model3DEntity(
    id = configId,
    productId = productId,
    modelUrl = modelUrl,
    previewImageUrl = previewImageUrl ?: "3D/${modelUrl}",
    status = status.name,
    generatedAt = generatedAt,
)

fun AdaptedModel3DEntity.toDomain() = AdaptedModel3D(
    id = id, configId = configId,
    baseModelId = baseModelId,
    modelUrl = modelUrl, previewImageUrl = previewImageUrl,
    status = ModelStatus.valueOf(status), generatedAt = generatedAt,
    estimatedWaitSeconds = estimatedWaitSeconds?.toInt(),
    error = error,
    progress = progress,
)

fun AdaptedModel3D.toEntity() = AdaptedModel3DEntity(
    id = id,
    configId = configId,
    baseModelId = baseModelId,
    modelUrl = modelUrl,
    previewImageUrl = previewImageUrl,
    status = status.name,
    generatedAt = generatedAt,
    estimatedWaitSeconds = estimatedWaitSeconds?.toLong(),
    error = error,
    progress = progress
)