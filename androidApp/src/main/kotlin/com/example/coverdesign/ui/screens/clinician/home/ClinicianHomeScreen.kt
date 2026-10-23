package com.example.coverdesign.ui.screens.clinician.home

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.coverdesign.ui.theme.AppTheme
import com.example.coverdesign.ui.theme.NeutralBackground
import com.example.coverdesign.ui.theme.NeutralLight
import com.example.coverdesign.ui.theme.NeutralText
import com.example.coverdesign.ui.theme.NeutralTextSecondary
import com.example.coverdesign.ui.theme.NeutralTextTertiary
import com.example.coverdesign.ui.theme.NeutralWhite
import com.example.coverdesign.ui.theme.PatientActiveTeal
import com.example.coverdesign.ui.theme.PatientActiveTealBorder
import com.example.coverdesign.ui.theme.PatientActiveTealLight
import com.example.coverdesign.ui.theme.PatientPendingAlert
import com.example.coverdesign.ui.theme.PatientPendingAlertLight
import com.example.coverdesign.ui.theme.PatientPendingAmber
import com.example.coverdesign.ui.theme.PatientPendingAmberBorder
import com.example.coverdesign.ui.theme.PatientPendingAmberLight
import com.example.coverdesign.ui.theme.PatientStableViolet
import com.example.coverdesign.ui.theme.PatientStableVioletBorder
import com.example.coverdesign.ui.theme.PatientStableVioletLight
import com.example.coverdesign.ui.viewmodel.ClinicianHomeUiState
import com.example.coverdesign.ui.viewmodel.ClinicianHomeViewModel
import com.example.coverdesign.ui.viewmodel.PatientStatus
import com.example.coverdesign.ui.viewmodel.PatientSummary
import org.koin.compose.viewmodel.koinViewModel


@Composable
fun ClinicianHomeScreen(
    viewModel: ClinicianHomeViewModel = koinViewModel(),
    onPatientClick: (String) -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onViewAllPatientsClick: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ClinicianHomeContent(
        state = state,
        onPatientClick = onPatientClick,
        onNotificationsClick = onNotificationsClick,
        onViewAllPatientsClick = onViewAllPatientsClick
    )
}

@Composable
internal fun ClinicianHomeContent(
    state: ClinicianHomeUiState,
    onPatientClick: (String) -> Unit,
    onNotificationsClick: () -> Unit = {},
    onViewAllPatientsClick: () -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(NeutralBackground),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            TopBar(
                doctorName = state.doctorName.ifBlank { "Doctor" },
                notificationCount = state.notificationCount,
                onNotificationsClick = onNotificationsClick
            )
        }

        if (state.notifications.isNotEmpty()) {
            item {
                NotificationBanner(notifications = state.notifications)
            }
        }

        item {
            RecentPatientsSection(
                patients = state.recentPatients,
                onPatientClick = onPatientClick,
                onViewAllClick = onViewAllPatientsClick
            )
        }

        item {
            MyPatientsSection(
                patients = state.allPatients,
                onPatientClick = onPatientClick
            )
        }
    }
}

// ── Top Bar ───────────────────────────────────────────────────────────────────

@Composable
private fun TopBar(doctorName: String, notificationCount: Int, onNotificationsClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AvatarCircle(
                initials = doctorName.split(" ")
                    .take(2)
                    .mapNotNull { it.firstOrNull()?.uppercaseChar() }
                    .joinToString(""),
                size = 48,
                backgroundColor = PatientActiveTeal
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Welcome back",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = NeutralTextSecondary
                )
                Text(
                    text = "Dr. ${doctorName.split(" ").lastOrNull() ?: doctorName}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeutralText
                )
            }
        }

        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(NeutralWhite)
                .border(1.dp, NeutralLight, CircleShape)
                .clickable(onClick = onNotificationsClick),
            contentAlignment = Alignment.Center
        ) {
            BadgedBox(
                badge = {
                    if (notificationCount > 0) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(PatientPendingAlert, CircleShape)
                        )
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Filled.Notifications,
                    contentDescription = "Notifications",
                    tint = NeutralText,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

// ── Notification Banner ────────────────────────────────────────────────────────

@Composable
private fun NotificationBanner(notifications: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NeutralWhite),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, PatientPendingAlertLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(PatientPendingAlertLight, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = null,
                        tint = PatientPendingAlert,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Text(
                    text = "${notifications.size} patient${if (notifications.size != 1) "s" else ""} updated their records",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeutralText
                )
            }

            Column(
                modifier = Modifier.padding(start = 32.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                notifications.forEach { notification ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .background(PatientPendingAlert, CircleShape)
                        )
                        Text(
                            text = notification,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = NeutralText
                        )
                    }
                }
            }
        }
    }
}

// ── Recent Patients Section ────────────────────────────────────────────────────

@Composable
private fun RecentPatientsSection(
    patients: List<PatientSummary>,
    onPatientClick: (String) -> Unit,
    onViewAllClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recent Patient Data",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = NeutralText
            )
            TextButton(onClick = onViewAllClick) {
                Text(
                    text = "View All",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PatientActiveTeal
                )
            }
        }

        if (patients.isEmpty()) {
            EmptyRecentCard()
        } else {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(patients) { patient ->
                    PatientSummaryCard(patient = patient, onClick = { onPatientClick(patient.id) })
                }
            }
        }
    }
}

@Composable
private fun EmptyRecentCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NeutralWhite),
        border = androidx.compose.foundation.BorderStroke(1.dp, NeutralLight)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No recent patient data",
                color = NeutralTextTertiary,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun PatientSummaryCard(patient: PatientSummary, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(260.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NeutralWhite),
        border = androidx.compose.foundation.BorderStroke(1.dp, NeutralLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // name + visit row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AvatarCircle(
                        initials = patient.name.split(" ")
                            .take(2)
                            .mapNotNull { it.firstOrNull()?.uppercaseChar() }
                            .joinToString(""),
                        size = 36,
                        backgroundColor = NeutralTextTertiary
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                        Text(
                            text = patient.name,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeutralText
                        )
                        Text(
                            text = "Visit: ${patient.lastVisitDate}",
                            fontSize = 11.sp,
                            color = NeutralTextTertiary
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = null,
                    tint = NeutralTextTertiary,
                    modifier = Modifier.size(16.dp)
                )
            }

            // metrics
            Column(
                modifier = Modifier.padding(top = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                patient.asymmetryCoeff?.let { coeff ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Asymmetry Coeff.", fontSize = 12.sp, color = NeutralTextSecondary)
                        Text(
                            text = "%.2f".format(coeff),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeutralText
                        )
                    }
                }
                patient.kyphosisCoeff?.let { coeff ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Kyphosis Coeff.", fontSize = 12.sp, color = NeutralTextSecondary)
                        Text(
                            text = "%.1f°".format(coeff),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeutralText
                        )
                    }
                }
            }

            // status chip
            StatusChip(status = patient.status, label = patient.latestUpdate ?: patient.status.chipLabel())
        }
    }
}

// ── My Patients Section ────────────────────────────────────────────────────────

@Composable
private fun MyPatientsSection(
    patients: List<PatientSummary>,
    onPatientClick: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "My Patients",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = NeutralText
        )

        if (patients.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NeutralWhite),
                border = androidx.compose.foundation.BorderStroke(1.dp, NeutralLight)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "No patients assigned", color = NeutralTextTertiary, fontSize = 13.sp)
                }
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NeutralWhite),
                border = androidx.compose.foundation.BorderStroke(1.dp, NeutralLight),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column {
                    patients.forEachIndexed { index, patient ->
                        PatientListRow(
                            patient = patient,
                            onClick = { onPatientClick(patient.id) }
                        )
                        if (index < patients.lastIndex) {
                            HorizontalDivider(color = NeutralLight)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PatientListRow(patient: PatientSummary, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AvatarCircle(
                initials = patient.name.split(" ")
                    .take(2)
                    .mapNotNull { it.firstOrNull()?.uppercaseChar() }
                    .joinToString(""),
                size = 40,
                backgroundColor = NeutralTextTertiary
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = patient.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeutralText
                )
                Text(
                    text = patient.treatmentType,
                    fontSize = 12.sp,
                    color = NeutralTextSecondary
                )
            }
        }
        Spacer(Modifier.width(8.dp))
        PatientBadge(status = patient.status, text = patient.treatmentType.abbreviate())
    }
}

// ── Shared UI components ───────────────────────────────────────────────────────

@Composable
private fun AvatarCircle(initials: String, size: Int, backgroundColor: Color) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .background(backgroundColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            color = NeutralWhite,
            fontSize = (size / 3).sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun StatusChip(status: PatientStatus, label: String) {
    val (bg, text) = when (status) {
        PatientStatus.Active -> PatientActiveTealLight to PatientActiveTeal
        PatientStatus.Stable -> PatientStableVioletLight to PatientStableViolet
        PatientStatus.Pending -> PatientPendingAmberLight to PatientPendingAmber
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bg, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(text, CircleShape)
        )
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = text
        )
    }
}

@Composable
private fun PatientBadge(status: PatientStatus, text: String) {
    val (bg, border, textColor) = when (status) {
        PatientStatus.Active -> Triple(PatientActiveTealLight, PatientActiveTealBorder, PatientActiveTeal)
        PatientStatus.Stable -> Triple(PatientStableVioletLight, PatientStableVioletBorder, PatientStableViolet)
        PatientStatus.Pending -> Triple(PatientPendingAmberLight, PatientPendingAmberBorder, PatientPendingAmber)
    }
    Box(
        modifier = Modifier
            .background(bg, RoundedCornerShape(20.dp))
            .border(1.dp, border, RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = textColor
        )
    }
}

// ── Helpers ────────────────────────────────────────────────────────────────────

private fun PatientStatus.chipLabel() = when (this) {
    PatientStatus.Active -> "Active Rehab"
    PatientStatus.Stable -> "Treatment Complete"
    PatientStatus.Pending -> "Awaiting Review"
}

private fun String.abbreviate(): String {
    val words = split(" ")
    return if (words.size <= 2) this
    else words.take(2).joinToString(" ") + "."
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
private fun ClinicianHomePreview() {
    val samplePatients = listOf(
        PatientSummary(
            id = "1", name = "Maria L.", lastVisitDate = "2 days ago",
            treatmentType = "Post-operative Rehab", status = PatientStatus.Active,
            asymmetryCoeff = 0.82f, kyphosisCoeff = 12.4f,
            latestUpdate = "New insole data synced"
        ),
        PatientSummary(
            id = "2", name = "Luca B.", lastVisitDate = "4 days ago",
            treatmentType = "Scoliosis Alignment", status = PatientStatus.Stable,
            asymmetryCoeff = 0.79f, kyphosisCoeff = 11.8f,
            latestUpdate = "Asymmetry updated"
        ),
        PatientSummary(
            id = "3", name = "Anna R.", lastVisitDate = "1 week ago",
            treatmentType = "Gait Instability", status = PatientStatus.Active,
            asymmetryCoeff = 0.91f, kyphosisCoeff = 15.2f
        ),
        PatientSummary(
            id = "4", name = "Stefano T.", lastVisitDate = "5 days ago",
            treatmentType = "Kyphosis Correction", status = PatientStatus.Stable,
            asymmetryCoeff = 0.85f, kyphosisCoeff = 18.7f
        )
    )
    AppTheme {
        ClinicianHomeContent(
            state = ClinicianHomeUiState(
                doctorName = "Marco Rossi",
                notificationCount = 3,
                notifications = listOf(
                    "Maria L. uploaded new insole data",
                    "Luca B. updated asymmetry report"
                ),
                recentPatients = samplePatients.take(2),
                allPatients = samplePatients
            ),
            onPatientClick = {},
            onNotificationsClick = {},
            onViewAllPatientsClick = {}
        )
    }
}
