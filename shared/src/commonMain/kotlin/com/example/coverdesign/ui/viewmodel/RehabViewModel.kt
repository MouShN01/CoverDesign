package com.example.coverdesign.ui.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.coverdesign.domain.model.GaitSession
import com.example.coverdesign.domain.repo.FireAuthRepository
import com.example.coverdesign.domain.util.Resource
import com.example.coverdesign.ui.viewmodel.base.BaseViewModel
import com.example.coverdesign.ui.viewmodel.base.UiEvent
import com.example.coverdesign.ui.viewmodel.base.UiState
import kotlinx.coroutines.launch

class RehabViewModel(
    private val authRepo: FireAuthRepository
) : BaseViewModel<RehabUiState, RehabUiEvent>() {

    override fun initialState() = RehabUiState()

    init {
        setState { copy(sessions = RehabMockData.generateSessions()) }
        viewModelScope.launch {
            val uid = authRepo.getCurrentUserId() ?: return@launch
            when (val result = authRepo.getUserProfile(uid)) {
                is Resource.Success -> setState { copy(hasInsoles = result.data.hasInsoles) }
                else -> {}
            }
        }
    }

    fun onAction(action: RehabAction) {
        when (action) {
            is RehabAction.ToggleSession -> {
                val next = if (uiState.value.expandedSessionId == action.id) null else action.id
                setState { copy(expandedSessionId = next) }
            }
            RehabAction.Back -> sendEvent(RehabUiEvent.NavigateBack)
        }
    }
}

// ── State ─────────────────────────────────────────────────────────────────────
data class RehabUiState(
    val sessions          : List<GaitSession> = emptyList(),
    val hasInsoles        : Boolean           = false,
    val expandedSessionId : String?           = null
) : UiState

// ── Events ────────────────────────────────────────────────────────────────────
sealed class RehabUiEvent : UiEvent {
    object NavigateBack : RehabUiEvent()
}

// ── Actions ───────────────────────────────────────────────────────────────────
sealed class RehabAction {
    data class ToggleSession(val id: String) : RehabAction()
    object Back : RehabAction()
}
