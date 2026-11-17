package com.example.coverdesign.ui.screens.patient.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.coverdesign.ui.components.AppTopBar
import com.example.coverdesign.ui.theme.AppTheme

private const val TEST_MODEL_PATH = "test_models/damaged_helmet.glb"

@Composable
fun ModelTestScreen(
    onTryOn : () -> Unit = {},
    onOrder : () -> Unit = {},
    onBack  : () -> Unit = {}
) {
    Scaffold(
        topBar = { AppTopBar(title = "3D Preview", onBack = onBack) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                ModelViewerComposable(
                    modelUrl = TEST_MODEL_PATH,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
//                Button(
//                    onClick  = onTryOn,
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .height(52.dp),
//                    shape = RoundedCornerShape(14.dp)
//                ) {
//                    Icon(Icons.Default.ViewInAr, null, Modifier.size(20.dp))
//                    Spacer(Modifier.width(8.dp))
//                    Text("Try On (AR)")
//                }
                OutlinedButton(
                    onClick  = onOrder,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.ShoppingCart, null, Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Proceed to Order")
                }
            }
        }
    }
}

@Preview
@Composable
private fun ModelTestScreenPreview() {
    AppTheme { ModelTestScreen() }
}
