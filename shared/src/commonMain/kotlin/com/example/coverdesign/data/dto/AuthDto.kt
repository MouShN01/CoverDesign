package com.example.coverdesign.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// data/dto/AuthDto.kt
@kotlinx.serialization.Serializable
data class LoginRequestDto(
    @SerialName("email")    val email: String,
    @SerialName("password") val password: String
)

@kotlinx.serialization.Serializable
data class TokenDto(
    @SerialName("access_token")  val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String,
    @SerialName("expires_at")    val expiresAt: Long
)

@Serializable
data class RefreshRequestDto(
    @SerialName("refresh_token") val refreshToken: String
)