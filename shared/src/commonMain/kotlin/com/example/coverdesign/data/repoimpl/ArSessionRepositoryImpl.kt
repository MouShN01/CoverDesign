package com.example.coverdesign.data.repoimpl

// shared/commonMain/data/repoimpl/ArSessionRepositoryImpl.kt
// Real implementation in Step 9 (ARCore/ARKit expect-actual).
// This placeholder lets the app compile and run end-to-end now.
import com.example.coverdesign.domain.model.ArSessionState
import com.example.coverdesign.domain.model.ProductConfig
import com.example.coverdesign.domain.model.TrackingState
import com.example.coverdesign.domain.repo.ArSessionRepository
import com.example.coverdesign.domain.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.Flow

class ArSessionRepositoryImpl : ArSessionRepository {

    private val _sessionState = MutableStateFlow<ArSessionState>(ArSessionState.Idle)
    private val _trackingState = MutableStateFlow(TrackingState.NOT_AVAILABLE)

    override fun isArSupported(): Boolean = false  // overridden per-platform in Step 9

    override suspend fun startSession(modelUrl: String): Resource<Unit> {
        _sessionState.value = ArSessionState.Error("AR not yet implemented")
        return Resource.Error("AR not yet implemented — coming in Step 9")
    }

    override suspend fun stopSession() {
        _sessionState.value = ArSessionState.Idle
    }

    override fun observeTrackingState(): Flow<TrackingState> = _trackingState
    override fun observeSessionState(): Flow<ArSessionState> = _sessionState

    override suspend fun updateOverlayConfig(config: ProductConfig) {
        // no-op until Step 9
    }

    override suspend fun captureSnapshot(): Resource<ByteArray> =
        Resource.Error("AR not yet implemented")
}