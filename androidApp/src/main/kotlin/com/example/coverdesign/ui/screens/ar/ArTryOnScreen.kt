package com.example.coverdesign.ui.screens.ar

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.coverdesign.domain.model.TrackingState
import com.example.coverdesign.ui.components.AppTopBar
import com.example.coverdesign.ui.components.EventHandler
import com.example.coverdesign.ui.viewmodel.ArTryOnAction
import com.example.coverdesign.ui.viewmodel.ArTryOnUiEvent
import com.example.coverdesign.ui.viewmodel.ArTryOnViewModel
import org.koin.compose.viewmodel.koinViewModel

// androidApp/ui/screens/ar/ArTryOnScreen.kt
@Composable
fun ArTryOnScreen(
    configId         : String,
    viewModel        : ArTryOnViewModel = koinViewModel(),
    onProceedToOrder : () -> Unit,
    onBack           : () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    EventHandler(viewModel.events) { event ->
        when (event) {
            is ArTryOnUiEvent.NavigateToOrder  -> onProceedToOrder()
            is ArTryOnUiEvent.SnapshotCaptured -> { /* save/share photo */ }
        }
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.onAction(ArTryOnAction.StopSession) }
    }
// TODO add WithCameraPermission
    Scaffold(
        topBar = { AppTopBar(title = "AR Try-On", onBack = onBack) }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {

            // in ArTryOnScreen — replace the black box placeholder
            if (state.isArSupported && state.modelUrl != null) {
                ArSceneViewComposable(
                    modelUrl   = state.modelUrl!!,
                    onTracking = { tracking ->
                        viewModel.onAction(ArTryOnAction.UpdateTracking(tracking))
                    },
                    modifier   = Modifier.fillMaxSize()
                )
            } else if (state.modelUrl == null && !state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No model loaded", color = Color.White)
                }
            }
//            if (!state.isArSupported) {
//                // AR not available on device
//                ErrorScreen("AR is not supported on this device")
//            }
//           else {
//                state.modelUrl?.let { url ->
//                    ArSceneViewComposable(
//                        modelUrl   = url,
//                        repository = get<ArSessionRepository>() as ArSessionRepositoryImpl,
//                        onTracking = { tracking ->
//                            viewModel.onAction(ArTryOnAction.UpdateTracking(tracking))
//                        },
//                        modifier   = Modifier.fillMaxSize()
//                    )
//                }
//            }
        }
    }
}

@Composable
private fun TrackingStateBanner(trackingState: TrackingState) {
    val (text, color) = when (trackingState) {
        TrackingState.INITIALIZING  ->
            "Initializing tracking…" to MaterialTheme.colorScheme.tertiary
        TrackingState.TRACKING      ->
            "Tracking active" to MaterialTheme.colorScheme.primary
        TrackingState.LOST          ->
            "Tracking lost — move slowly" to MaterialTheme.colorScheme.error
        TrackingState.NOT_AVAILABLE ->
            "Tracking unavailable" to MaterialTheme.colorScheme.error
    }
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = color.copy(alpha = 0.85f)
    ) {
        Text(
            text     = text,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color    = Color.White,
            style    = MaterialTheme.typography.labelMedium
        )
    }
}