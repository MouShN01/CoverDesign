package com.example.coverdesign.ui.viewmodel

// shared/commonMain/ui/viewmodel/CatalogViewModel.kt
import androidx.lifecycle.viewModelScope
import com.example.coverdesign.domain.model.Product
import com.example.coverdesign.domain.usecase.GetCatalogUseCase
import com.example.coverdesign.domain.util.onError
import com.example.coverdesign.domain.util.onSuccess
import com.example.coverdesign.ui.viewmodel.base.BaseViewModel
import com.example.coverdesign.ui.viewmodel.base.UiEvent
import com.example.coverdesign.ui.viewmodel.base.UiState
import kotlinx.coroutines.launch

class CatalogViewModel(
    private val getCatalog: GetCatalogUseCase
) : BaseViewModel<CatalogUiState, CatalogUiEvent>() {

    override fun initialState() = CatalogUiState()

    init {
        loadCatalog()
    }

    fun onAction(action: CatalogAction) {
        when (action) {
            is CatalogAction.Refresh        -> loadCatalog()
            is CatalogAction.SelectProduct  -> onProductSelected(action.product)
            is CatalogAction.Search         -> onSearch(action.query)
        }
    }

    private fun loadCatalog() {
        viewModelScope.launch {
            setState { copy(isLoading = true, error = null) }
            getCatalog()
                .onSuccess { products ->
                    setState {
                        copy(
                            isLoading = false,
                            products  = products,
                            filtered  = products
                        )
                    }
                }
                .onError { message, _ ->
                    setState { copy(isLoading = false, error = message) }
                }
        }
    }

    private fun onProductSelected(product: Product) {
        sendEvent(CatalogUiEvent.NavigateToConfig(product))
    }

    private fun onSearch(query: String) {
        val filtered = uiState.value.products.filter {
            it.name.contains(query, ignoreCase = true) ||
                    it.description.contains(query, ignoreCase = true)
        }
        setState { copy(searchQuery = query, filtered = filtered) }
    }
}

// ── State ────────────────────────────────────────────────────────────────────
data class CatalogUiState(
    val isLoading   : Boolean       = false,
    val products    : List<Product> = emptyList(),
    val filtered    : List<Product> = emptyList(),
    val searchQuery : String        = "",
    val error       : String?       = null
) : UiState

// ── One-time events ───────────────────────────────────────────────────────────
sealed class CatalogUiEvent : UiEvent {
    data class NavigateToConfig(val product: Product) : CatalogUiEvent()
}

// ── User actions ──────────────────────────────────────────────────────────────
sealed class CatalogAction {
    object Refresh : CatalogAction()
    data class SelectProduct(val product: Product) : CatalogAction()
    data class Search(val query: String) : CatalogAction()
}