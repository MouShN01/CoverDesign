package com.example.coverdesign.ui.screens
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Straighten


import androidx.compose.material3.Icon
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ─── Color Palette ───────────────────────────────────────────────────────────
private val BackgroundColor   = Color(0xFFF5F4F0)
private val SurfaceColor      = Color(0xFFFFFFFF)
private val PrimaryColor      = Color(0xFF1A1A2E)
private val AccentColor       = Color(0xFF4F46E5)
private val AccentLight       = Color(0xFFEEEDFD)
private val TextPrimary       = Color(0xFF1A1A2E)
private val TextSecondary     = Color(0xFF6B7280)
private val BorderColor       = Color(0xFFE5E7EB)
private val ErrorColor        = Color(0xFFEF4444)

// ─── Data Models ─────────────────────────────────────────────────────────────
data class MeasurementField(
    val label: String,
    val unit: String = "mm",
    var value: String = ""
)

val textureMaterials = listOf(
    "PLA", "Metal", "Wood", "Plastic",
    "Carbon Fiber", "Epoxy Resin", "Gold-plated",
    "Voronoi", "Woven Basket", "Custom Texture"
)

private val function1: (Map<String, String>, String, String) -> Unit =
    { measurements, texture, description ->
        // This is the default lambda body
    }

private val function: (Map<String, String>, String, String) -> Unit =
    { measurements, texture, description ->
        // This is the default lambda body
    }

// ─── Main Screen ─────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductConfigScreen(
    onProceed: (measurements: Map<String, String>, texture: String, description: String) -> Unit = { measurements, texture, description ->
        // This is the default lambda body
    }
) {
    // State
    var height    by remember { mutableStateOf("") }
    var width     by remember { mutableStateOf("") }
    var depth     by remember { mutableStateOf("") }
    var isPrecise by remember { mutableStateOf(false) }

    var description      by remember { mutableStateOf("") }
    var selectedMaterial by remember { mutableStateOf(textureMaterials[0]) }
    var materialExpanded by remember { mutableStateOf(false) }
    var showTips         by remember { mutableStateOf(false) }

    // Validation
    val heightError = height.isNotEmpty() && height.toFloatOrNull() == null
    val widthError  = width.isNotEmpty()  && width.toFloatOrNull()  == null
    val depthError  = depth.isNotEmpty()  && depth.toFloatOrNull()  == null
    val canProceed  = height.isNotEmpty() && width.isNotEmpty() && depth.isNotEmpty()
            && !heightError && !widthError && !depthError

    Scaffold(
        containerColor = BackgroundColor,
        topBar = { ProductConfigTopBar() },
        bottomBar = {
            ProceedButton(
                enabled = canProceed,
                onClick = {
                    onProceed(
                        mapOf("height" to height, "width" to width, "depth" to depth),
                        selectedMaterial,
                        description
                    )
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // ── Measurements Section ──────────────────────────────────────────
            SectionCard(title = "Measurements") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    MeasurementInputField(
                        label = "Height",
                        value = height,
                        onValueChange = { height = it },
                        isError = heightError,
                        errorMessage = "Enter a valid number"
                    )
                    MeasurementInputField(
                        label = "Width",
                        value = width,
                        onValueChange = { width = it },
                        isError = widthError,
                        errorMessage = "Enter a valid number"
                    )
                    MeasurementInputField(
                        label = "Depth",
                        value = depth,
                        onValueChange = { depth = it },
                        isError = depthError,
                        errorMessage = "Enter a valid number"
                    )

                    // Precise toggle
                    Spacer(modifier = Modifier.height(4.dp))
                    PreciseModeToggle(
                        checked = isPrecise,
                        onCheckedChange = { isPrecise = it }
                    )
                }
            }

            // ── Texture Section ───────────────────────────────────────────────
            SectionCard(title = "Texture") {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = "Enter a description for the custom prosthetic cover",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        lineHeight = 18.sp
                    )

                    // Description + Chat/Tips row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Text area
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            modifier = Modifier
                                .weight(1f)
                                .height(110.dp),
                            placeholder = {
                                Text(
                                    "Describe your ideal texture…",
                                    color = TextSecondary,
                                    fontSize = 13.sp
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = BorderColor,
                                focusedBorderColor   = AccentColor,
                                cursorColor          = AccentColor,
                                focusedContainerColor   = SurfaceColor,
                                unfocusedContainerColor = SurfaceColor
                            ),
                            maxLines = 5
                        )

                        // Chat + Tips column
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.width(80.dp)
                        ) {
                            ActionChip(
                                label = "Chat",
                                icon  = Icons.Default.Chat,
                                onClick = { /* open chat */ }
                            )
                            ActionChip(
                                label = "Tips",
                                icon  = Icons.Default.Lightbulb,
                                onClick = { showTips = !showTips },
                                trailing = if (showTips) Icons.Default.ExpandLess else Icons.Default.ExpandMore
                            )
                        }
                    }

                    // Tips panel
                    AnimatedVisibility(
                        visible = showTips,
                        enter   = fadeIn() + expandVertically(),
                        exit    = fadeOut() + shrinkVertically()
                    ) {
                        TipsPanel()
                    }

                    // Material selector
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Choose your desired texture material / style",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                        ExposedDropdownMenuBox(
                            expanded = materialExpanded,
                            onExpandedChange = { materialExpanded = !materialExpanded }
                        ) {
                            OutlinedTextField(
                                value    = selectedMaterial,
                                onValueChange = {},
                                readOnly = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = materialExpanded)
                                },
                                shape  = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor    = BorderColor,
                                    focusedBorderColor      = AccentColor,
                                    focusedContainerColor   = SurfaceColor,
                                    unfocusedContainerColor = SurfaceColor
                                )
                            )
                            ExposedDropdownMenu(
                                expanded         = materialExpanded,
                                onDismissRequest = { materialExpanded = false },
                                modifier         = Modifier.background(SurfaceColor)
                            ) {
                                textureMaterials.forEach { material ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text       = material,
                                                fontWeight = if (material == selectedMaterial)
                                                    FontWeight.SemiBold else FontWeight.Normal,
                                                color      = if (material == selectedMaterial)
                                                    AccentColor else TextPrimary
                                            )
                                        },
                                        onClick = {
                                            selectedMaterial = material
                                            materialExpanded = false
                                        },
                                        leadingIcon = if (material == selectedMaterial) ({
                                            Icon(
                                                Icons.Default.Check,
                                                contentDescription = null,
                                                tint   = AccentColor,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }) else null
                                    )
                                }
                            }
                        }

                        // Material chips
                        MaterialChipRow(
                            materials = textureMaterials.take(5),
                            selected  = selectedMaterial,
                            onSelect  = { selectedMaterial = it }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

// ─── Top Bar ─────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductConfigTopBar() {
    TopAppBar(
        title = {
            Text(
                text       = "Configure Product",
                fontWeight = FontWeight.Bold,
                fontSize   = 22.sp,
                color      = TextPrimary
            )
        },
        navigationIcon = {
            IconButton(onClick = {}) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundColor)
    )
}

// ─── Section Card ─────────────────────────────────────────────────────────────
@Composable
fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column (
    ) {
        Text(
            text       = title,
            fontSize   = 20.sp,
            fontWeight = FontWeight.Bold,
            color      = TextPrimary,
            modifier   = Modifier.padding(bottom = 12.dp)
        )
        Surface(
            shape         = RoundedCornerShape(20.dp),
            color         = SurfaceColor,
            tonalElevation = 0.dp,
            modifier      = Modifier
                .fillMaxWidth()
                .border(1.dp, BorderColor, RoundedCornerShape(20.dp))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                content  = content
            )
        }
    }
}

// ─── Measurement Input ────────────────────────────────────────────────────────
@Composable
fun MeasurementInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    unit: String = "mm",
    isError: Boolean = false,
    errorMessage: String = ""
) {
    Row(
        modifier     = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text      = label,
            fontSize  = 14.sp,
            color     = TextSecondary,
            modifier  = Modifier.width(64.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            OutlinedTextField(
                value         = value,
                onValueChange = onValueChange,
                modifier      = Modifier.fillMaxWidth(),
                placeholder   = { Text("0", color = TextSecondary.copy(alpha = 0.5f)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine    = true,
                isError       = isError,
                shape         = RoundedCornerShape(10.dp),
                colors        = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor    = BorderColor,
                    focusedBorderColor      = AccentColor,
                    errorBorderColor        = ErrorColor,
                    cursorColor             = AccentColor,
                    focusedContainerColor   = SurfaceColor,
                    unfocusedContainerColor = SurfaceColor
                ),
                trailingIcon  = {
                    Text(unit, color = TextSecondary, fontSize = 12.sp,
                        modifier = Modifier.padding(end = 12.dp))
                }
            )
            if (isError && errorMessage.isNotEmpty()) {
                Text(
                    text     = errorMessage,
                    color    = ErrorColor,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                )
            }
        }
    }
}

// ─── Precise Mode Toggle ──────────────────────────────────────────────────────
@Composable
fun PreciseModeToggle(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Surface(
        shape  = RoundedCornerShape(12.dp),
        color  = if (checked) AccentLight else Color(0xFFF9FAFB),
        border = BorderStroke(1.dp, if (checked) AccentColor.copy(alpha = 0.4f) else BorderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier  = Modifier
                .clickable { onCheckedChange(!checked) }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Straighten,
                    contentDescription = null,
                    tint     = if (checked) AccentColor else TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text       = "Precise mode",
                        fontSize   = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color      = if (checked) AccentColor else TextPrimary
                    )
                    Text(
                        text     = "Enter exact measurements in μm",
                        fontSize = 11.sp,
                        color    = TextSecondary
                    )
                }
            }
            Switch(
                checked         = checked,
                onCheckedChange = onCheckedChange,
                colors          = SwitchDefaults.colors(
                    checkedThumbColor      = Color.White,
                    checkedTrackColor      = AccentColor,
                    uncheckedThumbColor    = Color.White,
                    uncheckedTrackColor    = BorderColor
                )
            )
        }
    }
}

// ─── Action Chip ─────────────────────────────────────────────────────────────
@Composable
fun ActionChip(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    trailing: ImageVector? = null
) {
    Surface(
        shape    = RoundedCornerShape(10.dp),
        color    = PrimaryColor,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(label, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            if (trailing != null) {
                Spacer(modifier = Modifier.width(2.dp))
                Icon(trailing, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
            }
        }
    }
}

// ─── Tips Panel ──────────────────────────────────────────────────────────────
@Composable
fun TipsPanel() {
    val tips = listOf(
        "Mention the limb region (forearm, leg, finger) for better fit",
        "Describe preferred rigidity — soft, semi-rigid, or hard shell",
        "Note any skin sensitivity or allergy to specific materials",
        "Reference textures you admire (scales, fabric, lattice, etc.)"
    )
    Surface(
        shape    = RoundedCornerShape(12.dp),
        color    = Color(0xFFFFFBEB),
        border   = BorderStroke(1.dp, Color(0xFFFBBF24).copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Lightbulb, contentDescription = null,
                    tint = Color(0xFFF59E0B), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Helpful Tips", fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp, color = Color(0xFF92400E))
            }
            tips.forEach { tip ->
                Row(verticalAlignment = Alignment.Top) {
                    Text("•", color = Color(0xFFF59E0B), fontSize = 13.sp,
                        modifier = Modifier.padding(end = 6.dp, top = 1.dp))
                    Text(tip, fontSize = 12.sp, color = Color(0xFF78350F), lineHeight = 17.sp)
                }
            }
        }
    }
}

// ─── Material Chip Row ────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialChipRow(
    materials: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        materials.forEach { material ->
            val isSelected = material == selected
            FilterChip(
                selected = isSelected,
                onClick  = { onSelect(material) },
                label    = { Text(material, fontSize = 12.sp) },
                colors   = FilterChipDefaults.filterChipColors(
                    selectedContainerColor    = AccentColor,
                    selectedLabelColor        = Color.White,
                    containerColor            = Color(0xFFF3F4F6),
                    labelColor                = TextSecondary
                ),
                border   = FilterChipDefaults.filterChipBorder(
                    enabled              = true,
                    selected             = isSelected,
                    selectedBorderColor  = AccentColor,
                    borderColor          = BorderColor,
                    selectedBorderWidth  = 1.5.dp,
                    borderWidth          = 1.dp
                )
            )
        }
    }
}

// ─── Proceed Button ───────────────────────────────────────────────────────────
@Composable
fun ProceedButton(enabled: Boolean, onClick: () -> Unit) {
    Surface(color = BackgroundColor) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Button(
                onClick  = onClick,
                enabled  = enabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape    = RoundedCornerShape(16.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor         = PrimaryColor,
                    contentColor           = Color.White,
                    disabledContainerColor = BorderColor,
                    disabledContentColor   = TextSecondary
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text       = "Proceed",
                        fontSize   = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    AnimatedVisibility(visible = enabled) {
                        Row {
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                Icons.Default.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─── Preview ──────────────────────────────────────────────────────────────────
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ProductConfigScreenPreview() {
    MaterialTheme {
        ProductConfigScreen()
    }
}
