package com.example.coverdesign.ui.screens.patient.preview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.coverdesign.domain.model.ModelStatus


@Composable
fun ModelGeneratingIndicator(
    status               : ModelStatus,
    estimatedWaitSeconds : Int?
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier            = Modifier.padding(32.dp)
    ) {
        CircularProgressIndicator(
            modifier    = Modifier.size(48.dp),
            strokeWidth = 3.dp
        )
        Text(
            text  = when (status) {
                ModelStatus.PENDING    -> "Preparing your model…"
                ModelStatus.GENERATING -> "Generating 3D model…"
                else                     -> "Please wait…"
            },
            style     = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        // show estimated wait hint
        estimatedWaitSeconds?.let { secs ->
            if (secs > 0) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                ) {
                    Text(
                        text     = "Estimated wait: ~${secs}s",
                        style    = MaterialTheme.typography.labelSmall,
                        color    = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }
        }
        Text(
            text  = "You can leave this screen and come back later.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
