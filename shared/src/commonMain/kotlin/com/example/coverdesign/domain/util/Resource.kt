package com.example.coverdesign.domain.util

// shared/commonMain/domain/util/Resource.kt
sealed class Resource<out T> {
    data class Success<T>(val data: T) : Resource<T>()
    data class Error(val message: String, val cause: Throwable? = null) : Resource<Nothing>()
    object Loading : Resource<Nothing>()
}

inline fun <T> Resource<T>.onSuccess(action: (T) -> Unit): Resource<T> {
    if (this is Resource.Success) action(data)
    return this
}

inline fun <T> Resource<T>.onError(action: (String, Throwable?) -> Unit): Resource<T> {
    if (this is Resource.Error) action(message, cause)
    return this
}

inline fun <T, R> Resource<T>.mapSuccess(transform: (T) -> R): Resource<R> = when (this) {
    is Resource.Success -> Resource.Success(transform(data))
    is Resource.Error   -> this
    is Resource.Loading -> Resource.Loading
}