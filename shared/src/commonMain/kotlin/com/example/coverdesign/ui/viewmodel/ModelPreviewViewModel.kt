package com.example.coverdesign.ui.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.coverdesign.domain.model.Model3D
import com.example.coverdesign.domain.model.ModelStatus
import com.example.coverdesign.domain.model.ProductConfig
import com.example.coverdesign.domain.usecase.GenerateModelUseCase
import com.example.coverdesign.domain.usecase.GetModelPreviewUseCase
import com.example.coverdesign.domain.util.onError
import com.example.coverdesign.domain.util.onSuccess
import com.example.coverdesign.ui.viewmodel.base.BaseViewModel
import com.example.coverdesign.ui.viewmodel.base.UiEvent
import com.example.coverdesign.ui.viewmodel.base.UiState
import kotlinx.coroutines.launch

// shared/commonMain/ui/viewmodel/ModelPreviewViewModel.kt
class ModelPreviewViewModel(
    private val generateModel   : GenerateModelUseCase,
    private val getModelPreview : GetModelPreviewUseCase
) : BaseViewModel<ModelPreviewUiState, ModelPreviewUiEvent>() {

    override fun initialState() = ModelPreviewUiState()

    fun onAction(action: ModelPreviewAction) {
        when (action) {
            is ModelPreviewAction.LoadModel   -> loadModel(action.config)
            is ModelPreviewAction.ProceedToAr -> sendEvent(
                ModelPreviewUiEvent.NavigateToAr(action.configId)
            )
            is ModelPreviewAction.ProceedToOrder -> sendEvent(
                ModelPreviewUiEvent.NavigateToOrder(action.configId)
            )
        }
    }

    private fun loadModel(config: ProductConfig) {
        viewModelScope.launch {
            setState { copy(isGenerating = true, error = null) }

            generateModel(config)
                .onSuccess { model ->
                    setState { copy(model = model) }
                    observeStatus(model.id)
                }
                .onError { message, _ ->
                    setState { copy(isGenerating = false, error = message) }
                }
        }
    }

    private fun observeStatus(modelId: String) {
        viewModelScope.launch {
            getModelPreview.observeStatus(modelId).collect { status ->
                setState { copy(modelStatus = status) }
                when (status) {
                    ModelStatus.READY  -> {
                        val urlResult = getModelPreview(
                            uiState.value.model!!.configId
                        )
                        urlResult.onSuccess { url ->
                            setState { copy(modelUrl = url, isGenerating = false) }
                        }
                    }
                    ModelStatus.FAILED ->
                        setState { copy(isGenerating = false, error = "Model generation failed") }
                    else -> Unit
                }
            }
        }
    }
}

// ── State ─────────────────────────────────────────────────────────────────────
data class ModelPreviewUiState(
    val model        : Model3D?     = null,
    val modelUrl     : String?      = null,
    val modelStatus  : ModelStatus = ModelStatus.PENDING,
    val isGenerating : Boolean      = false,
    val error        : String?      = null
) : UiState

// ── Events ────────────────────────────────────────────────────────────────────
sealed class ModelPreviewUiEvent : UiEvent {
    data class NavigateToAr(val configId: String)    : ModelPreviewUiEvent()
    data class NavigateToOrder(val configId: String) : ModelPreviewUiEvent()
}

// ── Actions ───────────────────────────────────────────────────────────────────
sealed class ModelPreviewAction {
    data class LoadModel(val config: ProductConfig)       : ModelPreviewAction()
    data class ProceedToAr(val configId: String)          : ModelPreviewAction()
    data class ProceedToOrder(val configId: String)       : ModelPreviewAction()
}