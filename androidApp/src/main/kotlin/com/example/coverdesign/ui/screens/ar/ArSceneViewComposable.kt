package com.example.coverdesign.ui.screens.ar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.coverdesign.domain.model.TrackingState
import com.example.coverdesign.ui.components.AppTopBar
import com.example.coverdesign.ui.components.ErrorScreen
import com.example.coverdesign.ui.components.EventHandler
import com.example.coverdesign.ui.components.LoadingScreen
import com.example.coverdesign.ui.viewmodel.ArTryOnAction
import com.example.coverdesign.ui.viewmodel.ArTryOnUiEvent
import com.example.coverdesign.ui.viewmodel.ArTryOnViewModel
import com.google.ar.core.Anchor
import com.google.ar.core.Plane
import com.google.ar.core.TrackingFailureReason
import io.github.sceneview.ar.ARSceneView
import io.github.sceneview.ar.arcore.getUpdatedPlanes
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberModelInstance
import io.github.sceneview.rememberModelLoader
import org.koin.compose.viewmodel.koinViewModel

/**
 * Full virtual try-on screen.
 *
 * Layout mirrors VirtualTryOnPlaceholderScreen: Scaffold + AppTopBar, AR viewport in the
 * main slot (weight = 1f), "Edit Config" / "Order" buttons at the bottom.
 * The viewport adds a tracking-state banner and model-loading chip as overlays.
 */
@Composable
fun ArSceneViewComposable(
    configId    : String,
    viewModel   : ArTryOnViewModel = koinViewModel(),
    onOrder     : () -> Unit,
    onEditConfig: () -> Unit,
    onBack      : () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(configId) {
        viewModel.onAction(ArTryOnAction.Load(configId))
    }

    EventHandler(viewModel.events) { event ->
        when (event) {
            is ArTryOnUiEvent.NavigateToOrder      -> onOrder()
            is ArTryOnUiEvent.NavigateToConfigEdit -> onEditConfig()
        }
    }

    Scaffold(
        topBar = { AppTopBar(title = "Virtual Try-On", onBack = onBack) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── AR viewport (replaces the image slot from VirtualTryOnPlaceholderScreen) ──
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when {
                    !state.isArSupported                        -> ErrorScreen("AR is not supported on this device")
                    state.error != null                         -> ErrorScreen(state.error!!)
                    state.isLoading || state.modelUrl == null   -> LoadingScreen()
                    else -> ArViewport(
                        modelUrl   = state.modelUrl!!,
                        onTracking = { viewModel.onAction(ArTryOnAction.UpdateTracking(it)) }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── same button row as VirtualTryOnPlaceholderScreen ──────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick  = { viewModel.onAction(ArTryOnAction.UpdateParameters(configId)) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Tune, contentDescription = null, Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Edit Config")
                }
                Button(
                    onClick  = { viewModel.onAction(ArTryOnAction.Proceed(configId)) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Order")
                    Spacer(Modifier.width(6.dp))
                    Icon(Icons.Default.ArrowForward, contentDescription = null, Modifier.size(16.dp))
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

// ── AR viewport ───────────────────────────────────────────────────────────────

@Composable
private fun ArViewport(
    modelUrl  : String,
    onTracking: (TrackingState) -> Unit,
    modifier  : Modifier = Modifier
) {
    val engine      = rememberEngine()
    val modelLoader = rememberModelLoader(engine)

    val modelInstance = rememberModelInstance(
        modelLoader  = modelLoader,
        fileLocation = modelUrl
    )

    var anchor        by remember { mutableStateOf<Anchor?>(null) }
    var isTracking    by remember { mutableStateOf(false) }
    var failureReason by remember { mutableStateOf<TrackingFailureReason?>(null) }

    // Plain array ref to deduplicate onTracking() calls without triggering recomposition.
    val lastDomain = remember { arrayOf<TrackingState?>(null) }

    Box(modifier = modifier.fillMaxSize()) {
        ARSceneView(
            modifier      = Modifier.fillMaxSize(),
            engine        = engine,
            modelLoader   = modelLoader,
            planeRenderer = true,
            onTrackingFailureChanged = { reason ->
                // null = tracking restored; non-null = specific failure
                failureReason = reason
            },
            onSessionUpdated = { _, frame ->
                val tracking = frame.camera.trackingState ==
                        com.google.ar.core.TrackingState.TRACKING
                if (tracking != isTracking) isTracking = tracking

                val domain = when (frame.camera.trackingState) {
                    com.google.ar.core.TrackingState.TRACKING -> TrackingState.TRACKING
                    com.google.ar.core.TrackingState.PAUSED   -> TrackingState.LOST
                    else                                       -> TrackingState.NOT_AVAILABLE
                }
                if (domain != lastDomain[0]) {
                    lastDomain[0] = domain
                    onTracking(domain)
                }

                // Auto-anchor on first detected upward-facing horizontal plane
                if (anchor == null && tracking) {
                    anchor = frame.getUpdatedPlanes()
                        .firstOrNull { it.type == Plane.Type.HORIZONTAL_UPWARD_FACING }
                        ?.let { runCatching { it.createAnchor(it.centerPose) }.getOrNull() }
                }
            }
        ) {
            anchor?.let { a ->
                AnchorNode(anchor = a) {
                    modelInstance?.let { model ->
                        ModelNode(
                            modelInstance = model,
                            scaleToUnits  = 1.0f,
                            autoAnimate   = true
                        )
                    }
                }
            }
        }

        // ── Tracking hint banner — top-center overlay ─────────────────────
        val hint = trackingHint(failureReason, isTracking, anchor)
        AnimatedVisibility(
            visible  = hint != null,
            enter    = fadeIn(),
            exit     = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp)
        ) {
            if (hint != null) {
                TrackingBanner(message = hint, isWarning = failureReason != null)
            }
        }

        // ── Model loading chip — bottom-center overlay ────────────────────
        AnimatedVisibility(
            visible  = modelInstance == null,
            enter    = fadeIn(),
            exit     = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator(Modifier.size(14.dp), strokeWidth = 2.dp)
                    Text("Loading 3D model…", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

/**
 * Returns a user-facing guidance string, or null when no hint is needed
 * (model placed, tracking fine).
 */
private fun trackingHint(
    reason    : TrackingFailureReason?,
    isTracking: Boolean,
    anchor    : Anchor?
): String? = when {
    reason != null -> when (reason) {
        TrackingFailureReason.BAD_STATE             -> "Move around slowly to restore tracking"
        TrackingFailureReason.INSUFFICIENT_LIGHT    -> "Move to a brighter area"
        TrackingFailureReason.EXCESSIVE_MOTION      -> "Hold the phone steadier"
        TrackingFailureReason.INSUFFICIENT_FEATURES -> "Point at a textured surface — avoid plain walls"
        TrackingFailureReason.CAMERA_UNAVAILABLE    -> "Camera unavailable — check app permissions"
        else                                        -> "Searching for surfaces…"
    }
    !isTracking    -> "Initializing AR…"
    anchor == null -> "Point camera at a flat horizontal surface"
    else           -> null
}

@Composable
private fun TrackingBanner(message: String, isWarning: Boolean) {
    val containerColor = if (isWarning)
        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.92f)
    else
        MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
    val contentColor = if (isWarning)
        MaterialTheme.colorScheme.onErrorContainer
    else
        MaterialTheme.colorScheme.onSurface

    Surface(shape = RoundedCornerShape(20.dp), color = containerColor) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (isWarning) {
                Icon(
                    imageVector        = Icons.Default.Warning,
                    contentDescription = null,
                    modifier           = Modifier.size(16.dp),
                    tint               = contentColor
                )
            }
            Text(text = message, style = MaterialTheme.typography.labelMedium, color = contentColor)
        }
    }
}
