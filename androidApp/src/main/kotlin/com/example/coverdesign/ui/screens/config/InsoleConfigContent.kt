package com.example.coverdesign.ui.screens.config

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.coverdesign.domain.model.Measurement
import com.example.coverdesign.domain.model.OrderCategory
import com.example.coverdesign.ui.screens.config.measurement_section.FootwearSizeSection
import com.example.coverdesign.ui.viewmodel.InsoleFootConfig
import com.example.coverdesign.ui.viewmodel.LegSide
import com.example.coverdesign.ui.viewmodel.ProductConfigAction
import com.example.coverdesign.ui.viewmodel.ProductConfigUiState

@Composable
internal fun InsoleConfigContent(
    state: ProductConfigUiState,
    onAction: (ProductConfigAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val ic = state.insoleConfig
    val secondFoot = ic.secondFoot

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        ConfigSection(title = "Primary Foot") {
            LegSideSelector(
                selected = ic.primaryFoot.side,
                onSelected = { side ->
                    onAction(ProductConfigAction.UpdateInsolePrimaryFoot(ic.primaryFoot.copy(side = side)))
                }
            )
            FootwearSizeSection(
                measurement = ic.primaryFoot.toMeasurement(),
                sizeLabel = null,
                error = null,
                onMeasure = { m ->
                    onAction(ProductConfigAction.UpdateInsolePrimaryFoot(
                        ic.primaryFoot.copy(
                            lengthMm = m.height.toFloat(),
                            widthMm = m.width.toFloat(),
                            euSize = m.depth.toInt()
                        )
                    ))
                },
                onSize = {}
            )
        }

        if (secondFoot != null) {
            ConfigSection(title = "Second Foot") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LegSideSelector(
                        selected = secondFoot.side,
                        onSelected = { side ->
                            onAction(ProductConfigAction.UpdateInsoleSecondFoot(secondFoot.copy(side = side)))
                        }
                    )
                    IconButton(
                        onClick = { onAction(ProductConfigAction.RemoveInsoleSecondFoot) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove second foot",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                FootwearSizeSection(
                    measurement = secondFoot.toMeasurement(),
                    sizeLabel = null,
                    error = null,
                    onMeasure = { m ->
                        onAction(ProductConfigAction.UpdateInsoleSecondFoot(
                            secondFoot.copy(
                                lengthMm = m.height.toFloat(),
                                widthMm = m.width.toFloat(),
                                euSize = m.depth.toInt()
                            )
                        ))
                    },
                    onSize = {}
                )
            }
        } else {
            OutlinedButton(
                onClick = { onAction(ProductConfigAction.AddInsoleSecondFoot) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("+ Add other foot measurements")
            }
        }

        ConfigSection(title = "Appearance") {
            MaterialSelector(
                selected = ic.material,
                allowedMaterials = allowedMaterials(OrderCategory.INSOLE),
                onChanged = { onAction(ProductConfigAction.UpdateInsoleMaterial(it)) }
            )
            ColorPicker(
                selected = ic.color,
                onChanged = { onAction(ProductConfigAction.UpdateInsoleColor(it)) }
            )
        }


        ConfigSection(title = "Order") {
            QuantitySelector(
                value = ic.quantity,
                error = state.quantityError,
                onChanged = { onAction(ProductConfigAction.UpdateQuantity(it)) }
            )
        }

        state.priceEstimate?.let { PriceCard(price = it) }
        OutlinedButton(
            onClick = { onAction(ProductConfigAction.RequestPriceUpdate) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isPriceLoading
        ) {
            if (state.isPriceLoading) CircularProgressIndicator(Modifier.size(16.dp))
            else Text("Calculate Price")
        }

        Spacer(Modifier.height(80.dp))

//        ConfigSection(title = "Order") { //TODO add price estimate base on PricingEngine.kt
//            Text(
//                text = "Quantity: ${ic.quantity}",
//                style = MaterialTheme.typography.bodyMedium
//            )
//            if (ic.secondFoot != null) {
//                Text(
//                    text = "Includes both feet (×2)",
//                    style = MaterialTheme.typography.labelSmall,
//                    color = MaterialTheme.colorScheme.onSurfaceVariant
//                )
//            }
//        }
//
//        Spacer(Modifier.height(80.dp))
    }
}

@Composable
private fun LegSideSelector(
    selected: LegSide,
    onSelected: (LegSide) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        LegSide.entries.forEach { side ->
            FilterChip(
                selected = selected == side,
                onClick = { onSelected(side) },
                label = { Text(side.displayName) }
            )
        }
    }
}

private fun InsoleFootConfig.toMeasurement() = Measurement(
    height = lengthMm.toDouble(),
    width = widthMm.toDouble(),
    depth = euSize.toDouble()
)