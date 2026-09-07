import os

screen_code = """
package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.GuidanceEntity
import com.example.ui.components.SafeOutlinedTextField
import com.example.ui.viewmodel.JuktiViewModel
import com.example.ui.viewmodel.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageGuidanceScreen(viewModel: JuktiViewModel) {
    val examsList by viewModel.examsList.collectAsState()
    val allSubjectsChapters by viewModel.allSubjectsChapters.collectAsState()
    val allGuidance by viewModel.allGuidance.collectAsState()

    var selectedExam by remember { mutableStateOf<String?>(null) }
    var selectedSubject by remember { mutableStateOf<String?>(null) }

    var examDropdownExpanded by remember { mutableStateOf(false) }
    var subjectDropdownExpanded by remember { mutableStateOf(false) }

    // Derived list of unique subjects
    val uniqueSubjects = remember(allSubjectsChapters) {
        allSubjectsChapters.map { it.subject }.distinct().sorted()
    }

    // Derived list of chapters for the selected subject
    val chaptersForSubject = remember(allSubjectsChapters, selectedSubject) {
        if (selectedSubject == null) emptyList()
        else allSubjectsChapters.filter { it.subject == selectedSubject }.map { it.chapter }.distinct().sorted()
    }

    Scaffold(
        topBar = {
            com.example.ui.components.JuktiTopAppBar(
                title = "Manage Guidance",
                onBackClick = { viewModel.navigateTo(Screen.WORKSPACE) }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Exam Selection
            ExposedDropdownMenuBox(
                expanded = examDropdownExpanded,
                onExpandedChange = { examDropdownExpanded = !examDropdownExpanded }
            ) {
                SafeOutlinedTextField(
                    value = selectedExam ?: "Select Exam",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Exam") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = examDropdownExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = examDropdownExpanded,
                    onDismissRequest = { examDropdownExpanded = false }
                ) {
                    examsList.forEach { exam ->
                        DropdownMenuItem(
                            text = { Text(exam.title) },
                            onClick = {
                                selectedExam = exam.title
                                examDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            // 2. Subject Selection
            if (selectedExam != null) {
                ExposedDropdownMenuBox(
                    expanded = subjectDropdownExpanded,
                    onExpandedChange = { subjectDropdownExpanded = !subjectDropdownExpanded }
                ) {
                    SafeOutlinedTextField(
                        value = selectedSubject ?: "Select Subject",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Subject") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjectDropdownExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = subjectDropdownExpanded,
                        onDismissRequest = { subjectDropdownExpanded = false }
                    ) {
                        uniqueSubjects.forEach { subject ->
                            DropdownMenuItem(
                                text = { Text(subject) },
                                onClick = {
                                    selectedSubject = subject
                                    subjectDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // 3. Chapters for Selected Subject
            if (selectedExam != null && selectedSubject != null) {
                if (chaptersForSubject.isEmpty()) {
                    Text("No chapters found for this subject.", style = MaterialTheme.typography.bodyMedium)
                } else {
                    Text(
                        text = "Chapters in $selectedSubject",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(chaptersForSubject) { chapter ->
                            val currentGuidance = allGuidance.find {
                                it.exam == selectedExam && it.subject == selectedSubject && it.chapter == chapter
                            } ?: GuidanceEntity(exam = selectedExam!!, subject = selectedSubject!!, chapter = chapter)

                            GuidanceChapterCard(
                                guidance = currentGuidance,
                                onSave = { updatedGuidance ->
                                    viewModel.saveGuidance(updatedGuidance)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GuidanceChapterCard(
    guidance: GuidanceEntity,
    onSave: (GuidanceEntity) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    var pyqFocus by remember(guidance) { mutableStateOf(guidance.pyqFocus) }
    var focusTopics by remember(guidance) { mutableStateOf(guidance.focusTopics) }
    var prepStrategy by remember(guidance) { mutableStateOf(guidance.prepStrategy) }
    var strengthWeakness by remember(guidance) { mutableStateOf(guidance.strengthWeakness) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = guidance.chapter,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand"
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SafeOutlinedTextField(
                        value = pyqFocus,
                        onValueChange = { pyqFocus = it },
                        label = { Text("PYQ Focus") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )

                    SafeOutlinedTextField(
                        value = focusTopics,
                        onValueChange = { focusTopics = it },
                        label = { Text("Focus Topics") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )

                    SafeOutlinedTextField(
                        value = prepStrategy,
                        onValueChange = { prepStrategy = it },
                        label = { Text("Preparation Strategy") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )

                    SafeOutlinedTextField(
                        value = strengthWeakness,
                        onValueChange = { strengthWeakness = it },
                        label = { Text("Strength & Weakness") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )

                    Button(
                        onClick = {
                            val updated = guidance.copy(
                                pyqFocus = pyqFocus,
                                focusTopics = focusTopics,
                                prepStrategy = prepStrategy,
                                strengthWeakness = strengthWeakness,
                                updatedAt = System.currentTimeMillis()
                            )
                            onSave(updated)
                            isExpanded = false
                        },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}
"""

with open("app/src/main/java/com/example/ui/screens/ManageGuidanceScreen.kt", "w") as f:
    f.write(screen_code)

print("Created ManageGuidanceScreen.kt")
