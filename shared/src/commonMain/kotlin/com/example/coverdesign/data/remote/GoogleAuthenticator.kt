package com.example.coverdesign.data.remote


expect class GoogleAuthenticator {
    suspend fun getIdToken(context: Any): String
}
