import re

def rewrite():
    with open("app/src/main/java/com/example/ui/screens/ContentQuestionsOverviewScreen.kt", "r") as f:
        content = f.read()

    new_content = """package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.JuktiTopAppBar
import com.example.ui.viewmodel.JuktiViewModel
import com.example.ui.viewmodel.Screen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContentQuestionsOverviewScreen(viewModel: JuktiViewModel) {
    val examsList by viewModel.examsList.collectAsState()
    val questions by viewModel.questions.collectAsState()
    val selectedTargetExam by viewModel.selectedExam.collectAsState()
    val selectedSubject by viewModel.selectedSubject.collectAsState()

    val examOptions = remember(examsList, questions) {
        val examsFromQuestions = questions.map { it.examCategory }.filter { it.isNotBlank() }.flatMap { it.split(",") }.map { it.trim() }
        val examsFromDb = examsList.map { it.title }
        val allExams = (examsFromQuestions + examsFromDb).distinct().sorted()
        listOf("All Exams") + allExams
    }
    
    var examExpanded by remember { mutableStateOf(false) }

    val subjectsList = remember(questions, selectedTargetExam) {
        val filtered = if (selectedTargetExam == "All Exams") {
            questions
        } else {
            questions.filter { it.examCategory.contains(selectedTargetExam, ignoreCase = true) }
        }
        val subjs = filtered.map { com.example.data.repository.normalizeSubjectName(it.subject) }.filter { it.isNotBlank() }.distinct().sorted()
        if (subjs.isEmpty()) listOf("All Subjects") else subjs
    }
    
    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(subjectsList) {
        if (selectedSubject !in subjectsList && subjectsList.isNotEmpty()) {
            viewModel.setSubjectFilter(subjectsList.first())
        }
    }

    val chapterStatsResults by remember(selectedSubject, selectedTargetExam) {
        viewModel.getChapterStatsByExam(selectedSubject, selectedTargetExam)
    }.collectAsState(initial = emptyList())

    val chapterStats = remember(chapterStatsResults) {
        chapterStatsResults.map { 
            ChapterStat(it.chapter, it.total, it.easy, it.medium, it.hard)
        }.sortedByDescending { it.total }
    }

    val hasQuestions = chapterStats.isNotEmpty()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedChapterStat by remember { mutableStateOf<ChapterStat?>(null) }
    val scope = rememberCoroutineScope()

    val totalCount = chapterStats.sumOf { it.total }
    val totalEasy = chapterStats.sumOf { it.easy }
    val totalMedium = chapterStats.sumOf { it.medium }
    val totalHard = chapterStats.sumOf { it.hard }

    Scaffold(
        topBar = {
            JuktiTopAppBar(
                title = "Questions Overview",
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
            ExposedDropdownMenuBox(
                expanded = examExpanded,
                onExpandedChange = { examExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedTargetExam,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Select Exam") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = examExpanded) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = examExpanded,
                    onDismissRequest = { examExpanded = false }
                ) {
                    examOptions.forEach { exam ->
                        DropdownMenuItem(
                            text = { Text(exam) },
                            onClick = {
                                viewModel.setExamFilter(exam)
                                examExpanded = false
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it }
            ) {
                OutlinedTextField(
                    value = selectedSubject,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Select Subject") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    subjectsList.forEach { subj ->
                        DropdownMenuItem(
                            text = { Text(subj) },
                            onClick = {
                                viewModel.setSubjectFilter(subj)
                                expanded = false
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            // Summary Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Total Questions: $totalCount", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Easy: $totalEasy", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                        Text("Medium: $totalMedium", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.SemiBold)
                        Text("Hard: $totalHard", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth().weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Chapter", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Text("Total", fontWeight = FontWeight.Bold, modifier = Modifier.width(48.dp))
                        Text("E", fontWeight = FontWeight.Bold, modifier = Modifier.width(32.dp))
                        Text("M", fontWeight = FontWeight.Bold, modifier = Modifier.width(32.dp))
                        Text("H", fontWeight = FontWeight.Bold, modifier = Modifier.width(32.dp))
                        Spacer(modifier = Modifier.width(32.dp))
                    }
                    HorizontalDivider()
                    
                    if (!hasQuestions) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No questions found for this selection.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(chapterStats) { stat ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { 
                                            viewModel.setExamFilter(selectedTargetExam)
                                            viewModel.setSubjectFilter(selectedSubject)
                                            viewModel.setChapterFilter(stat.chapter)
                                            viewModel.setSearchQuery("")
                                            viewModel.navigateTo(Screen.ALL_QUESTIONS)
                                        }
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        stat.chapter, 
                                        modifier = Modifier.weight(1f),
                                        maxLines = 2,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text("${stat.total}", fontWeight = FontWeight.SemiBold, modifier = Modifier.width(48.dp), style = MaterialTheme.typography.bodyMedium)
                                    Text("${stat.easy}", modifier = Modifier.width(32.dp), style = MaterialTheme.typography.bodySmall)
                                    Text("${stat.medium}", modifier = Modifier.width(32.dp), style = MaterialTheme.typography.bodySmall)
                                    Text("${stat.hard}", modifier = Modifier.width(32.dp), style = MaterialTheme.typography.bodySmall)
                                    Text("View \u2192", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall, modifier = Modifier.width(32.dp))
                                }
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            }
                        }
                    }
                }
            }
        }
    }
}

data class ChapterStat(
    val chapter: String,
    var total: Int = 0,
    var easy: Int = 0,
    var medium: Int = 0,
    var hard: Int = 0
)
"""
    with open("app/src/main/java/com/example/ui/screens/ContentQuestionsOverviewScreen.kt", "w") as f:
        f.write(new_content)

rewrite()
