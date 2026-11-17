package com.example.coverdesign.ui.screens.patient.config

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.coverdesign.domain.model.Material
import com.example.coverdesign.domain.model.OrderCategory

/**
 * Materials allowed for a given order category.
 */
internal fun allowedMaterials(category: OrderCategory): List<Material> = when (category) {
    OrderCategory.INSOLE -> listOf(
        Material.TPU, Material.SILICONE, Material.RUBBER, Material.NYLON, Material.LEATHER
    )

    OrderCategory.PROSTHETIC_COVER -> listOf(
        Material.PLA, Material.ABS, Material.NYLON, Material.PETG, Material.RESIN, Material.TPU
    )

    OrderCategory.CLOTHING -> listOf(
        Material.COTTON, Material.POLYESTER, Material.DENIM, Material.LEATHER, Material.NYLON
    )

    OrderCategory.FOOTWEAR -> listOf(
        Material.LEATHER, Material.RUBBER, Material.NYLON
    )

    OrderCategory.JEWELRY -> listOf(
        Material.STAINLESS_STEEL,
        Material.TITANIUM,
        Material.SILVER,
        Material.GOLD,
        Material.EPOXY_RESIN,
        Material.PLASTIC
    )

    OrderCategory.ACCESSORY -> listOf(
        Material.LEATHER,
        Material.NYLON,
        Material.PLASTIC,
        Material.STAINLESS_STEEL,
        Material.EPOXY_RESIN,
        Material.SILICONE
    )

    OrderCategory.THREE_D_PRINT -> listOf(
        Material.PLA, Material.ABS, Material.NYLON, Material.PETG, Material.RESIN, Material.TPU
    )

    OrderCategory.INTERIOR_DECOR -> listOf(
        Material.WOOD,
        Material.PLASTIC,
        Material.EPOXY_RESIN,
        Material.METAL,
        Material.CERAMIC,
        Material.GLASS
    )

    else -> Material.values().toList()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MaterialSelector(
    selected: Material,
    allowedMaterials: List<Material>,
    readOnly: Boolean = false,
    onChanged: (Material) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(allowedMaterials) {
        if (selected !in allowedMaterials && allowedMaterials.isNotEmpty()) {
            onChanged(allowedMaterials.first())
        }
    }

    ExposedDropdownMenuBox(
        expanded = expanded, onExpandedChange = {
            if (!readOnly) {
                expanded = !expanded
            }
        }) {
        OutlinedTextField(
            value = selected.displayName,
            onValueChange = {},
            readOnly = true,
            label = { Text("Material") },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            shape = RoundedCornerShape(10.dp)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            allowedMaterials.forEach { material ->
                DropdownMenuItem(
                    text = { Text(material.displayName) },
                    onClick = { onChanged(material); expanded = false },
                    leadingIcon = if (material == selected) ({
                        Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary)
                    }) else null
                )
            }
        }
    }
}
