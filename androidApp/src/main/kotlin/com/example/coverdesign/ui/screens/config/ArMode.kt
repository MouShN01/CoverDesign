package com.example.coverdesign.ui.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.coverdesign.domain.model.Measurement
import com.example.coverdesign.domain.model.OrderCategory

sealed class ArMode {
    /** Prosthetic covers — overlay model on user's reference photo */
    data class OnUserPhoto(val photoUri: String?) : ArMode()

    /** Clothing — show on body silhouette using entered measurements */
    data class OnBodySilhouette(val measurements: Measurement) : ArMode()

    /** Accessory / room object — place in real environment via ARCore */
    object InRoomSpace : ArMode()

    val label: String
        get() = when (this) {
            is OnUserPhoto -> "Try on Photo"
            is OnBodySilhouette -> "Body Fitting"
            is InRoomSpace -> "Place in Room"
        }

    val icon: ImageVector
        get() = when (this) {
            is OnUserPhoto -> Icons.Default.Face
            is OnBodySilhouette -> Icons.Default.Person
            is InRoomSpace -> Icons.Default.Home
        }
}

/** Maps each ProductCategory to the correct AR mode */
fun OrderCategory.resolveArMode(photoUri: String?, measurements: Measurement): ArMode =
    when (this) {
        // Jewellery / accessories worn on the body — overlay on user photo
        OrderCategory.JEWELRY,
        OrderCategory.ACCESSORY -> ArMode.OnUserPhoto(photoUri)

        // Clothing / footwear — fit on body silhouette with measurements
//        ProductCategory.CLOTHING,
//        ProductCategory.FOOTWEAR       -> ArMode.OnBodySilhouette(measurements)
//
        OrderCategory.CLOTHING,
        OrderCategory.FOOTWEAR,
        OrderCategory.INSOLE -> ArMode.OnUserPhoto(photoUri)

        // 3D-print custom items — body silhouette by default
        OrderCategory.THREE_D_PRINT,
        OrderCategory.COSMETIC_COVER -> ArMode.InRoomSpace  // was OnBodySilhouette(measurements)

        // Interior decor — place in real room space
        OrderCategory.INTERIOR_DECOR -> ArMode.InRoomSpace

        // Fallback
        OrderCategory.OTHER -> ArMode.InRoomSpace
    }
