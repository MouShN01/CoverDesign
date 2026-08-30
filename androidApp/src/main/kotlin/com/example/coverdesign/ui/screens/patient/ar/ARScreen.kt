package com.example.coverdesign.ui.screens.patient.ar

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Modifier
import com.google.ar.core.Anchor
import com.google.ar.core.Frame
import com.google.ar.core.Plane
import com.google.ar.core.TrackingFailureReason
import com.google.ar.core.TrackingState
import io.github.sceneview.ar.ARSceneView
import io.github.sceneview.ar.rememberARCameraNode
import io.github.sceneview.model.ModelInstance
import io.github.sceneview.rememberCollisionSystem
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.rememberView


@Composable
fun ARScreen(
    modelUrl: String?
) {
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
//        bottomBar = { (navController = navController) }
    ) { contentPadding ->
        Box(
            contentAlignment = Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
        ) {
            ARSceneDisplay(modelUrl)
        }
    }
}

@Composable
fun ARSceneDisplay(modelUrl: String?) {
    if (modelUrl.isNullOrBlank()) {
        Log.e("ARDebug", "modelUrl is null/blank")
        return
    }

    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)
    val cameraNode = rememberARCameraNode(engine)
    val view = rememberView(engine)
    val collisionSystem = rememberCollisionSystem(view)
    var planeRenderer by remember { mutableStateOf(true) }

    var trackingFailureReason by remember {
        mutableStateOf<TrackingFailureReason?>(null)
    }
    var frame by remember { mutableStateOf<Frame?>(null) }

    var modelInstance by remember { mutableStateOf<ModelInstance?>(null) }
    var anchor by remember { mutableStateOf<Anchor?>(null) }

    LaunchedEffect(modelUrl) {
        Log.d("ARDebug", "Loading model: $modelUrl")
        modelInstance = modelLoader.loadModelInstance(modelUrl)
        Log.d("ARDebug", "Model loaded: $modelInstance")
    }

    Log.d("ARDebug", "Recomposing: anchor=$anchor, modelInstance=$modelInstance")
    val currentAnchor = anchor
    val currentModel = modelInstance

    ARSceneView(
        modifier = Modifier.fillMaxSize(),
        engine = engine,
        modelLoader = modelLoader,
        cameraNode = cameraNode,
        view = view,
        collisionSystem = collisionSystem,
        planeRenderer = planeRenderer,
        onSessionUpdated = { _, frame ->
            if (anchor == null && frame.camera.trackingState == TrackingState.TRACKING) {
                val screenWidth = frame.camera.imageIntrinsics.imageDimensions[0]
                val screenHeight = frame.camera.imageIntrinsics.imageDimensions[1]
                frame.hitTest(screenWidth / 2f, screenHeight / 2f)
                    .firstOrNull { hit ->
                        val trackable = hit.trackable
                        trackable is Plane &&
                                trackable.isPoseInPolygon(hit.hitPose) &&
                                trackable.type == Plane.Type.HORIZONTAL_UPWARD_FACING
                    }
                    ?.let { hit ->
                        anchor = hit.createAnchor()
                        Log.d("ARDebug", "Anchor created at hit: $anchor")
                    }
            }
        }
    ) {
        Log.d("ARDebug", "Content block: anchor=$currentAnchor, model=$currentModel")

        if (currentAnchor != null && currentModel != null) {
            Log.d("ARDebug", "Adding AnchorNode + ModelNode")
            io.github.sceneview.ar.node.AnchorNode(engine = engine, anchor = currentAnchor) {
                io.github.sceneview.node.ModelNode(
                    modelInstance = currentModel,
                    scaleToUnits = 0.5f
                )
            }
        }
    }
}

//    ARSceneView(
//        modifier = Modifier.fillMaxSize(),
//        planeRenderer = true,
//        onSessionUpdated = { _, frame ->
//            if (anchor == null) {
//                anchor =
//                    frame.getUpdatedPlanes()
//                        .firstOrNull {
//                            it.type == Plane.Type.HORIZONTAL_UPWARD_FACING
//                        }
//                        ?.let {
//                            it.createAnchorOrNull(it.centerPose)
//                        }
////
////                anchor = frame.getUpdatedPlanes()
////                    .firstOrNull { it.type == Plane.Type.HORIZONTAL_UPWARD_FACING }
////                    ?.let { frame.createAnchorOrNull(it.centerPose) }
//            }
//        }
//    ) {
//        anchor?.let {
//            io.github.sceneview.ar.node.AnchorNode(anchor = it, engine = engine) {
//                io.github.sceneview.node.ModelNode(modelInstance = modelInstance!!, scaleToUnits = 0.5f)
//            }
//        }
//    }
//}