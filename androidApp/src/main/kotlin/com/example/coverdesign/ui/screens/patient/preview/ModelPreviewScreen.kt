package com.example.coverdesign.ui.screens.patient.preview

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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

/**
 * 3D preview screen using SceneView
 * */
@Composable
fun ModelPreviewScreen(
    configId: String,
    taskId: String? = null,
    viewModel: ModelPreviewViewModel = koinViewModel(),
    onTryOn: () -> Unit,
    onOrder: () -> Unit,
    onBack: () -> Unit,
    loadAction: ModelPreviewAction = ModelPreviewAction.LoadForConfig(configId, taskId)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(configId) {
        viewModel.onAction(loadAction)
    }

    EventHandler(viewModel.events) { event ->
        when (event) {
            is ModelPreviewUiEvent.NavigateToAr -> onTryOn()
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
            // ── generation progress bar — shown while not READY ────────────
            AnimatedVisibility(
                visible = state.isGenerating,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                GenerationProgressBar(
                    status = state.modelStatus,
                    estimatedWaitSeconds = state.estimatedWaitSeconds
                )
            }

            // ── 3D viewer area ─────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                when {
                    state.isLoadingLocal -> {
                        CircularProgressIndicator()
                    }

                    // generation in progress
                    state.isGenerating -> {
                        ModelGeneratingIndicator(
                            status = state.modelStatus,
                            estimatedWaitSeconds = state.estimatedWaitSeconds
                        )
                    }
                    state.isFailed -> {
                        ModelFailedIndicator(
                            onRetry = {
                                taskId?.let {
                                    viewModel.onAction(ModelPreviewAction.Retry(it))
                                }
                            },
                            canRetry = taskId != null
                        )
                    }

                    // network / other error
                    state.error != null && !state.isReady -> {
                        ErrorScreen(state.error!!)
                    }

                    state.modelUrl != null -> {
//                        modelInstance?.let {
//                            ModelViewerComposable(
//                                modelInstance = it
//                            )
//                        }
                        ModelViewerComposable(
                            modelUrl = state.modelUrl!!,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    else -> {
                        Text(
                            "Select a product to generate the model",
                            style     = MaterialTheme.typography.bodyMedium,
                            color     = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier  = Modifier.padding(32.dp)
                        )
                    }
                }
            }

            // ── action buttons ─────────────────────────────────────────────
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { viewModel.onAction(ModelPreviewAction.ProceedToAr(configId)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    enabled = state.modelUrl != null,
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.ViewInAr, null, Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Try On (AR)")
                }
                OutlinedButton(
                    onClick = { viewModel.onAction(ModelPreviewAction.ProceedToOrder(configId)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    enabled = state.modelUrl != null,
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.ShoppingCart, null, Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Proceed to Order")
                }
            }
        }
    }
}

//@Composable
//private fun ModelGeneratingIndicator(status: ModelStatus) {
//    Column(
//        horizontalAlignment = Alignment.CenterHorizontally,
//        verticalArrangement = Arrangement.spacedBy(16.dp)
//    ) {
//        CircularProgressIndicator()
//        Text(
//            text = when (status) {
//                ModelStatus.PENDING -> "Preparing your model…"
//                ModelStatus.GENERATING -> "Generating 3D model…"
//                else -> "Please wait…"
//            },
//            style = MaterialTheme.typography.bodyMedium
//        )
//    }
//}