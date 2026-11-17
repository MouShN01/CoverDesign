package com.example.coverdesign.ui.screens.patient.config

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.coverdesign.domain.model.pricing.PriceEstimate

@Composable
fun PriceCard(price: PriceEstimate) {
    Surface(
        shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Column(
            modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text("Price breakdown", style = MaterialTheme.typography.titleSmall)

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            PriceRow("Base price", price.basePrice)

            price.materialSurcharges.forEach { (name, amount) ->
                if (amount > 0.0) PriceRow("  Material ($name)", amount)
            }

            price.textureSurcharge?.let {
                if (it > 0.0) PriceRow(
                    "  Texture", price.textureSurcharge!!
                )
            }

            price.inscriptionSurcharge?.let {
                if (it > 0.0) PriceRow(
                    "  Inscription", price.inscriptionSurcharge!!
                )
            }

            price.elementSurcharges?.forEach { (name, amount) ->
                if (amount > 0.0) PriceRow(
                    "  ${
                        name.lowercase().replaceFirstChar { it.uppercase() }
                    }", amount
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))

            if (price.sizeMultiplier != 1.0 || !price.sizeMultiplierLabel.isNullOrEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Size (${price.sizeMultiplierLabel})",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        "×${String.format("%.2f", price.sizeMultiplier)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (price.quantity > 1) {
                PriceRow("Subtotal (1 unit)", price.subtotal)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Quantity", style = MaterialTheme.typography.bodySmall)
                    Text(
                        "×${price.quantity}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            PriceRow("Total", price.total, isTotal = true, currency = price.currency)
        }
    }
}

@Composable
private fun PriceRow(
    label: String, amount: Double, isTotal: Boolean = false, currency: String = "USD"
) {
    val symbol = when (currency) {
        "EUR" -> "€"; "GBP" -> "£"; else -> "$"
    }
    Row(
        modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label, style = if (isTotal) MaterialTheme.typography.titleSmall
            else MaterialTheme.typography.bodySmall
        )
        Text(
            "$symbol${String.format("%.2f", amount)}",
            style = if (isTotal) MaterialTheme.typography.titleSmall
            else MaterialTheme.typography.bodySmall,
            color = if (isTotal) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurface
        )
    }
}
