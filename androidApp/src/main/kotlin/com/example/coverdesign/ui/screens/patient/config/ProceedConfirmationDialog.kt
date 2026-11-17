package com.example.coverdesign.ui.screens.patient.config

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.coverdesign.domain.model.ProductConfig


@Composable
fun ProceedConfirmationDialog(
    config   : ProductConfig,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon  = { Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.primary) },
        title = { Text("Ready to generate your 3D model?") },
        text  = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "Generating a personalised 3D model typically takes 2–5 minutes. " +
                            "Please make sure you are happy with all the options you selected before continuing.",
                    style = MaterialTheme.typography.bodySmall
                )
                HorizontalDivider()
                // quick summary of key choices
                SummaryRow("Material",  config.material.displayName)
                SummaryRow("Size",      config.sizeLabel?.name ?: "Custom")
                if (config.inscription.isNotBlank())
                    SummaryRow("Inscription", config.inscription)
                if (config.additionalElements.isNotEmpty())
                    SummaryRow("Extras", config.additionalElements.joinToString { it.displayName })
                HorizontalDivider()
                Text(
                    "You can still edit your configuration after previewing the model.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) { Text("Generate 3D Model") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Review options") }
        }
    )
}


@Composable
private fun SummaryRow(label: String, value: String) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodySmall)
    }
}