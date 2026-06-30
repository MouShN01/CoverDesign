package com.example.coverdesign.ui.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.coverdesign.domain.model.TrackingState
import com.example.coverdesign.domain.usecase.VirtualTryOnUseCase
import com.example.coverdesign.domain.util.onError
import com.example.coverdesign.domain.util.onSuccess
import com.example.coverdesign.ui.viewmodel.base.BaseViewModel
import com.example.coverdesign.ui.viewmodel.base.UiEvent
import com.example.coverdesign.ui.viewmodel.base.UiState
import kotlinx.coroutines.launch

// shared/commonMain/ui/viewmodel/ArTryOnViewModel.kt
class ArTryOnViewModel(
    private val virtualTryOn: VirtualTryOnUseCase
) : BaseViewModel<ArTryOnUiState, ArTryOnUiEvent>() {

    override fun initialState() = ArTryOnUiState()

    fun onAction(action: ArTryOnAction) {
        when (action) {
            is ArTryOnAction.Load             -> loadModel(action.configId)
            is ArTryOnAction.UpdateTracking   -> setState { copy(trackingState = action.state) }
            is ArTryOnAction.UpdateParameters -> sendEvent(ArTryOnUiEvent.NavigateToConfigEdit(action.configId))
            is ArTryOnAction.Proceed          -> sendEvent(ArTryOnUiEvent.NavigateToOrder(action.configId))
        }
    }

    private fun loadModel(configId: String) {
        viewModelScope.launch {
            setState {
                copy(isLoading = true, isArSupported = virtualTryOn.isArSupported(), error = null)
            }
            virtualTryOn.loadModelForTryOn(configId)
                .onSuccess { adapted ->
                    setState { copy(modelUrl = adapted?.modelUrl, isLoading = false) }
                }
                .onError { message, _ ->
                    setState { copy(isLoading = false, error = message) }
                }
        }
    }
}

data class ArTryOnUiState(
    val isArSupported : Boolean       = false,
    val isLoading      : Boolean       = false,
    val modelUrl        : String?       = null,
    val trackingState     : TrackingState = TrackingState.NOT_AVAILABLE,
    val error               : String?      = null
) : UiState

sealed class ArTryOnUiEvent : UiEvent {
    data class NavigateToOrder(val configId: String)      : ArTryOnUiEvent()
    data class NavigateToConfigEdit(val configId: String) : ArTryOnUiEvent()
}

sealed class ArTryOnAction {
    data class Load(val configId: String)               : ArTryOnAction()
    data class UpdateTracking(val state: TrackingState)  : ArTryOnAction()
    data class UpdateParameters(val configId: String)    : ArTryOnAction()
    data class Proceed(val configId: String)             : ArTryOnAction()
}