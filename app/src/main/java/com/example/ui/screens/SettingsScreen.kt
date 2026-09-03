package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.QuestionEntity
import com.example.ui.components.BilingualText
import com.example.ui.components.JuktiTopAppBar
import com.example.ui.theme.success
import com.example.ui.theme.successContainer
import com.example.ui.viewmodel.AppLanguage
import com.example.ui.viewmodel.JuktiViewModel
import com.example.ui.viewmodel.LocalMessageTranslator
import com.example.ui.viewmodel.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: JuktiViewModel) {
    val language = com.example.ui.viewmodel.AppLanguage.ENGLISH
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val bookmarkedQuestions by viewModel.bookmarkedQuestions.collectAsState()
    val isRefreshingFromFirebase by viewModel.isRefreshingFromFirebase.collectAsState()
    val refreshStatusMessage by viewModel.refreshStatusMessage.collectAsState()
    val returnToSavedQuestions by viewModel.returnToSavedQuestions.collectAsState()
    val context = LocalContext.current

    var showSavedQuestionsDialog by remember { mutableStateOf(false) }
    var showClearProgressDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }

    LaunchedEffect(returnToSavedQuestions) {
        if (returnToSavedQuestions) {
            showSavedQuestionsDialog = true
            viewModel.setReturnToSavedQuestions(false)
        }
    }

    var currentPasswordInput by remember { mutableStateOf("") }
    var newPasswordInput by remember { mutableStateOf("") }
    var confirmNewPasswordInput by remember { mutableStateOf("") }
    var currentPasswordVisible by remember { mutableStateOf(false) }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var changePasswordMessage by remember { mutableStateOf<String?>(null) }
    var isChangePasswordSuccess by remember { mutableStateOf(false) }
    var isChangePasswordLoading by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            JuktiTopAppBar(
                title = "Settings",
                onBackClick = { viewModel.navigateTo(Screen.MENU) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Preferences & Management",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Customize theme, review saved questions, sync & manage data",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            Text(
                text = "App Settings",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            // Notification Settings
            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val intent = android.content.Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                        intent.putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, context.packageName)
                        context.startActivity(intent)
                    }
                    .testTag("settings_notification_settings_card"),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text("Notification Settings", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text(
                            "Manage notification preferences in system settings",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Theme
            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("settings_theme_card"),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.DarkMode,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text("Theme (Dark Mode)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text(
                            "Comfortable reading mode for night study sessions",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    val systemDark = isSystemInDarkTheme()
                    Switch(
                        checked = isDarkTheme ?: systemDark,
                        onCheckedChange = { viewModel.setDarkTheme(it) },
                        modifier = Modifier.testTag("theme_switch")
                    )
                }
            }

            // Saved Questions
            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showSavedQuestionsDialog = true }
                    .testTag("settings_saved_questions_card"),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Bookmark,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text("Saved Questions", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text(
                            "${bookmarkedQuestions.size} saved MCQs. Tap to review.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Badge(containerColor = MaterialTheme.colorScheme.primaryContainer) {
                        Text(
                            "${bookmarkedQuestions.size}",
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Refresh App Data
            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !isRefreshingFromFirebase) { viewModel.refreshDataFromFirebase() }
                    .testTag("settings_refresh_data_card"),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text("Refresh App Data", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text(
                            "Fetch & update latest MCQs, tests, notes and exams",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (isRefreshingFromFirebase) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.5.dp
                        )
                    } else {
                        Icon(
                            Icons.Default.Sync,
                            contentDescription = "Sync",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            // Clear Progress
            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showClearProgressDialog = true }
                    .testTag("settings_clear_progress_card"),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.RestartAlt,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Clear Progress",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Reset speed, accuracy, questions solved, and rank stats",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Change Password
            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        currentPasswordInput = ""
                        newPasswordInput = ""
                        confirmNewPasswordInput = ""
                        changePasswordMessage = null
                        isChangePasswordSuccess = false
                        showChangePasswordDialog = true
                    }
                    .testTag("settings_change_password_card"),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text("Change Password", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text(
                            "Update your account password securely",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Delete Account (Destructive Action)
            Text(
                text = "Danger Zone",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDeleteAccountDialog = true }
                    .testTag("settings_delete_account_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.DeleteForever,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onError,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Delete Account",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            "Permanently delete user profile, scores, and all saved data",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }

    // Dialog 1: Clear Progress Confirmation Dialog
    if (showClearProgressDialog) {
        AlertDialog(
            onDismissRequest = { showClearProgressDialog = false },
            title = {
                Text(
                    "Clear Progress Data?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    "This will reset all your progress statistics including speed, accuracy, questions solved, and leaderboard rank. Your account profile and saved questions will be kept safe. Are you sure you want to proceed?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearUserProgressData()
                        showClearProgressDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Clear Progress")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearProgressDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Dialog 2: Delete Account Strong Confirmation Dialog
    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            title = {
                Text(
                    "Delete Account?",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            },
            text = {
                Text(
                    "This action is permanent and irreversible. Your user account, progress records, test history, and all personal data will be completely deleted. Are you sure you want to proceed?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteAccountDialog = false
                        viewModel.deleteAccount()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete Account")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Dialog 3: Refresh Status Message
    if (refreshStatusMessage != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearRefreshStatusMessage() },
            title = { Text("Refresh Status", fontWeight = FontWeight.Bold) },
            text = { Text(LocalMessageTranslator.translateGeneralMessage(context, refreshStatusMessage ?: "")) },
            confirmButton = {
                Button(onClick = { viewModel.clearRefreshStatusMessage() }) {
                    Text("OK")
                }
            }
        )
    }

    // Dialog 4: Saved Questions Viewer
    if (showSavedQuestionsDialog) {
        SavedQuestionsDialog(
            questions = bookmarkedQuestions,
            language = language,
            onDismiss = { showSavedQuestionsDialog = false },
            onToggleBookmark = { q -> viewModel.toggleBookmarkQuestion(q) },
            onPracticeQuestion = { q ->
                showSavedQuestionsDialog = false
                viewModel.startPracticeForQuestion(q)
                viewModel.setReturnToSavedQuestions(true)
            }
        )
    }

    // Dialog 6: Change Password Dialog
    if (showChangePasswordDialog) {
        AlertDialog(
            onDismissRequest = { if (!isChangePasswordLoading) showChangePasswordDialog = false },
            title = { Text("Change Password", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (changePasswordMessage != null) {
                        Text(
                            text = changePasswordMessage!!,
                            color = if (isChangePasswordSuccess) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    if (!isChangePasswordSuccess) {
                        Text("Please enter your current password and choose a new secure password.")

                        OutlinedTextField(
                            value = currentPasswordInput,
                            onValueChange = { currentPasswordInput = it },
                            label = { Text("Current Password") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            trailingIcon = {
                                IconButton(onClick = { currentPasswordVisible = !currentPasswordVisible }) {
                                    Icon(
                                        imageVector = if (currentPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Toggle password visibility"
                                    )
                                }
                            },
                            visualTransformation = if (currentPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            enabled = !isChangePasswordLoading,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = newPasswordInput,
                            onValueChange = { newPasswordInput = it },
                            label = { Text("New Password") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            trailingIcon = {
                                IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                                    Icon(
                                        imageVector = if (newPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Toggle password visibility"
                                    )
                                }
                            },
                            visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            enabled = !isChangePasswordLoading,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = confirmNewPasswordInput,
                            onValueChange = { confirmNewPasswordInput = it },
                            label = { Text("Confirm New Password") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            trailingIcon = {
                                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                    Icon(
                                        imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Toggle password visibility"
                                    )
                                }
                            },
                            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            enabled = !isChangePasswordLoading,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                if (!isChangePasswordSuccess) {
                    Button(
                        onClick = {
                            if (currentPasswordInput.isBlank()) {
                                changePasswordMessage = "The current password cannot be empty."
                                return@Button
                            }
                            if (newPasswordInput.length < 6) {
                                changePasswordMessage = "Your new password must be at least 6 characters."
                                return@Button
                            }
                            if (newPasswordInput != confirmNewPasswordInput) {
                                changePasswordMessage = "New password and confirmation do not match."
                                return@Button
                            }
                            isChangePasswordLoading = true
                            changePasswordMessage = null
                            viewModel.changePassword(currentPasswordInput, newPasswordInput) { success, msg ->
                                isChangePasswordLoading = false
                                isChangePasswordSuccess = success
                                changePasswordMessage = msg
                            }
                        },
                        enabled = !isChangePasswordLoading
                    ) {
                        if (isChangePasswordLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Change Password")
                        }
                    }
                } else {
                    Button(onClick = {
                        showChangePasswordDialog = false
                        currentPasswordInput = ""
                        newPasswordInput = ""
                        confirmNewPasswordInput = ""
                        changePasswordMessage = null
                        isChangePasswordSuccess = false
                    }) {
                        Text("OK")
                    }
                }
            },
            dismissButton = {
                if (!isChangePasswordSuccess) {
                    TextButton(
                        onClick = {
                            showChangePasswordDialog = false
                            currentPasswordInput = ""
                            newPasswordInput = ""
                            confirmNewPasswordInput = ""
                            changePasswordMessage = null
                        },
                        enabled = !isChangePasswordLoading
                    ) {
                        Text("Cancel")
                    }
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedQuestionsDialog(
    questions: List<QuestionEntity>,
    language: AppLanguage,
    isUserPremium: Boolean = false,
    isAdminOrOwner: Boolean = false,
    onDismiss: () -> Unit,
    onToggleBookmark: (QuestionEntity) -> Unit,
    onPracticeQuestion: (QuestionEntity) -> Unit
) {
    var selectedQuestionForAction by remember { mutableStateOf<QuestionEntity?>(null) }
    var viewingQuestion by remember { mutableStateOf<QuestionEntity?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Bookmark, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Saved Questions (${questions.size})",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            if (questions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No saved questions yet. Bookmark MCQs during study or practice sessions to review them here.",
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 450.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(questions, key = { it.id }) { q ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedQuestionForAction = q },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = q.subject,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                    IconButton(
                                        onClick = { onToggleBookmark(q) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Bookmark,
                                            contentDescription = "Unsave",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                BilingualText(
                                    textEn = q.questionEn,
                                    textAs = q.questionAs,
                                    language = language,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                val correctOptTextEn = when(q.correctOptionIndex) {
                                    0 -> q.optionAEn
                                    1 -> q.optionBEn
                                    2 -> q.optionCEn
                                    else -> q.optionDEn
                                }
                                val correctOptTextAs = when(q.correctOptionIndex) {
                                    0 -> q.optionAAs
                                    1 -> q.optionBAs
                                    2 -> q.optionCAs
                                    else -> q.optionDAs
                                }
                                Surface(
                                    color = MaterialTheme.colorScheme.successContainer,
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text(
                                            text = "Correct Answer:",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.success
                                        )
                                        BilingualText(
                                            textEn = correctOptTextEn,
                                            textAs = correctOptTextAs,
                                            language = language,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )

    if (selectedQuestionForAction != null) {
        val q = selectedQuestionForAction!!
        SavedQuestionActionDialog(
            question = q,
            language = language,
            onDismiss = { selectedQuestionForAction = null },
            onViewQuestion = {
                selectedQuestionForAction = null
                viewingQuestion = q
            },
            onPracticeQuestion = {
                selectedQuestionForAction = null
                onDismiss()
                onPracticeQuestion(q)
            }
        )
    }

    if (viewingQuestion != null) {
        val q = viewingQuestion!!
        ViewQuestionReadOnlyDialog(
            question = q,
            language = language,
            onDismiss = { viewingQuestion = null }
        )
    }
}

@Composable
fun SavedQuestionActionDialog(
    question: QuestionEntity,
    language: AppLanguage,
    onDismiss: () -> Unit,
    onViewQuestion: () -> Unit,
    onPracticeQuestion: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "What would you like to do?",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onViewQuestion)
                        .testTag("action_view_question"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "View Question",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Review answer & explain.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onPracticeQuestion)
                        .testTag("action_practice_question"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Practice Question",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Attempt this question.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ViewQuestionReadOnlyDialog(
    question: QuestionEntity,
    language: AppLanguage,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("View Question", fontWeight = FontWeight.Bold)
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = question.subject,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                BilingualText(
                    textEn = question.questionEn,
                    textAs = question.questionAs,
                    language = language,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )

                val options = listOf(
                    Triple("A", question.optionAEn, question.optionAAs),
                    Triple("B", question.optionBEn, question.optionBAs),
                    Triple("C", question.optionCEn, question.optionCAs),
                    Triple("D", question.optionDEn, question.optionDAs)
                )

                options.forEachIndexed { idx, (label, optEn, optAs) ->
                    val isCorrect = idx == question.correctOptionIndex
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isCorrect) 
                                MaterialTheme.colorScheme.successContainer.copy(alpha = 0.5f) 
                            else 
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "$label.",
                                fontWeight = FontWeight.Bold,
                                color = if (isCorrect) MaterialTheme.colorScheme.success else MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(modifier = Modifier.weight(1f)) {
                                BilingualText(
                                    textEn = optEn,
                                    textAs = optAs,
                                    language = language,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isCorrect) MaterialTheme.colorScheme.success else MaterialTheme.colorScheme.onSurface
                                )
                            }
                            if (isCorrect) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Correct Answer",
                                    tint = MaterialTheme.colorScheme.success,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                if (question.explanationEn.isNotBlank() || question.explanationAs.isNotBlank()) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Explanation:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            BilingualText(
                                textEn = question.explanationEn,
                                textAs = question.explanationAs,
                                language = language,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}


