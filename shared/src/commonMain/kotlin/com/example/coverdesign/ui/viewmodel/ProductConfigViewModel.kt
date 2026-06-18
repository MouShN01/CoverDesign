package com.example.coverdesign.ui.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.coverdesign.domain.model.AdditionalElement
import com.example.coverdesign.domain.model.Material
import com.example.coverdesign.domain.model.Measurement
import com.example.coverdesign.domain.model.PriceEstimate
import com.example.coverdesign.domain.model.Product
import com.example.coverdesign.domain.model.ProductColor
import com.example.coverdesign.domain.model.ProductConfig
import com.example.coverdesign.domain.model.ProductType
import com.example.coverdesign.domain.model.SizeLabel
import com.example.coverdesign.domain.model.Texture
import com.example.coverdesign.domain.repo.ProductRepository
import com.example.coverdesign.domain.usecase.CalculatePriceUseCase
import com.example.coverdesign.domain.usecase.ConfigureProductUseCase
import com.example.coverdesign.domain.usecase.UpdateConfigUseCase
import com.example.coverdesign.domain.util.currentEpochMillis
import com.example.coverdesign.domain.util.generateUuid
import com.example.coverdesign.domain.util.onError
import com.example.coverdesign.domain.util.onSuccess
import com.example.coverdesign.domain.validator.MeasurementValidator
import com.example.coverdesign.ui.viewmodel.base.BaseViewModel
import com.example.coverdesign.ui.viewmodel.base.UiEvent
import com.example.coverdesign.ui.viewmodel.base.UiState
import kotlinx.coroutines.launch

// shared/commonMain/ui/viewmodel/ProductConfigViewModel.kt
class ProductConfigViewModel(
    private val configureProduct : ConfigureProductUseCase,
    private val updateConfig     : UpdateConfigUseCase,
    private val calculatePrice   : CalculatePriceUseCase,
    private val getSavedConfig   : ProductRepository       // for observing draft
) : BaseViewModel<ProductConfigUiState, ProductConfigUiEvent>() {

    override fun initialState() = ProductConfigUiState()

    init {
        observeSavedConfig()
    }

    fun onAction(action: ProductConfigAction) {
        when (action) {
            is ProductConfigAction.SetProduct         -> onProductSet(action.product)
            is ProductConfigAction.UpdateMeasurement  -> onMeasurementUpdated(action.measurement)
            is ProductConfigAction.UpdateMaterial     -> onFieldUpdated { copy(material = action.material) }
            is ProductConfigAction.UpdateColor        -> onFieldUpdated { copy(color = action.color) }
            is ProductConfigAction.UpdateTexture      -> onFieldUpdated { copy(texture = action.texture) }
            is ProductConfigAction.UpdateInscription  -> onInscriptionUpdated(action.text)
            is ProductConfigAction.UpdateSizeLabel    -> onFieldUpdated { copy(sizeLabel = action.label) }
            is ProductConfigAction.UpdateQuantity     -> onQuantityUpdated(action.qty)
            is ProductConfigAction.AddElement         -> onElementToggled(action.element, add = true)
            is ProductConfigAction.RemoveElement      -> onElementToggled(action.element, add = false)
            is ProductConfigAction.SetUserPhoto       -> onFieldUpdated { copy(userPhotoUri = action.uri) }
            is ProductConfigAction.SaveAndProceed     -> onSaveAndProceed()
            is ProductConfigAction.RequestPriceUpdate -> onPriceRequested()
        }
    }

    private fun observeSavedConfig() {
        viewModelScope.launch {
            getSavedConfig.getSavedConfig().collect { config ->
                config?.let { setState { copy(currentConfig = it) } }
            }
        }
    }

    private fun onProductSet(product: Product) {
        val config = ProductConfig(
            id = generateUuid(),
            productId = product.id,
            productType = ProductType.valueOf(product.category.name),
            measurement = Measurement(0.0, 0.0, 0.0),
            sizeLabel = null,
            color = ProductColor("", "#000000"),
            material = product.availableMaterials.first(),
            texture = product.availableTextures.first(),
            inscription = "",
            userPhotoUri = null,
            additionalElements = emptyList(),
            quantity = 1,
            createdAt = currentEpochMillis()
        )
        setState { copy(currentConfig = config, selectedProduct = product) }
    }

    private fun onMeasurementUpdated(measurement: Measurement) {
        val validationError = uiState.value.currentConfig?.let {
            MeasurementValidator().validate(measurement)
        }
        setState { copy(measurementError = validationError) }
        if (validationError == null) onFieldUpdated { copy(measurement = measurement) }
    }

    private fun onInscriptionUpdated(text: String) {
        val error = MeasurementValidator().validateInscription(text)
        setState { copy(inscriptionError = error) }
        if (error == null) onFieldUpdated { copy(inscription = text) }
    }

    private fun onQuantityUpdated(qty: Int) {
        val error = MeasurementValidator().validateQuantity(qty)
        setState { copy(quantityError = error) }
        if (error == null) onFieldUpdated { copy(quantity = qty) }
    }

    private fun onElementToggled(element: AdditionalElement, add: Boolean) {
        onFieldUpdated {
            val updated = if (add)
                additionalElements + element
            else
                additionalElements - element
            copy(additionalElements = updated)
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
                .onSuccess { price ->
                    setState { copy(priceEstimate = price, isPriceLoading = false) }
                }
                .onError { message, _ ->
                    setState { copy(priceError = message, isPriceLoading = false) }
                }
        }
    }

    private fun onSaveAndProceed() {
        val config = uiState.value.currentConfig ?: return
        viewModelScope.launch {
            setState { copy(isSaving = true) }
            configureProduct(config)
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
    val error             : String?        = null
) : UiState

// ── Events ────────────────────────────────────────────────────────────────────
sealed class ProductConfigUiEvent : UiEvent {
    data class NavigateToPreview(val configId: String) : ProductConfigUiEvent()
}

// ── Actions ───────────────────────────────────────────────────────────────────
sealed class ProductConfigAction {
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
    object SaveAndProceed                                    : ProductConfigAction()
    object RequestPriceUpdate                                : ProductConfigAction()
}