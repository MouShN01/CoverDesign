package com.example.coverdesign.ui.screens.patient.ar

import androidx.compose.runtime.Composable
import com.example.coverdesign.ui.viewmodel.ArTryOnViewModel
import org.koin.compose.viewmodel.koinViewModel

// Thin wrapper kept for call-site compatibility.
// ArSceneViewComposable now owns the full screen UI (Scaffold, buttons, AR viewport).
@Composable
fun ArTryOnScreen(
    configId          : String,
    viewModel         : ArTryOnViewModel = koinViewModel(),
    onProceedToOrder  : () -> Unit,
    onUpdateParameters: () -> Unit,
    onBack            : () -> Unit
) {
    ArSceneViewComposable(
        configId     = configId,
        viewModel    = viewModel,
        onOrder      = onProceedToOrder,
        onEditConfig = onUpdateParameters,
        onBack       = onBack
    )
}
