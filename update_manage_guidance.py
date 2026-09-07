import os

screen_code = """
package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var selectedSubject by remember { mutableStateOf<String?>(null) }

    var examDropdownExpanded by remember { mutableStateOf(false) }
    var subjectDropdownExpanded by remember { mutableStateOf(false) }

    val categories = listOf(
        "PYQ Focus" to Icons.Default.History,
        "Focus Topics" to Icons.Default.TrackChanges,
        "Preparation Strategy" to Icons.Default.Lightbulb,
        "Strength & Weakness" to Icons.Default.FitnessCenter
    )

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
                title = if (selectedCategory != null) "Manage ${selectedCategory}" else "Manage Guidance",
                onBackClick = {
                    if (selectedCategory != null) {
                        // Go back to category selection
                        selectedCategory = null
                        selectedSubject = null
                    } else {
                        // Go back to workspace
                        viewModel.navigateTo(Screen.WORKSPACE)
                    }
                }
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
            if (selectedCategory == null) {
                // LEVEL 1: Select Exam and Category
                Text(
                    text = "1. Select Exam",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
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

                if (selectedExam != null) {
                    Text(
                        text = "2. Select Guidance Category",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    
                    categories.forEach { (catName, icon) ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedCategory = catName },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = icon, 
                                    contentDescription = null, 
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    text = catName,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            } else {
                // LEVEL 2: Inside a specific Category (Subject -> Chapter -> Field)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp), 
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Exam: ${selectedExam!!}", fontWeight = FontWeight.Bold)
                    }
                }

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

                if (selectedSubject != null) {
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

                                GuidanceSingleFieldChapterCard(
                                    guidance = currentGuidance,
                                    category = selectedCategory!!,
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
}

@Composable
fun GuidanceSingleFieldChapterCard(
    guidance: GuidanceEntity,
    category: String,
    onSave: (GuidanceEntity) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    var fieldValue by remember(guidance, category) {
        mutableStateOf(
            when (category) {
                "PYQ Focus" -> guidance.pyqFocus
                "Focus Topics" -> guidance.focusTopics
                "Preparation Strategy" -> guidance.prepStrategy
                "Strength & Weakness" -> guidance.strengthWeakness
                else -> ""
            }
        )
    }

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
                        value = fieldValue,
                        onValueChange = { fieldValue = it },
                        label = { Text(category) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )

                    Button(
                        onClick = {
                            val updated = when (category) {
                                "PYQ Focus" -> guidance.copy(pyqFocus = fieldValue, updatedAt = System.currentTimeMillis())
                                "Focus Topics" -> guidance.copy(focusTopics = fieldValue, updatedAt = System.currentTimeMillis())
                                "Preparation Strategy" -> guidance.copy(prepStrategy = fieldValue, updatedAt = System.currentTimeMillis())
                                "Strength & Weakness" -> guidance.copy(strengthWeakness = fieldValue, updatedAt = System.currentTimeMillis())
                                else -> guidance
                            }
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

print("Updated ManageGuidanceScreen.kt")
