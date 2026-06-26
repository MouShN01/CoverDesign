package com.example.coverdesign.ui.screens.config

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.runtime.Composable
import com.example.coverdesign.domain.model.ProductCategory
import com.example.coverdesign.ui.screens.ArMode


@Composable
fun PhotoUploadSection(
    currentUri      : String?,
    arMode          : ArMode,
    productCategory : ProductCategory,
    onChanged       : (String?) -> Unit
) {
    when (arMode) {
        is ArMode.OnUserPhoto -> {
            val hint = when (productCategory) {
                ProductCategory.JEWELRY   ->
                    "Upload a photo of yourself wearing similar jewellery — helps position the 3D model accurately."
                ProductCategory.ACCESSORY ->
                    "Upload a photo showing where you will wear this accessory — used for visual fitting reference."
                else -> "Upload a reference photo for fitting."
            }
            PhotoUploadButton(
                currentUri = currentUri,
                hint       = hint,
                onChanged  = onChanged
            )
        }
        is ArMode.OnBodySilhouette -> {
            val message = when (productCategory) {
                ProductCategory.CLOTHING ->
                    "Your clothing item will be previewed on a body silhouette based on the measurements you entered above."
                ProductCategory.FOOTWEAR ->
                    "Your footwear will be shown on a body silhouette — enter foot measurements above for best fit."
                ProductCategory.THREE_D_PRINT ->
                    "Your custom 3D-printed item will be previewed on a body silhouette. Adjust measurements above as needed."
                else ->
                    "This item will be previewed on a body silhouette based on the measurements entered above."
            }
            ArModeInfoCard(
                icon    = Icons.Default.Person,
                title   = "Body Fitting Mode",
                message = message
            )
        }
        is ArMode.InRoomSpace -> {
            val message = when (productCategory) {
                ProductCategory.INTERIOR_DECOR ->
                    "This décor item will be placed in your real environment using the phone camera and ARCore plane detection."
                else ->
                    "This item will be placed in your real environment using the phone camera and ARCore plane detection."
            }
            ArModeInfoCard(
                icon    = Icons.Default.ViewInAr,
                title   = "Room AR Mode",
                message = message
            )
        }
    }
}

//@Composable
//fun PhotoUploadSection(
//    currentUri      : String?,
//    arMode          : ArMode,
//    productCategory : ProductCategory,
//    onChanged       : (String?) -> Unit
//) {
//    when (arMode) {
//        is ArMode.OnUserPhoto -> {
//            val hint = when (productCategory) {
//                ProductCategory.JEWELRY   ->
//                    "Upload a photo of yourself wearing similar jewellery — helps position the 3D model accurately."
//                ProductCategory.ACCESSORY ->
//                    "Upload a photo showing where you will wear this accessory — used for visual fitting reference."
//                else -> "Upload a reference photo for fitting."
//            }
//            PhotoUploadButton(
//                currentUri = currentUri,
//                hint       = hint,
//                onChanged  = onChanged
//            )
//        }
//        is ArMode.OnBodySilhouette -> {
//            val message = when (productCategory) {
//                ProductCategory.CLOTHING ->
//                    "Your clothing item will be previewed on a body silhouette based on the measurements you entered above."
//                ProductCategory.FOOTWEAR ->
//                    "Your footwear will be shown on a body silhouette — enter foot measurements above for best fit."
//                ProductCategory.THREE_D_PRINT ->
//                    "Your custom 3D-printed item will be previewed on a body silhouette. Adjust measurements above as needed."
//                else ->
//                    "This item will be previewed on a body silhouette based on the measurements entered above."
//            }
//            ArModeInfoCard(
//                icon    = Icons.Default.Person,
//                title   = "Body Fitting Mode",
//                message = message
//            )
//        }
//        is ArMode.InRoomSpace -> {
//            val message = when (productCategory) {
//                ProductCategory.INTERIOR_DECOR ->
//                    "This décor item will be placed in your real environment using the phone camera and ARCore plane detection."
//                else ->
//                    "This item will be placed in your real environment using the phone camera and ARCore plane detection."
//            }
//            ArModeInfoCard(
//                icon    = Icons.Default.ViewInAr,
//                title   = "Room AR Mode",
//                message = message
//            )
//        }
//    }
//}

