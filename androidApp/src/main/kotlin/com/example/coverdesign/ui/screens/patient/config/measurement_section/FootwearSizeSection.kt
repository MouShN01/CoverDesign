package com.example.coverdesign.ui.screens.config.measurement_section

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.coverdesign.domain.model.Measurement
import com.example.coverdesign.domain.model.SizeLabel
import kotlin.math.abs


// ─────────────────────────────────────────────────────────────────────
// FOOTWEAR section
// ─────────────────────────────────────────────────────────────────────

private fun nearestEuFor(lengthMm: Double): Int {
    if (lengthMm <= 0) return 0
    return EU_TO_MM.minByOrNull { (_, mm) -> abs(mm - lengthMm.toInt()) }?.key ?: 0
}

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
fun FootwearSizeSection(
    measurement: Measurement,
    sizeLabel: SizeLabel?,
    error: String?,
    onMeasure: (Measurement) -> Unit,
    onSize: (SizeLabel?) -> Unit
) {
    var showGuide by remember { mutableStateOf(false) }
    // we store selected EU size in measurement.depth repurposed as euSize
    val selectedEu = measurement.depth.toInt().let { if (it < 35 || it > 46) 0 else it }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

        // EU size grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("EU Shoe Size", style = MaterialTheme.typography.bodyMedium)
            IconButton(onClick = { showGuide = true }) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "How to measure",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            EU_SIZES.forEach { eu ->
                FilterChip(
                    selected = selectedEu == eu,
                    onClick = {},
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
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }

        HorizontalDivider()

        // Foot length manual input
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Foot Length", style = MaterialTheme.typography.bodyMedium)
            IconButton(onClick = { showGuide = true }) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Measuring guide",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        OutlinedTextField(
            value = if (measurement.height == 0.0) "" else measurement.height.toInt().toString(),
            onValueChange = { raw ->
                val len = raw.toDoubleOrNull() ?: 0.0
                onMeasure(measurement.copy(height = len, depth = nearestEuFor(len).toDouble()))
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Foot length") },
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
        // Foot width
        OutlinedTextField(
            value = if (measurement.width == 0.0) "" else measurement.width.toInt().toString(),
            onValueChange = { onMeasure(measurement.copy(width = it.toDoubleOrNull() ?: 0.0)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Foot width (widest point)") },
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

    if (showGuide) {
        FootMeasuringGuideDialog(onDismiss = { showGuide = false })
    }
}

@Composable
private fun FootMeasuringGuideDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.primary) },
        title = { Text("How to measure your foot") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                GuideStep(
                    number = "1",
                    text = "Place a blank sheet of paper on a hard floor. Stand on it with your heel touching a wall."
                )
                GuideStep(
                    number = "2",
                    text = "Mark the tip of your longest toe with a pencil, keeping the pen vertical."
                )
                GuideStep(
                    number = "3",
                    text = "Measure the distance from the edge of the paper (wall side) to the mark — this is your foot length in mm."
                )
                GuideStep(
                    number = "4",
                    text = "Measure the widest part of your foot (usually across the ball of the foot) — this is your foot width."
                )
                GuideStep(
                    number = "5",
                    text = "Measure both feet and enter the larger measurement — feet are rarely identical in size."
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        "Tip: measure in the evening when feet are slightly larger after a day of activity.",
                        style = MaterialTheme.typography.bodySmall,
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
        verticalAlignment = CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(26.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary
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
