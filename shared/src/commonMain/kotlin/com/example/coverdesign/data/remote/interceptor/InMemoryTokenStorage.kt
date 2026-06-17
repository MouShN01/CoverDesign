package com.example.coverdesign.data.remote.interceptor

import com.example.coverdesign.domain.model.Token

class InMemoryTokenStorage : TokenStorage {
    private var token: Token? = null

    override suspend fun getToken(): Token? = token
    override suspend fun saveToken(token: Token) { this.token = token }
    override suspend fun clearToken() { token = null }
}