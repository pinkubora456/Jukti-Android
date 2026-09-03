package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.JuktiTopAppBar
import com.example.ui.viewmodel.JuktiViewModel
import com.example.ui.viewmodel.Screen

@Composable
fun ContentWithIssuesScreen(viewModel: JuktiViewModel) {
    val allQuestions by viewModel.questions.collectAsState()
    val allMocks by viewModel.mockTests.collectAsState()
    val allNotes by viewModel.studyNotes.collectAsState()

    val questionsWithIssues = remember(allQuestions) {
        allQuestions.map { q ->
            val issues = mutableListOf<String>()
            if (q.questionEn.isBlank()) issues.add("Missing question text")
            if (q.subject.isBlank()) issues.add("Missing subject")
            if (q.topic.isBlank()) issues.add("Missing topic")
            if (q.optionAEn.isBlank() || q.optionBEn.isBlank() || q.optionCEn.isBlank() || q.optionDEn.isBlank()) {
                issues.add("Missing options")
            }
            if (q.correctOptionIndex !in 0..3) {
                issues.add("Invalid correct option index")
            }
            Triple(q.id, "Question: ${q.questionEn.ifBlank { "Untitled Question" }} (${q.subject.ifBlank { "No Subject" }})", issues)
        }.filter { it.third.isNotEmpty() }
    }

    val mocksWithIssues = remember(allMocks) {
        allMocks.map { m ->
            val issues = mutableListOf<String>()
            if (m.titleEn.isBlank()) issues.add("Missing title")
            if (m.totalQuestions <= 0) issues.add("Invalid total questions")
            if (m.totalMarks <= 0f) issues.add("Invalid total marks")
            if (m.durationMinutes <= 0) issues.add("Invalid duration")
            Triple(m.id, "Mock Test: ${m.titleEn.ifBlank { "Untitled Mock" }} (${m.category})", issues)
        }.filter { it.third.isNotEmpty() }
    }

    val notesWithIssues = remember(allNotes) {
        allNotes.map { n ->
            val issues = mutableListOf<String>()
            if (n.titleEn.isBlank()) issues.add("Missing title")
            if (n.subject.isBlank()) issues.add("Missing subject")
            if (n.contentEn.isBlank()) issues.add("Missing content")
            Triple(n.id, "Study Note: ${n.titleEn.ifBlank { "Untitled Note" }} (${n.subject.ifBlank { "No Subject" }})", issues)
        }.filter { it.third.isNotEmpty() }
    }

    Scaffold(
        topBar = {
            JuktiTopAppBar(
                title = "Content with Issues",
                onBackClick = { viewModel.navigateTo(Screen.CONTENT_OVERVIEW) }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (questionsWithIssues.isEmpty() && mocksWithIssues.isEmpty() && notesWithIssues.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No Content with Issues Found!",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "All questions, mock tests, and study notes are fully valid.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                if (questionsWithIssues.isNotEmpty()) {
                    item {
                        Text(
                            text = "Questions with Issues (${questionsWithIssues.size})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    items(questionsWithIssues) { item ->
                        IssueItemCard(
                            title = item.second,
                            issues = item.third,
                            onFixClick = { viewModel.navigateTo(Screen.ALL_QUESTIONS) }
                        )
                    }
                }

                if (mocksWithIssues.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Mock Tests with Issues (${mocksWithIssues.size})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    items(mocksWithIssues) { item ->
                        IssueItemCard(
                            title = item.second,
                            issues = item.third,
                            onFixClick = { viewModel.navigateTo(Screen.MANAGE_MOCK) }
                        )
                    }
                }

                if (notesWithIssues.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Study Notes with Issues (${notesWithIssues.size})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    items(notesWithIssues) { item ->
                        IssueItemCard(
                            title = item.second,
                            issues = item.third,
                            onFixClick = { viewModel.navigateTo(Screen.MANAGE_STUDY_NOTES) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun IssueItemCard(
    title: String,
    issues: List<String>,
    onFixClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Issues Detected:",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(4.dp))
            issues.forEach { issue ->
                Row(
                    modifier = Modifier.padding(start = 8.dp, bottom = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "• $issue",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = onFixClick,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Review & Fix")
                }
            }
        }
    }
}
