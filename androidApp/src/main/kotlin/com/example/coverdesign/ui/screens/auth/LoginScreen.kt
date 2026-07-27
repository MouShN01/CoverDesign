package com.example.coverdesign.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.coverdesign.domain.model.UserRole
import com.example.coverdesign.ui.components.EventHandler
import com.example.coverdesign.ui.theme.AppTheme
import com.example.coverdesign.ui.viewmodel.AuthAction
import com.example.coverdesign.ui.viewmodel.AuthUiEvent
import com.example.coverdesign.ui.viewmodel.AuthUiState
import com.example.coverdesign.ui.viewmodel.AuthViewModel


@Composable
fun LoginScreen(
    viewModel: AuthViewModel, //= koinViewModel(),
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHost = remember { SnackbarHostState() }

    EventHandler(viewModel.events) { event ->
        when (event) {
            is AuthUiEvent.NavigateToHome -> onLoginSuccess()
            else -> {}
        }
    }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHost.showSnackbar(it, duration = SnackbarDuration.Long)
            viewModel.onAction(AuthAction.ClearError)
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHost) }) { padding ->
        LoginContent(
            state = state,
            onAction = viewModel::onAction,
            onNavigateToRegister = onNavigateToRegister,
            modifier = Modifier.padding(padding)
        )
    }
}
@Composable
internal fun LoginContent(
    state: AuthUiState,
    onAction: (AuthAction) -> Unit,
    onNavigateToRegister: () -> Unit,
    modifier: Modifier = Modifier
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 40.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ── header ────────────────────────────────────────────────────────────
        Text(
            "Sign In",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            "Welcome back to ProsCover",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        state.role?.let { RoleTag(it) }

        Spacer(Modifier.height(8.dp))

        // ── fields ────────────────────────────────────────────────────────────
        AuthTextField(
            value = email,
            onValueChange = { email = it },
            label = "Email",
            leadingIcon = { Icon(Icons.Default.Email, null) },
            keyboardType = KeyboardType.Email
        )

        AuthTextField(
            value = password,
            onValueChange = { password = it },
            label = "Password",
            leadingIcon = { Icon(Icons.Default.Lock, null) },
            keyboardType = KeyboardType.Password,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        if (passwordVisible) Icons.Default.VisibilityOff
                        else Icons.Default.Visibility, contentDescription = null
                    )
                }
            })

        TextButton(
            onClick = { onAction(AuthAction.ResetPassword(email)) },
            modifier = Modifier.align(Alignment.End)
        ) { Text("Forgot password?") }

        // reset sent confirmation
        if (state.resetEmailSent) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    "Password reset email sent — check your inbox.",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(12.dp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Spacer(Modifier.height(4.dp))

        // ── sign in button ────────────────────────────────────────────────────
        AuthPrimaryButton(
            text = "Sign In",
            isLoading = state.isLoading,
            onClick = { onAction(AuthAction.SignInWithEmail(email, password, state.role)) })

        // ── divider ───────────────────────────────────────────────────────────
        OrDivider()

        // ── Google ────────────────────────────────────────────────────────────
        GoogleSignInButton(
            isLoading = state.isLoading, onClick = { onAction(AuthAction.SignInWithGoogle(context, state.role)) })

        // ── navigate to register ──────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Don't have an account?",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TextButton(onClick = onNavigateToRegister) {
                Text("Create Account")
            }
        }
    }
}


@Composable
private fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    isError: Boolean = false,
    supportingText: String? = null,
    placeholder: String? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = visualTransformation,
        isError = isError,
        supportingText = supportingText?.let { { Text(it) } },
        placeholder = placeholder?.let {
            {
                Text(
                    it, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(10.dp)   // matches app's existing fields
    )
}

@Composable
private fun AuthPrimaryButton(
    text: String,
    isLoading: Boolean,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                Modifier.size(20.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp
            )
        } else {
            Text(text, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun OrDivider() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f))
        Text(
            "or",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        HorizontalDivider(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun RoleTag(role: UserRole) {
    Surface(
        shape = RoundedCornerShape(50.dp),
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Text(
            role.name.lowercase().replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
}

@Preview(showBackground = true, name = "Login")
@Composable
private fun LoginPreview() {
    AppTheme {
        LoginContent(state = AuthUiState(), onAction = {}, onNavigateToRegister = {})
    }
}
//
//
//@Composable
//fun LoginScreen(
//    viewModel           : AuthViewModel = koinViewModel(),
//    onLoginSuccess      : () -> Unit,
//    onNavigateToRegister: () -> Unit
//) {
//    val state by viewModel.uiState.collectAsStateWithLifecycle()
//
//    EventHandler(viewModel.events) { event ->
//        when (event) {
//            is AuthUiEvent.Success -> onLoginSuccess()
//        }
//    }
//
//    Scaffold { padding ->
//        Column(
//            modifier            = Modifier
//                .fillMaxSize()
//                .padding(padding)
//                .padding(horizontal = 28.dp)
//                .verticalScroll(rememberScrollState()),
//            horizontalAlignment = Alignment.CenterHorizontally,
//            verticalArrangement = Arrangement.Center
//        ) {
//            // ── Logo / title ───────────────────────────────────────────────
//            Icon(
//                imageVector        = Icons.Default.Accessibility,
//                contentDescription = null,
//                modifier           = Modifier.size(72.dp),
//                tint               = MaterialTheme.colorScheme.primary
//            )
//            Spacer(Modifier.height(8.dp))
//            Text(
//                text  = "Welcome back",
//                style = MaterialTheme.typography.headlineMedium
//            )
//            Text(
//                text  = "Sign in to continue",
//                style = MaterialTheme.typography.bodyMedium,
//                color = MaterialTheme.colorScheme.onSurfaceVariant
//            )
//
//            Spacer(Modifier.height(40.dp))
//
//            // ── Email ──────────────────────────────────────────────────────
//            OutlinedTextField(
//                value         = state.email,
//                onValueChange = { viewModel.onAction(AuthAction.UpdateEmail(it)) },
//                label         = { Text("Email") },
//                modifier      = Modifier.fillMaxWidth(),
//                keyboardOptions = KeyboardOptions(
//                    keyboardType = KeyboardType.Email,
//                    imeAction = ImeAction.Next
//                ),
//                leadingIcon   = { Icon(Icons.Default.Email, null) },
//                singleLine    = true,
//                shape         = RoundedCornerShape(12.dp)
//            )
//
//            Spacer(Modifier.height(12.dp))
//
//            // ── Password ───────────────────────────────────────────────────
//            OutlinedTextField(
//                value         = state.password,
//                onValueChange = { viewModel.onAction(AuthAction.UpdatePassword(it)) },
//                label         = { Text("Password") },
//                modifier      = Modifier.fillMaxWidth(),
//                visualTransformation = if (state.passwordVisible)
//                    VisualTransformation.None
//                else PasswordVisualTransformation(),
//                keyboardOptions = KeyboardOptions(
//                    keyboardType = KeyboardType.Password,
//                    imeAction = ImeAction.Done
//                ),
//                leadingIcon   = { Icon(Icons.Default.Lock, null) },
//                trailingIcon  = {
//                    IconButton(
//                        onClick = { viewModel.onAction(AuthAction.TogglePassword) }
//                    ) {
//                        Icon(
//                            if (state.passwordVisible) Icons.Default.VisibilityOff
//                            else Icons.Default.Visibility,
//                            contentDescription = "Toggle password"
//                        )
//                    }
//                },
//                singleLine    = true,
//                shape         = RoundedCornerShape(12.dp)
//            )
//
//            // ── Error ──────────────────────────────────────────────────────
//            state.error?.let { error ->
//                Spacer(Modifier.height(8.dp))
//                Text(
//                    text  = error,
//                    color = MaterialTheme.colorScheme.error,
//                    style = MaterialTheme.typography.labelMedium
//                )
//            }
//
//            Spacer(Modifier.height(28.dp))
//
//            // ── Login button ───────────────────────────────────────────────
//            Button(
//                onClick  = { viewModel.onAction(AuthAction.Login) },
//                modifier = Modifier.fillMaxWidth().height(52.dp),
//                enabled  = !state.isLoading,
//                shape    = RoundedCornerShape(14.dp)
//            ) {
//                if (state.isLoading) {
//                    CircularProgressIndicator(
//                        Modifier.size(20.dp),
//                        color = MaterialTheme.colorScheme.onPrimary
//                    )
//                } else {
//                    Text("Sign In", style = MaterialTheme.typography.titleSmall)
//                }
//            }
//
//            Spacer(Modifier.height(16.dp))
//
//            // ── Register link ──────────────────────────────────────────────
//            TextButton(onClick = onNavigateToRegister) {
//                Text("Don't have an account? ")
//                Text(
//                    "Register",
//                    color = MaterialTheme.colorScheme.primary,
//                    fontWeight = FontWeight.SemiBold
//                )
//            }
//        }
//    }
//}