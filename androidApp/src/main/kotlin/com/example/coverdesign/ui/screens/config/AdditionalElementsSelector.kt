package com.example.coverdesign.ui.screens.config

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.coverdesign.domain.model.AdditionalElement
import com.example.coverdesign.domain.model.OrderCategory


@Composable
// Which AdditionalElements make sense for each ProductCategory.
// Uses only the elements that already exist in the enum — no new ones added.
private fun availableElements(category: OrderCategory): List<AdditionalElement>? =
    when (category) {

        OrderCategory.COSMETIC_COVER -> null
        OrderCategory.INSOLE -> null
        OrderCategory.CLOTHING -> listOf(
            AdditionalElement.LOGO,
            AdditionalElement.DECOR,
            AdditionalElement.STRAP // лямка
        )

        OrderCategory.FOOTWEAR -> listOf(
            AdditionalElement.LOGO,
            AdditionalElement.DECOR,
            AdditionalElement.STRAP,
            //AdditionalElement.PADDING,
            AdditionalElement.FASTENER
        )

        // Jewellery / Accessories — engraving and decorative finish are relevant;
        // mounting hardware for clasps / clips
        OrderCategory.JEWELRY -> listOf(
            AdditionalElement.ENGRAVING,
            AdditionalElement.DECOR,
            AdditionalElement.LOGO,
            AdditionalElement.FASTENER
        )

        OrderCategory.ACCESSORY -> listOf(
            AdditionalElement.LOGO,
            AdditionalElement.ENGRAVING,
            AdditionalElement.DECOR,
            AdditionalElement.MOUNTING,
            AdditionalElement.STRAP
        )

        // 3D Print — all structural and finish options are relevant
        OrderCategory.THREE_D_PRINT -> listOf(
            AdditionalElement.LOGO,
            AdditionalElement.ENGRAVING,
            AdditionalElement.DECOR,
            AdditionalElement.MOUNTING,
            AdditionalElement.PADDING
        )

        // Interior décor — mounting (wall / shelf) + decorative + logo (brand pieces)
        OrderCategory.INTERIOR_DECOR -> listOf(
            AdditionalElement.LOGO,
            AdditionalElement.DECOR,
            AdditionalElement.MOUNTING,
            AdditionalElement.ENGRAVING
        )

        // Other — show everything
        OrderCategory.OTHER -> AdditionalElement.values().toList()
    }


@Composable
fun AdditionalElementsSelector(
    category: OrderCategory,
    selected: List<AdditionalElement>,
    onAdd: (AdditionalElement) -> Unit,
    onRemove: (AdditionalElement) -> Unit
) {
    val available = availableElements(category)

    if (available.isNullOrEmpty()) return

    LaunchedEffect(category) {
        selected.filter { it !in available }.forEach { onRemove(it) }
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Additional elements", style = MaterialTheme.typography.bodyMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            available.forEach { element ->
                val isSelected = element in selected
                FilterChip(
                    selected = isSelected,
                    onClick = { if (isSelected) onRemove(element) else onAdd(element) },
                    label = { Text(element.displayName) },
                    leadingIcon = if (isSelected) ({
                        Icon(Icons.Default.Check, null, Modifier.size(16.dp))
                    }) else null
                )
            }
        }
    }
}