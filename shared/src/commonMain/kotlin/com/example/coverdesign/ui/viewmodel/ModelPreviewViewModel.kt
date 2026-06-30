package com.example.coverdesign.ui.viewmodel


import androidx.lifecycle.viewModelScope
import com.example.coverdesign.domain.model.AdaptedModel3D
import com.example.coverdesign.domain.model.ModelStatus
import com.example.coverdesign.domain.repo.Model3DRepository
import com.example.coverdesign.domain.usecase.GenerateModelUseCase
import com.example.coverdesign.domain.usecase.PollModelStatusUseCase
import com.example.coverdesign.domain.util.Resource
import com.example.coverdesign.domain.util.onError
import com.example.coverdesign.domain.util.onSuccess
import com.example.coverdesign.ui.viewmodel.base.BaseViewModel
import com.example.coverdesign.ui.viewmodel.base.UiEvent
import com.example.coverdesign.ui.viewmodel.base.UiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────────────────────
// ModelPreviewViewModel
//
// Two entry paths:
//   LoadForProduct — 3D tab, base model only, no generation pipeline
//   LoadForConfig  — config pipeline, local DB check then poll if needed
//
// LoadForConfig decision tree:
//   local DB hit + READY        → show immediately, no network call
//   local DB hit + IN PROGRESS  → show cached data + resume polling
//   local DB hit + FAILED       → show retry UI
//   not in local DB             → start polling with provided taskId
// ─────────────────────────────────────────────────────────────────────────────

class ModelPreviewViewModel(
    private val generateModel: GenerateModelUseCase,
    private val pollModelStatus: PollModelStatusUseCase,
    private val model3DRepo: Model3DRepository
) : BaseViewModel<ModelPreviewUiState, ModelPreviewUiEvent>() {

    override fun initialState() = ModelPreviewUiState()

    private var pollingJob: Job? = null

    fun onAction(action: ModelPreviewAction) {
        when (action) {
            is ModelPreviewAction.LoadForProduct ->
                loadBaseModel(action.productId)

            is ModelPreviewAction.LoadForConfig ->
                loadAdaptedModel(action.configId, action.taskId)

            is ModelPreviewAction.Retry ->
                retry(action.taskId)

            is ModelPreviewAction.ProceedToAr ->
                sendEvent(ModelPreviewUiEvent.NavigateToAr(action.configId))

            is ModelPreviewAction.ProceedToOrder ->
                sendEvent(ModelPreviewUiEvent.NavigateToOrder(action.configId))
        }
    }

    // ── Path 1: 3D tab — base model, no generation ───────────────────────────

    private fun loadBaseModel(productId: String) {
        viewModelScope.launch {
            setState { copy(isLoadingLocal = true, error = null) }
            model3DRepo.getBaseModelForProduct(productId)
                .onSuccess { model ->
                    setState {
                        copy(
                            modelUrl = model.modelUrl,
                            modelStatus = ModelStatus.READY,
                            isLoadingLocal = false
                        )
                    }
                }
                .onError { message, _ ->
                    setState { copy(isLoadingLocal = false, error = message) }
                }
        }
    }

    // ── Path 2: config pipeline ───────────────────────────────────────────────

    private fun loadAdaptedModel(configId: String, taskId: String?) {
        viewModelScope.launch {
            setState { copy(isLoadingLocal = true, error = null) }

            val localResult = model3DRepo.getAdaptedModel(configId)
            println("loadAdaptedModel localResult: $localResult")
            when {
                localResult is Resource.Success && localResult.data != null -> {
                    val adapted = localResult.data
                    setState { copy(adaptedModel = adapted, isLoadingLocal = false) }

                    when (adapted.status) {
                        ModelStatus.READY -> setState {
                            copy(modelUrl = adapted.modelUrl, modelStatus = ModelStatus.READY)
                        }

                        ModelStatus.FAILED -> setState {
                            copy(modelStatus = ModelStatus.FAILED)
                        }

                        ModelStatus.PENDING,
                        ModelStatus.GENERATING -> {
                            // resume polling — app may have been closed mid-generation
                            val idToPoll = taskId ?: adapted.id
                            startPolling(idToPoll)
                        }
                    }
                }

                else -> {
                    setState { copy(isLoadingLocal = false) }
                    if (taskId != null) startPolling(taskId)
                    else setState { copy(error = "Model not found — please generate again") }
                }
            }
        }
    }

    // ── Polling ───────────────────────────────────────────────────────────────

    private fun startPolling(taskId: String) {
        println("loadAdaptedModel - startPolling")
        if (pollingJob?.isActive == true) return
        setState { copy(modelStatus = ModelStatus.PENDING) }

        pollingJob = viewModelScope.launch {
            pollModelStatus(taskId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        val model = result.data
                        setState {
                            copy(
                                modelStatus = model.status,
                                estimatedWaitSeconds = model.estimatedWaitSeconds,
                                error = null
                            )
                        }
                        println("loadAdaptedModel model - $model")

                        if (model.status == ModelStatus.READY) {
                            setState { copy(modelUrl = model.modelUrl) }
                        }
                    }

                    is Resource.Error -> setState {
                        //print("getGenerationStatus FROM INSIDE setState we got an error ${result.message}")
                        copy(error = result.message, modelStatus = ModelStatus.FAILED)
                    }

                    else -> {}
                }
            }
        }
    }

    private fun retry(taskId: String) {
        pollingJob?.cancel()
        setState { copy(error = null, modelStatus = ModelStatus.PENDING, modelUrl = null) }
        startPolling(taskId)
    }

    override fun onCleared() {
        pollingJob?.cancel()
        super.onCleared()
    }
}

// ── State ─────────────────────────────────────────────────────────────────────

data class ModelPreviewUiState(
    val adaptedModel: AdaptedModel3D? = null,
    val modelUrl: String? = null,
    val modelStatus: ModelStatus = ModelStatus.PENDING,
    val isLoadingLocal: Boolean = false,
    val estimatedWaitSeconds: Int? = null,
    val error: String? = null
) : UiState {
    val isGenerating: Boolean
        get() =
            modelStatus == ModelStatus.PENDING || modelStatus == ModelStatus.GENERATING
    val isReady: Boolean get() = modelStatus == ModelStatus.READY
    val isFailed: Boolean get() = modelStatus == ModelStatus.FAILED
}

// ── Events ────────────────────────────────────────────────────────────────────

sealed class ModelPreviewUiEvent : UiEvent {
    data class NavigateToAr(val configId: String) : ModelPreviewUiEvent()
    data class NavigateToOrder(val configId: String) : ModelPreviewUiEvent()
}

// ── Actions ───────────────────────────────────────────────────────────────────

sealed class ModelPreviewAction {
    data class LoadForProduct(val productId: String) : ModelPreviewAction()
    data class LoadForConfig(val configId: String, val taskId: String?) : ModelPreviewAction()
    data class Retry(val taskId: String) : ModelPreviewAction()
    data class ProceedToAr(val configId: String) : ModelPreviewAction()
    data class ProceedToOrder(val configId: String) : ModelPreviewAction()
}