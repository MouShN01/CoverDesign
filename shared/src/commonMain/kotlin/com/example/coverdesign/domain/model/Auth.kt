package com.example.coverdesign.domain.model

import com.example.coverdesign.domain.util.currentEpochMillis
import kotlin.time.Clock
import kotlin.time.TimeSource


data class Credentials(
    val email: String,
    val password: String
)

data class Token(
    val accessToken: String,
    val refreshToken: String,
    val expiresAt: Long             // epoch millis
) {
    fun isExpired(): Boolean =
        currentEpochMillis() > expiresAt - 60_000L  // 1 min buffer
}