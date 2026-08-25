package com.example.ui.screens

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.*
import com.example.ui.theme.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.QuestionEntity
import com.example.ui.components.BilingualText
import com.example.ui.viewmodel.AppLanguage
import com.example.ui.viewmodel.JuktiViewModel
import com.example.ui.viewmodel.Screen

enum class AnalysisFilter {
    ALL, CORRECT, INCORRECT, SKIPPED
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MockResultScreen(viewModel: JuktiViewModel) {
    val language by viewModel.language.collectAsState()
    val mockTest by viewModel.selectedMockTest.collectAsState()
    val activeQuestions by viewModel.activeMockQuestions.collectAsState()
    val currentMockAttempt by viewModel.currentMockAttempt.collectAsState()
    val userAnswers by viewModel.mockUserAnswers.collectAsState()
    val bookmarkedIds by viewModel.bookmarkedIds.collectAsState()
    val likedIds by viewModel.likedIds.collectAsState()
    val context = LocalContext.current

    var selectedTab by remember { mutableStateOf(0) } // 0 = Summary Scorecard, 1 = Question Analysis
    var analysisFilter by remember { mutableStateOf(AnalysisFilter.ALL) }
    var analysisLanguage by remember { mutableStateOf(AppLanguage.BOTH) }
    var showPaletteGrid by remember { mutableStateOf(false) }

    // Authoritative Result Calculations from attempt record / active questions
    val totalQ = activeQuestions.size.coerceAtLeast(mockTest?.totalQuestions ?: 0).coerceAtLeast(1)
    val attemptedCount = currentMockAttempt?.totalAttempted ?: userAnswers.size
    val correctCount = currentMockAttempt?.correctCount ?: activeQuestions.indices.count { idx ->
        userAnswers[idx] == activeQuestions.getOrNull(idx)?.correctOptionIndex
    }
    val incorrectCount = (attemptedCount - correctCount).coerceAtLeast(0)
    val skippedCount = (totalQ - attemptedCount).coerceAtLeast(0)

    val totalMarks = currentMockAttempt?.totalMarks ?: mockTest?.totalMarks ?: (totalQ * 2)
    val score = currentMockAttempt?.score ?: mockTest?.userScore ?: 0
    val accuracyPercent = currentMockAttempt?.accuracy ?: if (attemptedCount > 0) {
        ((correctCount.toFloat() / attemptedCount.toFloat()) * 100f).coerceIn(0f, 100f)
    } else 0f

    val percentile = mockTest?.userPercentile ?: if (accuracyPercent > 0f) accuracyPercent.coerceIn(5.0f, 99.9f) else 50.0f
    val rank = mockTest?.userRank ?: 1

    val qMarksMap = remember(currentMockAttempt, mockTest) {
        val json = currentMockAttempt?.questionMarksJson?.takeIf { it.isNotBlank() && it != "{}" } 
                   ?: mockTest?.questionMarksJson?.takeIf { it.isNotBlank() && it != "{}" }
        val map = mutableMapOf<Long, Float>()
        if (json != null) {
            try {
                val obj = org.json.JSONObject(json)
                obj.keys().forEach { k -> map[k.toLong()] = obj.getDouble(k).toFloat() }
            } catch (e: Exception) {}
        }
        map
    }

    val defaultMarkPerQuestion = if ((mockTest?.markPerQuestion ?: 0f) > 0f) {
        mockTest!!.markPerQuestion
    } else {
        (totalMarks.toFloat() / totalQ.coerceAtLeast(1).toFloat()).coerceAtLeast(1f)
    }

    val negPerQuestion = remember(mockTest) {
        val negStr = mockTest?.negativeMarking ?: "0.25"
        if (negStr.equals("None", ignoreCase = true) || negStr.isBlank()) 0f
        else {
            val match = Regex("""([0-9]+(?:\.[0-9]+)?)""").find(negStr)
            match?.value?.toFloatOrNull() ?: 0.25f
        }
    }

    val timeSpentSeconds = remember(currentMockAttempt, mockTest) {
        val json = currentMockAttempt?.userAnswersJson ?: ""
        var t = 0
        if (json.isNotBlank()) {
            try {
                val obj = org.json.JSONObject(json)
                t = obj.optInt("timeSpentSeconds", 0)
            } catch (e: Exception) {}
        }
        if (t <= 0) {
            t = (mockTest?.durationMinutes ?: 90) * 60
        }
        t
    }

    val solvingSpeedSecPerQ = if (attemptedCount > 0) (timeSpentSeconds / attemptedCount) else 0
    val speedDisplay = if (solvingSpeedSecPerQ > 0) "${solvingSpeedSecPerQ}s / MCQ" else "N/A"
    val timeDisplay = "%02dm %02ds".format(timeSpentSeconds / 60, timeSpentSeconds % 60)

    // Filtered list of questions with their original 0-based indices
    val indexedFilteredQuestions = remember(activeQuestions, userAnswers, analysisFilter) {
        activeQuestions.mapIndexed { origIdx, q ->
            Triple(origIdx, q, userAnswers[origIdx])
        }.filter { (origIdx, q, choice) ->
            when (analysisFilter) {
                AnalysisFilter.ALL -> true
                AnalysisFilter.CORRECT -> choice == q.correctOptionIndex
                AnalysisFilter.INCORRECT -> choice != null && choice != q.correctOptionIndex
                AnalysisFilter.SKIPPED -> choice == null
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // TOP APP BAR
        com.example.ui.components.JuktiTopAppBar(
            title = {
                Column {
                    Text(
                        text = "Mock Test Result & Analysis",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    BilingualText(
                        textEn = mockTest?.titleEn ?: "Full Length Mock Test",
                        textAs = mockTest?.titleAs ?: "সম্পূৰ্ণ দৈৰ্ঘ্যৰ মক টেষ্ট",
                        language = language,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            onBackClick = { viewModel.navigateTo(Screen.MOCK_TESTS) },
            actions = {
                IconButton(onClick = {
                    val shareIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(
                            Intent.EXTRA_TEXT,
                            "I scored ${mockTest?.userScore ?: 124}/${mockTest?.totalMarks ?: 150} in Jukti Mock Test!\nCorrect: $correctCount, Incorrect: $incorrectCount, Skipped: $skippedCount.\nDownload Jukti App: https://jukti.in"
                        )
                        type = "text/plain"
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Share Result"))
                }) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = "Share")
                }
            }
        )

        // TAB SWITCHER: SUMMARY vs DETAILED ANALYSIS
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Text(
                        text = "Scorecard Summary",
                        fontWeight = FontWeight.Bold
                    )
                },
                icon = { Icon(Icons.Default.Assessment, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Text(
                        text = "Analyze Questions (${activeQuestions.size})",
                        fontWeight = FontWeight.Bold
                    )
                },
                icon = { Icon(Icons.Default.Analytics, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
        }

        if (selectedTab == 0) {
            // TAB 0: SUMMARY SCORECARD
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(72.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(44.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Test Submitted Successfully!",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // SCORECARD MAIN CARD
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            ResultStatBox(
                                title = "Score",
                                value = "$score / $totalMarks",
                                color = MaterialTheme.colorScheme.primary
                            )
                            ResultStatBox(
                                title = "Assam Rank",
                                value = "#$rank",
                                color = Color(0xFFE65100)
                            )
                            ResultStatBox(
                                title = "Percentile",
                                value = "%.1f%%".format(percentile),
                                color = Color(0xFF2E7D32)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(16.dp))

                        // DETAILED QUESTION BREAKDOWN COUNTS
                        Text(
                            text = "Question Performance Breakdown:",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Correct
                            BreakdownChip(
                                modifier = Modifier.weight(1f),
                                label = "Correct",
                                count = "$correctCount",
                                icon = Icons.Default.CheckCircle,
                                color = Color(0xFF2E7D32),
                                containerColor = Color(0xFFE8F5E9)
                            )
                            // Incorrect
                            BreakdownChip(
                                modifier = Modifier.weight(1f),
                                label = "Incorrect",
                                count = "$incorrectCount",
                                icon = Icons.Default.Cancel,
                                color = Color(0xFFC62828),
                                containerColor = Color(0xFFFFEBEE)
                            )
                            // Skipped
                            BreakdownChip(
                                modifier = Modifier.weight(1f),
                                label = "Skipped",
                                count = "$skippedCount",
                                icon = Icons.Default.RemoveCircleOutline,
                                color = Color(0xFFE65100),
                                containerColor = Color(0xFFFFF3E0)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Attempted Questions:",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "$attemptedCount / $totalQ",
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Accuracy Rate:",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "%.1f%%".format(accuracyPercent),
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Time Taken:",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = timeDisplay,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Mock Test Speed / MCQ:",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = speedDisplay,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // PROMINENT "ANALYZE QUESTIONS" CALL TO ACTION BUTTON
                Button(
                    onClick = { selectedTab = 1 },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Analytics, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Analyze Questions & Explanations ➔",
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // FILTER DIRECT JUMP BUTTONS
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            analysisFilter = AnalysisFilter.CORRECT
                            selectedTab = 1
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF2E7D32))
                    ) {
                        Text("View Correct ($correctCount)", style = MaterialTheme.typography.labelSmall)
                    }

                    OutlinedButton(
                        onClick = {
                            analysisFilter = AnalysisFilter.INCORRECT
                            selectedTab = 1
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFC62828))
                    ) {
                        Text("View Incorrect ($incorrectCount)", style = MaterialTheme.typography.labelSmall)
                    }

                    OutlinedButton(
                        onClick = {
                            analysisFilter = AnalysisFilter.SKIPPED
                            selectedTab = 1
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE65100))
                    ) {
                        Text("View Skipped ($skippedCount)", style = MaterialTheme.typography.labelSmall)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = { viewModel.navigateTo(Screen.HOME) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Return to Home")
                }
            }
        } else {
            // TAB 1: DETAILED QUESTION ANALYSIS VIEW
            Column(modifier = Modifier.fillMaxSize()) {
                // CONTROL BAR (Filter Chips & Language Switcher & Palette toggle)
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Filter Analysis:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )

                            // PALETTE GRID TOGGLE
                            TextButton(
                                onClick = { showPaletteGrid = !showPaletteGrid },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Icon(Icons.Default.GridView, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (showPaletteGrid) "Hide Palette" else "Question Palette Grid",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // FILTER CHIPS ROW (All, Correct, Incorrect, Skipped)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            FilterChip(
                                selected = analysisFilter == AnalysisFilter.ALL,
                                onClick = { analysisFilter = AnalysisFilter.ALL },
                                label = { Text("All (${activeQuestions.size})", fontSize = 11.sp) },
                                modifier = Modifier.height(30.dp)
                            )
                            FilterChip(
                                selected = analysisFilter == AnalysisFilter.CORRECT,
                                onClick = { analysisFilter = AnalysisFilter.CORRECT },
                                label = { Text("Correct ($correctCount)", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFE8F5E9),
                                    selectedLabelColor = Color(0xFF2E7D32)
                                ),
                                modifier = Modifier.height(30.dp)
                            )
                            FilterChip(
                                selected = analysisFilter == AnalysisFilter.INCORRECT,
                                onClick = { analysisFilter = AnalysisFilter.INCORRECT },
                                label = { Text("Incorrect ($incorrectCount)", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFFFEBEE),
                                    selectedLabelColor = Color(0xFFC62828)
                                ),
                                modifier = Modifier.height(30.dp)
                            )
                            FilterChip(
                                selected = analysisFilter == AnalysisFilter.SKIPPED,
                                onClick = { analysisFilter = AnalysisFilter.SKIPPED },
                                label = { Text("Skipped ($skippedCount)", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFFFF3E0),
                                    selectedLabelColor = Color(0xFFE65100)
                                ),
                                modifier = Modifier.height(30.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // LANGUAGE TOGGLE FOR EXPLANATIONS
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Explanations Language:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                FilterChip(
                                    selected = analysisLanguage == AppLanguage.ENGLISH,
                                    onClick = { analysisLanguage = AppLanguage.ENGLISH },
                                    label = { Text("EN", fontSize = 10.sp) },
                                    modifier = Modifier.height(26.dp)
                                )
                                FilterChip(
                                    selected = analysisLanguage == AppLanguage.ASSAMESE,
                                    onClick = { analysisLanguage = AppLanguage.ASSAMESE },
                                    label = { Text("অসমীয়া", fontSize = 10.sp) },
                                    modifier = Modifier.height(26.dp)
                                )
                                FilterChip(
                                    selected = analysisLanguage == AppLanguage.BOTH,
                                    onClick = { analysisLanguage = AppLanguage.BOTH },
                                    label = { Text("Both", fontSize = 10.sp) },
                                    modifier = Modifier.height(26.dp)
                                )
                            }
                        }
                    }
                }

                // EXPANDABLE QUESTION PALETTE GRID
                AnimatedVisibility(visible = showPaletteGrid) {
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 2.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Tap any question number to filter or jump:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            LazyVerticalGrid(
                                columns = GridCells.Fixed(7),
                                modifier = Modifier.height(110.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                itemsIndexed(activeQuestions) { origIdx, q ->
                                    val choice = userAnswers[origIdx]
                                    val isCorrect = choice == q.correctOptionIndex
                                    val isIncorrect = choice != null && choice != q.correctOptionIndex
                                    val isSkipped = choice == null

                                    val bg = when {
                                        isCorrect -> Color(0xFF2E7D32)
                                        isIncorrect -> Color(0xFFC62828)
                                        else -> Color(0xFFE65100)
                                    }

                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(bg)
                                            .clickable {
                                                // Set filter to show all or clicked question
                                                showPaletteGrid = false
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${origIdx + 1}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // FILTERED QUESTIONS FEED LIST
                if (indexedFilteredQuestions.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No questions match the selected filter.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        itemsIndexed(indexedFilteredQuestions) { _, (origIdx, question, userChoice) ->
                            QuestionAnalysisCard(
                                questionIndex = origIdx,
                                question = question,
                                userChoice = userChoice,
                                markPerQuestion = qMarksMap[question.id] ?: defaultMarkPerQuestion,
                                negPerQuestion = negPerQuestion,
                                analysisLanguage = analysisLanguage,
                                appLanguage = language,
                                bookmarkedIds = bookmarkedIds,
                                likedIds = likedIds,
                                onBookmarkToggle = { viewModel.toggleBookmarkQuestion(question) },
                                onLikeToggle = { viewModel.toggleLikeQuestion(question) }
                            )
                        }
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// COMPONENT: QUESTION ANALYSIS CARD (Shows Q#, status badge, options, solution)
// -----------------------------------------------------------------------------
@Composable
fun QuestionAnalysisCard(
    questionIndex: Int,
    question: QuestionEntity,
    userChoice: Int?,
    markPerQuestion: Float = 1.0f,
    negPerQuestion: Float = 0.25f,
    analysisLanguage: AppLanguage,
    appLanguage: AppLanguage,
    bookmarkedIds: Set<Long>,
    likedIds: Set<Long>,
    onBookmarkToggle: () -> Unit,
    onLikeToggle: () -> Unit
) {
    val isCorrect = userChoice == question.correctOptionIndex
    val isIncorrect = userChoice != null && userChoice != question.correctOptionIndex
    val isSkipped = userChoice == null
    val isBookmarked = question.id in bookmarkedIds
    val isLiked = question.id in likedIds

    val statusContainerColor = when {
        isCorrect -> Color(0xFFE8F5E9)
        isIncorrect -> Color(0xFFFFEBEE)
        else -> Color(0xFFFFF3E0)
    }

    val statusTextColor = when {
        isCorrect -> Color(0xFF2E7D32)
        isIncorrect -> Color(0xFFC62828)
        else -> Color(0xFFE65100)
    }

    val statusIcon = when {
        isCorrect -> Icons.Default.CheckCircle
        isIncorrect -> Icons.Default.Cancel
        else -> Icons.Default.RemoveCircleOutline
    }

    val markStr = if (markPerQuestion % 1 == 0f) markPerQuestion.toInt().toString() else "%.2f".format(markPerQuestion)
    val negStr = if (negPerQuestion % 1 == 0f) negPerQuestion.toInt().toString() else "%.2f".format(negPerQuestion)
    val statusText = when {
        isCorrect -> "Correct (+$markStr)"
        isIncorrect -> "Incorrect (-$negStr)"
        else -> "Skipped (0.0)"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(
            1.dp,
            when {
                isCorrect -> Color(0xFF81C784)
                isIncorrect -> Color(0xFFE57373)
                else -> Color(0xFFFFB74D)
            }
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // QUESTION HEADER & STATUS BADGE
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Q${questionIndex + 1}.",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = question.subject,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = statusContainerColor
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = statusIcon,
                            contentDescription = null,
                            tint = statusTextColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = statusTextColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // QUESTION TEXT
            com.example.ui.components.QuestionTypeBadge(
                questionType = question.questionType,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            BilingualText(
                textEn = question.questionEn,
                textAs = question.questionAs,
                language = analysisLanguage,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 4 OPTIONS WITH COLOR-CODED CORRECT & INCORRECT HIGHLIGHTS
            val options = listOf(
                question.optionAEn to question.optionAAs,
                question.optionBEn to question.optionBAs,
                question.optionCEn to question.optionCAs,
                question.optionDEn to question.optionDAs
            )

            options.forEachIndexed { optIndex, pair ->
                val isThisCorrect = optIndex == question.correctOptionIndex
                val isThisUserChoice = userChoice == optIndex

                val optionBg = when {
                    isThisCorrect -> Color(0xFFE8F5E9)
                    isThisUserChoice -> Color(0xFFFFEBEE)
                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                }

                val optionBorder = when {
                    isThisCorrect -> Color(0xFF4CAF50)
                    isThisUserChoice -> Color(0xFFF44336)
                    else -> Color.Transparent
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = optionBg,
                    border = BorderStroke(1.dp, optionBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            val optionPrefix = when(optIndex) {
                                0 -> "(A)"
                                1 -> "(B)"
                                2 -> "(C)"
                                else -> "(D)"
                            }

                            Text(
                                text = optionPrefix,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isThisCorrect) Color(0xFF2E7D32) else if (isThisUserChoice) Color(0xFFC62828) else MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            BilingualText(
                                textEn = pair.first,
                                textAs = pair.second,
                                language = analysisLanguage,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = if (isThisCorrect || isThisUserChoice) FontWeight.Bold else FontWeight.Normal
                            )
                        }

                        // BADGES FOR USER CHOICE / CORRECT ANSWER
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isThisUserChoice && !isThisCorrect) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFFFFCDD2)
                                ) {
                                    Text(
                                        text = "Your Answer ✕",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFB71C1C),
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            if (isThisCorrect) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFFC8E6C9)
                                ) {
                                    Text(
                                        text = if (isThisUserChoice) {
                                            "Your Correct Choice ✓"
                                        } else {
                                            "Correct Answer ✓"
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1B5E20),
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // DETAILED SOLUTION & EXPLANATION BOX
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Lightbulb,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Solution & Detailed Explanation:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    BilingualText(
                        textEn = question.explanationEn.ifEmpty { "Detailed step-by-step solution provided for Assam competitive examination aspirants." },
                        textAs = question.explanationAs.ifEmpty { "অসমৰ প্ৰতিযোগিতামূলক পৰীক্ষাৰ্থীসকলৰ বাবে বিস্তৃত উত্তৰ ব্যাখ্যা আগবঢ়োৱা হৈছে।" },
                        language = analysisLanguage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // BOTTOM ACTIONS (Bookmark & Like)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isSkipped) {
                        "Question was left unattempted"
                    } else {
                        "Answer saved"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onBookmarkToggle) {
                        Icon(
                            imageVector = if (question.id in bookmarkedIds) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = if (question.id in bookmarkedIds) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(onClick = onLikeToggle) {
                        Icon(
                            imageVector = Icons.Outlined.ThumbUp,
                            contentDescription = "Like",
                            tint = if (isLiked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BreakdownChip(
    modifier: Modifier = Modifier,
    label: String,
    count: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    containerColor: Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = containerColor
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = count,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = color
            )
        }
    }
}

@Composable
fun ResultStatBox(title: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = color
        )
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

