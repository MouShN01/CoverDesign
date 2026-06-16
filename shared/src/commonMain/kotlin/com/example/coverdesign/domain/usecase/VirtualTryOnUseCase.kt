package com.example.coverdesign.domain.usecase

import com.example.coverdesign.domain.model.ArSessionState
import com.example.coverdesign.domain.model.ProductConfig
import com.example.coverdesign.domain.model.TrackingState
import com.example.coverdesign.domain.model.TryOnResult
import com.example.coverdesign.domain.repo.ArSessionRepository
import com.example.coverdesign.domain.repo.Model3DRepository
import com.example.coverdesign.domain.util.Resource
import kotlinx.coroutines.flow.Flow

// shared/commonMain/domain/usecase/VirtualTryOnUseCase.kt   FR6
class VirtualTryOnUseCase(
    private val arRepo: ArSessionRepository,
    private val model3DRepo: Model3DRepository
) {
    fun isSupported(): Boolean = arRepo.isArSupported()

    suspend fun start(configId: String): Resource<Unit> {
        val modelUrlResult = model3DRepo.getModelUrl(configId)
        return when (modelUrlResult) {
            is Resource.Success -> arRepo.startSession(modelUrlResult.data)
            is Resource.Error   -> modelUrlResult
            is Resource.Loading -> Resource.Loading
        }
    }

    fun observeTracking(): Flow<TrackingState> = arRepo.observeTrackingState()
    fun observeSession(): Flow<ArSessionState> = arRepo.observeSessionState()

    suspend fun updateConfig(config: ProductConfig) =
        arRepo.updateOverlayConfig(config)

    suspend fun capture(): Resource<ByteArray> = arRepo.captureSnapshot()

    suspend fun stop() = arRepo.stopSession()
}