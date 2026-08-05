package com.example.ui.screens

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.BilingualText
import com.example.ui.components.ReportQuestionDialog
import com.example.ui.viewmodel.AppLanguage
import com.example.ui.viewmodel.JuktiViewModel
import com.example.ui.viewmodel.Screen
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PracticeScreen(viewModel: JuktiViewModel) {
    val language by viewModel.language.collectAsState()
    val questionLanguage by viewModel.questionLanguage.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val allQuestions by viewModel.questions.collectAsState()
    val hiddenQuestions by viewModel.hiddenQuestions.collectAsState()

    val hiddenIds = remember(hiddenQuestions) { hiddenQuestions.map { it.id }.toSet() }

    // Filter out hidden questions
    val visibleQuestions = remember(allQuestions, hiddenIds) {
        allQuestions.filter { !it.isHidden && it.id !in hiddenIds }
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
            val matchSubject = when (selectedSubjectKey) {
                "All Subjects", "All Subject" -> true
                "General Knowledge" -> q.subject in listOf("General Knowledge", "Assam History", "Assam Geography", "Assamese Literature & Culture", "Current Affairs")
                "General English" -> q.subject == "General English"
                "Mathematics", "General Mathematics" -> q.subject in listOf("General Mathematics", "Mathematics", "Quantitative Aptitude")
                "Reasoning" -> q.subject == "Reasoning"
                else -> q.subject.equals(selectedSubjectKey, ignoreCase = true)
            }
            val matchChapter = if (selectedChapters.isEmpty()) {
                true
            } else {
                q.topic in selectedChapters || q.subject in selectedChapters
            }
            matchSubject && matchChapter
        }
    }

    var currentQuestionIndex by remember { mutableIntStateOf(0) }
    var selectedOptionIndex by remember { mutableStateOf<Int?>(null) }
    var isSubmitted by remember { mutableStateOf(false) }
    var scoreCount by remember { mutableIntStateOf(0) }
    val totalPracticedCount = userProfile?.totalSolved ?: 0

    // Reset index if practiceQuestions changes and out of bounds
    LaunchedEffect(practiceQuestions.size) {
        if (currentQuestionIndex >= practiceQuestions.size && practiceQuestions.isNotEmpty()) {
            currentQuestionIndex = 0
            selectedOptionIndex = null
            isSubmitted = false
        }
    }

    val currentQuestion = practiceQuestions.getOrNull(currentQuestionIndex)

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

    val isAssamese = language == AppLanguage.ASSAMESE
    val context = androidx.compose.ui.platform.LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Navigation Header
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 3.dp
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        if (isSessionStarted) {
                            isSessionStarted = false
                        } else {
                            viewModel.navigateTo(Screen.HOME)
                        }
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isSessionStarted) {
                                when (selectedSubjectKey) {
                                    "General Knowledge" -> if (isAssamese) "সাধাৰণ জ্ঞান (GK)" else "General Knowledge"
                                    "General English" -> if (isAssamese) "সাধাৰণ ইংৰাজী" else "General English"
                                    "Mathematics", "General Mathematics" -> if (isAssamese) "গণিত (Mathematics)" else "Mathematics"
                                    "Reasoning" -> if (isAssamese) "যুক্তিবিদ্যা" else "Reasoning"
                                    else -> if (isAssamese) "সকলো বিষয়" else "All Subjects"
                                }
                            } else {
                                if (isAssamese) "অনুশীলন মডিউল" else "Practice MCQ"
                            },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = if (isSessionStarted) {
                                val chText = if (selectedChapters.isEmpty()) {
                                    if (isAssamese) "সকলো অধ্যায় (Mix All)" else "All Chapters"
                                } else {
                                    if (isAssamese) "${selectedChapters.size} টা অধ্যায়" else "${selectedChapters.size} Chapters"
                                }
                                "$chText • ${practiceQuestions.size} ${if (isAssamese) "প্ৰশ্ন" else "Questions"}"
                            } else {
                                if (isAssamese) "বিষয় নিৰ্বাচন কৰি অনুশীলন কৰক" else "Select a subject banner to start practice"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        // Practiced Question Counter Badge
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(16.dp)
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
                                    text = if (isAssamese) "অনুশীলন: $totalPracticedCount" else "Practiced: $totalPracticedCount",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

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
                            text = if (isAssamese) "প্ৰশ্নৰ ভাষা:" else "Question Language:",
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
                    text = if (isAssamese) "অনুশীলন বাবে বিষয় বাছনি কৰক:" else "Choose Subject to Practice:",
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
                            "Reasoning" -> visibleQuestions.filter { it.subject == "Reasoning" }
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
                            "Reasoning" -> visibleQuestions.count { it.subject == "Reasoning" }
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
                            selectedOptionIndex = null
                            isSubmitted = false
                            isSessionStarted = true
                        },
                        isAssamese = isAssamese
                    )
                }
            }
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
                                title = if (isAssamese) "কোনো প্ৰশ্ন উপলব্ধ নাই" else "No Questions Found",
                                message = if (isAssamese) "এই বিষয় বা অধ্যায়ৰ বাবে কোনো প্ৰশ্ন উপলব্ধ নাই।" else "No questions found for the selected subject or chapter.",
                                modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp)
                            )
                            Button(onClick = { isSessionStarted = false }) {
                                Text(if (isAssamese) "পাছলৈ যাওক" else "Back to Subjects")
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
                                        text = "Q ${currentQuestionIndex + 1} of ${practiceQuestions.size}",
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
                                            if (currentQuestionIndex < practiceQuestions.size - 1) {
                                                selectedOptionIndex = null
                                                isSubmitted = false
                                            } else if (currentQuestionIndex > 0) {
                                                currentQuestionIndex--
                                                selectedOptionIndex = null
                                                isSubmitted = false
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
                                progress = { (currentQuestionIndex + 1).toFloat() / practiceQuestions.size.coerceAtLeast(1) },
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

                                Text(
                                    text = if (currentQuestion.examCategory.isNotBlank()) currentQuestion.examCategory else "Practice",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Question Text
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
                                            selectedOptionIndex = index
                                            isSubmitted = true
                                            viewModel.recordStudyProgress(1, 10)
                                            val isAnsCorrect = (index == currentQuestion.correctOptionIndex)
                                            if (isAnsCorrect) {
                                                scoreCount += 10
                                            }
                                            viewModel.submitQuestionAnswer(currentQuestion.id, isAnsCorrect)
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
                                                    text = if (isAssamese) "উত্তৰৰ ব্যাখ্যা (Explanation):" else "Explanation & Analysis:",
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
                                            selectedOptionIndex = null
                                            isSubmitted = false
                                        }
                                    },
                                    enabled = currentQuestionIndex > 0
                                ) {
                                    Icon(Icons.Default.ChevronLeft, contentDescription = null)
                                    Text(if (isAssamese) "পূৰ্বৱৰ্তী" else "Previous")
                                }

                                Button(
                                    onClick = {
                                        if (currentQuestionIndex < practiceQuestions.size - 1) {
                                            currentQuestionIndex++
                                            selectedOptionIndex = null
                                            isSubmitted = false
                                        } else {
                                            viewModel.awardChapterCompletionXp()
                                            viewModel.navigateTo(Screen.HOME)
                                        }
                                    }
                                ) {
                                    Text(
                                        if (currentQuestionIndex == practiceQuestions.size - 1) {
                                            if (isAssamese) "সমাপ্ত" else "Completed"
                                        } else {
                                            if (isAssamese) "পৰৱৰ্তী" else "Next"
                                        }
                                    )
                                    Icon(Icons.Default.ChevronRight, contentDescription = null)
                                }
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
                            text = if (isAssamese) banner.titleAs else banner.titleEn,
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
                        text = if (isAssamese) banner.subtitleAs else banner.subtitleEn,
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
                    selectedChapters.isEmpty() -> if (isAssamese) "সকলো অধ্যায় নিৰ্বাচিত (Mix All)" else "All Chapters Selected (Mix All)"
                    selectedChapters.size == 1 -> selectedChapters.first()
                    else -> if (isAssamese) "${selectedChapters.size} টা অধ্যায় নিৰ্বাচিত" else "${selectedChapters.size} Chapters Selected"
                }

                OutlinedTextField(
                    value = labelText,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(if (isAssamese) "অধ্যায় বাছনি (একক/বহু)" else "Select Chapters (Single/Multiple)", fontSize = 12.sp) },
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
                                    text = if (isAssamese) "সকলো অধ্যায় (Mix All)" else "All Chapters (Mix All)",
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
                        text = if (isAssamese) actionButtonTextAs else actionButtonTextEn,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}
