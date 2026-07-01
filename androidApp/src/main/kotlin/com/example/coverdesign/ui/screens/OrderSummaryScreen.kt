package com.example.coverdesign.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.coverdesign.domain.model.AdditionalElement
import com.example.coverdesign.domain.model.Material
import com.example.coverdesign.domain.model.Measurement
import com.example.coverdesign.domain.model.Order
import com.example.coverdesign.domain.model.OrderStatus
import com.example.coverdesign.domain.model.ProductCategory
import com.example.coverdesign.domain.model.ProductColor
import com.example.coverdesign.domain.model.ProductConfig
import com.example.coverdesign.domain.model.SizeLabel
import com.example.coverdesign.domain.model.Texture
import com.example.coverdesign.domain.model.pricing.PriceEstimate
import com.example.coverdesign.ui.components.AppTopBar
import com.example.coverdesign.ui.components.ErrorScreen
import com.example.coverdesign.ui.components.EventHandler
import com.example.coverdesign.ui.components.LoadingScreen
import com.example.coverdesign.ui.screens.config.PriceCard
import com.example.coverdesign.ui.theme.AppTheme
import com.example.coverdesign.ui.viewmodel.OrderSummaryAction
import com.example.coverdesign.ui.viewmodel.OrderSummaryUiEvent
import com.example.coverdesign.ui.viewmodel.OrderSummaryUiState
import com.example.coverdesign.ui.viewmodel.OrderSummaryViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun OrderSummaryScreen(
    configId         : String,
    viewModel        : OrderSummaryViewModel = koinViewModel(),
    onOrderSubmitted : () -> Unit,
    onBack           : () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    EventHandler(viewModel.events) { event ->
        when (event) {
            is OrderSummaryUiEvent.OrderSubmitted  -> onOrderSubmitted()
            is OrderSummaryUiEvent.NavigateToHistory -> { /* handle history nav */ }
        }
    }

    OrderSummaryScreenContent(
        uiState  = state,
        onBack   = onBack,
        onSubmit = { viewModel.onAction(OrderSummaryAction.SubmitOrder(it)) }
    )
}

@Composable
private fun OrderSummaryScreenContent(
    uiState  : OrderSummaryUiState,
    onBack   : () -> Unit,
    onSubmit : (Order) -> Unit
) {
    Scaffold(
        topBar = { AppTopBar(title = "Order Summary", onBack = onBack) },
        bottomBar = {
            SubmitOrderBar(
                isSubmitting = uiState.isSubmitting,
                enabled      = uiState.order != null && uiState.submittedOrderId == null,
                onSubmit     = {
                    uiState.order?.let { onSubmit(it) }
                }
            )
        }
    ) { padding ->
        when {
            uiState.isLoading  -> LoadingScreen()
            uiState.error != null -> ErrorScreen(uiState.error!!)
            uiState.order != null -> OrderSummaryContent(
                order    = uiState.order!!,
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@Composable
private fun OrderSummaryContent(order: Order, modifier: Modifier = Modifier) {
    val config = order.config
    Column(
        modifier            = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // ── Product info ───────────────────────────────────────────────────
        SummarySection(title = "Product") {
            SummaryRow("Type",     config.productCategory.name)
            SummaryRow("Material", config.material.displayName)
            config.texture?.let { SummaryRow("Texture", it.name) }
            SummaryRow("Color",    config.color.name)
        }

        // ── Measurements ──────────────────────────────────────────────────
        SummarySection(title = "Measurements") {
            SummaryRow("Height", "${config.measurement.height} mm")
            SummaryRow("Width",  "${config.measurement.width} mm")
            SummaryRow("Depth",  "${config.measurement.depth} mm")
            config.sizeLabel?.let { SummaryRow("Size", it.name) }
        }

        // ── Personalisation ───────────────────────────────────────────────
        if (config.inscription.isNotEmpty() ||
            config.additionalElements.isNotEmpty()) {
            SummarySection(title = "Personalisation") {
                if (config.inscription.isNotEmpty())
                    SummaryRow("Inscription", config.inscription)
                if (config.additionalElements.isNotEmpty())
                    SummaryRow("Elements",
                        config.additionalElements.joinToString { it.displayName })
            }
        }

        // ── Order details ─────────────────────────────────────────────────
        SummarySection(title = "Order") {
            SummaryRow("Quantity", config.quantity.toString())
            SummaryRow("Status",   order.status.name)
        }

        // ── Price breakdown ───────────────────────────────────────────────
        PriceCard(price = order.priceEstimate)
    }
}

@Composable
private fun SummarySection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Surface(
            shape  = RoundedCornerShape(12.dp),
            color  = colorScheme.surfaceVariant.copy(alpha = 0.4f),
            border = BorderStroke(1.dp, colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                content = content
            )
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall,
            color = colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun SubmitOrderBar(
    isSubmitting : Boolean,
    enabled      : Boolean,
    onSubmit     : () -> Unit
) {
    Surface(shadowElevation = 8.dp) {
        Button(
            onClick  = onSubmit,
            enabled  = enabled && !isSubmitting,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .height(52.dp),
            shape    = RoundedCornerShape(14.dp)
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(
                    Modifier.size(18.dp),
                    color = colorScheme.onPrimary
                )
            } else {
                Icon(Icons.Default.CheckCircle, null, Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Confirm Order", style = MaterialTheme.typography.titleSmall)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OrderSummaryScreenPreview() {
    AppTheme {
        OrderSummaryScreenContent(
            uiState = OrderSummaryUiState(
                order = sampleOrder
            ),
            onBack = {},
            onSubmit = {}
        )
    }
}

private val sampleOrder = Order(
    id = "ord_1",
    adaptedModelId = "mod_1",
    config = ProductConfig(
        id = "conf_1",
        productId = "prod_1",
        productCategory = ProductCategory.ACCESSORY,
        measurement = Measurement(100.0, 50.0, 10.0),
        sizeLabel = SizeLabel.M,
        color = ProductColor("Black", "#000000"),
        material = Material.METAL,
        texture = Texture("txt_1", "Brushed", null, null),
        inscription = "Custom Text",
        userPhotoUri = null,
        additionalElements = listOf(AdditionalElement.LOGO),
        quantity = 1,
        createdAt = 0L
    ),
    priceEstimate = PriceEstimate(
        configId = "conf_1",
        basePrice = 100.0,
        subtotal = 110.0,
        total = 110.0,
        quantity = 1
    ),
    status = OrderStatus.DRAFT,
    createdAt = 0L
)
