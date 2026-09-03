package com.example.ui.screens

import com.example.ui.components.SafeOutlinedTextField

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
fun PracticeScreen(viewModel: JuktiViewModel, isSmartPractice: Boolean = false) {
    val language = com.example.ui.viewmodel.AppLanguage.ENGLISH
    val questionLanguage by viewModel.questionLanguage.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val isUserPremium by viewModel.isUserPremium.collectAsState()
    val isAdminOrOwner by viewModel.isAdminOrOwner.collectAsState()
    val allQuestions by viewModel.accessibleQuestions.collectAsState()
    val smartPracticeQuestions by viewModel.smartPracticeQuestions.collectAsState()
    val bookmarkedIds by viewModel.bookmarkedIds.collectAsState()
    val allSubjectsChapters by viewModel.allSubjectsChapters.collectAsState()
    val activePracticeQuestion by viewModel.activePracticeQuestion.collectAsState()

    val visibleQuestions by produceState(
        initialValue = emptyList<QuestionEntity>(),
        allQuestions, isSmartPractice, smartPracticeQuestions
    ) {
        value = withContext(Dispatchers.Default) {
            val baseList = if (isSmartPractice) smartPracticeQuestions else allQuestions
            baseList.take(if (isSmartPractice) 10 else Int.MAX_VALUE)
        }
    }

    var isSessionStarted by rememberSaveable { mutableStateOf(false) }
    var selectedSubjectKey by rememberSaveable { mutableStateOf("All Subjects") }
    var selectedChapters by remember { mutableStateOf<Set<String>>(emptySet()) }
    var showReportDialog by remember { mutableStateOf(false) }

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
        visibleQuestions, selectedSubjectKey, selectedChapters
    ) {
        value = withContext(Dispatchers.Default) {
            visibleQuestions.filter { q ->
                try {
                    val matchSubject = when (selectedSubjectKey) {
                        "All Subjects", "All Subject" -> true
                        "General Knowledge" -> q.subject in listOf("General Knowledge", "Assam History", "Assam Geography", "Assamese Literature & Culture", "Current Affairs")
                        "General English" -> q.subject.equals("General English", ignoreCase = true) || q.subject.equals("English", ignoreCase = true) || q.subject.contains("English", ignoreCase = true)
                        "Mathematics", "General Mathematics" -> q.subject in listOf("General Mathematics", "Mathematics", "Quantitative Aptitude")
                        "Reasoning", "Reasoning & Mental Ability" -> q.subject in listOf("Reasoning", "Logical Reasoning", "Logical Reasoning & Mental Ability", "Mental Ability", "Logical Aptitude", "Reasoning & Mental Ability")
                        "Basic Computer", "Computer Knowledge", "Computer" -> q.subject in listOf("Basic Computer", "Computer Knowledge", "Computer", "Computer Awareness", "Computer Science", "Information Technology", "IT") || q.subject.contains("Computer", ignoreCase = true) || q.topic.contains("Computer", ignoreCase = true) || q.topic.contains("MS Office", ignoreCase = true) || q.topic.contains("Operating System", ignoreCase = true) || q.topic.contains("Internet", ignoreCase = true) || q.topic.contains("Hardware", ignoreCase = true)
                        "Transport & Motor Vehicle", "Transport Rule", "Transport Rules" -> q.subject.equals("Transport & Motor Vehicle", ignoreCase = true) || q.subject.equals("Transport Rule", ignoreCase = true) || q.subject.equals("Transport Rules", ignoreCase = true) || q.subject.equals("Manual Entry", ignoreCase = true) || q.subject.contains("Manual", ignoreCase = true) || q.subject.contains("Transport", ignoreCase = true) || q.subject.contains("Motor Vehicle", ignoreCase = true) || q.topic.contains("Transport Rule", ignoreCase = true) || q.topic.contains("Traffic Sign", ignoreCase = true) || q.topic.contains("Motor Vehicle", ignoreCase = true) || q.topic.contains("Driving Regulation", ignoreCase = true) || q.topic.contains("Vehicle Safety", ignoreCase = true)
                        else -> q.subject.equals(selectedSubjectKey, ignoreCase = true)
                    }
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
                                when (selSubj) {
                                    "General Knowledge" -> qSubject in listOf("General Knowledge", "Assam History", "Assam Geography", "Assamese Literature & Culture", "Current Affairs")
                                    "General English" -> qSubject.equals("General English", ignoreCase = true) || qSubject.equals("English", ignoreCase = true) || qSubject.contains("English", ignoreCase = true)
                                    "General Mathematics", "Mathematics" -> qSubject in listOf("General Mathematics", "Mathematics", "Quantitative Aptitude")
                                    "Reasoning", "Reasoning & Mental Ability" -> qSubject in listOf("Reasoning", "Logical Reasoning", "Logical Reasoning & Mental Ability", "Mental Ability", "Logical Aptitude", "Reasoning & Mental Ability")
                                    "Transport & Motor Vehicle" -> qSubject.equals("Transport & Motor Vehicle", ignoreCase = true) || qSubject.contains("Transport", ignoreCase = true) || qSubject.contains("Motor Vehicle", ignoreCase = true)
                                    else -> qSubject.equals(selSubj, ignoreCase = true) || qSubject.contains(selSubj, ignoreCase = true) || selSubj.contains(qSubject, ignoreCase = true)
                                }
                            } else {
                                true
                            }

                            if (!subjectMatches) return@any false

                            val nCh = com.example.data.repository.normalizeChapterName(ch, qSubject)
                            normTopic.equals(nCh, ignoreCase = true) ||
                            topicStr.equals(ch, ignoreCase = true) ||
                            topicStr.contains(ch, ignoreCase = true) ||
                            ch.contains(topicStr, ignoreCase = true) ||
                            normTopic.contains(ch, ignoreCase = true) ||
                            ch.contains(normTopic, ignoreCase = true)
                        }
                    }
                    
                    matchSubject && matchChapter
                } catch (e: Exception) {
                    false
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
                    norm != "transport & motor vehicle"
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
                visibleQuestions.filter { q ->
                    when (banner.subjectKey) {
                        "All Subjects" -> true
                        "General Knowledge" -> q.subject in listOf("General Knowledge", "Assam History", "Assam Geography", "Assamese Literature & Culture", "Current Affairs")
                        "General English" -> q.subject.equals("General English", ignoreCase = true) || q.subject.equals("English", ignoreCase = true) || q.subject.contains("English", ignoreCase = true)
                        "General Mathematics", "Mathematics" -> q.subject in listOf("General Mathematics", "Mathematics", "Quantitative Aptitude")
                        "Reasoning", "Reasoning & Mental Ability" -> q.subject in listOf("Reasoning", "Logical Reasoning", "Logical Reasoning & Mental Ability", "Mental Ability", "Logical Aptitude", "Reasoning & Mental Ability")
                        "Basic Computer", "Computer Knowledge", "Computer" -> q.subject in listOf("Basic Computer", "Computer Knowledge", "Computer", "Computer Awareness", "Computer Science", "Information Technology", "IT") || q.subject.contains("Computer", ignoreCase = true) || q.topic.contains("Computer", ignoreCase = true) || q.topic.contains("MS Office", ignoreCase = true) || q.topic.contains("Operating System", ignoreCase = true) || q.topic.contains("Internet", ignoreCase = true) || q.topic.contains("Hardware", ignoreCase = true)
                        "Transport & Motor Vehicle", "Transport Rule", "Transport Rules" -> q.subject.equals("Transport & Motor Vehicle", ignoreCase = true) || q.subject.equals("Transport Rule", ignoreCase = true) || q.subject.equals("Transport Rules", ignoreCase = true) || q.subject.equals("Manual Entry", ignoreCase = true) || q.subject.contains("Manual", ignoreCase = true) || q.subject.contains("Transport", ignoreCase = true) || q.subject.contains("Motor Vehicle", ignoreCase = true) || q.topic.contains("Transport Rule", ignoreCase = true) || q.topic.contains("Traffic Sign", ignoreCase = true) || q.topic.contains("Motor Vehicle", ignoreCase = true) || q.topic.contains("Driving Regulation", ignoreCase = true) || q.topic.contains("Vehicle Safety", ignoreCase = true)
                        else -> q.subject.equals(banner.subjectKey, ignoreCase = true)
                    }
                }
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
                    "General Knowledge" -> {
                        bannerQs.forEach { q ->
                            val norm = qIdToNormalizedTopic[q.id]
                            if (norm != null && norm.isNotBlank()) set.add(norm)
                        }
                        allSubjectsChapters.filter { it.subject in listOf("General Knowledge", "Assam History", "Assam Geography", "Assamese Literature & Culture", "Current Affairs") }
                            .forEach { if (it.chapter.isNotBlank()) set.add(it.chapter) }
                    }
                    "General English" -> {
                        bannerQs.forEach { q ->
                            val norm = qIdToNormalizedTopic[q.id]
                            if (norm != null && norm.isNotBlank()) set.add(norm)
                        }
                        allSubjectsChapters.filter { (it.subject.equals("General English", ignoreCase = true) || it.subject.equals("English", ignoreCase = true) || it.subject.contains("English", ignoreCase = true)) }
                            .forEach { if (it.chapter.isNotBlank()) set.add(it.chapter) }
                    }
                    "General Mathematics" -> {
                        bannerQs.forEach { q ->
                            val norm = qIdToNormalizedTopic[q.id]
                            if (norm != null && norm.isNotBlank()) set.add(norm)
                        }
                        allSubjectsChapters.filter { it.subject in listOf("General Mathematics", "Mathematics") }
                            .forEach { if (it.chapter.isNotBlank()) set.add(it.chapter) }
                    }
                    "Reasoning", "Reasoning & Mental Ability" -> {
                        bannerQs.forEach { q ->
                            val norm = qIdToNormalizedTopic[q.id]
                            if (norm != null && norm.isNotBlank()) set.add(norm)
                        }
                        allSubjectsChapters.filter { it.subject in listOf("Reasoning", "Logical Reasoning & Mental Ability", "Reasoning & Mental Ability") }
                            .forEach { if (it.chapter.isNotBlank()) set.add(it.chapter) }
                    }
                    "Basic Computer", "Computer Knowledge", "Computer" -> {
                        bannerQs.forEach { q ->
                            val norm = qIdToNormalizedTopic[q.id]
                            if (norm != null && norm.isNotBlank()) set.add(norm)
                        }
                        allSubjectsChapters.filter { it.subject in listOf("Basic Computer", "Computer Knowledge", "Computer", "Computer Awareness") || it.subject.contains("Computer", ignoreCase = true) }
                            .forEach { if (it.chapter.isNotBlank()) set.add(it.chapter) }
                    }
                    "Transport & Motor Vehicle" -> {
                        bannerQs.forEach { q ->
                            val norm = qIdToNormalizedTopic[q.id]
                            if (norm != null && norm.isNotBlank()) set.add(norm)
                        }
                        allSubjectsChapters.filter { it.subject.equals("Transport & Motor Vehicle", ignoreCase = true) || it.subject.equals("Transport Rule", ignoreCase = true) || it.subject.equals("Transport Rules", ignoreCase = true) || it.subject.equals("Manual Entry", ignoreCase = true) || it.subject.contains("Manual", ignoreCase = true) || it.subject.contains("Transport", ignoreCase = true) }
                            .forEach { if (it.chapter.isNotBlank()) set.add(it.chapter) }
                    }
                    else -> {
                        bannerQs.forEach { q ->
                            val norm = qIdToNormalizedTopic[q.id]
                            if (norm != null && norm.isNotBlank()) set.add(norm)
                        }
                        allSubjectsChapters.filter { it.subject.equals(banner.subjectKey, ignoreCase = true) }
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
                            topicStr.equals(ch, ignoreCase = true) ||
                            topicStr.contains(ch, ignoreCase = true) ||
                            ch.contains(topicStr, ignoreCase = true) ||
                            normTopic.contains(ch, ignoreCase = true) ||
                            ch.contains(normTopic, ignoreCase = true)
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



    var scoreCount by remember { mutableIntStateOf(0) }
    var currentQuestionIndex by rememberSaveable { mutableIntStateOf(0) }
    val totalPracticedCount = userProfile?.totalSolved ?: 0

    var showSummary by rememberSaveable { mutableStateOf(false) }
    val userAnswers = remember { mutableStateMapOf<Long, Int>() }

    var sessionTotalSeconds by rememberSaveable { mutableIntStateOf(0) }
    var activeSessionQuestions by remember { mutableStateOf<List<QuestionEntity>>(emptyList()) }
    var lastStartingQuestionId by rememberSaveable { mutableLongStateOf(-1L) }

    LaunchedEffect(isSessionStarted, selectedSubjectKey, selectedChapters, isSmartPractice, activePracticeQuestion) {
        if (isSessionStarted) {
            userAnswers.clear()
            scoreCount = 0
            sessionTotalSeconds = 0
            val eligible = mutableListOf<QuestionEntity>()
            if (activePracticeQuestion != null) {
                eligible.add(activePracticeQuestion!!)
                val otherQs = practiceQuestions.filter { it.id != activePracticeQuestion!!.id }
                eligible.addAll(otherQs)
            } else {
                eligible.addAll(practiceQuestions.shuffled())
            }
            if (eligible.size > 1 && activePracticeQuestion == null && eligible[0].id == lastStartingQuestionId) {
                val temp = eligible[0]
                eligible[0] = eligible[1]
                eligible[1] = temp
            }
            if (eligible.isNotEmpty() && activePracticeQuestion == null) {
                lastStartingQuestionId = eligible[0].id
            }
            activeSessionQuestions = eligible
            currentQuestionIndex = 0
        } else {
            activeSessionQuestions = emptyList()
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
                    "Reasoning", "Reasoning & Mental Ability" -> "Reasoning"
                    "Basic Computer", "Computer Knowledge", "Computer" -> "Basic Computer"
                    "Transport Rule" -> "Transport Rule"
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
                if (activePracticeQuestion != null) {
                    viewModel.clearActivePracticeQuestion()
                }
                if (isSessionStarted && !isSmartPractice) {
                    isSessionStarted = false
                } else {
                    if (!viewModel.goBack()) {
                        viewModel.navigateTo(Screen.HOME)
                    }
                }
            },
            actions = {
                // Practiced Question Counter Badge
                Surface(
                    color = androidx.compose.material3.MaterialTheme.colorScheme.secondaryContainer,
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
                            tint = androidx.compose.material3.MaterialTheme.colorScheme.secondary,
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
                            tint = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Question Language:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = androidx.compose.material3.MaterialTheme.colorScheme.primary
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
                                selectedContainerColor = androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }
        }

        // BODY CONTENT
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
                    color = androidx.compose.material3.MaterialTheme.colorScheme.primary
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
                    scoreCount = 0
                    viewModel.navigateTo(Screen.HOME)
                },
                onPracticeAgain = {
                    showSummary = false
                    userAnswers.clear()
                    scoreCount = 0
                    currentQuestionIndex = 0
                    sessionTotalSeconds = 0
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
                    isSessionStarted = true
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
                        colors = CardDefaults.cardColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant)
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
                                    color = androidx.compose.material3.MaterialTheme.colorScheme.secondaryContainer,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "Q ${currentQuestionIndex + 1} of ${displayQuestions.size}",
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
    

                                // Per-question Timer
                                }
                                Surface(
                                    color = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant,
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
                                            tint = androidx.compose.material3.MaterialTheme.colorScheme.primary,
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
        
    

                                // Action Icons: Save & Hide
                                    }
                                }
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
                                            imageVector = if (currentQuestion.id in bookmarkedIds) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                            contentDescription = "Save Question",
                                            tint = if (currentQuestion.id in bookmarkedIds) androidx.compose.material3.MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    // Hide Question Button

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
                                color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
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
                                    color = androidx.compose.material3.MaterialTheme.colorScheme.primary
                                )

                            }
                            Spacer(modifier = Modifier.height(10.dp))

                            // Question Text
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
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                }

                                val borderColor = when {
                                    isSubmitted && isCorrect -> MaterialTheme.colorScheme.success
                                    isSubmitted && isSelected && !isCorrect -> MaterialTheme.colorScheme.error
                                    isSelected -> MaterialTheme.colorScheme.primary
                                    else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
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
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = borderColor
                                    ),
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(16.dp)
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
                                                else -> MaterialTheme.colorScheme.onSurface
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
                                        color = androidx.compose.material3.MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Lightbulb, contentDescription = null, tint = androidx.compose.material3.MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
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
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Previous")
                                }

                                Button(
                                    onClick = {
                                        if (currentQuestionIndex < displayQuestions.size - 1) {
                                            currentQuestionIndex++
                                        }
                                        else {
                                            viewModel.awardChapterCompletionXp()
                                            showSummary = true
                                        }
                                    }
                                ) {
                                    Text(
                                        if (currentQuestionIndex == displayQuestions.size - 1) {
                                            "Completed"
                                        }
                                        else {
                                            "Next"
                                        }
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
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
}
