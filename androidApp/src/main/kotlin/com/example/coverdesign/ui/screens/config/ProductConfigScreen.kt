package com.example.coverdesign.ui.screens.config

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.input.KeyboardType
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
import com.example.coverdesign.ui.components.AppTopBar
import com.example.coverdesign.ui.components.ErrorScreen
import com.example.coverdesign.ui.components.EventHandler
import com.example.coverdesign.ui.components.LoadingScreen
import com.example.coverdesign.ui.screens.ArMode
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
    entryMode : ConfigEntryMode,
    viewModel : ProductConfigViewModel = koinViewModel(),
    onProceed : (configId: String) -> Unit,
    onBack    : () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(entryMode) {
        viewModel.onAction(ProductConfigAction.Enter(entryMode))
    }

    EventHandler(viewModel.events) { event ->
        when (event) {
            is ProductConfigUiEvent.NavigateToPreview -> onProceed(event.configId)
        }
    }

    var showProceedDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            AppTopBar(
                title  = state.selectedProduct?.name ?: "Configure",
                onBack = onBack
            )
        },
        bottomBar = {
            ProceedBottomBar(
                priceEstimate = state.priceEstimate,
                isLoading     = state.isSaving,
                enabled       = state.currentConfig != null &&
                        state.measurementError == null &&
                        isProceedEnabled(state.currentConfig),
                onProceed     = { showProceedDialog = true }
            )
        }
    ) { padding ->
        when {
            state.isLoading -> LoadingScreen()
            state.error != null && state.currentConfig == null -> ErrorScreen(state.error!!)
            state.currentConfig != null -> ProductConfigContent(
                state    = state,
                onAction = viewModel::onAction,
                modifier = Modifier.padding(padding)
            )
        }
    }

    // Proceed confirmation — warns user about generation time
    if (showProceedDialog) {
        state.currentConfig?.let { config ->
            ProceedConfirmationDialog(
                config    = config,
                onConfirm = {
                    showProceedDialog = false
                    viewModel.onAction(ProductConfigAction.SaveAndProceed)
                },
                onDismiss = { showProceedDialog = false }
            )
        }
    }

    // FR3 — resume or discard prior draft
    state.pendingResumePrompt?.let { previousConfig ->
        ResumeConfigDialog(
            previousConfig = previousConfig,
            onResume       = { viewModel.onAction(ProductConfigAction.ResumePreviousConfig(previousConfig)) },
            onStartFresh   = {
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

// ─────────────────────────────────────────────────────────────────────
// Proceed confirmation dialog — shown before kicking off 3D generation
// ─────────────────────────────────────────────────────────────────────


@Composable
private fun ResumeConfigDialog(
    previousConfig : ProductConfig,
    onResume       : () -> Unit,
    onStartFresh   : () -> Unit
) {
    AlertDialog(
        onDismissRequest = onStartFresh,
        title = { Text("Continue previous configuration?") },
        text  = {
            Text(
                "You previously configured this product " +
                        "(material: ${previousConfig.material.displayName}, " +
                        "quantity: ${previousConfig.quantity}). " +
                        "Continue editing or start fresh?"
            )
        },
        confirmButton = { TextButton(onClick = onResume)     { Text("Continue") } },
        dismissButton = { TextButton(onClick = onStartFresh) { Text("Start fresh") } }
    )
}

// ═══════════════════════════════════════════════════════════════════
// Internal scaffold used by preview
// ═══════════════════════════════════════════════════════════════════

@Composable
private fun ProductConfigScreenContent(
    state    : ProductConfigUiState,
    onAction : (ProductConfigAction) -> Unit,
    onBack   : () -> Unit
) {
    Scaffold(
        topBar = {
            AppTopBar(
                title  = stringResource(R.string.configure_product),
                onBack = onBack
            )
        },
        bottomBar = {
            ProceedBottomBar(
                priceEstimate = state.priceEstimate,
                isLoading     = state.isSaving,
                enabled       = state.currentConfig != null && state.measurementError == null,
                onProceed     = { onAction(ProductConfigAction.SaveAndProceed) }
            )
        }
    ) { padding ->
        if (state.isLoading) LoadingScreen()
        else ProductConfigContent(
            state    = state,
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
    state    : ProductConfigUiState,
    onAction : (ProductConfigAction) -> Unit,
    modifier : Modifier = Modifier
) {
    val config = state.currentConfig ?: return

    // resolve AR mode up-front so PhotoUploadSection can adapt
    val arMode = config.productCategory.resolveArMode(
        photoUri     = config.userPhotoUri,
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
                category    = config.productCategory,
                measurement = config.measurement,
                sizeLabel   = config.sizeLabel,
                measError   = state.measurementError,
                onMeasure   = { onAction(ProductConfigAction.UpdateMeasurement(it)) },
                onSize      = { onAction(ProductConfigAction.UpdateSizeLabel(it)) }
            )
        }

        // ── Appearance ────────────────────────────────────────────────────
        ConfigSection(title = "Appearance") {
            MaterialSelector(
                selected         = config.material,
                allowedMaterials = allowedMaterials(config.productCategory),
                onChanged        = { onAction(ProductConfigAction.UpdateMaterial(it)) }
            )
            ColorPicker(
                selected  = config.color,
                onChanged = { onAction(ProductConfigAction.UpdateColor(it)) }
            )
            TextureSelector(
                selected  = config.texture,
                onChanged = { onAction(ProductConfigAction.UpdateTexture(it)) }
            )
        }

        // ── Personalisation ───────────────────────────────────────────────
        ConfigSection(title = "Personalisation") {
            InscriptionField(
                value     = config.inscription,
                error     = state.inscriptionError,
                onChanged = { onAction(ProductConfigAction.UpdateInscription(it)) }
            )
            AdditionalElementsSelector(
                category = config.productCategory,
                selected = config.additionalElements,
                onAdd    = { onAction(ProductConfigAction.AddElement(it)) },
                onRemove = { onAction(ProductConfigAction.RemoveElement(it)) }
            )
        }

        // ── Photo / AR section — adapts to product category ───────────────
        ConfigSection(title = "Fitting & Try-On") {
            PhotoUploadSection(
                currentUri      = config.userPhotoUri,
                arMode          = arMode,
                productCategory = config.productCategory,
                onChanged       = { onAction(ProductConfigAction.UpdateUserPhoto(it)) }
            )
        }

        // ── Order options ─────────────────────────────────────────────────
        ConfigSection(title = "Order") {
            QuantitySelector(
                value     = config.quantity,
                error     = state.quantityError,
                onChanged = { onAction(ProductConfigAction.UpdateQuantity(it)) }
            )
        }

        // ── Price ─────────────────────────────────────────────────────────
        state.priceEstimate?.let { PriceCard(price = it) }
        OutlinedButton(
            onClick  = { onAction(ProductConfigAction.RequestPriceUpdate) },
            modifier = Modifier.fillMaxWidth(),
            enabled  = !state.isPriceLoading
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

@Composable
private fun ProductHeader(product: Product) {
    Surface(
        shape  = RoundedCornerShape(16.dp),
        color  = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier              = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment     = CenterVertically
        ) {
            // product image
            Surface(
                modifier = Modifier.size(80.dp),
                shape    = RoundedCornerShape(12.dp),
                color    = MaterialTheme.colorScheme.surface
            ) {
                if (product.imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model              = product.imageUrl,
                        contentDescription = product.name,
                        modifier           = Modifier.fillMaxSize(),
                        contentScale       = ContentScale.Crop
                    )
                } else {
                    // placeholder when no image URL
                    Box(
                        modifier         = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector        = Icons.Default.ViewInAr,
                            contentDescription = null,
                            tint               = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            modifier           = Modifier.size(36.dp)
                        )
                    }
                }
            }

            // text info
            Column(
                modifier            = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text  = product.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text  = product.category.name,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                if (product.description.isNotEmpty()) {
                    Text(
                        text     = product.description,
                        style    = MaterialTheme.typography.bodySmall,
                        color    = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }
                Text(
                    text  = "Base price: ${"$"}${String.format("%.2f", product.basePrice)}",
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
            modifier              = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment     = CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector        = arMode.icon,
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.tertiary,
                modifier           = Modifier.size(16.dp)
            )
            Text(
                text  = "Try-on mode: ${arMode.label}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}


// ═══════════════════════════════════════════════════════════════════
// HumanPlaceholderScreen
// Shown after 3D generation is triggered.  Displays a body silhouette
// with the relevant part highlighted and waits for the model to arrive
// from the server.  When ready, the user can proceed to live AR.
// ═══════════════════════════════════════════════════════════════════


// ═══════════════════════════════════════════════════════════════════
// Body silhouette — Compose-only (no SVG dependency needed)
// Highlights the relevant body segment for each product category.
// ═══════════════════════════════════════════════════════════════════

@Composable
private fun BodySilhouette(
    category     : ProductCategory,
    isModelReady : Boolean,
    modifier     : Modifier = Modifier
) {
    val bodyColor    = MaterialTheme.colorScheme.outlineVariant
    val highlightOn  = MaterialTheme.colorScheme.primary
    val highlightOff = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
    val accent       = if (isModelReady) highlightOn else highlightOff

    // Jewellery / accessories -> highlight arms (wrists, hands, neck area)
    fun isUpperLimb()  = category == ProductCategory.JEWELRY ||
            category == ProductCategory.ACCESSORY
    // Footwear -> highlight lower legs / feet
    fun isLowerLimb()  = category == ProductCategory.FOOTWEAR
    // Clothing / 3D-print -> highlight full torso
    fun isTorso()      = category == ProductCategory.CLOTHING ||
            category == ProductCategory.THREE_D_PRINT
    fun isFullBody()   = category == ProductCategory.CLOTHING ||
            category == ProductCategory.THREE_D_PRINT

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // Head
            Surface(
                modifier = Modifier.size(38.dp),
                shape    = CircleShape,
                color    = if (isTorso()) accent else bodyColor,
                border   = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline)
            ) {}

            Spacer(Modifier.height(2.dp))

            // Torso + arms row
            Row(
                verticalAlignment     = Alignment.Top,
                horizontalArrangement = Arrangement.Center
            ) {
                // left side — upper arm + forearm
                Column(horizontalAlignment = Alignment.End) {
                    Surface(   // upper arm
                        modifier = Modifier.width(20.dp).height(72.dp),
                        shape    = RoundedCornerShape(10.dp),
                        color    = if (isUpperLimb() || isFullBody()) accent else bodyColor,
                        border   = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {}
                    Spacer(Modifier.height(2.dp))
                    Surface(   // forearm + hand
                        modifier = Modifier.width(17.dp).height(58.dp),
                        shape    = RoundedCornerShape(9.dp),
                        color    = if (isUpperLimb()) accent else bodyColor,
                        border   = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {}
                }

                Spacer(Modifier.width(4.dp))

                // torso
                Surface(
                    modifier = Modifier.width(58.dp).height(120.dp),
                    shape    = RoundedCornerShape(
                        topStart    = 10.dp, topEnd    = 10.dp,
                        bottomStart = 6.dp,  bottomEnd = 6.dp
                    ),
                    color  = if (isTorso()) accent else bodyColor,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {}

                Spacer(Modifier.width(4.dp))

                // right side — upper arm + forearm
                Column(horizontalAlignment = Alignment.Start) {
                    Surface(   // upper arm
                        modifier = Modifier.width(20.dp).height(72.dp),
                        shape    = RoundedCornerShape(10.dp),
                        color    = if (isUpperLimb() || isFullBody()) accent else bodyColor,
                        border   = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {}
                    Spacer(Modifier.height(2.dp))
                    Surface(   // forearm + hand
                        modifier = Modifier.width(17.dp).height(58.dp),
                        shape    = RoundedCornerShape(9.dp),
                        color    = if (isUpperLimb()) accent else bodyColor,
                        border   = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {}
                }
            }

            // Legs
            Spacer(Modifier.height(2.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                repeat(2) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(   // thigh
                            modifier = Modifier.width(28.dp).height(80.dp),
                            shape    = RoundedCornerShape(9.dp),
                            color    = if (isLowerLimb() || isFullBody()) accent else bodyColor,
                            border   = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {}
                        Spacer(Modifier.height(2.dp))
                        Surface(   // lower leg
                            modifier = Modifier.width(23.dp).height(70.dp),
                            shape    = RoundedCornerShape(8.dp),
                            color    = if (isLowerLimb()) accent else bodyColor,
                            border   = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {}
                    }
                }
            }
        }

        // 3D model-ready indicator on the highlighted zone
        if (isModelReady) {
            val alignment = when {
                isUpperLimb() -> Alignment.CenterEnd
                isLowerLimb() -> Alignment.BottomCenter
                else          -> Alignment.Center
            }
            Surface(
                modifier = Modifier
                    .align(alignment)
                    .padding(6.dp)
                    .size(44.dp),
                shape  = CircleShape,
                color  = highlightOn.copy(alpha = 0.12f),
                border = BorderStroke(2.dp, highlightOn)
            ) {
                Icon(
                    imageVector        = Icons.Default.ViewInAr,
                    contentDescription = "3D model ready",
                    tint               = highlightOn,
                    modifier           = Modifier.fillMaxSize().padding(10.dp)
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// Common section composables (unchanged from original)
// ═══════════════════════════════════════════════════════════════════

@Composable
private fun ConfigSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Surface(
            shape  = RoundedCornerShape(16.dp),
            color  = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier            = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                content             = content
            )
        }
    }
}

@Composable
fun MeasurementSection(
    category    : ProductCategory,
    measurement : Measurement,
    sizeLabel   : SizeLabel?,
    measError   : String?,
    onMeasure   : (Measurement) -> Unit,
    onSize      : (SizeLabel?) -> Unit
) {
    when (category) {

        // ── FOOTWEAR ──────────────────────────────────────────────────────
        // EU shoe size grid + foot-length input with measuring guide
        ProductCategory.FOOTWEAR -> {
            FootwearSizeSection(
                measurement = measurement,
                sizeLabel   = sizeLabel,
                error       = measError,
                onMeasure   = onMeasure,
                onSize      = onSize
            )
        }

        // ── ACCESSORY ─────────────────────────────────────────────────────
        // Subgroup selector (Ring / Earring / Bracelet) then size inputs
        ProductCategory.ACCESSORY -> {
            AccessorySizeSection(
                measurement = measurement,
                sizeLabel   = sizeLabel,
                error       = measError,
                onMeasure   = onMeasure,
                onSize      = onSize
            )
        }

        // ── THREE_D_PRINT ─────────────────────────────────────────────────
        // Only raw mm measurements; no size grid
        ProductCategory.THREE_D_PRINT -> {
            MeasurementInputs(
                measurement = measurement,
                labels      = listOf("Height", "Width", "Depth"),
                unit        = "mm",
                error       = measError,
                onChanged   = onMeasure
            )
        }

        // ── INTERIOR_DECOR ────────────────────────────────────────────────
        // Only mm measurements; no size grid
        ProductCategory.INTERIOR_DECOR -> {
            MeasurementInputs(
                measurement = measurement,
                labels      = listOf("Height", "Width", "Depth"),
                unit        = "mm",
                error       = measError,
                onChanged   = onMeasure
            )
        }

        // ── CLOTHING ─────────────────────────────────────────────────────
        // Height (cm) required; Weight (kg) + Waist (cm) optional but helpful
        ProductCategory.CLOTHING -> {
            ClothingSizeSection(
                measurement = measurement,
                sizeLabel   = sizeLabel,
                measError   = measError,
                onMeasure   = onMeasure,
                onSize      = onSize
            )
        }

        // ── OTHER / JEWELRY ───────────────────────────────────────────────
        // Standard S/M/L selector + mm measurements
        else -> {
            SizeLabelSelector(
                selected  = sizeLabel,
                onChanged = onSize
            )
            MeasurementInputs(
                measurement = measurement,
                labels      = listOf("Height", "Width", "Depth"),
                unit        = "mm",
                error       = measError,
                onChanged   = onMeasure
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────
// FOOTWEAR section
// ─────────────────────────────────────────────────────────────────────

private val EU_SIZES = listOf(35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46)

// EU size → approximate foot length in mm
private val EU_TO_MM = mapOf(
    35 to 222, 36 to 228, 37 to 235, 38 to 242, 39 to 248,
    40 to 255, 41 to 262, 42 to 268, 43 to 275, 44 to 282,
    45 to 288, 46 to 295
)

// SizeLabel is reused here to store EU size as a string label
// We use SizeLabel.CUSTOM(value) if available, otherwise map EU→closest SizeLabel
// For simplicity we store the raw EU number in measurement.depth (a dedicated field)
// and keep sizeLabel for the S/M/L fallback on other categories.

@Composable
private fun FootwearSizeSection(
    measurement : Measurement,
    sizeLabel   : SizeLabel?,
    error       : String?,
    onMeasure   : (Measurement) -> Unit,
    onSize      : (SizeLabel?) -> Unit
) {
    var showGuide by remember { mutableStateOf(false) }
    // we store selected EU size in measurement.depth repurposed as euSize
    val selectedEu = measurement.depth.toInt().let { if (it < 35 || it > 46) 0 else it }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

        // EU size grid
        Row(
            modifier          = Modifier.fillMaxWidth(),
            verticalAlignment = CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("EU Shoe Size", style = MaterialTheme.typography.bodyMedium)
            IconButton(onClick = { showGuide = true }) {
                Icon(
                    imageVector        = Icons.Default.Info,
                    contentDescription = "How to measure",
                    tint               = MaterialTheme.colorScheme.primary,
                    modifier           = Modifier.size(20.dp)
                )
            }
        }

        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            EU_SIZES.forEach { eu ->
                FilterChip(
                    selected = selectedEu == eu,
                    onClick  = {
                        onMeasure(measurement.copy(depth = eu.toDouble()))
                    },
                    label = { Text(eu.toString()) }
                )
            }
        }

        if (selectedEu > 0) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
            ) {
                Text(
                    "EU $selectedEu ≈ ${EU_TO_MM[selectedEu]} mm foot length",
                    style    = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    color    = MaterialTheme.colorScheme.secondary
                )
            }
        }

        HorizontalDivider()

        // Foot length manual input
        Row(
            modifier          = Modifier.fillMaxWidth(),
            verticalAlignment = CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Foot Length", style = MaterialTheme.typography.bodyMedium)
            IconButton(onClick = { showGuide = true }) {
                Icon(
                    imageVector        = Icons.Default.Info,
                    contentDescription = "Measuring guide",
                    tint               = MaterialTheme.colorScheme.primary,
                    modifier           = Modifier.size(20.dp)
                )
            }
        }
        OutlinedTextField(
            value           = if (measurement.height == 0.0) "" else measurement.height.toString(),
            onValueChange   = { onMeasure(measurement.copy(height = it.toDoubleOrNull() ?: 0.0)) },
            modifier        = Modifier.fillMaxWidth(),
            label           = { Text("Foot length") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            isError         = error != null,
            trailingIcon    = {
                Text("mm", style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(end = 8.dp))
            },
            singleLine = true,
            shape      = RoundedCornerShape(10.dp)
        )
        // Foot width
        OutlinedTextField(
            value           = if (measurement.width == 0.0) "" else measurement.width.toString(),
            onValueChange   = { onMeasure(measurement.copy(width = it.toDoubleOrNull() ?: 0.0)) },
            modifier        = Modifier.fillMaxWidth(),
            label           = { Text("Foot width (widest point)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            trailingIcon    = {
                Text("mm", style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(end = 8.dp))
            },
            singleLine = true,
            shape      = RoundedCornerShape(10.dp)
        )

        error?.let {
            Text(it, color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall)
        }
    }

    if (showGuide) {
        FootMeasuringGuideDialog(onDismiss = { showGuide = false })
    }
}

@Composable
private fun FootMeasuringGuideDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon  = { Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.primary) },
        title = { Text("How to measure your foot") },
        text  = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                GuideStep(
                    number = "1",
                    text   = "Place a blank sheet of paper on a hard floor. Stand on it with your heel touching a wall."
                )
                GuideStep(
                    number = "2",
                    text   = "Mark the tip of your longest toe with a pencil, keeping the pen vertical."
                )
                GuideStep(
                    number = "3",
                    text   = "Measure the distance from the edge of the paper (wall side) to the mark — this is your foot length in mm."
                )
                GuideStep(
                    number = "4",
                    text   = "Measure the widest part of your foot (usually across the ball of the foot) — this is your foot width."
                )
                GuideStep(
                    number = "5",
                    text   = "Measure both feet and enter the larger measurement — feet are rarely identical in size."
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        "Tip: measure in the evening when feet are slightly larger after a day of activity.",
                        style    = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Got it") }
        }
    )
}

@Composable
private fun GuideStep(number: String, text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment     = CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(26.dp),
            shape    = CircleShape,
            color    = MaterialTheme.colorScheme.primary
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    number,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
        Text(text, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
    }
}



// ─────────────────────────────────────────────────────────────────────
// CLOTHING section — body measurements
// ─────────────────────────────────────────────────────────────────────

@Composable
private fun ClothingSizeSection(
    measurement : Measurement,
    sizeLabel   : SizeLabel?,
    measError   : String?,
    onMeasure   : (Measurement) -> Unit,
    onSize      : (SizeLabel?) -> Unit
) {
    // height stored in measurement.height (cm) — required
    // weight stored in measurement.width  (kg) — optional
    // waist  stored in measurement.depth  (cm) — optional
    val heightCm = measurement.height
    val weightKg = measurement.width
    val waistCm  = measurement.depth

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

        // ── Standard size chips (S/M/L as quick pick) ─────────────────────
        Text("Quick size", style = MaterialTheme.typography.bodyMedium)
        SizeLabelSelector(selected = sizeLabel, onChanged = onSize)

        Text(
            "Or enter your measurements for a more accurate fit:",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        HorizontalDivider()

        // ── Height (required) ─────────────────────────────────────────────
        Row(
            modifier              = Modifier.fillMaxWidth(),
            verticalAlignment     = CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Height *", style = MaterialTheme.typography.bodyMedium)
                Text(
                    "Required for 3D generation",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
        OutlinedTextField(
            value           = if (heightCm == 0.0) "" else heightCm.toString(),
            onValueChange   = { onMeasure(measurement.copy(height = it.toDoubleOrNull() ?: 0.0)) },
            modifier        = Modifier.fillMaxWidth(),
            label           = { Text("Height") },
            placeholder     = { Text("e.g. 175") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            isError         = measError != null || (heightCm == 0.0),
            supportingText  = {
                if (heightCm == 0.0)
                    Text("Height is required to generate the 3D model",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall)
                else if (heightCm < 100.0 || heightCm > 220.0)
                    Text("Please enter a realistic height (100–220 cm)",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall)
            },
            trailingIcon    = {
                Text("cm", style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(end = 8.dp))
            },
            singleLine = true,
            shape      = RoundedCornerShape(10.dp)
        )

        // ── Weight (optional) ─────────────────────────────────────────────
        OutlinedTextField(
            value           = if (weightKg == 0.0) "" else weightKg.toString(),
            onValueChange   = { onMeasure(measurement.copy(width = it.toDoubleOrNull() ?: 0.0)) },
            modifier        = Modifier.fillMaxWidth(),
            label           = { Text("Weight (optional)") },
            placeholder     = { Text("e.g. 70") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            trailingIcon    = {
                Text("kg", style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(end = 8.dp))
            },
            singleLine = true,
            shape      = RoundedCornerShape(10.dp)
        )

        // ── Waist (optional) ──────────────────────────────────────────────
        OutlinedTextField(
            value           = if (waistCm == 0.0) "" else waistCm.toString(),
            onValueChange   = { onMeasure(measurement.copy(depth = it.toDoubleOrNull() ?: 0.0)) },
            modifier        = Modifier.fillMaxWidth(),
            label           = { Text("Waist circumference (optional)") },
            placeholder     = { Text("e.g. 80") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            trailingIcon    = {
                Text("cm", style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(end = 8.dp))
            },
            singleLine = true,
            shape      = RoundedCornerShape(10.dp)
        )

        measError?.let {
            Text(it, color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────
// ACCESSORY section
// ─────────────────────────────────────────────────────────────────────

enum class AccessorySubgroup(val label: String) {
    RING("Ring"),
    EARRING("Earring"),
    BRACELET("Bracelet")
}

// Ring size S/M/L → inner diameter in mm (standard EU ring sizes)
private val RING_SIZE_INFO = mapOf(
    SizeLabel.S to "~15–16 mm diameter (EU 48–51)",
    SizeLabel.M to "~17–18 mm diameter (EU 53–57)",
    SizeLabel.L to "~19–20 mm diameter (EU 60–63)"
)

@Composable
private fun AccessorySizeSection(
    measurement : Measurement,
    sizeLabel   : SizeLabel?,
    error       : String?,
    onMeasure   : (Measurement) -> Unit,
    onSize      : (SizeLabel?) -> Unit
) {
    // store selected subgroup in measurement.depth repurposed as ordinal
    val subgroupOrdinal = measurement.depth.toInt()
        .coerceIn(0, AccessorySubgroup.values().lastIndex)
    val subgroup = AccessorySubgroup.values()[subgroupOrdinal]

    var showRingInfo by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

        // ── Subgroup selector ─────────────────────────────────────────────
        Text("Accessory type", style = MaterialTheme.typography.bodyMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AccessorySubgroup.values().forEach { sg ->
                FilterChip(
                    selected = subgroup == sg,
                    onClick  = {
                        onMeasure(measurement.copy(depth = sg.ordinal.toDouble()))
                    },
                    label = { Text(sg.label) }
                )
            }
        }

        HorizontalDivider()

        // ── Size selector — same S/M/L for all, but ring shows "i" button ─
        Row(
            modifier          = Modifier.fillMaxWidth(),
            verticalAlignment = CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Size", style = MaterialTheme.typography.bodyMedium)
            if (subgroup == AccessorySubgroup.RING) {
                IconButton(onClick = { showRingInfo = true }) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = "Ring size guide",
                        tint     = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SizeLabel.values().forEach { label ->
                FilterChip(
                    selected = sizeLabel == label,
                    onClick  = { onSize(if (sizeLabel == label) null else label) },
                    label    = { Text(label.name) }
                )
            }
        }

        // show diameter hint under the chips when a ring size is selected
        if (subgroup == AccessorySubgroup.RING && sizeLabel != null) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
            ) {
                Text(
                    RING_SIZE_INFO[sizeLabel] ?: "",
                    style    = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    color    = MaterialTheme.colorScheme.secondary
                )
            }
        }

        HorizontalDivider()

        // ── Thickness input — applicable to all accessory subgroups ───────
        Text("Dimensions", style = MaterialTheme.typography.bodyMedium)
        OutlinedTextField(
            value           = if (measurement.height == 0.0) "" else measurement.height.toString(),
            onValueChange   = { onMeasure(measurement.copy(height = it.toDoubleOrNull() ?: 0.0)) },
            modifier        = Modifier.fillMaxWidth(),
            label           = {
                Text(when (subgroup) {
                    AccessorySubgroup.RING     -> "Band thickness"
                    AccessorySubgroup.EARRING  -> "Length"
                    AccessorySubgroup.BRACELET -> "Width"
                })
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            isError         = error != null,
            trailingIcon    = {
                Text("mm", style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(end = 8.dp))
            },
            singleLine = true,
            shape      = RoundedCornerShape(10.dp)
        )
        OutlinedTextField(
            value           = if (measurement.width == 0.0) "" else measurement.width.toString(),
            onValueChange   = { onMeasure(measurement.copy(width = it.toDoubleOrNull() ?: 0.0)) },
            modifier        = Modifier.fillMaxWidth(),
            label           = {
                Text(when (subgroup) {
                    AccessorySubgroup.RING     -> "Band width"
                    AccessorySubgroup.EARRING  -> "Width"
                    AccessorySubgroup.BRACELET -> "Length"
                })
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            trailingIcon    = {
                Text("mm", style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(end = 8.dp))
            },
            singleLine = true,
            shape      = RoundedCornerShape(10.dp)
        )

        error?.let {
            Text(it, color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall)
        }
    }

    if (showRingInfo) {
        RingSizeInfoDialog(onDismiss = { showRingInfo = false })
    }
}

@Composable
private fun RingSizeInfoDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon  = { Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.primary) },
        title = { Text("Ring sizes guide") },
        text  = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "To find your ring size, measure the inner diameter of a ring that fits well, " +
                            "or wrap a thin strip of paper around your finger and measure the length in mm.",
                    style = MaterialTheme.typography.bodySmall
                )
                HorizontalDivider()
                // size table
                listOf(
                    Triple("S", "EU 48–51", "~15–16 mm"),
                    Triple("M", "EU 53–57", "~17–18 mm"),
                    Triple("L", "EU 60–63", "~19–20 mm")
                ).forEach { (size, eu, diam) ->
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(30.dp),
                            shape    = CircleShape,
                            color    = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(size, style = MaterialTheme.typography.labelLarge)
                            }
                        }
                        Text(eu,   style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f).padding(start = 12.dp))
                        Text(diam, style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary)
                    }
                }
                HorizontalDivider()
                Text(
                    "Tip: measure in the afternoon — fingers swell slightly during the day.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Got it") }
        }
    )
}

// ─────────────────────────────────────────────────────────────────────
// Generic MeasurementInputs — accepts custom labels
// ─────────────────────────────────────────────────────────────────────

@Composable
private fun MeasurementInputs(
    measurement : Measurement,
    labels      : List<String> = listOf("Height", "Width", "Depth"),
    unit        : String = "mm",
    error       : String?,
    onChanged   : (Measurement) -> Unit
) {
    val fields = listOf(
        Triple(labels.getOrElse(0) { "Height" }, measurement.height) { v: Double -> onChanged(measurement.copy(height = v)) },
        Triple(labels.getOrElse(1) { "Width"  }, measurement.width)  { v: Double -> onChanged(measurement.copy(width  = v)) },
        Triple(labels.getOrElse(2) { "Depth"  }, measurement.depth)  { v: Double -> onChanged(measurement.copy(depth  = v)) }
    )
    fields.forEach { (label, value, updater) ->
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = CenterVertically) {
            Text(label, modifier = Modifier.width(64.dp), style = MaterialTheme.typography.bodyMedium)
            OutlinedTextField(
                value           = if (value == 0.0) "" else value.toString(),
                onValueChange   = { updater(it.toDoubleOrNull() ?: 0.0) },
                modifier        = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError         = error != null,
                trailingIcon    = {
                    Text(unit, style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(end = 8.dp))
                },
                singleLine = true,
                shape      = RoundedCornerShape(10.dp)
            )
        }
    }
    error?.let {
        Text(it, color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun SizeLabelSelector(selected: SizeLabel?, onChanged: (SizeLabel?) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        SizeLabel.values().forEach { label ->
            FilterChip(
                selected = selected == label,
                onClick  = { onChanged(if (selected == label) null else label) },
                label    = { Text(label.name) }
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
    selected         : Material,
    allowedMaterials : List<Material>,
    onChanged        : (Material) -> Unit
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
            value         = selected.displayName,
            onValueChange = {},
            readOnly      = true,
            label         = { Text("Material") },
            modifier      = Modifier.fillMaxWidth().menuAnchor(),
            trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            shape         = RoundedCornerShape(10.dp)
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            allowedMaterials.forEach { material ->
                DropdownMenuItem(
                    text        = { Text(material.displayName) },
                    onClick     = { onChanged(material); expanded = false },
                    leadingIcon = if (material == selected) ({
                        Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary)
                    }) else null
                )
            }
        }
    }
}

// Default preset textures shown in the dropdown.
// These use stable IDs so the ViewModel can match them by id if needed.
private val DEFAULT_TEXTURES = listOf(
    Texture(id = "tex-glossy",      name = "Glossy",      previewUrl = null, description = "Smooth high-gloss finish"),
    Texture(id = "tex-rough",       name = "Rough",        previewUrl = null, description = "Matte rough surface"),
    Texture(id = "tex-transparent", name = "Transparent",  previewUrl = null, description = "Clear / translucent finish")
)

// Sentinel id used when the user has attached a custom image
private const val CUSTOM_TEXTURE_ID = "tex-custom-image"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TextureSelector(
    selected  : Texture,
    onChanged : (Texture) -> Unit
) {
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            // switch to custom-image texture — name becomes "Image texture"
            onChanged(
                Texture(
                    id          = CUSTOM_TEXTURE_ID,
                    name        = "Image texture",
                    previewUrl  = it.toString(),
                    description = "User-provided texture image"
                )
            )
        }
    }

    // Is the current selection one of the presets or a custom image?
    val isCustomImage = selected.id == CUSTOM_TEXTURE_ID || selected.previewUrl != null && selected.id !in DEFAULT_TEXTURES.map { it.id }

    var dropdownExpanded by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

        // ── dropdown row + paperclip ──────────────────────────────────────
        ExposedDropdownMenuBox(
            expanded        = dropdownExpanded,
            onExpandedChange = { dropdownExpanded = !dropdownExpanded }
        ) {
            OutlinedTextField(
                value         = selected.name,
                onValueChange = {},
                readOnly      = true,
                label         = { Text("Texture") },
                modifier      = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                trailingIcon  = {
                    // paperclip sits where the dropdown arrow normally would be
                    Row(verticalAlignment = CenterVertically) {
                        // paperclip
                        IconButton(
                            onClick = { launcher.launch("image/*") },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector        = Icons.Default.AttachFile,
                                contentDescription = "Attach texture image",
                                tint               = if (isCustomImage)
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
                expanded        = dropdownExpanded,
                onDismissRequest = { dropdownExpanded = false }
            ) {
                DEFAULT_TEXTURES.forEach { texture ->
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(texture.name, style = MaterialTheme.typography.bodyMedium)
                                texture.description?.let {
                                    Text(it, style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        },
                        onClick = {
                            onChanged(texture)
                            dropdownExpanded = false
                        },
                        leadingIcon = if (selected.id == texture.id) ({
                            Icon(Icons.Default.Check, null,
                                tint = MaterialTheme.colorScheme.primary)
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
                    model              = selected.previewUrl,
                    contentDescription = "Texture preview",
                    modifier           = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    contentScale       = ContentScale.Crop
                )
                // clear — revert to first default texture
                IconButton(
                    onClick  = { onChanged(DEFAULT_TEXTURES.first()) },
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Remove texture image",
                            modifier           = Modifier.padding(4.dp).size(14.dp),
                            tint               = MaterialTheme.colorScheme.error
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
        value         = value,
        onValueChange = { if (it.length <= 50) onChanged(it) },
        label         = { Text("Inscription") },
        modifier      = Modifier.fillMaxWidth(),
        placeholder   = { Text("Name, phrase, initials…") },
        isError       = error != null,
        supportingText = {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(error ?: "", color = MaterialTheme.colorScheme.error)
                Text("${value.length}/50", style = MaterialTheme.typography.labelSmall)
            }
        },
        singleLine = true,
        shape      = RoundedCornerShape(10.dp)
    )
}
//
//@Composable
//private fun AdditionalElementsSelector(
//    selected : List<AdditionalElement>,
//    onAdd    : (AdditionalElement) -> Unit,
//    onRemove : (AdditionalElement) -> Unit
//) {
//    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
//        Text("Additional elements", style = MaterialTheme.typography.bodyMedium)
//        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
//            AdditionalElement.values().forEach { element ->
//                val isSelected = element in selected
//                FilterChip(
//                    selected    = isSelected,
//                    onClick     = { if (isSelected) onRemove(element) else onAdd(element) },
//                    label       = { Text(element.displayName) },
//                    leadingIcon = if (isSelected) ({
//                        Icon(Icons.Default.Check, null, Modifier.size(16.dp))
//                    }) else null
//                )
//            }
//        }
//    }
//}

@Composable
private fun QuantitySelector(value: Int, error: String?, onChanged: (Int) -> Unit) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        verticalAlignment     = CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("Quantity", style = MaterialTheme.typography.bodyMedium)
        Row(verticalAlignment = CenterVertically) {
            IconButton(onClick = { if (value > 1) onChanged(value - 1) }, enabled = value > 1) {
                Icon(Icons.Default.Remove, null)
            }
            Text(value.toString(), style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 12.dp))
            IconButton(onClick = { if (value < 999) onChanged(value + 1) }, enabled = value < 999) {
                Icon(Icons.Default.Add, null)
            }
        }
    }
    error?.let {
        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun PriceCard(price: PriceEstimate) {
    Surface(
        shape    = RoundedCornerShape(12.dp),
        color    = MaterialTheme.colorScheme.primaryContainer
    ) {
        Column(
            modifier            = Modifier.padding(16.dp),
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
                    modifier              = Modifier.fillMaxWidth(),
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
                    modifier              = Modifier.fillMaxWidth(),
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
    label    : String,
    amount   : Double,
    isTotal  : Boolean = false,
    currency : String = "USD"
) {
    val symbol = when (currency) { "EUR" -> "€"; "GBP" -> "£"; else -> "$" }
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = if (isTotal) MaterialTheme.typography.titleSmall
            else         MaterialTheme.typography.bodySmall
        )
        Text(
            "$symbol${String.format("%.2f", amount)}",
            style = if (isTotal) MaterialTheme.typography.titleSmall
            else         MaterialTheme.typography.bodySmall,
            color = if (isTotal) MaterialTheme.colorScheme.primary
            else         MaterialTheme.colorScheme.onSurface
        )
    }
}


@Composable
private fun ProceedBottomBar(
    priceEstimate : PriceEstimate?,
    isLoading     : Boolean,
    enabled       : Boolean,
    onProceed     : () -> Unit
) {
    Surface(shadowElevation = 8.dp, color = MaterialTheme.colorScheme.surface) {
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment     = CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            priceEstimate?.let {
                Column {
                    Text("Total", style = MaterialTheme.typography.labelSmall)
                    Text("$${String.format("%.2f", it.total)}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary)
                }
            }
            Button(
                onClick  = onProceed,
                enabled  = enabled && !isLoading,
                modifier = Modifier.height(48.dp),
                shape    = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(Modifier.size(18.dp),
                        color = MaterialTheme.colorScheme.onPrimary)
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
            isModelReady    = true,
            onProceedToAr   = {},
            onBack          = {}
        )
    }
}

@Preview(showBackground = true, name = "Human Placeholder – Lower Limb (loading)")
@Composable
private fun PreviewHumanLower_Loading() {
    AppTheme {
        HumanPlaceholderScreen(
            productCategory = ProductCategory.FOOTWEAR,
            isModelReady    = false,
            onProceedToAr   = {},
            onBack          = {}
        )
    }
}

@Preview(showBackground = true, name = "Human Placeholder – Clothing")
@Composable
private fun PreviewHumanClothing() {
    AppTheme {
        HumanPlaceholderScreen(
            productCategory = ProductCategory.CLOTHING,
            isModelReady    = true,
            onProceedToAr   = {},
            onBack          = {}
        )
    }
}

@Preview(showBackground = true, name = "Config Screen – Jewellery")
@Composable
private fun PreviewConfigScreen() {
    val sampleProduct = Product(
        id                 = "prod-001",
        name               = "Forearm Cover",
        description        = "Personalised prosthetic forearm cover.",
        basePrice          = 149.99,
        category           = ProductCategory.JEWELRY,
        availableMaterials = listOf(Material.PLA, Material.CARBON_FIBER),
        availableTextures  = listOf(
            Texture("tex-001", "Matte",   "", "Smooth matte surface"),
            Texture("tex-002", "Glossy",  "", "High-gloss finish")
        ),
        imageUrl = ""
    )

    val sampleConfig = ProductConfig(
        id                 = "config-123",
        productId          = "prod-001",
        productCategory    = ProductCategory.JEWELRY,
        measurement        = Measurement(150.0, 75.0, 10.0),
        sizeLabel          = SizeLabel.M,
        color              = ProductColor("Dark green", "#0f3F15"),
        material           = Material.PLA,
        texture            = Texture("tex-001", "Matte", "", "Smooth matte surface"),
        inscription        = "Custom",
        userPhotoUri       = null,
        additionalElements = listOf(AdditionalElement.LOGO),
        quantity           = 1,
        createdAt          = 0L
    )

    AppTheme {
        ProductConfigScreenContent(
            state    = ProductConfigUiState(
                selectedProduct = sampleProduct,
                currentConfig   = sampleConfig,
                priceEstimate   = PriceEstimate(
                    configId = "config-123",
                    basePrice = 149.99,
                    materialSurcharges = mapOf("PLA" to 2.00),
                    textureSurcharge = 2.00,
                    sizeMultiplier = 1.15,
                    sizeMultiplierLabel = "L +15%",
                    quantity = 1,
                    subtotal = 155.67,
                    total = 155.67
                )
            ),
            onAction = {},
            onBack   = {}
        )
    }
}

//import androidx.compose.foundation.BorderStroke
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.ColumnScope
//import androidx.compose.foundation.layout.FlowRow
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.size
//import androidx.compose.foundation.layout.width
//import androidx.compose.foundation.rememberScrollState
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.foundation.text.KeyboardOptions
//import androidx.compose.foundation.verticalScroll
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Add
//import androidx.compose.material.icons.filled.ArrowForward
//import androidx.compose.material.icons.filled.Check
//import androidx.compose.material.icons.filled.Info
//import androidx.compose.material.icons.filled.Remove
//import androidx.compose.material.icons.filled.ViewInAr
//import androidx.compose.material3.AlertDialog
//import androidx.compose.material3.Button
//import androidx.compose.material3.CircularProgressIndicator
//import androidx.compose.material3.DropdownMenuItem
//import androidx.compose.material3.ExperimentalMaterial3Api
//import androidx.compose.material3.ExposedDropdownMenuBox
//import androidx.compose.material3.ExposedDropdownMenuDefaults
//import androidx.compose.material3.FilterChip
//import androidx.compose.material3.HorizontalDivider
//import androidx.compose.material3.Icon
//import androidx.compose.material3.IconButton
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.OutlinedButton
//import androidx.compose.material3.OutlinedTextField
//import androidx.compose.material3.Scaffold
//import androidx.compose.material3.Surface
//import androidx.compose.material3.Text
//import androidx.compose.material3.TextButton
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Alignment.Companion.CenterVertically
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.layout.ContentScale
//import androidx.compose.ui.res.stringResource
//import androidx.compose.ui.text.input.KeyboardType
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.dp
//import androidx.lifecycle.compose.collectAsStateWithLifecycle
//import coil3.compose.AsyncImage
//import com.example.coverdesign.R
//import com.example.coverdesign.domain.model.AdditionalElement
//import com.example.coverdesign.domain.model.Material
//import com.example.coverdesign.domain.model.Measurement
//import com.example.coverdesign.domain.model.Product
//import com.example.coverdesign.domain.model.ProductCategory
//import com.example.coverdesign.domain.model.ProductColor
//import com.example.coverdesign.domain.model.ProductConfig
//import com.example.coverdesign.domain.model.SizeLabel
//import com.example.coverdesign.domain.model.Texture
//import com.example.coverdesign.domain.model.pricing.PriceEstimate
//import com.example.coverdesign.ui.components.AppTopBar
//import com.example.coverdesign.ui.components.ErrorScreen
//import com.example.coverdesign.ui.components.EventHandler
//import com.example.coverdesign.ui.components.LoadingScreen
//import com.example.coverdesign.ui.constants.UIConstants.DEFAULT_TEXTURES
//import com.example.coverdesign.ui.screens.ArMode
//import com.example.coverdesign.ui.screens.resolveArMode
//import com.example.coverdesign.ui.theme.AppTheme
//import com.example.coverdesign.ui.viewmodel.ConfigEntryMode
//import com.example.coverdesign.ui.viewmodel.ProductConfigAction
//import com.example.coverdesign.ui.viewmodel.ProductConfigUiEvent
//import com.example.coverdesign.ui.viewmodel.ProductConfigUiState
//import com.example.coverdesign.ui.viewmodel.ProductConfigViewModel
//import org.koin.compose.viewmodel.koinViewModel
//
//
//@Composable
//fun ProductConfigScreen(
//    entryMode : ConfigEntryMode,
//    viewModel : ProductConfigViewModel = koinViewModel(),
//    onProceed : (configId: String) -> Unit,
//    onBack    : () -> Unit
//) {
//    val state by viewModel.uiState.collectAsStateWithLifecycle()
//
//    LaunchedEffect(entryMode) {
//        viewModel.onAction(ProductConfigAction.Enter(entryMode))
//    }
//
//    EventHandler(viewModel.events) { event ->
//        when (event) {
//            is ProductConfigUiEvent.NavigateToPreview -> onProceed(event.configId)
//        }
//    }
//
//    Scaffold(
//        topBar = {
//            AppTopBar(
//                title  = state.selectedProduct?.name ?: "Configure",
//                onBack = onBack
//            )
//        },
//        bottomBar = {
//            ProceedBottomBar(
//                priceEstimate = state.priceEstimate,
//                isLoading     = state.isSaving,
//                enabled       = state.currentConfig != null && state.measurementError == null,
//                onProceed     = { viewModel.onAction(ProductConfigAction.SaveAndProceed) }
//            )
//        }
//    ) { padding ->
//        when {
//            state.isLoading -> LoadingScreen()
//            state.error != null && state.currentConfig == null -> ErrorScreen(state.error!!)
//            state.currentConfig != null -> ProductConfigContent(
//                state    = state,
//                onAction = viewModel::onAction,
//                modifier = Modifier.padding(padding)
//            )
//        }
//    }
//
//    // FR3 — resume or discard prior draft
//    state.pendingResumePrompt?.let { previousConfig ->
//        ResumeConfigDialog(
//            previousConfig = previousConfig,
//            onResume       = { viewModel.onAction(ProductConfigAction.ResumePreviousConfig(previousConfig)) },
//            onStartFresh   = {
//                state.selectedProduct?.let {
//                    viewModel.onAction(ProductConfigAction.StartFreshConfig(it))
//                }
//            }
//        )
//    }
//}
//
//@Composable
//private fun ResumeConfigDialog(
//    previousConfig : ProductConfig,
//    onResume       : () -> Unit,
//    onStartFresh   : () -> Unit
//) {
//    AlertDialog(
//        onDismissRequest = onStartFresh,
//        title = { Text("Continue previous configuration?") },
//        text  = {
//            Text(
//                "You previously configured this product " +
//                        "(material: ${previousConfig.material.displayName}, " +
//                        "quantity: ${previousConfig.quantity}). " +
//                        "Continue editing or start fresh?"
//            )
//        },
//        confirmButton = { TextButton(onClick = onResume)     { Text("Continue") } },
//        dismissButton = { TextButton(onClick = onStartFresh) { Text("Start fresh") } }
//    )
//}
//
//// ═══════════════════════════════════════════════════════════════════
//// Internal scaffold used by preview
//// ═══════════════════════════════════════════════════════════════════
//
//@Composable
//private fun ProductConfigScreenContent(
//    state    : ProductConfigUiState,
//    onAction : (ProductConfigAction) -> Unit,
//    onBack   : () -> Unit
//) {
//    Scaffold(
//        topBar = {
//            AppTopBar(
//                title  = stringResource(R.string.configure_product),
//                onBack = onBack
//            )
//        },
//        bottomBar = {
//            ProceedBottomBar(
//                priceEstimate = state.priceEstimate,
//                isLoading     = state.isSaving,
//                enabled       = state.currentConfig != null && state.measurementError == null,
//                onProceed     = { onAction(ProductConfigAction.SaveAndProceed) }
//            )
//        }
//    ) { padding ->
//        if (state.isLoading) LoadingScreen()
//        else ProductConfigContent(
//            state    = state,
//            onAction = onAction,
//            modifier = Modifier.padding(padding)
//        )
//    }
//}
//
//// ═══════════════════════════════════════════════════════════════════
//// Main content scroll column
//// ═══════════════════════════════════════════════════════════════════
//
//@Composable
//private fun ProductConfigContent(
//    state    : ProductConfigUiState,
//    onAction : (ProductConfigAction) -> Unit,
//    modifier : Modifier = Modifier
//) {
//    val config = state.currentConfig ?: return
//    val productTextures = state.selectedProduct?.availableTextures ?: DEFAULT_TEXTURES
//    // resolve AR mode up-front so PhotoUploadSection can adapt
//    val arMode = config.productCategory.resolveArMode(
//        photoUri     = config.userPhotoUri,
//        measurements = config.measurement
//    )
//
//    Column(
//        modifier = modifier
//            .fillMaxSize()
//            .verticalScroll(rememberScrollState())
//            .padding(horizontal = 20.dp, vertical = 8.dp),
//        verticalArrangement = Arrangement.spacedBy(20.dp)
//    ) {
//        // ── AR mode badge ─────────────────────────────────────────────────
//        // ── Product header — photo + name + category + base price ──────────
//        state.selectedProduct?.let { product ->
//            ProductHeader(product = product)
//        }
//
//        ArModeBadge(arMode = arMode)
//
//        // ── Measurements — content adapts per product category ───────────
//        ConfigSection(title = "Measurements & Size") {
//            MeasurementSection(
//                category    = config.productCategory,
//                measurement = config.measurement,
//                sizeLabel   = config.sizeLabel,
//                measError   = state.measurementError,
//                onMeasure   = { onAction(ProductConfigAction.UpdateMeasurement(it)) },
//                onSize      = { onAction(ProductConfigAction.UpdateSizeLabel(it)) }
//            )
//        }
//
//        // ── Appearance ────────────────────────────────────────────────────
//        ConfigSection(title = "Appearance") {
//            MaterialSelector(
//                selected  = config.material,
//                onChanged = { onAction(ProductConfigAction.UpdateMaterial(it)) }
//            )
//            ColorPicker(
//                selected  = config.color,
//                onChanged = { onAction(ProductConfigAction.UpdateColor(it)) }
//            )
//            TextureSelector(
//                selected = config.texture,
//                //textureImageUri   = config.texture.previewUrl,   // String?
//                onChanged = { onAction(ProductConfigAction.UpdateTexture(it)) },
//               // onImageAttached = { onAction(ProductConfigAction.UpdateTextureImage(it)) },
//                textures = productTextures
//            )
//        }
//
//        // ── Personalisation ───────────────────────────────────────────────
//        ConfigSection(title = "Personalisation") {
//            InscriptionField(
//                value     = config.inscription,
//                error     = state.inscriptionError,
//                onChanged = { onAction(ProductConfigAction.UpdateInscription(it)) }
//            )
//            AdditionalElementsSelector(
//                selected = config.additionalElements,
//                onAdd    = { onAction(ProductConfigAction.AddElement(it)) },
//                onRemove = { onAction(ProductConfigAction.RemoveElement(it)) }
//            )
//        }
//
//        // ── Photo / AR section — adapts to product category ───────────────
//        ConfigSection(title = "Fitting & Try-On") {
//            PhotoUploadSection(
//                currentUri      = config.userPhotoUri,
//                arMode          = arMode,
//                productCategory = config.productCategory,
//                onChanged       = { onAction(ProductConfigAction.UpdateUserPhoto(it)) }
//            )
//        }
//
//        // ── Order options ─────────────────────────────────────────────────
//        ConfigSection(title = "Order") {
//            QuantitySelector(
//                value     = config.quantity,
//                error     = state.quantityError,
//                onChanged = { onAction(ProductConfigAction.UpdateQuantity(it)) }
//            )
//        }
//
//        // ── Price ─────────────────────────────────────────────────────────
//        state.priceEstimate?.let { PriceCard(price = it) }
//        OutlinedButton(
//            onClick  = { onAction(ProductConfigAction.RequestPriceUpdate) },
//            modifier = Modifier.fillMaxWidth(),
//            enabled  = !state.isPriceLoading
//        ) {
//            if (state.isPriceLoading) CircularProgressIndicator(Modifier.size(16.dp))
//            else Text("Calculate Price")
//        }
//
//        Spacer(Modifier.height(80.dp))
//    }
//}
//
//// ═══════════════════════════════════════════════════════════════════
//// Product header card — shown at top of the config form
//// ═══════════════════════════════════════════════════════════════════
//
//@Composable
//private fun ProductHeader(product: Product) {
//    Surface(
//        shape  = RoundedCornerShape(16.dp),
//        color  = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
//        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
//        modifier = Modifier.fillMaxWidth()
//    ) {
//        Row(
//            modifier              = Modifier.padding(14.dp),
//            horizontalArrangement = Arrangement.spacedBy(14.dp),
//            verticalAlignment     = CenterVertically
//        ) {
//            // product image
//            Surface(
//                modifier = Modifier.size(80.dp),
//                shape    = RoundedCornerShape(12.dp),
//                color    = MaterialTheme.colorScheme.surface
//            ) {
//                if (product.imageUrl.isNotEmpty()) {
//                    AsyncImage(
//                        model              = product.imageUrl,
//                        contentDescription = product.name,
//                        modifier           = Modifier.fillMaxSize(),
//                        contentScale       = ContentScale.Crop
//                    )
//                } else {
//                    // placeholder when no image URL
//                    Box(
//                        modifier         = Modifier.fillMaxSize(),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        Icon(
//                            imageVector        = Icons.Default.ViewInAr,
//                            contentDescription = null,
//                            tint               = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
//                            modifier           = Modifier.size(36.dp)
//                        )
//                    }
//                }
//            }
//
//            // text info
//            Column(
//                modifier            = Modifier.weight(1f),
//                verticalArrangement = Arrangement.spacedBy(4.dp)
//            ) {
//                Text(
//                    text  = product.name,
//                    style = MaterialTheme.typography.titleMedium
//                )
//                Text(
//                    text  = product.category.name,
//                    style = MaterialTheme.typography.labelMedium,
//                    color = MaterialTheme.colorScheme.primary
//                )
//                if (product.description.isNotEmpty()) {
//                    Text(
//                        text     = product.description,
//                        style    = MaterialTheme.typography.bodySmall,
//                        color    = MaterialTheme.colorScheme.onSurfaceVariant,
//                        maxLines = 2
//                    )
//                }
//                Text(
//                    text  = $$"Base price: $$${String.format("%.2f", product.basePrice)}",
//                    style = MaterialTheme.typography.labelSmall,
//                    color = MaterialTheme.colorScheme.onSurfaceVariant
//                )
//            }
//        }
//    }
//}
//
//// ═══════════════════════════════════════════════════════════════════
//// AR mode badge — shows at top of config form
//// ═══════════════════════════════════════════════════════════════════
//
//@Composable
//private fun ArModeBadge(arMode: ArMode) {
//    Surface(
//        shape = RoundedCornerShape(20.dp),
//        color = MaterialTheme.colorScheme.tertiaryContainer
//    ) {
//        Row(
//            modifier              = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
//            verticalAlignment     = CenterVertically,
//            horizontalArrangement = Arrangement.spacedBy(8.dp)
//        ) {
//            Icon(
//                imageVector        = arMode.icon,
//                contentDescription = null,
//                tint               = MaterialTheme.colorScheme.tertiary,
//                modifier           = Modifier.size(16.dp)
//            )
//            Text(
//                text  = "Try-on mode: ${arMode.label}",
//                style = MaterialTheme.typography.labelMedium,
//                color = MaterialTheme.colorScheme.tertiary
//            )
//        }
//    }
//}
//
//
//@Composable
//private fun ConfigSection(title: String, content: @Composable ColumnScope.() -> Unit) {
//    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
//        Text(title, style = MaterialTheme.typography.titleMedium)
//        Surface(
//            shape  = RoundedCornerShape(16.dp),
//            color  = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
//            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
//        ) {
//            Column(
//                modifier            = Modifier.padding(16.dp),
//                verticalArrangement = Arrangement.spacedBy(12.dp),
//                content             = content
//            )
//        }
//    }
//}
//
//// ═══════════════════════════════════════════════════════════════════
//// MeasurementSection — dispatcher: content depends on product category
//// ═══════════════════════════════════════════════════════════════════
//
//@Composable
//private fun MeasurementSection(
//    category    : ProductCategory,
//    measurement : Measurement,
//    sizeLabel   : SizeLabel?,
//    measError   : String?,
//    onMeasure   : (Measurement) -> Unit,
//    onSize      : (SizeLabel?) -> Unit
//) {
//    when (category) {
//
//        // ── FOOTWEAR ──────────────────────────────────────────────────────
//        // EU shoe size grid + foot-length input with measuring guide
//        ProductCategory.FOOTWEAR -> {
//            FootwearSizeSection(
//                measurement = measurement,
//                sizeLabel   = sizeLabel,
//                error       = measError,
//                onMeasure   = onMeasure,
//                onSize      = onSize
//            )
//        }
//
//        // ── ACCESSORY ─────────────────────────────────────────────────────
//        // Subgroup selector (Ring / Earring / Bracelet) then size inputs
//        ProductCategory.ACCESSORY -> {
//            AccessorySizeSection(
//                measurement = measurement,
//                sizeLabel   = sizeLabel,
//                error       = measError,
//                onMeasure   = onMeasure,
//                onSize      = onSize
//            )
//        }
//
//        // ── THREE_D_PRINT ─────────────────────────────────────────────────
//        // Only raw mm measurements; no size grid
//        ProductCategory.THREE_D_PRINT -> {
//            MeasurementInputs(
//                measurement = measurement,
//                labels      = listOf("Height", "Width", "Depth"),
//                unit        = "mm",
//                error       = measError,
//                onChanged   = onMeasure
//            )
//        }
//
//        // ── INTERIOR_DECOR ────────────────────────────────────────────────
//        // Only mm measurements; no size grid
//        ProductCategory.INTERIOR_DECOR -> {
//            MeasurementInputs(
//                measurement = measurement,
//                labels      = listOf("Height", "Width", "Depth"),
//                unit        = "mm",
//                error       = measError,
//                onChanged   = onMeasure
//            )
//        }
//
//        // ── CLOTHING / OTHER / JEWELRY ────────────────────────────────────
//        // Standard S/M/L selector + mm measurements
//        else -> {
//            SizeLabelSelector(
//                selected  = sizeLabel,
//                onChanged = onSize
//            )
//            MeasurementInputs(
//                measurement = measurement,
//                labels      = listOf("Height", "Width", "Depth"),
//                unit        = "mm",
//                error       = measError,
//                onChanged   = onMeasure
//            )
//        }
//    }
//}
//
//// ─────────────────────────────────────────────────────────────────────
//// FOOTWEAR section
//// ─────────────────────────────────────────────────────────────────────
//
//private val EU_SIZES = listOf(35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46)
//
//// EU size → approximate foot length in mm
//private val EU_TO_MM = mapOf(
//    35 to 222, 36 to 228, 37 to 235, 38 to 242, 39 to 248,
//    40 to 255, 41 to 262, 42 to 268, 43 to 275, 44 to 282,
//    45 to 288, 46 to 295
//)
//
//// SizeLabel is reused here to store EU size as a string label
//// We use SizeLabel.CUSTOM(value) if available, otherwise map EU→closest SizeLabel
//// For simplicity we store the raw EU number in measurement.depth (a dedicated field)
//// and keep sizeLabel for the S/M/L fallback on other categories.
//
//@Composable
//private fun FootwearSizeSection(
//    measurement : Measurement,
//    sizeLabel   : SizeLabel?,
//    error       : String?,
//    onMeasure   : (Measurement) -> Unit,
//    onSize      : (SizeLabel?) -> Unit
//) {
//    var showGuide by remember { mutableStateOf(false) }
//    // we store selected EU size in measurement.depth repurposed as euSize
//    val selectedEu = measurement.depth.toInt().let { if (it < 35 || it > 46) 0 else it }
//
//    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
//
//        // EU size grid
//        Row(
//            modifier          = Modifier.fillMaxWidth(),
//            verticalAlignment = CenterVertically,
//            horizontalArrangement = Arrangement.SpaceBetween
//        ) {
//            Text("EU Shoe Size", style = MaterialTheme.typography.bodyMedium)
//            IconButton(onClick = { showGuide = true }) {
//                Icon(
//                    imageVector        = Icons.Default.Info,
//                    contentDescription = "How to measure",
//                    tint               = MaterialTheme.colorScheme.primary,
//                    modifier           = Modifier.size(20.dp)
//                )
//            }
//        }
//
//        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
//            EU_SIZES.forEach { eu ->
//                FilterChip(
//                    selected = selectedEu == eu,
//                    onClick  = {
//                        onMeasure(measurement.copy(depth = eu.toDouble()))
//                    },
//                    label = { Text(eu.toString()) }
//                )
//            }
//        }
//
//        if (selectedEu > 0) {
//            Surface(
//                shape = RoundedCornerShape(8.dp),
//                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
//            ) {
//                Text(
//                    "EU $selectedEu ≈ ${EU_TO_MM[selectedEu]} mm foot length",
//                    style    = MaterialTheme.typography.bodySmall,
//                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
//                    color    = MaterialTheme.colorScheme.secondary
//                )
//            }
//        }
//
//        HorizontalDivider()
//
//        // Foot length manual input
//        Row(
//            modifier          = Modifier.fillMaxWidth(),
//            verticalAlignment = CenterVertically,
//            horizontalArrangement = Arrangement.SpaceBetween
//        ) {
//            Text("Foot Length", style = MaterialTheme.typography.bodyMedium)
//            IconButton(onClick = { showGuide = true }) {
//                Icon(
//                    imageVector        = Icons.Default.Info,
//                    contentDescription = "Measuring guide",
//                    tint               = MaterialTheme.colorScheme.primary,
//                    modifier           = Modifier.size(20.dp)
//                )
//            }
//        }
//        OutlinedTextField(
//            value           = if (measurement.height == 0.0) "" else measurement.height.toString(),
//            onValueChange   = { onMeasure(measurement.copy(height = it.toDoubleOrNull() ?: 0.0)) },
//            modifier        = Modifier.fillMaxWidth(),
//            label           = { Text("Foot length") },
//            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
//            isError         = error != null,
//            trailingIcon    = {
//                Text("mm", style = MaterialTheme.typography.labelSmall,
//                    modifier = Modifier.padding(end = 8.dp))
//            },
//            singleLine = true,
//            shape      = RoundedCornerShape(10.dp)
//        )
//        // Foot width
//        OutlinedTextField(
//            value           = if (measurement.width == 0.0) "" else measurement.width.toString(),
//            onValueChange   = { onMeasure(measurement.copy(width = it.toDoubleOrNull() ?: 0.0)) },
//            modifier        = Modifier.fillMaxWidth(),
//            label           = { Text("Foot width (widest point)") },
//            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
//            trailingIcon    = {
//                Text("mm", style = MaterialTheme.typography.labelSmall,
//                    modifier = Modifier.padding(end = 8.dp))
//            },
//            singleLine = true,
//            shape      = RoundedCornerShape(10.dp)
//        )
//
//        error?.let {
//            Text(it, color = MaterialTheme.colorScheme.error,
//                style = MaterialTheme.typography.labelSmall)
//        }
//    }
//
//    if (showGuide) {
//        FootMeasuringGuideDialog(onDismiss = { showGuide = false })
//    }
//}
//
//@Composable
//private fun FootMeasuringGuideDialog(onDismiss: () -> Unit) {
//    AlertDialog(
//        onDismissRequest = onDismiss,
//        icon  = { Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.primary) },
//        title = { Text("How to measure your foot") },
//        text  = {
//            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
//                GuideStep(
//                    number = "1",
//                    text   = "Place a blank sheet of paper on a hard floor. Stand on it with your heel touching a wall."
//                )
//                GuideStep(
//                    number = "2",
//                    text   = "Mark the tip of your longest toe with a pencil, keeping the pen vertical."
//                )
//                GuideStep(
//                    number = "3",
//                    text   = "Measure the distance from the edge of the paper (wall side) to the mark — this is your foot length in mm."
//                )
//                GuideStep(
//                    number = "4",
//                    text   = "Measure the widest part of your foot (usually across the ball of the foot) — this is your foot width."
//                )
//                GuideStep(
//                    number = "5",
//                    text   = "Measure both feet and enter the larger measurement — feet are rarely identical in size."
//                )
//                Surface(
//                    shape = RoundedCornerShape(8.dp),
//                    color = MaterialTheme.colorScheme.primaryContainer
//                ) {
//                    Text(
//                        "Tip: measure in the evening when feet are slightly larger after a day of activity.",
//                        style    = MaterialTheme.typography.bodySmall,
//                        modifier = Modifier.padding(10.dp)
//                    )
//                }
//            }
//        },
//        confirmButton = {
//            TextButton(onClick = onDismiss) { Text("Got it") }
//        }
//    )
//}
//
//@Composable
//private fun GuideStep(number: String, text: String) {
//    Row(
//        horizontalArrangement = Arrangement.spacedBy(10.dp),
//        verticalAlignment     = CenterVertically
//    ) {
//        Surface(
//            modifier = Modifier.size(26.dp),
//            shape    = CircleShape,
//            color    = MaterialTheme.colorScheme.primary
//        ) {
//            Box(contentAlignment = Alignment.Center) {
//                Text(
//                    number,
//                    style = MaterialTheme.typography.labelMedium,
//                    color = MaterialTheme.colorScheme.onPrimary
//                )
//            }
//        }
//        Text(text, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
//    }
//}
//
//// ─────────────────────────────────────────────────────────────────────
//// ACCESSORY section
//// ─────────────────────────────────────────────────────────────────────
//
//enum class AccessorySubgroup(val label: String) {
//    RING("Ring"),
//    EARRING("Earring"),
//    BRACELET("Bracelet")
//}
//
//// Ring size S/M/L → inner diameter in mm (standard EU ring sizes)
//private val RING_SIZE_INFO = mapOf(
//    SizeLabel.S to "~15–16 mm diameter (EU 48–51)",
//    SizeLabel.M to "~17–18 mm diameter (EU 53–57)",
//    SizeLabel.L to "~19–20 mm diameter (EU 60–63)"
//)
//
//@Composable
//private fun AccessorySizeSection(
//    measurement : Measurement,
//    sizeLabel   : SizeLabel?,
//    error       : String?,
//    onMeasure   : (Measurement) -> Unit,
//    onSize      : (SizeLabel?) -> Unit
//) {
//    // store selected subgroup in measurement.depth repurposed as ordinal
//    val subgroupOrdinal = measurement.depth.toInt()
//        .coerceIn(0, AccessorySubgroup.values().lastIndex)
//    val subgroup = AccessorySubgroup.values()[subgroupOrdinal]
//
//    var showRingInfo by remember { mutableStateOf(false) }
//
//    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
//
//        // ── Subgroup selector ─────────────────────────────────────────────
//        Text("Accessory type", style = MaterialTheme.typography.bodyMedium)
//        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
//            AccessorySubgroup.values().forEach { sg ->
//                FilterChip(
//                    selected = subgroup == sg,
//                    onClick  = {
//                        onMeasure(measurement.copy(depth = sg.ordinal.toDouble()))
//                    },
//                    label = { Text(sg.label) }
//                )
//            }
//        }
//
//        HorizontalDivider()
//
//        // ── Size selector — same S/M/L for all, but ring shows "i" button ─
//        Row(
//            modifier          = Modifier.fillMaxWidth(),
//            verticalAlignment = CenterVertically,
//            horizontalArrangement = Arrangement.SpaceBetween
//        ) {
//            Text("Size", style = MaterialTheme.typography.bodyMedium)
//            if (subgroup == AccessorySubgroup.RING) {
//                IconButton(onClick = { showRingInfo = true }) {
//                    Icon(
//                        Icons.Default.Info,
//                        contentDescription = "Ring size guide",
//                        tint     = MaterialTheme.colorScheme.primary,
//                        modifier = Modifier.size(20.dp)
//                    )
//                }
//            }
//        }
//        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
//            SizeLabel.values().forEach { label ->
//                FilterChip(
//                    selected = sizeLabel == label,
//                    onClick  = { onSize(if (sizeLabel == label) null else label) },
//                    label    = { Text(label.name) }
//                )
//            }
//        }
//
//        // show diameter hint under the chips when a ring size is selected
//        if (subgroup == AccessorySubgroup.RING && sizeLabel != null) {
//            Surface(
//                shape = RoundedCornerShape(8.dp),
//                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
//            ) {
//                Text(
//                    RING_SIZE_INFO[sizeLabel] ?: "",
//                    style    = MaterialTheme.typography.bodySmall,
//                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
//                    color    = MaterialTheme.colorScheme.secondary
//                )
//            }
//        }
//
//        HorizontalDivider()
//
//        // ── Thickness input — applicable to all accessory subgroups ───────
//        Text("Dimensions", style = MaterialTheme.typography.bodyMedium)
//        OutlinedTextField(
//            value           = if (measurement.height == 0.0) "" else measurement.height.toString(),
//            onValueChange   = { onMeasure(measurement.copy(height = it.toDoubleOrNull() ?: 0.0)) },
//            modifier        = Modifier.fillMaxWidth(),
//            label           = {
//                Text(when (subgroup) {
//                    AccessorySubgroup.RING     -> "Band thickness"
//                    AccessorySubgroup.EARRING  -> "Length"
//                    AccessorySubgroup.BRACELET -> "Width"
//                })
//            },
//            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
//            isError         = error != null,
//            trailingIcon    = {
//                Text("mm", style = MaterialTheme.typography.labelSmall,
//                    modifier = Modifier.padding(end = 8.dp))
//            },
//            singleLine = true,
//            shape      = RoundedCornerShape(10.dp)
//        )
//        OutlinedTextField(
//            value           = if (measurement.width == 0.0) "" else measurement.width.toString(),
//            onValueChange   = { onMeasure(measurement.copy(width = it.toDoubleOrNull() ?: 0.0)) },
//            modifier        = Modifier.fillMaxWidth(),
//            label           = {
//                Text(when (subgroup) {
//                    AccessorySubgroup.RING     -> "Band width"
//                    AccessorySubgroup.EARRING  -> "Width"
//                    AccessorySubgroup.BRACELET -> "Length"
//                })
//            },
//            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
//            trailingIcon    = {
//                Text("mm", style = MaterialTheme.typography.labelSmall,
//                    modifier = Modifier.padding(end = 8.dp))
//            },
//            singleLine = true,
//            shape      = RoundedCornerShape(10.dp)
//        )
//
//        error?.let {
//            Text(it, color = MaterialTheme.colorScheme.error,
//                style = MaterialTheme.typography.labelSmall)
//        }
//    }
//
//    if (showRingInfo) {
//        RingSizeInfoDialog(onDismiss = { showRingInfo = false })
//    }
//}
//
//@Composable
//private fun RingSizeInfoDialog(onDismiss: () -> Unit) {
//    AlertDialog(
//        onDismissRequest = onDismiss,
//        icon  = { Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.primary) },
//        title = { Text("Ring sizes guide") },
//        text  = {
//            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
//                Text(
//                    "To find your ring size, measure the inner diameter of a ring that fits well, " +
//                            "or wrap a thin strip of paper around your finger and measure the length in mm.",
//                    style = MaterialTheme.typography.bodySmall
//                )
//                HorizontalDivider()
//                // size table
//                listOf(
//                    Triple("S", "EU 48–51", "~15–16 mm"),
//                    Triple("M", "EU 53–57", "~17–18 mm"),
//                    Triple("L", "EU 60–63", "~19–20 mm")
//                ).forEach { (size, eu, diam) ->
//                    Row(
//                        modifier              = Modifier.fillMaxWidth(),
//                        horizontalArrangement = Arrangement.SpaceBetween,
//                        verticalAlignment     = CenterVertically
//                    ) {
//                        Surface(
//                            modifier = Modifier.size(30.dp),
//                            shape    = CircleShape,
//                            color    = MaterialTheme.colorScheme.primaryContainer
//                        ) {
//                            Box(contentAlignment = Alignment.Center) {
//                                Text(size, style = MaterialTheme.typography.labelLarge)
//                            }
//                        }
//                        Text(eu,   style = MaterialTheme.typography.bodySmall,
//                            modifier = Modifier.weight(1f).padding(start = 12.dp))
//                        Text(diam, style = MaterialTheme.typography.bodySmall,
//                            color = MaterialTheme.colorScheme.primary)
//                    }
//                }
//                HorizontalDivider()
//                Text(
//                    "Tip: measure in the afternoon — fingers swell slightly during the day.",
//                    style = MaterialTheme.typography.bodySmall,
//                    color = MaterialTheme.colorScheme.onSurfaceVariant
//                )
//            }
//        },
//        confirmButton = {
//            TextButton(onClick = onDismiss) { Text("Got it") }
//        }
//    )
//}
//
//// ─────────────────────────────────────────────────────────────────────
//// Generic MeasurementInputs — accepts custom labels
//// ─────────────────────────────────────────────────────────────────────
//
//@Composable
//private fun MeasurementInputs(
//    measurement : Measurement,
//    labels      : List<String> = listOf("Height", "Width", "Depth"),
//    unit        : String = "mm",
//    error       : String?,
//    onChanged   : (Measurement) -> Unit
//) {
//    val fields = listOf(
//        Triple(labels.getOrElse(0) { "Height" }, measurement.height) { v: Double -> onChanged(measurement.copy(height = v)) },
//        Triple(labels.getOrElse(1) { "Width"  }, measurement.width)  { v: Double -> onChanged(measurement.copy(width  = v)) },
//        Triple(labels.getOrElse(2) { "Depth"  }, measurement.depth)  { v: Double -> onChanged(measurement.copy(depth  = v)) }
//    )
//    fields.forEach { (label, value, updater) ->
//        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = CenterVertically) {
//            Text(label, modifier = Modifier.width(64.dp), style = MaterialTheme.typography.bodyMedium)
//            OutlinedTextField(
//                value           = if (value == 0.0) "" else value.toString(),
//                onValueChange   = { updater(it.toDoubleOrNull() ?: 0.0) },
//                modifier        = Modifier.weight(1f),
//                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
//                isError         = error != null,
//                trailingIcon    = {
//                    Text(unit, style = MaterialTheme.typography.labelSmall,
//                        modifier = Modifier.padding(end = 8.dp))
//                },
//                singleLine = true,
//                shape      = RoundedCornerShape(10.dp)
//            )
//        }
//    }
//    error?.let {
//        Text(it, color = MaterialTheme.colorScheme.error,
//            style = MaterialTheme.typography.labelSmall)
//    }
//}
//
//@Composable
//private fun SizeLabelSelector(selected: SizeLabel?, onChanged: (SizeLabel?) -> Unit) {
//    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
//        SizeLabel.values().forEach { label ->
//            FilterChip(
//                selected = selected == label,
//                onClick  = { onChanged(if (selected == label) null else label) },
//                label    = { Text(label.name) }
//            )
//        }
//    }
//}
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//private fun MaterialSelector(selected: Material, onChanged: (Material) -> Unit) {
//    var expanded by remember { mutableStateOf(false) }
//    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
//        OutlinedTextField(
//            value         = selected.displayName,
//            onValueChange = {},
//            readOnly      = true,
//            label         = { Text("Material") },
//            modifier      = Modifier.fillMaxWidth().menuAnchor(),
//            trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
//            shape         = RoundedCornerShape(10.dp)
//        )
//        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
//            Material.values().forEach { material ->
//                DropdownMenuItem(
//                    text        = { Text(material.displayName) },
//                    onClick     = { onChanged(material); expanded = false },
//                    leadingIcon = if (material == selected) ({
//                        Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary)
//                    }) else null
//                )
//            }
//        }
//    }
//}
//
//@Composable
//private fun InscriptionField(value: String, error: String?, onChanged: (String) -> Unit) {
//    OutlinedTextField(
//        value         = value,
//        onValueChange = { if (it.length <= 50) onChanged(it) },
//        label         = { Text("Inscription") },
//        modifier      = Modifier.fillMaxWidth(),
//        placeholder   = { Text("Name, phrase, initials…") },
//        isError       = error != null,
//        supportingText = {
//            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
//                Text(error ?: "", color = MaterialTheme.colorScheme.error)
//                Text("${value.length}/50", style = MaterialTheme.typography.labelSmall)
//            }
//        },
//        singleLine = true,
//        shape      = RoundedCornerShape(10.dp)
//    )
//}
//
//@Composable
//private fun AdditionalElementsSelector(
//    selected : List<AdditionalElement>,
//    onAdd    : (AdditionalElement) -> Unit,
//    onRemove : (AdditionalElement) -> Unit
//) {
//    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
//        Text("Additional elements", style = MaterialTheme.typography.bodyMedium)
//        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
//            AdditionalElement.values().forEach { element ->
//                val isSelected = element in selected
//                FilterChip(
//                    selected    = isSelected,
//                    onClick     = { if (isSelected) onRemove(element) else onAdd(element) },
//                    label       = { Text(element.displayName) },
//                    leadingIcon = if (isSelected) ({
//                        Icon(Icons.Default.Check, null, Modifier.size(16.dp))
//                    }) else null
//                )
//            }
//        }
//    }
//}
//
//@Composable
//private fun QuantitySelector(value: Int, error: String?, onChanged: (Int) -> Unit) {
//    Row(
//        modifier              = Modifier.fillMaxWidth(),
//        verticalAlignment     = CenterVertically,
//        horizontalArrangement = Arrangement.SpaceBetween
//    ) {
//        Text("Quantity", style = MaterialTheme.typography.bodyMedium)
//        Row(verticalAlignment = CenterVertically) {
//            IconButton(onClick = { if (value > 1) onChanged(value - 1) }, enabled = value > 1) {
//                Icon(Icons.Default.Remove, null)
//            }
//            Text(value.toString(), style = MaterialTheme.typography.titleMedium,
//                modifier = Modifier.padding(horizontal = 12.dp))
//            IconButton(onClick = { if (value < 999) onChanged(value + 1) }, enabled = value < 999) {
//                Icon(Icons.Default.Add, null)
//            }
//        }
//    }
//    error?.let {
//        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
//    }
//}
//
//@Composable
//fun PriceCard(price: PriceEstimate) {
//    Surface(
//        shape    = RoundedCornerShape(12.dp),
//        color    = MaterialTheme.colorScheme.primaryContainer
//    ) {
//        Column(
//            modifier            = Modifier.padding(16.dp),
//            verticalArrangement = Arrangement.spacedBy(4.dp)
//        ) {
//            Text("Price breakdown", style = MaterialTheme.typography.titleSmall)
//
//            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
//
//            // base
//            PriceRow("Base price", price.basePrice)
//
//            // material surcharges — one per material
//            price.materialSurcharges.forEach { (name, amount) ->
//                if (amount > 0.0)
//                    PriceRow("  Material ($name)", amount)
//            }
//
//            // texture
//            if (price.textureSurcharge > 0.0)
//                PriceRow("  Texture", price.textureSurcharge)
//
//            // inscription
//            if (price.inscriptionSurcharge > 0.0)
//                PriceRow("  Inscription", price.inscriptionSurcharge)
//
//            // additional elements
//            price.elementSurcharges.forEach { (name, amount) ->
//                if (amount > 0.0)
//                    PriceRow("  ${name.lowercase().replaceFirstChar { it.uppercase() }}", amount)
//            }
//
//            HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))
//
//            // size multiplier row — only shown when it differs from 1.0
//            if (price.sizeMultiplier != 1.0 || price.sizeMultiplierLabel.isNotEmpty()) {
//                Row(
//                    modifier              = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.SpaceBetween
//                ) {
//                    Text(
//                        "Size (${price.sizeMultiplierLabel})",
//                        style = MaterialTheme.typography.bodySmall
//                    )
//                    Text(
//                        "×${String.format("%.2f", price.sizeMultiplier)}",
//                        style = MaterialTheme.typography.bodySmall,
//                        color = MaterialTheme.colorScheme.primary
//                    )
//                }
//            }
//
//            // subtotal (per unit after size)
//            if (price.quantity > 1) {
//                PriceRow(
//                    "Subtotal (1 unit)",
//                    price.subtotal
//                )
//                Row(
//                    modifier              = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.SpaceBetween
//                ) {
//                    Text(
//                        "Quantity",
//                        style = MaterialTheme.typography.bodySmall
//                    )
//                    Text(
//                        "×${price.quantity}",
//                        style = MaterialTheme.typography.bodySmall,
//                        color = MaterialTheme.colorScheme.primary
//                    )
//                }
//            }
//
//            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
//
//            PriceRow("Total", price.total, isTotal = true, currency = price.currency)
//        }
//    }
//}
//@Composable
//private fun PriceRow(
//    label    : String,
//    amount   : Double,
//    isTotal  : Boolean = false,
//    currency : String = "USD"
//) {
//    val symbol = when (currency) { "EUR" -> "€"; "GBP" -> "£"; else -> "$" }
//    Row(
//        modifier              = Modifier.fillMaxWidth(),
//        horizontalArrangement = Arrangement.SpaceBetween
//    ) {
//        Text(
//            label,
//            style = if (isTotal) MaterialTheme.typography.titleSmall
//            else         MaterialTheme.typography.bodySmall
//        )
//        Text(
//            "$symbol${String.format("%.2f", amount)}",
//            style = if (isTotal) MaterialTheme.typography.titleSmall
//            else         MaterialTheme.typography.bodySmall,
//            color = if (isTotal) MaterialTheme.colorScheme.primary
//            else         MaterialTheme.colorScheme.onSurface
//        )
//    }
//}
//
//
//@Composable
//private fun ProceedBottomBar(
//    priceEstimate : PriceEstimate?,
//    isLoading     : Boolean,
//    enabled       : Boolean,
//    onProceed     : () -> Unit
//) {
//    Surface(shadowElevation = 8.dp, color = MaterialTheme.colorScheme.surface) {
//        Row(
//            modifier              = Modifier
//                .fillMaxWidth()
//                .padding(horizontal = 20.dp, vertical = 16.dp),
//            verticalAlignment     = CenterVertically,
//            horizontalArrangement = Arrangement.SpaceBetween
//        ) {
//            priceEstimate?.let {
//                Column {
//                    Text("Total", style = MaterialTheme.typography.labelSmall)
//                    Text("$${String.format("%.2f", it.total)}",
//                        style = MaterialTheme.typography.titleMedium,
//                        color = MaterialTheme.colorScheme.primary)
//                }
//            }
//            Button(
//                onClick  = onProceed,
//                enabled  = enabled && !isLoading,
//                modifier = Modifier.height(48.dp),
//                shape    = RoundedCornerShape(12.dp)
//            ) {
//                if (isLoading) {
//                    CircularProgressIndicator(Modifier.size(18.dp),
//                        color = MaterialTheme.colorScheme.onPrimary)
//                } else {
//                    Text("Preview 3D Model")
//                    Spacer(Modifier.width(8.dp))
//                    Icon(Icons.Default.ArrowForward, null, Modifier.size(18.dp))
//                }
//            }
//        }
//    }
//}

// ═══════════════════════════════════════════════════════════════════
// Previews
// ═══════════════════════════════════════════════════════════════════
//
//@Preview(showBackground = true, name = "Human Placeholder – Upper Limb (model ready)")
//@Composable
//private fun PreviewHumanUpper_Ready() {
//    AppTheme {
//        HumanPlaceholderScreen(
//            productCategory = ProductCategory.JEWELRY,
//            isModelReady    = true,
//            onProceedToAr   = {},
//            onBack          = {}
//        )
//    }
//}
//
//@Preview(showBackground = true, name = "Human Placeholder – Lower Limb (loading)")
//@Composable
//private fun PreviewHumanLower_Loading() {
//    AppTheme {
//        HumanPlaceholderScreen(
//            productCategory = ProductCategory.FOOTWEAR,
//            isModelReady    = false,
//            onProceedToAr   = {},
//            onBack          = {}
//        )
//    }
//}

//@Preview(showBackground = true, name = "Human Placeholder – Clothing")
//@Composable
//private fun PreviewHumanClothing() {
//    AppTheme {
//        HumanPlaceholderScreen(
//            productCategory = ProductCategory.CLOTHING,
//            isModelReady    = true,
//            onProceedToAr   = {},
//            onBack          = {}
//        )
//    }
//}
//
//@Preview(showBackground = true, name = "Config Screen – Jewellery")
//@Composable
//private fun PreviewConfigScreen() {
//    val sampleProduct = Product(
//        id                 = "prod-001",
//        name               = "Forearm Cover",
//        description        = "Personalised prosthetic forearm cover.",
//        basePrice          = 149.99,
//        category           = ProductCategory.JEWELRY,
//        availableMaterials = listOf(Material.PLA, Material.CARBON_FIBER),
//        availableTextures  = listOf(
//            Texture("tex-001", "Matte",   "", "Smooth matte surface"),
//            Texture("tex-002", "Glossy",  "", "High-gloss finish")
//        ),
//        imageUrl = ""
//    )
//
//    val sampleConfig = ProductConfig(
//        id                 = "config-123",
//        productId          = "prod-001",
//        productCategory    = ProductCategory.JEWELRY,
//        measurement        = Measurement(150.0, 75.0, 10.0),
//        sizeLabel          = SizeLabel.M,
//        color              = ProductColor("Dark green", "#0f3F15"),
//        material           = Material.PLA,
//        texture            = Texture("tex-001", "Matte", "", "Smooth matte surface"),
//        inscription        = "Custom",
//        userPhotoUri       = null,
//        additionalElements = listOf(AdditionalElement.LOGO),
//        quantity           = 1,
//        createdAt          = 0L
//    )
//
//    AppTheme {
//        ProductConfigScreenContent(
//            state    = ProductConfigUiState(
//                selectedProduct = sampleProduct,
//                currentConfig   = sampleConfig,
//                priceEstimate   = PriceEstimate(
//                    configId           = "config-123",
//                    basePrice          = 149.99,
//                    materialSurcharges = mapOf("PLA" to 2.00),
//                    textureSurcharge   = 2.00,
//                    sizeMultiplier     = 1.15,
//                    sizeMultiplierLabel = "L +15%",
//                    quantity           = 1,
//                    subtotal           = 155.67,
//                    total              = 155.67
//                )
//            ),
//            onAction = {},
//            onBack   = {}
//        )
//    }
//}