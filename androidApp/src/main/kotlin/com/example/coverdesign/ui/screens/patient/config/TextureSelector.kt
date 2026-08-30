package com.example.coverdesign.ui.screens.config

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.coverdesign.domain.model.Texture
import com.example.coverdesign.domain.util.generateUuid


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextureSelector(
    selected  : Texture,
    onChanged : (Texture) -> Unit,
    textures : List<Texture>
) {
    val texId = "tex-${generateUuid()}"
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {

            // switch to custom-image texture — name becomes "Image texture"
            onChanged(
                Texture(
                    id          = texId,
                    name        = "Image texture",
                    previewUrl  = it.toString(),
                    description = "User-provided texture image"
                )
            )
        }
    }

    // Is the current selection one of the presets or a custom image?
    val isCustomImage = selected.id == texId || selected.previewUrl != null && selected.id !in textures.map { it.id }

    var dropdownExpanded by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

        // ── dropdown row + paperclip ──────────────────────────────────────
        ExposedDropdownMenuBox(
            expanded        = dropdownExpanded,
            onExpandedChange = { dropdownExpanded = !dropdownExpanded }
        ) {
            OutlinedTextField(
                value         = selected.name,
                onValueChange = {},
                readOnly      = true,
                label         = { Text("Texture") },
                modifier      = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                trailingIcon  = {
                    // paperclip sits where the dropdown arrow normally would be
                    Row(verticalAlignment = CenterVertically) {
                        // paperclip
                        IconButton(
                            onClick = { launcher.launch("image/*") },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector        = Icons.Default.AttachFile,
                                contentDescription = "Attach texture image",
                                tint               = if (isCustomImage)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        // standard dropdown arrow
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded)
                    }
                },
                shape = RoundedCornerShape(10.dp)
            )

            ExposedDropdownMenu(
                expanded        = dropdownExpanded,
                onDismissRequest = { dropdownExpanded = false }
            ) {
                textures.forEach { texture ->
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(texture.name, style = MaterialTheme.typography.bodyMedium)
                                texture.description?.let {
                                    Text(it, style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        },
                        onClick = {
                            onChanged(texture)
                            dropdownExpanded = false
                        },
                        leadingIcon = if (selected.id == texture.id) ({
                            Icon(Icons.Default.Check, null,
                                tint = MaterialTheme.colorScheme.primary)
                        }) else null
                    )
                }
            }
        }

        // ── description of the selected preset ───────────────────────────
        if (!isCustomImage) {
            selected.description?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // ── custom image preview ──────────────────────────────────────────
        if (isCustomImage && !selected.previewUrl.isNullOrEmpty()) {
            Box(modifier = Modifier.fillMaxWidth()) {
                AsyncImage(
                    model              = selected.previewUrl,
                    contentDescription = "Texture preview",
                    modifier           = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    contentScale       = ContentScale.Crop
                )
                // clear — revert to first default texture
                IconButton(
                    onClick  = { onChanged(textures.first()) },
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Remove texture image",
                            modifier           = Modifier.padding(4.dp).size(14.dp),
                            tint               = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TextureSelectorV0(
    selected        : Texture,
    textureImageUri : String?,
    onChanged       : (Texture) -> Unit,
    onImageAttached : (String?) -> Unit
) {
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? -> uri?.let { onImageAttached(it.toString()) } }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // ── label row with paperclip ──────────────────────────────────────
        Row(
            modifier          = Modifier.fillMaxWidth(),
            verticalAlignment = CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Texture", style = MaterialTheme.typography.bodyMedium)
                Text(
                    selected.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            // paperclip — attach a texture reference image
            IconButton(onClick = { launcher.launch("image/*") }) {
                Icon(
                    imageVector        = Icons.Default.AttachFile,
                    contentDescription = "Attach texture image",
                    tint               = if (textureImageUri != null)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // ── preview of attached image ─────────────────────────────────────
        if (textureImageUri != null) {
            Box(modifier = Modifier.fillMaxWidth()) {
                AsyncImage(
                    model              = textureImageUri,
                    contentDescription = "Texture reference",
                    modifier           = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    contentScale       = ContentScale.Crop
                )
                // remove button
                IconButton(
                    onClick  = { onImageAttached(null) },
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Remove texture image",
                            modifier           = Modifier.padding(4.dp).size(14.dp),
                            tint               = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}