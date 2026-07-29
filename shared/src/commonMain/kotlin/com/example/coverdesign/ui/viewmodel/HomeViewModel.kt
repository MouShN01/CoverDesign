package com.example.coverdesign.ui.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.coverdesign.domain.model.GaitSession
import com.example.coverdesign.domain.model.OrderListItem
import com.example.coverdesign.domain.repo.FireAuthRepository
import com.example.coverdesign.domain.usecase.GetOrderListUseCase
import com.example.coverdesign.domain.util.Resource
import com.example.coverdesign.ui.viewmodel.base.BaseViewModel
import com.example.coverdesign.ui.viewmodel.base.UiEvent
import com.example.coverdesign.ui.viewmodel.base.UiState
import kotlinx.coroutines.launch

class HomeViewModel(
    private val getOrderList : GetOrderListUseCase,
    private val authRepo     : FireAuthRepository
) : BaseViewModel<HomeUiState, HomeUiEvent>() {

    override fun initialState() = HomeUiState()

    init {
        loadRecentOrders()
        loadUserState()
        setState { copy(rehabSessions = RehabMockData.generateSessions()) }
    }

    fun onAction(action: HomeAction) {
        when (action) {
            is HomeAction.NewOrder          -> sendEvent(HomeUiEvent.NavigateToNewOrder)
            is HomeAction.Open3D            -> sendEvent(HomeUiEvent.NavigateTo3D)
            is HomeAction.OpenTryOn         -> sendEvent(HomeUiEvent.NavigateToTryOn)
            is HomeAction.OpenOrder         -> sendEvent(HomeUiEvent.NavigateToOrder(action.orderId))
            is HomeAction.SeeAllOrders      -> sendEvent(HomeUiEvent.NavigateToAllOrders)
            is HomeAction.OpenRehabHistory  -> sendEvent(HomeUiEvent.NavigateToRehabHistory)
            is HomeAction.OrderInsoles      -> sendEvent(HomeUiEvent.NavigateToInsoleOrder)
            is HomeAction.Logout            -> logout()
        }
    }

    private fun loadRecentOrders() {
        viewModelScope.launch {
            getOrderList().collect { items ->
                setState { copy(recentOrders = items.take(3)) }
            }
        }
    }

    private fun loadUserState() {
        viewModelScope.launch {
            val uid = authRepo.getCurrentUserId() ?: return@launch
            when (val result = authRepo.getUserProfile(uid)) {
                is Resource.Success -> setState { copy(hasInsoles = result.data.hasInsoles) }
                else -> {}
            }
        }
    }

    private fun logout() {
        viewModelScope.launch { authRepo.signOut() }
    }
}

// ── State ─────────────────────────────────────────────────────────────────────
data class HomeUiState(
    val recentOrders  : List<OrderListItem> = emptyList(),
    val rehabSessions : List<GaitSession> = emptyList(),
    val hasInsoles    : Boolean           = false,
    val isLoading     : Boolean           = false
) : UiState

// ── Events ────────────────────────────────────────────────────────────────────
sealed class HomeUiEvent : UiEvent {
    object NavigateToNewOrder      : HomeUiEvent()
    object NavigateTo3D            : HomeUiEvent()
    object NavigateToTryOn         : HomeUiEvent()
    object NavigateToAllOrders     : HomeUiEvent()
    object NavigateToRehabHistory  : HomeUiEvent()
    object NavigateToInsoleOrder   : HomeUiEvent()
    data class NavigateToOrder(val orderId: String) : HomeUiEvent()
}

// ── Actions ───────────────────────────────────────────────────────────────────
sealed class HomeAction {
    object NewOrder          : HomeAction()
    object Open3D            : HomeAction()
    object OpenTryOn         : HomeAction()
    object SeeAllOrders      : HomeAction()
    object OpenRehabHistory  : HomeAction()
    object OrderInsoles      : HomeAction()
    object Logout            : HomeAction()
    data class OpenOrder(val orderId: String) : HomeAction()
}
