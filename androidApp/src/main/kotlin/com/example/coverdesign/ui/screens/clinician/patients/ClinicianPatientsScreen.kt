package com.example.coverdesign.ui.screens.clinician.patients

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.coverdesign.ui.theme.AppTheme
import com.example.coverdesign.ui.theme.NeutralBackground
import com.example.coverdesign.ui.theme.NeutralChipBg
import com.example.coverdesign.ui.theme.NeutralLight
import com.example.coverdesign.ui.theme.NeutralText
import com.example.coverdesign.ui.theme.NeutralTextSecondary
import com.example.coverdesign.ui.theme.NeutralTextTertiary
import com.example.coverdesign.ui.theme.NeutralWhite
import com.example.coverdesign.ui.theme.PatientActiveTeal
import com.example.coverdesign.ui.theme.PatientActiveTealBorder
import com.example.coverdesign.ui.theme.PatientActiveTealLight
import com.example.coverdesign.ui.theme.PatientPendingAmber
import com.example.coverdesign.ui.theme.PatientPendingAmberBorder
import com.example.coverdesign.ui.theme.PatientPendingAmberLight
import com.example.coverdesign.ui.theme.PatientStableViolet
import com.example.coverdesign.ui.theme.PatientStableVioletBorder
import com.example.coverdesign.ui.theme.PatientStableVioletLight
import com.example.coverdesign.ui.viewmodel.ClinicianPatientsUiState
import com.example.coverdesign.ui.viewmodel.ClinicianPatientsViewModel
import com.example.coverdesign.ui.viewmodel.PatientFilter
import com.example.coverdesign.ui.viewmodel.PatientStatus
import com.example.coverdesign.ui.viewmodel.PatientSummary
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ClinicianPatientsScreen(
    viewModel: ClinicianPatientsViewModel = koinViewModel(),
    onBack: () -> Unit = {},
    onPatientClick: (String) -> Unit = {}
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ClinicianPatientsContent(
        state = state,
        onBack = onBack,
        onSearchChange = viewModel::onSearchChange,
        onFilterChange = viewModel::onFilterChange,
        onPatientClick = onPatientClick
    )
}

@Composable
internal fun ClinicianPatientsContent(
    state: ClinicianPatientsUiState,
    onBack: () -> Unit,
    onSearchChange: (String) -> Unit,
    onFilterChange: (PatientFilter) -> Unit,
    onPatientClick: (String) -> Unit
) {
    val patients = state.filteredPatients
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(NeutralBackground),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            PatientsHeader(
                total = state.patients.size,
                onBack = onBack
            )
        }

        item {
            SearchField(
                query = state.searchQuery,
                onQueryChange = onSearchChange
            )
        }

        item {
            FilterRow(
                selected = state.selectedFilter,
                countFor = state::count,
                onSelect = onFilterChange
            )
        }

        if (patients.isEmpty()) {
            item { EmptyState(hasPatients = state.patients.isNotEmpty()) }
        } else {
            items(patients, key = { it.id }) { patient ->
                PatientCard(
                    patient = patient,
                    onClick = { onPatientClick(patient.id) }
                )
            }
        }
    }
}

// ── Header ─────────────────────────────────────────────────────────────────────

@Composable
private fun PatientsHeader(total: Int, onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(NeutralWhite)
                .border(1.dp, NeutralLight, CircleShape)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = NeutralText,
                modifier = Modifier.size(16.dp)
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = "My Patients",
                style = MaterialTheme.typography.titleLarge,
                color = NeutralText
            )
            Text(
                text = "$total patient${if (total != 1) "s" else ""}",
                style = MaterialTheme.typography.bodySmall,
                color = NeutralTextTertiary
            )
        }
    }
}

// ── Search ───────────────────────────────────────────────────────────────────

@Composable
private fun SearchField(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = MaterialTheme.shapes.large,
        textStyle = MaterialTheme.typography.bodyMedium,
        placeholder = {
            Text(
                text = "Search by name or treatment",
                style = MaterialTheme.typography.bodyMedium,
                color = NeutralTextTertiary
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = null,
                tint = NeutralTextTertiary,
                modifier = Modifier.size(20.dp)
            )
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = NeutralWhite,
            unfocusedContainerColor = NeutralWhite,
            focusedBorderColor = PatientActiveTeal,
            unfocusedBorderColor = NeutralLight,
            cursorColor = PatientActiveTeal
        )
    )
}

// ── Filter chips ───────────────────────────────────────────────────────────────

@Composable
private fun FilterRow(
    selected: PatientFilter,
    countFor: (PatientFilter) -> Int,
    onSelect: (PatientFilter) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(PatientFilter.entries) { filter ->
            FilterChip(
                label = "${filter.label} (${countFor(filter)})",
                selected = filter == selected,
                onClick = { onSelect(filter) }
            )
        }
    }
}

@Composable
private fun FilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val background = if (selected) PatientActiveTeal else NeutralChipBg
    val textColor = if (selected) NeutralWhite else NeutralTextSecondary
    Box(
        modifier = Modifier
            .clip(MaterialTheme.shapes.extraLarge)
            .background(background)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = textColor
        )
    }
}

private val PatientFilter.label: String
    get() = when (this) {
        PatientFilter.All -> "All"
        PatientFilter.Active -> "Active"
        PatientFilter.Stable -> "Stable"
        PatientFilter.Pending -> "Pending"
    }

// ── Patient card ───────────────────────────────────────────────────────────────

@Composable
private fun PatientCard(patient: PatientSummary, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = NeutralWhite),
        border = BorderStroke(1.dp, NeutralLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AvatarCircle(initials = patient.initials(), size = 44)
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = patient.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = NeutralText
                    )
                    Text(
                        text = patient.treatmentType,
                        style = MaterialTheme.typography.bodySmall,
                        color = NeutralTextSecondary
                    )
                    Text(
                        text = "Visit: ${patient.lastVisitDate}",
                        style = MaterialTheme.typography.labelSmall,
                        color = NeutralTextTertiary
                    )
                }
                Spacer(Modifier.width(8.dp))
                StatusBadge(status = patient.status)
                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = null,
                    tint = NeutralTextTertiary,
                    modifier = Modifier.size(18.dp)
                )
            }

            if (patient.asymmetryCoeff != null || patient.kyphosisCoeff != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    patient.asymmetryCoeff?.let {
                        MetricColumn(label = "Asymmetry", value = "%.2f".format(it))
                    }
                    patient.kyphosisCoeff?.let {
                        MetricColumn(label = "Kyphosis", value = "%.1f°".format(it))
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricColumn(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = NeutralTextTertiary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = NeutralText
        )
    }
}

// ── Empty state ─────────────────────────────────────────────────────────────────

@Composable
private fun EmptyState(hasPatients: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = NeutralWhite),
        border = BorderStroke(1.dp, NeutralLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (hasPatients) "No patients match your filters" else "No patients assigned",
                style = MaterialTheme.typography.bodyMedium,
                color = NeutralTextTertiary
            )
        }
    }
}

// ── Shared components ─────────────────────────────────────────────────────────

@Composable
private fun AvatarCircle(initials: String, size: Int) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .background(NeutralTextTertiary, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = NeutralWhite
        )
    }
}

@Composable
private fun StatusBadge(status: PatientStatus) {
    val (bg, border, textColor, label) = when (status) {
        PatientStatus.Active -> StatusBadgeStyle(PatientActiveTealLight, PatientActiveTealBorder, PatientActiveTeal, "Active")
        PatientStatus.Stable -> StatusBadgeStyle(PatientStableVioletLight, PatientStableVioletBorder, PatientStableViolet, "Stable")
        PatientStatus.Pending -> StatusBadgeStyle(PatientPendingAmberLight, PatientPendingAmberBorder, PatientPendingAmber, "Pending")
    }
    Box(
        modifier = Modifier
            .clip(MaterialTheme.shapes.extraLarge)
            .background(bg)
            .border(1.dp, border, MaterialTheme.shapes.extraLarge)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = textColor
        )
    }
}

private data class StatusBadgeStyle(
    val background: Color,
    val border: Color,
    val text: Color,
    val label: String
)

private fun PatientSummary.initials(): String = name.split(" ")
    .take(2)
    .mapNotNull { it.firstOrNull()?.uppercaseChar() }
    .joinToString("")

// ── Preview ─────────────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
private fun ClinicianPatientsPreview() {
    val samplePatients = listOf(
        PatientSummary(
            id = "1", name = "Maria L.", lastVisitDate = "2 days ago",
            treatmentType = "Post-operative Rehab", status = PatientStatus.Active,
            asymmetryCoeff = 0.82f, kyphosisCoeff = 12.4f
        ),
        PatientSummary(
            id = "2", name = "Luca B.", lastVisitDate = "4 days ago",
            treatmentType = "Scoliosis Alignment", status = PatientStatus.Stable,
            asymmetryCoeff = 0.79f, kyphosisCoeff = 11.8f
        ),
        PatientSummary(
            id = "3", name = "Anna R.", lastVisitDate = "1 week ago",
            treatmentType = "Gait Instability", status = PatientStatus.Pending,
            asymmetryCoeff = 0.91f, kyphosisCoeff = 15.2f
        ),
        PatientSummary(
            id = "4", name = "Stefano T.", lastVisitDate = "5 days ago",
            treatmentType = "Kyphosis Correction", status = PatientStatus.Stable,
            asymmetryCoeff = 0.85f, kyphosisCoeff = 18.7f
        )
    )
    AppTheme {
        ClinicianPatientsContent(
            state = ClinicianPatientsUiState(
                doctorName = "Marco Rossi",
                patients = samplePatients
            ),
            onBack = {},
            onSearchChange = {},
            onFilterChange = {},
            onPatientClick = {}
        )
    }
}
