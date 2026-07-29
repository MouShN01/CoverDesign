package com.example.coverdesign.ui.screens.config

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import com.example.coverdesign.ui.components.ErrorScreen
import com.example.coverdesign.ui.components.EventHandler
import com.example.coverdesign.ui.components.LoadingScreen
import com.example.coverdesign.ui.theme.AppTheme
import com.example.coverdesign.ui.viewmodel.ConfigEntryMode
import com.example.coverdesign.ui.viewmodel.OrderType
import com.example.coverdesign.ui.viewmodel.ProductConfigAction
import com.example.coverdesign.ui.viewmodel.ProductConfigUiEvent
import com.example.coverdesign.ui.viewmodel.ProductConfigUiState
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

                state.orderType == null && state.currentConfig == null -> TypePickerContent(
                    onAction = viewModel::onAction, modifier = Modifier.padding(padding)
                )

                state.currentConfig != null -> ProductConfigContent(
                    state = state, onAction = viewModel::onAction, modifier = Modifier.padding(padding)
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

    if (showProceedDialog) {
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
        config.measurement.height in 100.0..220.0 && !config.userPhotoUri.isNullOrEmpty()
    } else {
        true
    }
}

@Composable
private fun ResumeConfigDialog(
    previousConfig: ProductConfig, onResume: () -> Unit, onStartFresh: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onStartFresh,
        title = { Text("Continue previous configuration?") },
        text = {
            Text(
                "You previously configured this product " + "(material: ${previousConfig.material.displayName}, " + "quantity: ${previousConfig.quantity}). " + "Continue editing or start fresh?"
            )
        },
        confirmButton = { TextButton(onClick = onResume) { Text("Continue") } },
        dismissButton = { TextButton(onClick = onStartFresh) { Text("Start fresh") } })
}

// ═══════════════════════════════════════════════════════════════════
// Internal scaffold used by preview
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

// ═══════════════════════════════════════════════════════════════════
// Shared section container — used by all ConfigContent files
// ═══════════════════════════════════════════════════════════════════

@Composable
internal fun ConfigSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                content = content
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// Shared helpers — available to all config content files
// ═══════════════════════════════════════════════════════════════════

internal fun allowedMaterials(category: OrderCategory): List<Material> = when (category) {
    OrderCategory.INSOLE -> listOf(
        Material.TPU, Material.SILICONE, Material.RUBBER, Material.NYLON, Material.LEATHER
    )

    OrderCategory.COSMETIC_COVER -> listOf(
        Material.PLA, Material.ABS, Material.NYLON, Material.PETG, Material.RESIN, Material.TPU
    )

    OrderCategory.CLOTHING -> listOf(
        Material.COTTON, Material.POLYESTER, Material.DENIM, Material.LEATHER, Material.NYLON
    )

    OrderCategory.FOOTWEAR -> listOf(
        Material.LEATHER, Material.RUBBER, Material.NYLON
    )

    OrderCategory.JEWELRY -> listOf(
        Material.STAINLESS_STEEL,
        Material.TITANIUM,
        Material.SILVER,
        Material.GOLD,
        Material.EPOXY_RESIN,
        Material.PLASTIC
    )

    OrderCategory.ACCESSORY -> listOf(
        Material.LEATHER,
        Material.NYLON,
        Material.PLASTIC,
        Material.STAINLESS_STEEL,
        Material.EPOXY_RESIN,
        Material.SILICONE
    )

    OrderCategory.THREE_D_PRINT -> listOf(
        Material.PLA, Material.ABS, Material.NYLON, Material.PETG, Material.RESIN, Material.TPU
    )

    OrderCategory.INTERIOR_DECOR -> listOf(
        Material.WOOD,
        Material.PLASTIC,
        Material.EPOXY_RESIN,
        Material.METAL,
        Material.CERAMIC,
        Material.GLASS
    )

    else -> Material.values().toList()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MaterialSelector(
    selected: Material,
    allowedMaterials: List<Material>,
    readOnly: Boolean = false,
    onChanged: (Material) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(allowedMaterials) {
        if (selected !in allowedMaterials && allowedMaterials.isNotEmpty()) {
            onChanged(allowedMaterials.first())
        }
    }

    ExposedDropdownMenuBox(
        expanded = expanded, onExpandedChange = {
            if (!readOnly) {
                expanded = !expanded
            }
        }) {
        OutlinedTextField(
            value = selected.displayName,
            onValueChange = {},
            readOnly = true,
            label = { Text("Material") },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            shape = RoundedCornerShape(10.dp)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            allowedMaterials.forEach { material ->
                DropdownMenuItem(
                    text = { Text(material.displayName) },
                    onClick = { onChanged(material); expanded = false },
                    leadingIcon = if (material == selected) ({
                        Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary)
                    }) else null
                )
            }
        }
    }
}

@Composable
internal fun InscriptionField(value: String, error: String?, onChanged: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = { if (it.length <= 50) onChanged(it) },
        label = { Text("Inscription") },
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("Name, phrase, initials…") },
        isError = error != null,
        supportingText = {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(error ?: "", color = MaterialTheme.colorScheme.error)
                Text("${value.length}/50", style = MaterialTheme.typography.labelSmall)
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(10.dp)
    )
}

@Composable
internal fun QuantitySelector(value: Int, error: String?, onChanged: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("Quantity", style = MaterialTheme.typography.bodyMedium)
        Row(verticalAlignment = CenterVertically) {
            IconButton(onClick = { if (value > 1) onChanged(value - 1) }, enabled = value > 1) {
                Icon(Icons.Default.Remove, null)
            }
            Text(
                value.toString(),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
            IconButton(onClick = { if (value < 999) onChanged(value + 1) }, enabled = value < 999) {
                Icon(Icons.Default.Add, null)
            }
        }
    }
    error?.let {
        Text(
            it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
fun PriceCard(price: PriceEstimate) {
    Surface(
        shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Column(
            modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text("Price breakdown", style = MaterialTheme.typography.titleSmall)

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            PriceRow("Base price", price.basePrice)

            price.materialSurcharges.forEach { (name, amount) ->
                if (amount > 0.0) PriceRow("  Material ($name)", amount)
            }

            price.textureSurcharge?.let { if (it > 0.0) PriceRow("  Texture",
                price.textureSurcharge!!
            ) }

            price.inscriptionSurcharge?.let {
                if (it > 0.0) PriceRow(
                    "  Inscription", price.inscriptionSurcharge!!
                )
            }

            price.elementSurcharges?.forEach { (name, amount) ->
                if (amount > 0.0) PriceRow(
                    "  ${
                        name.lowercase().replaceFirstChar { it.uppercase() }
                    }", amount)
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))

            if (price.sizeMultiplier != 1.0 || !price.sizeMultiplierLabel.isNullOrEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Size (${price.sizeMultiplierLabel})",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        "×${String.format("%.2f", price.sizeMultiplier)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (price.quantity > 1) {
                PriceRow("Subtotal (1 unit)", price.subtotal)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Quantity", style = MaterialTheme.typography.bodySmall)
                    Text(
                        "×${price.quantity}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            PriceRow("Total", price.total, isTotal = true, currency = price.currency)
        }
    }
}

@Composable
private fun PriceRow(
    label: String, amount: Double, isTotal: Boolean = false, currency: String = "USD"
) {
    val symbol = when (currency) {
        "EUR" -> "€"; "GBP" -> "£"; else -> "$"
    }
    Row(
        modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label, style = if (isTotal) MaterialTheme.typography.titleSmall
            else MaterialTheme.typography.bodySmall
        )
        Text(
            "$symbol${String.format("%.2f", amount)}",
            style = if (isTotal) MaterialTheme.typography.titleSmall
            else MaterialTheme.typography.bodySmall,
            color = if (isTotal) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun ProceedBottomBar(
    priceEstimate: PriceEstimate?, isLoading: Boolean, enabled: Boolean, onProceed: () -> Unit
) {
    Surface(shadowElevation = 8.dp, color = MaterialTheme.colorScheme.surface) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            priceEstimate?.let {
                Column {
                    Text("Total", style = MaterialTheme.typography.labelSmall)
                    Text(
                        "$${String.format("%.2f", it.total)}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Button(
                onClick = onProceed,
                enabled = enabled && !isLoading,
                modifier = Modifier.height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        Modifier.size(18.dp), color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Preview 3D Model")
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.Default.ArrowForward, null, Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun DirectOrderBar(
    label: String, isSaving: Boolean, enabled: Boolean, onClick: () -> Unit
) {
    Surface(shadowElevation = 8.dp, color = MaterialTheme.colorScheme.surface) {
        Button(
            onClick = onClick,
            enabled = enabled && !isSaving,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    Modifier.size(18.dp), color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text(label)
                Spacer(Modifier.width(8.dp))
                Icon(Icons.Default.ArrowForward, null, Modifier.size(18.dp))
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// Preview
// ═══════════════════════════════════════════════════════════════════

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