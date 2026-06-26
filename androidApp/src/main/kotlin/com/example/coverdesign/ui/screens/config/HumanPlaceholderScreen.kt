package com.example.coverdesign.ui.screens.config

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.coverdesign.domain.model.Measurement
import com.example.coverdesign.domain.model.ProductCategory
import com.example.coverdesign.ui.components.AppTopBar
import com.example.coverdesign.ui.screens.resolveArMode


// ═══════════════════════════════════════════════════════════════════
// HumanPlaceholderScreen
// Shown after 3D generation is triggered.  Displays a body silhouette
// with the relevant part highlighted and waits for the model to arrive
// from the server.  When ready, the user can proceed to live AR.
// ═══════════════════════════════════════════════════════════════════

@Composable
fun HumanPlaceholderScreen(
    productCategory : ProductCategory,
    isModelReady    : Boolean,
    onProceedToAr   : () -> Unit,
    onBack          : () -> Unit
) {
    Scaffold(
        topBar = { AppTopBar(title = "Fitting Preview", onBack = onBack) }
    ) { padding ->
        Column(
            modifier            = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(12.dp))

            // mode chip
            val arMode = productCategory.resolveArMode(null, Measurement(0.0, 0.0, 0.0))
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Row(
                    modifier              = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment     = CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(arMode.icon, null,
                        tint     = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(16.dp))
                    Text(
                        arMode.label,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // silhouette
            Box(
                modifier         = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                BodySilhouette(
                    category     = productCategory,
                    isModelReady = isModelReady,
                    modifier     = Modifier.fillMaxSize()
                )

                // loading overlay
                if (!isModelReady) {
                    Column(
                        modifier            = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier    = Modifier.size(28.dp),
                            strokeWidth = 2.5.dp
                        )
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                        ) {
                            Text(
                                "Generating 3D model…",
                                style    = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // body part label
            Text(
                text = when (productCategory) {
                    ProductCategory.JEWELRY        -> "Arms / neck — jewellery placement highlighted"
                    ProductCategory.ACCESSORY      -> "Arms — accessory placement highlighted"
                    ProductCategory.CLOTHING       -> "Full body — clothing fitting mode"
                    ProductCategory.THREE_D_PRINT  -> "Full body — 3D-print fitting mode"
                    ProductCategory.FOOTWEAR       -> "Lower legs — footwear fitting highlighted"
                    ProductCategory.INTERIOR_DECOR -> "Room placement mode — no body required"
                    ProductCategory.OTHER          -> "Room placement mode"
                },
                style     = MaterialTheme.typography.bodySmall,
                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(20.dp))

            // action buttons
            Column(
                modifier            = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick  = onProceedToAr,
                    enabled  = isModelReady,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape    = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.ViewInAr, null, Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (isModelReady) "Start Live Try-On"
                        else              "Waiting for 3D model…"
                    )
                }

                if (!isModelReady) {
                    OutlinedButton(
                        onClick  = onBack,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape    = RoundedCornerShape(14.dp)
                    ) { Text("Continue editing configuration") }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// Body silhouette — Compose-only (no SVG dependency needed)
// Highlights the relevant body segment for each product category.
// ═══════════════════════════════════════════════════════════════════

@Composable
private fun BodySilhouette(
    category     : ProductCategory,
    isModelReady : Boolean,
    modifier     : Modifier = Modifier
) {
    val bodyColor    = MaterialTheme.colorScheme.outlineVariant
    val highlightOn  = MaterialTheme.colorScheme.primary
    val highlightOff = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
    val accent       = if (isModelReady) highlightOn else highlightOff

    // Jewellery / accessories -> highlight arms (wrists, hands, neck area)
    fun isUpperLimb()  = category == ProductCategory.JEWELRY ||
            category == ProductCategory.ACCESSORY
    // Footwear -> highlight lower legs / feet
    fun isLowerLimb()  = category == ProductCategory.FOOTWEAR
    // Clothing / 3D-print -> highlight full torso
    fun isTorso()      = category == ProductCategory.CLOTHING ||
            category == ProductCategory.THREE_D_PRINT
    fun isFullBody()   = category == ProductCategory.CLOTHING ||
            category == ProductCategory.THREE_D_PRINT

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // Head
            Surface(
                modifier = Modifier.size(38.dp),
                shape    = CircleShape,
                color    = if (isTorso()) accent else bodyColor,
                border   = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline)
            ) {}

            Spacer(Modifier.height(2.dp))

            // Torso + arms row
            Row(
                verticalAlignment     = Alignment.Top,
                horizontalArrangement = Arrangement.Center
            ) {
                // left side — upper arm + forearm
                Column(horizontalAlignment = Alignment.End) {
                    Surface(   // upper arm
                        modifier = Modifier.width(20.dp).height(72.dp),
                        shape    = RoundedCornerShape(10.dp),
                        color    = if (isUpperLimb() || isFullBody()) accent else bodyColor,
                        border   = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {}
                    Spacer(Modifier.height(2.dp))
                    Surface(   // forearm + hand
                        modifier = Modifier.width(17.dp).height(58.dp),
                        shape    = RoundedCornerShape(9.dp),
                        color    = if (isUpperLimb()) accent else bodyColor,
                        border   = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {}
                }

                Spacer(Modifier.width(4.dp))

                // torso
                Surface(
                    modifier = Modifier.width(58.dp).height(120.dp),
                    shape    = RoundedCornerShape(
                        topStart    = 10.dp, topEnd    = 10.dp,
                        bottomStart = 6.dp,  bottomEnd = 6.dp
                    ),
                    color  = if (isTorso()) accent else bodyColor,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {}

                Spacer(Modifier.width(4.dp))

                // right side — upper arm + forearm
                Column(horizontalAlignment = Alignment.Start) {
                    Surface(   // upper arm
                        modifier = Modifier.width(20.dp).height(72.dp),
                        shape    = RoundedCornerShape(10.dp),
                        color    = if (isUpperLimb() || isFullBody()) accent else bodyColor,
                        border   = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {}
                    Spacer(Modifier.height(2.dp))
                    Surface(   // forearm + hand
                        modifier = Modifier.width(17.dp).height(58.dp),
                        shape    = RoundedCornerShape(9.dp),
                        color    = if (isUpperLimb()) accent else bodyColor,
                        border   = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {}
                }
            }

            // Legs
            Spacer(Modifier.height(2.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                repeat(2) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(   // thigh
                            modifier = Modifier.width(28.dp).height(80.dp),
                            shape    = RoundedCornerShape(9.dp),
                            color    = if (isLowerLimb() || isFullBody()) accent else bodyColor,
                            border   = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {}
                        Spacer(Modifier.height(2.dp))
                        Surface(   // lower leg
                            modifier = Modifier.width(23.dp).height(70.dp),
                            shape    = RoundedCornerShape(8.dp),
                            color    = if (isLowerLimb()) accent else bodyColor,
                            border   = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {}
                    }
                }
            }
        }

        // 3D model-ready indicator on the highlighted zone
        if (isModelReady) {
            val alignment = when {
                isUpperLimb() -> Alignment.CenterEnd
                isLowerLimb() -> Alignment.BottomCenter
                else          -> Alignment.Center
            }
            Surface(
                modifier = Modifier
                    .align(alignment)
                    .padding(6.dp)
                    .size(44.dp),
                shape  = CircleShape,
                color  = highlightOn.copy(alpha = 0.12f),
                border = BorderStroke(2.dp, highlightOn)
            ) {
                Icon(
                    imageVector        = Icons.Default.ViewInAr,
                    contentDescription = "3D model ready",
                    tint               = highlightOn,
                    modifier           = Modifier.fillMaxSize().padding(10.dp)
                )
            }
        }
    }
}