package com.example.ui.screens

import com.example.ui.components.SafeOutlinedTextField

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.QuestionEntity
import com.example.ui.components.BilingualText
import com.example.ui.components.ReportQuestionDialog
import com.example.ui.viewmodel.AppLanguage
import com.example.ui.viewmodel.JuktiViewModel
import com.example.ui.viewmodel.Screen
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class BannerConfig(
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
fun PracticeScreen(
    viewModel: JuktiViewModel,
    isSmartPractice: Boolean = false,
    isSavedPractice: Boolean = false
) {
    val language = com.example.ui.viewmodel.AppLanguage.ENGLISH
    val questionLanguage by viewModel.questionLanguage.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val isUserPremium by viewModel.isUserPremium.collectAsState()
    val isAdminOrOwner by viewModel.isAdminOrOwner.collectAsState()
    val allQuestions by viewModel.accessibleQuestions.collectAsState()
    val smartPracticeQuestions by viewModel.smartPracticeQuestions.collectAsState()
    val bookmarkedQuestions by viewModel.bookmarkedQuestions.collectAsState()
    val bookmarkedIds by viewModel.bookmarkedIds.collectAsState()
    val allSubjectsChapters by viewModel.allSubjectsChapters.collectAsState()
    val activePracticeQuestion by viewModel.activePracticeQuestion.collectAsState()

    val visibleQuestions by produceState(
        initialValue = emptyList<QuestionEntity>(),
        allQuestions, isSmartPractice, isSavedPractice, smartPracticeQuestions, bookmarkedQuestions
    ) {
        value = withContext(Dispatchers.Default) {
            when {
                isSavedPractice -> bookmarkedQuestions
                isSmartPractice -> smartPracticeQuestions.take(10)
                else -> allQuestions
            }
        }
    }

    var isSessionStarted by rememberSaveable { mutableStateOf(false) }
    var selectedSubjectKey by rememberSaveable { mutableStateOf("All Subjects") }
    var selectedChapters by remember { mutableStateOf<Set<String>>(emptySet()) }
    var showReportDialog by remember { mutableStateOf(false) }
    var showEndPracticeConfirmDialog by remember { mutableStateOf(false) }

    // Sync isSessionStarted with viewModel.isStudySessionActive to manage global bottom navigation bar
    LaunchedEffect(isSessionStarted) {
        viewModel.setStudySessionActive(isSessionStarted)
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.setStudySessionActive(false)
        }
    }

    LaunchedEffect(activePracticeQuestion) {
        if (activePracticeQuestion != null) {
            selectedSubjectKey = activePracticeQuestion!!.subject
            isSessionStarted = true
        }
    }

    // State map for selected chapters per banner before session starts
    var chaptersMap by remember { mutableStateOf<Map<String, Set<String>>>(emptyMap()) }

    // Filter questions by selected subject and chapters asynchronously on Dispatchers.Default
    val practiceQuestions by produceState(
        initialValue = emptyList<QuestionEntity>(),
        visibleQuestions, selectedSubjectKey, selectedChapters, isSavedPractice
    ) {
        value = withContext(Dispatchers.Default) {
            if (isSavedPractice) {
                visibleQuestions
            } else {
                visibleQuestions.filter { q ->
                    try {
                        val matchSubject = isQuestionInSubject(q, selectedSubjectKey)
                        val matchChapter = if (selectedChapters.isEmpty()) {
                            true
                        } else {
                            val topicStr = q.topic ?: ""
                            val qSubject = q.subject ?: ""
                            val normTopic = com.example.data.repository.normalizeChapterName(topicStr, qSubject)

                            selectedChapters.any { rawCh ->
                                val selSubj = if (rawCh.contains(": ")) rawCh.substringBefore(": ").trim() else ""
                                val ch = if (rawCh.contains(": ")) rawCh.substringAfter(": ").trim() else rawCh.trim()

                                val subjectMatches = if (selSubj.isNotBlank()) {
                                    isQuestionInSubject(q, selSubj)
                                } else {
                                    true
                                }

                                if (!subjectMatches) return@any false

                                val nCh = com.example.data.repository.normalizeChapterName(ch, qSubject)
                                normTopic.equals(nCh, ignoreCase = true) ||
                                (topicStr.isNotBlank() && ch.isNotBlank() && (
                                    topicStr.equals(ch, ignoreCase = true) ||
                                    topicStr.contains(ch, ignoreCase = true) ||
                                    ch.contains(topicStr, ignoreCase = true) ||
                                    normTopic.contains(nCh, ignoreCase = true) ||
                                    nCh.contains(normTopic, ignoreCase = true)
                                ))
                            }
                        }
                        
                        matchSubject && matchChapter
                    } catch (e: Exception) {
                        false
                    }
                }
            }
        }
    }

    val colorSurfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val colorPrimary = MaterialTheme.colorScheme.primary

    val bannersAndData = remember(visibleQuestions, allSubjectsChapters, colorSurfaceVariant, colorPrimary) {
        try {
            val predefined = listOf(
                BannerConfig(
                    titleEn = "General Knowledge",
                    titleAs = "সাধাৰণ জ্ঞান",
                    subtitleEn = "Assam history, geography, and more",
                    subtitleAs = "অসমৰ ইতিহাস, ভূগোল আৰু অন্যান্য",
                    subjectKey = "General Knowledge",
                    icon = Icons.Default.Public,
                    containerColor = colorSurfaceVariant,
                    iconColor = colorPrimary
                ),
                BannerConfig(
                    titleEn = "General English",
                    titleAs = "সাধাৰণ ইংৰাজী",
                    subtitleEn = "Grammar, vocabulary, and comprehension",
                    subtitleAs = "ব্যাকৰণ, শব্দভাণ্ডাৰ আৰু বুজাপৰা",
                    subjectKey = "General English",
                    icon = Icons.Default.MenuBook,
                    containerColor = colorSurfaceVariant,
                    iconColor = colorPrimary
                ),
                BannerConfig(
                    titleEn = "General Mathematics",
                    titleAs = "সাধাৰণ গণিত",
                    subtitleEn = "Arithmetic, algebra, and geometry",
                    subtitleAs = "পাটিগণিত, বীজগণিত আৰু জ্যামিতি",
                    subjectKey = "General Mathematics",
                    icon = Icons.Default.Calculate,
                    containerColor = colorSurfaceVariant,
                    iconColor = colorPrimary
                ),
                BannerConfig(
                    titleEn = "Reasoning & Mental Ability",
                    titleAs = "যুক্তিবিদ্যা (Reasoning)",
                    subtitleEn = "Logical and analytical reasoning",
                    subtitleAs = "যৌক্তিক আৰু বিশ্লেষণাত্মক যুক্তি",
                    subjectKey = "Reasoning & Mental Ability",
                    icon = Icons.Default.Psychology,
                    containerColor = colorSurfaceVariant,
                    iconColor = colorPrimary
                ),
                BannerConfig(
                    titleEn = "Transport & Motor Vehicle",
                    titleAs = "পৰিবহন আৰু মটৰ বাহন",
                    subtitleEn = "Motor vehicle act and traffic signs",
                    subtitleAs = "মটৰ বাহন আইন আৰু যান-বাহনৰ সংকেত",
                    subjectKey = "Transport & Motor Vehicle",
                    icon = Icons.Default.Traffic,
                    containerColor = colorSurfaceVariant,
                    iconColor = colorPrimary
                )
            )

            val existingKeys = predefined.map { it.subjectKey.lowercase() }.toMutableSet()
            existingKeys.add("all subjects")

            val dynamicSubjects = mutableSetOf<String>()
            allSubjectsChapters.forEach { if (it.subject.isNotBlank()) dynamicSubjects.add(it.subject) }
            visibleQuestions.forEach { if (it.subject.isNotBlank()) dynamicSubjects.add(it.subject) }

            val dynamicBanners = dynamicSubjects
                .filter { subj ->
                    val norm = subj.lowercase()
                    !existingKeys.contains(norm) &&
                    norm != "general knowledge" &&
                    norm != "general english" &&
                    norm != "english" &&
                    norm != "general mathematics" &&
                    norm != "mathematics" &&
                    norm != "reasoning" &&
                    norm != "reasoning & mental ability" &&
                    norm != "transport & motor vehicle" &&
                    norm != "voice" &&
                    norm != "basic computer"
                }
                .sorted()
                .map { subj ->
                    BannerConfig(
                        titleEn = subj,
                        titleAs = subj,
                        subtitleEn = "Practice questions for $subj",
                        subtitleAs = "$subj ৰ প্ৰশ্নসমূহ",
                        subjectKey = subj,
                        icon = Icons.Default.Book,
                        containerColor = colorSurfaceVariant,
                        iconColor = colorPrimary
                    )
                }

            val banners = predefined + dynamicBanners + listOf(
                BannerConfig(
                    titleEn = "All Subjects",
                    titleAs = "সকলো বিষয়",
                    subtitleEn = "Mixed questions from all subjects",
                    subtitleAs = "সকলো বিষয়ৰ পৰা মিশ্ৰিত প্ৰশ্ন",
                    subjectKey = "All Subjects",
                    icon = Icons.Default.AllInclusive,
                    containerColor = colorSurfaceVariant,
                    iconColor = colorPrimary
                )
            )

            val qIdToNormalizedTopic = visibleQuestions.associate { q ->
                q.id to com.example.data.repository.normalizeChapterName(q.topic ?: "", q.subject)
            }

            val bannerQsMap = banners.associateWith { banner ->
                visibleQuestions.filter { q -> isQuestionInSubject(q, banner.subjectKey) }
            }

            val bannerDataMap = banners.associate { banner ->
                val set = mutableSetOf<String>()
                val bannerQs = bannerQsMap[banner] ?: emptyList()

                when (banner.subjectKey) {
                    "All Subjects", "All Subject" -> {
                        allSubjectsChapters.forEach { sc ->
                            if (sc.chapter.isNotBlank()) {
                                set.add("${sc.subject}: ${sc.chapter}")
                            }
                        }
                        bannerQs.forEach { q ->
                            if (!q.topic.isNullOrBlank() && !q.subject.isNullOrBlank()) {
                                set.add("${q.subject}: ${q.topic}")
                            }
                        }
                    }
                    else -> {
                        bannerQs.forEach { q ->
                            val norm = qIdToNormalizedTopic[q.id]
                            if (norm != null && norm.isNotBlank()) set.add(norm)
                        }
                        allSubjectsChapters.filter { isQuestionSubjectMatch(it.subject, banner.subjectKey) }
                            .forEach { if (it.chapter.isNotBlank()) set.add(it.chapter) }
                    }
                }
                val availableChaptersList = set.toList().sorted()

                val chapterCountsMap = availableChaptersList.associateWith { rawCh ->
                    val selSubj = if (rawCh.contains(": ")) rawCh.substringBefore(": ").trim() else ""
                    val ch = if (rawCh.contains(": ")) rawCh.substringAfter(": ").trim() else rawCh.trim()

                    bannerQs.count { q ->
                        val qSubj = q.subject ?: ""
                        val topicStr = q.topic ?: ""
                        val normTopic = qIdToNormalizedTopic[q.id] ?: ""

                        val subjectMatches = if (selSubj.isNotBlank()) {
                            when (selSubj) {
                                "General Knowledge" -> qSubj in listOf("General Knowledge", "Assam History", "Assam Geography", "Assamese Literature & Culture", "Current Affairs")
                                "General English" -> qSubj.equals("General English", ignoreCase = true) || qSubj.equals("English", ignoreCase = true) || qSubj.contains("English", ignoreCase = true)
                                "General Mathematics", "Mathematics" -> qSubj in listOf("General Mathematics", "Mathematics", "Quantitative Aptitude")
                                "Reasoning", "Reasoning & Mental Ability" -> qSubj in listOf("Reasoning", "Logical Reasoning", "Logical Reasoning & Mental Ability", "Mental Ability", "Logical Aptitude", "Reasoning & Mental Ability")
                                "Transport & Motor Vehicle" -> qSubj.equals("Transport & Motor Vehicle", ignoreCase = true) || qSubj.contains("Transport", ignoreCase = true) || qSubj.contains("Motor Vehicle", ignoreCase = true)
                                else -> qSubj.equals(selSubj, ignoreCase = true) || qSubj.contains(selSubj, ignoreCase = true) || selSubj.contains(qSubj, ignoreCase = true)
                            }
                        } else true

                        if (!subjectMatches) false
                        else {
                            val normCh = com.example.data.repository.normalizeChapterName(ch, qSubj)
                            normTopic.equals(normCh, ignoreCase = true) ||
                            (topicStr.isNotBlank() && ch.isNotBlank() && (
                                topicStr.equals(ch, ignoreCase = true) ||
                                topicStr.contains(ch, ignoreCase = true) ||
                                ch.contains(topicStr, ignoreCase = true) ||
                                normTopic.contains(normCh, ignoreCase = true) ||
                                normCh.contains(normTopic, ignoreCase = true)
                            ))
                        }
                    }
                }

                banner.subjectKey to Triple(availableChaptersList, bannerQs, chapterCountsMap)
            }

            banners to bannerDataMap
        } catch (e: Exception) {
            emptyList<BannerConfig>() to emptyMap()
        }
    }
    val dynamicBanners = bannersAndData.first
    val precomputedBannerData = bannersAndData.second

    var currentQuestionIndex by rememberSaveable { mutableIntStateOf(0) }
    val totalPracticedCount = userProfile?.totalSolved ?: 0

    var showSummary by rememberSaveable { mutableStateOf(false) }
    val userAnswers = remember { mutableStateMapOf<Long, Int>() }

    var sessionTotalSeconds by rememberSaveable { mutableIntStateOf(0) }
    var activeSessionQuestions by remember { mutableStateOf<List<QuestionEntity>>(emptyList()) }
    var lastStartingQuestionId by rememberSaveable { mutableLongStateOf(-1L) }

    val sectionName = remember(isSavedPractice, isSmartPractice) {
        if (isSavedPractice) "SAVED_PRACTICE"
        else if (isSmartPractice) "SMART_PRACTICE"
        else "PRACTICE"
    }

    val scopeKeyName = remember(isSavedPractice, isSmartPractice, selectedSubjectKey, selectedChapters) {
        if (isSavedPractice) "ALL_SAVED"
        else if (isSmartPractice) "ALL_SMART"
        else com.example.data.repository.SessionDeckManager.buildScopeKey(selectedSubjectKey, selectedChapters)
    }

    LaunchedEffect(isSessionStarted, selectedSubjectKey, selectedChapters, isSmartPractice, isSavedPractice, activePracticeQuestion, practiceQuestions) {
        if (isSessionStarted) {
            userAnswers.clear()
            sessionTotalSeconds = 0
            val eligible = mutableListOf<QuestionEntity>()
            if (activePracticeQuestion != null) {
                eligible.add(activePracticeQuestion!!)
                val otherQs = practiceQuestions.filter { it.id != activePracticeQuestion!!.id }
                eligible.addAll(otherQs)
                activeSessionQuestions = eligible
                currentQuestionIndex = 0
            } else {
                val deckResult = viewModel.getOrUpdateSessionDeck(sectionName, scopeKeyName, practiceQuestions)
                activeSessionQuestions = deckResult.orderedQuestions
                currentQuestionIndex = deckResult.currentIndex
            }
        } else {
            activeSessionQuestions = emptyList()
        }
    }

    LaunchedEffect(isSessionStarted, currentQuestionIndex, activeSessionQuestions) {
        if (isSessionStarted && activeSessionQuestions.isNotEmpty() && activePracticeQuestion == null) {
            viewModel.saveSessionIndex(sectionName, scopeKeyName, currentQuestionIndex, activeSessionQuestions.size)
        }
    }

    // Session Live Timer
    LaunchedEffect(isSessionStarted, showSummary) {
        if (isSessionStarted && !showSummary) {
            while (true) {
                delay(1000L)
                sessionTotalSeconds++
            }
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

    // Real session correct and incorrect counts
    var correctCount = 0
    var incorrectCount = 0
    displayQuestions.forEach { q ->
        val ans = userAnswers[q.id]
        if (ans != null) {
            if (ans == q.correctOptionIndex) correctCount++ else incorrectCount++
        }
    }

    LaunchedEffect(isSmartPractice, isSavedPractice, smartPracticeQuestions.size, bookmarkedQuestions.size) {
        if (isSmartPractice || isSavedPractice) {
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
        val practiceTitle = if (showSummary) {
            "Practice Summary"
        } else if (isSessionStarted) {
            if (isSavedPractice) {
                "Saved Questions Practice"
            } else if (isSmartPractice) {
                "Smart Practice"
            } else {
                when (selectedSubjectKey) {
                    "General Knowledge" -> "General Knowledge"
                    "General English" -> "General English"
                    "Mathematics", "General Mathematics" -> "General Mathematics"
                    "Reasoning", "Reasoning & Mental Ability" -> "Reasoning & Mental Ability"
                    "Basic Computer", "Computer Knowledge", "Computer" -> "Basic Computer"
                    "Transport & Motor Vehicle", "Transport Rule", "Transport Rules" -> "Transport & Motor Vehicle"
                    "All Subjects", "All Subject" -> "All Subjects"
                    else -> selectedSubjectKey
                }
            }
        } else {
            "Practice"
        }

        val practiceSubtitle = if (showSummary) {
            "Performance Review"
        } else if (isSessionStarted) {
            if (isSavedPractice) {
                "${practiceQuestions.size} Saved Questions"
            } else if (isSmartPractice) {
                "Your personalized practice session"
            } else {
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

        val handleBack: () -> Unit = {
            if (showSummary) {
                showSummary = false
                isSessionStarted = false
                userAnswers.clear()
                if (activePracticeQuestion != null) {
                    viewModel.clearActivePracticeQuestion()
                }
                if (!viewModel.goBack()) {
                    viewModel.navigateTo(Screen.HOME)
                }
            } else if (isSessionStarted) {
                showEndPracticeConfirmDialog = true
            } else {
                // Not in session (e.g. subject selection view)
                if (activePracticeQuestion != null) {
                    viewModel.clearActivePracticeQuestion()
                }
                if (!viewModel.goBack()) {
                    viewModel.navigateTo(Screen.HOME)
                }
            }
        }

        com.example.ui.components.JuktiTopAppBar(
            title = practiceTitle,
            subtitle = practiceSubtitle,
            onBackClick = handleBack,
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

        BackHandler(enabled = !showEndPracticeConfirmDialog && !showReportDialog) {
            handleBack()
        }

        // Compact Question Language Selector & Active Session Header
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 1.dp
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                // Language Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
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
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Language:",
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

                // Active Session Status Bar (Question count, Timer, Correct/Incorrect, Progress Bar)
                if (isSessionStarted && displayQuestions.isNotEmpty() && !showSummary) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Question ${currentQuestionIndex + 1} / ${displayQuestions.size}",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        // Timer
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = "Timer",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                            val minutes = sessionTotalSeconds / 60
                            val secs = sessionTotalSeconds % 60
                            Text(
                                text = String.format("%02d:%02d", minutes, secs),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Session Stats Badge (✓ Correct   ✕ Incorrect)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "✓ $correctCount",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.success
                            )
                            Text(
                                text = "✕ $incorrectCount",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LinearProgressIndicator(
                        progress = { (currentQuestionIndex + 1).toFloat() / displayQuestions.size.coerceAtLeast(1) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )
                }
            }
        }

        // BODY CONTENT
        if (!isSessionStarted) {
            // STEP 1: SUBJECT BANNERS SELECTION
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

                if (dynamicBanners.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                } else {
                    dynamicBanners.forEach { banner ->
                        val preData = precomputedBannerData[banner.subjectKey]
                        val availableChapters = preData?.first ?: emptyList()
                        val bannerQs = preData?.second ?: emptyList()
                        val chapterCounts = preData?.third ?: emptyMap()

                        val currentSelectedChapters = chaptersMap[banner.subjectKey] ?: emptySet()

                        val questionCount = if (currentSelectedChapters.isEmpty()) {
                            bannerQs.size
                        } else {
                            currentSelectedChapters.sumOf { ch -> chapterCounts[ch] ?: 0 }
                        }

                        PracticeSubjectBannerCard(
                            banner = banner,
                            availableChapters = availableChapters,
                            selectedChapters = currentSelectedChapters,
                            onChaptersChanged = { newSet: Set<String> ->
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
                            isAssamese = isAssamese,
                            chapterCounts = chapterCounts
                        )
                    }
                }
            }
        } else if (showSummary) {
            PracticeSummaryView(
                questions = displayQuestions,
                userAnswers = userAnswers,
                totalTimeSeconds = sessionTotalSeconds,
                onFinish = {
                    showSummary = false
                    isSessionStarted = false
                    userAnswers.clear()
                    viewModel.navigateTo(Screen.HOME)
                },
                onPracticeAgain = {
                    showSummary = false
                    userAnswers.clear()
                    sessionTotalSeconds = 0
                    val deckResult = viewModel.resetAndReshuffleSessionDeck(sectionName, scopeKeyName, practiceQuestions)
                    activeSessionQuestions = deckResult.orderedQuestions
                    currentQuestionIndex = 0
                    isSessionStarted = true
                },
                questionLanguage = questionLanguage,
                isSmartPractice = isSmartPractice,
                isSavedPractice = isSavedPractice
            )
        } else {
            // ACTIVE QUESTION SCREEN
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
                    // QUESTION CARD
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Top Row inside Question Card: Chapter/Topic Tag + Bookmark + Overflow Menu
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Topic / Chapter Chip and Question Tag side by side
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f, fill = false)
                                ) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = currentQuestion.topic.ifBlank { currentQuestion.subject },
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    
                                    com.example.ui.components.QuestionTypeBadge(
                                        questionType = currentQuestion.questionType
                                    )
                                }

                                // Bookmark & Report Buttons
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    // Bookmark Button
                                    IconButton(
                                        onClick = { viewModel.toggleBookmarkQuestion(currentQuestion) },
                                        modifier = Modifier.size(38.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (currentQuestion.id in bookmarkedIds) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                            contentDescription = "Bookmark Question",
                                            tint = if (currentQuestion.id in bookmarkedIds) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    // Report Question Button (Flag)
                                    IconButton(
                                        onClick = { showReportDialog = true },
                                        modifier = Modifier.size(38.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Flag,
                                            contentDescription = "Report question",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Question Content
                            if (currentQuestion.isPremium && !isUserPremium && !isAdminOrOwner) {
                                Surface(
                                    color = MaterialTheme.colorScheme.errorContainer,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(Icons.Default.Lock, contentDescription = "Premium Content", modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.error)
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text("Premium Question", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("This question is only available to Premium users.", textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onErrorContainer)
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Button(onClick = { viewModel.showPaywall() }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                                            Text("Unlock Premium")
                                        }
                                    }
                                }
                            } else {
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
                                        else -> MaterialTheme.colorScheme.surfaceVariant
                                    }

                                    val borderColor = when {
                                        isSubmitted && isCorrect -> MaterialTheme.colorScheme.success
                                        isSubmitted && isSelected && !isCorrect -> MaterialTheme.colorScheme.error
                                        isSelected -> MaterialTheme.colorScheme.primary
                                        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                                    }

                                    Surface(
                                        onClick = {
                                            if (!isSubmitted) {
                                                val isAnsCorrect = (index == currentQuestion.correctOptionIndex)
                                                userAnswers[currentQuestion.id] = index
                                                if (isAnsCorrect) {
                                                    viewModel.awardCorrectAnswerXp()
                                                }
                                            }
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        color = backgroundColor,
                                        border = BorderStroke(
                                            width = if (isSelected || (isSubmitted && isCorrect)) 2.dp else 1.dp,
                                            color = borderColor
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 5.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(14.dp)
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
                                            Spacer(modifier = Modifier.width(12.dp))

                                            BilingualText(
                                                textEn = pair.first,
                                                textAs = pair.second,
                                                language = questionLanguage,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = if (isSubmitted && isCorrect) FontWeight.Bold else FontWeight.Normal,
                                                color = when {
                                                    isSubmitted && isCorrect -> MaterialTheme.colorScheme.onSuccessContainer
                                                    isSubmitted && isSelected && !isCorrect -> MaterialTheme.colorScheme.onErrorContainer
                                                    else -> MaterialTheme.colorScheme.onSurface
                                                }
                                            )
                                        }
                                    }
                                }

                                // After Answering Feedback Banner & Explanation Area
                                AnimatedVisibility(visible = isSubmitted) {
                                    Column {
                                        Spacer(modifier = Modifier.height(14.dp))

                                        val userAnsIndex = selectedOptionIndex ?: -1
                                        val isUserCorrect = (userAnsIndex == currentQuestion.correctOptionIndex)

                                        // Feedback Card
                                        Surface(
                                            color = if (isUserCorrect) MaterialTheme.colorScheme.successContainer.copy(alpha = 0.4f)
                                                    else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
                                            shape = RoundedCornerShape(12.dp),
                                            border = BorderStroke(
                                                1.dp,
                                                if (isUserCorrect) MaterialTheme.colorScheme.success else MaterialTheme.colorScheme.error
                                            ),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(14.dp)) {
                                                if (isUserCorrect) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(
                                                            Icons.Default.CheckCircle,
                                                            contentDescription = "Correct",
                                                            tint = MaterialTheme.colorScheme.success,
                                                            modifier = Modifier.size(20.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Text(
                                                            text = "✓ Correct Answer!",
                                                            style = MaterialTheme.typography.titleSmall,
                                                            fontWeight = FontWeight.Bold,
                                                            color = MaterialTheme.colorScheme.success
                                                        )
                                                    }
                                                } else {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(
                                                            Icons.Default.Cancel,
                                                            contentDescription = "Incorrect",
                                                            tint = MaterialTheme.colorScheme.error,
                                                            modifier = Modifier.size(20.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Text(
                                                            text = "✕ Incorrect",
                                                            style = MaterialTheme.typography.titleSmall,
                                                            fontWeight = FontWeight.Bold,
                                                            color = MaterialTheme.colorScheme.error
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.height(6.dp))

                                                    val userSelectedPair = options.getOrNull(userAnsIndex)
                                                    if (userSelectedPair != null) {
                                                        Row {
                                                            Text("Your answer: ", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                                                            BilingualText(
                                                                textEn = userSelectedPair.first,
                                                                textAs = userSelectedPair.second,
                                                                language = questionLanguage,
                                                                style = MaterialTheme.typography.bodySmall,
                                                                fontWeight = FontWeight.Bold,
                                                                color = MaterialTheme.colorScheme.error
                                                            )
                                                        }
                                                    }

                                                    val correctPair = options.getOrNull(currentQuestion.correctOptionIndex)
                                                    if (correctPair != null) {
                                                        Spacer(modifier = Modifier.height(2.dp))
                                                        Row {
                                                            Text("✓ Correct answer: ", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.success)
                                                            BilingualText(
                                                                textEn = correctPair.first,
                                                                textAs = correctPair.second,
                                                                language = questionLanguage,
                                                                style = MaterialTheme.typography.bodySmall,
                                                                fontWeight = FontWeight.Bold,
                                                                color = MaterialTheme.colorScheme.success
                                                            )
                                                        }
                                                    }
                                                }

                                                Spacer(modifier = Modifier.height(10.dp))
                                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                                Spacer(modifier = Modifier.height(10.dp))

                                                // Explanation
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        Icons.Default.Lightbulb,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = "Explanation:",
                                                        style = MaterialTheme.typography.titleSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                BilingualText(
                                                    textEn = currentQuestion.explanationEn,
                                                    textAs = currentQuestion.explanationAs,
                                                    language = questionLanguage,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Bottom Navigation Controls: Previous & Next with End Practice below
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
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
                                            enabled = currentQuestionIndex > 0,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Default.ChevronLeft, contentDescription = null)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Previous")
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Button(
                                            onClick = {
                                                if (currentQuestionIndex < displayQuestions.size - 1) {
                                                    currentQuestionIndex++
                                                } else {
                                                    viewModel.awardChapterCompletionXp()
                                                    showSummary = true
                                                }
                                            },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(
                                                if (currentQuestionIndex == displayQuestions.size - 1) {
                                                    "Finish Practice"
                                                } else {
                                                    "Next"
                                                }
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(Icons.Default.ChevronRight, contentDescription = null)
                                        }
                                    }

                                    // End Practice Button directly below Previous / Next
                                    TextButton(
                                        onClick = { showEndPracticeConfirmDialog = true },
                                        modifier = Modifier.padding(top = 2.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ExitToApp,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "End Practice",
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // End Practice Confirmation Dialog
        if (showEndPracticeConfirmDialog) {
            val remainingCount = (displayQuestions.size - userAnswers.size).coerceAtLeast(0)
            AlertDialog(
                onDismissRequest = { showEndPracticeConfirmDialog = false },
                title = { Text("End Practice?", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Your progress will be saved.")
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text("✓ Correct: $correctCount", color = MaterialTheme.colorScheme.success, fontWeight = FontWeight.Bold)
                            Text("✕ Incorrect: $incorrectCount", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                        }
                        Text("○ Remaining: $remainingCount", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { showEndPracticeConfirmDialog = false }
                    ) {
                        Text("Continue")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showEndPracticeConfirmDialog = false
                            showSummary = true
                        }
                    ) {
                        Text("End", color = MaterialTheme.colorScheme.error)
                    }
                }
            )
        }

        // Report Question Dialog
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
