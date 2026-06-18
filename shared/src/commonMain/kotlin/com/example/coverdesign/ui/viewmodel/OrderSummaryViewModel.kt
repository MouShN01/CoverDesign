package com.example.coverdesign.ui.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.coverdesign.domain.model.Order
import com.example.coverdesign.domain.usecase.GetOrderSummaryUseCase
import com.example.coverdesign.domain.usecase.SubmitOrderUseCase
import com.example.coverdesign.domain.util.onError
import com.example.coverdesign.domain.util.onSuccess
import com.example.coverdesign.ui.viewmodel.base.BaseViewModel
import com.example.coverdesign.ui.viewmodel.base.UiEvent
import com.example.coverdesign.ui.viewmodel.base.UiState
import kotlinx.coroutines.launch

// shared/commonMain/ui/viewmodel/OrderSummaryViewModel.kt
class OrderSummaryViewModel(
    private val submitOrderUseCase     : SubmitOrderUseCase,
    private val getOrderSummary : GetOrderSummaryUseCase
) : BaseViewModel<OrderSummaryUiState, OrderSummaryUiEvent>() {

    override fun initialState() = OrderSummaryUiState()

    init {
        observeOrderHistory()
    }

    fun onAction(action: OrderSummaryAction) {
        when (action) {
            is OrderSummaryAction.LoadOrder   -> loadOrder(action.orderId)
            is OrderSummaryAction.SubmitOrder -> submitOrder(action.order)
            is OrderSummaryAction.ViewHistory -> sendEvent(
                OrderSummaryUiEvent.NavigateToHistory
            )
        }
    }

    private fun loadOrder(orderId: String) {
        viewModelScope.launch {
            setState { copy(isLoading = true) }
            getOrderSummary(orderId)
                .onSuccess { order ->
                    setState { copy(order = order, isLoading = false) }
                }
                .onError { message, _ ->
                    setState { copy(error = message, isLoading = false) }
                }
        }
    }

    private fun submitOrder(order: Order) {
        viewModelScope.launch {
            setState { copy(isSubmitting = true, error = null) }
            submitOrderUseCase(order)
                .onSuccess { orderId ->
                    setState { copy(isSubmitting = false, submittedOrderId = orderId.value) }
                    sendEvent(OrderSummaryUiEvent.OrderSubmitted(orderId.value))
                }
                .onError { message, _ ->
                    setState { copy(isSubmitting = false, error = message) }
                }
        }
    }

    private fun observeOrderHistory() {
        viewModelScope.launch {
            getOrderSummary.observeHistory().collect { orders ->
                setState { copy(orderHistory = orders) }
            }
        }
    }
}

// ── State ─────────────────────────────────────────────────────────────────────
data class OrderSummaryUiState(
    val order            : Order?       = null,
    val orderHistory     : List<Order>  = emptyList(),
    val submittedOrderId : String?      = null,
    val isLoading        : Boolean      = false,
    val isSubmitting     : Boolean      = false,
    val error            : String?      = null
) : UiState

// ── Events ────────────────────────────────────────────────────────────────────
sealed class OrderSummaryUiEvent : UiEvent {
    data class OrderSubmitted(val orderId: String) : OrderSummaryUiEvent()
    object NavigateToHistory                       : OrderSummaryUiEvent()
}

// ── Actions ───────────────────────────────────────────────────────────────────
sealed class OrderSummaryAction {
    data class LoadOrder(val orderId: String)  : OrderSummaryAction()
    data class SubmitOrder(val order: Order)   : OrderSummaryAction()
    object ViewHistory                         : OrderSummaryAction()
}