package com.example.coverdesign.data.repoimpl

import android.content.Context
import com.example.coverdesign.domain.ar.ArCapability
import com.example.coverdesign.domain.model.ArSessionState
import com.example.coverdesign.domain.model.ProductConfig
import com.example.coverdesign.domain.model.TrackingState
import com.example.coverdesign.domain.repo.ArSessionRepository
import com.example.coverdesign.domain.util.Resource
import io.github.sceneview.math.Color
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

actual class ArSessionRepositoryImpl(
    private val context: Context
) : ArSessionRepository {

    private val _sessionState  = MutableStateFlow<ArSessionState>(ArSessionState.Idle)
    private val _trackingState = MutableStateFlow(TrackingState.NOT_AVAILABLE)

    // reference held so the ViewModel can call updateOverlayConfig live
    //private var sceneViewRef: SceneView? = null // TODO fix with snapshot
//    private var currentModelNode: ARModelNode? = null

    actual override fun isArSupported(): Boolean = ArCapability.isSupported()

    actual override suspend fun startSession(modelUrl: String): Resource<Unit> {
        return try {
            _sessionState.value = ArSessionState.LoadingModel
            // sceneViewRef is set from the Composable via setSceneView()
            // model loading happens inside the ArSceneViewComposable below
            _sessionState.value = ArSessionState.Active(TrackingState.INITIALIZING)
            Resource.Success(Unit)
        } catch (e: Exception) {
            _sessionState.value = ArSessionState.Error(e.message ?: "AR start failed")
            Resource.Error(e.message ?: "AR start failed", e)
        }
    }

    actual override suspend fun stopSession() {
//        currentModelNode = null
//        sceneViewRef = null
        _sessionState.value = ArSessionState.Idle
        _trackingState.value = TrackingState.NOT_AVAILABLE
    }

    actual override fun observeTrackingState(): Flow<TrackingState> = _trackingState
    actual override fun observeSessionState(): Flow<ArSessionState> = _sessionState

    actual override suspend fun updateOverlayConfig(config: ProductConfig) {
        TODO()
        // update material/color/texture on the live model node
//        currentModelNode?.let { node ->
//            // SceneView material API — applied in real time
//            node.setMaterialColor(
//                materialName = "base",
//                color        = parseHexColor(config.color.hex)
//            )
//        }
    }
    //private var sceneView: SceneView? = null

//    fun attachSceneView(view: SceneView) {
//        sceneView = view
//    }

    actual override suspend fun captureSnapshot(): Resource<ByteArray> {
        return try {
            TODO()
//            val bitmap = sceneViewRef?.
//                ?: return Resource.Error("Scene not available")
//            val stream = java.io.ByteArrayOutputStream()
//            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, stream)
//            Resource.Success(stream.toByteArray())
        } catch (e: Exception) {
            Resource.Error("Snapshot failed: ${e.message}", e)
        }
    }

    // called by the Composable to give repo access to the live SceneView
    //fun setSceneView(view: ARSceneView) { sceneViewRef = view }

    fun updateTrackingState(state: TrackingState) {
        _trackingState.value = state
        if (_sessionState.value is ArSessionState.Active) {
            _sessionState.value = ArSessionState.Active(state)
        }
    }

//    fun setModelNode(node: ModelNode) {
//        currentModelNode = node
//    }

    private fun parseHexColor(hex: String): Color {
        TODO()
//        val color = hex.ifEmpty { "#000000" }.toColorInt()
//        return io.github.sceneview.math.Color(
//            r = android.graphics.Color.red(color) / 255f,
//            g = android.graphics.Color.green(color) / 255f,
//            b = android.graphics.Color.blue(color) / 255f,
//            a = 1f,
//            x = TODO(),
//            y = TODO(),
//            z = TODO(),
//            w = TODO()
//        )
    }
}