package com.example.coverdesign.ui.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.coverdesign.domain.model.ArSessionState
import com.example.coverdesign.domain.model.ProductConfig
import com.example.coverdesign.domain.model.TrackingState
import com.example.coverdesign.domain.usecase.GetModelPreviewUseCase
import com.example.coverdesign.domain.usecase.UpdateConfigUseCase
import com.example.coverdesign.domain.usecase.VirtualTryOnUseCase
import com.example.coverdesign.domain.util.onError
import com.example.coverdesign.domain.util.onSuccess
import com.example.coverdesign.ui.viewmodel.base.BaseViewModel
import com.example.coverdesign.ui.viewmodel.base.UiEvent
import com.example.coverdesign.ui.viewmodel.base.UiState
import kotlinx.coroutines.launch

// shared/commonMain/ui/viewmodel/ArTryOnViewModel.kt
class ArTryOnViewModel(
    private val virtualTryOn  : VirtualTryOnUseCase,
    private val updateConfig  : UpdateConfigUseCase,
    private val getModelPreview : GetModelPreviewUseCase
) : BaseViewModel<ArTryOnUiState, ArTryOnUiEvent>() {

    override fun initialState() = ArTryOnUiState()

    init {
        checkArSupport()
        observeArSession()
    }

    fun onAction(action: ArTryOnAction) {
        when (action) {
            is ArTryOnAction.StartSession    -> startSession(action.configId)
            is ArTryOnAction.StopSession     -> stopSession()
            is ArTryOnAction.UpdateConfig    -> onConfigUpdated(action.config)
            is ArTryOnAction.CaptureSnapshot -> captureSnapshot()
            is ArTryOnAction.ProceedToOrder  -> sendEvent(
                ArTryOnUiEvent.NavigateToOrder(action.configId)
            )
            is ArTryOnAction.UpdateTracking ->
                setState { copy(trackingState = action.state) }
        }
    }

    private fun checkArSupport() {
        setState { copy(isArSupported = virtualTryOn.isSupported()) }
    }

    private fun observeArSession() {
        viewModelScope.launch {
            virtualTryOn.observeSession().collect { sessionState ->
                setState { copy(sessionState = sessionState) }
            }
        }
        viewModelScope.launch {
            virtualTryOn.observeTracking().collect { trackingState ->
                setState { copy(trackingState = trackingState) }
            }
        }
    }
    private fun startSession(configId: String) {
        viewModelScope.launch {
            setState { copy(isLoading = true, error = null) }

            // 1. fetch model URL
            virtualTryOn.start(configId)
                .onSuccess {
                    // model URL comes back through ArSessionState
                }
                .onError { message, _ ->
                    setState { copy(isLoading = false, error = message) }
                    return@launch
                }

            val urlResult = getModelPreview(configId)
            urlResult.onSuccess { url ->
                setState { copy(modelUrl = url, isLoading = false) }
            }.onError { message, _ ->
                setState { copy(isLoading = false, error = message) }
            }
        }
    }

    private fun stopSession() {
        viewModelScope.launch { virtualTryOn.stop() }
    }

    private fun onConfigUpdated(config: ProductConfig) {
        viewModelScope.launch {
            updateConfig(config)
                .onError { message, _ ->
                    setState { copy(error = message) }
                }
        }
    }

    private fun captureSnapshot() {
        viewModelScope.launch {
            virtualTryOn.capture()
                .onSuccess { bytes ->
                    sendEvent(ArTryOnUiEvent.SnapshotCaptured(bytes))
                }
                .onError { message, _ ->
                    setState { copy(error = message) }
                }
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch { virtualTryOn.stop() }
    }
}

// ── State ─────────────────────────────────────────────────────────────────────
data class ArTryOnUiState(
    val isArSupported : Boolean         = false,
    val isLoading     : Boolean         = false,
    val modelUrl      : String?        = null,   // ← add
    val sessionState  : ArSessionState = ArSessionState.Idle,
    val trackingState : TrackingState = TrackingState.NOT_AVAILABLE,
    val error         : String?         = null
) : UiState

// ── Events ────────────────────────────────────────────────────────────────────
sealed class ArTryOnUiEvent : UiEvent {
    data class NavigateToOrder(val configId: String)   : ArTryOnUiEvent()
    data class SnapshotCaptured(val bytes: ByteArray)  : ArTryOnUiEvent()
}

// ── Actions ───────────────────────────────────────────────────────────────────
sealed class ArTryOnAction {
    data class StartSession(val configId: String)    : ArTryOnAction()
    data class UpdateConfig(val config: ProductConfig) : ArTryOnAction()
    data class ProceedToOrder(val configId: String)  : ArTryOnAction()

    data class UpdateTracking(val state: TrackingState) : ArTryOnAction()

    object StopSession                               : ArTryOnAction()
    object CaptureSnapshot                           : ArTryOnAction()

}