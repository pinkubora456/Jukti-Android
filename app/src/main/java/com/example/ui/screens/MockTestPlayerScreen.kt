package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.*
import com.example.ui.theme.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ui.components.ReportQuestionDialog
import androidx.compose.material.icons.filled.Report
import androidx.compose.ui.unit.sp
import com.example.ui.components.BilingualText
import com.example.ui.viewmodel.AppLanguage
import com.example.ui.viewmodel.JuktiViewModel
import com.example.ui.viewmodel.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MockTestPlayerScreen(viewModel: JuktiViewModel) {
    val language by viewModel.language.collectAsState()
    val questionLanguage by viewModel.questionLanguage.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    val mockTest by viewModel.selectedMockTest.collectAsState()
    val questions by viewModel.questions.collectAsState()
    val userAnswers by viewModel.mockUserAnswers.collectAsState()
    val markedForReview by viewModel.mockMarkedForReview.collectAsState()
    val timeRemainingSeconds by viewModel.mockTimeRemainingSeconds.collectAsState()

    var currentQuestionIndex by remember { mutableStateOf(0) }
    var showPaletteSheet by remember { mutableStateOf(false) }
    var showSubmitConfirmDialog by remember { mutableStateOf(false) }
    var showExitConfirmDialog by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }

    val activeMockQuestions = remember(mockTest, questions) {
        val mock = mockTest ?: return@remember emptyList()
        var filtered = questions
        
        if (mock.testType == "Subject-wise") {
            filtered = filtered.filter { it.subject.equals(mock.subjectOrChapter, ignoreCase = true) }
        } else if (mock.testType == "Chapter-wise") {
            val parts = mock.subjectOrChapter.split("||")
            val subj = parts.getOrNull(0) ?: ""
            val chap = parts.getOrNull(1) ?: ""
            filtered = filtered.filter { 
                it.subject.equals(subj, ignoreCase = true) && it.topic.equals(chap, ignoreCase = true) 
            }
        } else {
            // Full-Length or Exam-wise mock functionality
            if (mock.questionIds.isNotBlank()) {
                val ids = mock.questionIds.split(",").mapNotNull { it.trim().toLongOrNull() }.toSet()
                filtered = filtered.filter { it.id in ids }
            }
        }
        
        filtered.shuffled().take(mock.totalQuestions)
    }

    val currentQuestion = activeMockQuestions.getOrNull(currentQuestionIndex)

    val minutesLeft = timeRemainingSeconds / 60
    val secondsLeft = timeRemainingSeconds % 60

    if (showExitConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showExitConfirmDialog = false },
            title = { Text("Exit Mock Test?") },
            text = { Text("Your current test progress will not be saved.") },
            confirmButton = {
                Button(
                    onClick = {
                        showExitConfirmDialog = false
                        viewModel.navigateTo(Screen.MOCK_TESTS)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Exit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showSubmitConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showSubmitConfirmDialog = false },
            title = { Text("Submit Mock Test?") },
            text = {
                Text(
                    text = "Answered: ${userAnswers.size} / ${activeMockQuestions.size}\nMarked for Review: ${markedForReview.size}"
                )
            },
            confirmButton = {
                Button(onClick = {
                    showSubmitConfirmDialog = false
                    viewModel.submitCurrentMockTest()
                }) {
                    Text("Submit Test")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSubmitConfirmDialog = false }) {
                    Text("Continue Test")
                }
            }
        )
    }

    if (activeMockQuestions.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Icon(Icons.Default.CloudOff, contentDescription = "Offline", modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
                Text("No questions available for this mock test.", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Please check your internet connection or Refresh App Data to cache questions offline.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 32.dp))
                Button(onClick = { viewModel.navigateTo(Screen.MOCK_TESTS) }) {
                    Text("Go Back")
                }
            }
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Player Top Bar
        com.example.ui.components.JuktiTopAppBar(
            title = {
                Column {
                    BilingualText(
                        textEn = mockTest?.titleEn ?: "Mock Test",
                        textAs = mockTest?.titleAs ?: "মক টেষ্ট",
                        language = language,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Time Left: %02d:%02d".format(minutesLeft, secondsLeft),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.error
                    )
                    
                    val mockType = mockTest?.testType
                    val subjectOrChapter = mockTest?.subjectOrChapter
                    if (mockType == "Subject-wise" && !subjectOrChapter.isNullOrBlank()) {
                        Text(
                            text = "«Subject: $subjectOrChapter»",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else if (mockType == "Chapter-wise" && !subjectOrChapter.isNullOrBlank()) {
                        val parts = subjectOrChapter.split("||")
                        val subj = parts.getOrNull(0) ?: ""
                        val chap = parts.getOrNull(1) ?: ""
                        Text(
                            text = "«Subject: $subj • Chapter: $chap»",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            onBackClick = { showExitConfirmDialog = true }
        )

        Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 1.dp) {
            Column {
                // Question Language Switcher Bar with Question Palette Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { showPaletteSheet = !showPaletteSheet },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            Icons.Default.GridView,
                            contentDescription = "Question Palette",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Palette",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        FilterChip(
                            selected = questionLanguage == AppLanguage.ENGLISH,
                            onClick = { viewModel.setQuestionLanguage(AppLanguage.ENGLISH) },
                            label = { Text("EN", fontSize = 11.sp) },
                            modifier = Modifier.height(28.dp)
                        )
                        FilterChip(
                            selected = questionLanguage == AppLanguage.ASSAMESE,
                            onClick = { viewModel.setQuestionLanguage(AppLanguage.ASSAMESE) },
                            label = { Text("অসমীয়া", fontSize = 11.sp) },
                            modifier = Modifier.height(28.dp)
                        )
                        FilterChip(
                            selected = questionLanguage == AppLanguage.BOTH,
                            onClick = { viewModel.setQuestionLanguage(AppLanguage.BOTH) },
                            label = { Text("Both", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            modifier = Modifier.height(28.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }
        }

        if (showPaletteSheet) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Question Palette (${userAnswers.size}/${activeMockQuestions.size} Answered)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(6),
                        modifier = Modifier.height(140.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        itemsIndexed(activeMockQuestions) { index, _ ->
                            val isAnswered = userAnswers.containsKey(index)
                            val isReviewed = markedForReview.contains(index)

                            val cellBg = when {
                                isReviewed -> MaterialTheme.colorScheme.warning
                                isAnswered -> MaterialTheme.colorScheme.success
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }

                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(cellBg)
                                    .clickable {
                                        currentQuestionIndex = index
                                        showPaletteSheet = false
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${index + 1}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isAnswered || isReviewed) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Active Question Area
        if (currentQuestion != null) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Question ${currentQuestionIndex + 1} of ${activeMockQuestions.size}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Row {
                        IconButton(onClick = { showReportDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Report,
                                contentDescription = "Report Question",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                        TextButton(onClick = { viewModel.toggleMarkForReview(currentQuestionIndex) }) {
                            Icon(
                                imageVector = if (markedForReview.contains(currentQuestionIndex)) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (markedForReview.contains(currentQuestionIndex)) "Marked" else "Mark for Review")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                com.example.ui.components.QuestionTypeBadge(
                    questionType = currentQuestion.questionType,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                BilingualText(
                    textEn = currentQuestion.questionEn,
                    textAs = currentQuestion.questionAs,
                    language = questionLanguage,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(20.dp))

                val selectedAnswer = userAnswers[currentQuestionIndex]
                val options = listOf(
                    currentQuestion.optionAEn to currentQuestion.optionAAs,
                    currentQuestion.optionBEn to currentQuestion.optionBAs,
                    currentQuestion.optionCEn to currentQuestion.optionCAs,
                    currentQuestion.optionDEn to currentQuestion.optionDAs
                )

                options.forEachIndexed { optIndex, pair ->
                    val isSelected = (selectedAnswer == optIndex)

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                viewModel.recordMockAnswer(currentQuestionIndex, optIndex)
                            },
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { viewModel.recordMockAnswer(currentQuestionIndex, optIndex) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            BilingualText(
                                textEn = pair.first,
                                textAs = pair.second,
                                language = questionLanguage,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            // Bottom Navigation Controls
            Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 8.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { if (currentQuestionIndex > 0) currentQuestionIndex-- },
                        enabled = (currentQuestionIndex > 0)
                    ) {
                        Text("Previous")
                    }

                    Button(
                        onClick = { showSubmitConfirmDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Submit")
                    }

                    Button(
                        onClick = {
                            if (currentQuestionIndex < activeMockQuestions.size - 1) {
                                currentQuestionIndex++
                            }
                        },
                        enabled = (currentQuestionIndex < activeMockQuestions.size - 1)
                    ) {
                        Text("Next")
                    }
                }
            }
        }
        
        if (showReportDialog && currentQuestion != null) {
            ReportQuestionDialog(
                questionId = currentQuestion.id,
                onDismissRequest = { showReportDialog = false },
                onSubmitReport = { reason, details ->
                    showReportDialog = false
                    viewModel.reportQuestion(currentQuestion)
                    android.widget.Toast.makeText(context, "Question reported successfully", android.widget.Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}
