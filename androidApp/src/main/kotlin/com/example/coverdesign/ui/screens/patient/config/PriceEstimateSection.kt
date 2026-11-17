package com.example.coverdesign.ui.screens.patient.config

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.coverdesign.ui.viewmodel.ProductConfigAction
import com.example.coverdesign.ui.viewmodel.ProductConfigUiState

@Composable
fun PriceEstimateSection(
    state: ProductConfigUiState,
    onAction: (ProductConfigAction) -> Unit
) {
    state.priceEstimate?.let { PriceCard(price = it) }
    OutlinedButton(
        onClick = { onAction(ProductConfigAction.RequestPriceUpdate) },
        modifier = Modifier.fillMaxWidth(),
        enabled = !state.isPriceLoading
    ) {
        if (state.isPriceLoading) CircularProgressIndicator(Modifier.size(16.dp))
        else Text("Calculate Price")
    }
}