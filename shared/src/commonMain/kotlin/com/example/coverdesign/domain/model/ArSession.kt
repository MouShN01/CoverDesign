package com.example.coverdesign.domain.model

// shared/commonMain/domain/model/ArSession.kt
enum class TrackingState {
    NOT_AVAILABLE, INITIALIZING, TRACKING, LOST
}

sealed class ArSessionState {
    object Idle : ArSessionState()
    object LoadingModel : ArSessionState()
    data class Active(val trackingState: TrackingState) : ArSessionState()
    data class Error(val message: String) : ArSessionState()
}