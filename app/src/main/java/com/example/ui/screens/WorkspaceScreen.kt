package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import android.widget.Toast
import com.example.ui.viewmodel.JuktiViewModel
import com.example.ui.viewmodel.LocalMessageTranslator
import com.example.ui.viewmodel.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkspaceScreen(viewModel: JuktiViewModel) {
    val isAdminOrOwner by viewModel.isAdminOrOwner.collectAsState()
    val isOwner by viewModel.isOwner.collectAsState()
    val reportedQuestions by viewModel.reportedQuestions.collectAsState()

    Scaffold(
        topBar = {
            com.example.ui.components.JuktiTopAppBar(
                title = "Workspace"
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (!isAdminOrOwner) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Access Denied. Admins Only.")
                }
            } else {
                WorkspaceDashboardContent(viewModel, isOwner)
            }
        }
    }
}

@Composable
fun WorkspaceDashboardContent(viewModel: JuktiViewModel, isOwner: Boolean) {
    val context = LocalContext.current
    val reportedQuestions by viewModel.reportedQuestions.collectAsState()
    val pendingQueue by viewModel.pendingSyncQueue.collectAsState()
    val isSyncingActive by viewModel.isSyncUploading.collectAsState()
    val syncProgress by viewModel.syncProgressState.collectAsState()
    var showConfirmPrompt by remember { mutableStateOf(false) }
    var isUploadingLocal by remember { mutableStateOf(false) }
    var resultPromptMessage by remember { mutableStateOf<String?>(null) }

    var showQueueDetails by remember { mutableStateOf(false) }
    val isBusy = isSyncingActive || isUploadingLocal || syncProgress.isUploading
    val pendingCount = pendingQueue.size

    if (showConfirmPrompt) {
        AlertDialog(
            onDismissRequest = { if (!isBusy) showConfirmPrompt = false },
            title = { Text("Upload All Workspace Changes", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    if (pendingCount > 0)
                        "There are $pendingCount pending change(s) waiting to be uploaded to Firebase Firestore. Upload all now?"
                    else
                        "All pending changes are up to date! Would you like to force a full workspace sync to Firebase Firestore?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmPrompt = false
                        isUploadingLocal = true
                        viewModel.uploadWorkspaceChangesToFirebase { success, message ->
                            isUploadingLocal = false
                            val translated = LocalMessageTranslator.translateGeneralMessage(context, message)
                            resultPromptMessage = translated
                            Toast.makeText(context, translated, Toast.LENGTH_LONG).show()
                        }
                    }
                ) {
                    Text("Sync Now")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmPrompt = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (resultPromptMessage != null) {
        AlertDialog(
            onDismissRequest = { resultPromptMessage = null },
            title = { Text("Upload Confirmation", fontWeight = FontWeight.Bold) },
            text = { Text(resultPromptMessage ?: "") },
            confirmButton = {
                Button(onClick = { resultPromptMessage = null }) {
                    Text("OK")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Upload Changes / Sync Status Card for Admin and Owner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = !isBusy) { showConfirmPrompt = true },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (pendingCount > 0) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.tertiaryContainer
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isBusy) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(32.dp),
                            color = if (pendingCount > 0) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onTertiaryContainer,
                            strokeWidth = 3.dp
                        )
                    } else {
                        Icon(
                            imageVector = if (pendingCount > 0) Icons.Default.SyncProblem else Icons.Default.CloudDone,
                            contentDescription = "Sync Status",
                            tint = if (pendingCount > 0) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isBusy) "Syncing (${syncProgress.stage})..." else if (pendingCount > 0) "⏳ $pendingCount Pending Sync(s)" else "☁️ All Synced with Firestore",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (pendingCount > 0) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Text(
                            text = if (isBusy)
                                syncProgress.message.ifBlank { "Uploading workspace changes to Firebase..." }
                            else if (pendingCount > 0)
                                "Tap to force immediate upload of pending changes"
                            else
                                "All changes auto-uploaded. Tap to trigger Sync Now.",
                            style = MaterialTheme.typography.bodySmall,
                            color = (if (pendingCount > 0) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onTertiaryContainer).copy(alpha = 0.85f)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    FilledTonalButton(
                        onClick = { showConfirmPrompt = true },
                        enabled = !isBusy
                    ) {
                        Text("Sync Now", fontWeight = FontWeight.Bold)
                    }
                }

                if (isBusy) {
                    Spacer(modifier = Modifier.height(12.dp))
                    if (syncProgress.totalItems > 0) {
                        LinearProgressIndicator(
                            progress = { (syncProgress.currentItem.toFloat() / syncProgress.totalItems.toFloat()).coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth(),
                            color = if (pendingCount > 0) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    } else {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            color = if (pendingCount > 0) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }

                if (pendingCount > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showQueueDetails = !showQueueDetails },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (showQueueDetails) "Hide Error Diagnostics" else "Show Pending Items & Error Diagnostics (${pendingQueue.size})",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Icon(
                            imageVector = if (showQueueDetails) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "Toggle queue details",
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }

                    if (showQueueDetails) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            pendingQueue.take(15).forEach { item ->
                                val lastAttemptTimeStr = if (item.lastAttemptAt > 0) {
                                    java.text.SimpleDateFormat("MMM dd, HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(item.lastAttemptAt))
                                } else "Never"

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    tonalElevation = 2.dp,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "${item.operation} • ${item.dataType}",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            OutlinedButton(
                                                onClick = { viewModel.retrySingleSyncItem(item) },
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                                modifier = Modifier.height(32.dp)
                                            ) {
                                                Text("Retry Now", style = MaterialTheme.typography.labelSmall)
                                            }
                                        }
                                        Text(
                                            text = "Document ID: ${item.entityId}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "Status: ${item.syncStatus} • Retries: ${item.retryCount} • Last attempt: $lastAttemptTimeStr",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        if (!item.lastError.isNullOrBlank()) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Firebase Diagnostic: ${item.lastError}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.error,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }
                            if (pendingQueue.size > 15) {
                                Text(
                                    text = "...and ${pendingQueue.size - 15} more pending items",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                }
            }
        }

        // Diagnostic Button
        var isRunningTest by remember { mutableStateOf(false) }
        Button(
            onClick = {
                isRunningTest = true
                viewModel.runMinimalDiagnosticTest { success, msg ->
                    isRunningTest = false
                    resultPromptMessage = msg
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
            enabled = !isRunningTest
        ) {
            if (isRunningTest) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onSecondary, strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text("Run Minimal Diagnostic Test")
        }

        if (isOwner) {

            WorkspaceBannerCard(
                title = "Owner Dashboard",
                icon = Icons.Default.Dashboard,
                onClick = { viewModel.navigateTo(com.example.ui.viewmodel.Screen.OWNER_DASHBOARD) }
            )
        }


        WorkspaceBannerCard(
            title = "Content Overview",
            icon = Icons.Default.Assessment,
            onClick = { viewModel.navigateTo(com.example.ui.viewmodel.Screen.CONTENT_OVERVIEW) }
        )
        WorkspaceBannerCard(
            title = "Manage Q-Bank",
            icon = Icons.Default.LibraryBooks,
            onClick = { viewModel.navigateTo(com.example.ui.viewmodel.Screen.MANAGE_QBANK) }
        )


        WorkspaceBannerCard(
            title = "Manage Mocks",
            icon = Icons.Default.Timer,
            onClick = { viewModel.navigateTo(com.example.ui.viewmodel.Screen.MANAGE_MOCK) }
        )


        WorkspaceBannerCard(
            title = "Manage Plans",
            icon = Icons.Default.CardMembership,
            onClick = { viewModel.navigateTo(com.example.ui.viewmodel.Screen.MANAGE_PLAN) }
        )


        WorkspaceBannerCard(
            title = "Manage Exams",
            icon = Icons.Default.School,
            onClick = { viewModel.navigateTo(com.example.ui.viewmodel.Screen.MANAGE_EXAMS) }
        )


        WorkspaceBannerCard(
            title = "Manage User Log",
            icon = Icons.Default.People,
            onClick = { viewModel.navigateTo(com.example.ui.viewmodel.Screen.MANAGE_USER_LOG) }
        )


        WorkspaceBannerCard(
            title = "Reported Questions",
            icon = Icons.Default.BugReport,
            onClick = { viewModel.navigateTo(com.example.ui.viewmodel.Screen.REPORTED_QUESTIONS) },
            badgeCount = reportedQuestions.size
        )


        WorkspaceBannerCard(
            title = "Manage Current Affairs",
            icon = Icons.Default.Newspaper,
            onClick = { viewModel.navigateTo(com.example.ui.viewmodel.Screen.MANAGE_CURRENT_AFFAIRS) }
        )


        WorkspaceBannerCard(
            title = "Manage Study Notes",
            icon = Icons.Default.MenuBook,
            onClick = { viewModel.navigateTo(com.example.ui.viewmodel.Screen.MANAGE_STUDY_NOTES) }
        )


        WorkspaceBannerCard(
            title = "Manage Subjects & Chapters",
            icon = Icons.Default.Category,
            onClick = { viewModel.navigateTo(com.example.ui.viewmodel.Screen.MANAGE_SUBJECTS_CHAPTERS) }
        )


        WorkspaceBannerCard(
            title = "Information Banners",
            icon = Icons.Default.ViewCarousel,
            onClick = { viewModel.navigateTo(Screen.MANAGE_BANNERS) }
        )

        var showExamPatternChoiceDialog by remember { mutableStateOf(false) }


        WorkspaceBannerCard(
            title = "Exam Patern & Cutoff",
            icon = Icons.Default.Analytics,
            onClick = { showExamPatternChoiceDialog = true }
        )

        if (showExamPatternChoiceDialog) {
            AlertDialog(
                onDismissRequest = { showExamPatternChoiceDialog = false },
                title = { Text("Exam Patterns, Syllabus & Cutoff") },
                text = { Text("Choose an option:") },
                confirmButton = {
                    TextButton(onClick = {
                        showExamPatternChoiceDialog = false
                        viewModel.navigateTo(Screen.MANAGE_EXAM_PATTERN_CUTOFF_UPDATE)
                    }) {
                        Text("Update (Add New)")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showExamPatternChoiceDialog = false
                        viewModel.navigateTo(Screen.MANAGE_EXAM_PATTERN_CUTOFF_VIEW)
                    }) {
                        Text("View (All, Edit & Delete)")
                    }
                }
            )
        }


        WorkspaceBannerCard(
            title = "Notification",
            icon = Icons.Default.Notifications,
            onClick = { viewModel.navigateTo(Screen.MANAGE_NOTIFICATIONS) }
        )
    }
}

@Composable

fun WorkspaceBannerCard(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    badgeCount: Int? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (badgeCount != null) {
                Surface(
                    color = if (badgeCount > 0) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "$badgeCount",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (badgeCount > 0) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}
