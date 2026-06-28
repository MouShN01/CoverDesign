package com.example.coverdesign.ui.screens.config.measurement_section

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.coverdesign.domain.model.Measurement
import com.example.coverdesign.domain.model.SizeLabel


// ─────────────────────────────────────────────────────────────────────
// CLOTHING section — body measurements
// ─────────────────────────────────────────────────────────────────────

@Composable
fun ClothingSizeSection(
    measurement: Measurement,
    sizeLabel: SizeLabel?,
    measError: String?,
    onMeasure: (Measurement) -> Unit,
    onSize: (SizeLabel?) -> Unit
) {
    // height stored in measurement.height (cm) — required
    // weight stored in measurement.width  (kg) — optional
    // waist  stored in measurement.depth  (cm) — optional
    val heightCm = measurement.height
    val weightKg = measurement.width
    val waistCm = measurement.depth

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
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = CenterVertically,
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
            value = if (heightCm == 0.0) "" else heightCm.toString(),
            onValueChange = { onMeasure(measurement.copy(height = it.toDoubleOrNull() ?: 0.0)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Height") },
            placeholder = { Text("e.g. 175") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            isError = measError != null || (heightCm == 0.0),
            supportingText = {
                if (heightCm == 0.0)
                    Text(
                        "Height is required to generate the 3D model",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall
                    )
                else if (heightCm < 100.0 || heightCm > 220.0)
                    Text(
                        "Please enter a realistic height (100–220 cm)",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall
                    )
            },
            trailingIcon = {
                Text(
                    "cm", style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(end = 8.dp)
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(10.dp)
        )

        // ── Weight (optional) ─────────────────────────────────────────────
        OutlinedTextField(
            value = if (weightKg == 0.0) "" else weightKg.toString(),
            onValueChange = { onMeasure(measurement.copy(width = it.toDoubleOrNull() ?: 0.0)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Weight (optional)") },
            placeholder = { Text("e.g. 70") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            trailingIcon = {
                Text(
                    "kg", style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(end = 8.dp)
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(10.dp)
        )

        // ── Waist (optional) ──────────────────────────────────────────────
        OutlinedTextField(
            value = if (waistCm == 0.0) "" else waistCm.toString(),
            onValueChange = { onMeasure(measurement.copy(depth = it.toDoubleOrNull() ?: 0.0)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Waist circumference (optional)") },
            placeholder = { Text("e.g. 80") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            trailingIcon = {
                Text(
                    "cm", style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(end = 8.dp)
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(10.dp)
        )

        measError?.let {
            Text(
                it, color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

