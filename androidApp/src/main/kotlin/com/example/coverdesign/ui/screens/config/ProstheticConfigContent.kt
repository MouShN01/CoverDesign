package com.example.coverdesign.ui.screens.config

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.coverdesign.domain.model.ButtonPlacement
import com.example.coverdesign.domain.model.FasteningType
import com.example.coverdesign.domain.model.OrderCategory
import com.example.coverdesign.domain.model.ProstheticType
import com.example.coverdesign.ui.constants.UIConstants.DEFAULT_TEXTURES
import com.example.coverdesign.ui.viewmodel.LegSide
import com.example.coverdesign.ui.viewmodel.LimbLevel
import com.example.coverdesign.ui.viewmodel.ProductConfigAction
import com.example.coverdesign.ui.viewmodel.ProductConfigUiState

@Composable
internal fun ProstheticCoverConfigContent(
    state: ProductConfigUiState,
    onAction: (ProductConfigAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val pc = state.prostheticConfig
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        ConfigSection(title = "Prosthesis Type") {
            Text("Limb side", style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LegSide.entries.forEach { side ->
                    FilterChip(
                        selected = pc.limbSide == side,
                        onClick = { onAction(ProductConfigAction.UpdateProstheticSide(side)) },
                        label = { Text(side.displayName) }
                    )
                }
            }
            Text("Level", style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                LimbLevel.values().forEach { level ->
                    FilterChip(
                        selected = pc.limbLevel == level,
                        onClick = { onAction(ProductConfigAction.SelectLimbLevel(level)) },
                        label = { Text(level.displayName) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            if (pc.limbLevel != null) {
                ProstheticSubtypeDropdown(
                    limbLevel = pc.limbLevel!!,
                    selected = pc.prostheticType,
                    onSelected = { onAction(ProductConfigAction.SelectProstheticSubtype(it)) }
                )
            }
        }
        ConfigSection(title = "Common Measurements") {
            MeasurementFieldWithInfo(
                label = "Circumference",
                value = pc.commonMeasurements.circumferenceMm,
                instructions = listOf("Measure the circumference of the limb at its widest point."),
                imageUrl = null,
                onChange = { onAction(ProductConfigAction.UpdateCircumference(it)) }
            )
            MeasurementFieldWithInfo(
                label = "Knee Apex to Top of Footshell",
                value = pc.commonMeasurements.kneeApexToFootshellTopMm,
                instructions = listOf("Measure from the apex of the knee unit all the way down to the top of the footshell."),
                imageUrl = null,
                onChange = { onAction(ProductConfigAction.UpdateKneeApexLength(it)) }
            )
        }
        if (pc.prostheticType != null) {
            ConfigSection(title = "${pc.prostheticType!!.displayName} Measurements") {
                TypeSpecificMeasurementsSection(
                    prostheticType = pc.prostheticType!!,
                    onTypeUpdated = { onAction(ProductConfigAction.UpdateProstheticSubtypeMeasurement(it)) }
                )
            }
        }
        ConfigSection(title = "Fastening") {
            FasteningTypePicker(
                selected = pc.fasteningType,
                onChanged = { onAction(ProductConfigAction.UpdateFasteningType(it)) }
            )
        }
        ConfigSection(title = "Appearance") {
            MaterialSelector(
                selected = pc.material,
                allowedMaterials = allowedMaterials(OrderCategory.COSMETIC_COVER),
                onChanged = { onAction(ProductConfigAction.UpdateProstheticMaterial(it)) }
            )
            ColorPicker(
                selected = pc.color,
                onChanged = { onAction(ProductConfigAction.UpdateProstheticColor(it)) }
            )
            TextureSelector(
                selected = pc.texture,
                textures = DEFAULT_TEXTURES,
                onChanged = { onAction(ProductConfigAction.UpdateProstheticTexture(it)) }
            )
        }
        ConfigSection(title = "Price Estimate") {
            PriceEstimateSection(
                state = state,
                onAction = onAction
            )
        }
        ConfigSection(title = "Order") {
            QuantitySelector(
                value = pc.quantity,
                error = null,
                onChanged = { onAction(ProductConfigAction.UpdateProstheticQuantity(it)) }
            )
        }
        ConfigSection(title = "Additional Notes") {
            OutlinedTextField(
                value = pc.additionalNotes,
                onValueChange = { onAction(ProductConfigAction.UpdateProstheticAdditionalNotes(it)) },
                label = { Text("Notes (optional)") },
                placeholder = { Text("Any specific requirements or details about your prosthetic cover…") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 6,
                shape = RoundedCornerShape(10.dp)
            )
        }
        Spacer(Modifier.height(80.dp))
    }
}

@Composable
private fun MeasurementFieldWithInfo(
    label: String,
    value: Float,
    instructions: List<String>,
    imageUrl: String?,
    onChange: (Float) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    Row(
        verticalAlignment = CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        OutlinedTextField(
            value = if (value == 0f) "" else value.toInt().toString(),
            onValueChange = { it.toFloatOrNull()?.let(onChange) },
            label = { Text(label) },
            suffix = { Text("mm") },
            modifier = Modifier.weight(1f),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            shape = RoundedCornerShape(10.dp)
        )
        IconButton(onClick = { showDialog = true }) {
            Icon(
                Icons.Outlined.Info,
                "Measurement guide",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
        }
    }
    if (showDialog) {
        MeasurementInfoDialog(
            instructions = instructions,
            imageUrl = imageUrl,
            onDismiss = { showDialog = false }
        )
    }
}

@Composable
private fun MeasurementInfoDialog(
    instructions: List<String>,
    imageUrl: String?,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Measurement Guide") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                instructions.forEachIndexed { i, instruction ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "${i + 1}.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(instruction, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Got it") } }
    )
}

@Composable
private fun TypeSpecificMeasurementsSection(
    prostheticType: ProstheticType,
    onTypeUpdated: (ProstheticType) -> Unit
) {
    when (prostheticType) {
        is ProstheticType.AboveKnee.PolycentricKnee -> MeasurementFieldWithInfo(
            label = "Knee Unit",
            value = prostheticType.kneeUnitMm,
            instructions = prostheticType.measurementSpec.instructions,
            imageUrl = prostheticType.measurementSpec.imageUrl
        ) { onTypeUpdated(prostheticType.copy(kneeUnitMm = it)) }

        is ProstheticType.AboveKnee.TotalKnee -> MeasurementFieldWithInfo(
            label = "Knee Unit",
            value = prostheticType.kneeUnitMm,
            instructions = prostheticType.measurementSpec.instructions,
            imageUrl = prostheticType.measurementSpec.imageUrl
        ) { onTypeUpdated(prostheticType.copy(kneeUnitMm = it)) }

        is ProstheticType.AboveKnee.ThroughKnee -> Text(
            "No additional measurements required for Through Knee.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        is ProstheticType.AboveKnee.HighProfile -> MeasurementFieldWithInfo(
            label = "Top of Footshell to Top of Foot Unit",
            value = prostheticType.footshellToFootUnitMm,
            instructions = prostheticType.measurementSpec.instructions,
            imageUrl = prostheticType.measurementSpec.imageUrl
        ) { onTypeUpdated(prostheticType.copy(footshellToFootUnitMm = it)) }

        is ProstheticType.AboveKnee.Blade -> MeasurementFieldWithInfo(
            label = "Top of Footshell to Top of Foot Unit",
            value = prostheticType.footshellToFootUnitMm,
            instructions = prostheticType.measurementSpec.instructions,
            imageUrl = prostheticType.measurementSpec.imageUrl
        ) { onTypeUpdated(prostheticType.copy(footshellToFootUnitMm = it)) }

        is ProstheticType.AboveKnee.Microprocessor -> MeasurementFieldWithInfo(
            label = "Top of Footshell to Top of Foot Unit",
            value = prostheticType.footshellToFootUnitMm,
            instructions = prostheticType.measurementSpec.instructions,
            imageUrl = prostheticType.measurementSpec.imageUrl
        ) { onTypeUpdated(prostheticType.copy(footshellToFootUnitMm = it)) }

        is ProstheticType.AboveKnee.HydraulicFoot -> MeasurementFieldWithInfo(
            label = "Top of Footshell to Top of Foot Unit",
            value = prostheticType.footshellToFootUnitMm,
            instructions = prostheticType.measurementSpec.instructions,
            imageUrl = prostheticType.measurementSpec.imageUrl
        ) { onTypeUpdated(prostheticType.copy(footshellToFootUnitMm = it)) }

        is ProstheticType.BelowKnee.ReleaseButton -> {
            MeasurementFieldWithInfo(
                label = "Top of Footshell to Button Centre",
                value = prostheticType.footshellToButtonCenterMm,
                instructions = prostheticType.measurementSpec.instructions,
                imageUrl = prostheticType.measurementSpec.imageUrl
            ) { onTypeUpdated(prostheticType.copy(footshellToButtonCenterMm = it)) }
            ButtonPlacementSelector(selected = prostheticType.buttonPlacement) {
                onTypeUpdated(prostheticType.copy(buttonPlacement = it))
            }
        }

        is ProstheticType.BelowKnee.ReleaseValve -> {
            MeasurementFieldWithInfo(
                label = "Top of Footshell to Button Centre",
                value = prostheticType.footshellToButtonCenterMm,
                instructions = prostheticType.measurementSpec.instructions,
                imageUrl = prostheticType.measurementSpec.imageUrl
            ) { onTypeUpdated(prostheticType.copy(footshellToButtonCenterMm = it)) }
            ButtonPlacementSelector(selected = prostheticType.buttonPlacement) {
                onTypeUpdated(prostheticType.copy(buttonPlacement = it))
            }
        }

        is ProstheticType.BelowKnee.PinlockButton -> {
            MeasurementFieldWithInfo(
                label = "Top of Footshell to Button Centre",
                value = prostheticType.footshellToButtonCenterMm,
                instructions = prostheticType.measurementSpec.instructions,
                imageUrl = prostheticType.measurementSpec.imageUrl
            ) { onTypeUpdated(prostheticType.copy(footshellToButtonCenterMm = it)) }
            ButtonPlacementSelector(selected = prostheticType.buttonPlacement) {
                onTypeUpdated(prostheticType.copy(buttonPlacement = it))
            }
        }

        is ProstheticType.BelowKnee.HighProfile -> MeasurementFieldWithInfo(
            label = "Top of Footshell to Top of Foot Unit",
            value = prostheticType.footshellToFootUnitMm,
            instructions = prostheticType.measurementSpec.instructions,
            imageUrl = prostheticType.measurementSpec.imageUrl
        ) { onTypeUpdated(prostheticType.copy(footshellToFootUnitMm = it)) }

        is ProstheticType.BelowKnee.Blade -> MeasurementFieldWithInfo(
            label = "Top of Footshell to Top of Foot Unit",
            value = prostheticType.footshellToFootUnitMm,
            instructions = prostheticType.measurementSpec.instructions,
            imageUrl = prostheticType.measurementSpec.imageUrl
        ) { onTypeUpdated(prostheticType.copy(footshellToFootUnitMm = it)) }

        is ProstheticType.BelowKnee.Microprocessor -> MeasurementFieldWithInfo(
            label = "Top of Footshell to Top of Foot Unit",
            value = prostheticType.footshellToFootUnitMm,
            instructions = prostheticType.measurementSpec.instructions,
            imageUrl = prostheticType.measurementSpec.imageUrl
        ) { onTypeUpdated(prostheticType.copy(footshellToFootUnitMm = it)) }

        is ProstheticType.BelowKnee.HydraulicFoot -> MeasurementFieldWithInfo(
            label = "Top of Footshell to Top of Foot Unit",
            value = prostheticType.footshellToFootUnitMm,
            instructions = prostheticType.measurementSpec.instructions,
            imageUrl = prostheticType.measurementSpec.imageUrl
        ) { onTypeUpdated(prostheticType.copy(footshellToFootUnitMm = it)) }

        is ProstheticType.BelowKnee.Rotationplasty -> {
            MeasurementFieldWithInfo(
                label = "Tip of Pointed Toe to Top of Footshell",
                value = prostheticType.toeToFootshellMm,
                instructions = listOf(
                    prostheticType.measurementSpec.instructions.getOrElse(0) {
                        "Measure from tip of Pointed Toe to top of footshell."
                    }
                ),
                imageUrl = prostheticType.measurementSpec.imageUrl
            ) { onTypeUpdated(prostheticType.copy(toeToFootshellMm = it)) }
            MeasurementFieldWithInfo(
                label = "Heel/Knee of Socket to Top of Footshell",
                value = prostheticType.heelKneeToFootshellMm,
                instructions = listOf(
                    prostheticType.measurementSpec.instructions.getOrElse(1) {
                        "Measure from just below the 'Heel/Knee' of the socket to the top of the footshell."
                    }
                ),
                imageUrl = null
            ) { onTypeUpdated(prostheticType.copy(heelKneeToFootshellMm = it)) }
        }

        is ProstheticType.BelowKnee.SymesSocket -> Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                "Part of the prosthetic cover will be removed to accommodate the Symes socket fit.",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(14.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProstheticSubtypeDropdown(
    limbLevel: LimbLevel,
    selected: ProstheticType?,
    onSelected: (ProstheticType) -> Unit
) {
    val subtypes = remember(limbLevel) { subtypesForLevel(limbLevel) }
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = selected?.displayName ?: "Select type…",
            onValueChange = {},
            readOnly = true,
            label = { Text("Prosthesis Subtype") },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            shape = RoundedCornerShape(10.dp)
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            subtypes.forEach { type ->
                DropdownMenuItem(
                    text = { Text(type.displayName) },
                    onClick = { onSelected(type); expanded = false },
                    leadingIcon = if (selected?.displayName == type.displayName) ({
                        Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary)
                    }) else null
                )
            }
        }
    }
}

private fun subtypesForLevel(level: LimbLevel): List<ProstheticType> = when (level) {
    LimbLevel.ABOVE_KNEE -> listOf(
        ProstheticType.AboveKnee.PolycentricKnee(),
        ProstheticType.AboveKnee.TotalKnee(),
        ProstheticType.AboveKnee.ThroughKnee,
        ProstheticType.AboveKnee.HighProfile(),
        ProstheticType.AboveKnee.Blade(),
        ProstheticType.AboveKnee.Microprocessor(),
        ProstheticType.AboveKnee.HydraulicFoot()
    )

    LimbLevel.BELOW_KNEE -> listOf(
        ProstheticType.BelowKnee.ReleaseButton(),
        ProstheticType.BelowKnee.ReleaseValve(),
        ProstheticType.BelowKnee.PinlockButton(),
        ProstheticType.BelowKnee.HighProfile(),
        ProstheticType.BelowKnee.Blade(),
        ProstheticType.BelowKnee.Microprocessor(),
        ProstheticType.BelowKnee.HydraulicFoot(),
        ProstheticType.BelowKnee.Rotationplasty(),
        ProstheticType.BelowKnee.SymesSocket
    )
}

@Composable
private fun FasteningTypePicker(
    selected: FasteningType,
    onChanged: (FasteningType) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        FasteningType.entries.forEach { type ->
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (type == selected)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.surface,
                tonalElevation = if (type == selected) 0.dp else 1.dp,
                onClick = { onChanged(type) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (type == selected) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            type.displayName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (type == selected)
                                MaterialTheme.colorScheme.onPrimaryContainer
                            else
                                MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            type.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ButtonPlacementSelector(
    selected: ButtonPlacement,
    onChanged: (ButtonPlacement) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = selected.name.lowercase().replaceFirstChar { it.uppercase() },
            onValueChange = {},
            readOnly = true,
            label = { Text("Button Placement") },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            shape = RoundedCornerShape(10.dp)
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            ButtonPlacement.values().forEach { placement ->
                DropdownMenuItem(
                    text = { Text(placement.name.lowercase().replaceFirstChar { it.uppercase() }) },
                    onClick = { onChanged(placement); expanded = false },
                    leadingIcon = if (placement == selected) ({
                        Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary)
                    }) else null
                )
            }
        }
    }
}