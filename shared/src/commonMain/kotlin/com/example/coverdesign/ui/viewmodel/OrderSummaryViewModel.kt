package com.example.coverdesign.ui.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.coverdesign.domain.model.Order
import com.example.coverdesign.domain.model.OrderDetail
import com.example.coverdesign.domain.usecase.GetOrderDetailUseCase
import com.example.coverdesign.domain.usecase.SubmitOrderUseCase
import com.example.coverdesign.domain.util.onError
import com.example.coverdesign.domain.util.onSuccess
import com.example.coverdesign.ui.viewmodel.base.BaseViewModel
import com.example.coverdesign.ui.viewmodel.base.UiEvent
import com.example.coverdesign.ui.viewmodel.base.UiState
import kotlinx.coroutines.launch

class OrderSummaryViewModel(
    private val submitOrderUseCase : SubmitOrderUseCase,
    private val getOrderDetail     : GetOrderDetailUseCase,
) : BaseViewModel<OrderSummaryUiState, OrderSummaryUiEvent>() {

    override fun initialState() = OrderSummaryUiState()

    fun onAction(action: OrderSummaryAction) {
        when (action) {
            is OrderSummaryAction.LoadOrder   -> loadOrder(action.orderId)
            is OrderSummaryAction.SubmitOrder -> submitOrder(action.order)
        }
    }

    private fun loadOrder(orderId: String) {
        viewModelScope.launch {
            setState { copy(isLoading = true, error = null) }
            getOrderDetail(orderId)
                .onSuccess { detail ->
                    setState { copy(orderDetail = detail, isLoading = false) }
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
}

// ── State ──────────────────────────────────────────────────────────────────────
data class OrderSummaryUiState(
    val orderDetail      : OrderDetail? = null,
    val submittedOrderId : String?      = null,
    val isLoading        : Boolean      = false,
    val isSubmitting     : Boolean      = false,
    val error            : String?      = null,
) : UiState {
    val order: Order? get() = (orderDetail as? OrderDetail.CatalogOrder)?.order
}

// ── Events ─────────────────────────────────────────────────────────────────────
sealed class OrderSummaryUiEvent : UiEvent {
    data class OrderSubmitted(val orderId: String) : OrderSummaryUiEvent()
}

// ── Actions ────────────────────────────────────────────────────────────────────
sealed class OrderSummaryAction {
    data class LoadOrder(val orderId: String)  : OrderSummaryAction()
    data class SubmitOrder(val order: Order)   : OrderSummaryAction()
}