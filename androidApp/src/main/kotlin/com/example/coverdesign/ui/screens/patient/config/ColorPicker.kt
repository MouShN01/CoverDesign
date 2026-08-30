package com.example.coverdesign.ui.screens.config

import android.graphics.Color.parseColor
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.coverdesign.domain.model.ProductColor

@Composable
fun ColorPicker(selected: ProductColor, onChanged: (ProductColor) -> Unit) {
    val presets = listOf(
        ProductColor("Black",  "#000000"),
        ProductColor("Yellow", "#FFEF70"),
        ProductColor("Red",    "#FF0000"),
        ProductColor("Green",  "#00FF15"),
        ProductColor("Blue",   "#0000FF"),
        ProductColor("Custom", selected.hex)
    )
    var errorFlag: Boolean

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Color", style = MaterialTheme.typography.bodyMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            presets.forEach { color ->
                val hexColor = try {
                    val rawHex = color.hex.trim()
                    val formattedHex = when {
                        rawHex.isEmpty() -> "#000000"
                        rawHex.startsWith("#") -> rawHex
                        else -> "#$rawHex"
                    }
                    errorFlag = false
                    Color(parseColor(formattedHex))
                } catch (e: Exception) {
                    errorFlag = true
                    Color.Gray
                }
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(hexColor)
                        .border(
                            width = if (selected.hex == color.hex) 3.dp else 1.dp,
                            color = if (selected.hex == color.hex)
                                MaterialTheme.colorScheme.primary
                            else if (errorFlag) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.outline,
                            shape = CircleShape
                        )
                        .clickable { onChanged(color) }
                )
            }
        }
        OutlinedTextField(
            value         = selected.hex,
            onValueChange = { onChanged(selected.copy(hex = it)) },
            label         = { Text("Hex code") },
            modifier      = Modifier.fillMaxWidth(),
            singleLine    = true,
            shape         = RoundedCornerShape(10.dp),
            placeholder   = { Text("#FFAA00") }
        )
    }
}