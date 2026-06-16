package com.example.coverdesign.data.remote.interceptor

import com.example.coverdesign.domain.model.Token

// data/remote/interceptor/TokenStorage.kt
// placeholder — will be replaced with EncryptedStorage in security step
interface TokenStorage {
    suspend fun getToken(): Token?
    suspend fun saveToken(token: Token)
    suspend fun clearToken()
}