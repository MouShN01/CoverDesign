package com.example.coverdesign.ui.screens.patient.config

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.coverdesign.domain.model.OrderCategory
import com.example.coverdesign.domain.model.ProductConfig
import com.example.coverdesign.ui.components.AppTopBar
import com.example.coverdesign.ui.components.ErrorScreen
import com.example.coverdesign.ui.components.EventHandler
import com.example.coverdesign.ui.components.LoadingScreen
import com.example.coverdesign.ui.viewmodel.ConfigEntryMode
import com.example.coverdesign.ui.viewmodel.OrderType
import com.example.coverdesign.ui.viewmodel.ProductConfigAction
import com.example.coverdesign.ui.viewmodel.ProductConfigUiEvent
import com.example.coverdesign.ui.viewmodel.ProductConfigViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ProductConfigScreen(
    entryMode: ConfigEntryMode,
    viewModel: ProductConfigViewModel = koinViewModel(),
    onProceed: (taskId: String, configId: String) -> Unit,
    onOrderSummary: (configId: String) -> Unit = {},
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(entryMode) {
        viewModel.onAction(ProductConfigAction.Enter(entryMode))
    }

    // Clear the post-save input lock whenever the screen returns to the foreground
    // (e.g. after pressing Back from the 3D preview) so the form is editable again.
    LifecycleResumeEffect(Unit) {
        viewModel.onAction(ProductConfigAction.ResetLock)
        onPauseOrDispose { }
    }

    LaunchedEffect(state.error) {
        if (state.error != null) {
            snackbarHostState.showSnackbar(
                message = state.error.toString(), duration = SnackbarDuration.Long
            )
            viewModel.onAction(ProductConfigAction.ClearError)
        }
    }

    LaunchedEffect(state.isSaving) {
        if (!state.isSaving) snackbarHostState.currentSnackbarData?.dismiss()
    }

    EventHandler(viewModel.events) { event ->
        when (event) {
            is ProductConfigUiEvent.NavigateToPreview -> onProceed(event.taskId, event.configId)
            is ProductConfigUiEvent.NavigateToOrderSummary -> onOrderSummary(event.configId)
        }
    }

    var showProceedDialog by remember { mutableStateOf(false) }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }, topBar = {
        AppTopBar(
            title = when (state.orderType) {
                OrderType.INSOLE -> "Order Insole"
                OrderType.PROSTHETIC_COVER -> "Order Prosthetic Cover"
                null -> state.selectedProduct?.name ?: "New Order"
            }, onBack = onBack
        )
    }, bottomBar = {
        when (state.orderType) {
            OrderType.INSOLE -> DirectOrderBar(
                label = "Place Order",
                isSaving = state.isSaving,
                enabled = !state.isLockedAfterSave && state.insoleConfig.primaryFoot.lengthMm > 0 && state.insoleConfig.primaryFoot.widthMm > 0,
                onClick = { viewModel.onAction(ProductConfigAction.SubmitInsoleOrder) })

            OrderType.PROSTHETIC_COVER -> DirectOrderBar(
                label = "Preview 3D Model",
                isSaving = state.isSaving,
                enabled = !state.isLockedAfterSave && state.prostheticConfig.prostheticType != null && state.prostheticConfig.commonMeasurements.circumferenceMm > 0 && state.prostheticConfig.commonMeasurements.kneeApexToFootshellTopMm > 0,
                onClick = { viewModel.onAction(ProductConfigAction.SubmitProstheticOrder) })

            null -> if (state.currentConfig != null) {
                ProceedBottomBar(
                    priceEstimate = state.priceEstimate,
                    isLoading = state.isSaving,
                    enabled = state.currentConfig != null && state.measurementError == null && isProceedEnabled(
                        state.currentConfig
                    ),
                    onProceed = { showProceedDialog = true })
            }
        }
    }) { padding ->
        Box(Modifier.fillMaxSize()) {
            when {
                state.isLoading -> LoadingScreen()
                state.error != null && state.currentConfig == null && state.orderType == null -> ErrorScreen(
                    state.error!!
                )

                state.orderType == OrderType.INSOLE -> InsoleConfigContent(
                    state = state, onAction = viewModel::onAction, modifier = Modifier.padding(padding)
                )

                state.orderType == OrderType.PROSTHETIC_COVER -> ProstheticCoverConfigContent(
                    state = state, onAction = viewModel::onAction, modifier = Modifier.padding(padding)
                )

                state.currentConfig != null -> ProductConfigContent(
                    state = state, onAction = viewModel::onAction, modifier = Modifier.padding(padding)
                )

                else -> TypePickerContent(
                    onAction = viewModel::onAction,
                    modifier = Modifier.padding(padding)
                )
            }

            if (state.isLockedAfterSave) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.6f))
                        .pointerInput(Unit) {
                            awaitPointerEventScope {
                                while (true) {
                                    val event = awaitPointerEvent(PointerEventPass.Initial)
                                    event.changes.forEach { it.consume() }
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (state.isSaving) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
                            Text(
                                "Placing your order…",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }
        }
    }

    if (showProceedDialog) { //TODO adapt for insoles and prosthetics
        state.currentConfig?.let { config ->
            ProceedConfirmationDialog(config = config, onConfirm = {
                showProceedDialog = false
                viewModel.onAction(ProductConfigAction.SaveAndProceed)
            }, onDismiss = { showProceedDialog = false })
        }
    }

    state.pendingResumePrompt?.let { previousConfig ->
        ResumeConfigDialog(
            previousConfig = previousConfig,
            onResume = { viewModel.onAction(ProductConfigAction.ResumePreviousConfig(previousConfig)) },
            onStartFresh = {
                state.selectedProduct?.let {
                    viewModel.onAction(ProductConfigAction.StartFreshConfig(it))
                }
            })
    }
}

private fun isProceedEnabled(config: ProductConfig?): Boolean {
    config ?: return false
    return if (config.orderCategory == OrderCategory.CLOTHING) {
        config.measurement.height in 100.0..220.0
    } else {
        true
    }
}
