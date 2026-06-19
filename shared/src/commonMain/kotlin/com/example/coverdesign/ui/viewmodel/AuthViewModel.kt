package com.example.coverdesign.ui.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.coverdesign.domain.model.Credentials
import com.example.coverdesign.domain.repo.AuthRepository
import com.example.coverdesign.domain.util.onError
import com.example.coverdesign.domain.util.onSuccess
import com.example.coverdesign.ui.viewmodel.base.BaseViewModel
import com.example.coverdesign.ui.viewmodel.base.UiEvent
import com.example.coverdesign.ui.viewmodel.base.UiState
import kotlinx.coroutines.launch

// shared/commonMain/ui/viewmodel/AuthViewModel.kt
class AuthViewModel(
    private val authRepo: AuthRepository
) : BaseViewModel<AuthUiState, AuthUiEvent>() {

    override fun initialState() = AuthUiState()

    fun onAction(action: AuthAction) {
        when (action) {
            is AuthAction.UpdateEmail    -> setState { copy(email = action.value) }
            is AuthAction.UpdatePassword -> setState { copy(password = action.value) }
            is AuthAction.UpdateName     -> setState { copy(name = action.value) }
            is AuthAction.TogglePassword -> setState { copy(passwordVisible = !passwordVisible) }
            is AuthAction.Login          -> login()
            is AuthAction.Register       -> register()
            else -> {}
        }
    }

    private fun login() {
        val state = uiState.value
        if (!validateInputs(state.email, state.password)) return

        viewModelScope.launch {
            setState { copy(isLoading = true, error = null) }
            authRepo.login(Credentials(state.email, state.password))
                .onSuccess {
                    setState { copy(isLoading = false) }
                    sendEvent(AuthUiEvent.Success)
                }
                .onError { message, _ ->
                    setState { copy(isLoading = false, error = message) }
                }
        }
    }

    private fun register() {
        val state = uiState.value
        if (!validateInputs(state.email, state.password)) return

        viewModelScope.launch {
            setState { copy(isLoading = true, error = null) }
            // for now same login call — backend registration added later
            authRepo.login(Credentials(state.email, state.password))
                .onSuccess {
                    setState { copy(isLoading = false) }
                    sendEvent(AuthUiEvent.Success)
                }
                .onError { message, _ ->
                    setState { copy(isLoading = false, error = message) }
                }
        }
    }

    private fun validateInputs(email: String, password: String): Boolean {
        val error = when {
            email.isBlank()              -> "Email is required"
            !email.contains("@")         -> "Enter a valid email"
            password.isBlank()           -> "Password is required"
            password.length < 6          -> "Password must be at least 6 characters"
            else                         -> null
        }
        if (error != null) setState { copy(error = error) }
        return error == null
    }
}

// ── State ─────────────────────────────────────────────────────────────────────
data class AuthUiState(
    val email           : String  = "",
    val password        : String  = "",
    val name            : String  = "",
    val passwordVisible : Boolean = false,
    val isLoading       : Boolean = false,
    val error           : String? = null
) : UiState

sealed class AuthUiEvent : UiEvent {
    object Success : AuthUiEvent()
}

sealed class AuthAction {
    data class UpdateEmail(val value: String)    : AuthAction()
    data class UpdatePassword(val value: String) : AuthAction()
    data class UpdateName(val value: String)     : AuthAction()
    object TogglePassword                        : AuthAction()
    object Login                                 : AuthAction()
    object Register                              : AuthAction()
    object Logout                              : AuthAction()
}