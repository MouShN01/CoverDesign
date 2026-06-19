package com.example.coverdesign.data.repoimpl

import com.example.coverdesign.domain.model.ArSessionState
import com.example.coverdesign.domain.model.ProductConfig
import com.example.coverdesign.domain.model.TrackingState
import com.example.coverdesign.domain.repo.ArSessionRepository
import com.example.coverdesign.domain.util.Resource
import kotlinx.coroutines.flow.Flow

expect class ArSessionRepositoryImpl : ArSessionRepository {
    override fun isArSupported(): Boolean
    override suspend fun startSession(modelUrl: String): Resource<Unit>
    override suspend fun stopSession()
    override fun observeTrackingState(): Flow<TrackingState>
    override fun observeSessionState(): Flow<ArSessionState>
    override suspend fun updateOverlayConfig(config: ProductConfig)
    override suspend fun captureSnapshot(): Resource<ByteArray>
}