package com.example.coverdesign.ui.screens.patient.config

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.coverdesign.R
import com.example.coverdesign.domain.model.AdditionalElement
import com.example.coverdesign.domain.model.Material
import com.example.coverdesign.domain.model.Measurement
import com.example.coverdesign.domain.model.OrderCategory
import com.example.coverdesign.domain.model.Product
import com.example.coverdesign.domain.model.ProductColor
import com.example.coverdesign.domain.model.ProductConfig
import com.example.coverdesign.domain.model.SizeLabel
import com.example.coverdesign.domain.model.Texture
import com.example.coverdesign.domain.model.pricing.PriceEstimate
import com.example.coverdesign.ui.components.AppTopBar
import com.example.coverdesign.ui.components.LoadingScreen
import com.example.coverdesign.ui.theme.AppTheme
import com.example.coverdesign.ui.viewmodel.ProductConfigAction
import com.example.coverdesign.ui.viewmodel.ProductConfigUiState

// ═══════════════════════════════════════════════════════════════════
// Preview-only scaffold + @Preview
// ═══════════════════════════════════════════════════════════════════

@Composable
private fun ProductConfigScreenContent(
    state: ProductConfigUiState, onAction: (ProductConfigAction) -> Unit, onBack: () -> Unit
) {
    Scaffold(topBar = {
        AppTopBar(
            title = stringResource(R.string.configure_product), onBack = onBack
        )
    }, bottomBar = {
        ProceedBottomBar(
            priceEstimate = state.priceEstimate,
            isLoading = state.isSaving,
            enabled = state.currentConfig != null && state.measurementError == null,
            onProceed = { onAction(ProductConfigAction.SaveAndProceed) })
    }) { padding ->
        if (state.isLoading) LoadingScreen()
        else ProductConfigContent(
            state = state, onAction = onAction, modifier = Modifier.padding(padding)
        )
    }
}

@Preview(showBackground = true, name = "Config Screen – Jewellery")
@Composable
private fun PreviewConfigScreen() {
    val sampleProduct = Product.Generic(
        id = "prod-001",
        name = "Forearm Cover",
        description = "Personalised prosthetic forearm cover.",
        basePrice = 149.99,
        category = OrderCategory.JEWELRY,
        availableMaterials = listOf(Material.PLA, Material.CARBON_FIBER),
        availableTextures = listOf(
            Texture("tex-001", "Matte", "", "Smooth matte surface"),
            Texture("tex-002", "Glossy", "", "High-gloss finish")
        ),
        imageUrl = ""
    )

    val sampleConfig = ProductConfig(
        id = "config-123",
        productId = "prod-001",
        orderCategory = OrderCategory.JEWELRY,
        measurement = Measurement(150.0, 75.0, 10.0),
        sizeLabel = SizeLabel.M,
        color = ProductColor("Dark green", "#0f3F15"),
        material = Material.PLA,
        texture = Texture("tex-001", "Matte", "", "Smooth matte surface"),
        inscription = "Custom",
        userPhotoUri = null,
        additionalElements = listOf(AdditionalElement.LOGO),
        quantity = 1,
        createdAt = 0L
    )

    AppTheme {
        ProductConfigScreenContent(
            state = ProductConfigUiState(
                selectedProduct = sampleProduct,
                currentConfig = sampleConfig,
                priceEstimate = PriceEstimate(
                    configId = "config-123",
                    basePrice = 149.99,
                    materialSurcharges = mapOf("PLA" to 2.00),
                    textureSurcharge = 2.00,
                    sizeMultiplier = 1.15,
                    sizeMultiplierLabel = "L +15%",
                    quantity = 1,
                    subtotal = 155.67,
                    total = 155.67
                ),
            ), onAction = {}, onBack = {})
    }
}
