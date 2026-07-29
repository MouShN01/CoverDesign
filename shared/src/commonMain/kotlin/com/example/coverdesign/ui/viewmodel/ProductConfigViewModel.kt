package com.example.coverdesign.ui.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.coverdesign.data.dto.toDto
import com.example.coverdesign.domain.model.AdditionalElement
import com.example.coverdesign.domain.model.ButtonPlacement
import com.example.coverdesign.domain.model.FasteningType
import com.example.coverdesign.domain.model.Material
import com.example.coverdesign.domain.model.Measurement
import com.example.coverdesign.domain.model.OrderCategory
import com.example.coverdesign.domain.model.Product
import com.example.coverdesign.domain.model.ProductColor
import com.example.coverdesign.domain.model.ProductConfig
import com.example.coverdesign.domain.model.ProstheticCommonMeasurements
import com.example.coverdesign.domain.model.ProstheticType
import com.example.coverdesign.domain.model.ProstheticTypeEnum
import com.example.coverdesign.domain.model.SizeLabel
import com.example.coverdesign.domain.model.Texture
import com.example.coverdesign.domain.model.pricing.PriceEstimate
import com.example.coverdesign.domain.model.pricing.PricingEngine
import com.example.coverdesign.domain.repo.OrderRepository
import com.example.coverdesign.domain.repo.ProductRepository
import com.example.coverdesign.domain.repo.UploadState
import com.example.coverdesign.domain.usecase.GenerateModelUseCase
import com.example.coverdesign.domain.usecase.SaveOrderLocallyUseCase
import com.example.coverdesign.domain.usecase.UploadReferencePhotoUseCase
import com.example.coverdesign.domain.usecase.UploadTextureImageUseCase
import com.example.coverdesign.domain.util.Resource
import com.example.coverdesign.domain.util.currentEpochMillis
import com.example.coverdesign.domain.util.generateUuid
import com.example.coverdesign.domain.util.onError
import com.example.coverdesign.domain.util.onSuccess
import com.example.coverdesign.domain.validator.MeasurementValidator
import com.example.coverdesign.ui.viewmodel.base.BaseViewModel
import com.example.coverdesign.ui.viewmodel.base.UiEvent
import com.example.coverdesign.ui.viewmodel.base.UiState
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.launch
import com.example.coverdesign.domain.model.InsoleFootConfig as DomainInsoleFootConfig
import com.example.coverdesign.domain.model.InsoleOrderConfig as DomainInsoleOrderConfig
import com.example.coverdesign.domain.model.LegSide as DomainLegSide
import com.example.coverdesign.domain.model.LimbLevel as DomainLimbLevel
import com.example.coverdesign.domain.model.ProstheticCoverOrderConfig as DomainProstheticCoverOrderConfig

// ── Direct-order support types ────────────────────────────────────────────────

enum class OrderType { INSOLE, PROSTHETIC_COVER }

enum class LimbLevel(val displayName: String) {
    ABOVE_KNEE("Above Knee"),
    BELOW_KNEE("Below Knee")
}

enum class LegSide(val displayName: String) {
    LEFT("Left"),
    RIGHT("Right")
}

data class InsoleFootConfig(
    val euSize: Int = 0,
    val lengthMm: Float = 0f,
    val widthMm: Float = 0f,
    val side: LegSide
)

data class InsoleOrderUiState(
    val primaryFoot: InsoleFootConfig = InsoleFootConfig(side = LegSide.LEFT),
    val secondFoot: InsoleFootConfig? = null,
    val material: Material = Material.SILICONE,
    val color: ProductColor = ProductColor("Black", "#000000"),
    val quantity: Int = 1
)

data class ProstheticOrderUiState(
    val limbSide: LegSide = LegSide.LEFT,
    val limbLevel: LimbLevel? = null,
    val prostheticType: ProstheticType? = null,
    val commonMeasurements: ProstheticCommonMeasurements = ProstheticCommonMeasurements(),
    val fasteningType: FasteningType = FasteningType.STRAP,
    val material: Material = Material.CARBON_FIBER,
    val color: ProductColor = ProductColor("Black", "#000000"),
    val texture: Texture = Texture("", "Smooth", null, "Smooth matte surface"),
    val quantity: Int = 1
)

// ── ViewModel ─────────────────────────────────────────────────────────────────

class ProductConfigViewModel(
    private val productRepo: ProductRepository,
    private val uploadTextureImage: UploadTextureImageUseCase,
    private val uploadUserImage: UploadReferencePhotoUseCase,
    private val generateModel: GenerateModelUseCase,
    private val validator: MeasurementValidator,
    private val saveOrderLocally: SaveOrderLocallyUseCase,
    private val orderRepo: OrderRepository
) : BaseViewModel<ProductConfigUiState, ProductConfigUiEvent>()
{

    override fun initialState() = ProductConfigUiState()

    @OptIn(ExperimentalStdlibApi::class)
    fun onAction(action: ProductConfigAction) {
        when (action) {
            // ── existing flow ──────────────────────────────────────────────
            is ProductConfigAction.Enter -> onEnter(action.mode)
            is ProductConfigAction.ResumePreviousConfig -> resumeConfig(action.config)
            is ProductConfigAction.StartFreshConfig -> startFresh(action.product)
            is ProductConfigAction.UpdateMeasurement -> onMeasurementUpdated(
                action.measurement,
                action.category
            )

            is ProductConfigAction.UpdateMaterial -> onFieldUpdated { copy(material = action.material) }
            is ProductConfigAction.UpdateColor -> onFieldUpdated { copy(color = action.color) }
            is ProductConfigAction.UpdateTexture -> onFieldUpdated { copy(texture = action.texture) }
            is ProductConfigAction.UpdateInscription -> onInscriptionUpdated(action.text)
            is ProductConfigAction.UpdateSizeLabel -> onFieldUpdated { copy(sizeLabel = action.label) }
            is ProductConfigAction.UpdateQuantity -> onQuantityUpdated(action.qty)
            is ProductConfigAction.AddElement -> onElementToggled(action.element, add = true)
            is ProductConfigAction.RemoveElement -> onElementToggled(action.element, add = false)
            is ProductConfigAction.UpdateUserPhoto -> onFieldUpdated { copy(userPhotoUri = action.imageURL) }
            is ProductConfigAction.RequestPriceUpdate -> onPriceRequested()
            is ProductConfigAction.SaveAndProceed -> onSaveAndProceed()
            is ProductConfigAction.ClearError -> setState { copy(error = null) }

            // ── direct ordering: order type ────────────────────────────────
            is ProductConfigAction.SelectOrderType -> setState { copy(orderType = action.type) }

            // ── insole actions ─────────────────────────────────────────────
            is ProductConfigAction.UpdateInsolePrimaryFoot -> setState {
                copy(insoleConfig = insoleConfig.copy(primaryFoot = action.foot))
            }

            is ProductConfigAction.UpdateInsoleSecondFoot -> setState {
                copy(insoleConfig = insoleConfig.copy(secondFoot = action.foot))
            }

            is ProductConfigAction.AddInsoleSecondFoot -> setState {
                val oppositeSide =
                    if (insoleConfig.primaryFoot.side == LegSide.LEFT) LegSide.RIGHT else LegSide.LEFT
                copy(
                    insoleConfig = insoleConfig.copy(
                        secondFoot = InsoleFootConfig(side = oppositeSide),
                        quantity = 2
                    )
                )
            }

            is ProductConfigAction.RemoveInsoleSecondFoot -> setState {
                copy(insoleConfig = insoleConfig.copy(secondFoot = null, quantity = 1))
            }

            is ProductConfigAction.UpdateInsoleMaterial -> setState {
                copy(
                    insoleConfig = insoleConfig.copy(
                        material = action.material
                    )
                )
            }

            is ProductConfigAction.UpdateInsoleColor -> setState {
                copy(
                    insoleConfig = insoleConfig.copy(
                        color = action.color
                    )
                )
            }

            is ProductConfigAction.UpdateInsoleQuantity -> setState {
                copy(
                    insoleConfig = insoleConfig.copy(
                        quantity = action.qty
                    )
                )
            }

            is ProductConfigAction.SubmitInsoleOrder -> onSubmitInsole()

            // ── prosthetic cover actions ───────────────────────────────────
            is ProductConfigAction.UpdateProstheticSide -> setState {
                copy(prostheticConfig = prostheticConfig.copy(limbSide = action.side))
            }

            is ProductConfigAction.SelectLimbLevel -> setState {
                copy(
                    prostheticConfig = prostheticConfig.copy(
                        limbLevel = action.level,
                        prostheticType = null
                    )
                )
            }

            is ProductConfigAction.SelectProstheticSubtype -> setState {
                copy(prostheticConfig = prostheticConfig.copy(prostheticType = action.type))
            }

            is ProductConfigAction.UpdateCircumference -> setState {
                copy(
                    prostheticConfig = prostheticConfig.copy(
                        commonMeasurements = prostheticConfig.commonMeasurements.copy(
                            circumferenceMm = action.mm
                        )
                    )
                )
            }

            is ProductConfigAction.UpdateKneeApexLength -> setState {
                copy(
                    prostheticConfig = prostheticConfig.copy(
                        commonMeasurements = prostheticConfig.commonMeasurements.copy(
                            kneeApexToFootshellTopMm = action.mm
                        )
                    )
                )
            }

            is ProductConfigAction.UpdateProstheticSubtypeMeasurement -> setState {
                copy(prostheticConfig = prostheticConfig.copy(prostheticType = action.type))
            }

            is ProductConfigAction.UpdateProstheticMaterial -> setState {
                copy(prostheticConfig = prostheticConfig.copy(material = action.material))
            }

            is ProductConfigAction.UpdateProstheticColor -> setState {
                copy(prostheticConfig = prostheticConfig.copy(color = action.color))
            }

            is ProductConfigAction.UpdateProstheticTexture -> setState {
                copy(prostheticConfig = prostheticConfig.copy(texture = action.texture))
            }

            is ProductConfigAction.UpdateProstheticQuantity -> setState {
                copy(prostheticConfig = prostheticConfig.copy(quantity = action.qty))
            }

            is ProductConfigAction.UpdateFasteningType -> setState {
                copy(prostheticConfig = prostheticConfig.copy(fasteningType = action.type))
            }

            is ProductConfigAction.SubmitProstheticOrder -> onSubmitProsthetic()

            else -> {}
        }
    }

    // ── entry point ───────────────────────────────────────────────────────────

    private fun onEnter(mode: ConfigEntryMode) {
        viewModelScope.launch {
            setState { copy(isLoading = true, error = null) }

            when (mode) {
                is ConfigEntryMode.DirectNew -> {
                    setState { copy(isLoading = false, orderType = mode.preselect) }
                }

                is ConfigEntryMode.FromExistingConfig -> {
                    val config = productRepo.getConfigById(mode.configId)
                    if (config != null) {
                        val product = productRepo.getProduct(config.productId)
                        setState {
                            copy(
                                currentConfig = config,
                                selectedProduct = (product as? Resource.Success)?.data,
                                isLoading = false
                            )
                        }
                    } else {
                        setState { copy(isLoading = false, error = "Configuration not found") }
                    }
                }

                is ConfigEntryMode.FromProduct -> {
                    productRepo.getProduct(mode.productId).onSuccess { product ->
                        val previousConfigs = productRepo.getConfigsForProduct(product.id)
                        val lastConfig = previousConfigs.firstOrNull()
                        setState { copy(isLoading = false, selectedProduct = product) }
                        if (lastConfig != null) {
                            setState { copy(pendingResumePrompt = lastConfig) }
                        } else {
                            startFresh(product)
                        }
                    }.onError { message, _ ->
                        setState { copy(isLoading = false, error = message) }
                    }
                }
            }
        }
    }

    private fun resumeConfig(config: ProductConfig) {
        setState { copy(currentConfig = config, pendingResumePrompt = null) }
    }

    private fun startFresh(product: Product) {
        val draft = ProductConfig(
            id = generateUuid(),
            productId = product.id,
            orderCategory = product.category,
            measurement = Measurement(0.0, 0.0, 0.0),
            sizeLabel = null,
            color = ProductColor("", "#000000"),
            material = product.availableMaterials.firstOrNull() ?: Material.PLA,
            texture = product.availableTextures.first(),
            inscription = "",
            userPhotoUri = null,
            additionalElements = emptyList(),
            quantity = 1,
            createdAt = currentEpochMillis()
        )
        setState { copy(currentConfig = draft, pendingResumePrompt = null) }
    }

    // ── existing form helpers ─────────────────────────────────────────────────

    private fun onMeasurementUpdated(measurement: Measurement, category: OrderCategory) {
        onFieldUpdated { copy(measurement = measurement) }
        val error = validator.validate(m = measurement, category = category)
        setState { copy(measurementError = error) }
    }

    private fun onInscriptionUpdated(text: String) {
        val error = validator.validateInscription(text)
        setState { copy(inscriptionError = error) }
        if (error == null) onFieldUpdated { copy(inscription = text) }
    }

    private fun onQuantityUpdated(qty: Int) {
        val error = validator.validateQuantity(qty)
        setState { copy(quantityError = error) }
        if (error == null) onFieldUpdated { copy(quantity = qty) }
    }

    private fun onElementToggled(element: AdditionalElement, add: Boolean) {
        onFieldUpdated {
            copy(additionalElements = if (add) additionalElements + element else additionalElements - element)
        }
    }

    private fun onFieldUpdated(reducer: ProductConfig.() -> ProductConfig) {
        val config = uiState.value.currentConfig ?: return
        setState { copy(currentConfig = config.reducer()) }
    }

    private fun onPriceRequested() {
        val base = uiState.value.selectedProduct?.basePrice ?: 0.0
        val estimate = when {
            uiState.value.orderType == OrderType.INSOLE ->
                PricingEngine.calculateForInsole(uiState.value.insoleConfig.toDomainConfig(), base)
            uiState.value.currentConfig != null ->
                PricingEngine.calculate(uiState.value.currentConfig!!, base)
            else -> return
        }
        setState { copy(priceEstimate = estimate, isPriceLoading = false) }
    }

    // ── insole submission ─────────────────────────────────────────────────────

    private fun onSubmitInsole() {
        viewModelScope.launch {
            setState { copy(isSaving = true, error = null) }
            val orderId = generateUuid()
            val domainConfig = uiState.value.insoleConfig.toDomainConfig(orderId)
            val saved = orderRepo.saveInsoleOrderLocally(domainConfig)
            if (saved is Resource.Error) {
                setState { copy(isSaving = false, error = saved.message) }
                return@launch
            }
            setState { copy(isLockedAfterSave = true) }
            orderRepo.submitInsoleOrder(domainConfig).onSuccess {
                setState { copy(isSaving = false) }
                sendEvent(ProductConfigUiEvent.NavigateToOrderSummary(orderId))
            }.onError { msg, _ ->
                setState { copy(isSaving = false, error = msg) }
            }
        }
    }

    // ── prosthetic submission (→ 3D model generation) ─────────────────────────

    private fun onSubmitProsthetic() {
        val pc = uiState.value.prostheticConfig
        val type = pc.prostheticType ?: return
        val orderId = generateUuid()
        val notes =
            "${pc.limbSide.displayName} | ${pc.limbLevel?.displayName ?: ""}: ${type.displayName}"

        val domainConfig = DomainProstheticCoverOrderConfig(
            orderId = orderId,
            category = OrderCategory.COSMETIC_COVER,
            quantity = pc.quantity,
            priceEstimate = PriceEstimate(
                configId = orderId, currency = "USD", basePrice = 0.0,
                materialSurcharges = emptyMap(), textureSurcharge = 0.0,
                inscriptionSurcharge = 0.0, elementSurcharges = emptyMap(),
                sizeMultiplier = 1.0, sizeMultiplierLabel = "",
                quantity = pc.quantity, subtotal = 0.0, total = 0.0
            ),
            limbSide = DomainLegSide.valueOf(pc.limbSide.name),
            limbLevel = DomainLimbLevel.valueOf(pc.limbLevel!!.name),
            prostheticType = type.toEnum(),
            commonMeasurements = pc.commonMeasurements,
            typeSpecificMeasurementMm = type.typeSpecificMeasurementMm(),
            secondTypeSpecificMeasurementMm = type.secondTypeSpecificMeasurementMm(),
            buttonPlacement = type.buttonPlacementOrNull(),
            fasteningType = pc.fasteningType,
            color = pc.color,
            material = pc.material,
            texture = pc.texture,
            additionalNotes = notes,
            createdAt = currentEpochMillis()
        )

        val generationConfig = ProductConfig(
            id = orderId,
            productId = "prosthetic_direct",
            orderCategory = OrderCategory.COSMETIC_COVER,
            measurement = Measurement(
                pc.commonMeasurements.kneeApexToFootshellTopMm.toDouble(),
                pc.commonMeasurements.circumferenceMm.toDouble(),
                0.0
            ),
            sizeLabel = null, color = pc.color, material = pc.material, texture = pc.texture,
            inscription = "", userPhotoUri = null, additionalElements = emptyList(),
            quantity = pc.quantity, additionalNotes = notes, createdAt = domainConfig.createdAt
        )

        viewModelScope.launch {
            setState { copy(isSaving = true, error = null) }
            val saved = orderRepo.saveProstheticCoverOrderLocally(domainConfig)
            if (saved is Resource.Error) {
                setState { copy(isSaving = false, error = saved.message) }
                return@launch
            }
            setState { copy(isLockedAfterSave = true) }
            generateModel(generationConfig.toDto()).onSuccess { taskId ->
                setState { copy(isSaving = false, uploadProgress = null) }
                sendEvent(ProductConfigUiEvent.NavigateToPreview(taskId, orderId))
            }.onError { message, _ ->
                setState { copy(isSaving = false, error = message) }
            }
        }
    }

    // ── ProstheticType → domain mapping helpers ───────────────────────────────

    private fun ProstheticType.toEnum(): ProstheticTypeEnum = when (this) {
        is ProstheticType.AboveKnee.PolycentricKnee -> ProstheticTypeEnum.ABOVE_KNEE_POLYCENTRIC_KNEE
        is ProstheticType.AboveKnee.TotalKnee -> ProstheticTypeEnum.ABOVE_KNEE_TOTAL_KNEE
        ProstheticType.AboveKnee.ThroughKnee -> ProstheticTypeEnum.ABOVE_KNEE_THROUGH_KNEE
        is ProstheticType.AboveKnee.HighProfile -> ProstheticTypeEnum.ABOVE_KNEE_HIGH_PROFILE
        is ProstheticType.AboveKnee.Blade -> ProstheticTypeEnum.ABOVE_KNEE_BLADE
        is ProstheticType.AboveKnee.Microprocessor -> ProstheticTypeEnum.ABOVE_KNEE_MICROPROCESSOR
        is ProstheticType.AboveKnee.HydraulicFoot -> ProstheticTypeEnum.ABOVE_KNEE_HYDRAULIC_FOOT
        is ProstheticType.BelowKnee.ReleaseButton -> ProstheticTypeEnum.BELOW_KNEE_RELEASE_BUTTON
        is ProstheticType.BelowKnee.ReleaseValve -> ProstheticTypeEnum.BELOW_KNEE_RELEASE_VALVE
        is ProstheticType.BelowKnee.PinlockButton -> ProstheticTypeEnum.BELOW_KNEE_PINLOCK_BUTTON
        is ProstheticType.BelowKnee.HighProfile -> ProstheticTypeEnum.BELOW_KNEE_HIGH_PROFILE
        is ProstheticType.BelowKnee.Blade -> ProstheticTypeEnum.BELOW_KNEE_BLADE
        is ProstheticType.BelowKnee.Microprocessor -> ProstheticTypeEnum.BELOW_KNEE_MICROPROCESSOR
        is ProstheticType.BelowKnee.HydraulicFoot -> ProstheticTypeEnum.BELOW_KNEE_HYDRAULIC_FOOT
        is ProstheticType.BelowKnee.Rotationplasty -> ProstheticTypeEnum.BELOW_KNEE_ROTATIONPLASTY
        ProstheticType.BelowKnee.SymesSocket -> ProstheticTypeEnum.BELOW_KNEE_SYMES_SOCKET
    }

    private fun ProstheticType.typeSpecificMeasurementMm(): Float = when (this) {
        is ProstheticType.AboveKnee.PolycentricKnee -> kneeUnitMm
        is ProstheticType.AboveKnee.TotalKnee -> kneeUnitMm
        is ProstheticType.AboveKnee.HighProfile -> footshellToFootUnitMm
        is ProstheticType.AboveKnee.Blade -> footshellToFootUnitMm
        is ProstheticType.AboveKnee.Microprocessor -> footshellToFootUnitMm
        is ProstheticType.AboveKnee.HydraulicFoot -> footshellToFootUnitMm
        is ProstheticType.BelowKnee.ReleaseButton -> footshellToButtonCenterMm
        is ProstheticType.BelowKnee.ReleaseValve -> footshellToButtonCenterMm
        is ProstheticType.BelowKnee.PinlockButton -> footshellToButtonCenterMm
        is ProstheticType.BelowKnee.HighProfile -> footshellToFootUnitMm
        is ProstheticType.BelowKnee.Blade -> footshellToFootUnitMm
        is ProstheticType.BelowKnee.Microprocessor -> footshellToFootUnitMm
        is ProstheticType.BelowKnee.HydraulicFoot -> footshellToFootUnitMm
        is ProstheticType.BelowKnee.Rotationplasty -> toeToFootshellMm
        else -> 0f
    }

    private fun ProstheticType.secondTypeSpecificMeasurementMm(): Float = when (this) {
        is ProstheticType.BelowKnee.Rotationplasty -> heelKneeToFootshellMm
        else -> 0f
    }

    private fun ProstheticType.buttonPlacementOrNull(): ButtonPlacement? = when (this) {
        is ProstheticType.BelowKnee.ReleaseButton -> buttonPlacement
        is ProstheticType.BelowKnee.ReleaseValve -> buttonPlacement
        is ProstheticType.BelowKnee.PinlockButton -> buttonPlacement
        else -> null
    }

    private fun InsoleOrderUiState.toDomainConfig(orderId: String = generateUuid()): DomainInsoleOrderConfig =
        DomainInsoleOrderConfig(
            orderId = orderId,
            category = OrderCategory.INSOLE,
            quantity = quantity,
            priceEstimate = PriceEstimate(
                configId = orderId, currency = "USD", basePrice = 0.0,
                materialSurcharges = emptyMap(), textureSurcharge = 0.0,
                inscriptionSurcharge = 0.0, elementSurcharges = emptyMap(),
                sizeMultiplier = 1.0, sizeMultiplierLabel = "",
                quantity = quantity, subtotal = 0.0, total = 0.0
            ),
            rightFoot = primaryFoot.toDomainFoot(),
            leftFoot = secondFoot?.toDomainFoot(),
            material = material,
            color = color
        )

    private fun InsoleFootConfig.toDomainFoot() = DomainInsoleFootConfig(
        euSize = euSize,
        lengthMm = lengthMm,
        widthMm = widthMm,
        side = DomainLegSide.valueOf(side.name)
    )

    // ── legacy save+generate flow ─────────────────────────────────────────────

    private fun onSaveAndProceed() {
        onSaveAndProceedWith(uiState.value.currentConfig ?: return)
    }

    private fun onSaveAndProceedWith(config: ProductConfig) {
        val userId = Firebase.auth.currentUser?.uid ?: run {
            setState { copy(error = "You must be signed in to save your configuration") }
            return
        }
        viewModelScope.launch {
            setState { copy(isSaving = true, currentConfig = config, error = null) }
            if (uploadTextureImage.needsUpload(config.texture.previewUrl)) {
                uploadTexture(config.texture.previewUrl!!, config.id, userId)
            }
            if (uploadUserImage.needsUpload(config.userPhotoUri)) {
                uploadImage(config.userPhotoUri!!, config.id, userId)
            }
            val updatedConfig = uiState.value.currentConfig ?: run {
                setState { copy(isSaving = false, error = "Config lost after upload") }
                return@launch
            }
            productRepo.saveConfig(updatedConfig)
            saveOrderLocally(
                updatedConfig, uiState.value.priceEstimate ?: PriceEstimate(
                    configId = updatedConfig.id, currency = "USD",
                    basePrice = 0.0, materialSurcharges = emptyMap(),
                    textureSurcharge = 0.0, inscriptionSurcharge = 0.0,
                    elementSurcharges = emptyMap(), sizeMultiplier = 1.0,
                    sizeMultiplierLabel = "", quantity = updatedConfig.quantity,
                    subtotal = 0.0, total = 0.0
                )
            ).onError { string, _ ->
                setState { copy(isSaving = false, error = string) }
            }
            generateModel(updatedConfig.toDto()).onSuccess { task ->
                setState { copy(isSaving = false, uploadProgress = null) }
                sendEvent(ProductConfigUiEvent.NavigateToPreview(task, updatedConfig.id))
            }.onError { message, _ ->
                setState { copy(isSaving = false, error = message) }
            }
        }
    }

    private suspend fun uploadTexture(localUri: String, configId: String, userId: String) {
        uploadTextureImage(localUri, configId, userId).collect { state ->
            when (state) {
                is UploadState.Progress -> setState { copy(uploadProgress = state.percent) }
                is UploadState.Success -> onFieldUpdated { copy(texture = texture.copy(previewUrl = state.downloadUrl)) }
                is UploadState.Error -> setState { copy(error = state.message) }
            }
        }
    }

    private suspend fun uploadImage(localUri: String, configId: String, userId: String) {
        uploadUserImage(localUri, configId, userId).collect { state ->
            when (state) {
                is UploadState.Progress -> setState { copy(uploadProgress = state.percent) }
                is UploadState.Success -> onFieldUpdated { copy(userPhotoUri = state.downloadUrl) }
                is UploadState.Error -> setState { copy(error = state.message) }
            }
        }
    }
}

// ── State ─────────────────────────────────────────────────────────────────────

data class ProductConfigUiState(
    // existing catalog/order-history flow
    val selectedProduct: Product? = null,
    val currentConfig: ProductConfig? = null,
    val priceEstimate: PriceEstimate? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isPriceLoading: Boolean = false,
    val measurementError: String? = null,
    val inscriptionError: String? = null,
    val quantityError: String? = null,
    val priceError: String? = null,
    val error: String? = null,
    val pendingResumePrompt: ProductConfig? = null,
    val uploadProgress: Int? = null,
    val isLockedAfterSave: Boolean = false,
    // direct ordering
    val orderType: OrderType? = null,
    val insoleConfig: InsoleOrderUiState = InsoleOrderUiState(),
    val prostheticConfig: ProstheticOrderUiState = ProstheticOrderUiState()
) : UiState

// ── Events ────────────────────────────────────────────────────────────────────

sealed class ProductConfigUiEvent : UiEvent {
    data class NavigateToPreview(val taskId: String, val configId: String) : ProductConfigUiEvent()
    data class NavigateToOrderSummary(val configId: String) : ProductConfigUiEvent()
}

// ── Actions ───────────────────────────────────────────────────────────────────

sealed class ProductConfigAction {
    // existing
    data class Init(val productId: String) : ProductConfigAction()
    data class SetProduct(val product: Product) : ProductConfigAction()
    data class UpdateMeasurement(val measurement: Measurement, val category: OrderCategory) :
        ProductConfigAction()

    data class UpdateMaterial(val material: Material) : ProductConfigAction()
    data class UpdateColor(val color: ProductColor) : ProductConfigAction()
    data class UpdateTexture(val texture: Texture) : ProductConfigAction()
    data class UpdateInscription(val text: String) : ProductConfigAction()
    data class UpdateSizeLabel(val label: SizeLabel?) : ProductConfigAction()
    data class UpdateQuantity(val qty: Int) : ProductConfigAction()
    data class AddElement(val element: AdditionalElement) : ProductConfigAction()
    data class RemoveElement(val element: AdditionalElement) : ProductConfigAction()
    data class Enter(val mode: ConfigEntryMode) : ProductConfigAction()
    data class ResumePreviousConfig(val config: ProductConfig) : ProductConfigAction()
    data class StartFreshConfig(val product: Product) : ProductConfigAction()
    data class UpdateUserPhoto(val imageURL: String?) : ProductConfigAction()
    object SaveAndProceed : ProductConfigAction()
    object RequestPriceUpdate : ProductConfigAction()
    object ClearError : ProductConfigAction()

    // direct order type selection
    data class SelectOrderType(val type: OrderType) : ProductConfigAction()

    // insole
    data class UpdateInsolePrimaryFoot(val foot: InsoleFootConfig) : ProductConfigAction()
    data class UpdateInsoleSecondFoot(val foot: InsoleFootConfig) : ProductConfigAction()
    object AddInsoleSecondFoot : ProductConfigAction()
    object RemoveInsoleSecondFoot : ProductConfigAction()
    data class UpdateInsoleMaterial(val material: Material) : ProductConfigAction()
    data class UpdateInsoleColor(val color: ProductColor) : ProductConfigAction()
    data class UpdateInsoleQuantity(val qty: Int) : ProductConfigAction()
    object SubmitInsoleOrder : ProductConfigAction()

    // prosthetic cover
    data class UpdateProstheticSide(val side: LegSide) : ProductConfigAction()
    data class SelectLimbLevel(val level: LimbLevel) : ProductConfigAction()
    data class SelectProstheticSubtype(val type: ProstheticType) : ProductConfigAction()
    data class UpdateCircumference(val mm: Float) : ProductConfigAction()
    data class UpdateKneeApexLength(val mm: Float) : ProductConfigAction()
    data class UpdateProstheticSubtypeMeasurement(val type: ProstheticType) : ProductConfigAction()
    data class UpdateProstheticMaterial(val material: Material) : ProductConfigAction()
    data class UpdateProstheticColor(val color: ProductColor) : ProductConfigAction()
    data class UpdateProstheticTexture(val texture: Texture) : ProductConfigAction()
    data class UpdateProstheticQuantity(val qty: Int) : ProductConfigAction()
    data class UpdateFasteningType(val type: FasteningType) : ProductConfigAction()
    object SubmitProstheticOrder : ProductConfigAction()
}

// ── Entry modes ───────────────────────────────────────────────────────────────

sealed class ConfigEntryMode {
    data class DirectNew(val preselect: OrderType? = null) : ConfigEntryMode()
    data class FromProduct(val productId: String) : ConfigEntryMode()
    data class FromExistingConfig(val configId: String) : ConfigEntryMode()
}