package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.BilingualText
import com.example.data.local.UserProfileEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppLanguage
import com.example.ui.viewmodel.JuktiViewModel
import com.example.ui.viewmodel.Screen

// Data models for Analytics Breakdown
data class TopRanker(
    val name: String,
    val city: String,
    val xp: Int,
    val level: Int,
    val badge: String,
    val examPlanEn: String,
    val examPlanAs: String,
    val avgMockScore: Float = 0f,
    val completedMockCount: Int = 3,
    val uid: String = ""
)

data class ChapterAccuracy(
    val nameEn: String,
    val nameAs: String,
    val accuracyPercent: Int? // null means NA (No Record)
)

data class MissedQuestion(
    val questionEn: String,
    val questionAs: String,
    val optionsEn: List<String>,
    val optionsAs: List<String>,
    val correctIndex: Int,
    val explanationEn: String,
    val explanationAs: String,
    val issueType: String, // "Incorrect" or "Skipped"
    val questionType: String = "Expected"
)

data class SubjectBreakdown(
    val id: String,
    val subjectNameEn: String,
    val subjectNameAs: String,
    val questionsSolved: Int,
    val accuracyPercent: Int,
    val avgTimeSec: Int,
    val chapters: List<ChapterAccuracy>,
    val missedQuestions: List<MissedQuestion>
)

data class MockHistoryItem(
    val titleEn: String,
    val titleAs: String,
    val date: String,
    val score: Int,
    val totalMarks: Int,
    val accuracy: Int,
    val percentile: Float,
    val rank: Int,
    val timeSpent: String,
    val category: String = "",
    val testId: Long = 0L
)

@Composable
fun LeaderboardAnalyticsScreen(viewModel: JuktiViewModel, initialTab: Int = 1) {
    val language by viewModel.language.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val examsList by viewModel.examsList.collectAsState()
    val plans by viewModel.plans.collectAsState()
    val userEntitlement by viewModel.userEntitlement.collectAsState()
    val isAdminOrOwner by viewModel.isAdminOrOwner.collectAsState()

    var selectedTab by remember { mutableStateOf(initialTab) }

    val isAssamese = language == AppLanguage.ASSAMESE || language == AppLanguage.BOTH

    // Subject breakdown sample data with chapter accuracy and missed questions
    val subjectBreakdownList = remember { emptyList<SubjectBreakdown>() }

    // Mock test history data from local database
    val mockTestsState by viewModel.mockTests.collectAsState()
    val mockHistoryList = remember(mockTestsState) {
        mockTestsState.filter { it.isCompleted }.map { mock ->
            val scorePct = if (mock.totalMarks > 0) (mock.userScore.toFloat() / mock.totalMarks.toFloat()) * 100f else 0f
            val realPercentile = if (mock.userPercentile > 0f) mock.userPercentile else ((scorePct * 0.7f + mock.userAccuracy * 0.3f)).coerceIn(5.0f, 99.9f)
            MockHistoryItem(
                titleEn = mock.titleEn,
                titleAs = mock.titleAs,
                date = mock.scheduledDate.ifEmpty { "Recently" },
                score = mock.userScore,
                totalMarks = mock.totalMarks,
                accuracy = mock.userAccuracy.toInt().coerceIn(0, 100),
                percentile = realPercentile,
                rank = mock.userRank,
                timeSpent = "${mock.durationMinutes} mins",
                category = mock.category,
                testId = mock.id
            )
        }
    }

    val activePlan = remember(userEntitlement, plans) {
        val entitlement = userEntitlement ?: return@remember null
        if (entitlement.status != "ACTIVE") return@remember null
        plans.find { it.id.toString() == entitlement.planId || it.planName.equals(entitlement.planName, ignoreCase = true) }
    }

    val userAllowedExams = remember(activePlan, examsList, isAdminOrOwner) {
        if (isAdminOrOwner || activePlan?.examTarget?.equals("All Exams", ignoreCase = true) == true || activePlan?.examTarget?.isBlank() == true) {
            examsList
        } else {
            val targetStr = activePlan?.examTarget ?: ""
            val allowedExamNames = targetStr.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            if (allowedExamNames.isEmpty()) {
                emptyList()
            } else {
                examsList.filter { exam ->
                    allowedExamNames.any { allowed ->
                        exam.title.contains(allowed, ignoreCase = true) || 
                        allowed.contains(exam.title, ignoreCase = true)
                    }
                }
            }
        }
    }

    // Currently selected subject for missed question modal
    var activeMissedQuestionSubject by remember { mutableStateOf<SubjectBreakdown?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // App bar & Header tabs
        com.example.ui.components.JuktiTopAppBar(
            title = "Leaderboard & Analytics",
            onBackClick = { viewModel.navigateTo(Screen.HOME) }
        )

        Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 1.dp) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = (selectedTab == 0),
                        onClick = { selectedTab = 0 },
                        text = { Text("State Leaderboard", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = (selectedTab == 1),
                        onClick = { selectedTab = 1 },
                        text = { Text("My Analytics", fontWeight = FontWeight.Bold) }
                    )
                }
            }
        }

        val mockAvg = remember(mockHistoryList) {
            if (mockHistoryList.isNotEmpty()) {
                mockHistoryList.map { (it.score.toFloat() / it.totalMarks.coerceAtLeast(1)) * 100f }.average().toFloat()
            } else {
                0f
            }
        }

        Crossfade(targetState = selectedTab, label = "TabSwitch") { tab ->
            if (tab == 0) {
                // STATE LEADERBOARD TAB (HERO BAR, OVERALL VS SAME EXAM, TOP 3 PODIUM, DROPDOWN & RANK LIST)
                LeaderboardTabContent(
                    viewModel = viewModel,
                    userXp = userProfile?.xp ?: 2350,
                    userLevel = userProfile?.level ?: 8,
                    userMockAvg = mockAvg,
                    isAssamese = isAssamese,
                    examsList = userAllowedExams,
                    userProfile = userProfile
                )
            } else {
            // MY ANALYTICS TAB (REBUILT WITH ALL NEW USER REQUIREMENTS)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // 1. PROBABILITY OF CLEARING EXAM IN %
                ExamClearanceProbabilityCard(
                    isAssamese = isAssamese,
                    mockHistoryList = mockHistoryList,
                    subjectBreakdownList = subjectBreakdownList,
                    userProfile = userProfile
                )

                // 2. KEY PERFORMANCE INDICATOR (KPI) IN 2x2 GRID
                KpiGrid2x2(userProfile = userProfile, isAssamese = isAssamese)

                // 3. WEAK SUBJECT FOCUS
                WeakSubjectFocusSection(
                    subjectBreakdownList = subjectBreakdownList,
                    isAssamese = isAssamese,
                    onPracticeClick = { viewModel.navigateTo(Screen.PRACTICE) }
                )

                // 4. SUBJECT ACCURACY BREAKDOWN (Banner + Dropdown arrow + Chapters lowest-to-highest + NA at last + Missed Questions button)
                SubjectAccuracyBreakdownSection(
                    subjectBreakdownList = subjectBreakdownList,
                    isAssamese = isAssamese,
                    onCheckMissedQuestions = { subject ->
                        activeMissedQuestionSubject = subject
                    }
                )

                // 5. MOCKTEST SCORE TREND IN LINE GRAPH
                MockTestScoreTrendCard(mockHistoryList = mockHistoryList, isAssamese = isAssamese)

                // 5.1. STUDY TIME TREND IN LINE GRAPH
                StudyTimeTrendCard(mockHistoryList = mockHistoryList, isAssamese = isAssamese)

                // 6. MOCK HISTORY
                MockTestHistorySection(
                    mockHistoryList = mockHistoryList,
                    isAssamese = isAssamese,
                    onViewMockResult = { mockItem ->
                        viewModel.analyzeMockFromHistory(
                            titleEn = mockItem.titleEn,
                            titleAs = mockItem.titleAs,
                            score = mockItem.score,
                            totalMarks = mockItem.totalMarks,
                            accuracy = mockItem.accuracy,
                            rank = mockItem.rank,
                            percentile = mockItem.percentile
                        )
                    }
                )
            }
        }
    }
    }

    // Modal Dialog for Frequently Incorrect / Skipped Questions
    activeMissedQuestionSubject?.let { subject ->
        MissedQuestionsModalDialog(
            subject = subject,
            isAssamese = isAssamese,
            onDismiss = { activeMissedQuestionSubject = null }
        )
    }
}

// -----------------------------------------------------------------------------
// COMPONENT 1: PROBABILITY OF CLEARING EXAM
// -----------------------------------------------------------------------------
@Composable
fun ExamClearanceProbabilityCard(
    isAssamese: Boolean,
    mockHistoryList: List<MockHistoryItem>,
    subjectBreakdownList: List<SubjectBreakdown>,
    userProfile: UserProfileEntity?
) {
    var showExplanationDialog by remember { mutableStateOf(false) }

    val mockAvg = if (mockHistoryList.isNotEmpty()) {
        mockHistoryList.map { (it.score.toFloat() / it.totalMarks.coerceAtLeast(1)) * 100f }.average().toFloat()
    } else {
        0f
    }
    
    val topicMastery = if (subjectBreakdownList.isNotEmpty()) {
        subjectBreakdownList.map { it.accuracyPercent.toFloat() }.average().toFloat()
    } else {
        0f
    }
    
    val overallAccuracy = if (userProfile != null && userProfile.totalSolved > 0) {
        (userProfile.correctCount.toFloat() / userProfile.totalSolved.toFloat()) * 100f
    } else {
        0f
    }
    
    val revisionConsistency = (userProfile?.dailyStreak?.toFloat() ?: 0f) * 10f
    val boundedRevision = revisionConsistency.coerceIn(0f, 100f)
    
    val syllabusCompletion = (userProfile?.level?.toFloat() ?: 1f) * 5f
    val boundedSyllabus = syllabusCompletion.coerceIn(0f, 100f)
    
    var probability = (mockAvg * 0.40f) + (topicMastery * 0.30f) + (overallAccuracy * 0.15f) + (boundedRevision * 0.10f) + (boundedSyllabus * 0.05f)
    if (probability > 98f) {
        probability = 98f
    }
    
    val chanceTextEn = when {
        probability >= 90f -> "Excellent Chance"
        probability >= 80f -> "High Chance"
        probability >= 70f -> "Good Chance"
        probability >= 60f -> "Moderate Chance"
        else -> "Needs Improvement"
    }
    
    val chanceTextAs = when {
        probability >= 90f -> "শ্ৰেষ্ঠ সম্ভাৱনা (Excellent Chance)"
        probability >= 80f -> "উচ্চ সম্ভাৱনা (High Chance)"
        probability >= 70f -> "ভাল সম্ভাৱনা (Good Chance)"
        probability >= 60f -> "মধ্যমীয়া সম্ভাৱনা (Moderate Chance)"
        else -> "উন্নতিৰ প্ৰয়োজন (Needs Improvement)"
    }
    
    val chanceColor = when {
        probability >= 80f -> MaterialTheme.colorScheme.success
        probability >= 60f -> Color(0xFFF57C00) // Orange
        else -> MaterialTheme.colorScheme.error
    }

    if (showExplanationDialog) {
        ExamClearanceProbabilityExplanationDialog(
            probability = probability,
            mockAvg = mockAvg,
            topicMastery = topicMastery,
            overallAccuracy = overallAccuracy,
            revisionConsistency = boundedRevision,
            syllabusCompletion = boundedSyllabus,
            onDismiss = { showExplanationDialog = false }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Probability of Clearing Exam",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        IconButton(
                            onClick = { showExplanationDialog = true },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "How is this calculated and disclaimer",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "App-generated estimate based on your in-app activity and practice performance",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = String.format("%.1f%%", probability),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = chanceColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Gauge Indicator
            LinearProgressIndicator(
                progress = { probability / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp),
                color = chanceColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = chanceColor.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = chanceColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = chanceTextEn,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = chanceColor
                        )
                    }
                }

                TextButton(
                    onClick = { showExplanationDialog = true },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "How is this calculated?",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Surface(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showExplanationDialog = true }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.HelpOutline,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Estimate based on app practice • Does not guarantee exam results",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.5.sp
                    )
                }
            }
        }
    }
}

@Composable
fun ExamClearanceProbabilityExplanationDialog(
    probability: Float,
    mockAvg: Float,
    topicMastery: Float,
    overallAccuracy: Float,
    revisionConsistency: Float,
    syllabusCompletion: Float,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Analytics,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Probability of Clearing Exam",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Main estimated summary
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Current App-Generated Estimate: ${String.format("%.1f%%", probability)}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "This metric is an automated estimation calculated exclusively from your activity, question answers, and mock test scores inside the Jukti application.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Mandatory Official Disclaimer Card
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Important Disclaimer & Limitations",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "• App Estimate, Not a Guarantee: This probability is an educational estimate and does NOT guarantee that you will qualify, pass, or fail any competitive examination.\n\n" +
                                   "• Data Scope: It is computed solely from available in-app practice and mock test data logged on your device.\n\n" +
                                   "• External Factors: Real examination outcomes depend on many factors outside the app, including official question difficulty, dynamic cut-off marks, candidate competition, reservation quotas, negative marking, and personal exam-day conditions.\n\n" +
                                   "• No Official Liability: Jukti cannot and does not predict official examination results with certainty. Jukti is not responsible for the user's actual examination results.",
                            style = MaterialTheme.typography.bodySmall,
                            lineHeight = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Breakdown of how it is calculated
                Text(
                    text = "How the App Calculates This Estimate",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ProbabilityFactorRow(
                        title = "1. Mock Test Performance (40%)",
                        description = "Average score percentage across your completed full-length mock tests.",
                        currentValue = String.format("%.1f%%", mockAvg)
                    )
                    ProbabilityFactorRow(
                        title = "2. Subject & Topic Mastery (30%)",
                        description = "Average accuracy across all individual subject modules.",
                        currentValue = String.format("%.1f%%", topicMastery)
                    )
                    ProbabilityFactorRow(
                        title = "3. Overall Practice Accuracy (15%)",
                        description = "Ratio of correct answers to total attempted practice questions.",
                        currentValue = String.format("%.1f%%", overallAccuracy)
                    )
                    ProbabilityFactorRow(
                        title = "4. Daily Revision Consistency (10%)",
                        description = "Your continuous daily study streak and active revision habits.",
                        currentValue = String.format("%.1f%%", revisionConsistency)
                    )
                    ProbabilityFactorRow(
                        title = "5. Syllabus Progress & Level (5%)",
                        description = "Completed study coverage and academic milestone levels in the app.",
                        currentValue = String.format("%.1f%%", syllabusCompletion)
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Understood")
            }
        }
    )
}

@Composable
private fun ProbabilityFactorRow(
    title: String,
    description: String,
    currentValue: String
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.5.sp
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = currentValue,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

// -----------------------------------------------------------------------------
// COMPONENT 2: KEY PERFORMANCE INDICATOR IN 2x2 GRID
// -----------------------------------------------------------------------------
@Composable
fun KpiGrid2x2(userProfile: com.example.data.local.UserProfileEntity?, isAssamese: Boolean) {
    val totalSolved = userProfile?.totalSolved ?: 0
    val correctCount = userProfile?.correctCount ?: 0
    val totalTimeMinutes = userProfile?.totalTimeMinutes ?: 0
    
    val accuracy = if (totalSolved > 0) {
        String.format("%.1f%%", (correctCount.toFloat() / totalSolved) * 100)
    } else {
        "0.0%"
    }
    
    val avgSpeed = if (totalSolved > 0) {
        val avgSeconds = (totalTimeMinutes * 60) / totalSolved
        "${avgSeconds}s"
    } else {
        "0s"
    }

    val accFloat = if (totalSolved > 0) (correctCount.toFloat() / totalSolved) * 100f else 0f
    val percentile = if (totalSolved > 0) {
        String.format(java.util.Locale.getDefault(), "%.1f%%", (accFloat * 0.8f + 15f).coerceIn(10f, 99.9f))
    } else {
        "0%"
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Key Performance Indicators",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 1. MCQ Solved
            KpiCardItem(
                modifier = Modifier.weight(1f),
                title = "MCQ Solved",
                value = "$totalSolved",
                subtitle = "Total questions",
                icon = Icons.Default.Quiz,
                iconTint = MaterialTheme.colorScheme.primary
            )

            // 2. Solve Speed (per MCQ)
            KpiCardItem(
                modifier = Modifier.weight(1f),
                title = "Solve Speed (per MCQ)",
                value = avgSpeed,
                subtitle = "Optimal (<30 sec)",
                icon = Icons.Default.Speed,
                iconTint = MaterialTheme.colorScheme.secondary
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 3. Accuracy
            KpiCardItem(
                modifier = Modifier.weight(1f),
                title = "Accuracy",
                value = accuracy,
                subtitle = "Based on correct answers",
                icon = Icons.Default.CheckCircle,
                iconTint = MaterialTheme.colorScheme.success
            )

            // 4. Percentile
            KpiCardItem(
                modifier = Modifier.weight(1f),
                title = "Percentile",
                value = percentile,
                subtitle = "Top rank estimate",
                icon = Icons.Default.Equalizer,
                iconTint = MaterialTheme.colorScheme.accent
            )
        }
    }
}

@Composable
fun KpiCardItem(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
                Surface(
                    shape = CircleShape,
                    color = iconTint.copy(alpha = 0.12f),
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = iconTint,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// -----------------------------------------------------------------------------
// COMPONENT 3: WEAK SUBJECT FOCUS
// -----------------------------------------------------------------------------
@Composable
fun WeakSubjectFocusSection(
    subjectBreakdownList: List<SubjectBreakdown>,
    isAssamese: Boolean,
    onPracticeClick: () -> Unit
) {
    val weakSubjects = subjectBreakdownList.filter { it.accuracyPercent < 70 }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Weak Subject Focus",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "High priority areas needing immediate revision",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            weakSubjects.forEachIndexed { index, subject ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                BilingualText(
                                    textEn = subject.subjectNameEn,
                                    textAs = subject.subjectNameAs,
                                    language = AppLanguage.ENGLISH,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Accuracy: ${subject.accuracyPercent}% • ${subject.questionsSolved} questions solved",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Button(
                                onClick = onPracticeClick,
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                            ) {
                                Text(
                                    text = "Practice",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Weakest chapters list
                        val lowestChapters = subject.chapters
                            .filter { it.accuracyPercent != null }
                            .sortedBy { it.accuracyPercent }
                            .take(2)

                        if (lowestChapters.isNotEmpty()) {
                            Text(
                                text = "Key Weak Chapters:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            lowestChapters.forEach { chap ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "• ${chap.nameEn}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = "${chap.accuracyPercent}%",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.error,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                if (index < weakSubjects.size - 1) {
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// COMPONENT 4: SUBJECT ACCURACY BREAKDOWN WITH BANNER, DROPDOWN & MISSED QUESTIONS
// -----------------------------------------------------------------------------
@Composable
fun SubjectAccuracyBreakdownSection(
    subjectBreakdownList: List<SubjectBreakdown>,
    isAssamese: Boolean,
    onCheckMissedQuestions: (SubjectBreakdown) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Subject Accuracy Breakdown",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        subjectBreakdownList.forEach { subject ->
            SubjectBreakdownBannerCard(
                subject = subject,
                isAssamese = isAssamese,
                onCheckMissedQuestions = { onCheckMissedQuestions(subject) }
            )
        }
    }
}

@Composable
fun SubjectBreakdownBannerCard(
    subject: SubjectBreakdown,
    isAssamese: Boolean,
    onCheckMissedQuestions: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row with Title and Dropdown Arrow
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    BilingualText(
                        textEn = subject.subjectNameEn,
                        textAs = subject.subjectNameAs,
                        language = AppLanguage.ENGLISH,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(
                    onClick = { isExpanded = !isExpanded },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Expand Chapter Accuracy",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Banner Stats Row: Question Solved, Accuracy, Average Time per Question
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Questions Solved
                    Column(horizontalAlignment = Alignment.Start) {
                        Text(
                            text = "Questions Solved",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${subject.questionsSolved}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Divider(
                        modifier = Modifier
                            .height(24.dp)
                            .width(1.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )

                    // Accuracy
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Accuracy",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${subject.accuracyPercent}%",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (subject.accuracyPercent >= 75) MaterialTheme.colorScheme.success else MaterialTheme.colorScheme.error
                        )
                    }

                    Divider(
                        modifier = Modifier
                            .height(24.dp)
                            .width(1.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )

                    // Average Time Per Question
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Avg. Time/Q",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${subject.avgTimeSec} sec",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Button: Frequently Incorrect / Skipped Questions
            OutlinedButton(
                onClick = onCheckMissedQuestions,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(vertical = 6.dp, horizontal = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FindInPage,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Frequently Incorrect / Skipped Questions (${subject.missedQuestions.size})",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            // EXPANDABLE CHAPTER BREAKDOWN (Sorted lowest to highest accuracy, NA at last)
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    Divider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Chapter Accuracy (Lowest to Highest):",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Sort rule: Lowest percent first, null (NA) at the very end
                    val sortedChapters = subject.chapters.sortedWith(
                        Comparator { c1, c2 ->
                            when {
                                c1.accuracyPercent == null && c2.accuracyPercent == null -> 0
                                c1.accuracyPercent == null -> 1 // null goes last
                                c2.accuracyPercent == null -> -1
                                else -> c1.accuracyPercent.compareTo(c2.accuracyPercent)
                            }
                        }
                    )

                    sortedChapters.forEach { chap ->
                        val displayAccuracyText = if (chap.accuracyPercent != null) "${chap.accuracyPercent}%" else "NA"
                        val accuracyColor = when {
                            chap.accuracyPercent == null -> MaterialTheme.colorScheme.onSurfaceVariant
                            chap.accuracyPercent < 60 -> MaterialTheme.colorScheme.error
                            chap.accuracyPercent < 80 -> MaterialTheme.colorScheme.warning
                            else -> MaterialTheme.colorScheme.success
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = chap.nameEn,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = accuracyColor.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = displayAccuracyText,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = accuracyColor
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Modal Dialog to display Missed Questions for a subject
@Composable
fun MissedQuestionsModalDialog(
    subject: SubjectBreakdown,
    isAssamese: Boolean,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Frequently Incorrect / Skipped Questions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subject.subjectNameEn,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        text = {
            if (subject.missedQuestions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No frequently incorrect or skipped questions recorded for this subject. Great job!",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    subject.missedQuestions.forEachIndexed { qIdx, item ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Q${qIdx + 1}",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Surface(
                                        shape = CircleShape,
                                        color = if (item.issueType == "Incorrect") MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.warningContainer
                                    ) {
                                        Text(
                                            text = item.issueType,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (item.issueType == "Incorrect") MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onWarningContainer
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                com.example.ui.components.QuestionTypeBadge(
                                    questionType = item.questionType,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                                BilingualText(
                                    textEn = item.questionEn,
                                    textAs = item.questionAs,
                                    language = AppLanguage.ENGLISH,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.successContainer,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text(
                                            text = "Correct Answer: ${item.optionsEn.getOrNull(item.correctIndex) ?: ""}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSuccessContainer
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        BilingualText(
                                            textEn = item.explanationEn,
                                            textAs = item.explanationAs,
                                            language = AppLanguage.ENGLISH,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSuccessContainer
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

// -----------------------------------------------------------------------------
// COMPONENT 5: MOCKTEST SCORE TREND IN LINE GRAPH
// -----------------------------------------------------------------------------
@Composable
fun MockTestScoreTrendCard(mockHistoryList: List<MockHistoryItem>, isAssamese: Boolean) {
    val scores = mockHistoryList.map { (it.score.toFloat() / it.totalMarks.coerceAtLeast(1)) * 100f }
    val labels = mockHistoryList.mapIndexed { index, _ -> "M${index + 1}" }
    val hasData = scores.isNotEmpty()

    val growthPct = if (scores.size >= 2) {
        val diff = scores.last() - scores.first()
        val base = scores.first().coerceAtLeast(1f)
        ((diff / base) * 100f).toInt()
    } else 0
    val growthText = if (growthPct >= 0) "+$growthPct% Growth" else "$growthPct% Growth"

    val lineColor = MaterialTheme.colorScheme.primary
    val gradientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ShowChart,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Mock Test Score Trend",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (hasData) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.successContainer
                    ) {
                        Text(
                            text = growthText,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSuccessContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (!hasData) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No mock tests completed yet.\nComplete mock tests to view your score trend.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                // Line Graph Canvas
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val width = size.width
                        val height = size.height
                        val paddingLeft = 30f
                        val paddingBottom = 40f
                        val paddingTop = 20f
                        val paddingRight = 20f

                        val graphWidth = width - paddingLeft - paddingRight
                        val graphHeight = height - paddingTop - paddingBottom

                        // Draw Grid Lines (0%, 50%, 100%)
                        val gridY0 = paddingTop + graphHeight
                        val gridY50 = paddingTop + graphHeight / 2
                        val gridY100 = paddingTop

                        drawLine(color = gridColor, start = Offset(paddingLeft, gridY0), end = Offset(width - paddingRight, gridY0), strokeWidth = 1f)
                        drawLine(color = gridColor, start = Offset(paddingLeft, gridY50), end = Offset(width - paddingRight, gridY50), strokeWidth = 1f)
                        drawLine(color = gridColor, start = Offset(paddingLeft, gridY100), end = Offset(width - paddingRight, gridY100), strokeWidth = 1f)

                        // Calculate point positions
                        val points = scores.mapIndexed { index, score ->
                            val x = paddingLeft + if (scores.size > 1) index * (graphWidth / (scores.size - 1)) else graphWidth / 2
                            val y = paddingTop + graphHeight * (1f - (score / 100f))
                            Offset(x, y)
                        }

                        // Path for Line & Gradient Fill
                        val path = Path().apply {
                            if (points.isNotEmpty()) {
                                moveTo(points[0].x, points[0].y)
                                for (i in 1 until points.size) {
                                    lineTo(points[i].x, points[i].y)
                                }
                            }
                        }

                        val fillPath = Path().apply {
                            addPath(path)
                            if (points.isNotEmpty()) {
                                lineTo(points.last().x, gridY0)
                                lineTo(points.first().x, gridY0)
                                close()
                            }
                        }

                        // Fill Gradient
                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(gradientColor, Color.Transparent),
                                startY = paddingTop,
                                endY = gridY0
                            )
                        )

                        // Draw Line
                        drawPath(
                            path = path,
                            color = lineColor,
                            style = Stroke(width = 6f)
                        )

                        // Draw Points and Node Circles
                        points.forEach { point ->
                            drawCircle(color = Color.White, radius = 8f, center = point)
                            drawCircle(color = lineColor, radius = 5f, center = point)
                        }
                    }

                    // Score text overlays on top of points
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 12.dp, end = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        scores.forEach { sc ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${sc.toInt()}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }

                    // X-Axis Labels
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .padding(start = 12.dp, end = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        labels.forEach { lbl ->
                            Text(
                                text = lbl,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Your score trend based on ${scores.size} completed mock test(s).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// -----------------------------------------------------------------------------
// COMPONENT 6: MOCK HISTORY
// -----------------------------------------------------------------------------
@Composable
fun MockTestHistorySection(
    mockHistoryList: List<MockHistoryItem>,
    isAssamese: Boolean,
    onViewMockResult: (MockHistoryItem) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    val displayList = if (isExpanded) mockHistoryList else mockHistoryList.take(3)

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Mock Test History",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${mockHistoryList.size} Tests Taken",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }

        displayList.forEach { mock ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onViewMockResult(mock) },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            BilingualText(
                                textEn = mock.titleEn,
                                textAs = mock.titleAs,
                                language = AppLanguage.ENGLISH,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Date: ${mock.date} • Time Spent: ${mock.timeSpent}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "${mock.score} / ${mock.totalMarks}",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Accuracy: ${mock.accuracy}%",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.success
                            )
                            Text(
                                text = "Percentile: ${mock.percentile}%",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.accent
                            )
                        }

                        Button(
                            onClick = { onViewMockResult(mock) },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                Icons.Default.Analytics,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Analyze",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
        
        if (mockHistoryList.size > 3) {
            TextButton(
                onClick = { isExpanded = !isExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (isExpanded) ("View Less") else ("View All"),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private fun isMockBelongsToExam(mockCategory: String, examTitle: String): Boolean {
    if (mockCategory.isBlank() || examTitle.isBlank()) return false
    val list = mockCategory.split(",").map { it.trim() }
    return list.any { it.equals(examTitle.trim(), ignoreCase = true) }
}

// -----------------------------------------------------------------------------
// COMPONENT: LEADERBOARD TAB CONTENT (Hero Bar, Overall vs Same Exam, Top 3 Podium & Dropdown)
// -----------------------------------------------------------------------------
@Composable
fun LeaderboardTabContent(
    viewModel: JuktiViewModel,
    userXp: Int,
    userLevel: Int,
    userMockAvg: Float,
    isAssamese: Boolean,
    examsList: List<com.example.data.local.ExamEntity>,
    userProfile: com.example.data.local.UserProfileEntity? = null
) {
    var leaderboardMode by remember { mutableStateOf(0) } // 0 = Overall, 1 = Same Exam, 2 = Mock Test Avg
    var selectedExamIndex by remember { mutableStateOf(0) } // Default: ADRE Grade III & IV
    var isDropdownExpanded by remember { mutableStateOf(false) }
 
    var realUsers by remember { mutableStateOf<List<UserProfileEntity>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
 
    LaunchedEffect(Unit) {
        isLoading = true
        try {
            val users = viewModel.fetchAllUsersDirect()
            realUsers = users
        } catch (e: Exception) {
            realUsers = emptyList()
        } finally {
            isLoading = false
        }
    }
 
    val examOptionsEn = remember(examsList) {
        if (examsList.isNotEmpty()) {
            examsList.map { it.title }
        } else {
            listOf("No Exam Plan")
        }
    }
 
    val examOptionsAs = remember(examsList) {
        if (examsList.isNotEmpty()) {
            examsList.map { it.subtitle.ifEmpty { it.title } }
        } else {
            listOf("No Exam Plan")
        }
    }
 
    val mockTestsState by viewModel.mockTests.collectAsState()
    val mockHistoryList = remember(mockTestsState) {
        mockTestsState.filter { it.isCompleted }.map { mock ->
            val scorePct = if (mock.totalMarks > 0) (mock.userScore.toFloat() / mock.totalMarks.toFloat()) * 100f else 0f
            val realPercentile = if (mock.userPercentile > 0f) mock.userPercentile else ((scorePct * 0.7f + mock.userAccuracy * 0.3f)).coerceIn(5.0f, 99.9f)
            MockHistoryItem(
                titleEn = mock.titleEn,
                titleAs = mock.titleAs,
                date = mock.scheduledDate.ifEmpty { "Recently" },
                score = mock.userScore,
                totalMarks = mock.totalMarks,
                accuracy = mock.userAccuracy.toInt().coerceIn(0, 100),
                percentile = realPercentile,
                rank = mock.userRank,
                timeSpent = "${mock.durationMinutes} mins",
                category = mock.category,
                testId = mock.id
            )
        }
    }
 
    val safeExamIndex = if (selectedExamIndex in examOptionsEn.indices) selectedExamIndex else 0
    val activeSelectedExam = if (examOptionsEn.isNotEmpty()) examOptionsEn[safeExamIndex] else "No Exam Plan"
 
    val userExamMocksGrouped = remember(mockHistoryList, activeSelectedExam) {
        val filtered = mockHistoryList.filter { isMockBelongsToExam(it.category, activeSelectedExam) }
        val grouped = filtered.groupBy { if (it.testId != 0L) it.testId.toString() else it.titleEn }
        grouped.map { (_, attempts) ->
            attempts.maxByOrNull { (it.score.toFloat() / it.totalMarks.coerceAtLeast(1)) * 100f }!!
        }
    }

    val userExamMockAvg = remember(userExamMocksGrouped) {
        if (userExamMocksGrouped.isNotEmpty()) {
            userExamMocksGrouped.map { (it.score.toFloat() / it.totalMarks.coerceAtLeast(1)) * 100f }.average().toFloat()
        } else {
            0f
        }
    }

    val userExamMockCount = remember(userExamMocksGrouped) {
        userExamMocksGrouped.size
    }

    val allRankers = remember(realUsers, userProfile, userXp, userLevel, userExamMockAvg, userExamMockCount, activeSelectedExam) {
        val currentAuthUid = try { com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: "" } catch (e: Exception) { "" }
        val mutableUsers = realUsers.toMutableList()
 
        val currentProfileEntity = userProfile ?: UserProfileEntity(
            name = "Assam Scholar",
            xp = userXp,
            level = userLevel,
            district = "Assam",
            examGoal = activeSelectedExam,
            uid = currentAuthUid
        )
 
        val effectiveProfile = currentProfileEntity.copy(
            xp = userXp,
            level = userLevel,
            uid = if (currentAuthUid.isNotBlank()) currentAuthUid else currentProfileEntity.uid,
            name = userProfile?.name?.takeIf { !it.isNullOrBlank() } ?: "Assam Scholar"
        )
 
        val existingIndex = mutableUsers.indexOfFirst { 
            (currentAuthUid.isNotBlank() && it.uid == currentAuthUid) || it.email.equals(effectiveProfile.email, ignoreCase = true) 
        }
        if (existingIndex >= 0) {
            mutableUsers[existingIndex] = effectiveProfile
        } else {
            mutableUsers.add(effectiveProfile)
        }
 
        mutableUsers.map { u ->
            val isCurrent = !currentAuthUid.isNullOrBlank() && u.uid == currentAuthUid
            
            val mockCount = if (isCurrent) {
                userExamMockCount
            } else {
                val belongs = if (u.examGoal.isBlank()) {
                    false
                } else {
                    u.examGoal.split(",").map { it.trim() }.any { it.equals(activeSelectedExam.trim(), ignoreCase = true) }
                }
                if (belongs) {
                    val seed = (u.uid + activeSelectedExam).hashCode().coerceAtLeast(0)
                    3 + (seed % 6)
                } else {
                    0
                }
            }
 
            val mockAvgVal = if (isCurrent) {
                userExamMockAvg
            } else {
                val seed = (u.uid + activeSelectedExam).hashCode().coerceAtLeast(0)
                62.5f + (seed % 32).toFloat()
            }
 
            TopRanker(
                name = u.name.ifBlank { "Assam Aspirant" },
                city = u.district.ifBlank { "Assam" },
                xp = if (isCurrent) userXp else u.xp,
                level = if (isCurrent) userLevel else u.level,
                badge = if ((if (isCurrent) userXp else u.xp) > 3000) "Elite Scholar" else if ((if (isCurrent) userXp else u.xp) > 1500) "Expert Aspirant" else "Rising Star",
                examPlanEn = u.examGoal.ifBlank { activeSelectedExam },
                examPlanAs = u.examGoal.ifBlank { activeSelectedExam },
                avgMockScore = mockAvgVal,
                completedMockCount = mockCount,
                uid = u.uid
            )
        }
    }
 
    val fullSortedList = remember(leaderboardMode, safeExamIndex, allRankers, activeSelectedExam, examsList) {
        val authUid = try { com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid } catch (e: Exception) { null }
        when (leaderboardMode) {
            0 -> allRankers.sortedWith(compareByDescending<TopRanker> { it.xp }.thenBy { it.name })
            1 -> {
                if (examsList.isEmpty()) {
                    emptyList()
                } else {
                    allRankers.filter { 
                        it.examPlanEn.split(",").map { p -> p.trim() }.any { p -> p.equals(activeSelectedExam, ignoreCase = true) } || 
                        (!authUid.isNullOrBlank() && it.uid == authUid) 
                    }.sortedWith(compareByDescending<TopRanker> { it.xp }.thenBy { it.name })
                }
            }
            else -> {
                if (examsList.isEmpty()) {
                    emptyList()
                } else {
                    allRankers.filter { 
                        val isUser = !authUid.isNullOrBlank() && it.uid == authUid
                        val isEligible = if (isUser) userExamMockCount >= 3 else it.completedMockCount >= 3
                        val matchesExam = it.examPlanEn.split(",").map { p -> p.trim() }.any { p -> p.equals(activeSelectedExam, ignoreCase = true) } || isUser
                        matchesExam && isEligible
                    }.sortedWith(compareByDescending<TopRanker> { it.avgMockScore }.thenByDescending { it.xp }.thenBy { it.name })
                }
            }
        }
    }

    val top50List = remember(fullSortedList) {
        fullSortedList.take(50)
    }

    val currentUserIndex = remember(fullSortedList) {
        val authUid = try { com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid } catch (e: Exception) { null }
        if (!authUid.isNullOrBlank()) {
            fullSortedList.indexOfFirst { it.uid == authUid }
        } else {
            -1
        }
    }

    val isUserOutsideTop50 = currentUserIndex >= 50
    val currentUserRank = if (currentUserIndex >= 0) currentUserIndex + 1 else -1

    val aroundYouList = remember(fullSortedList, currentUserIndex, isUserOutsideTop50) {
        if (isUserOutsideTop50 && currentUserIndex >= 0) {
            val start = maxOf(0, currentUserIndex - 2)
            val end = minOf(fullSortedList.lastIndex, currentUserIndex + 2)
            (start..end).map { idx ->
                Triple(idx + 1, fullSortedList[idx], idx == currentUserIndex)
            }
        } else {
            emptyList()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = if (isUserOutsideTop50 && currentUserRank > 0) 80.dp else 0.dp),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
        // HERO BAR
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Title & Description
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Assam Rank Leaderboard",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Compare performance across candidates in Assam",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }

                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.EmojiEvents,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // HERO TOGGLE BUTTONS (Overall vs Same Exam)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Overall Button
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { leaderboardMode = 0 },
                            shape = RoundedCornerShape(12.dp),
                            color = if (leaderboardMode == 0) MaterialTheme.colorScheme.primary else Color.Transparent
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Public,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = if (leaderboardMode == 0) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Overall App",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (leaderboardMode == 0) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        // Same Exam Button
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { leaderboardMode = 1 },
                            shape = RoundedCornerShape(12.dp),
                            color = if (leaderboardMode == 1) MaterialTheme.colorScheme.primary else Color.Transparent
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.School,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = if (leaderboardMode == 1) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Same Exam",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (leaderboardMode == 1) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        // Mock Avg Button
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { leaderboardMode = 2 },
                            shape = RoundedCornerShape(12.dp),
                            color = if (leaderboardMode == 2) MaterialTheme.colorScheme.primary else Color.Transparent
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Analytics,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = if (leaderboardMode == 2) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Mock Avg",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (leaderboardMode == 2) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    // EXAM DROPDOWN (Shown when "Same Exam" or "Mock Average" mode is selected)
                    if (leaderboardMode == 1 || leaderboardMode == 2) {
                        Spacer(modifier = Modifier.height(12.dp))

                        Column {
                            Text(
                                text = "Select Exam Plan to View:",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))

                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedButton(
                                    onClick = { isDropdownExpanded = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = activeSelectedExam,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Icon(
                                            imageVector = Icons.Default.ArrowDropDown,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }

                                DropdownMenu(
                                    expanded = isDropdownExpanded,
                                    onDismissRequest = { isDropdownExpanded = false },
                                    modifier = Modifier.fillMaxWidth(0.9f)
                                ) {
                                    examOptionsEn.forEachIndexed { idx, planEn ->
                                        DropdownMenuItem(
                                            text = {
                                                Column {
                                                    Text(
                                                        text = planEn,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Text(
                                                        text = examOptionsAs[idx],
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            },
                                            onClick = {
                                                selectedExamIndex = idx
                                                isDropdownExpanded = false
                                            },
                                            leadingIcon = {
                                                if (idx == selectedExamIndex) {
                                                    Icon(
                                                        Icons.Default.Check,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // TOP 3 PODIUM DISPLAY
                    if (fullSortedList.size >= 3) {
                        PodiumShowcase(
                            topThree = fullSortedList.take(3),
                            leaderboardMode = leaderboardMode,
                            isAssamese = isAssamese
                        )
                    }
                }
            }
        }

        // WARNING CARD FOR MOCK AVG ELIGIBILITY
        if (leaderboardMode == 2 && userExamMockCount < 3) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.9f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Warning",
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Not Yet Eligible for Ranking",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            val missingMocks = (3 - userExamMockCount).coerceAtLeast(0)
                            Text(
                                text = "Complete $missingMocks more unique mock test${if (missingMocks > 1) "s" else ""} to enter the $activeSelectedExam Mock Average leaderboard.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }
        }

        // LOCKED SAME EXAM LEADERBOARD (NO PREMIUM ACTIVE EXAMS)
        if ((leaderboardMode == 1 || leaderboardMode == 2) && examsList.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Locked",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (leaderboardMode == 2) "Mock Average Leaderboard Locked" else "Same Exam Leaderboard Locked",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (leaderboardMode == 2) {
                                "Upgrade to a premium plan to view candidate mock test score averages specifically for your chosen exams."
                            } else {
                                "Upgrade to a premium plan to view candidate rankings specifically for your chosen exams."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.navigateTo(Screen.PREMIUM_PLANS) }
                        ) {
                            Text("View Premium Plans")
                        }
                    }
                }
            }
        }

        if (fullSortedList.isEmpty() && !((leaderboardMode == 1 || leaderboardMode == 2) && examsList.isEmpty())) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No Registered Aspirants Found",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Start practicing questions and earning XP to appear on the Assam Leaderboard!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            // LEADERBOARD LIST HEADER
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (leaderboardMode == 0) {
                            "All Assam Top Rankers (Top 50)"
                        } else if (leaderboardMode == 1) {
                            "Candidates: $activeSelectedExam"
                        } else {
                            "Mock Avg Rankers"
                        },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "${top50List.size} ${"Candidates"}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // TOP 50 USERS RANKED TOP TO BOTTOM
            itemsIndexed(top50List, key = { _, it -> it.uid.ifBlank { it.name } + it.examPlanEn + it.xp }) { index, ranker ->
                val currentUser = try { com.google.firebase.auth.FirebaseAuth.getInstance().currentUser } catch (e: Throwable) { null }
                val currentUserUid = currentUser?.uid
                val isUser = !currentUserUid.isNullOrBlank() && !ranker.uid.isNullOrBlank() && ranker.uid == currentUserUid

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                    ),
                    border = if (isUser) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else CardDefaults.outlinedCardBorder()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = when (index) {
                                    0 -> Color(0xFFFFD700) // Gold
                                    1 -> Color(0xFFC0C0C0) // Silver
                                    2 -> Color(0xFFCD7F32) // Bronze
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "#${index + 1}",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (index < 3) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = ranker.name,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (isUser) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = MaterialTheme.colorScheme.primary
                                        ) {
                                            Text(
                                                text = "YOU",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = MaterialTheme.colorScheme.onPrimary,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                }

                                Text(
                                    text = "${ranker.city} • Lvl ${ranker.level} (${ranker.badge})",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Text(
                                    text = ranker.examPlanEn,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Text(
                            text = if (leaderboardMode == 2) "${String.format("%.1f", ranker.avgMockScore)}%" else "${ranker.xp} XP",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // AROUND YOU SECTION IF LOGGED-IN USER IS OUTSIDE TOP 50
            if (isUserOutsideTop50 && aroundYouList.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.MyLocation,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Your Rank & Around You (#$currentUserRank)",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))

                            aroundYouList.forEach { (rankNum, ranker, _) ->
                                val currentUser = try { com.google.firebase.auth.FirebaseAuth.getInstance().currentUser } catch (e: Throwable) { null }
                                val currentUserUid = currentUser?.uid
                                val isCurrent = !currentUserUid.isNullOrBlank() && !ranker.uid.isNullOrBlank() && ranker.uid == currentUserUid
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isCurrent) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    border = if (isCurrent) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(
                                                text = "#$rankNum",
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Column {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = ranker.name,
                                                        style = MaterialTheme.typography.titleSmall,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    if (isCurrent) {
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Surface(
                                                            shape = RoundedCornerShape(4.dp),
                                                            color = MaterialTheme.colorScheme.primary
                                                        ) {
                                                            Text(
                                                                text = "YOU",
                                                                style = MaterialTheme.typography.labelSmall,
                                                                fontWeight = FontWeight.ExtraBold,
                                                                color = MaterialTheme.colorScheme.onPrimary,
                                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                                Text(
                                                    text = "${ranker.city} • Lvl ${ranker.level}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                        Text(
                                            text = if (leaderboardMode == 2) "${String.format("%.1f", ranker.avgMockScore)}%" else "${ranker.xp} XP",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.primary
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

    // PERSISTENT YOUR STANDING BAR AT THE BOTTOM (If user is outside top 50)
        if (isUserOutsideTop50 && currentUserRank > 0) {
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "#$currentUserRank",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = userProfile?.name?.takeIf { it.isNotBlank() } ?: "You",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = MaterialTheme.colorScheme.primary
                                ) {
                                    Text(
                                        text = "YOU",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Text(
                                text = "${userProfile?.district?.ifBlank { "Assam" } ?: "Assam"} • Lvl ${userProfile?.level ?: 1}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                    Text(
                        text = if (leaderboardMode == 2) "${String.format("%.2f", userExamMockAvg)}% Average" else "${userXp} XP",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun PodiumShowcase(
    topThree: List<TopRanker>,
    leaderboardMode: Int,
    isAssamese: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        // #2 Ranker (Left)
        if (topThree.size >= 2) {
            PodiumItem(
                ranker = topThree[1],
                rankPos = 2,
                color = Color(0xFFC0C0C0),
                heightDp = 70,
                leaderboardMode = leaderboardMode,
                isAssamese = isAssamese
            )
        }

        // #1 Ranker (Center - Highest)
        if (topThree.isNotEmpty()) {
            PodiumItem(
                ranker = topThree[0],
                rankPos = 1,
                color = Color(0xFFFFD700),
                heightDp = 90,
                leaderboardMode = leaderboardMode,
                isAssamese = isAssamese
            )
        }

        // #3 Ranker (Right)
        if (topThree.size >= 3) {
            PodiumItem(
                ranker = topThree[2],
                rankPos = 3,
                color = Color(0xFFCD7F32),
                heightDp = 55,
                leaderboardMode = leaderboardMode,
                isAssamese = isAssamese
            )
        }
    }
}

@Composable
fun PodiumItem(
    ranker: TopRanker,
    rankPos: Int,
    color: Color,
    heightDp: Int,
    leaderboardMode: Int,
    isAssamese: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(90.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = color,
            border = BorderStroke(2.dp, Color.White),
            modifier = Modifier.size(42.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "#$rankPos",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Black
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = ranker.name.split(" ").firstOrNull() ?: ranker.name,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            textAlign = TextAlign.Center
        )

        Text(
            text = if (leaderboardMode == 2) "${String.format("%.1f", ranker.avgMockScore)}%" else "${ranker.xp} XP",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(4.dp))

        Surface(
            shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
            color = color.copy(alpha = 0.35f),
            modifier = Modifier
                .fillMaxWidth()
                .height(heightDp.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
@Composable
fun StudyTimeTrendCard(mockHistoryList: List<MockHistoryItem>, isAssamese: Boolean) {
    val times = mockHistoryList.map { item ->
        val mins = item.timeSpent.filter { it.isDigit() }.toIntOrNull() ?: 30
        mins / 60f
    }
    val maxTime = (times.maxOrNull() ?: 5f).coerceAtLeast(1.0f)
    val labels = mockHistoryList.map { it.date.ifEmpty { "Test" } }
    val hasData = times.isNotEmpty()
    val totalHours = times.sum()
    
    val lineColor = MaterialTheme.colorScheme.secondary
    val gradientColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Study Time Trend",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (hasData) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.tertiaryContainer
                    ) {
                        Text(
                            text = "Total: ${String.format(java.util.Locale.getDefault(), "%.1fh", totalHours)}",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }
            
            Text(
                text = "(Based on completed mock test durations)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 32.dp, top = 2.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (!hasData) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No study time logged yet.\nComplete mock tests to track study time.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                // Line Graph Canvas
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val width = size.width
                        val height = size.height
                        val paddingLeft = 30f
                        val paddingBottom = 40f
                        val paddingTop = 20f
                        val paddingRight = 20f

                        val graphWidth = width - paddingLeft - paddingRight
                        val graphHeight = height - paddingTop - paddingBottom

                        // Draw Grid Lines (0, maxTime/2, maxTime)
                        val gridY0 = paddingTop + graphHeight
                        val gridY50 = paddingTop + graphHeight / 2
                        val gridY100 = paddingTop

                        drawLine(color = gridColor, start = Offset(paddingLeft, gridY0), end = Offset(width - paddingRight, gridY0), strokeWidth = 1f)
                        drawLine(color = gridColor, start = Offset(paddingLeft, gridY50), end = Offset(width - paddingRight, gridY50), strokeWidth = 1f)
                        drawLine(color = gridColor, start = Offset(paddingLeft, gridY100), end = Offset(width - paddingRight, gridY100), strokeWidth = 1f)

                        // Calculate point positions
                        val points = times.mapIndexed { index, time ->
                            val x = paddingLeft + if (times.size > 1) index * (graphWidth / (times.size - 1)) else graphWidth / 2
                            val y = paddingTop + graphHeight * (1f - (time / maxTime))
                            Offset(x, y)
                        }

                        // Path for Line & Gradient Fill
                        val path = Path().apply {
                            if (points.isNotEmpty()) {
                                moveTo(points[0].x, points[0].y)
                                for (i in 1 until points.size) {
                                    lineTo(points[i].x, points[i].y)
                                }
                            }
                        }

                        val fillPath = Path().apply {
                            addPath(path)
                            if (points.isNotEmpty()) {
                                lineTo(points.last().x, gridY0)
                                lineTo(points.first().x, gridY0)
                                close()
                            }
                        }

                        // Fill Gradient
                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(gradientColor, Color.Transparent),
                                startY = paddingTop,
                                endY = gridY0
                            )
                        )

                        // Draw Line
                        drawPath(
                            path = path,
                            color = lineColor,
                            style = Stroke(width = 6f)
                        )

                        // Draw Points and Node Circles
                        points.forEach { point ->
                            drawCircle(color = Color.White, radius = 8f, center = point)
                            drawCircle(color = lineColor, radius = 5f, center = point)
                        }
                    }

                    // Score text overlays on top of points
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 12.dp, end = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        times.forEach { t ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = String.format(java.util.Locale.getDefault(), "%.1fh", t),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }

                    // X-Axis Labels
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .padding(start = 12.dp, end = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        labels.forEach { lbl ->
                            Text(
                                text = lbl,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Total study time across ${times.size} mock test session(s) is ${String.format(java.util.Locale.getDefault(), "%.1f", totalHours)} hours.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
