package com.example.coverdesign.ui.screens.config

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.coverdesign.domain.model.Product
import com.example.coverdesign.ui.constants.UIConstants.DEFAULT_TEXTURES
import com.example.coverdesign.ui.screens.ArMode
import com.example.coverdesign.ui.screens.config.measurement_section.MeasurementSection
import com.example.coverdesign.ui.screens.resolveArMode
import com.example.coverdesign.ui.viewmodel.ProductConfigAction
import com.example.coverdesign.ui.viewmodel.ProductConfigUiState

@Composable
internal fun ProductConfigContent(
    state: ProductConfigUiState,
    onAction: (ProductConfigAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val config = state.currentConfig ?: return

    val arMode = config.orderCategory.resolveArMode(
        photoUri = config.userPhotoUri, measurements = config.measurement
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        state.selectedProduct?.let { product ->
            ProductHeader(product = product)
        }

        ArModeBadge(arMode = arMode)

        ConfigSection(title = "Measurements & Size") {
            MeasurementSection(
                category = config.orderCategory,
                measurement = config.measurement,
                sizeLabel = config.sizeLabel,
                measError = state.measurementError,
                onMeasure = {
                    onAction(ProductConfigAction.UpdateMeasurement(it, config.orderCategory))
                },
                onSize = { onAction(ProductConfigAction.UpdateSizeLabel(it)) }
            )
        }

        ConfigSection(title = "Appearance") {
            MaterialSelector(
                selected = config.material,
                allowedMaterials = allowedMaterials(config.orderCategory),
                onChanged = { onAction(ProductConfigAction.UpdateMaterial(it)) }
            )
            ColorPicker(
                selected = config.color,
                onChanged = { onAction(ProductConfigAction.UpdateColor(it)) }
            )
            TextureSelector(
                selected = config.texture,
                textures = DEFAULT_TEXTURES,
                onChanged = { onAction(ProductConfigAction.UpdateTexture(it)) }
            )
        }

        ConfigSection(title = "Personalisation") {
            InscriptionField(
                value = config.inscription,
                error = state.inscriptionError,
                onChanged = { onAction(ProductConfigAction.UpdateInscription(it)) }
            )
            AdditionalElementsSelector(
                category = config.orderCategory,
                selected = config.additionalElements,
                onAdd = { onAction(ProductConfigAction.AddElement(it)) },
                onRemove = { onAction(ProductConfigAction.RemoveElement(it)) }
            )
        }

        ConfigSection(title = "Fitting & Try-On") {
            PhotoUploadSection(
                currentUri = config.userPhotoUri,
                arMode = arMode,
                orderCategory = config.orderCategory,
                onChanged = { onAction(ProductConfigAction.UpdateUserPhoto(it)) }
            )
        }

        ConfigSection(title = "Order") {
            QuantitySelector(
                value = config.quantity,
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
    }
}

@SuppressLint("DefaultLocale")
@Composable
private fun ProductHeader(product: Product) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(80.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                if (product.imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = product.imageUrl,
                        contentDescription = product.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ViewInAr,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(text = product.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = product.category.name,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                if (product.description.isNotEmpty()) {
                    Text(
                        text = product.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }
                Text(
                    text = "Base price: $${String.format("%.2f", product.basePrice)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ArModeBadge(arMode: ArMode) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.tertiaryContainer
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = arMode.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "Try-on mode: ${arMode.label}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}