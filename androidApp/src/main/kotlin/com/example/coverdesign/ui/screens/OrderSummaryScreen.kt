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
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.coverdesign.domain.model.AdditionalElement
import com.example.coverdesign.domain.model.FasteningType
import com.example.coverdesign.domain.model.InsoleFootConfig
import com.example.coverdesign.domain.model.InsoleOrderConfig
import com.example.coverdesign.domain.model.LegSide
import com.example.coverdesign.domain.model.LimbLevel
import com.example.coverdesign.domain.model.Material
import com.example.coverdesign.domain.model.Measurement
import com.example.coverdesign.domain.model.Order
import com.example.coverdesign.domain.model.OrderCategory
import com.example.coverdesign.domain.model.OrderDetail
import com.example.coverdesign.domain.model.OrderStatus
import com.example.coverdesign.domain.model.ProductColor
import com.example.coverdesign.domain.model.ProductConfig
import com.example.coverdesign.domain.model.ProstheticCoverOrderConfig
import com.example.coverdesign.domain.model.ProstheticTypeEnum
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
    orderId: String,
    viewModel: OrderSummaryViewModel = koinViewModel(),
    onOrderSubmitted: () -> Unit,
    onView3DModel: ((configId: String, adaptedModelId: String?) -> Unit)? = null,
    onBack: () -> Unit,
    onNavigateHome: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showSuccessDialog by remember { mutableStateOf(false) }

    LaunchedEffect(orderId) {
        viewModel.onAction(OrderSummaryAction.LoadOrder(orderId))
    }

    EventHandler(viewModel.events) { event ->
        when (event) {
            is OrderSummaryUiEvent.OrderSubmitted -> showSuccessDialog = true
        }
    }

    if (showSuccessDialog) {
        val orderTypeLabel = when (state.orderDetail) {
            is OrderDetail.InsoleOrder -> "Insole"
            is OrderDetail.ProstheticCoverOrder -> "Prosthetic Cover"
            else -> "Order"
        }
        OrderSubmittedDialog(orderTypeLabel = orderTypeLabel, onDismiss = onOrderSubmitted)
    }

    OrderSummaryScreenContent(
        uiState = state,
        onBack = onBack,
        onSubmit = { viewModel.onAction(OrderSummaryAction.SubmitCurrentOrder) },
        onView3DModel = onView3DModel,
        onNavigateHome = onNavigateHome
    )
}

@Composable
internal fun OrderSummaryScreenContent(
    uiState: OrderSummaryUiState,
    onBack: () -> Unit,
    onSubmit: () -> Unit,
    onView3DModel: ((configId: String, adaptedModelId: String?) -> Unit)? = null,
    onNavigateHome: () -> Unit = {}
) {
    Scaffold(
        topBar = { AppTopBar(title = "Order Summary", onBack = onBack) },
        bottomBar = {
            Column {
                OutlinedButton(
                    onClick = { onNavigateHome() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp)
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        "Home",
                        style = MaterialTheme.typography.titleSmall,
                        color = colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                when (val detail = uiState.orderDetail) {
                    is OrderDetail.CatalogOrder -> {
                        Column {
                            detail.order.adaptedModelId?.let { adaptedModelId ->
                                if (onView3DModel != null) {
                                    OutlinedButton(
                                        onClick = {
                                            onView3DModel(
                                                detail.order.config.id, adaptedModelId
                                            )
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 20.dp, vertical = 4.dp)
                                            .height(52.dp),
                                        shape = RoundedCornerShape(14.dp)
                                    ) {
                                        Icon(Icons.Default.ViewInAr, null, Modifier.size(20.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            "View 3D Model",
                                            style = MaterialTheme.typography.titleSmall
                                        )
                                    }
                                }
                            }
                            SubmitOrderBar(
                                isSubmitting = uiState.isSubmitting,
                                enabled = detail.order.status == OrderStatus.DRAFT,
                                onSubmit = onSubmit
                            )
                        }
                    }

                    is OrderDetail.InsoleOrder -> {
                        SubmitOrderBar(
                            isSubmitting = uiState.isSubmitting,
                            enabled = detail.status == OrderStatus.DRAFT,
                            onSubmit = onSubmit
                        )
                    }

                    is OrderDetail.ProstheticCoverOrder -> {
                        Column {
                            val latestModel = detail.adaptedModels.firstOrNull()
                            if (latestModel != null && onView3DModel != null) {
                                OutlinedButton(
                                    onClick = {
                                        onView3DModel(
                                            detail.config.orderId,
                                            latestModel.id
                                        )
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 20.dp, vertical = 4.dp)
                                        .height(52.dp),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Icon(Icons.Default.ViewInAr, null, Modifier.size(20.dp))
                                    Spacer(Modifier.width(8.dp))
                                    val label =
                                        if (detail.adaptedModels.size > 1) "View 3D Models (${detail.adaptedModels.size})" else "View 3D Model"
                                    Text(label, style = MaterialTheme.typography.titleSmall)
                                }
                            }
                            SubmitOrderBar(
                                isSubmitting = uiState.isSubmitting,
                                enabled = detail.status == OrderStatus.DRAFT,
                                onSubmit = onSubmit
                            )
                        }
                    }

                    null -> {}
                }
            }

        }) { padding ->
        when {
            uiState.isLoading -> LoadingScreen()
            uiState.error != null -> ErrorScreen(
                uiState.error!!,
                modifier = Modifier.verticalScroll(rememberScrollState())
            )

            uiState.orderDetail != null -> OrderDetailContent(
                detail = uiState.orderDetail!!, modifier = Modifier.padding(padding)
            )
        }
    }
}

// ── Content dispatcher ─────────────────────────────────────────────────────────

@Composable
private fun OrderDetailContent(detail: OrderDetail, modifier: Modifier = Modifier) {
    when (detail) {
        is OrderDetail.CatalogOrder -> CatalogOrderContent(detail.order, modifier)
        is OrderDetail.InsoleOrder -> InsoleOrderContent(detail, modifier)
        is OrderDetail.ProstheticCoverOrder -> ProstheticCoverOrderContent(detail, modifier)
    }
}

// ── Catalog order ──────────────────────────────────────────────────────────────

@Composable
private fun CatalogOrderContent(order: Order, modifier: Modifier = Modifier) {
    val config = order.config
    SummaryColumn(modifier) {
        SummarySection(title = "Product") {
            SummaryRow("Type", config.orderCategory.name)
            SummaryRow("Material", config.material.displayName)
            config.texture?.let { SummaryRow("Texture", it.name) }
            SummaryRow("Color", config.color.name)
        }
        SummarySection(title = "Measurements") {
            SummaryRow("Height", "${config.measurement.height} mm")
            SummaryRow("Width", "${config.measurement.width} mm")
            SummaryRow("Depth", "${config.measurement.depth} mm")
            config.sizeLabel?.let { SummaryRow("Size", it.name) }
        }
        if (config.inscription.isNotEmpty() || config.additionalElements.isNotEmpty()) {
            SummarySection(title = "Personalisation") {
                if (config.inscription.isNotEmpty()) SummaryRow("Inscription", config.inscription)
                if (config.additionalElements.isNotEmpty()) SummaryRow(
                    "Elements", config.additionalElements.joinToString { it.displayName })
            }
        }
        SummarySection(title = "Order") {
            SummaryRow("Quantity", config.quantity.toString())
            SummaryRow("Status", order.status.name)
        }
        PriceCard(price = order.priceEstimate)
    }
}

// ── Insole order ───────────────────────────────────────────────────────────────

@Composable
private fun InsoleOrderContent(detail: OrderDetail.InsoleOrder, modifier: Modifier = Modifier) {
    val config = detail.config
    SummaryColumn(modifier) {
        SummarySection(title = "Order") {
            SummaryRow("Status", detail.status.name)
            SummaryRow("Quantity", config.quantity.toString())
        }
        SummarySection(title = "Right Foot") {
            FootConfigRows(config.rightFoot)
        }
        config.leftFoot?.let { left ->
            SummarySection(title = "Left Foot") {
                FootConfigRows(left)
            }
        }
        SummarySection(title = "Material & Colour") {
            SummaryRow("Material", config.material.displayName)
            SummaryRow("Colour", config.color.name)
        }
        PriceCard(price = config.priceEstimate)
    }
}

@Composable
private fun ColumnScope.FootConfigRows(foot: com.example.coverdesign.domain.model.InsoleFootConfig) {
    SummaryRow("EU Size", foot.euSize.toString())
    SummaryRow("Length", "${foot.lengthMm} mm")
    SummaryRow("Width", "${foot.widthMm} mm")
}

// ── Prosthetic cover order ─────────────────────────────────────────────────────

@Composable
private fun ProstheticCoverOrderContent(
    detail: OrderDetail.ProstheticCoverOrder, modifier: Modifier = Modifier
) {
    val config = detail.config
    SummaryColumn(modifier) {
        SummarySection(title = "Order") {
            SummaryRow("Status", detail.status.name)
            SummaryRow("Quantity", config.quantity.toString())
        }
        SummarySection(title = "Limb") {
            SummaryRow("Side", config.limbSide.displayName)
            SummaryRow("Level", config.limbLevel.displayName)
            config.prostheticType?.let { SummaryRow("Type", it.displayName) }
        }
        SummarySection(title = "Measurements") {
            SummaryRow("Circumference", "${config.commonMeasurements.circumferenceMm} mm")
            SummaryRow(
                "Knee to Footshell", "${config.commonMeasurements.kneeApexToFootshellTopMm} mm"
            )
            if (config.typeSpecificMeasurementMm > 0f) SummaryRow(
                "Type-Specific",
                "${config.typeSpecificMeasurementMm} mm"
            )
            if (config.secondTypeSpecificMeasurementMm > 0f) SummaryRow(
                "Secondary",
                "${config.secondTypeSpecificMeasurementMm} mm"
            )
        }
        SummarySection(title = "Fastening") {
            SummaryRow("Type", config.fasteningType.displayName)
            config.buttonPlacement?.let { SummaryRow("Button placement", it.name) }
        }
        SummarySection(title = "Appearance") {
            SummaryRow("Colour", config.color.name)
            SummaryRow("Material", config.material.displayName)
            SummaryRow("Texture", config.texture.name)
            if (config.inscription.isNotEmpty()) SummaryRow("Inscription", config.inscription)
            if (config.additionalElements.isNotEmpty()) SummaryRow(
                "Elements",
                config.additionalElements.joinToString { it.displayName })
        }
        if (config.additionalNotes.isNotEmpty()) {
            SummarySection(title = "Notes") {
                Text(config.additionalNotes, style = MaterialTheme.typography.bodySmall)
            }
        }
        PriceCard(price = config.priceEstimate)
    }
}

// ── Shared layout helpers ──────────────────────────────────────────────────────

@Composable
private fun SummaryColumn(
    modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        content = content
    )
}

@Composable
private fun SummarySection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = colorScheme.surfaceVariant.copy(alpha = 0.4f),
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
        modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label, style = MaterialTheme.typography.bodySmall, color = colorScheme.onSurfaceVariant
        )
        Text(value, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun SubmitOrderBar(
    isSubmitting: Boolean, enabled: Boolean, onSubmit: () -> Unit
) {
    Surface(shadowElevation = 8.dp) {
        Button(
            onClick = onSubmit,
            enabled = enabled && !isSubmitting,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .height(52.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(Modifier.size(18.dp), color = colorScheme.onPrimary)
            } else {
                Icon(Icons.Default.CheckCircle, null, Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Confirm Order", style = MaterialTheme.typography.titleSmall)
            }
        }
    }
}

// ── Success dialog ─────────────────────────────────────────────────────────────

@Composable
private fun OrderSubmittedDialog(orderTypeLabel: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.primary) },
        title = { Text("$orderTypeLabel submitted successfully") },
        text = { Text("Please wait till it will be processed.") },
        confirmButton = { Button(onClick = onDismiss) { Text("OK") } }
    )
}

// ── Previews ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
private fun CatalogOrderPreview() {
    AppTheme {
        OrderSummaryScreenContent(
            uiState = OrderSummaryUiState(
                orderDetail = OrderDetail.CatalogOrder(sampleCatalogOrder)
            ), onBack = {}, onSubmit = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun InsoleOrderPreview() {
    AppTheme {
        OrderSummaryScreenContent(
            uiState = OrderSummaryUiState(
                orderDetail = OrderDetail.InsoleOrder(
                    config = InsoleOrderConfig(
                        orderId = "ins_1", quantity = 1, priceEstimate = PriceEstimate(
                            "ins_1", basePrice = 80.0, subtotal = 80.0, total = 80.0, quantity = 1
                        ), rightFoot = InsoleFootConfig(
                            euSize = 42, lengthMm = 268f, widthMm = 98f, side = LegSide.RIGHT
                        ), leftFoot = InsoleFootConfig(
                            euSize = 42, lengthMm = 265f, widthMm = 96f, side = LegSide.LEFT
                        ), material = Material.SILICONE, color = ProductColor("Black", "#000000")
                    ), status = OrderStatus.DRAFT
                )
            ), onBack = {}, onSubmit = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun ProstheticCoverOrderPreview() {
    AppTheme {
        OrderSummaryScreenContent(
            uiState = OrderSummaryUiState(
                orderDetail = OrderDetail.ProstheticCoverOrder(
                    config = ProstheticCoverOrderConfig(
                        orderId = "cov_1",
                        quantity = 1,
                        priceEstimate = PriceEstimate(
                            "cov_1",
                            basePrice = 320.0,
                            subtotal = 340.0,
                            total = 340.0,
                            quantity = 1
                        ),
                        limbSide = LegSide.LEFT,
                        limbLevel = LimbLevel.BELOW_KNEE,
                        prostheticType = ProstheticTypeEnum.BELOW_KNEE_PINLOCK_BUTTON,
                        color = ProductColor("Carbon Black", "#1a1a1a"),
                        material = Material.CARBON_FIBER,
                        texture = Texture("t1", "Matte", null, null),
                        fasteningType = FasteningType.STRAP,
                        createdAt = System.currentTimeMillis()
                    ), status = OrderStatus.IN_PRODUCTION
                )
            ), onBack = {}, onSubmit = {})
    }
}

private val sampleCatalogOrder = Order(
    id = "ord_1", adaptedModelId = null, config = ProductConfig(
        id = "conf_1",
        productId = "prod_1",
        orderCategory = OrderCategory.ACCESSORY,
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
    ), priceEstimate = PriceEstimate(
        "conf_1", basePrice = 100.0, subtotal = 110.0, total = 110.0, quantity = 1
    ), status = OrderStatus.DRAFT, createdAt = 0L
)