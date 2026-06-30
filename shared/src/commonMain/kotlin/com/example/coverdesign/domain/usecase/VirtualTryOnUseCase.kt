package com.example.coverdesign.domain.usecase

import com.example.coverdesign.domain.ar.ArCapability
import com.example.coverdesign.domain.model.AdaptedModel3D
import com.example.coverdesign.domain.repo.Model3DRepository
import com.example.coverdesign.domain.util.Resource

// shared/commonMain/domain/usecase/VirtualTryOnUseCase.kt   FR6

// shared/commonMain/domain/usecase/VirtualTryOnUseCase.kt   FR6
class VirtualTryOnUseCase(
    private val model3DRepo: Model3DRepository
) {
    suspend fun loadModelForTryOn(configId: String): Resource<AdaptedModel3D?> =
        model3DRepo.getAdaptedModel(configId)

    fun isArSupported(): Boolean = ArCapability.isSupported()
}
//class VirtualTryOnUseCase(
//    private val arRepo: ArSessionRepository,
//    private val model3DRepo: Model3DRepository,
//) {
//    fun isSupported(): Boolean = arRepo.isArSupported()
//
//    suspend fun start(configId: String): Resource<Unit> {
//        val modelUrlResult = model3DRepo.getAdaptedModel(configId)
//        return when (modelUrlResult) {
//            is Resource.Success -> arRepo.startSession(modelUrlResult.data.modelUrl)
//            is Resource.Error   -> modelUrlResult
//            is Resource.Loading -> Resource.Loading
//        }
//    }
//
//    fun observeTracking(): Flow<TrackingState> = arRepo.observeTrackingState()
//    fun observeSession(): Flow<ArSessionState> = arRepo.observeSessionState()
//
//    suspend fun updateConfig(config: ProductConfig) =
//        arRepo.updateOverlayConfig(config)
//
//    suspend fun capture(): Resource<ByteArray> = arRepo.captureSnapshot()
//
//    suspend fun stop() = arRepo.stopSession()
//}