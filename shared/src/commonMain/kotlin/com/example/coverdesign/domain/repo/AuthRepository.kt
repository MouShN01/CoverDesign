package com.example.coverdesign.domain.repo

import com.example.coverdesign.domain.model.Credentials
import com.example.coverdesign.domain.model.Token
import com.example.coverdesign.domain.util.Resource
import kotlinx.coroutines.flow.Flow

// shared/commonMain/domain/repo/AuthRepository.kt
// placeholder — full impl later
interface AuthRepository {
    suspend fun login(credentials: Credentials): Resource<Token>
    suspend fun refreshToken(): Resource<Token>
    suspend fun logout()
    fun isAuthenticated(): Flow<Boolean>
}