package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import com.example.ui.theme.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.QuestionEntity
import com.example.ui.components.BilingualText
import com.example.ui.viewmodel.AppLanguage
import com.example.ui.viewmodel.JuktiViewModel
import com.example.ui.viewmodel.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: JuktiViewModel) {
    val language by viewModel.language.collectAsState()
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val bookmarkedQuestions by viewModel.bookmarkedQuestions.collectAsState()
    val hiddenQuestions by viewModel.hiddenQuestions.collectAsState()
    val isAssamese = language == AppLanguage.ASSAMESE

    var offlineSync by remember { mutableStateOf(true) }
    var showCacheDialog by remember { mutableStateOf(false) }
    var cacheClearedMessage by remember { mutableStateOf(false) }
    var showSavedQuestionsDialog by remember { mutableStateOf(false) }
    var showHiddenQuestionsDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("App Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo(Screen.MENU) }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
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
            // Language & Theme Section
            Text(
                text = "Display Preference",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Dark Theme Mode", fontWeight = FontWeight.SemiBold)
                            Text(
                                "Comfortable reading for night study",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        val systemDark = androidx.compose.foundation.isSystemInDarkTheme()
                        Switch(
                            checked = isDarkTheme ?: systemDark,
                            onCheckedChange = { viewModel.setDarkTheme(it) },
                            colors = SwitchDefaults.colors(
                                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                                uncheckedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )
                    }
                }
            }

            // Storage & Cloud Services
            Text(
                text = "Storage & Data Services",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Offline Questions Caching", fontWeight = FontWeight.SemiBold)
                            Text(
                                "Store mock tests & questions offline",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = offlineSync, 
                            onCheckedChange = { offlineSync = it },
                            colors = SwitchDefaults.colors(
                                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                                uncheckedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                    ListItem(
                        headlineContent = { Text("Clear Temporary App Cache") },
                        supportingContent = { Text("Free up cached test files (34 MB)") },
                        leadingContent = { Icon(Icons.Default.CleaningServices, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                        modifier = Modifier.clickable { showCacheDialog = true }
                    )
                }
            }

            if (cacheClearedMessage) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Cache memory successfully cleared!",
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Saved & Hidden Questions Management
            Text(
                text = "Saved & Hidden Questions",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column {
                    // Item 1: Saved Questions
                    ListItem(
                        headlineContent = {
                            Text(
                                "Saved Questions",
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        supportingContent = {
                            Text(
                                "${bookmarkedQuestions.size} saved MCQs. Tap to review.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        leadingContent = {
                            Icon(Icons.Default.Bookmark, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        },
                        trailingContent = {
                            Badge(containerColor = MaterialTheme.colorScheme.primaryContainer) {
                                Text("${bookmarkedQuestions.size}", color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                        },
                        modifier = Modifier.clickable { showSavedQuestionsDialog = true }
                    )

                    HorizontalDivider()

                    // Item 2: Hidden Questions
                    ListItem(
                        headlineContent = {
                            Text(
                                "Hidden Questions",
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        supportingContent = {
                            Text(
                                "${hiddenQuestions.size} hidden MCQs. Tap to view or unhide.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        leadingContent = {
                            Icon(Icons.Outlined.VisibilityOff, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        },
                        trailingContent = {
                            Badge(containerColor = MaterialTheme.colorScheme.errorContainer) {
                                Text("${hiddenQuestions.size}", color = MaterialTheme.colorScheme.onErrorContainer)
                            }
                        },
                        modifier = Modifier.clickable { showHiddenQuestionsDialog = true }
                    )
                }
            }

            // Account Management / Danger Zone
            Text(
                text = "Account Management",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f))
            ) {
                ListItem(
                    headlineContent = {
                        Text(
                            "Delete User Account",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    },
                    supportingContent = {
                        Text(
                            "Permanently delete user profile, scores, and saved local data",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    leadingContent = {
                        Icon(Icons.Default.DeleteForever, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    },
                    modifier = Modifier.clickable { showDeleteAccountDialog = true }
                )
            }
        }
    }

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
                    "This will permanently delete your user profile, study progress, and saved data from this device. Are you sure you want to proceed?"
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

    if (showCacheDialog) {
        AlertDialog(
            onDismissRequest = { showCacheDialog = false },
            title = { Text("Clear App Cache?") },
            text = { Text("This will clear 34 MB of temporary cached files. Your score history will remain safe.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showCacheDialog = false
                        cacheClearedMessage = true
                    }
                ) {
                    Text("Clear")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCacheDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showSavedQuestionsDialog) {
        SavedQuestionsDialog(
            questions = bookmarkedQuestions,
            language = language,
            onDismiss = { showSavedQuestionsDialog = false },
            onToggleBookmark = { q -> viewModel.toggleBookmarkQuestion(q) }
        )
    }

    if (showHiddenQuestionsDialog) {
        HiddenQuestionsDialog(
            questions = hiddenQuestions,
            language = language,
            onDismiss = { showHiddenQuestionsDialog = false },
            onUnhideQuestion = { q -> viewModel.toggleHideQuestion(q) },
            onUnhideAll = { viewModel.unhideAllQuestions() }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedQuestionsDialog(
    questions: List<QuestionEntity>,
    language: AppLanguage,
    onDismiss: () -> Unit,
    onToggleBookmark: (QuestionEntity) -> Unit
) {
    val isAssamese = language == AppLanguage.ASSAMESE
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
                            modifier = Modifier.fillMaxWidth(),
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
                                            color = MaterialTheme.colorScheme.onSuccessContainer
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
}

@Composable
fun HiddenQuestionsDialog(
    questions: List<QuestionEntity>,
    language: AppLanguage,
    onDismiss: () -> Unit,
    onUnhideQuestion: (QuestionEntity) -> Unit,
    onUnhideAll: () -> Unit
) {
    val isAssamese = language == AppLanguage.ASSAMESE
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.VisibilityOff, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Hidden Questions (${questions.size})",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column {
                if (questions.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Questions you mastered:",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        TextButton(
                            onClick = onUnhideAll,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("Unhide All", fontSize = 12.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (questions.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No hidden questions. When you hide questions you know well during study sessions, they will appear here.",
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
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            color = MaterialTheme.colorScheme.secondaryContainer,
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = q.subject,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer
                                            )
                                        }
                                        OutlinedButton(
                                            onClick = { onUnhideQuestion(q) },
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                            modifier = Modifier.height(32.dp)
                                        ) {
                                            Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Unhide", fontSize = 11.sp)
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    BilingualText(
                                        textEn = q.questionEn,
                                        textAs = q.questionAs,
                                        language = language,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
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
}
