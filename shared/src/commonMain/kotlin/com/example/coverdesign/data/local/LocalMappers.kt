package com.example.coverdesign.data.local

// shared/commonMain/data/local/LocalMappers.kt
import com.example.coverdesign.db.Model3DEntity
import com.example.coverdesign.db.OrderEntity
import com.example.coverdesign.db.ProductConfigEntity
import com.example.coverdesign.db.ProductEntity
import com.example.coverdesign.domain.model.Material
import com.example.coverdesign.domain.model.Measurement
import com.example.coverdesign.domain.model.Model3D
import com.example.coverdesign.domain.model.ModelStatus
import com.example.coverdesign.domain.model.Order
import com.example.coverdesign.domain.model.OrderStatus
import com.example.coverdesign.domain.model.PriceEstimate
import com.example.coverdesign.domain.model.Product
import com.example.coverdesign.domain.model.ProductCategory
import com.example.coverdesign.domain.model.ProductColor
import com.example.coverdesign.domain.model.ProductConfig
import com.example.coverdesign.domain.model.SizeLabel
import com.example.coverdesign.domain.model.Texture
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
// ── Order ───────────────────────────────────────────────────────────────────
fun OrderEntity.toDomain(): Order = Order(
    id = id,
    config = json.decodeFromString<ProductConfig>(configJson),
    priceEstimate = PriceEstimate(
        configId = configId,
        basePrice = basePrice,
        materialSurcharge = materialSurcharge,
        textureSurcharge = textureSurcharge,
        total = totalPrice,
        currency = currency
    ),
    status = OrderStatus.valueOf(status),
    createdAt = createdAt
)

fun Order.toEntity(): OrderEntity = OrderEntity(
    id = id,
    configId = config.id,
    configJson = json.encodeToString(config),
    basePrice = priceEstimate.basePrice,
    materialSurcharge = priceEstimate.materialSurcharge,
    textureSurcharge = priceEstimate.textureSurcharge,
    totalPrice = priceEstimate.total,
    currency = priceEstimate.currency,
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
    id = id,
    configId = configId ?: "",
    modelUrl = modelUrl,
    previewImageUrl = previewImageUrl,
    status = ModelStatus.valueOf(status),
    generatedAt = generatedAt,
    estimatedWaitSeconds = estimatedWaitSeconds?.toInt()
)

fun Model3D.toEntity(productId: String) = Model3DEntity(
    id                   = id,
    productId            = productId,
    configId             = configId.ifEmpty { null },
    modelUrl             = modelUrl,
    previewImageUrl      = previewImageUrl ?: "3D/${modelUrl}",
    status               = status.name,
    generatedAt          = generatedAt,
    estimatedWaitSeconds = estimatedWaitSeconds?.toLong()
)