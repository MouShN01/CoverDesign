package com.example.coverdesign.ui.screens.patient.preview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.coverdesign.domain.model.ModelStatus
import kotlinx.coroutines.delay


@Composable
fun GenerationProgressBar(
    status               : ModelStatus,
    estimatedWaitSeconds : Int?
) {
    // countdown from estimatedWaitSeconds — ticks every second
    var secondsLeft by remember(estimatedWaitSeconds) {
        mutableIntStateOf(estimatedWaitSeconds ?: 0)
    }

    LaunchedEffect(estimatedWaitSeconds) {
        if (estimatedWaitSeconds != null && estimatedWaitSeconds > 0) {
            while (secondsLeft > 0) {
                delay(1_000)
                secondsLeft--
            }
        }
    }

    Column {
        // thin indeterminate progress bar
        LinearProgressIndicator(
            modifier  = Modifier.fillMaxWidth(),
            color     = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
        // status + countdown label
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Text(
                text  = when (status) {
                    ModelStatus.PENDING    -> "Queued — preparing your model…"
                    ModelStatus.GENERATING -> "Generating 3D model…"
                    else                     -> "Processing…"
                },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (secondsLeft > 0) {
                Text(
                    text  = "~${secondsLeft}s",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}