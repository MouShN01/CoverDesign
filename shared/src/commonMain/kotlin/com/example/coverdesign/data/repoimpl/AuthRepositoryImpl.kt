package com.example.coverdesign.data.repoimpl

// shared/commonMain/data/repoimpl/AuthRepositoryImpl.kt
import com.example.coverdesign.data.dto.LoginRequestDto
import com.example.coverdesign.data.dto.toDomain
import com.example.coverdesign.data.remote.interceptor.TokenStorage
import com.example.coverdesign.data.remote.service.AuthApiService
import com.example.coverdesign.domain.model.Credentials
import com.example.coverdesign.domain.model.Token
import com.example.coverdesign.domain.repo.AuthRepository
import com.example.coverdesign.domain.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class AuthRepositoryImpl(
    private val api: AuthApiService,
    private val tokenStorage: TokenStorage
) : AuthRepository {

    private val _isAuthenticated = MutableStateFlow(false)

    override suspend fun login(credentials: Credentials): Resource<Token> =
        try {
            val token = api.login(
                LoginRequestDto(credentials.email, credentials.password)
            ).toDomain()
            tokenStorage.saveToken(token)
            _isAuthenticated.value = true
            Resource.Success(token)
        } catch (e: Exception) {
            Resource.Error("Login failed: ${e.message}", e)
        }

    override suspend fun refreshToken(): Resource<Token> {
        // Ktor's Auth plugin handles this automatically (Step 3) —
        // this method exists for manual refresh triggers if needed
        val current = tokenStorage.getToken()
            ?: return Resource.Error("No token to refresh")
        return Resource.Success(current)
    }

    override suspend fun logout() {
        try { api.logout() } catch (_: Exception) { /* best-effort */ }
        tokenStorage.clearToken()
        _isAuthenticated.value = false
    }

    override fun isAuthenticated(): Flow<Boolean> = _isAuthenticated
}