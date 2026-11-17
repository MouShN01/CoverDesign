package com.example.coverdesign.ui.viewmodel


import androidx.lifecycle.viewModelScope
import com.example.coverdesign.domain.model.GenerationTask
import com.example.coverdesign.domain.model.ModelStatus
import com.example.coverdesign.domain.repo.Model3DRepository
import com.example.coverdesign.domain.usecase.LinkOrderToAdaptedModelUseCase
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
    private val pollModelStatus: PollModelStatusUseCase,
    private val model3DRepo: Model3DRepository,
    private val linkOrderToAdaptedModel : LinkOrderToAdaptedModelUseCase
) : BaseViewModel<ModelPreviewUiState, ModelPreviewUiEvent>() {

    override fun initialState() = ModelPreviewUiState()

    private var pollingJob: Job? = null

    fun onAction(action: ModelPreviewAction) {
        when (action) {
            is ModelPreviewAction.LoadForProduct ->
                loadBaseModel(action.productId)

            is ModelPreviewAction.LoadForConfig ->
                loadAdaptedModel(action.configId, action.taskId)

            is ModelPreviewAction.LoadFromOrder ->
                loadFromOrder(action.adaptedModelId)

            is ModelPreviewAction.Retry ->
                retry(action.taskId)

            is ModelPreviewAction.ProceedToOrder -> {
                val taskId = uiState.value.adaptedModel?.id ?: return
                viewModelScope.launch {
                    linkOrderToAdaptedModel(action.configId, taskId)
                    sendEvent(ModelPreviewUiEvent.NavigateToOrder(action.configId))
                }
            }
        }
    }
    private fun loadBaseModel(productId: String) { //Ideally not used in the app
        viewModelScope.launch {
            setState { copy(isLoadingLocal = true, error = null) }
            model3DRepo.getBaseModelForProduct(productId)
                .onSuccess { model ->
                    setState { copy(baseModelUrl = model.modelUrl, isLoadingLocal = false) }
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
                        ModelStatus.READY  -> { /* modelUrl/modelStatus auto-derived */ }
                        ModelStatus.FAILED -> { /* isFailed auto-derived */ }
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

        pollingJob = viewModelScope.launch {
            pollModelStatus(taskId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        val model = result.data
                        println("loadAdaptedModel model - $model")
                        setState { copy(adaptedModel = model, error = null) }
                    }

                    is Resource.Error -> setState {
                        copy(error = result.message, adaptedModel = adaptedModel?.copy(status = ModelStatus.FAILED))
                    }

                    else -> {}
                }
            }
        }
    }

    // ── Path 3: from Order history — load by adaptedModelId directly ─────────

    private fun loadFromOrder(adaptedModelId: String) {
        viewModelScope.launch {
            setState { copy(isLoadingLocal = true, error = null) }
            model3DRepo.getAdaptedModelById(adaptedModelId)
                .onSuccess { adapted ->
                    if (adapted != null) {
                        setState { copy(adaptedModel = adapted, isLoadingLocal = false) }
                    } else {
                        setState { copy(isLoadingLocal = false, error = "3D model not found") }
                    }
                }
                .onError { message, _ ->
                    setState { copy(isLoadingLocal = false, error = message) }
                }
        }
    }

    private fun retry(taskId: String) {
        pollingJob?.cancel()
        setState { copy(adaptedModel = null, error = null) }
        startPolling(taskId)
    }

    override fun onCleared() {
        pollingJob?.cancel()
        super.onCleared()
    }
}

// ── State ─────────────────────────────────────────────────────────────────────

data class ModelPreviewUiState(
    val adaptedModel: GenerationTask? = null,
    val baseModelUrl: String? = null,
    val isLoadingLocal: Boolean = false,
    val error: String? = null,
) : UiState {
    val modelUrl: String?     get() = adaptedModel?.modelUrl ?: baseModelUrl
    val modelStatus: ModelStatus
        get() = adaptedModel?.status
            ?: if (baseModelUrl != null) ModelStatus.READY else ModelStatus.PENDING
    val estimatedWaitSeconds: Int? get() = adaptedModel?.estimatedWaitSeconds
    val isGenerating: Boolean get() = modelStatus == ModelStatus.PENDING || modelStatus == ModelStatus.GENERATING
    val isReady: Boolean      get() = modelStatus == ModelStatus.READY
    // isFailed covers both: adaptedModel with FAILED status, and a polling error before any model arrived
    val isFailed: Boolean     get() = modelStatus == ModelStatus.FAILED ||
        (adaptedModel == null && baseModelUrl == null && !isLoadingLocal && error != null)
}

// ── Events ────────────────────────────────────────────────────────────────────

sealed class ModelPreviewUiEvent : UiEvent {
    data class NavigateToOrder(val configId: String) : ModelPreviewUiEvent()
}

// ── Actions ───────────────────────────────────────────────────────────────────

sealed class ModelPreviewAction {
    data class LoadForProduct(val productId: String) : ModelPreviewAction()
    data class LoadForConfig(val configId: String, val taskId: String?) : ModelPreviewAction()
    data class LoadFromOrder(val adaptedModelId: String) : ModelPreviewAction()
    data class Retry(val taskId: String) : ModelPreviewAction()
    data class ProceedToOrder(val configId: String) : ModelPreviewAction()
}