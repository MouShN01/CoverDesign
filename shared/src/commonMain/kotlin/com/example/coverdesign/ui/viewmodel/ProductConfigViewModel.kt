package com.example.coverdesign.ui.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.coverdesign.data.dto.toDto
import com.example.coverdesign.domain.model.AdditionalElement
import com.example.coverdesign.domain.model.Material
import com.example.coverdesign.domain.model.Measurement
import com.example.coverdesign.domain.model.OrderCategory
import com.example.coverdesign.domain.model.Product
import com.example.coverdesign.domain.model.ProductColor
import com.example.coverdesign.domain.model.ProductConfig
import com.example.coverdesign.domain.model.ProstheticCommonMeasurements
import com.example.coverdesign.domain.model.ProstheticType
import com.example.coverdesign.domain.model.SizeLabel
import com.example.coverdesign.domain.model.SuspensionType
import com.example.coverdesign.domain.model.Texture
import com.example.coverdesign.domain.model.pricing.PriceEstimate
import com.example.coverdesign.domain.model.pricing.PricingEngine
import com.example.coverdesign.domain.model.pricing.toPriceEstimate
import com.example.coverdesign.domain.repo.OrderRepository
import com.example.coverdesign.domain.repo.ProductRepository
import com.example.coverdesign.domain.repo.UploadState
import com.example.coverdesign.domain.usecase.GenerateModelUseCase
import com.example.coverdesign.domain.usecase.SaveInsoleOrderLocallyUseCase
import com.example.coverdesign.domain.usecase.SaveOrderLocallyUseCase
import com.example.coverdesign.domain.usecase.SubmitProstheticOrderUseCase
import com.example.coverdesign.domain.usecase.UploadReferencePhotoUseCase
import com.example.coverdesign.domain.usecase.UploadTextureImageUseCase
import com.example.coverdesign.domain.util.Resource
import com.example.coverdesign.domain.util.currentEpochMillis
import com.example.coverdesign.domain.util.generateUuid
import com.example.coverdesign.domain.util.onError
import com.example.coverdesign.domain.util.onSuccess
import com.example.coverdesign.domain.validator.MeasurementValidator
import com.example.coverdesign.ui.mapper.toDomainConfig
import com.example.coverdesign.ui.mapper.toInsoleOrderConfig
import com.example.coverdesign.ui.viewmodel.base.BaseViewModel
import com.example.coverdesign.ui.viewmodel.base.UiEvent
import com.example.coverdesign.ui.viewmodel.base.UiState
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.launch

// ── Direct-order support types ────────────────────────────────────────────────

enum class OrderType { INSOLE, PROSTHETIC_COVER }

enum class LimbLevel(val displayName: String) {
    ABOVE_KNEE("Above Knee"), BELOW_KNEE("Below Knee")
}

enum class LegSide(val displayName: String) {
    LEFT("Left"), RIGHT("Right")
}

data class InsoleFootConfig(
    val euSize: Int = 0, val lengthMm: Float = 0f, val widthMm: Float = 0f, val side: LegSide
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
    val suspensionType: SuspensionType = SuspensionType.STRAP,
    val material: Material = Material.CARBON_FIBER,
    val color: ProductColor = ProductColor("Black", "#000000"),
    val texture: Texture = Texture("", "Smooth", null, "Smooth matte surface"),
    val quantity: Int = 1,
    val additionalNotes: String = ""
)

// ── ViewModel ─────────────────────────────────────────────────────────────────

class ProductConfigViewModel(
    private val productRepo: ProductRepository,
    private val uploadTextureImage: UploadTextureImageUseCase,
    private val uploadUserImage: UploadReferencePhotoUseCase,
    private val generateModel: GenerateModelUseCase,
    private val submitProstheticOrder: SubmitProstheticOrderUseCase,
    private val validator: MeasurementValidator,
    private val saveOrderLocally: SaveOrderLocallyUseCase,
    private val saveInsoleOrderLocally: SaveInsoleOrderLocallyUseCase,
    private val orderRepo: OrderRepository
) : BaseViewModel<ProductConfigUiState, ProductConfigUiEvent>() {

    override fun initialState() = ProductConfigUiState()

    @OptIn(ExperimentalStdlibApi::class)
    fun onAction(action: ProductConfigAction) {
        when (action) {
            // ── existing flow ──────────────────────────────────────────────
            is ProductConfigAction.Enter -> onEnter(action.mode)
            is ProductConfigAction.ResumePreviousConfig -> resumeProductConfig(action.config)
            is ProductConfigAction.StartFreshConfig -> startFreshWithProduct(action.product)
            is ProductConfigAction.UpdateMeasurement -> onMeasurementUpdated(
                action.measurement, action.category
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
                        secondFoot = InsoleFootConfig(side = oppositeSide), quantity = 2
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
                        limbLevel = action.level, prostheticType = null
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
                copy(prostheticConfig = prostheticConfig.copy(suspensionType = action.type))
            }

            is ProductConfigAction.UpdateProstheticAdditionalNotes -> setState {
                copy(prostheticConfig = prostheticConfig.copy(additionalNotes = action.text))
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
                    val config =
                        productRepo.getConfigById(mode.configId) // TODO make it clean architecture use UseCase
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
                            startFreshWithProduct(product)
                        }
                    }.onError { message, _ ->
                        setState { copy(isLoading = false, error = message) }
                    }
                }
            }
        }
    }

    private fun resumeProductConfig(config: ProductConfig) {
        setState { copy(currentConfig = config, pendingResumePrompt = null) }
    }

    private fun startFreshWithProduct(product: Product) { // Not use in the prosthetic cover app
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

        val estimate = when {
            uiState.value.orderType == OrderType.INSOLE -> {
                val base = 1.0
                PricingEngine.calculateForInsole(
                    uiState.value.insoleConfig.toInsoleOrderConfig(orderId = "_"), base
                )
            }

            uiState.value.orderType == OrderType.PROSTHETIC_COVER -> {
                val base = 1.0
                uiState.value.prostheticConfig.toDomainConfig().toPriceEstimate(base)
            }

            uiState.value.currentConfig != null -> {
                val base = uiState.value.selectedProduct?.basePrice ?: 1.0
                PricingEngine.calculate(
                    uiState.value.currentConfig!!, base
                )
            }

            else -> return
        }
        setState { copy(priceEstimate = estimate, isPriceLoading = false) }
    }


    private fun onSubmitInsole() {
        viewModelScope.launch {
            setState { copy(isSaving = true, error = null) }
            val orderId = generateUuid() // TODO think orderId
            val basePrice =
                uiState.value.selectedProduct?.basePrice ?: 1.0 // TODO set prices from server
            val domainConfig = uiState.value.insoleConfig.toInsoleOrderConfig(orderId)
            val saved = saveInsoleOrderLocally(domainConfig, uiState.value.priceEstimate, basePrice)
            if (saved is Resource.Error) {
                setState { copy(isSaving = false, error = saved.message) }
                return@launch
            }
            setState { copy(isSaving = false, isLockedAfterSave = true) }
            sendEvent(ProductConfigUiEvent.NavigateToOrderSummary(orderId))
        }
    }

    private fun onSubmitProsthetic() {
        val pc = uiState.value.prostheticConfig
        pc.prostheticType ?: return

        val domainConfig = pc.toDomainConfig()

        viewModelScope.launch {
            setState { copy(isSaving = true, isLockedAfterSave = true, error = null) }
            submitProstheticOrder(domainConfig).onSuccess { taskId ->
                    setState { copy(isSaving = false, uploadProgress = null) }
                    sendEvent(ProductConfigUiEvent.NavigateToPreview(taskId, domainConfig.orderId))
                }.onError { message, _ ->
                    setState { copy(isSaving = false, isLockedAfterSave = false, error = message) }
                }
        }
    }

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
                updatedConfig, uiState.value.priceEstimate
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
    data class UpdateFasteningType(val type: SuspensionType) : ProductConfigAction()
    data class UpdateProstheticAdditionalNotes(val text: String) : ProductConfigAction()
    object SubmitProstheticOrder : ProductConfigAction()
}

// ── Entry modes ───────────────────────────────────────────────────────────────

sealed class ConfigEntryMode {
    data class DirectNew(val preselect: OrderType? = null) : ConfigEntryMode()
    data class FromProduct(val productId: String) : ConfigEntryMode()
    data class FromExistingConfig(val configId: String) : ConfigEntryMode()
}