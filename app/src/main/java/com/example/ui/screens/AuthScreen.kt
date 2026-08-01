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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(viewModel: JuktiViewModel) {
    val language by viewModel.language.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()

    var isLoginTab by remember { mutableStateOf(true) }
    var nameInput by remember { mutableStateOf("") }
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var confirmPasswordInput by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var showGoogleAccountDialog by remember { mutableStateOf(false) }

    if (showForgotPasswordDialog) {
        var resetEmail by remember { mutableStateOf(emailInput) }
        var resetSentMessage by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showForgotPasswordDialog = false },
            title = { Text(if (language == AppLanguage.ASSAMESE) "পাছৱৰ্ড পাহৰিলে" else "Forgot Password", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    if (resetSentMessage) {
                        Text(
                            text = if (language == AppLanguage.ASSAMESE) "পাছৱৰ্ড পুনৰসংস্থাপনৰ লিংক প্ৰেৰণ কৰা হৈছে: $resetEmail"
                            else "Password reset link sent to $resetEmail. Please check your inbox.",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    } else {
                        Text(if (language == AppLanguage.ASSAMESE) "আপোনাৰ ইমেইল দিয়ক পাছৱৰ্ড পুনৰ সংস্থাপন লিংক পাবলৈ।" else "Enter your registered email address to receive a password reset link.")
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
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
                if (resetSentMessage) {
                    Button(onClick = { showForgotPasswordDialog = false }) {
                        Text("OK")
                    }
                } else {
                    Button(onClick = {
                        if (resetEmail.isNotBlank() && resetEmail.contains("@")) {
                            resetSentMessage = true
                        }
                    }) {
                        Text(if (language == AppLanguage.ASSAMESE) "লিংক প্ৰেৰণ কৰক" else "Send Reset Link")
                    }
                }
            },
            dismissButton = {
                if (!resetSentMessage) {
                    TextButton(onClick = { showForgotPasswordDialog = false }) {
                        Text(if (language == AppLanguage.ASSAMESE) "বাতিল কৰক" else "Cancel")
                    }
                }
            }
        )
    }

    if (showGoogleAccountDialog) {
        var customGoogleEmail by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showGoogleAccountDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF4285F4),
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("G", color = Color.White, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                    Text(if (language == AppLanguage.ASSAMESE) "Google একাউণ্ট বাছনি কৰক" else "Choose a Google Account", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = if (language == AppLanguage.ASSAMESE) "Jukti-ত ছাইন ইন কৰিবলৈ এটা গুগল একাউণ্ট বাছক:" else "Select a Google account to continue to Jukti App:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Account 1: Pinku Bora (Admin)
                    Surface(
                        onClick = {
                            viewModel.loginWithEmail("borapinku151@gmail.com", "Pinku Bora")
                            viewModel.toggleGuestMode(false)
                            showGoogleAccountDialog = false
                            viewModel.navigateTo(Screen.HOME)
                        },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(40.dp)) {
                                Box(contentAlignment = Alignment.Center) { Text("PB", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Pinku Bora", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Text("borapinku151@gmail.com", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Surface(shape = RoundedCornerShape(4.dp), color = MaterialTheme.colorScheme.secondaryContainer) {
                                Text("ADMIN", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }
                    }

                    // Account 2: Jukti Education (Owner)
                    Surface(
                        onClick = {
                            viewModel.loginWithEmail("juktieducation@gmail.com", "Jukti Education")
                            viewModel.toggleGuestMode(false)
                            showGoogleAccountDialog = false
                            viewModel.navigateTo(Screen.HOME)
                        },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.tertiaryContainer, modifier = Modifier.size(40.dp)) {
                                Box(contentAlignment = Alignment.Center) { Text("JE", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary) }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Jukti Education", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Text("juktieducation@gmail.com", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Surface(shape = RoundedCornerShape(4.dp), color = MaterialTheme.colorScheme.tertiaryContainer) {
                                Text("OWNER", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }
                    }

                    // Account 3: Assam Scholar (Student)
                    Surface(
                        onClick = {
                            viewModel.loginWithEmail("scholar.assam@gmail.com", "Assam Scholar")
                            viewModel.toggleGuestMode(false)
                            showGoogleAccountDialog = false
                            viewModel.navigateTo(Screen.HOME)
                        },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.size(40.dp)) {
                                Box(contentAlignment = Alignment.Center) { Text("AS", fontWeight = FontWeight.Bold) }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Assam Scholar", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Text("scholar.assam@gmail.com", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    Divider(modifier = Modifier.padding(vertical = 4.dp))

                    Text("Or enter another Google email:", style = MaterialTheme.typography.labelMedium)
                    OutlinedTextField(
                        value = customGoogleEmail,
                        onValueChange = { customGoogleEmail = it },
                        placeholder = { Text("your.email@gmail.com") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val email = customGoogleEmail.trim().ifBlank { "borapinku151@gmail.com" }
                        viewModel.loginWithEmail(email)
                        viewModel.toggleGuestMode(false)
                        showGoogleAccountDialog = false
                        viewModel.navigateTo(Screen.HOME)
                    }
                ) {
                    Text("Continue")
                }
            },
            dismissButton = {
                TextButton(onClick = { showGoogleAccountDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
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
                    imageVector = Icons.Default.MenuBook,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Jukti",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = if (language == AppLanguage.ASSAMESE) "অসমৰ প্ৰতিযোগিতামূলক পৰীক্ষা প্ৰস্তুতি পৰ্টেল" else "Assam Competitive Exam Preparation Portal",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Tabs
        TabRow(
            selectedTabIndex = if (isLoginTab) 0 else 1,
            containerColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = isLoginTab,
                onClick = {
                    isLoginTab = true
                    errorMessage = null
                },
                text = { Text(if (language == AppLanguage.ASSAMESE) "লগ ইন (Sign In)" else "Sign In", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = !isLoginTab,
                onClick = {
                    isLoginTab = false
                    errorMessage = null
                },
                text = { Text(if (language == AppLanguage.ASSAMESE) "পঞ্জীয়ন (Register)" else "Register", fontWeight = FontWeight.Bold) }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (errorMessage != null) {
            Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = errorMessage!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (!isLoginTab) {
            // Full Name for Register
            OutlinedTextField(
                value = nameInput,
                onValueChange = {
                    nameInput = it
                    errorMessage = null
                },
                label = { Text(if (language == AppLanguage.ASSAMESE) "সম্পূৰ্ণ নাম (Full Name)" else "Full Name") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Email Address
        OutlinedTextField(
            value = emailInput,
            onValueChange = {
                emailInput = it
                errorMessage = null
            },
            label = { Text(if (language == AppLanguage.ASSAMESE) "ইমেইল ঠিকনা (Email Address)" else "Email Address") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Password
        OutlinedTextField(
            value = passwordInput,
            onValueChange = {
                passwordInput = it
                errorMessage = null
            },
            label = { Text(if (language == AppLanguage.ASSAMESE) "পাছৱৰ্ড (Password)" else "Password") },
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
            OutlinedTextField(
                value = confirmPasswordInput,
                onValueChange = {
                    confirmPasswordInput = it
                    errorMessage = null
                },
                label = { Text(if (language == AppLanguage.ASSAMESE) "পাছৱৰ্ড নিশ্চিত কৰক (Confirm Password)" else "Confirm Password") },
                leadingIcon = { Icon(Icons.Default.LockReset, contentDescription = null) },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (isLoginTab) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { showForgotPasswordDialog = true }) {
                    Text(if (language == AppLanguage.ASSAMESE) "পাছৱৰ্ড পাহৰিলে?" else "Forgot Password?")
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
                        errorMessage = if (language == AppLanguage.ASSAMESE) "অনুগ্ৰহ কৰি বৈধ ইমেইল প্ৰৱেশ কৰক" else "Please enter a valid email address."
                        return@Button
                    }
                    if (trimmedPassword.isBlank()) {
                        errorMessage = if (language == AppLanguage.ASSAMESE) "অনুগ্ৰহ কৰি পাছৱৰ্ড প্ৰৱেশ কৰক" else "Please enter your password."
                        return@Button
                    }
                    viewModel.loginWithEmail(trimmedEmail)
                    viewModel.toggleGuestMode(false)
                    viewModel.navigateTo(Screen.HOME)
                } else {
                    val trimmedName = nameInput.trim()
                    if (trimmedName.isBlank()) {
                        errorMessage = if (language == AppLanguage.ASSAMESE) "অনুগ্ৰহ কৰি আপোনাৰ সম্পূৰ্ণ নাম লিখক" else "Please enter your full name."
                        return@Button
                    }
                    if (trimmedEmail.isBlank() || !trimmedEmail.contains("@")) {
                        errorMessage = if (language == AppLanguage.ASSAMESE) "অনুগ্ৰহ কৰি বৈধ ইমেইল প্ৰৱেশ কৰক" else "Please enter a valid email address."
                        return@Button
                    }
                    if (trimmedPassword.length < 6) {
                        errorMessage = if (language == AppLanguage.ASSAMESE) "পাছৱৰ্ড অন্ততঃ ৬টা আখৰৰ হ'ব লাগে" else "Password must be at least 6 characters."
                        return@Button
                    }
                    if (trimmedPassword != confirmPasswordInput.trim()) {
                        errorMessage = if (language == AppLanguage.ASSAMESE) "পাছৱৰ্ড অমিল হৈছে" else "Passwords do not match."
                        return@Button
                    }
                    viewModel.loginWithEmail(trimmedEmail, trimmedName)
                    viewModel.toggleGuestMode(false)
                    viewModel.navigateTo(Screen.HOME)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = if (isLoginTab) Icons.Default.Login else Icons.Default.PersonAdd,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isLoginTab) {
                    if (language == AppLanguage.ASSAMESE) "ছাইন ইন (Sign In)" else "Sign In with Email"
                } else {
                    if (language == AppLanguage.ASSAMESE) "পঞ্জীয়ন কৰক (Create Account)" else "Create Account & Register"
                },
                fontWeight = FontWeight.Bold
            )
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

        // Google Login Button
        OutlinedButton(
            onClick = {
                showGoogleAccountDialog = true
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
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
                text = if (language == AppLanguage.ASSAMESE) "Google ৰ সৈতে অব্যাহত ৰাখক" else "Continue with Google",
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(
            onClick = {
                viewModel.toggleGuestMode(true)
                viewModel.navigateTo(Screen.HOME)
            }
        ) {
            Text(if (language == AppLanguage.ASSAMESE) "অতিথি হিচাপে অব্যাহত ৰাখক (Guest Mode)" else "Continue as Guest (Limited Access)")
        }
    }
}

