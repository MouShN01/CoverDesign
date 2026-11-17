package com.example.coverdesign.ui.screens.patient.config

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

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
