package com.example.coverdesign.ui.screens.config.measurement_section

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.coverdesign.domain.model.Measurement
import com.example.coverdesign.domain.model.SizeLabel


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
fun AccessorySizeSection(
    measurement: Measurement,
    sizeLabel: SizeLabel?,
    error: String?,
    onMeasure: (Measurement) -> Unit,
    onSize: (SizeLabel?) -> Unit
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
                    onClick = {
                        onMeasure(measurement.copy(depth = sg.ordinal.toDouble()))
                    },
                    label = { Text(sg.label) }
                )
            }
        }

        HorizontalDivider()

        // ── Size selector — same S/M/L for all, but ring shows "i" button ─
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Size", style = MaterialTheme.typography.bodyMedium)
            if (subgroup == AccessorySubgroup.RING) {
                IconButton(onClick = { showRingInfo = true }) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = "Ring size guide",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SizeLabel.values().forEach { label ->
                FilterChip(
                    selected = sizeLabel == label,
                    onClick = { onSize(if (sizeLabel == label) null else label) },
                    label = { Text(label.name) }
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
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }

        HorizontalDivider()

        // ── Thickness input — applicable to all accessory subgroups ───────
        Text("Dimensions", style = MaterialTheme.typography.bodyMedium)
        OutlinedTextField(
            value = if (measurement.height == 0.0) "" else measurement.height.toString(),
            onValueChange = { onMeasure(measurement.copy(height = it.toDoubleOrNull() ?: 0.0)) },
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text(
                    when (subgroup) {
                        AccessorySubgroup.RING -> "Band thickness"
                        AccessorySubgroup.EARRING -> "Length"
                        AccessorySubgroup.BRACELET -> "Width"
                    }
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            isError = error != null,
            trailingIcon = {
                Text(
                    "mm", style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(end = 8.dp)
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(10.dp)
        )
        OutlinedTextField(
            value = if (measurement.width == 0.0) "" else measurement.width.toString(),
            onValueChange = { onMeasure(measurement.copy(width = it.toDoubleOrNull() ?: 0.0)) },
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text(
                    when (subgroup) {
                        AccessorySubgroup.RING -> "Band width"
                        AccessorySubgroup.EARRING -> "Width"
                        AccessorySubgroup.BRACELET -> "Length"
                    }
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            trailingIcon = {
                Text(
                    "mm", style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(end = 8.dp)
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(10.dp)
        )

        error?.let {
            Text(
                it, color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }

    if (showRingInfo) {
        RingSizeInfoDialog(onDismiss = { showRingInfo = false })
    }
}
