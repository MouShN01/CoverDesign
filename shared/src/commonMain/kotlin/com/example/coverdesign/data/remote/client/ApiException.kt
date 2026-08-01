package com.example.coverdesign.data.remote.client

sealed class ApiException(message: String) : Exception(message) {
    class Unauthorized(message: String = "Session expired, please log in again") : ApiException(message)
    class Forbidden(message: String = "Access denied") : ApiException(message)
    class NotFound(message: String = "Resource not found") : ApiException(message)
    class UnprocessableEntity(message: String) : ApiException(message)
    class ServerError(val code: Int, message: String) : ApiException(message)
    class Unknown(val code: Int, message: String) : ApiException(message)
}
