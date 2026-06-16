package com.example.coverdesign.data.remote.client

// data/remote/client/HttpClientFactory.kt
import com.example.coverdesign.data.dto.RefreshRequestDto
import com.example.coverdesign.data.dto.TokenDto
import com.example.coverdesign.data.dto.toDomain
import com.example.coverdesign.data.remote.interceptor.TokenStorage
import com.example.coverdesign.domain.util.getPlatformName
import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.koin.core.logger.Logger

fun createHttpClient(tokenStorage: TokenStorage): HttpClient = HttpClient {

    // ── JSON ────────────────────────────────────────────────────────────────
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true   // safe: backend can add fields freely
            isLenient = true
            explicitNulls = false
        })
    }

    // ── Logging (debug only) ────────────────────────────────────────────────
//    install(Logging) {
//        logger = Logger.DEFAULT
//        level  = LogLevel.BODY        // switch to NONE for release builds
//    }

    // ── Timeouts ────────────────────────────────────────────────────────────
    install(HttpTimeout) {
        requestTimeoutMillis  = 30_000
        connectTimeoutMillis  = 15_000
        socketTimeoutMillis   = 30_000
    }

    // ── Auth: bearer token + silent refresh ─────────────────────────────────
    install(Auth) {
        bearer {
            loadTokens {
                val token = tokenStorage.getToken() ?: return@loadTokens null
                BearerTokens(
                    accessToken  = token.accessToken,
                    refreshToken = token.refreshToken
                )
            }
            refreshTokens {
                // called automatically by Ktor on 401
                val current = tokenStorage.getToken()
                    ?: return@refreshTokens null

                val response = client.post(ApiConstants.Endpoints.REFRESH) {
                    setBody(RefreshRequestDto(current.refreshToken))
                    markAsRefreshTokenRequest()     // prevents infinite loop
                }

                if (response.status.value == 200) {
                    val newTokenDto = response.body<TokenDto>()
                    val newToken = newTokenDto.toDomain()
                    tokenStorage.saveToken(newToken)
                    BearerTokens(newToken.accessToken, newToken.refreshToken)
                } else {
                    tokenStorage.clearToken()       // force re-login
                    null
                }
            }
        }
    }

    // ── Default request config ───────────────────────────────────────────────
    defaultRequest {
        url(ApiConstants.BASE_URL)
        headers.append("Accept",       "application/json")
        headers.append("Content-Type", "application/json")
        headers.append("X-Platform",   getPlatformName())   // expect/actual below
    }
}