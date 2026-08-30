package com.example.coverdesign.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun ClinicianBottomNavBar(
    currentRoute: String?,
    onItemSelected: (ClinicianNavItem) -> Unit
) {
    NavigationBar {
        clinicianNavItems.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                selected = selected,
                onClick = { onItemSelected(item) },
                icon = {
                    Icon(
                        imageVector = if (selected) item.iconSelected else item.icon,
                        contentDescription = item.label
                    )
                },
                label = { Text(item.label) }
            )
        }
    }
}

sealed class ClinicianNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val iconSelected: ImageVector = icon
) {
    object Home : ClinicianNavItem(
        Screen.ClinicianHome.route, "Home",
        Icons.Outlined.Home, Icons.Filled.Home
    )

    object Patients : ClinicianNavItem(
        "clinician_patients", "Patients",
        Icons.Outlined.Group, Icons.Filled.Group
    )

    object Reports : ClinicianNavItem(
        "clinician_reports", "Reports",
        Icons.Outlined.BarChart, Icons.Filled.BarChart
    )

    object Settings : ClinicianNavItem(
        "clinician_settings", "Settings",
        Icons.Outlined.Settings, Icons.Filled.Settings
    )
}

val clinicianNavItems = listOf(
    ClinicianNavItem.Home,
    ClinicianNavItem.Patients,
    ClinicianNavItem.Reports,
    ClinicianNavItem.Settings
)
