package com.example.coverdesign.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coverdesign.domain.model.UserRole
import com.example.coverdesign.domain.repo.FireAuthRepository
import com.example.coverdesign.domain.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AppViewModel(
    private val authRepo: FireAuthRepository
) : ViewModel() {

    private val _userRole = MutableStateFlow<UserRole?>(null)
    val userRole: StateFlow<UserRole?> = _userRole.asStateFlow()

    private val _isResolving = MutableStateFlow(true)
    val isResolving: StateFlow<Boolean> = _isResolving.asStateFlow()

    init {
        viewModelScope.launch {
            val uid = authRepo.getCurrentUserId()
            if (uid != null) {
                _userRole.value = when (val result = authRepo.getUserProfile(uid)) {
                    is Resource.Success -> result.data.role
                    else -> UserRole.PATIENT
                }
            }
            _isResolving.value = false
        }
    }
}
