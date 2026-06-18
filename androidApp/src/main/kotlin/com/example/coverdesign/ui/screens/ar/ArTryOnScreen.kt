package com.example.coverdesign.ui.screens.ar
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ShoppingCart

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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

import org.koin.compose.viewmodel.koinViewModel

import com.example.coverdesign.domain.model.TrackingState

import com.example.coverdesign.ui.components.AppTopBar
import com.example.coverdesign.ui.components.ErrorScreen
import com.example.coverdesign.ui.components.EventHandler
import com.example.coverdesign.ui.viewmodel.ArTryOnAction
import com.example.coverdesign.ui.viewmodel.ArTryOnUiEvent
import com.example.coverdesign.ui.viewmodel.ArTryOnViewModel

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

    Scaffold(
        topBar = { AppTopBar(title = "AR Try-On", onBack = onBack) }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {

            if (!state.isArSupported) {
                // AR not available on device
                ErrorScreen("AR is not supported on this device")
            } else {
                // ── AR camera view — real impl in Step 9 ──────────────────
                Box(
                    modifier         = Modifier.fillMaxSize()
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "AR Camera View — Step 9",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                // ── overlay UI ────────────────────────────────────────────
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // tracking state banner
                    TrackingStateBanner(state.trackingState)

                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        // capture snapshot
                        FloatingActionButton(
                            onClick = { viewModel.onAction(ArTryOnAction.CaptureSnapshot) },
                            containerColor = Color.White
                        ) {
                            Icon(Icons.Default.CameraAlt, null,
                                tint = Color.Black)
                        }

                        // proceed to order
                        ExtendedFloatingActionButton(
                            onClick = {
                                viewModel.onAction(ArTryOnAction.ProceedToOrder(configId))
                            },
                            icon    = { Icon(Icons.Default.ShoppingCart, null) },
                            text    = { Text("Order Now") }
                        )
                    }
                }

                // loading overlay
                if (state.isLoading) {
                    Box(
                        modifier         = Modifier.fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator(color = Color.White) }
                }
            }
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