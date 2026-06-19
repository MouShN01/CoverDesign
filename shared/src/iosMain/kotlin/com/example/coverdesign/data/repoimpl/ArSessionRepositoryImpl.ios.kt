package com.example.coverdesign.data.repoimpl

import com.example.coverdesign.domain.model.ArSessionState
import com.example.coverdesign.domain.model.ProductConfig
import com.example.coverdesign.domain.model.TrackingState
import com.example.coverdesign.domain.repo.ArSessionRepository
import com.example.coverdesign.domain.util.Resource
import kotlinx.coroutines.flow.Flow

actual class ArSessionRepositoryImpl : ArSessionRepository {
    actual override fun isArSupported(): Boolean {
        TODO("Not yet implemented")
    }

    actual override suspend fun startSession(modelUrl: String): Resource<Unit> {
        TODO("Not yet implemented")
    }

    actual override suspend fun stopSession() {
    }

    actual override fun observeTrackingState(): Flow<TrackingState> {
        TODO("Not yet implemented")
    }

    actual override fun observeSessionState(): Flow<ArSessionState> {
        TODO("Not yet implemented")
    }

    actual override suspend fun updateOverlayConfig(config: ProductConfig) {
    }

    actual override suspend fun captureSnapshot(): Resource<ByteArray> {
        TODO("Not yet implemented")
    }
}