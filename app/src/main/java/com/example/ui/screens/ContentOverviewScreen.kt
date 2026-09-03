package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.JuktiTopAppBar
import com.example.ui.viewmodel.JuktiViewModel
import com.example.ui.viewmodel.Screen
import androidx.compose.runtime.remember

@Composable
fun ContentOverviewScreen(viewModel: JuktiViewModel) {
    val allQuestions by viewModel.questions.collectAsState()
    val allMocks by viewModel.mockTests.collectAsState()
    val allNotes by viewModel.studyNotes.collectAsState()
    val currentAffairs = remember(allNotes) {
        allNotes.filter { it.subject.contains("Current Affairs", ignoreCase = true) }
    }
    val regularNotes = remember(allNotes) {
        allNotes.filter { !it.subject.contains("Current Affairs", ignoreCase = true) }
    }

    val questionsWithIssuesCount = remember(allQuestions) {
        allQuestions.count { q ->
            q.questionEn.isBlank() || q.subject.isBlank() || q.topic.isBlank() ||
            q.optionAEn.isBlank() || q.optionBEn.isBlank() || q.optionCEn.isBlank() || q.optionDEn.isBlank() ||
            q.correctOptionIndex !in 0..3
        }
    }
    val mocksWithIssuesCount = remember(allMocks) {
        allMocks.count { m ->
            m.titleEn.isBlank() || m.totalQuestions <= 0 || m.totalMarks <= 0f || m.durationMinutes <= 0
        }
    }
    val notesWithIssuesCount = remember(allNotes) {
        allNotes.count { n ->
            n.titleEn.isBlank() || n.subject.isBlank() || n.contentEn.isBlank()
        }
    }
    val totalIssueCount = questionsWithIssuesCount + mocksWithIssuesCount + notesWithIssuesCount

    Scaffold(
        topBar = {
            JuktiTopAppBar(
                title = "Content Overview",
                onBackClick = { viewModel.navigateTo(Screen.WORKSPACE) }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (totalIssueCount > 0) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.navigateTo(Screen.CONTENT_WITH_ISSUES) },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(48.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = "Content with Issues",
                                        modifier = Modifier.size(24.dp),
                                        tint = MaterialTheme.colorScheme.onError
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Content with Issues ($totalIssueCount)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Text(
                                    text = "Issues Found: $totalIssueCount items",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Review and fix malformed or missing data content",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }
            item {
                ContentOverviewBannerCard(
                    title = "Questions",
                    totalCount = allQuestions.size,
                    subtitle = "Subject & Chapter-wise Overview",
                    icon = Icons.Default.LibraryBooks,
                    onClick = { viewModel.navigateTo(Screen.CONTENT_QUESTIONS_OVERVIEW) }
                )
            }
            item {
                ContentOverviewBannerCard(
                    title = "Mock Tests",
                    totalCount = allMocks.size,
                    subtitle = "Mock Test Overview",
                    icon = Icons.Default.Timer,
                    onClick = { viewModel.navigateTo(Screen.CONTENT_MOCKS_OVERVIEW) }
                )
            }
            item {
                ContentOverviewBannerCard(
                    title = "Study Notes",
                    totalCount = regularNotes.size,
                    subtitle = "Subject & Chapter-wise Overview",
                    icon = Icons.Default.MenuBook,
                    onClick = { viewModel.navigateTo(Screen.CONTENT_NOTES_OVERVIEW) }
                )
            }
            item {
                ContentOverviewBannerCard(
                    title = "Current Affairs",
                    totalCount = currentAffairs.size,
                    subtitle = "Current Affairs Overview",
                    icon = Icons.Default.Newspaper,
                    onClick = { viewModel.navigateTo(Screen.CONTENT_CURRENT_AFFAIRS_OVERVIEW) }
                )
            }
        }
    }
}

@Composable
fun ContentOverviewBannerCard(
    title: String,
    totalCount: Int,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
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
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Total: $totalCount",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
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
}
