package com.example.coverdesign.ui.viewmodel.base

// shared/commonMain/ui/viewmodel/base/BaseViewModel.kt
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

abstract class BaseViewModel<S : UiState, E : UiEvent> : ViewModel() {

    private val _uiState = MutableStateFlow(initialState())
    val uiState: StateFlow<S> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<E>(extraBufferCapacity = 1)
    val events: SharedFlow<E> = _events.asSharedFlow()

    protected abstract fun initialState(): S

    protected fun setState(reducer: S.() -> S) {
        _uiState.value = _uiState.value.reducer()
    }

    protected fun sendEvent(event: E) {
        _events.tryEmit(event)
    }
}

// marker interfaces — every screen's state and events implement these
interface UiState
interface UiEvent