package com.example.coverdesign.ui.screens

import android.graphics.Color.parseColor
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Remove
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.coverdesign.domain.model.AdditionalElement
import com.example.coverdesign.domain.model.Material
import com.example.coverdesign.domain.model.Measurement
import com.example.coverdesign.domain.model.PriceEstimate
import com.example.coverdesign.domain.model.ProductColor
import com.example.coverdesign.domain.model.SizeLabel
import com.example.coverdesign.domain.model.Texture
import com.example.coverdesign.ui.components.AppTopBar
import com.example.coverdesign.ui.components.EventHandler
import com.example.coverdesign.ui.components.LoadingScreen
import com.example.coverdesign.ui.viewmodel.ProductConfigAction
import com.example.coverdesign.ui.viewmodel.ProductConfigUiEvent
import com.example.coverdesign.ui.viewmodel.ProductConfigUiState
import com.example.coverdesign.ui.viewmodel.ProductConfigViewModel
import org.koin.compose.viewmodel.koinViewModel

// androidApp/ui/screens/config/ProductConfigScreen.kt
@Composable
fun ProductConfigScreen(
    productId  : String,
    viewModel  : ProductConfigViewModel = koinViewModel(),
    onProceed  : (configId: String) -> Unit,
    onBack     : () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    EventHandler(viewModel.events) { event ->
        when (event) {
            is ProductConfigUiEvent.NavigateToPreview -> onProceed(event.configId)
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title  = "Configure Product",
                onBack = onBack
            )
        },
        bottomBar = {
            ProceedBottomBar(
                priceEstimate = state.priceEstimate,
                isLoading     = state.isSaving,
                enabled       = state.currentConfig != null && state.measurementError == null,
                onProceed     = { viewModel.onAction(ProductConfigAction.SaveAndProceed) }
            )
        }
    ) { padding ->
        if (state.isLoading) {
            LoadingScreen()
        } else {
            ProductConfigContent(
                state    = state,
                onAction = viewModel::onAction,
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@Composable
private fun ProductConfigContent(
    state    : ProductConfigUiState,
    onAction : (ProductConfigAction) -> Unit,
    modifier : Modifier = Modifier
) {
    val config = state.currentConfig ?: return

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // ── Measurements ──────────────────────────────────────────────────
        ConfigSection(title = "Measurements") {
            MeasurementInputs(
                measurement = config.measurement,
                error       = state.measurementError,
                onChanged   = { onAction(ProductConfigAction.UpdateMeasurement(it)) }
            )
            SizeLabelSelector(
                selected  = config.sizeLabel,
                onChanged = { onAction(ProductConfigAction.UpdateSizeLabel(it)) }
            )
        }

        // ── Appearance ────────────────────────────────────────────────────
        ConfigSection(title = "Appearance") {
            MaterialSelector(
                selected  = config.material,
                onChanged = { onAction(ProductConfigAction.UpdateMaterial(it)) }
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
                value    = config.inscription,
                error    = state.inscriptionError,
                onChanged = { onAction(ProductConfigAction.UpdateInscription(it)) }
            )
            AdditionalElementsSelector(
                selected = config.additionalElements,
                onAdd    = { onAction(ProductConfigAction.AddElement(it)) },
                onRemove = { onAction(ProductConfigAction.RemoveElement(it)) }
            )
        }

        // ── Order options ─────────────────────────────────────────────────
        ConfigSection(title = "Order") {
            QuantitySelector(
                value    = config.quantity,
                error    = state.quantityError,
                onChanged = { onAction(ProductConfigAction.UpdateQuantity(it)) }
            )
        }

        // ── Price ─────────────────────────────────────────────────────────
        state.priceEstimate?.let { price ->
            PriceCard(price = price)
        }
        OutlinedButton(
            onClick  = { onAction(ProductConfigAction.RequestPriceUpdate) },
            modifier = Modifier.fillMaxWidth(),
            enabled  = !state.isPriceLoading
        ) {
            if (state.isPriceLoading) {
                CircularProgressIndicator(Modifier.size(16.dp))
            } else {
                Text("Calculate Price")
            }
        }

        Spacer(Modifier.height(80.dp))   // bottom bar clearance
    }
}

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
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                content = content
            )
        }
    }
}

@Composable
private fun MeasurementInputs(
    measurement : Measurement,
    error       : String?,
    onChanged   : (Measurement) -> Unit
) {
    listOf(
        Triple("Height", measurement.height) { v: Double -> onChanged(measurement.copy(height = v)) },
        Triple("Width",  measurement.width)  { v: Double -> onChanged(measurement.copy(width  = v)) },
        Triple("Depth",  measurement.depth)  { v: Double -> onChanged(measurement.copy(depth  = v)) }
    ).forEach { (label, value, updater) ->
        Row(
            modifier          = Modifier.fillMaxWidth(),
            verticalAlignment = CenterVertically
        ) {
            Text(label, modifier = Modifier.width(60.dp),
                style = MaterialTheme.typography.bodyMedium)
            OutlinedTextField(
                value         = if (value == 0.0) "" else value.toString(),
                onValueChange = { updater(it.toDoubleOrNull() ?: 0.0) },
                modifier      = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError       = error != null,
                trailingIcon  = { Text("mm", style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(end = 8.dp)) },
                singleLine    = true,
                shape         = RoundedCornerShape(10.dp)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MaterialSelector(selected: Material, onChanged: (Material) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
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
            Material.values().forEach { material ->
                DropdownMenuItem(
                    text    = { Text(material.displayName) },
                    onClick = { onChanged(material); expanded = false },
                    leadingIcon = if (material == selected) ({
                        Icon(Icons.Default.Check, null,
                            tint = MaterialTheme.colorScheme.primary)
                    }) else null
                )
            }
        }
    }
}

@Composable
private fun ColorPicker(selected: ProductColor, onChanged: (ProductColor) -> Unit) {
    // preset colors — in a real app these come from Product.availableColors
    val presets = listOf(
        ProductColor("Black", "#000000"),
        ProductColor("White",  "#FFFFFF"),
        ProductColor("Red",    "#FF0000"),
        ProductColor("Blue",   "#0000FF"),
        ProductColor("Custom", selected.hex)
    )
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Color", style = MaterialTheme.typography.bodyMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            presets.forEach { color ->
                val hexColor = Color(parseColor(
                    color.hex.ifEmpty { "#000000" }
                ))
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(hexColor)
                        .border(
                            width = if (selected.hex == color.hex) 3.dp else 1.dp,
                            color = if (selected.hex == color.hex)
                                MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outline,
                            shape = CircleShape
                        )
                        .clickable { onChanged(color) }
                )
            }
        }
        // hex code input for custom color
        OutlinedTextField(
            value         = selected.hex,
            onValueChange = { onChanged(selected.copy(hex = it)) },
            label         = { Text("Hex code") },
            modifier      = Modifier.fillMaxWidth(),
            singleLine    = true,
            shape         = RoundedCornerShape(10.dp),
            placeholder   = { Text("#FFAA00") }
        )
    }
}

@Composable
private fun TextureSelector(selected: Texture, onChanged: (Texture) -> Unit) {
    // textures loaded from product — simplified here
    Text("Texture", style = MaterialTheme.typography.bodyMedium)
    Text(
        selected.name,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
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
                Text("${value.length}/50",
                    style = MaterialTheme.typography.labelSmall)
            }
        },
        singleLine    = true,
        shape         = RoundedCornerShape(10.dp)
    )
}

@Composable
private fun AdditionalElementsSelector(
    selected : List<AdditionalElement>,
    onAdd    : (AdditionalElement) -> Unit,
    onRemove : (AdditionalElement) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Additional elements", style = MaterialTheme.typography.bodyMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AdditionalElement.values().forEach { element ->
                val isSelected = element in selected
                FilterChip(
                    selected  = isSelected,
                    onClick   = { if (isSelected) onRemove(element) else onAdd(element) },
                    label     = { Text(element.displayName) },
                    leadingIcon = if (isSelected) ({
                        Icon(Icons.Default.Check, null, Modifier.size(16.dp))
                    }) else null
                )
            }
        }
    }
}

@Composable
private fun QuantitySelector(value: Int, error: String?, onChanged: (Int) -> Unit) {
    Row(
        modifier          = Modifier.fillMaxWidth(),
        verticalAlignment = CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("Quantity", style = MaterialTheme.typography.bodyMedium)
        Row(verticalAlignment = CenterVertically) {
            IconButton(
                onClick  = { if (value > 1) onChanged(value - 1) },
                enabled  = value > 1
            ) { Icon(Icons.Default.Remove, null) }
            Text(
                text  = value.toString(),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
            IconButton(
                onClick = { if (value < 999) onChanged(value + 1) },
                enabled = value < 999
            ) { Icon(Icons.Default.Add, null) }
        }
    }
    error?.let {
        Text(it, color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun PriceCard(price: PriceEstimate) {
    Surface(
        shape  = RoundedCornerShape(12.dp),
        color  = MaterialTheme.colorScheme.primaryContainer
    ) {
        Column(modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Price breakdown",
                style = MaterialTheme.typography.titleSmall)
            PriceRow("Base price",         price.basePrice)
            PriceRow("Material surcharge", price.materialSurcharge)
            PriceRow("Texture surcharge",  price.textureSurcharge)
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            PriceRow("Total", price.total, isTotal = true)
        }
    }
}

@Composable
private fun PriceRow(label: String, amount: Double, isTotal: Boolean = false) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label,
            style = if (isTotal) MaterialTheme.typography.titleSmall
            else MaterialTheme.typography.bodySmall)
        Text("$${String.format("%.2f", amount)}",
            style = if (isTotal) MaterialTheme.typography.titleSmall
            else MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun ProceedBottomBar(
    priceEstimate : PriceEstimate?,
    isLoading     : Boolean,
    enabled       : Boolean,
    onProceed     : () -> Unit
) {
    Surface(
        shadowElevation = 8.dp,
        color           = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
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
                if (isLoading) CircularProgressIndicator(
                    Modifier.size(18.dp), color = MaterialTheme.colorScheme.onPrimary
                )
                else {
                    Text("Preview 3D Model")
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.Default.ArrowForward, null, Modifier.size(18.dp))
                }
            }
        }
    }
}