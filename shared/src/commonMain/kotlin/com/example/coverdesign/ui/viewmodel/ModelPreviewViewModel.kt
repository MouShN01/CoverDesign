package com.example.coverdesign.ui.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.coverdesign.domain.model.AdaptedModel3D
import com.example.coverdesign.domain.model.ModelStatus
import com.example.coverdesign.domain.repo.Model3DRepository
import com.example.coverdesign.domain.usecase.GenerateModelUseCase
import com.example.coverdesign.domain.util.onError
import com.example.coverdesign.domain.util.onSuccess
import com.example.coverdesign.ui.viewmodel.base.BaseViewModel
import com.example.coverdesign.ui.viewmodel.base.UiEvent
import com.example.coverdesign.ui.viewmodel.base.UiState
import kotlinx.coroutines.launch


class ModelPreviewViewModel(
    private val generateModel   : GenerateModelUseCase,
    //private val getModelPreview : GetModelPreviewUseCase,
    private val model3DRepo        : Model3DRepository
) : BaseViewModel<ModelPreviewUiState, ModelPreviewUiEvent>() {



    override fun initialState() = ModelPreviewUiState()


    fun onAction(action: ModelPreviewAction) {
        when (action) {
            is ModelPreviewAction.LoadForProduct -> loadBaseModel(action.productId)   // 3D tab only
            is ModelPreviewAction.LoadForConfig  -> loadAdaptedModel(action.configId) // config pipeline
            is ModelPreviewAction.ProceedToAr    -> sendEvent(ModelPreviewUiEvent.NavigateToAr(action.configId))
            is ModelPreviewAction.ProceedToOrder -> sendEvent(ModelPreviewUiEvent.NavigateToOrder(action.configId))
        }
    }

    private fun loadBaseModel(productId: String) {
        viewModelScope.launch {
            setState { copy(isGenerating = true, error = null) }
            model3DRepo.getBaseModelForProduct(productId)
                .onSuccess { model ->
                    setState {
                        copy(modelUrl = model.modelUrl, modelStatus = ModelStatus.READY, isGenerating = false)
                    }
                }
                .onError { message, _ -> setState { copy(isGenerating = false, error = message) } }
        }
    }

    private fun loadAdaptedModel(configId: String) {
        viewModelScope.launch {
            setState { copy(isGenerating = true, error = null) }
            model3DRepo.getAdaptedModel(configId)
                .onSuccess { adapted ->
                    setState { copy(adaptedModel = adapted) }
                    if (adapted.status == ModelStatus.READY) {
                        setState { copy(modelUrl = adapted.modelUrl, modelStatus = ModelStatus.READY, isGenerating = false) }
                    } else {
                        observeStatus(adapted.id)
                    }
                }
                .onError { message, _ -> setState { copy(isGenerating = false, error = message) } }
        }
    }

    private fun observeStatus(adaptedModelId: String) {
        viewModelScope.launch {
            model3DRepo.getAdaptedModelStatus(adaptedModelId).collect { status ->
                setState { copy(modelStatus = status) }
                when (status) {
                    ModelStatus.READY -> {
                        model3DRepo.getAdaptedModel(uiState.value.adaptedModel!!.configId)
                            .onSuccess { adapted ->
                                setState { copy(modelUrl = adapted.modelUrl, isGenerating = false) }
                            }
                    }
                    ModelStatus.FAILED -> setState { copy(isGenerating = false, error = "Generation failed") }
                    else -> Unit
                }
            }
        }
    }
}

// ── State ─────────────────────────────────────────────────────────────────────
data class ModelPreviewUiState(
    val adaptedModel : AdaptedModel3D? = null,
    val modelUrl     : String?         = null,
    val modelStatus  : ModelStatus     = ModelStatus.PENDING,
    val isGenerating : Boolean         = false,
    val error        : String?         = null
) : UiState

// ── Events ────────────────────────────────────────────────────────────────────
sealed class ModelPreviewUiEvent : UiEvent {
    data class NavigateToAr(val configId: String)    : ModelPreviewUiEvent()
    data class NavigateToOrder(val configId: String) : ModelPreviewUiEvent()
}

// ── Actions ───────────────────────────────────────────────────────────────────
sealed class ModelPreviewAction {
    data class LoadForProduct(val productId: String) : ModelPreviewAction()
    data class LoadForConfig(val configId: String)   : ModelPreviewAction()
    data class ProceedToAr(val configId: String)     : ModelPreviewAction()
    data class ProceedToOrder(val configId: String)  : ModelPreviewAction()
}
