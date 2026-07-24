package com.example.coverdesign.ui.screens.preview

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.coverdesign.ui.components.AppTopBar
import com.example.coverdesign.ui.components.ErrorScreen
import com.example.coverdesign.ui.components.EventHandler
import com.example.coverdesign.ui.viewmodel.ModelPreviewAction
import com.example.coverdesign.ui.viewmodel.ModelPreviewUiEvent
import com.example.coverdesign.ui.viewmodel.ModelPreviewViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ModifiableModelPreviewScreen(
    configId   : String,
    taskId     : String? = null,
    viewModel  : ModelPreviewViewModel = koinViewModel(),
    onTryOn    : () -> Unit,
    onOrder    : () -> Unit,
    onBack     : () -> Unit,
    loadAction : ModelPreviewAction = ModelPreviewAction.LoadForConfig(configId, taskId)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    var scaleX by remember { mutableFloatStateOf(1f) }
    var scaleY by remember { mutableFloatStateOf(1f) }
    var scaleZ by remember { mutableFloatStateOf(1f) }

    LaunchedEffect(configId) {
        viewModel.onAction(loadAction)
    }

    EventHandler(viewModel.events) { event ->
        when (event) {
            is ModelPreviewUiEvent.NavigateToAr    -> onTryOn()
            is ModelPreviewUiEvent.NavigateToOrder -> onOrder()
        }
    }

    Scaffold(
        topBar = { AppTopBar(title = "3D Preview", onBack = onBack) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedVisibility(
                visible = state.isGenerating,
                enter   = fadeIn(),
                exit    = fadeOut()
            ) {
                GenerationProgressBar(
                    status               = state.modelStatus,
                    estimatedWaitSeconds = state.estimatedWaitSeconds
                )
            }

            // ── 3D viewer ─────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                when {
                    state.isLoadingLocal -> CircularProgressIndicator()

                    state.isGenerating -> ModelGeneratingIndicator(
                        status               = state.modelStatus,
                        estimatedWaitSeconds = state.estimatedWaitSeconds
                    )

                    state.isFailed -> ModelFailedIndicator(
                        onRetry  = { taskId?.let { viewModel.onAction(ModelPreviewAction.Retry(it)) } },
                        canRetry = taskId != null
                    )

                    state.error != null && !state.isReady -> ErrorScreen(state.error!!)

                    state.modelUrl != null -> ModelViewerComposable(
                        modelUrl = state.modelUrl!!,
                        scaleX   = scaleX,
                        scaleY   = scaleY,
                        scaleZ   = scaleZ,
                        modifier = Modifier.fillMaxSize()
                    )

                    else -> Text(
                        "Select a product to generate the model",
                        style     = MaterialTheme.typography.bodyMedium,
                        color     = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier  = Modifier.padding(32.dp)
                    )
                }
            }

            // ── size sliders — visible only when model is ready ───────────────
            AnimatedVisibility(
                visible = state.modelUrl != null,
                enter   = fadeIn(),
                exit    = fadeOut()
            ) {
                SizeAdjustmentPanel(
                    scaleX        = scaleX,
                    onScaleXChange = { scaleX = it },
                    scaleY        = scaleY,
                    onScaleYChange = { scaleY = it },
                    scaleZ        = scaleZ,
                    onScaleZChange = { scaleZ = it },
                    onReset       = { scaleX = 1f; scaleY = 1f; scaleZ = 1f }
                )
            }

            // ── action buttons ────────────────────────────────────────────────
            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick  = { viewModel.onAction(ModelPreviewAction.ProceedToAr(configId)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    enabled = state.modelUrl != null,
                    shape   = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.ViewInAr, null, Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Try On (AR)")
                }
                OutlinedButton(
                    onClick  = { viewModel.onAction(ModelPreviewAction.ProceedToOrder(configId)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    enabled = state.modelUrl != null,
                    shape   = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.ShoppingCart, null, Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Proceed to Order")
                }
            }
        }
    }
}

@Composable
private fun SizeAdjustmentPanel(
    scaleX        : Float, onScaleXChange : (Float) -> Unit,
    scaleY        : Float, onScaleYChange : (Float) -> Unit,
    scaleZ        : Float, onScaleZChange : (Float) -> Unit,
    onReset       : () -> Unit
) {
    Surface(
        modifier      = Modifier.fillMaxWidth(),
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier             = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment    = Alignment.CenterVertically
            ) {
                Text("Size", style = MaterialTheme.typography.labelLarge)
                TextButton(onClick = onReset) { Text("Reset") }
            }
            ScaleSliderRow(label = "Width",  value = scaleX, onValueChange = onScaleXChange)
            ScaleSliderRow(label = "Height", value = scaleY, onValueChange = onScaleYChange)
            ScaleSliderRow(label = "Depth",  value = scaleZ, onValueChange = onScaleZChange)
        }
    }
}

@Composable
private fun ScaleSliderRow(
    label         : String,
    value         : Float,
    onValueChange : (Float) -> Unit
) {
    Row(
        modifier          = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text     = label,
            modifier = Modifier.width(48.dp),
            style    = MaterialTheme.typography.bodySmall,
            color    = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Slider(
            value         = value,
            onValueChange = onValueChange,
            valueRange    = 0.5f..2.0f,
            modifier      = Modifier.weight(1f)
        )
        Text(
            text     = "×${"%.2f".format(value)}",
            modifier = Modifier.width(44.dp),
            style    = MaterialTheme.typography.bodySmall,
            color    = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.End
        )
    }
}
