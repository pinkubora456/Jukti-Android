package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.JuktiTopAppBar
import com.example.ui.viewmodel.JuktiViewModel
import com.example.ui.viewmodel.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContentMocksOverviewScreen(viewModel: JuktiViewModel) {
    val allMocks by viewModel.mockTests.collectAsState()
    val allSubjectsChapters by viewModel.allSubjectsChapters.collectAsState()

    // Filters Options
    val mockTypeOptions = listOf("All", "Full Length", "Subject", "Chapter")
    val examOptions = remember(allMocks) {
        val exams = allMocks.flatMap { it.category.split(",") }.map { it.trim() }.filter { it.isNotBlank() }.distinct().sorted()
        listOf("All Exams") + exams
    }
    val subjectOptions = remember(allSubjectsChapters) {
        val subjects = allSubjectsChapters.map { it.subject }.distinct().sorted()
        listOf("All Subjects") + subjects
    }

    // Selected Filters
    var selectedMockType by remember { mutableStateOf("All") }
    var selectedTargetExam by remember { mutableStateOf("All Exams") }
    var selectedSubject by remember { mutableStateOf("All Subjects") }
    var selectedChapter by remember { mutableStateOf("All Chapters") }

    // Dynamic Chapter Options based on Selected Subject
    val chapterOptions = remember(selectedSubject, allSubjectsChapters) {
        if (selectedSubject == "All Subjects") {
            listOf("All Chapters")
        } else {
            val chapters = allSubjectsChapters.filter { it.subject == selectedSubject }.map { it.chapter }.distinct().sorted()
            listOf("All Chapters") + chapters
        }
    }

    // If subject changes and the new chapter options don't contain the selected chapter, reset chapter
    LaunchedEffect(selectedSubject) {
        if (!chapterOptions.contains(selectedChapter)) {
            selectedChapter = "All Chapters"
        }
    }

    // Filter Logic
    val filteredMocks = remember(allMocks, selectedMockType, selectedTargetExam, selectedSubject, selectedChapter) {
        allMocks.filter { mock ->
            val typeMatches = when (selectedMockType) {
                "Full Length" -> mock.testType.equals("Full-Length", ignoreCase = true)
                "Subject" -> mock.testType.equals("Subject-wise", ignoreCase = true)
                "Chapter" -> mock.testType.equals("Chapter-wise", ignoreCase = true)
                else -> true
            }

            val examMatches = if (selectedTargetExam == "All Exams") {
                true
            } else {
                val mockExams = mock.category.split(",").map { it.trim() }
                mockExams.contains(selectedTargetExam)
            }

            val subjectMatches = if (selectedSubject == "All Subjects") {
                true
            } else {
                if (mock.testType.equals("Subject-wise", ignoreCase = true)) {
                    mock.subjectOrChapter.equals(selectedSubject, ignoreCase = true)
                } else if (mock.testType.equals("Chapter-wise", ignoreCase = true)) {
                    mock.subjectOrChapter.startsWith("$selectedSubject||", ignoreCase = true)
                } else {
                    mock.subjectOrChapter.equals(selectedSubject, ignoreCase = true) || mock.subjectOrChapter.startsWith("$selectedSubject||", ignoreCase = true)
                }
            }

            val chapterMatches = if (selectedChapter == "All Chapters" || selectedSubject == "All Subjects") {
                true
            } else {
                if (mock.testType.equals("Chapter-wise", ignoreCase = true)) {
                    val expected = "$selectedSubject||$selectedChapter"
                    mock.subjectOrChapter.equals(expected, ignoreCase = true)
                } else {
                    false
                }
            }

            typeMatches && examMatches && subjectMatches && chapterMatches
        }
    }

    Scaffold(
        topBar = {
            JuktiTopAppBar(
                title = "Mock Tests Overview",
                onBackClick = { viewModel.navigateTo(Screen.CONTENT_OVERVIEW) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Total Mock Tests: ${allMocks.size}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                TextButton(
                    onClick = {
                        selectedMockType = "All"
                        selectedTargetExam = "All Exams"
                        selectedSubject = "All Subjects"
                        selectedChapter = "All Chapters"
                    },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("Clear Filters")
                }
            }

            // Mock Type Summary
            if (selectedMockType == "All") {
                val fullLengthCount = allMocks.count { it.testType.equals("Full-Length", ignoreCase = true) }
                val subjectCount = allMocks.count { it.testType.equals("Subject-wise", ignoreCase = true) }
                val chapterCount = allMocks.count { it.testType.equals("Chapter-wise", ignoreCase = true) }
                
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Text("FL: $fullLengthCount", style = MaterialTheme.typography.labelMedium)
                        Text("Sub: $subjectCount", style = MaterialTheme.typography.labelMedium)
                        Text("Chap: $chapterCount", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            // Filters
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.weight(1f)) {
                    FilterDropdownMenu(
                        label = "Mock Type",
                        options = mockTypeOptions,
                        selectedOption = selectedMockType,
                        onOptionSelected = { selectedMockType = it }
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    FilterDropdownMenu(
                        label = "Target Exam",
                        options = examOptions,
                        selectedOption = selectedTargetExam,
                        onOptionSelected = { selectedTargetExam = it }
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.weight(1f)) {
                    FilterDropdownMenu(
                        label = "Subject",
                        options = subjectOptions,
                        selectedOption = selectedSubject,
                        onOptionSelected = { selectedSubject = it }
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    FilterDropdownMenu(
                        label = "Chapter",
                        options = chapterOptions,
                        selectedOption = selectedChapter,
                        onOptionSelected = { selectedChapter = it },
                        enabled = selectedSubject != "All Subjects",
                        placeholder = if (selectedSubject == "All Subjects") "«Select a subject first»" else ""
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Showing ${filteredMocks.size} Mock Tests",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp),
                color = MaterialTheme.colorScheme.primary
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(filteredMocks) { mock ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = mock.titleEn,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            
                            val meta1 = when {
                                mock.testType.equals("Full-Length", ignoreCase = true) -> "Full Length · ${mock.category}"
                                mock.testType.equals("Subject-wise", ignoreCase = true) -> "Subject · ${mock.subjectOrChapter}"
                                mock.testType.equals("Chapter-wise", ignoreCase = true) -> {
                                    val parts = mock.subjectOrChapter.split("||")
                                    if (parts.size == 2) "Chapter · ${parts[0]} → ${parts[1]}" else "Chapter · ${mock.subjectOrChapter}"
                                }
                                else -> "${mock.testType} · ${mock.category}"
                            }
                            Text(text = meta1, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "${mock.totalQuestions} Questions", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterDropdownMenu(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    enabled: Boolean = true,
    placeholder: String = ""
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = if (enabled) expanded else false,
        onExpandedChange = { if (enabled) expanded = it }
    ) {
        OutlinedTextField(
            value = if (!enabled && placeholder.isNotBlank()) placeholder else selectedOption,
            onValueChange = {},
            readOnly = true,
            label = { Text(label, maxLines = 1) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            enabled = enabled,
            singleLine = true,
            textStyle = MaterialTheme.typography.bodySmall
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option, style = MaterialTheme.typography.bodyMedium) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
