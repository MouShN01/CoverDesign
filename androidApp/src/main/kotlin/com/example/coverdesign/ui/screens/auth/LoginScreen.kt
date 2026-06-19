package com.example.coverdesign.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.coverdesign.ui.components.EventHandler
import com.example.coverdesign.ui.viewmodel.AuthAction
import com.example.coverdesign.ui.viewmodel.AuthUiEvent
import com.example.coverdesign.ui.viewmodel.AuthViewModel
import org.koin.compose.viewmodel.koinViewModel


@Composable
fun LoginScreen(
    viewModel           : AuthViewModel = koinViewModel(),
    onLoginSuccess      : () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    EventHandler(viewModel.events) { event ->
        when (event) {
            is AuthUiEvent.Success -> onLoginSuccess()
        }
    }

    Scaffold { padding ->
        Column(
            modifier            = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 28.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // ── Logo / title ───────────────────────────────────────────────
            Icon(
                imageVector        = Icons.Default.Accessibility,
                contentDescription = null,
                modifier           = Modifier.size(72.dp),
                tint               = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text  = "Welcome back",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text  = "Sign in to continue",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(40.dp))

            // ── Email ──────────────────────────────────────────────────────
            OutlinedTextField(
                value         = state.email,
                onValueChange = { viewModel.onAction(AuthAction.UpdateEmail(it)) },
                label         = { Text("Email") },
                modifier      = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                leadingIcon   = { Icon(Icons.Default.Email, null) },
                singleLine    = true,
                shape         = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.height(12.dp))

            // ── Password ───────────────────────────────────────────────────
            OutlinedTextField(
                value         = state.password,
                onValueChange = { viewModel.onAction(AuthAction.UpdatePassword(it)) },
                label         = { Text("Password") },
                modifier      = Modifier.fillMaxWidth(),
                visualTransformation = if (state.passwordVisible)
                    VisualTransformation.None
                else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                leadingIcon   = { Icon(Icons.Default.Lock, null) },
                trailingIcon  = {
                    IconButton(
                        onClick = { viewModel.onAction(AuthAction.TogglePassword) }
                    ) {
                        Icon(
                            if (state.passwordVisible) Icons.Default.VisibilityOff
                            else Icons.Default.Visibility,
                            contentDescription = "Toggle password"
                        )
                    }
                },
                singleLine    = true,
                shape         = RoundedCornerShape(12.dp)
            )

            // ── Error ──────────────────────────────────────────────────────
            state.error?.let { error ->
                Spacer(Modifier.height(8.dp))
                Text(
                    text  = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelMedium
                )
            }

            Spacer(Modifier.height(28.dp))

            // ── Login button ───────────────────────────────────────────────
            Button(
                onClick  = { viewModel.onAction(AuthAction.Login) },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled  = !state.isLoading,
                shape    = RoundedCornerShape(14.dp)
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Sign In", style = MaterialTheme.typography.titleSmall)
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Register link ──────────────────────────────────────────────
            TextButton(onClick = onNavigateToRegister) {
                Text("Don't have an account? ")
                Text(
                    "Register",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}