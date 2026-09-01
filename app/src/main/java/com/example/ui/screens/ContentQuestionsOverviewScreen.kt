package com.example.ui.screens

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
    val allSubjectsChapters by viewModel.allSubjectsChapters.collectAsState()
    
    val examOptions = remember(examsList) {
        val exams = examsList.map { it.title }.distinct().sorted()
        listOf("All Exams") + exams
    }
    var selectedTargetExam by remember { mutableStateOf("All Exams") }
    var examExpanded by remember { mutableStateOf(false) }

    val subjectsList = remember(allSubjectsChapters) {
        allSubjectsChapters.map { it.subject }.distinct().sorted()
    }
    var selectedSubject by remember { mutableStateOf(subjectsList.firstOrNull() ?: "") }
    var expanded by remember { mutableStateOf(false) }

    if (selectedSubject.isNotEmpty() && !subjectsList.contains(selectedSubject)) {
        selectedSubject = subjectsList.firstOrNull() ?: ""
    }

    val chapterStatsResults by remember(selectedSubject, selectedTargetExam) {
        viewModel.getChapterStatsByExam(selectedSubject, selectedTargetExam)
    }.collectAsState(initial = emptyList())

    val chapterStats = remember(chapterStatsResults, allSubjectsChapters, selectedSubject) {
        val statsMap = mutableMapOf<String, ChapterStat>()
        allSubjectsChapters.filter { it.subject == selectedSubject }.forEach { sc ->
            statsMap[sc.chapter] = ChapterStat(sc.chapter)
        }
        chapterStatsResults.forEach { result ->
            val stat = statsMap[result.chapter] ?: ChapterStat(result.chapter)
            stat.total += result.total
            stat.easy += result.easy
            stat.medium += result.medium
            stat.hard += result.hard
            statsMap[result.chapter] = stat
        }
        statsMap.values.toList().sortedByDescending { it.total }
    }

    val hasQuestions = remember(chapterStats) {
        chapterStats.sumOf { it.total } > 0
    }

    var selectedChapterStat by remember { mutableStateOf<ChapterStat?>(null) }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

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
                    label = { Text("Target Exam") },
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
                                selectedTargetExam = exam
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
                                selectedSubject = subj
                                expanded = false
                            }
                        )
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
                    }
                    HorizontalDivider()
                    
                    if (!hasQuestions) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No questions available for this exam.", color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                                            selectedChapterStat = stat 
                                            scope.launch { sheetState.show() }
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
                                }
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            }
                        }
                    }
                }
            }
        }
    }

    if (selectedChapterStat != null) {
        ModalBottomSheet(
            onDismissRequest = { selectedChapterStat = null },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = selectedChapterStat!!.chapter,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("Total Questions: ${selectedChapterStat!!.total}", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                
                Text("Easy: ${selectedChapterStat!!.easy}", color = MaterialTheme.colorScheme.primary)
                Text("Medium: ${selectedChapterStat!!.medium}", color = MaterialTheme.colorScheme.secondary)
                Text("Hard: ${selectedChapterStat!!.hard}", color = MaterialTheme.colorScheme.error)
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = {
                        viewModel.setSubjectFilter(selectedSubject)
                        viewModel.setSearchQuery(selectedChapterStat!!.chapter)
                        viewModel.navigateTo(Screen.ALL_QUESTIONS)
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            selectedChapterStat = null
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("View Questions")
                }
                Spacer(modifier = Modifier.height(32.dp))
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
