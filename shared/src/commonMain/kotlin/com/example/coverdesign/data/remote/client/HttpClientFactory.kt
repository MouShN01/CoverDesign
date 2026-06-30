package com.example.coverdesign.data.remote.client

// data/remote/client/HttpClientFactory.kt
import com.example.coverdesign.data.remote.interceptor.TokenStorage
import com.example.coverdesign.domain.model.Token
import com.example.coverdesign.domain.util.getPlatformName
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

fun createHttpClient(tokenStorage: TokenStorage): HttpClient = HttpClient {

    // ── JSON ────────────────────────────────────────────────────────────────
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true   // safe: backend can add fields freely
            isLenient = true
            explicitNulls = false
        })
    }

//     ── Logging (debug only) ────────────────────────────────────────────────
    install(Logging) {
        level = LogLevel.ALL
       // level  = LogLevel.BODY        // switch to NONE for release builds
    }

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
                println("Refreshing Firebase token...")
                val user = Firebase.auth.currentUser
                    ?: run { tokenStorage.clearToken(); return@refreshTokens null }

               // val mUser: FirebaseUser = FirebaseAuth.getInstance().getCurrentUser()
//                user.getIdToken(true)
//                    .addOnCompleteListener(object : OnCompleteListener<GetTokenResult?>() {
//                        fun onComplete(@NonNull task: Task<GetTokenResult?>) {
//                            if (task.isSuccessful()) {
//                                val idToken: String? = task.getResult().getToken()
//                                // Send token to your backend via HTTPS
//                                // ...
//                            } else {
//                                // Handle error -> task.getException();
//                            }
//                        }
//                    })
                val freshToken = user.getIdToken(forceRefresh = false) //was true
                    ?: run { tokenStorage.clearToken(); return@refreshTokens null }

                val newToken = Token(accessToken = freshToken, refreshToken = "")
                tokenStorage.saveToken(newToken)

                BearerTokens(
                    accessToken  = freshToken,
                    refreshToken = ""   // Firebase doesn't use refresh tokens directly
                )
            }
//            refreshTokens {
//                // called automatically by Ktor on 401
//                val current = tokenStorage.getToken()
//                    ?: return@refreshTokens null
//
//                val response = client.post(ApiConstants.Endpoints.REFRESH) {
//                    setBody(RefreshRequestDto(current.refreshToken))
//                    markAsRefreshTokenRequest()     // prevents infinite loop
//                }
//
//                if (response.status.value == 200) {
//                    val newTokenDto = response.body<TokenDto>()
//                    val newToken = newTokenDto.toDomain()
//                    tokenStorage.saveToken(newToken)
//                    BearerTokens(newToken.accessToken, newToken.refreshToken)
//                } else {
//                    tokenStorage.clearToken()       // force re-login
//                    null
//                }
//            }
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