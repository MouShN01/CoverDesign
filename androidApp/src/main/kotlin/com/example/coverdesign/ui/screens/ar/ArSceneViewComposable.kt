package com.example.coverdesign.ui.screens.ar

// androidApp/ui/screens/ar/ArSceneViewComposable.kt

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.coverdesign.domain.model.TrackingState
import com.google.ar.core.Anchor
import com.google.ar.core.Plane
import io.github.sceneview.ar.ARSceneView
import io.github.sceneview.ar.arcore.getUpdatedPlanes
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberModelInstance
import io.github.sceneview.rememberModelLoader

// androidApp/ui/screens/ar/ArSceneViewComposable.kt
// androidApp/ui/screens/ar/ArSceneViewComposable.kt
@Composable
fun ArSceneViewComposable(
    modelUrl   : String,
    onTracking : (TrackingState) -> Unit,
    modifier   : Modifier = Modifier
) {
    val engine      = rememberEngine()
    val modelLoader = rememberModelLoader(engine)

    val modelInstance = rememberModelInstance(
        modelLoader  = modelLoader,
        fileLocation = modelUrl
    )

    var anchor by remember { mutableStateOf<Anchor?>(null) }

    //var modelInstance by remember { mutableStateOf<ModelInstance?>(null) }
    var bodyAnchor by remember { mutableStateOf<Anchor?>(null) }

    ARSceneView(
        onSessionUpdated = { _, frame ->

            if (anchor == null &&
                frame.camera.trackingState ==  com.google.ar.core.TrackingState.TRACKING
            ) {

                frame.getUpdatedPlanes()
                    .firstOrNull {
                        it.type == Plane.Type.HORIZONTAL_UPWARD_FACING
                    }
                    ?.let { plane ->
                        anchor = plane.createAnchor(plane.centerPose)
                    }
            }
        }
//        modifier      = modifier,
//        engine        = engine,
//        modelLoader   = modelLoader,
//        planeRenderer = true,
//        onSessionUpdated = { _, frame ->
//            val tracking = when (frame.camera.trackingState) {
//                com.google.ar.core.TrackingState.TRACKING -> TrackingState.TRACKING
//                com.google.ar.core.TrackingState.PAUSED   -> TrackingState.LOST
//                com.google.ar.core.TrackingState.STOPPED  -> TrackingState.NOT_AVAILABLE
//                else -> TrackingState.NOT_AVAILABLE
//            }
//            onTracking(tracking)
//
//            if (anchor == null) {
//                anchor = frame.getUpdatedPlanes()
//                    .firstOrNull { it.type == Plane.Type.HORIZONTAL_UPWARD_FACING }
//                    ?.let { plane ->
//                        // FIX: call createAnchorOrNull on the Plane (Trackable),
//                        // NOT on Frame — Frame.createAnchorOrNull doesn't exist
//                        // (sceneview/sceneview#2525)
//                        plane.createAnchorOrNull(plane.centerPose)
//                    }
//            }
//        }
    ) {

        anchor?.let { anchor ->
            AnchorNode(anchor) {
                modelInstance?.let { model ->
                    ModelNode(
                        modelInstance = model
                    )
                }
            }
        }

//
//        anchor?.let { resolvedAnchor ->
//            AnchorNode(anchor = resolvedAnchor) {
//                modelInstance?.let {
//                    ModelNode(modelInstance = it, scaleToUnits = 0.3f, autoAnimate = true)
//                }
//            }
//        }
    }
}
//
//@Composable
//fun ArSceneViewComposable(
//    modelUrl     : String,
//    onTracking   : (TrackingState) -> Unit,
//    modifier     : Modifier = Modifier
//) {
//    val engine      = rememberEngine()
//    val modelLoader = rememberModelLoader(engine)
//
//    // load model from URL — null while downloading
//    val modelInstance = rememberModelInstance(
//        modelLoader  = modelLoader,
//        fileLocation = modelUrl
//    )
//
//    // anchor placed on first detected horizontal plane
//    var anchor by remember { mutableStateOf<Anchor?>(null) }
//
//    Box(modifier = modifier) {
//        ARSceneView(
//            modifier      = Modifier.fillMaxSize(),
//            engine        = engine,
//            modelLoader   = modelLoader,
//            planeRenderer = true,
//            onSessionUpdated = { _, frame ->
//                // map ARCore tracking state to our domain enum
//                val tracking = when (frame.camera.trackingState) {
//                    com.google.ar.core.TrackingState.TRACKING ->
//                        TrackingState.TRACKING
//                    com.google.ar.core.TrackingState.PAUSED   ->
//                        TrackingState.LOST
//                    com.google.ar.core.TrackingState.STOPPED  ->
//                        TrackingState.NOT_AVAILABLE
//                    else -> TrackingState.NOT_AVAILABLE
//                }
//                onTracking(tracking)
//
//                // TODO auto-anchor on first upward-facing horizontal plane
////                if (anchor == null) {
////                    anchor = frame.getUpdatedPlanes()
////                        .firstOrNull {
////                            it.type == Plane.Type.HORIZONTAL_UPWARD_FACING
////                        }
////                        ?.let { frame.createAnchorOrNull(it.centerPose) }
////                }
//            }
//        ) {
//            anchor?.let { resolvedAnchor ->
//                AnchorNode(anchor = resolvedAnchor) {
//                    modelInstance?.let {
//                        ModelNode(
//                            modelInstance = it,
//                            scaleToUnits  = 0.3f,    // ~30cm — prosthetic scale
//                            autoAnimate   = true
//                        )
//                    }
//                }
//            }
//        }
//
//        // overlay UI on top of AR view
//        Column(
//            modifier            = Modifier
//                .align(Alignment.TopCenter)
//                .padding(top = 24.dp),
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            // model loading indicator
//            if (modelInstance == null) {
//                Surface(
//                    shape = RoundedCornerShape(20.dp),
//                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
//                ) {
//                    Row(
//                        modifier            = Modifier.padding(
//                            horizontal = 16.dp, vertical = 8.dp
//                        ),
//                        verticalAlignment   = Alignment.CenterVertically,
//                        horizontalArrangement = Arrangement.spacedBy(8.dp)
//                    ) {
//                        CircularProgressIndicator(Modifier.size(14.dp), strokeWidth = 2.dp)
//                        Text("Loading model…",
//                            style = MaterialTheme.typography.labelMedium)
//                    }
//                }
//            }
//
//            // placement hint — shown when model ready but not yet anchored
//            if (modelInstance != null && anchor == null) {
//                Spacer(Modifier.height(8.dp))
//                Surface(
//                    shape = RoundedCornerShape(20.dp),
//                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
//                ) {
//                    Text(
//                        text     = "Point camera at a flat surface",
//                        modifier = Modifier.padding(
//                            horizontal = 16.dp, vertical = 8.dp
//                        ),
//                        style    = MaterialTheme.typography.labelMedium
//                    )
//                }
//            }
//        }
//    }
//}