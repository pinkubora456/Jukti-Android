package com.example.ui.screens

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.QuestionEntity
import com.example.ui.components.EditQuestionDialog
import com.example.ui.viewmodel.JuktiViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MockQuestionsScreen(viewModel: JuktiViewModel) {
    val context = LocalContext.current
    val questions by viewModel.mockOnlyQuestions.collectAsState()
    
    val selectedTargetExam by viewModel.selectedExam.collectAsState()
    val selectedSubject by viewModel.selectedSubject.collectAsState()
    val selectedChapter by viewModel.selectedChapter.collectAsState()
    
    var searchQuery by remember { mutableStateOf("") }
    var showOnlyIssues by remember { mutableStateOf(false) }
    var editingQuestion by remember { mutableStateOf<QuestionEntity?>(null) }
    var questionToDelete by remember { mutableStateOf<QuestionEntity?>(null) }
    var showBulkDeleteConfirm by remember { mutableStateOf(false) }
    var showMoveDialog by remember { mutableStateOf(false) }
    var showPublishConfirm by remember { mutableStateOf(false) }
    var selectedQuestionIds by remember { mutableStateOf<Set<Long>>(emptySet()) }

    val issueCount = remember(questions) {
        questions.count { q ->
            q.questionEn.isBlank() || q.subject.isBlank() || q.topic.isBlank() ||
            q.optionAEn.isBlank() || q.optionBEn.isBlank() || q.optionCEn.isBlank() || q.optionDEn.isBlank() ||
            q.correctOptionIndex !in 0..3
        }
    }

    val filteredQuestions = remember(questions, searchQuery, selectedTargetExam, selectedSubject, selectedChapter, showOnlyIssues) {
        questions.filter { q ->
            val hasIssue = q.questionEn.isBlank() || q.subject.isBlank() || q.topic.isBlank() ||
                           q.optionAEn.isBlank() || q.optionBEn.isBlank() || q.optionCEn.isBlank() || q.optionDEn.isBlank() ||
                           q.correctOptionIndex !in 0..3
            val matchesIssue = !showOnlyIssues || hasIssue
            val matchesExam = selectedTargetExam == "All Exams" || q.examCategory.contains(selectedTargetExam, ignoreCase = true)
            
            val normSubj = com.example.data.repository.normalizeSubjectName(q.subject)
            val matchesSubject = selectedSubject == "All Subjects" || 
                  normSubj.equals(selectedSubject, ignoreCase = true) ||
                  q.subject.equals(selectedSubject, ignoreCase = true)
                  
            val normChapter = com.example.data.repository.normalizeChapterName(q.topic, q.subject)
            val matchesChapter = selectedChapter == "All Chapters" || 
                 normChapter.equals(selectedChapter, ignoreCase = true) ||
                 q.topic.equals(selectedChapter, ignoreCase = true)
                 
            val matchesSearch = searchQuery.isBlank() ||
                    q.questionEn.contains(searchQuery, ignoreCase = true) ||
                    q.questionAs.contains(searchQuery, ignoreCase = true) ||
                    q.subject.contains(searchQuery, ignoreCase = true) ||
                    normSubj.contains(searchQuery, ignoreCase = true) ||
                    q.topic.contains(searchQuery, ignoreCase = true)
            matchesIssue && matchesExam && matchesSubject && matchesChapter && matchesSearch
        }
    }

    Scaffold(
        topBar = {
            com.example.ui.components.JuktiTopAppBar(
                title = "Mock Questions (${filteredQuestions.size})",
                onBackClick = { viewModel.navigateTo(com.example.ui.viewmodel.Screen.MANAGE_MOCK) },
                actions = {
                    IconButton(onClick = { viewModel.exportQuestionsCsv(context) }) {
                        Icon(Icons.Default.Download, contentDescription = "Export CSV")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))
            
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search questions, subject, topic...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Filter Info (since the filters were set from Overview)
            if (selectedTargetExam != "All Exams" || selectedSubject != "All Subjects" || selectedChapter != "All Chapters") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Active Filters:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSecondaryContainer)
                        if (selectedTargetExam != "All Exams") Text("Exam: $selectedTargetExam", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        if (selectedSubject != "All Subjects") Text("Subject: $selectedSubject", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        if (selectedChapter != "All Chapters") Text("Chapter: $selectedChapter", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Clear Filters", 
                            color = MaterialTheme.colorScheme.primary, 
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.clickable {
                                viewModel.setExamFilter("All Exams")
                                viewModel.setSubjectFilter("All Subjects")
                                viewModel.setChapterFilter("All Chapters")
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (issueCount > 0) {
                FilterChip(
                    selected = showOnlyIssues,
                    onClick = { showOnlyIssues = !showOnlyIssues },
                    label = { Text("⚠️ Questions With Issues ($issueCount)") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.errorContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Bulk Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = selectedQuestionIds.isNotEmpty() && selectedQuestionIds.size == filteredQuestions.size,
                        onCheckedChange = { checked ->
                            if (checked) {
                                selectedQuestionIds = filteredQuestions.map { it.id }.toSet()
                            } else {
                                selectedQuestionIds = emptySet()
                            }
                        }
                    )
                    Text(if (selectedQuestionIds.isEmpty()) "Select All" else "${selectedQuestionIds.size} Selected")
                }
                
                if (selectedQuestionIds.isNotEmpty()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedButton(
                            onClick = {
                                showBulkDeleteConfirm = true
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Delete (${selectedQuestionIds.size})")
                        }
                        Button(onClick = { showMoveDialog = true }) {
                            Text("Move")
                        }
                        Button(onClick = { showPublishConfirm = true }) {
                            Text("Publish to Q-Bank")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (filteredQuestions.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No questions found.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredQuestions, key = { it.id }) { question ->
                        val isSelected = selectedQuestionIds.contains(question.id)
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable {
                                if (isSelected) selectedQuestionIds = selectedQuestionIds - question.id
                                else selectedQuestionIds = selectedQuestionIds + question.id
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                val hasIssue = question.questionEn.isBlank() || question.subject.isBlank() || question.topic.isBlank() ||
                                               question.optionAEn.isBlank() || question.optionBEn.isBlank() || question.optionCEn.isBlank() || question.optionDEn.isBlank() ||
                                               question.correctOptionIndex !in 0..3
                                if (hasIssue) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.errorContainer,
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = buildString {
                                                    append("Issue: ")
                                                    if (question.questionEn.isBlank()) append("Missing Text; ")
                                                    if (question.subject.isBlank()) append("Missing Subject; ")
                                                    if (question.topic.isBlank()) append("Missing Topic; ")
                                                    if (question.optionAEn.isBlank() || question.optionBEn.isBlank() || question.optionCEn.isBlank() || question.optionDEn.isBlank()) append("Missing Options; ")
                                                    if (question.correctOptionIndex !in 0..3) append("Invalid Correct Option;")
                                                },
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onErrorContainer
                                            )
                                        }
                                    }
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = isSelected,
                                        onCheckedChange = { checked ->
                                            if (checked) selectedQuestionIds = selectedQuestionIds + question.id
                                            else selectedQuestionIds = selectedQuestionIds - question.id
                                        }
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (question.isPremium) {
                                            Icon(
                                                imageVector = Icons.Default.Star,
                                                contentDescription = "Premium",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                        }
                                        Text(
                                            text = question.subject,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.weight(1f))
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = MaterialTheme.colorScheme.secondaryContainer
                                        ) {
                                            Text(
                                                text = question.difficulty.ifBlank { "Medium" },
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(4.dp))
                                        IconButton(
                                            onClick = { editingQuestion = question },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Edit Question",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(4.dp))
                                        IconButton(
                                            onClick = { viewModel.deleteQuestion(question) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete Question",
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                }
                                if (question.topic.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Topic: ${question.topic}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                com.example.ui.components.QuestionTypeBadge(
                                    questionType = question.questionType,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                                Text(
                                    text = question.questionEn,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (question.questionAs.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = question.questionAs,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                val optionsEn = listOf(question.optionAEn, question.optionBEn, question.optionCEn, question.optionDEn)
                                val optionsAs = listOf(question.optionAAs, question.optionBAs, question.optionCAs, question.optionDAs)
                                optionsEn.forEachIndexed { index, opt ->
                                    if (opt.isNotBlank()) {
                                        val isCorrect = question.correctOptionIndex == index
                                        val optAs = optionsAs.getOrNull(index) ?: ""
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (isCorrect) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 2.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "${('A' + index)}. ",
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isCorrect) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                                )
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = opt,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = if (isCorrect) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                                    )
                                                    if (optAs.isNotBlank()) {
                                                        Text(
                                                            text = optAs,
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                }
                                                if (isCorrect) {
                                                    Icon(
                                                        imageVector = Icons.Default.CheckCircle,
                                                        contentDescription = "Correct Answer",
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showMoveDialog) {
        BulkMoveQuestionsDialog(
            viewModel = viewModel,
            selectedCount = selectedQuestionIds.size,
            onDismiss = { showMoveDialog = false },
            onConfirm = { destExam, destSubj, destChap ->
                val selectedQs = questions.filter { it.id in selectedQuestionIds }
                viewModel.bulkMoveQuestions(
                    questionsToUpdate = selectedQs,
                    targetExam = destExam,
                    targetSubject = destSubj,
                    targetChapter = destChap
                ) { success, _ ->
                    if (success) {
                        selectedQuestionIds = emptySet()
                        showMoveDialog = false
                    }
                }
            }
        )
    }

    if (showPublishConfirm) {
        AlertDialog(
            onDismissRequest = { showPublishConfirm = false },
            title = { Text("Publish to Q-Bank") },
            text = { Text("Are you sure you want to publish ${selectedQuestionIds.size} mock question(s) to the main Question Bank? They will become accessible across all practice modes.") },
            confirmButton = {
                Button(onClick = {
                    val selectedQs = questions.filter { it.id in selectedQuestionIds }
                    val updatedQs = selectedQs.map { it.copy(status = "ACTIVE") }
                    viewModel.bulkUpdateQuestions(updatedQs) { success, _ ->
                        if (success) {
                            selectedQuestionIds = emptySet()
                            showPublishConfirm = false
                        }
                    }
                }) {
                    Text("Publish")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPublishConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    editingQuestion?.let { question ->
        EditQuestionDialog(
            question = question,
            onDismiss = { editingQuestion = null },
            onSave = { updated ->
                viewModel.updateQuestion(updated)
                editingQuestion = null
            }
        )
    }

    if (questionToDelete != null) {
        AlertDialog(
            onDismissRequest = { questionToDelete = null },
            title = { Text("Confirm Delete") },
            text = { Text("Are you sure you want to delete this question?") },
            confirmButton = {
                Button(onClick = {
                    viewModel.deleteQuestion(questionToDelete!!)
                    questionToDelete = null
                }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { questionToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showBulkDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showBulkDeleteConfirm = false },
            title = { Text("Confirm Bulk Delete") },
            text = { Text("Are you sure you want to delete these ${selectedQuestionIds.size} questions?") },
            confirmButton = {
                Button(onClick = {
                    val selectedQs = questions.filter { it.id in selectedQuestionIds }
                    selectedQs.forEach { q ->
                        viewModel.deleteQuestion(q)
                    }
                    selectedQuestionIds = emptySet()
                    showBulkDeleteConfirm = false
                }) {
                    Text("Delete All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBulkDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

