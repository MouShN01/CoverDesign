package com.example.coverdesign.ui.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.coverdesign.domain.model.AdditionalElement
import com.example.coverdesign.domain.model.Material
import com.example.coverdesign.domain.model.Measurement
import com.example.coverdesign.domain.model.PriceEstimate
import com.example.coverdesign.domain.model.Product
import com.example.coverdesign.domain.model.ProductColor
import com.example.coverdesign.domain.model.ProductConfig
import com.example.coverdesign.domain.model.SizeLabel
import com.example.coverdesign.domain.model.Texture
import com.example.coverdesign.domain.repo.ProductRepository
import com.example.coverdesign.domain.usecase.CalculatePriceUseCase
import com.example.coverdesign.domain.usecase.GenerateModelUseCase
import com.example.coverdesign.domain.util.Resource
import com.example.coverdesign.domain.util.currentEpochMillis
import com.example.coverdesign.domain.util.generateUuid
import com.example.coverdesign.domain.util.onError
import com.example.coverdesign.domain.util.onSuccess
import com.example.coverdesign.domain.validator.MeasurementValidator
import com.example.coverdesign.ui.viewmodel.base.BaseViewModel
import com.example.coverdesign.ui.viewmodel.base.UiEvent
import com.example.coverdesign.ui.viewmodel.base.UiState
import kotlinx.coroutines.launch

class ProductConfigViewModel(
    private val productRepo    : ProductRepository,
    private val calculatePrice : CalculatePriceUseCase,
    private val generateModel  : GenerateModelUseCase,
    private val validator      : MeasurementValidator
) : BaseViewModel<ProductConfigUiState, ProductConfigUiEvent>() {

    override fun initialState() = ProductConfigUiState()

    fun onAction(action: ProductConfigAction) {
        when (action) {
            is ProductConfigAction.Enter               -> onEnter(action.mode)
            is ProductConfigAction.ResumePreviousConfig -> resumeConfig(action.config)
            is ProductConfigAction.StartFreshConfig     -> startFresh(action.product)
            is ProductConfigAction.UpdateMeasurement    -> onMeasurementUpdated(action.measurement)
            is ProductConfigAction.UpdateMaterial       -> onFieldUpdated { copy(material = action.material) }
            is ProductConfigAction.UpdateColor          -> onFieldUpdated { copy(color = action.color) }
            is ProductConfigAction.UpdateTexture        -> onFieldUpdated { copy(texture = action.texture) }
            is ProductConfigAction.UpdateInscription    -> onInscriptionUpdated(action.text)
            is ProductConfigAction.UpdateSizeLabel      -> onFieldUpdated { copy(sizeLabel = action.label) }
            is ProductConfigAction.UpdateQuantity       -> onQuantityUpdated(action.qty)
            is ProductConfigAction.AddElement           -> onElementToggled(action.element, add = true)
            is ProductConfigAction.RemoveElement        -> onElementToggled(action.element, add = false)
            is ProductConfigAction.SetUserPhoto         -> onFieldUpdated { copy(userPhotoUri = action.uri) }
            is ProductConfigAction.RequestPriceUpdate   -> onPriceRequested()
            is ProductConfigAction.SaveAndProceed       -> onSaveAndProceed()
            else -> {}
        }
    }

    // ── single entry point, branches by how the screen was reached ────────────
    private fun onEnter(mode: ConfigEntryMode) {
        viewModelScope.launch {
            setState { copy(isLoading = true, error = null) }

            when (mode) {
                // Path 3: came from Order/History — config is final, just load it, no prompt
                is ConfigEntryMode.FromExistingConfig -> {
                    val config = productRepo.getConfigById(mode.configId)
                    if (config != null) {
                        val product = productRepo.getProduct(config.productId)
                        setState {
                            copy(
                                currentConfig   = config,
                                selectedProduct = (product as? Resource.Success)?.data,
                                isLoading       = false
                            )
                        }
                    } else {
                        setState { copy(isLoading = false, error = "Configuration not found") }
                    }
                }

                // Path 1 & 2: came from Catalog — check for a prior draft on this product
                is ConfigEntryMode.FromProduct -> {
                    productRepo.getProduct(mode.productId)
                        .onSuccess { product ->
                            val previousConfigs = productRepo.getConfigsForProduct(product.id)
                            val lastConfig = previousConfigs.firstOrNull()  // most recent, query is DESC

                            setState { copy(isLoading = false, selectedProduct = product) }

                            if (lastConfig != null) {
                                // FR3: ask the user, don't silently pick one
                                setState { copy(pendingResumePrompt = lastConfig) }
                            } else {
                                startFresh(product)
                            }
                        }
                        .onError { message, _ ->
                            setState { copy(isLoading = false, error = message) }
                        }
                }
            }
        }
    }

    // ── user answered the FR3 prompt: "yes, continue previous" ────────────────
    private fun resumeConfig(config: ProductConfig) {
        setState { copy(currentConfig = config, pendingResumePrompt = null) }
    }

    // ── user answered: "no, start fresh" ───────────────────────────────────────
    private fun startFresh(product: Product) {
        val draft = ProductConfig(
            id                 = generateUuid(),
            productId          = product.id,
            productCategory    = product.category,
            measurement        = Measurement(0.0, 0.0, 0.0),
            sizeLabel          = null,
            color              = ProductColor("", "#000000"),
            material           = product.availableMaterials.firstOrNull() ?: Material.PLA,
            texture            = product.availableTextures.first(),
            inscription        = "",
            userPhotoUri       = null,
            additionalElements = emptyList(),
            quantity           = 1,
            createdAt          = currentEpochMillis()
        )
        setState { copy(currentConfig = draft, pendingResumePrompt = null) }
    }

    private fun onMeasurementUpdated(measurement: Measurement) {
        onFieldUpdated { copy(measurement = measurement) }

        // validate for DISPLAY purposes only — doesn't gate the update
        val error = validator.validate(measurement)
        setState { copy(measurementError = error) }
//
//        val error = validator.validate(measurement)
//        setState { copy(measurementError = error) }
//        if (error == null) onFieldUpdated { copy(measurement = measurement) }
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
        val config = uiState.value.currentConfig ?: return
        viewModelScope.launch {
            setState { copy(isPriceLoading = true) }
            calculatePrice(config)
                .onSuccess { price -> setState { copy(priceEstimate = price, isPriceLoading = false) } }
                .onError { message, _ -> setState { copy(priceError = message, isPriceLoading = false) } }
        }
    }

    private fun onSaveAndProceed() {
        val config = uiState.value.currentConfig ?: return
        viewModelScope.launch {
            setState { copy(isSaving = true, error = null) }
            productRepo.saveConfig(config)
            generateModel(config)
                .onSuccess {
                    setState { copy(isSaving = false) }
                    sendEvent(ProductConfigUiEvent.NavigateToPreview(config.id))
                }
                .onError { message, _ ->
                    setState { copy(isSaving = false, error = message) }
                }
        }
    }
}

// ── State ─────────────────────────────────────────────────────────────────────
data class ProductConfigUiState(
    val selectedProduct   : Product?       = null,
    val currentConfig     : ProductConfig? = null,
    val priceEstimate     : PriceEstimate? = null,
    val isLoading         : Boolean        = false,
    val isSaving          : Boolean        = false,
    val isPriceLoading    : Boolean        = false,
    val measurementError  : String?        = null,
    val inscriptionError  : String?        = null,
    val quantityError     : String?        = null,
    val priceError        : String?        = null,
    val error             : String?        = null,
    val pendingResumePrompt : ProductConfig?  = null,
) : UiState

// ── Events ────────────────────────────────────────────────────────────────────
sealed class ProductConfigUiEvent : UiEvent {
    data class NavigateToPreview(val configId: String) : ProductConfigUiEvent()
}

// ── Actions ───────────────────────────────────────────────────────────────────
sealed class ProductConfigAction {
    data class Init(val productId: String) : ProductConfigAction()
    data class SetProduct(val product: Product)              : ProductConfigAction()
    data class UpdateMeasurement(val measurement: Measurement) : ProductConfigAction()
    data class UpdateMaterial(val material: Material)        : ProductConfigAction()
    data class UpdateColor(val color: ProductColor)          : ProductConfigAction()
    data class UpdateTexture(val texture: Texture)          : ProductConfigAction()
    data class UpdateInscription(val text: String)           : ProductConfigAction()
    data class UpdateSizeLabel(val label: SizeLabel?)        : ProductConfigAction()
    data class UpdateQuantity(val qty: Int)                  : ProductConfigAction()
    data class AddElement(val element: AdditionalElement)    : ProductConfigAction()
    data class RemoveElement(val element: AdditionalElement) : ProductConfigAction()
    data class SetUserPhoto(val uri: String?)                : ProductConfigAction()
    data class Enter(val mode: ConfigEntryMode)                 : ProductConfigAction()
    data class ResumePreviousConfig(val config: ProductConfig)  : ProductConfigAction()
    data class StartFreshConfig(val product: Product)           : ProductConfigAction()
    object SaveAndProceed                                    : ProductConfigAction()
    object RequestPriceUpdate                                : ProductConfigAction()
}
// shared/commonMain/ui/viewmodel/ProductConfigViewModel.kt
sealed class ConfigEntryMode {
    data class FromProduct(val productId: String) : ConfigEntryMode()
    data class FromExistingConfig(val configId: String) : ConfigEntryMode()
}
