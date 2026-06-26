package com.example.coverdesign.ui.screens.ar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.coverdesign.ui.components.AppTopBar
import com.example.coverdesign.ui.components.ErrorScreen
import com.example.coverdesign.ui.components.EventHandler
import com.example.coverdesign.ui.components.LoadingScreen
import com.example.coverdesign.ui.viewmodel.ArTryOnAction
import com.example.coverdesign.ui.viewmodel.ArTryOnUiEvent
import com.example.coverdesign.ui.viewmodel.ArTryOnViewModel
import org.koin.compose.viewmodel.koinViewModel

// androidApp/ui/screens/ar/ArTryOnScreen.kt
@Composable
fun ArTryOnScreen(
    configId           : String,
    viewModel           : ArTryOnViewModel = koinViewModel(),
    onProceedToOrder     : () -> Unit,
    onUpdateParameters     : () -> Unit,
    onBack                  : () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(configId) {
        viewModel.onAction(ArTryOnAction.Load(configId))
    }

    EventHandler(viewModel.events) { event ->
        when (event) {
            is ArTryOnUiEvent.NavigateToOrder      -> onProceedToOrder()
            is ArTryOnUiEvent.NavigateToConfigEdit -> onUpdateParameters()
        }
    }
    val graphicsLayer = rememberGraphicsLayer()

    Scaffold(topBar = { AppTopBar(title = "AR Try-On", onBack = onBack) }) { padding ->
        Box(Modifier.fillMaxSize().drawWithContent {
            graphicsLayer.record { this@drawWithContent.drawContent() }
            drawLayer(graphicsLayer)
        }.padding(padding)) {
            when {
                !state.isArSupported -> ErrorScreen("AR is not supported on this device")
                state.isLoading || state.modelUrl == null -> LoadingScreen()
                else -> ArSceneViewComposable(
                    modelUrl   = state.modelUrl!!,
                    onTracking = { viewModel.onAction(ArTryOnAction.UpdateTracking(it)) },
                    modifier   = Modifier.fillMaxSize()
                )
            }

            if (state.modelUrl != null) {
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 24.dp)
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
//                    IconButton(onClick = { TODO add button fro screenshot
//                        coroutineScope.launch {
//                            val bitmap = graphicsLayer.toImageBitmap()
//                            // save bitmap to MediaStore / app-private storage here
//                        }
//                    }) {
//                        Icon(Icons.Default.CameraAlt, contentDescription = "Capture")
//                    }
                    OutlinedButton(
                        onClick = { viewModel.onAction(ArTryOnAction.UpdateParameters(configId)) },
                        colors  = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border  = BorderStroke(1.dp, Color.White)
                    ) {
                        Icon(Icons.Default.Tune, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Update parameters")
                    }
                    Button(onClick = { viewModel.onAction(ArTryOnAction.Proceed(configId)) }) {
                        Text("Proceed")
                        Spacer(Modifier.width(6.dp))
                        Icon(Icons.Default.ArrowForward, null, Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}