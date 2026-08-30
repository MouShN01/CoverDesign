package com.example.coverdesign.ui.screens.config

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

@Composable
fun PhotoUploadButton(
    currentUri : String?,
    hint       : String,
    onChanged  : (String?) -> Unit
) {
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? -> uri?.let { onChanged(it.toString()) } }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Reference Photo", style = MaterialTheme.typography.bodyMedium)
        Text(
            text  = hint,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (currentUri != null) {
            Box(modifier = Modifier.fillMaxWidth()) {
                AsyncImage(
                    model              = currentUri,
                    contentDescription = "Reference photo",
                    modifier           = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    contentScale       = ContentScale.Crop
                )
                // remove button
                IconButton(
                    onClick  = { onChanged(null) },
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Remove photo",
                            modifier           = Modifier.padding(4.dp).size(16.dp),
                            tint               = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        } else {
            OutlinedButton(
                onClick  = { launcher.launch("image/*") },
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Camera, null, Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Upload Reference Photo")
            }
        }
    }
}
