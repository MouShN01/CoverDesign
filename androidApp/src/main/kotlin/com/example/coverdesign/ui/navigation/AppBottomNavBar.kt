package com.example.coverdesign.ui.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun AppBottomNavBar(
    currentRoute   : String?,
    onItemSelected : (BottomNavItem) -> Unit
) {
    NavigationBar {
        bottomNavItems.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                selected = selected,
                onClick  = { onItemSelected(item) },
                icon     = {
                    Icon(
                        imageVector        = if (selected) item.iconSelected else item.icon,
                        contentDescription = item.label
                    )
                },
                label    = { Text(item.label) }
            )
        }
    }
}