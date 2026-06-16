package com.example.coverdesign.data.remote.service

import com.example.coverdesign.data.dto.LoginRequestDto
import com.example.coverdesign.data.dto.TokenDto
import com.example.coverdesign.data.remote.client.ApiConstants
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody

// data/remote/service/AuthApiService.kt
class AuthApiService(private val client: HttpClient) {

    suspend fun login(request: LoginRequestDto): TokenDto =
        client.post(ApiConstants.Endpoints.LOGIN) {
            setBody(request)
        }.body()

    suspend fun logout() {
        client.post(ApiConstants.Endpoints.LOGOUT)
    }
}