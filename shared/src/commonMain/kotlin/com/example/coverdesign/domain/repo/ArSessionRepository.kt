package com.example.coverdesign.domain.repo

import com.example.coverdesign.domain.model.ArSessionState
import com.example.coverdesign.domain.model.ProductConfig
import com.example.coverdesign.domain.model.TrackingState
import com.example.coverdesign.domain.util.Resource
import kotlinx.coroutines.flow.Flow

interface ArSessionRepository {
    fun isArSupported(): Boolean

    suspend fun startSession(modelUrl: String): Resource<Unit>
    suspend fun stopSession()

    fun observeTrackingState(): Flow<TrackingState>
    fun observeSessionState(): Flow<ArSessionState>

    suspend fun updateOverlayConfig(config: ProductConfig)
    suspend fun captureSnapshot(): Resource<ByteArray>  // user can save/share the AR photo
}