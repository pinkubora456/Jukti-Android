package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.ui.viewmodel.AppLanguage
import com.example.ui.viewmodel.JuktiViewModel
import com.example.ui.viewmodel.Screen
import com.example.ui.viewmodel.LocalMessageTranslator
import com.example.ui.components.getLogoIcon
import com.example.ui.components.SafeOutlinedTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(viewModel: JuktiViewModel) {
    val language by viewModel.language.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val sessionMessage by viewModel.sessionMessage.collectAsState()
    val aboutConfig by viewModel.aboutConfig.collectAsState()
    val isAuthLoading by viewModel.isAuthLoading.collectAsState()

    var isLoginTab by remember { mutableStateOf(true) }
    var nameInput by remember { mutableStateOf("") }
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var confirmPasswordInput by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }

    val context = androidx.compose.ui.platform.LocalContext.current
    val activity = context as? android.app.Activity

    if (showForgotPasswordDialog) {
        var resetEmail by remember { mutableStateOf(emailInput) }
        var resetSentMessage by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showForgotPasswordDialog = false },
            title = { Text("Forgot Password", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    if (resetSentMessage) {
                        Text(
                            text = "Password reset link sent to $resetEmail. Please check your inbox.",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    } else {
                        Text("Enter your registered email address to receive a password reset link.")
                        Spacer(modifier = Modifier.height(12.dp))
                        SafeOutlinedTextField(
                            value = resetEmail,
                            onValueChange = { resetEmail = it },
                            label = { Text("Email Address") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                if (!resetSentMessage) {
                    Button(onClick = { resetSentMessage = true }) {
                        Text("Send Reset Link")
                    }
                } else {
                    Button(onClick = { showForgotPasswordDialog = false }) {
                        Text("OK")
                    }
                }
            },
            dismissButton = {
                if (!resetSentMessage) {
                    TextButton(onClick = { showForgotPasswordDialog = false }) {
                        Text("Cancel")
                    }
                }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(72.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = getLogoIcon(aboutConfig.logoIconName),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = aboutConfig.appTitle,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(32.dp))

            TabRow(
                selectedTabIndex = if (isLoginTab) 0 else 1,
                containerColor = Color.Transparent,
                divider = { },
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = isLoginTab,
                    onClick = {
                        if (!isAuthLoading) {
                            isLoginTab = true
                            errorMessage = null
                        }
                    },
                    text = { Text("Sign In", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = !isLoginTab,
                    onClick = {
                        if (!isAuthLoading) {
                            isLoginTab = false
                            errorMessage = null
                        }
                    },
                    text = { Text("Register", fontWeight = FontWeight.Bold) }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (sessionMessage != null) {
                val translatedMessage = remember(sessionMessage) {
                    if (sessionMessage!!.startsWith("Login failed: Firebase API Key is missing")) {
                        sessionMessage!!
                    } else {
                        LocalMessageTranslator.translateAuthError(context, sessionMessage)
                    }
                }
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = translatedMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            if (!isLoginTab) {
                SafeOutlinedTextField(
                    value = nameInput,
                    onValueChange = {
                        nameInput = it
                        errorMessage = null
                    },
                    enabled = !isAuthLoading,
                    label = { Text("Full Name") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            SafeOutlinedTextField(
                value = emailInput,
                onValueChange = {
                    emailInput = it
                    errorMessage = null
                },
                enabled = !isAuthLoading,
                label = { Text("Email Address") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Password
            SafeOutlinedTextField(
                value = passwordInput,
                onValueChange = {
                    passwordInput = it
                    errorMessage = null
                },
                enabled = !isAuthLoading,
                label = { Text("Password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Toggle password visibility"
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            if (!isLoginTab) {
                Spacer(modifier = Modifier.height(12.dp))
                // Confirm Password
                SafeOutlinedTextField(
                    value = confirmPasswordInput,
                    onValueChange = {
                        confirmPasswordInput = it
                        errorMessage = null
                    },
                    enabled = !isAuthLoading,
                    label = { Text("Confirm Password") },
                    leadingIcon = { Icon(Icons.Default.LockReset, contentDescription = null) },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (isLoginTab) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(
                        onClick = { showForgotPasswordDialog = true },
                        enabled = !isAuthLoading
                    ) {
                        Text("Forgot Password?")
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Action Button: Sign In or Register
            Button(
                onClick = {
                    val trimmedEmail = emailInput.trim()
                    val trimmedPassword = passwordInput.trim()

                    if (isLoginTab) {
                        if (trimmedEmail.isBlank() || !trimmedEmail.contains("@")) {
                            errorMessage = "Please enter a valid email address."
                            return@Button
                        }
                        viewModel.loginWithEmail(trimmedEmail, "", trimmedPassword)
                        viewModel.toggleGuestMode(false)
                    } else {
                        val trimmedName = nameInput.trim()
                        if (trimmedName.isBlank()) {
                            errorMessage = "Please enter your full name."
                            return@Button
                        }
                        if (trimmedEmail.isBlank() || !trimmedEmail.contains("@")) {
                            errorMessage = "Please enter a valid email address."
                            return@Button
                        }
                        if (trimmedPassword.length < 6) {
                            errorMessage = "Password must be at least 6 characters."
                            return@Button
                        }
                        if (trimmedPassword != confirmPasswordInput.trim()) {
                            errorMessage = "Passwords do not match."
                            return@Button
                        }
                        viewModel.loginWithEmail(trimmedEmail, trimmedName, trimmedPassword, isRegister = true)
                        viewModel.toggleGuestMode(false)
                    }
                },
                enabled = !isAuthLoading,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isAuthLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(if (isLoginTab) "Signing In..." else "Registering...", fontWeight = FontWeight.Bold)
                } else {
                    Text(
                        text = if (isLoginTab) {
                            "Sign In with Email"
                        } else {
                            "Create Account & Register"
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                HorizontalDivider(modifier = Modifier.weight(1f))
                Text(
                    text = "  OR  ",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                HorizontalDivider(modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Real Google Sign-In Button
            OutlinedButton(
                onClick = {
                    if (activity != null) {
                        viewModel.loginWithGoogle(activity)
                    }
                },
                enabled = !isAuthLoading,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                if (isAuthLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Signing in with Google...", fontWeight = FontWeight.Bold)
                } else {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF4285F4),
                        modifier = Modifier.size(20.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("G", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Continue with Google",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
