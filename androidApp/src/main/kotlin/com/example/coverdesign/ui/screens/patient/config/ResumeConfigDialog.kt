package com.example.coverdesign.ui.screens.patient.config

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.example.coverdesign.domain.model.ProductConfig

@Composable
internal fun ResumeConfigDialog(
    previousConfig: ProductConfig, onResume: () -> Unit, onStartFresh: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onStartFresh,
        title = { Text("Continue previous configuration?") },
        text = {
            Text(
                "You previously configured this product " + "(material: ${previousConfig.material.displayName}, " + "quantity: ${previousConfig.quantity}). " + "Continue editing or start fresh?"
            )
        },
        confirmButton = { TextButton(onClick = onResume) { Text("Continue") } },
        dismissButton = { TextButton(onClick = onStartFresh) { Text("Start fresh") } })
}
