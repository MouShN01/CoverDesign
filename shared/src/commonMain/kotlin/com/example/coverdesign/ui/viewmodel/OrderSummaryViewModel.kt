package com.example.coverdesign.ui.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.coverdesign.domain.model.OrderDetail
import com.example.coverdesign.domain.model.OrderId
import com.example.coverdesign.domain.model.OrderStatus
import com.example.coverdesign.domain.repo.OrderRepository
import com.example.coverdesign.domain.usecase.GetOrderDetailUseCase
import com.example.coverdesign.domain.util.Resource
import com.example.coverdesign.domain.util.onError
import com.example.coverdesign.domain.util.onSuccess
import com.example.coverdesign.ui.viewmodel.base.BaseViewModel
import com.example.coverdesign.ui.viewmodel.base.UiEvent
import com.example.coverdesign.ui.viewmodel.base.UiState
import kotlinx.coroutines.launch

class OrderSummaryViewModel(
    private val orderRepo    : OrderRepository,
    private val getOrderDetail: GetOrderDetailUseCase,
) : BaseViewModel<OrderSummaryUiState, OrderSummaryUiEvent>() {

    override fun initialState() = OrderSummaryUiState()

    fun onAction(action: OrderSummaryAction) {
        when (action) {
            is OrderSummaryAction.LoadOrder      -> loadOrder(action.orderId)
            is OrderSummaryAction.SubmitCurrentOrder -> submitCurrentOrder()
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

    private fun submitCurrentOrder() {
        viewModelScope.launch {
            setState { copy(isSubmitting = true, error = null) }
            val detail = uiState.value.orderDetail ?: return@launch

            val result: Resource<OrderId> = when (detail) {
                is OrderDetail.CatalogOrder         -> orderRepo.submitOrder(detail.order)
                is OrderDetail.InsoleOrder          -> orderRepo.submitInsoleOrder(detail.config)
                is OrderDetail.ProstheticCoverOrder -> orderRepo.submitProstheticCoverOrder(detail.config)
            }

            result
                .onSuccess { orderId ->
                    val submitted = when (detail) {
                        is OrderDetail.CatalogOrder         -> detail.copy(order = detail.order.copy(status = OrderStatus.SUBMITTED))
                        is OrderDetail.InsoleOrder          -> detail.copy(status = OrderStatus.SUBMITTED)
                        is OrderDetail.ProstheticCoverOrder -> detail.copy(status = OrderStatus.SUBMITTED)
                    }
                    setState { copy(isSubmitting = false, submittedOrderId = orderId.orderId, orderDetail = submitted) }
                    sendEvent(OrderSummaryUiEvent.OrderSubmitted(orderId.orderId))
                }
                .onError { msg, _ ->
                    setState { copy(isSubmitting = false, error = msg) }
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
) : UiState

// ── Events ─────────────────────────────────────────────────────────────────────
sealed class OrderSummaryUiEvent : UiEvent {
    data class OrderSubmitted(val orderId: String) : OrderSummaryUiEvent()
}

// ── Actions ────────────────────────────────────────────────────────────────────
sealed class OrderSummaryAction {
    data class LoadOrder(val orderId: String) : OrderSummaryAction()
    data object SubmitCurrentOrder            : OrderSummaryAction()
}