package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.sp
import com.example.data.local.QuestionEntity
import com.example.ui.components.BilingualText
import com.example.ui.components.ReportQuestionDialog
import com.example.ui.viewmodel.AppLanguage
import com.example.ui.viewmodel.JuktiViewModel
import com.example.ui.viewmodel.Screen

/**
 * Maps raw question subject/category to user-facing display subject.
 * Requirement: Social Studies + GK are grouped together as "General Knowledge".
 * Any other subject present in the mock (e.g. Mathematics, Reasoning, English, Transport Rules, Computer, etc.)
 * is preserved and dynamically displayed.
 */
fun mapQuestionSubjectToDisplaySubject(rawSubject: String?): String {
    val trimmed = (rawSubject ?: "").trim()
    if (trimmed.isBlank()) return "General Knowledge"
    val lower = trimmed.lowercase()
    return when {
        // Group Social Studies and GK together into "General Knowledge"
        lower == "social studies" || lower == "social study" || lower == "social science" ||
        lower == "gk" || lower == "general knowledge" || lower == "general studies" ||
        lower == "general awareness" || lower == "assam gk" || lower == "static gk" ||
        lower.startsWith("social stud") || lower.startsWith("social sci") ||
        lower.contains("history") || lower.contains("geography") || lower.contains("polity") || lower.contains("constitution") || lower.contains("economy") -> "General Knowledge"

        lower == "general mathematics" || lower == "mathematics" || lower == "math" || lower == "maths" || lower == "quantitative aptitude" -> "Mathematics"

        lower == "logical reasoning & mental ability" || lower == "logical reasoning" || lower == "reasoning" || lower == "mental ability" || lower == "reasoning & mental ability" -> "Reasoning"

        lower == "general english" || lower == "english" || lower == "english language" || lower == "comprehension" -> "English"

        lower == "computer knowledge" || lower == "computer" || lower == "computer awareness" || lower == "computer science" || lower == "it" -> "Computer"

        lower == "transport rule" || lower == "transport rules" || lower == "transport" || lower == "motor vehicle" -> "Transport Rules"

        lower == "current affairs" || lower == "current affairs & general awareness" -> "Current Affairs"

        lower == "general science" || lower == "science" -> "General Science"

        else -> trimmed
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MockTestPlayerScreen(viewModel: JuktiViewModel) {
    val language = com.example.ui.viewmodel.AppLanguage.ENGLISH
    val questionLanguage by viewModel.questionLanguage.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    val mockTest by viewModel.selectedMockTest.collectAsState()
    val activeMockQuestions by viewModel.activeMockQuestions.collectAsState()
    val userAnswers by viewModel.mockUserAnswers.collectAsState()
    val markedForReview by viewModel.mockMarkedForReview.collectAsState()
    val timeRemainingSeconds by viewModel.mockTimeRemainingSeconds.collectAsState()
    val isSubmittingMock by viewModel.isSubmittingMock.collectAsState()

    var currentQuestionIndex by remember { mutableStateOf(0) }
    var selectedSubject by remember { mutableStateOf("All") }
    var showPaletteSheet by remember { mutableStateOf(false) }
    var showSubmitConfirmDialog by remember { mutableStateOf(false) }
    var showExitConfirmDialog by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }

    // Map each question to its normalized display subject
    val questionDisplaySubjects = remember(activeMockQuestions) {
        activeMockQuestions.map { q -> mapQuestionSubjectToDisplaySubject(q.subject) }
    }

    // Dynamic, mock-specific subject list detected from questions
    val availableSubjects = remember(questionDisplaySubjects) {
        val subjectsInMock = mutableListOf<String>()
        questionDisplaySubjects.forEach { subj ->
            if (subj.isNotBlank() && !subjectsInMock.contains(subj)) {
                subjectsInMock.add(subj)
            }
        }
        listOf("All") + subjectsInMock
    }

    // Reset selected subject if not present in available subjects
    LaunchedEffect(availableSubjects) {
        if (selectedSubject !in availableSubjects) {
            selectedSubject = "All"
        }
    }

    // Questions list scoped to selected subject paired with original global index
    val currentSubjectQuestions: List<Pair<Int, QuestionEntity>> = remember(activeMockQuestions, selectedSubject, questionDisplaySubjects) {
        if (selectedSubject == "All") {
            activeMockQuestions.mapIndexed { idx, q -> idx to q }
        } else {
            activeMockQuestions.mapIndexedNotNull { idx, q ->
                if (questionDisplaySubjects.getOrNull(idx) == selectedSubject) {
                    idx to q
                } else null
            }
        }
    }

    // Active test timer countdown
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(1000L)
            viewModel.decrementMockTimer()
        }
    }

    val currentQuestion = activeMockQuestions.getOrNull(currentQuestionIndex)

    val hoursLeft = (timeRemainingSeconds / 3600).coerceAtLeast(0)
    val minutesLeft = ((timeRemainingSeconds % 3600) / 60).coerceAtLeast(0)
    val secondsLeft = (timeRemainingSeconds % 60).coerceAtLeast(0)
    val formattedTime = "%02d:%02d:%02d".format(hoursLeft, minutesLeft, secondsLeft)

    if (showExitConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showExitConfirmDialog = false },
            title = { Text("Exit Mock Test?") },
            text = { Text("Your current test progress will not be saved.") },
            confirmButton = {
                Button(
                    onClick = {
                        showExitConfirmDialog = false
                        viewModel.cancelMockTimer()
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
        val totalQ = activeMockQuestions.size
        val answeredQ = userAnswers.size
        val markedQ = markedForReview.size
        val skippedQ = (totalQ - answeredQ).coerceAtLeast(0)

        AlertDialog(
            onDismissRequest = { if (!isSubmittingMock) showSubmitConfirmDialog = false },
            title = { Text("Submit Mock Test?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Are you sure you want to finish and submit this test?")
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    Text("• Total Questions: $totalQ", fontWeight = FontWeight.Bold)
                    Text("• Answered: $answeredQ", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                    Text("• Skipped / Unanswered: $skippedQ", color = Color(0xFFE65100))
                    Text("• Marked for Review: $markedQ", color = Color(0xFF1976D2))
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSubmitConfirmDialog = false
                        viewModel.submitCurrentMockTest()
                    },
                    enabled = !isSubmittingMock,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    if (isSubmittingMock) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Submitting...")
                    } else {
                        Text("Submit Test")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showSubmitConfirmDialog = false },
                    enabled = !isSubmittingMock
                ) {
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
                        text = "Time Remaining: $formattedTime",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (timeRemainingSeconds < 300) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
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

        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            shadowElevation = 1.dp
        ) {
            Column {
                // Dynamic Subject Switcher Row (Only appears if subjects exist in this mock)
                if (availableSubjects.size > 1) {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        items(availableSubjects) { subject ->
                            val isSelected = (subject == selectedSubject)
                            val totalForSubj = if (subject == "All") {
                                activeMockQuestions.size
                            } else {
                                questionDisplaySubjects.count { it == subject }
                            }
                            val answeredForSubj = if (subject == "All") {
                                userAnswers.size
                            } else {
                                activeMockQuestions.indices.count { idx ->
                                    questionDisplaySubjects.getOrNull(idx) == subject && userAnswers.containsKey(idx)
                                }
                            }

                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    if (selectedSubject != subject) {
                                        selectedSubject = subject
                                        val subjectIndices = if (subject == "All") {
                                            activeMockQuestions.indices.toList()
                                        } else {
                                            activeMockQuestions.indices.filter { questionDisplaySubjects.getOrNull(it) == subject }
                                        }
                                        // If current viewed question is not in the newly selected subject, jump to its first question
                                        if (subjectIndices.isNotEmpty() && currentQuestionIndex !in subjectIndices) {
                                            currentQuestionIndex = subjectIndices.first()
                                        }
                                    }
                                },
                                label = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        Text(
                                            text = subject,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 12.sp
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                        ) {
                                            Text(
                                                text = "$answeredForSubj/$totalForSubj",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                ),
                                modifier = Modifier.height(32.dp)
                            )
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                }

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
            val paletteAnsweredCount = currentSubjectQuestions.count { userAnswers.containsKey(it.first) }
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (selectedSubject == "All") "Question Palette ($paletteAnsweredCount/${currentSubjectQuestions.size} Answered)" else "Palette: $selectedSubject ($paletteAnsweredCount/${currentSubjectQuestions.size} Answered)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        if (selectedSubject != "All") {
                            Text(
                                text = "Total Mock: ${userAnswers.size}/${activeMockQuestions.size}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(6),
                        modifier = Modifier.height(140.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        itemsIndexed(currentSubjectQuestions) { subPos, (origIndex, _) ->
                            val isAnswered = userAnswers.containsKey(origIndex)
                            val isReviewed = markedForReview.contains(origIndex)
                            val isCurrent = (origIndex == currentQuestionIndex)

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
                                    .then(
                                        if (isCurrent) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(6.dp))
                                        else Modifier
                                    )
                                    .clickable {
                                        currentQuestionIndex = origIndex
                                        showPaletteSheet = false
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${origIndex + 1}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isCurrent) FontWeight.ExtraBold else FontWeight.Bold,
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
            val currentPosInSubject = currentSubjectQuestions.indexOfFirst { it.first == currentQuestionIndex }.coerceAtLeast(0)
            val totalInCurrentSubject = currentSubjectQuestions.size
            val activeQuestionSubject = questionDisplaySubjects.getOrNull(currentQuestionIndex) ?: "General Knowledge"

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
                    Column {
                        if (selectedSubject == "All") {
                            Text(
                                text = "Question ${currentQuestionIndex + 1} of ${activeMockQuestions.size}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = activeQuestionSubject,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold
                            )
                        } else {
                            Text(
                                text = "Question ${currentPosInSubject + 1} of $totalInCurrentSubject • $selectedSubject",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Mock Q#${currentQuestionIndex + 1} of ${activeMockQuestions.size}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
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
                    val hasPrevious = currentPosInSubject > 0
                    val hasNext = currentPosInSubject < currentSubjectQuestions.size - 1

                    OutlinedButton(
                        onClick = {
                            if (hasPrevious) {
                                currentQuestionIndex = currentSubjectQuestions[currentPosInSubject - 1].first
                            }
                        },
                        enabled = hasPrevious
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
                            if (hasNext) {
                                currentQuestionIndex = currentSubjectQuestions[currentPosInSubject + 1].first
                            }
                        },
                        enabled = hasNext
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

