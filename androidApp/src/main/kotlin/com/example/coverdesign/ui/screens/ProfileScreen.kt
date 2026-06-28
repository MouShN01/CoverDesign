package com.example.coverdesign.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Help
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.coverdesign.ui.components.AppTopBar
import com.example.coverdesign.ui.viewmodel.AuthAction
import com.example.coverdesign.ui.viewmodel.AuthViewModel
import org.koin.compose.viewmodel.koinViewModel

// androidApp/ui/screens/profile/ProfileScreen.kt
@Composable
fun ProfileScreen(
    viewModel : AuthViewModel = koinViewModel()
) {
    Scaffold(
        topBar = { AppTopBar(title = "Profile") }
    ) { padding ->
        Column(
            modifier            = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            // avatar placeholder
            Surface(
                shape  = CircleShape,
                color  = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(80.dp)
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    modifier           = Modifier
                        .padding(20.dp)
                        .fillMaxSize(),
                    tint               = MaterialTheme.colorScheme.primary
                )
            }

            Text("User", style = MaterialTheme.typography.titleLarge)
            Text("user@example.com",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(Modifier.height(16.dp))
            Divider()
            Spacer(Modifier.height(8.dp))

            // placeholder profile options
            listOf(
                Icons.Outlined.Settings to "Settings",
                Icons.Outlined.Notifications to "Notifications",
                Icons.Outlined.Help to "Help & Support",
                Icons.Outlined.PrivacyTip to "Privacy Policy"
            ).forEach { (icon, label) ->
                Surface(
                    shape  = RoundedCornerShape(12.dp),
                    color  = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier          = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(icon, null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.width(12.dp))
                        Text(label, modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyMedium)
                        Icon(Icons.Default.ChevronRight, null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            // logout
            OutlinedButton(
                onClick  = { viewModel.onAction(AuthAction.SignOut) },
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(12.dp),
                colors   = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ),
                border   = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Default.Logout, null)
                Spacer(Modifier.width(8.dp))
                Text("Sign Out")
            }
        }
    }
}