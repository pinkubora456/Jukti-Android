package com.example.ui.screens

import com.example.ui.components.SafeOutlinedTextField

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import com.example.ui.theme.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PracticeScreen(viewModel: JuktiViewModel, isSmartPractice: Boolean = false) {
    val language by viewModel.language.collectAsState()
    val questionLanguage by viewModel.questionLanguage.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val allQuestions by viewModel.questions.collectAsState()
    val smartPracticeQuestions by viewModel.smartPracticeQuestions.collectAsState()
    val hiddenQuestions by viewModel.hiddenQuestions.collectAsState()

    val hiddenIds = remember(hiddenQuestions) { hiddenQuestions.map { it.id }.toSet() }

    // Filter out hidden questions
    val visibleQuestions = remember(allQuestions, hiddenIds, isSmartPractice, smartPracticeQuestions) {
        val baseList = if (isSmartPractice) smartPracticeQuestions else allQuestions
        baseList.filter { !it.isHidden && it.id !in hiddenIds }.take(if (isSmartPractice) 10 else Int.MAX_VALUE)
    }

    var isSessionStarted by rememberSaveable { mutableStateOf(false) }
    var selectedSubjectKey by rememberSaveable { mutableStateOf("All Subjects") }
    var selectedChapters by remember { mutableStateOf<Set<String>>(emptySet()) }
    var showReportDialog by remember { mutableStateOf(false) }

    // State map for selected chapters per banner before session starts
    var chaptersMap by remember { mutableStateOf<Map<String, Set<String>>>(emptyMap()) }

    // Filter questions by selected subject and chapters
    val practiceQuestions = remember(visibleQuestions, selectedSubjectKey, selectedChapters) {
        visibleQuestions.filter { q ->
            try {
                val matchSubject = when (selectedSubjectKey) {
                    "All Subjects", "All Subject" -> true
                    "General Knowledge" -> q.subject in listOf("General Knowledge", "Assam History", "Assam Geography", "Assamese Literature & Culture", "Current Affairs")
                    "General English" -> q.subject == "General English"
                    "Mathematics", "General Mathematics" -> q.subject in listOf("General Mathematics", "Mathematics", "Quantitative Aptitude")
                    "Reasoning" -> q.subject in listOf("Reasoning", "Logical Reasoning", "Logical Reasoning & Mental Ability", "Mental Ability", "Logical Aptitude", "Reasoning & Mental Ability")
                    else -> q.subject.equals(selectedSubjectKey, ignoreCase = true)
                }
                val matchChapter = if (selectedChapters.isEmpty()) {
                    true
                } else {
                    val topicStr = q.topic ?: ""
                    if (selectedSubjectKey == "Reasoning") {
                        selectedChapters.any { ch ->
                            topicStr.contains(ch, ignoreCase = true) || 
                            ch.contains(topicStr, ignoreCase = true) ||
                            q.subject.contains(ch, ignoreCase = true) ||
                            (ch.contains("Coding", ignoreCase = true) && (topicStr.isBlank() || topicStr.contains("Code", ignoreCase = true) || topicStr.contains("Series", ignoreCase = true) || topicStr.contains("Analogy", ignoreCase = true))) ||
                            (ch.contains("Blood", ignoreCase = true) && (topicStr.contains("Blood", ignoreCase = true) || topicStr.contains("Direction", ignoreCase = true) || topicStr.contains("Relation", ignoreCase = true))) ||
                            (ch.contains("Seating", ignoreCase = true) && (topicStr.contains("Seat", ignoreCase = true) || topicStr.contains("Puzzle", ignoreCase = true) || topicStr.contains("Venn", ignoreCase = true))) ||
                            (ch.contains("Syllogism", ignoreCase = true) && (topicStr.contains("Syllogism", ignoreCase = true) || topicStr.contains("Statement", ignoreCase = true) || topicStr.contains("Assumption", ignoreCase = true)))
                        }
                    } else {
                        topicStr in selectedChapters || q.subject in selectedChapters
                    }
                }
                matchSubject && matchChapter
            } catch (e: Exception) {
                false
            }
        }
    }

    var currentQuestionIndex by remember { mutableIntStateOf(0) }
    var scoreCount by remember { mutableIntStateOf(0) }
    val totalPracticedCount = userProfile?.totalSolved ?: 0

    var showSummary by rememberSaveable { mutableStateOf(false) }
    val userAnswers = remember { mutableStateMapOf<Long, Int>() }

    var activeSessionQuestions by remember { mutableStateOf<List<QuestionEntity>>(emptyList()) }
    var lastStartingQuestionId by rememberSaveable { mutableLongStateOf(-1L) }

    LaunchedEffect(isSessionStarted, selectedSubjectKey, selectedChapters, isSmartPractice) {
        if (isSessionStarted) {
            val eligible = practiceQuestions.shuffled().toMutableList()
            if (eligible.size > 1 && eligible[0].id == lastStartingQuestionId) {
                val temp = eligible[0]
                eligible[0] = eligible[1]
                eligible[1] = temp
            }
            if (eligible.isNotEmpty()) {
                lastStartingQuestionId = eligible[0].id
            }
            activeSessionQuestions = eligible
            currentQuestionIndex = 0
        } else {
            activeSessionQuestions = emptyList()
        }
    }

    val displayQuestions = if (isSessionStarted && activeSessionQuestions.isNotEmpty()) activeSessionQuestions else practiceQuestions

    // Reset index if displayQuestions changes and out of bounds
    LaunchedEffect(displayQuestions.size) {
        if (currentQuestionIndex >= displayQuestions.size && displayQuestions.isNotEmpty()) {
            currentQuestionIndex = 0
        }
    }

    val currentQuestion = displayQuestions.getOrNull(currentQuestionIndex)
    val selectedOptionIndex = currentQuestion?.id?.let { userAnswers[it] }
    val isSubmitted = selectedOptionIndex != null

    // Per-question Timer
    var secondsElapsed by remember { mutableIntStateOf(0) }
    LaunchedEffect(currentQuestionIndex, isSubmitted, isSessionStarted) {
        secondsElapsed = 0
        if (isSessionStarted && !isSubmitted) {
            while (true) {
                delay(1000L)
                secondsElapsed++
            }
        }
    }

    LaunchedEffect(isSmartPractice, smartPracticeQuestions.size) {
        if (isSmartPractice) {
            isSessionStarted = true
        }
    }

    val isAssamese = language == AppLanguage.ASSAMESE
    val context = androidx.compose.ui.platform.LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Navigation Header
        val practiceTitle = if (isSessionStarted) {
            if (isSmartPractice) {
                "Smart Practice"
            } else {
                when (selectedSubjectKey) {
                    "General Knowledge" -> "General Knowledge"
                    "General English" -> "General English"
                    "Mathematics", "General Mathematics" -> "Mathematics"
                    "Reasoning" -> "Reasoning"
                    else -> "All Subjects"
                }
            }
        } else {
            "Practice"
        }

        val practiceSubtitle = if (isSessionStarted) {
            if (isSmartPractice) "Your personalized practice session" else {
                val chText = if (selectedChapters.isEmpty()) {
                    "All Chapters"
                } else {
                    "${selectedChapters.size} Chapters"
                }
                "$chText • ${practiceQuestions.size} Questions"
            }
        } else {
            "Select a subject banner to start practice"
        }

        com.example.ui.components.JuktiTopAppBar(
            title = practiceTitle,
            subtitle = practiceSubtitle,
            onBackClick = {
                if (isSessionStarted && !isSmartPractice) {
                    isSessionStarted = false
                } else {
                    viewModel.navigateTo(Screen.HOME)
                }
            },
            actions = {
                // Practiced Question Counter Badge
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Practiced: $totalPracticedCount",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        )

        Surface(
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 1.dp
        ) {
            Column {
                // Question Language Selector Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Translate,
                            contentDescription = "Language",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Question Language:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        FilterChip(
                            selected = questionLanguage == AppLanguage.ENGLISH,
                            onClick = { viewModel.setQuestionLanguage(AppLanguage.ENGLISH) },
                            label = { Text("English", fontSize = 11.sp) },
                            modifier = Modifier.height(30.dp)
                        )
                        FilterChip(
                            selected = questionLanguage == AppLanguage.ASSAMESE,
                            onClick = { viewModel.setQuestionLanguage(AppLanguage.ASSAMESE) },
                            label = { Text("অসমীয়া", fontSize = 11.sp) },
                            modifier = Modifier.height(30.dp)
                        )
                        FilterChip(
                            selected = questionLanguage == AppLanguage.BOTH,
                            onClick = { viewModel.setQuestionLanguage(AppLanguage.BOTH) },
                            label = { Text("Both", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            modifier = Modifier.height(30.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }
        }

        // BODY CONTENT
        if (!isSessionStarted) {
            // STEP 1: SHOW 5 SUBJECT BANNERS
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Choose Subject to Practice:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                // Define 5 Banners
                val banners = listOf(
                    BannerConfig(
                        titleEn = "All Subjects",
                        titleAs = "সকলো বিষয় (Mixed)",
                        subtitleEn = "Practice mixed MCQs from all subjects and chapters",
                        subtitleAs = "সকলো বিষয়ৰ সংমিশ্ৰিত প্ৰশ্নৰ অনুশীলন কৰক",
                        subjectKey = "All Subjects",
                        icon = Icons.Default.Apps,
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        iconColor = MaterialTheme.colorScheme.primary
                    ),
                    BannerConfig(
                        titleEn = "General Knowledge",
                        titleAs = "সাধাৰণ জ্ঞান (GK)",
                        subtitleEn = "History, Geography, Polity, Assam GK, Science & Static GK",
                        subtitleAs = "ইতিহাস, ভূগোল, ৰাজনীতি, অসম বিৱৰণ আৰু বিজ্ঞান",
                        subjectKey = "General Knowledge",
                        icon = Icons.Default.Public,
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                        iconColor = MaterialTheme.colorScheme.secondary
                    ),
                    BannerConfig(
                        titleEn = "General English",
                        titleAs = "সাধাৰণ ইংৰাজী",
                        subtitleEn = "Grammar, Vocabulary, Synonyms, Antonyms & Comprehension",
                        subtitleAs = "ইংৰাজী ব্যাকৰণ, শব্দকোষ, বিপৰীত শব্দ আৰু সমাৰ্থক শব্দ",
                        subjectKey = "General English",
                        icon = Icons.Default.Translate,
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f),
                        iconColor = MaterialTheme.colorScheme.tertiary
                    ),
                    BannerConfig(
                        titleEn = "Mathematics",
                        titleAs = "গণিত (Mathematics)",
                        subtitleEn = "Arithmetic, Quantitative Aptitude, Algebra & Geometry",
                        subtitleAs = "পাটিগণিত, বীজগণিত, জ্যামিতি আৰু সংখ্যা সংক্ৰান্তীয়",
                        subjectKey = "General Mathematics",
                        icon = Icons.Default.Calculate,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        iconColor = MaterialTheme.colorScheme.primary
                    ),
                    BannerConfig(
                        titleEn = "Reasoning",
                        titleAs = "যুক্তিবিদ্যা (Reasoning)",
                        subtitleEn = "Logical Aptitude, Verbal & Non-Verbal Reasoning",
                        subtitleAs = "মানসিক দক্ষতা আৰু যুক্তিনিৰ্ভৰ প্ৰশ্নৱালী",
                        subjectKey = "Reasoning",
                        icon = Icons.Default.Psychology,
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        iconColor = MaterialTheme.colorScheme.secondary
                    )
                )

                banners.forEach { banner ->
                    // Available chapters for this banner's subject
                    val availableChapters = remember(visibleQuestions, banner.subjectKey) {
                        val set = mutableSetOf<String>()
                        val relevant = when (banner.subjectKey) {
                            "All Subjects" -> visibleQuestions
                            "General Knowledge" -> visibleQuestions.filter { it.subject in listOf("General Knowledge", "Assam History", "Assam Geography", "Assamese Literature & Culture", "Current Affairs") }
                            "General English" -> visibleQuestions.filter { it.subject == "General English" }
                            "General Mathematics" -> visibleQuestions.filter { it.subject in listOf("General Mathematics", "Mathematics", "Quantitative Aptitude") }
                            "Reasoning" -> {
                                set.add("Coding-Decoding, Series & Analogy")
                                set.add("Blood Relations & Direction Sense Test")
                                set.add("Seating Arrangement, Puzzles & Venn Diagrams")
                                set.add("Syllogism, Statements & Assumptions")
                                visibleQuestions.filter { it.subject in listOf("Reasoning", "Logical Reasoning", "Logical Reasoning & Mental Ability", "Mental Ability", "Logical Aptitude", "Reasoning & Mental Ability") }
                            }
                            else -> visibleQuestions.filter { it.subject.equals(banner.subjectKey, ignoreCase = true) }
                        }
                        relevant.forEach { if (it.topic.isNotBlank()) set.add(it.topic) }
                        set.toList().sorted()
                    }

                    val questionCount = remember(visibleQuestions, banner.subjectKey) {
                        when (banner.subjectKey) {
                            "All Subjects" -> visibleQuestions.size
                            "General Knowledge" -> visibleQuestions.count { it.subject in listOf("General Knowledge", "Assam History", "Assam Geography", "Assamese Literature & Culture", "Current Affairs") }
                            "General English" -> visibleQuestions.count { it.subject == "General English" }
                            "General Mathematics" -> visibleQuestions.count { it.subject in listOf("General Mathematics", "Mathematics", "Quantitative Aptitude") }
                            "Reasoning" -> visibleQuestions.count { it.subject in listOf("Reasoning", "Logical Reasoning", "Logical Reasoning & Mental Ability", "Mental Ability", "Logical Aptitude", "Reasoning & Mental Ability") }
                            else -> visibleQuestions.count { it.subject.equals(banner.subjectKey, ignoreCase = true) }
                        }
                    }

                    val currentSelectedChapters = chaptersMap[banner.subjectKey] ?: emptySet()

                    PracticeSubjectBannerCard(
                        banner = banner,
                        availableChapters = availableChapters,
                        selectedChapters = currentSelectedChapters,
                        onChaptersChanged = { newSet ->
                            chaptersMap = chaptersMap + (banner.subjectKey to newSet)
                        },
                        totalQuestionsCount = questionCount,
                        actionButtonTextEn = "Start Practice",
                        actionButtonTextAs = "অনুশীলন আৰম্ভ কৰক",
                        onStartClick = {
                            selectedSubjectKey = banner.subjectKey
                            selectedChapters = chaptersMap[banner.subjectKey] ?: emptySet()
                            currentQuestionIndex = 0
                            showSummary = false
                            userAnswers.clear()
                            isSessionStarted = true
                        },
                        isAssamese = isAssamese
                    )
                }
            }
        } else if (showSummary) {
            PracticeSummaryView(
                questions = practiceQuestions,
                userAnswers = userAnswers,
                onFinish = {
                    showSummary = false
                    isSessionStarted = false
                    viewModel.navigateTo(Screen.HOME)
                },
                questionLanguage = questionLanguage,
                isSmartPractice = isSmartPractice
            )
        } else {
            // STEP 2: ACTIVE QUESTION AVAILABLE PAGE (Bigger view, NO Subject/Chapter selectors)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (practiceQuestions.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            com.example.ui.components.EmptyStateIllustration(
                                type = com.example.ui.components.EmptyStateType.RHINO_BOOK,
                                title = "No Questions Found",
                                message = "No questions found for the selected subject or chapter.",
                                modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp)
                            )
                            Button(onClick = { isSessionStarted = false }) {
                                Text("Back to Subjects")
                            }
                        }
                    }
                } else if (currentQuestion != null) {
                    // QUESTION CARD (Takes full screen width & height)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Top Question Meta Row (Count, Timer, Save & Hide Buttons)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Question Count Tag
                                Surface(
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "Q ${currentQuestionIndex + 1} of ${displayQuestions.size}",
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }

                                // Per-question Timer
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Timer,
                                            contentDescription = "Timer",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        val minutes = secondsElapsed / 60
                                        val secs = secondsElapsed % 60
                                        Text(
                                            text = String.format("%02d:%02d", minutes, secs),
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                // Action Icons: Save & Hide
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Report Button
                                    IconButton(
                                        onClick = { showReportDialog = true },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Report,
                                            contentDescription = "Report Question",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }

                                    // Save / Bookmark Button
                                    IconButton(
                                        onClick = { viewModel.toggleBookmarkQuestion(currentQuestion) },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (currentQuestion.isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                            contentDescription = "Save Question",
                                            tint = if (currentQuestion.isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    // Hide Question Button
                                    IconButton(
                                        onClick = {
                                            viewModel.toggleHideQuestion(currentQuestion)
                                            activeSessionQuestions = activeSessionQuestions.filter { it.id != currentQuestion.id }
                                            if (currentQuestionIndex >= displayQuestions.size - 1 && currentQuestionIndex > 0) {
                                                currentQuestionIndex--
                                            }
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.VisibilityOff,
                                            contentDescription = "Hide Question",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Progress bar for question count
                            LinearProgressIndicator(
                                progress = { (currentQuestionIndex + 1).toFloat() / displayQuestions.size.coerceAtLeast(1) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Subject & Topic Badge
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${currentQuestion.subject} • ${currentQuestion.topic}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Question Text
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

                            Spacer(modifier = Modifier.height(16.dp))

                            // Options List
                            val options = listOf(
                                currentQuestion.optionAEn to currentQuestion.optionAAs,
                                currentQuestion.optionBEn to currentQuestion.optionBAs,
                                currentQuestion.optionCEn to currentQuestion.optionCAs,
                                currentQuestion.optionDEn to currentQuestion.optionDAs
                            )

                            options.forEachIndexed { index, pair ->
                                val isSelected = (selectedOptionIndex == index)
                                val isCorrect = (index == currentQuestion.correctOptionIndex)
                                val optionLetter = ('A' + index).toString()

                                val backgroundColor = when {
                                    isSubmitted && isCorrect -> MaterialTheme.colorScheme.successContainer
                                    isSubmitted && isSelected && !isCorrect -> MaterialTheme.colorScheme.errorContainer
                                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                }

                                val borderColor = when {
                                    isSubmitted && isCorrect -> MaterialTheme.colorScheme.success
                                    isSubmitted && isSelected && !isCorrect -> MaterialTheme.colorScheme.error
                                    else -> null
                                }

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clickable(enabled = !isSubmitted) {
                                            userAnswers[currentQuestion.id] = index
                                            val isAnsCorrect = (index == currentQuestion.correctOptionIndex)
                                            if (isAnsCorrect) {
                                                scoreCount += 10
                                            }
                                            viewModel.submitQuestionAnswer(currentQuestion.id, isAnsCorrect, 10)
                                        },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = backgroundColor),
                                    border = borderColor?.let { BorderStroke(1.5.dp, it) }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = when {
                                                isSubmitted && isCorrect -> MaterialTheme.colorScheme.success
                                                isSubmitted && isSelected && !isCorrect -> MaterialTheme.colorScheme.error
                                                else -> MaterialTheme.colorScheme.primaryContainer
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                if (isSubmitted && isCorrect) {
                                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                                } else if (isSubmitted && isSelected && !isCorrect) {
                                                    Icon(Icons.Default.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                                } else {
                                                    Text(
                                                        text = optionLetter,
                                                        style = MaterialTheme.typography.labelMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        BilingualText(
                                            textEn = pair.first,
                                            textAs = pair.second,
                                            language = questionLanguage,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (isSubmitted && isCorrect) FontWeight.Bold else FontWeight.Normal,
                                            color = when {
                                                isSubmitted && isCorrect -> MaterialTheme.colorScheme.onSuccessContainer
                                                isSubmitted && isSelected && !isCorrect -> MaterialTheme.colorScheme.onErrorContainer
                                                else -> Color.Unspecified
                                            }
                                        )
                                    }
                                }
                            }

                            // Explanation Card
                            AnimatedVisibility(visible = isSubmitted) {
                                Column {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Surface(
                                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Lightbulb, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "Explanation & Analysis:",
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(6.dp))
                                            BilingualText(
                                                textEn = currentQuestion.explanationEn,
                                                textAs = currentQuestion.explanationAs,
                                                language = questionLanguage,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Navigation Buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        if (currentQuestionIndex > 0) {
                                            currentQuestionIndex--
                                        }
                                    },
                                    enabled = currentQuestionIndex > 0
                                ) {
                                    Icon(Icons.Default.ChevronLeft, contentDescription = null)
                                    Text("Previous")
                                }

                                Button(
                                    onClick = {
                                        if (currentQuestionIndex < displayQuestions.size - 1) {
                                            currentQuestionIndex++
                                        } else {
                                            viewModel.awardChapterCompletionXp()
                                            showSummary = true
                                        }
                                    }
                                ) {
                                    Text(
                                        if (currentQuestionIndex == displayQuestions.size - 1) {
                                            "Completed"
                                        } else {
                                            "Next"
                                        }
                                    )
                                    Icon(Icons.Default.ChevronRight, contentDescription = null)
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))

                            TextButton(
                                onClick = { showSummary = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("End Practice", color = MaterialTheme.colorScheme.error)
                            }
                        }
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

private data class BannerConfig(
    val titleEn: String,
    val titleAs: String,
    val subtitleEn: String,
    val subtitleAs: String,
    val subjectKey: String,
    val icon: ImageVector,
    val containerColor: Color,
    val iconColor: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PracticeSubjectBannerCard(
    banner: BannerConfig,
    availableChapters: List<String>,
    selectedChapters: Set<String>,
    onChaptersChanged: (Set<String>) -> Unit,
    totalQuestionsCount: Int,
    actionButtonTextEn: String,
    actionButtonTextAs: String,
    onStartClick: () -> Unit,
    isAssamese: Boolean
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = banner.containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header: Icon + Title + Subtitle + Total Count
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = banner.iconColor,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = banner.icon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = banner.titleEn,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Surface(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "$totalQuestionsCount Qs",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Text(
                        text = banner.subtitleEn,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Chapter Dropdown (Single / Multiple selection)
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                val labelText = when {
                    selectedChapters.isEmpty() -> "All Chapters Selected (Mix All)"
                    selectedChapters.size == 1 -> selectedChapters.first()
                    else -> "${selectedChapters.size} Chapters Selected"
                }

                SafeOutlinedTextField(
                    value = labelText,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Select Chapters (Single/Multiple)", fontSize = 12.sp) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        focusedBorderColor = MaterialTheme.colorScheme.primary
                    ),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.heightIn(max = 280.dp)
                ) {
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = selectedChapters.isEmpty(),
                                    onCheckedChange = null
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "All Chapters (Mix All)",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        },
                        onClick = {
                            onChaptersChanged(emptySet())
                            expanded = false
                        }
                    )

                    if (availableChapters.isNotEmpty()) {
                        HorizontalDivider()
                        availableChapters.forEach { chapter ->
                            val isChecked = selectedChapters.contains(chapter)
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(
                                            checked = isChecked,
                                            onCheckedChange = null
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = chapter)
                                    }
                                },
                                onClick = {
                                    val updated = if (isChecked) selectedChapters - chapter else selectedChapters + chapter
                                    onChaptersChanged(updated)
                                }
                            )
                        }
                    }
                }
            }

            // Display Chips if chapters selected
            if (selectedChapters.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    selectedChapters.forEach { ch ->
                        InputChip(
                            selected = true,
                            onClick = { onChaptersChanged(selectedChapters - ch) },
                            label = { Text(ch, style = MaterialTheme.typography.labelSmall) },
                            trailingIcon = {
                                Icon(Icons.Default.Close, contentDescription = "Remove", modifier = Modifier.size(12.dp))
                            }
                        )
                    }
                }
            }

            // Start Option Button
            Button(
                onClick = onStartClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = banner.iconColor)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = actionButtonTextEn,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

@Composable
fun PracticeSummaryView(
    questions: List<com.example.data.local.QuestionEntity>,
    userAnswers: Map<Long, Int>,
    onFinish: () -> Unit,
    questionLanguage: AppLanguage,
    isSmartPractice: Boolean = false
) {
    var correctCount = 0
    var incorrectCount = 0
    val unattemptedCount = questions.size - userAnswers.size

    val answeredQuestions = mutableListOf<Pair<com.example.data.local.QuestionEntity, Boolean>>()

    questions.forEach { q ->
        val answerIndex = userAnswers[q.id]
        if (answerIndex != null) {
            val isCorrect = answerIndex == q.correctOptionIndex
            if (isCorrect) correctCount++ else incorrectCount++
            answeredQuestions.add(q to isCorrect)
        }
    }

    val totalAttempted = correctCount + incorrectCount
    val accuracy = if (totalAttempted > 0) (correctCount.toFloat() / totalAttempted * 100).toInt() else 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = if (isSmartPractice) "Smart Practice Complete 🎯" else "Practice Summary",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )

        Text(
            text = "${questions.size} Questions Practiced",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = "Accuracy: $accuracy%",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            SummaryStatCard(title = "Correct", count = correctCount, color = MaterialTheme.colorScheme.success)
            SummaryStatCard(title = "Incorrect", count = incorrectCount, color = MaterialTheme.colorScheme.error)
            SummaryStatCard(title = "Skipped", count = unattemptedCount, color = MaterialTheme.colorScheme.outline)
        }

        Button(
            onClick = onFinish,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        ) {
            Text("Finish Practice")
        }

        if (answeredQuestions.isNotEmpty()) {
            Text(
                text = "Detailed Review",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(top = 16.dp, bottom = 8.dp)
            )

            answeredQuestions.forEachIndexed { index, (q, isCorrect) ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isCorrect) MaterialTheme.colorScheme.successContainer.copy(alpha = 0.2f)
                        else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (isCorrect) MaterialTheme.colorScheme.success else MaterialTheme.colorScheme.error
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        com.example.ui.components.QuestionTypeBadge(
                            questionType = q.questionType,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                imageVector = if (isCorrect) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                contentDescription = null,
                                tint = if (isCorrect) MaterialTheme.colorScheme.success else MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(24.dp).padding(end = 8.dp)
                            )
                            BilingualText(
                                textEn = "${index + 1}. ${q.questionEn}",
                                textAs = "${index + 1}. ${q.questionAs}",
                                language = questionLanguage,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        val selectedAnsIndex = userAnswers[q.id] ?: -1
                        val selectedAnsEn = when (selectedAnsIndex) {
                            0 -> q.optionAEn; 1 -> q.optionBEn; 2 -> q.optionCEn; 3 -> q.optionDEn; else -> ""
                        }
                        val correctAnsEn = when (q.correctOptionIndex) {
                            0 -> q.optionAEn; 1 -> q.optionBEn; 2 -> q.optionCEn; 3 -> q.optionDEn; else -> ""
                        }

                        Text(
                            text = "Your Answer: $selectedAnsEn",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isCorrect) MaterialTheme.colorScheme.success else MaterialTheme.colorScheme.error
                        )
                        if (!isCorrect) {
                            Text(
                                text = "Correct Answer: $correctAnsEn",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.success
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryStatCard(title: String, count: Int, color: Color) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        border = BorderStroke(1.dp, color)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = count.toString(), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = color)
            Text(text = title, style = MaterialTheme.typography.labelMedium, color = color)
        }
    }
}
