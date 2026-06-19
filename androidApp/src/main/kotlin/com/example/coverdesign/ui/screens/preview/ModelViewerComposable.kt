package com.example.coverdesign.ui.screens.preview

// androidApp/ui/screens/preview/ModelViewerComposable.kt
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.sceneview.SceneView
import io.github.sceneview.createEnvironment
import io.github.sceneview.math.Position
import io.github.sceneview.model.ModelInstance
import io.github.sceneview.node.ModelNode
import io.github.sceneview.rememberCameraManipulator
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberEnvironment
import io.github.sceneview.rememberEnvironmentLoader
import io.github.sceneview.rememberModelInstance
import io.github.sceneview.rememberModelLoader

@Composable
fun ModelViewerComposable(
    modelUrl : String,
    modifier : Modifier = Modifier
) {
    val engine              = rememberEngine()
    val modelLoader         = rememberModelLoader(engine)
    val environmentLoader   = rememberEnvironmentLoader(engine)

    // load model instance from URL — null while loading
    val modelInstance = rememberModelInstance(
        modelLoader  = modelLoader,
        fileLocation = modelUrl
    )

    Box(modifier = modifier) {
        SceneView(
            modifier            = Modifier.fillMaxSize(),
            engine              = engine,
            modelLoader         = modelLoader,
            cameraManipulator   = rememberCameraManipulator(),
            environment         = rememberEnvironment(environmentLoader) {
                environmentLoader.createHDREnvironment("envs/studio.hdr")
                    ?: createEnvironment(environmentLoader)
            }
        ) {
            modelInstance?.let {
                ModelNode(
                    modelInstance = it,
                    scaleToUnits  = 1.0f,
                    autoAnimate   = true
                )
            }
        }

        // loading overlay — shown while modelInstance is null
        if (modelInstance == null) {
            Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator()
                    Text(
                        "Loading 3D model…",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // drag hint — shown once model is ready
        if (modelInstance != null) {
            Text(
                text     = "Drag to rotate",
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp),
                style    = MaterialTheme.typography.labelSmall,
                color    = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ModelViewer(
    modelInstance: ModelInstance,
    modifier : Modifier = Modifier
) {
    var modelNode by remember { mutableStateOf<ModelNode?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error     by remember { mutableStateOf<String?>(null) }

    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)
    val environmentLoader = rememberEnvironmentLoader(engine)

    Box(modifier = modifier) {
                SceneView().apply {
                    // transparent background so theme shows through
                    //setBackgroundColor(android.graphics.Color.TRANSPARENT) TOdO

                    // load glTF/glb model from URL
                    // val coroutineScope = CoroutineScope(Dispatchers.Main)
                    LaunchedEffect(Unit){
                        try {
                            val node = ModelNode(
                                modelInstance = modelInstance,
                                scaleToUnits  = 1.0f,        // auto-scale to 1 metre
                                centerOrigin  = Position(y = -1.0f)
                            )
                            //addChild(node)
                            modelNode = node
                            isLoading = false

                            // enable rotation gesture
                            node.isEditable = true
                        } catch (e: Exception) {
                            error     = e.message
                            isLoading = false
                        }
                    }
                }

        // loading overlay
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(Modifier.height(8.dp))
                    Text("Loading 3D model…",
                        style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        // error state
        error?.let {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(it, color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall)
            }
        }

        // rotation hint
        if (!isLoading && error == null) {
            Text(
                text     = "Drag to rotate",
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp),
                style    = MaterialTheme.typography.labelSmall,
                color    = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}