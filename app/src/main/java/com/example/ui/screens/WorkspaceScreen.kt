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
import com.example.ui.viewmodel.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkspaceScreen(viewModel: JuktiViewModel) {
    val isAdminOrOwner by viewModel.isAdminOrOwner.collectAsState()
    val isOwner by viewModel.isOwner.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Workspace", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
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
    val pendingQueue by viewModel.pendingSyncQueue.collectAsState()
    val isSyncingActive by viewModel.isSyncUploading.collectAsState()
    var showConfirmPrompt by remember { mutableStateOf(false) }
    var isUploadingLocal by remember { mutableStateOf(false) }
    var resultPromptMessage by remember { mutableStateOf<String?>(null) }

    val isBusy = isSyncingActive || isUploadingLocal
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
                            resultPromptMessage = message
                            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
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
                        text = if (isBusy) "Syncing with Firestore..." else if (pendingCount > 0) "⏳ $pendingCount Pending Sync(s)" else "☁️ All Synced with Firestore",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (pendingCount > 0) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Text(
                        text = if (pendingCount > 0) "Tap to force immediate upload of pending changes" else "All changes auto-uploaded. Tap to trigger Sync Now.",
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
        }

        if (isOwner) {
            WorkspaceBannerCard(
                title = "Owner Dashboard",
                icon = Icons.Default.Dashboard,
                onClick = { viewModel.navigateTo(com.example.ui.viewmodel.Screen.OWNER_DASHBOARD) }
            )
        }

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
            onClick = { viewModel.navigateTo(com.example.ui.viewmodel.Screen.REPORTED_QUESTIONS) }
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

        WorkspaceBannerCard(
            title = "Exam Patern & Cutoff",
            icon = Icons.Default.Analytics,
            onClick = { viewModel.navigateTo(Screen.MANAGE_EXAM_PATTERN_CUTOFF) }
        )

        WorkspaceBannerCard(
            title = "Notification",
            icon = Icons.Default.Notifications,
            onClick = { viewModel.navigateTo(Screen.MANAGE_NOTIFICATIONS) }
        )
    }
}

@Composable
fun WorkspaceBannerCard(title: String, icon: ImageVector, onClick: () -> Unit) {
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
            verticalAlignment = Alignment.CenterVertically
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
    }
}
