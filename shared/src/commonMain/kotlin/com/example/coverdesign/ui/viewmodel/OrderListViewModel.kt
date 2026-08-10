package com.example.coverdesign.ui.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.coverdesign.domain.model.OrderCategory
import com.example.coverdesign.domain.model.OrderListItem
import com.example.coverdesign.domain.usecase.GetOrderListUseCase
import com.example.coverdesign.ui.viewmodel.base.BaseViewModel
import com.example.coverdesign.ui.viewmodel.base.UiEvent
import com.example.coverdesign.ui.viewmodel.base.UiState
import kotlinx.coroutines.launch

enum class OrderListFilter { ALL, COSMETIC_COVER, INSOLE }

data class OrderListUiState(
    val allItems: List<OrderListItem> = emptyList(),
    val filter: OrderListFilter = OrderListFilter.ALL,
) : UiState {
    val visibleItems: List<OrderListItem> get() = when (filter) {
        OrderListFilter.ALL            -> allItems
        OrderListFilter.COSMETIC_COVER -> allItems.filter { it.category == OrderCategory.PROSTHETIC_COVER }
        OrderListFilter.INSOLE         -> allItems.filter { it.category == OrderCategory.INSOLE }
    }
}

sealed class OrderListUiEvent : UiEvent

class OrderListViewModel(
    private val getOrderList: GetOrderListUseCase
) : BaseViewModel<OrderListUiState, OrderListUiEvent>() {

    override fun initialState() = OrderListUiState()

    init {
        viewModelScope.launch {
            getOrderList().collect { items ->
                setState { copy(allItems = items) }
            }
        }
    }

    fun setFilter(filter: OrderListFilter) = setState { copy(filter = filter) }
}