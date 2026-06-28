package com.example.coverdesign.ui.screens.config

import android.annotation.SuppressLint
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ViewInAr
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
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.example.coverdesign.R
import com.example.coverdesign.domain.model.AdditionalElement
import com.example.coverdesign.domain.model.Material
import com.example.coverdesign.domain.model.Measurement
import com.example.coverdesign.domain.model.Product
import com.example.coverdesign.domain.model.ProductCategory
import com.example.coverdesign.domain.model.ProductColor
import com.example.coverdesign.domain.model.ProductConfig
import com.example.coverdesign.domain.model.SizeLabel
import com.example.coverdesign.domain.model.Texture
import com.example.coverdesign.domain.model.pricing.PriceEstimate
import com.example.coverdesign.domain.util.generateUuid
import com.example.coverdesign.ui.components.AppTopBar
import com.example.coverdesign.ui.components.ErrorScreen
import com.example.coverdesign.ui.components.EventHandler
import com.example.coverdesign.ui.components.LoadingScreen
import com.example.coverdesign.ui.constants.UIConstants.DEFAULT_TEXTURES
import com.example.coverdesign.ui.constants.UIConstants.TEXTURE_ID_PREFIX
import com.example.coverdesign.ui.screens.ArMode
import com.example.coverdesign.ui.screens.config.measurement_section.MeasurementSection
import com.example.coverdesign.ui.screens.resolveArMode
import com.example.coverdesign.ui.theme.AppTheme
import com.example.coverdesign.ui.viewmodel.ConfigEntryMode
import com.example.coverdesign.ui.viewmodel.ProductConfigAction
import com.example.coverdesign.ui.viewmodel.ProductConfigUiEvent
import com.example.coverdesign.ui.viewmodel.ProductConfigUiState
import com.example.coverdesign.ui.viewmodel.ProductConfigViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ProductConfigScreen(
    entryMode: ConfigEntryMode,
    viewModel: ProductConfigViewModel = koinViewModel(),
    onProceed: (configId: String) -> Unit,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(entryMode) {
        viewModel.onAction(ProductConfigAction.Enter(entryMode))
    }

    LaunchedEffect(state.error) {
        state.error?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Long
            )
            viewModel.onAction(ProductConfigAction.ClearError)
        }
    }
    LaunchedEffect(state.isSaving) {
        if (!state.isSaving) snackbarHostState.currentSnackbarData?.dismiss()
    }

    EventHandler(viewModel.events) { event ->
        when (event) {
            is ProductConfigUiEvent.NavigateToPreview -> onProceed(event.configId)
        }
    }

    var showProceedDialog by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            AppTopBar(
                title = state.selectedProduct?.name ?: "Configure",
                onBack = onBack
            )
        },
        bottomBar = {
            ProceedBottomBar(
                priceEstimate = state.priceEstimate,
                isLoading = state.isSaving,
                enabled = state.currentConfig != null &&
                        state.measurementError == null &&
                        isProceedEnabled(state.currentConfig),
                onProceed = { showProceedDialog = true }
            )
        }
    ) { padding ->
        when {
            state.isLoading -> LoadingScreen()
            state.error != null && state.currentConfig == null -> ErrorScreen(state.error!!)
            state.currentConfig != null -> ProductConfigContent(
                state = state,
                onAction = viewModel::onAction,
                modifier = Modifier.padding(padding)
            )
        }
    }

    // Proceed confirmation — warns user about generation time
    if (showProceedDialog) {
        state.currentConfig?.let { config ->
            ProceedConfirmationDialog(
                config = config,
                onConfirm = {
                    showProceedDialog = false
                    viewModel.onAction(ProductConfigAction.SaveAndProceed)
                },
                onDismiss = { showProceedDialog = false }
            )
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
            }
        )
    }
}


// Returns false for CLOTHING when height has not been entered or is unrealistic
private fun isProceedEnabled(config: ProductConfig?): Boolean {
    config ?: return false
    return if (config.productCategory == ProductCategory.CLOTHING) {
        config.measurement.height in 100.0..220.0
    } else {
        true
    }
}

@Composable
private fun ResumeConfigDialog(
    previousConfig: ProductConfig,
    onResume: () -> Unit,
    onStartFresh: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onStartFresh,
        title = { Text("Continue previous configuration?") },
        text = {
            Text(
                "You previously configured this product " +
                        "(material: ${previousConfig.material.displayName}, " +
                        "quantity: ${previousConfig.quantity}). " +
                        "Continue editing or start fresh?"
            )
        },
        confirmButton = { TextButton(onClick = onResume) { Text("Continue") } },
        dismissButton = { TextButton(onClick = onStartFresh) { Text("Start fresh") } }
    )
}

// ═══════════════════════════════════════════════════════════════════
// Internal scaffold used by preview
// ═══════════════════════════════════════════════════════════════════

@Composable
private fun ProductConfigScreenContent(
    state: ProductConfigUiState,
    onAction: (ProductConfigAction) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(R.string.configure_product),
                onBack = onBack
            )
        },
        bottomBar = {
            ProceedBottomBar(
                priceEstimate = state.priceEstimate,
                isLoading = state.isSaving,
                enabled = state.currentConfig != null && state.measurementError == null,
                onProceed = { onAction(ProductConfigAction.SaveAndProceed) }
            )
        }
    ) { padding ->
        if (state.isLoading) LoadingScreen()
        else ProductConfigContent(
            state = state,
            onAction = onAction,
            modifier = Modifier.padding(padding)
        )
    }
}

// ═══════════════════════════════════════════════════════════════════
// Main content scroll column
// ═══════════════════════════════════════════════════════════════════

@Composable
private fun ProductConfigContent(
    state: ProductConfigUiState,
    onAction: (ProductConfigAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val config = state.currentConfig ?: return

    // resolve AR mode up-front so PhotoUploadSection can adapt
    val arMode = config.productCategory.resolveArMode(
        photoUri = config.userPhotoUri,
        measurements = config.measurement
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // ── AR mode badge ─────────────────────────────────────────────────
        // ── Product header — photo + name + category + base price ──────────
        state.selectedProduct?.let { product ->
            ProductHeader(product = product)
        }

        ArModeBadge(arMode = arMode)

        // ── Measurements — content adapts per product category ───────────
        ConfigSection(title = "Measurements & Size") {
            MeasurementSection(
                category = config.productCategory,
                measurement = config.measurement,
                sizeLabel = config.sizeLabel,
                measError = state.measurementError,
                onMeasure = { onAction(ProductConfigAction.UpdateMeasurement(it)) },
                onSize = { onAction(ProductConfigAction.UpdateSizeLabel(it)) }
            )
        }

        // ── Appearance ────────────────────────────────────────────────────
        ConfigSection(title = "Appearance") {
            MaterialSelector(
                selected = config.material,
                allowedMaterials = allowedMaterials(config.productCategory),
                onChanged = { onAction(ProductConfigAction.UpdateMaterial(it)) }
            )
            ColorPicker(
                selected = config.color,
                onChanged = { onAction(ProductConfigAction.UpdateColor(it)) }
            )
            TextureSelector(
                selected = config.texture,
                onChanged = { onAction(ProductConfigAction.UpdateTexture(it)) }
            )
        }

        // ── Personalisation ───────────────────────────────────────────────
        ConfigSection(title = "Personalisation") {
            InscriptionField(
                value = config.inscription,
                error = state.inscriptionError,
                onChanged = { onAction(ProductConfigAction.UpdateInscription(it)) }
            )
            AdditionalElementsSelector(
                category = config.productCategory,
                selected = config.additionalElements,
                onAdd = { onAction(ProductConfigAction.AddElement(it)) },
                onRemove = { onAction(ProductConfigAction.RemoveElement(it)) }
            )
        }

        // ── Photo / AR section — adapts to product category ───────────────
        ConfigSection(title = "Fitting & Try-On") {
            PhotoUploadSection(
                currentUri = config.userPhotoUri,
                arMode = arMode,
                productCategory = config.productCategory,
                onChanged = { onAction(ProductConfigAction.UpdateUserPhoto(it)) }
            )
        }

        // ── Order options ─────────────────────────────────────────────────
        ConfigSection(title = "Order") {
            QuantitySelector(
                value = config.quantity,
                error = state.quantityError,
                onChanged = { onAction(ProductConfigAction.UpdateQuantity(it)) }
            )
        }

        // ── Price ─────────────────────────────────────────────────────────
        state.priceEstimate?.let { PriceCard(price = it) }
        OutlinedButton(
            onClick = { onAction(ProductConfigAction.RequestPriceUpdate) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isPriceLoading
        ) {
            if (state.isPriceLoading) CircularProgressIndicator(Modifier.size(16.dp))
            else Text("Calculate Price")
        }

        Spacer(Modifier.height(80.dp))
    }
}

// ═══════════════════════════════════════════════════════════════════
// Product header card — shown at top of the config form
// ═══════════════════════════════════════════════════════════════════

@SuppressLint("DefaultLocale")
@Composable
private fun ProductHeader(product: Product) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = CenterVertically
        ) {
            // product image
            Surface(
                modifier = Modifier.size(80.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                if (product.imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = product.imageUrl,
                        contentDescription = product.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // placeholder when no image URL
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ViewInAr,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }

            // text info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = product.category.name,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                if (product.description.isNotEmpty()) {
                    Text(
                        text = product.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }
                Text(
                    text = "Base price: $${String.format("%.2f", product.basePrice)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// AR mode badge — shows at top of config form
// ═══════════════════════════════════════════════════════════════════

@Composable
private fun ArModeBadge(arMode: ArMode) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.tertiaryContainer
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = arMode.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "Try-on mode: ${arMode.label}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}

@Composable
private fun ConfigSection(title: String, content: @Composable ColumnScope.() -> Unit) {
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


// ─────────────────────────────────────────────────────────────────────
// Material restrictions — not all materials make sense for every category
// ─────────────────────────────────────────────────────────────────────


private fun allowedMaterials(category: ProductCategory): List<Material> = when (category) {
    ProductCategory.CLOTHING -> listOf(
        Material.COTTON,
        Material.POLYESTER,
        Material.DENIM,
        Material.LEATHER,   // leather jacket etc.
        Material.NYLON
    )

    ProductCategory.FOOTWEAR -> listOf(
        Material.LEATHER,
        Material.RUBBER,
        Material.NYLON,
    )

    ProductCategory.JEWELRY -> listOf(
        Material.STAINLESS_STEEL,
        Material.TITANIUM,
        Material.SILVER,
        Material.GOLD,
        Material.EPOXY_RESIN,
        Material.PLASTIC
    )

    ProductCategory.ACCESSORY -> listOf(
        Material.LEATHER,
        Material.NYLON,
        Material.PLASTIC,
        Material.STAINLESS_STEEL,
        Material.EPOXY_RESIN,
        Material.SILICONE
    )

    ProductCategory.THREE_D_PRINT -> listOf(
        Material.PLA,
        Material.ABS,
        Material.NYLON,
        Material.PETG,
        Material.RESIN,
        Material.TPU
    )

    ProductCategory.INTERIOR_DECOR -> listOf(
        Material.WOOD,
        Material.PLASTIC,
        Material.EPOXY_RESIN,
        Material.METAL,
        Material.CERAMIC,
        Material.GLASS
    )

    ProductCategory.OTHER -> Material.values().toList()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MaterialSelector(
    selected: Material,
    allowedMaterials: List<Material>,
    onChanged: (Material) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    // If the currently selected material is not in the allowed list,
    // auto-reset to the first allowed material on first composition.
    LaunchedEffect(allowedMaterials) {
        if (selected !in allowedMaterials && allowedMaterials.isNotEmpty()) {
            onChanged(allowedMaterials.first())
        }
    }

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
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
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TextureSelector(
    selected: Texture,
    onChanged: (Texture) -> Unit
) {
    // val imageTextureId = remember { "$TEXTURE_ID_PREFIX${generateUuid()}" }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            // switch to custom-image texture — name becomes "Image texture"
            onChanged(
                Texture(
                    id = "$TEXTURE_ID_PREFIX${generateUuid()}",
                    name = "Image texture",
                    previewUrl = it.toString(),
                    description = "User-provided texture image"
                )
            )
        }
    }

    val isCustomImage =
        selected.previewUrl != null && selected.id !in DEFAULT_TEXTURES.map { it.id }

    var dropdownExpanded by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

        // ── dropdown row + paperclip ──────────────────────────────────────
        ExposedDropdownMenuBox(
            expanded = dropdownExpanded,
            onExpandedChange = { dropdownExpanded = !dropdownExpanded }
        ) {
            OutlinedTextField(
                value = selected.name,
                onValueChange = {},
                readOnly = true,
                label = { Text("Texture") },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                trailingIcon = {
                    // paperclip sits where the dropdown arrow normally would be
                    Row(verticalAlignment = CenterVertically) {
                        // paperclip
                        IconButton(
                            onClick = { launcher.launch("image/*") },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AttachFile,
                                contentDescription = "Attach texture image",
                                tint = if (isCustomImage)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        // standard dropdown arrow
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded)
                    }
                },
                shape = RoundedCornerShape(10.dp)
            )

            ExposedDropdownMenu(
                expanded = dropdownExpanded,
                onDismissRequest = { dropdownExpanded = false }
            ) {
                DEFAULT_TEXTURES.forEach { texture ->
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(texture.name, style = MaterialTheme.typography.bodyMedium)
                                texture.description?.let {
                                    Text(
                                        it, style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        },
                        onClick = {
                            onChanged(texture)
                            dropdownExpanded = false
                        },
                        leadingIcon = if (selected.id == texture.id) ({
                            Icon(
                                Icons.Default.Check, null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }) else null
                    )
                }
            }
        }

        // ── description of the selected preset ───────────────────────────
        if (!isCustomImage) {
            selected.description?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // ── custom image preview ──────────────────────────────────────────
        if (isCustomImage && !selected.previewUrl.isNullOrEmpty()) {
            Box(modifier = Modifier.fillMaxWidth()) {
                AsyncImage(
                    model = selected.previewUrl,
                    contentDescription = "Texture preview",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    contentScale = ContentScale.Crop
                )
                // clear — revert to first default texture
                IconButton(
                    onClick = { onChanged(DEFAULT_TEXTURES.first()) },
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Remove texture image",
                            modifier = Modifier
                                .padding(4.dp)
                                .size(14.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun InscriptionField(value: String, error: String?, onChanged: (String) -> Unit) {
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
private fun QuantitySelector(value: Int, error: String?, onChanged: (Int) -> Unit) {
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
                value.toString(), style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
            IconButton(onClick = { if (value < 999) onChanged(value + 1) }, enabled = value < 999) {
                Icon(Icons.Default.Add, null)
            }
        }
    }
    error?.let {
        Text(
            it,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
fun PriceCard(price: PriceEstimate) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text("Price breakdown", style = MaterialTheme.typography.titleSmall)

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            // base
            PriceRow("Base price", price.basePrice)

            // material surcharges — one per material
            price.materialSurcharges.forEach { (name, amount) ->
                if (amount > 0.0)
                    PriceRow("  Material ($name)", amount)
            }

            // texture
            if (price.textureSurcharge > 0.0)
                PriceRow("  Texture", price.textureSurcharge)

            // inscription
            if (price.inscriptionSurcharge > 0.0)
                PriceRow("  Inscription", price.inscriptionSurcharge)

            // additional elements
            price.elementSurcharges.forEach { (name, amount) ->
                if (amount > 0.0)
                    PriceRow("  ${name.lowercase().replaceFirstChar { it.uppercase() }}", amount)
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))

            // size multiplier row — only shown when it differs from 1.0
            if (price.sizeMultiplier != 1.0 || price.sizeMultiplierLabel.isNotEmpty()) {
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

            // subtotal (per unit after size)
            if (price.quantity > 1) {
                PriceRow(
                    "Subtotal (1 unit)",
                    price.subtotal
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Quantity",
                        style = MaterialTheme.typography.bodySmall
                    )
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
    label: String,
    amount: Double,
    isTotal: Boolean = false,
    currency: String = "USD"
) {
    val symbol = when (currency) {
        "EUR" -> "€"; "GBP" -> "£"; else -> "$"
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = if (isTotal) MaterialTheme.typography.titleSmall
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
    priceEstimate: PriceEstimate?,
    isLoading: Boolean,
    enabled: Boolean,
    onProceed: () -> Unit
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
                        Modifier.size(18.dp),
                        color = MaterialTheme.colorScheme.onPrimary
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

// ═══════════════════════════════════════════════════════════════════
// Previews
// ═══════════════════════════════════════════════════════════════════

@Preview(showBackground = true, name = "Human Placeholder – Upper Limb (model ready)")
@Composable
private fun PreviewHumanUpper_Ready() {
    AppTheme {
        HumanPlaceholderScreen(
            productCategory = ProductCategory.JEWELRY,
            isModelReady = true,
            onProceedToAr = {},
            onBack = {}
        )
    }
}

@Preview(showBackground = true, name = "Human Placeholder – Lower Limb (loading)")
@Composable
private fun PreviewHumanLower_Loading() {
    AppTheme {
        HumanPlaceholderScreen(
            productCategory = ProductCategory.FOOTWEAR,
            isModelReady = false,
            onProceedToAr = {},
            onBack = {}
        )
    }
}

@Preview(showBackground = true, name = "Human Placeholder – Clothing")
@Composable
private fun PreviewHumanClothing() {
    AppTheme {
        HumanPlaceholderScreen(
            productCategory = ProductCategory.CLOTHING,
            isModelReady = true,
            onProceedToAr = {},
            onBack = {}
        )
    }
}

@Preview(showBackground = true, name = "Config Screen – Jewellery")
@Composable
private fun PreviewConfigScreen() {
    val sampleProduct = Product(
        id = "prod-001",
        name = "Forearm Cover",
        description = "Personalised prosthetic forearm cover.",
        basePrice = 149.99,
        category = ProductCategory.JEWELRY,
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
        productCategory = ProductCategory.JEWELRY,
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
            ),
            onAction = {},
            onBack = {}
        )
    }
}