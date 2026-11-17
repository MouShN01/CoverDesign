package com.example.coverdesign.ui.screens.patient.config

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun InscriptionField(value: String, error: String?, onChanged: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = { if (it.length <= 50) onChanged(it) },
        label = { Text("Inscription") },
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("Name, phrase, initials…") },
        isError = error != null,
        supportingText = {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(error ?: "", color = MaterialTheme.colorScheme.error)
                Text("${value.length}/50", style = MaterialTheme.typography.labelSmall)
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(10.dp)
    )
}
