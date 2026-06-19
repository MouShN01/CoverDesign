package com.example.coverdesign.ui.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.coverdesign.domain.model.Order
import com.example.coverdesign.domain.repo.AuthRepository
import com.example.coverdesign.domain.usecase.GetOrderSummaryUseCase
import com.example.coverdesign.ui.viewmodel.base.BaseViewModel
import com.example.coverdesign.ui.viewmodel.base.UiEvent
import com.example.coverdesign.ui.viewmodel.base.UiState
import kotlinx.coroutines.launch

class HomeViewModel(
    private val getOrderSummary : GetOrderSummaryUseCase,
    private val authRepo        : AuthRepository
) : BaseViewModel<HomeUiState, HomeUiEvent>() {

    override fun initialState() = HomeUiState()

    init {
        loadRecentOrders()
    }

    fun onAction(action: HomeAction) {
        when (action) {
            is HomeAction.NewOrder     -> sendEvent(HomeUiEvent.NavigateToNewOrder)
            is HomeAction.Open3D       -> sendEvent(HomeUiEvent.NavigateTo3D)
            is HomeAction.OpenTryOn    -> sendEvent(HomeUiEvent.NavigateToTryOn)
            is HomeAction.OpenOrder    -> sendEvent(HomeUiEvent.NavigateToOrder(action.orderId))
            is HomeAction.SeeAllOrders -> sendEvent(HomeUiEvent.NavigateToAllOrders)
            is HomeAction.Logout       -> logout()
        }
    }

    private fun loadRecentOrders() {
        viewModelScope.launch {
            getOrderSummary.observeHistory().collect { orders ->
                setState { copy(recentOrders = orders.take(3)) }
            }
        }
    }

    private fun logout() {
        viewModelScope.launch { authRepo.logout() }
    }
}

// ── State ─────────────────────────────────────────────────────────────────────
data class HomeUiState(
    val recentOrders : List<Order> = emptyList(),
    val isLoading    : Boolean     = false
) : UiState

// ── Events ────────────────────────────────────────────────────────────────────
sealed class HomeUiEvent : UiEvent {
    object NavigateToNewOrder  : HomeUiEvent()
    object NavigateTo3D        : HomeUiEvent()
    object NavigateToTryOn     : HomeUiEvent()
    object NavigateToAllOrders : HomeUiEvent()
    data class NavigateToOrder(val orderId: String) : HomeUiEvent()
}

// ── Actions ───────────────────────────────────────────────────────────────────
sealed class HomeAction {
    object NewOrder     : HomeAction()
    object Open3D       : HomeAction()
    object OpenTryOn    : HomeAction()
    object SeeAllOrders : HomeAction()
    object Logout       : HomeAction()
    data class OpenOrder(val orderId: String) : HomeAction()
}