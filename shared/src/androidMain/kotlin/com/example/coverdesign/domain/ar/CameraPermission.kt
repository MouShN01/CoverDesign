package com.example.coverdesign.domain.ar
//
//// androidApp/ui/screens/ar/CameraPermission.kt
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.size
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.CameraAlt
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.unit.dp
//import com.google.accompanist.permissions.*
//
//@Composable
//fun WithCameraPermission(
//    onGranted : @Composable () -> Unit
//) {
//    val permission = rememberPermissionState(
//        android.Manifest.permission.CAMERA
//    )
//
//    when {
//        permission.status.isGranted -> onGranted()
//
//        permission.status.shouldShowRationale -> {
//            // user denied once — explain why we need camera
//            Column(
//                modifier            = Modifier.fillMaxSize().padding(24.dp),
//                horizontalAlignment = Alignment.CenterHorizontally,
//                verticalArrangement = Arrangement.Center
//            ) {
//                Icon(
//                    Icons.Default.CameraAlt, null,
//                    modifier = Modifier.size(48.dp),
//                    tint     = MaterialTheme.colorScheme.primary)
//                Spacer(Modifier.height(16.dp))
//                Text(
//                    "Camera access is required for AR try-on",
//                    style     = MaterialTheme.typography.bodyLarge,
//                    textAlign = TextAlign.Center
//                )
//                Spacer(Modifier.height(16.dp))
//                Button(onClick = { permission.launchPermissionRequest() }) {
//                    Text("Grant Permission")
//                }
//            }
//        }
//
//        else -> {
//            // first time — request immediately
//            LaunchedEffect(Unit) { permission.launchPermissionRequest() }
//        }
//    }
//}