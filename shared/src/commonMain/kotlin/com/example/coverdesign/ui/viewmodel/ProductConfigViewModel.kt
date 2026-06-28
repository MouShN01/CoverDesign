package com.example.coverdesign.ui.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.coverdesign.domain.model.AdditionalElement
import com.example.coverdesign.domain.model.Material
import com.example.coverdesign.domain.model.Measurement
import com.example.coverdesign.domain.model.Product
import com.example.coverdesign.domain.model.ProductColor
import com.example.coverdesign.domain.model.ProductConfig
import com.example.coverdesign.domain.model.SizeLabel
import com.example.coverdesign.domain.model.Texture
import com.example.coverdesign.domain.model.pricing.PriceEstimate
import com.example.coverdesign.domain.model.pricing.PricingEngine
import com.example.coverdesign.domain.repo.ProductRepository
import com.example.coverdesign.domain.repo.UploadState
import com.example.coverdesign.domain.usecase.GenerateModelUseCase
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


class ProductConfigViewModel(
    private val productRepo: ProductRepository,
    private val uploadTextureImage: UploadTextureImageUseCase,
    private val uploadUserImage: UploadReferencePhotoUseCase,
    private val generateModel: GenerateModelUseCase,
    private val validator: MeasurementValidator
) : BaseViewModel<ProductConfigUiState, ProductConfigUiEvent>() {

    override fun initialState() = ProductConfigUiState()

    @OptIn(ExperimentalStdlibApi::class)
    fun onAction(action: ProductConfigAction) {
        when (action) {
            is ProductConfigAction.Enter -> onEnter(action.mode)
            is ProductConfigAction.ResumePreviousConfig -> resumeConfig(action.config)
            is ProductConfigAction.StartFreshConfig -> startFresh(action.product)
            is ProductConfigAction.UpdateMeasurement -> onMeasurementUpdated(action.measurement)
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

            else -> {}
//            is ProductConfigAction.Init -> TODO()
//            is ProductConfigAction.SetProduct -> TODO()
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
                                currentConfig = config,
                                selectedProduct = (product as? Resource.Success)?.data,
                                isLoading = false
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
                            val lastConfig =
                                previousConfigs.firstOrNull()  // most recent, query is DESC

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
            id = generateUuid(),
            productId = product.id,
            productCategory = product.category,
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
        val base = uiState.value.selectedProduct?.basePrice ?: 0.0
        val estimate = PricingEngine.calculate(uiState.value.currentConfig!!, base)
        setState { copy(priceEstimate = estimate, isPriceLoading = false) }
    }

    private suspend fun uploadTexture(localUri: String, configId: String, userId: String) {
        println("onSaveAndProceed Uploading texture")
        uploadTextureImage(localUri, configId, userId)
            .collect { state ->
                when (state) {
                    is UploadState.Progress -> setState { copy(uploadProgress = state.percent) }
                    is UploadState.Success -> {
                        onFieldUpdated {
                            println("onSaveAndProceed texture ${state.downloadUrl}")
                            copy(
                                texture = texture.copy(previewUrl = state.downloadUrl)
                            )
                        }
                    }

                    is UploadState.Error -> setState { copy(error = state.message) }
                }
            }

    }
      private suspend fun uploadImage(localUri: String, configId: String, userId: String) {
          println("onSaveAndProceed Uploading image")
        uploadUserImage(localUri, configId, userId)
            .collect { state ->
                when (state) {
                    is UploadState.Progress -> setState { copy(uploadProgress = state.percent) }
                    is UploadState.Success -> {
                        onFieldUpdated {
                            println("onSaveAndProceed image ${state.downloadUrl}")
                            copy(
                                userPhotoUri = state.downloadUrl
                            )
                        }
                    }

                    is UploadState.Error -> setState { copy(error = state.message) }
                }
            }

    }


    private fun onSaveAndProceed() {
        val userId = Firebase.auth.currentUser?.uid ?: run {
            setState { copy(error = "You must be signed in to save your configuration") }
            return
        }

        val config = uiState.value.currentConfig ?: return
        val previewUrl = config.texture.previewUrl ?: return

        viewModelScope.launch {
            if (uploadTextureImage.needsUpload(config.texture.previewUrl)) {
                uploadTexture(previewUrl, config.id, userId)
            }

            if (uploadUserImage.needsUpload(config.userPhotoUri)) {
                uploadImage(config.userPhotoUri!!, config.id, userId)
            }

            val updatedConfig = uiState.value.currentConfig ?: run {
                setState { copy(isSaving = false, error = "Config lost after upload") }
                return@launch
            }

            productRepo.saveConfig(updatedConfig)
            println("onSaveAndProceed Config was saved")
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
) : UiState

// ── Events ────────────────────────────────────────────────────────────────────
sealed class ProductConfigUiEvent : UiEvent {
    data class NavigateToPreview(val configId: String) : ProductConfigUiEvent()
}

// ── Actions ───────────────────────────────────────────────────────────────────
sealed class ProductConfigAction {
    data class Init(val productId: String) : ProductConfigAction()
    data class SetProduct(val product: Product) : ProductConfigAction()
    data class UpdateMeasurement(val measurement: Measurement) : ProductConfigAction()
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
    //data class UpdateTextureImage(val uri: String?) : ProductConfigAction()

    data class UpdateUserPhoto(val imageURL: String?) : ProductConfigAction()
    object SaveAndProceed : ProductConfigAction()
    object RequestPriceUpdate : ProductConfigAction()
    object ClearError : ProductConfigAction() {

    }


}

// shared/commonMain/ui/viewmodel/ProductConfigViewModel.kt
sealed class ConfigEntryMode {
    data class FromProduct(val productId: String) : ConfigEntryMode()
    data class FromExistingConfig(val configId: String) : ConfigEntryMode()
}
