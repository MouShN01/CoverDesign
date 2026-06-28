package com.example.coverdesign.ui.screens.config.measurement_section

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.coverdesign.domain.model.Measurement
import com.example.coverdesign.domain.model.ProductCategory
import com.example.coverdesign.domain.model.SizeLabel

@Composable
fun MeasurementSection(
    category: ProductCategory,
    measurement: Measurement,
    sizeLabel: SizeLabel?,
    measError: String?,
    onMeasure: (Measurement) -> Unit,
    onSize: (SizeLabel?) -> Unit
) {
    when (category) {

        // ── FOOTWEAR ──────────────────────────────────────────────────────
        // EU shoe size grid + foot-length input with measuring guide
        ProductCategory.FOOTWEAR -> {
            FootwearSizeSection(
                measurement = measurement,
                sizeLabel = sizeLabel,
                error = measError,
                onMeasure = onMeasure,
                onSize = onSize
            )
        }

        // ── ACCESSORY ─────────────────────────────────────────────────────
        // Subgroup selector (Ring / Earring / Bracelet) then size inputs
        ProductCategory.ACCESSORY -> {
            AccessorySizeSection(
                measurement = measurement,
                sizeLabel = sizeLabel,
                error = measError,
                onMeasure = onMeasure,
                onSize = onSize
            )
        }

        // ── THREE_D_PRINT ─────────────────────────────────────────────────
        // Only raw mm measurements; no size grid
        ProductCategory.THREE_D_PRINT -> {
            MeasurementInputs(
                measurement = measurement,
                labels = listOf("Height", "Width", "Depth"),
                unit = "mm",
                error = measError,
                onChanged = onMeasure
            )
        }

        // ── INTERIOR_DECOR ────────────────────────────────────────────────
        // Only mm measurements; no size grid
        ProductCategory.INTERIOR_DECOR -> {
            MeasurementInputs(
                measurement = measurement,
                labels = listOf("Height", "Width", "Depth"),
                unit = "mm",
                error = measError,
                onChanged = onMeasure
            )
        }

        // ── CLOTHING ─────────────────────────────────────────────────────
        // Height (cm) required; Weight (kg) + Waist (cm) optional but helpful
        ProductCategory.CLOTHING -> {
            ClothingSizeSection(
                measurement = measurement,
                sizeLabel = sizeLabel,
                measError = measError,
                onMeasure = onMeasure,
                onSize = onSize
            )
        }

        ProductCategory.JEWELRY -> {
            AccessorySizeSection(
                measurement = measurement,
                sizeLabel = sizeLabel,
                error = measError,
                onMeasure = onMeasure,
                onSize = onSize
            )
        }
        // ── OTHER  ───────────────────────────────────────────────
        // Standard S/M/L selector + mm measurements
        else -> {
            SizeLabelSelector(
                selected = sizeLabel, onChanged = onSize
            )
            MeasurementInputs(
                measurement = measurement,
                labels = listOf("Height", "Width", "Depth"),
                unit = "mm",
                error = measError,
                onChanged = onMeasure
            )
        }
    }
}


// ─────────────────────────────────────────────────────────────────────
// Generic MeasurementInputs — accepts custom labels
// ─────────────────────────────────────────────────────────────────────

@Composable
private fun MeasurementInputs(
    measurement: Measurement,
    labels: List<String> = listOf("Height", "Width", "Depth"),
    unit: String = "mm",
    error: String?,
    onChanged: (Measurement) -> Unit
) {
    val fields = listOf(Triple(labels.getOrElse(0) { "Height" }, measurement.height) { v: Double ->
        onChanged(
            measurement.copy(height = v)
        )
    }, Triple(labels.getOrElse(1) { "Width" }, measurement.width) { v: Double ->
        onChanged(
            measurement.copy(width = v)
        )
    }, Triple(labels.getOrElse(2) { "Depth" }, measurement.depth) { v: Double ->
        onChanged(
            measurement.copy(depth = v)
        )
    })
    fields.forEach { (label, value, updater) ->
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = CenterVertically) {
            Text(
                label, modifier = Modifier.width(64.dp), style = MaterialTheme.typography.bodyMedium
            )
            OutlinedTextField(
                value = if (value == 0.0) "" else value.toString(),
                onValueChange = { updater(it.toDoubleOrNull() ?: 0.0) },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = error != null,
                trailingIcon = {
                    Text(
                        unit,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(10.dp)
            )
        }
    }
    error?.let {
        Text(
            it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall
        )
    }
}


@Composable
fun SizeLabelSelector(selected: SizeLabel?, onChanged: (SizeLabel?) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        SizeLabel.values().forEach { label ->
            FilterChip(
                selected = selected == label,
                onClick = { onChanged(if (selected == label) null else label) },
                label = { Text(label.name) })
        }
    }
}
