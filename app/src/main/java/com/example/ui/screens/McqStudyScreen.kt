package com.example.ui.screens

import com.example.ui.components.SafeOutlinedTextField

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.QuestionEntity
import com.example.data.local.StudyNoteEntity
import com.example.ui.components.BilingualText
import com.example.ui.components.ReportQuestionDialog
import com.example.ui.viewmodel.AppLanguage
import com.example.ui.viewmodel.JuktiViewModel
import com.example.ui.viewmodel.Screen
import kotlinx.coroutines.delay
import java.util.Locale

data class StudyBannerConfig(
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
fun StudySubjectBannerCard(
    banner: StudyBannerConfig,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun McqStudyScreen(viewModel: JuktiViewModel) {
    val language by viewModel.language.collectAsState()
    val questionLanguage by viewModel.questionLanguage.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val questions by viewModel.questions.collectAsState()
    val mockTests by viewModel.mockTests.collectAsState()
    val studyNotes by viewModel.studyNotes.collectAsState()
    val bookmarkedQuestions by viewModel.bookmarkedQuestions.collectAsState()
    val isAssamese = language == AppLanguage.ASSAMESE
    val context = androidx.compose.ui.platform.LocalContext.current

    val activeStudySubView by viewModel.studySubView.collectAsState()
    val openedStudyDirectly by viewModel.openedStudyDirectly.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Bar Navigation Header
        val titleText = when (activeStudySubView) {
            "STUDY_MCQS" -> "Learn"
            "POMODORO" -> "Pomodoro Study Timer"
            "CURRENT_AFFAIRS" -> "Current Affairs"
            else -> "Study Hub"
        }
        val subtitleText = when (activeStudySubView) {
            "STUDY_MCQS" -> "Learn chapter-wise MCQs"
            "POMODORO" -> "Stay focused with timed study sessions"
            "CURRENT_AFFAIRS" -> "Daily updated news capsules & study notes"
            else -> "Choose a module to start learning"
        }

        com.example.ui.components.JuktiTopAppBar(
            title = titleText,
            subtitle = subtitleText,
            onBackClick = {
                if (activeStudySubView != null) {
                    if (openedStudyDirectly) {
                        viewModel.setStudySubView(null)
                        viewModel.navigateTo(Screen.HOME)
                    } else {
                        viewModel.setStudySubView(null)
                    }
                } else {
                    viewModel.navigateTo(Screen.HOME)
                }
            },
            actions = {
                if (activeStudySubView == "CURRENT_AFFAIRS") {
                    TextButton(onClick = { 
                        viewModel.setLanguage(if (language == AppLanguage.ENGLISH) AppLanguage.ASSAMESE else AppLanguage.ENGLISH) 
                    }) {
                        Icon(Icons.Default.Translate, contentDescription = "Change Language", modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(if (language == AppLanguage.ENGLISH) "EN" else "অসমীয়া", fontWeight = FontWeight.Bold)
                    }
                }

                if (activeStudySubView != "CURRENT_AFFAIRS") {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.MenuBook,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Lvl ${userProfile?.level ?: 1}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
        )

        if (activeStudySubView == "STUDY_MCQS") {
            Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 1.dp) {
                // Question Language Switcher Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
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

        // Body Content based on Active Sub-view
        when (activeStudySubView) {
            "STUDY_MCQS" -> {
                StudyMcqInteractiveTab(viewModel = viewModel)
            }
            "POMODORO" -> {
                PomodoroClockTab(viewModel = viewModel)
            }
            "CURRENT_AFFAIRS" -> {
                CurrentAffairsNotesTab(viewModel = viewModel)
            }
            else -> {
                // Main Study Feature Cards - Vertically Scrollable List
                
                var searchQuery by remember { mutableStateOf("") }
                var showSavedQuestionsDialog by remember { mutableStateOf(false) }
                
                val isPremium by viewModel.isUserPremium.collectAsState()
                val availableQuestions = if (isPremium) questions.size else questions.count { !it.isPremium }
                val solvedQuestions = userProfile?.totalSolved ?: 0
                val availableNotes = if (isPremium) studyNotes.size else studyNotes.count { !it.isPremium }
                
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    SafeOutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search subjects, chapters, notes...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )

                    // 1. Learn
                    StudyFeatureCard(
                        title = "Learn",
                        description = "Master chapter-wise questions.",
                        actionText = "Start Learning",
                        icon = Icons.Default.AutoStories,
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        iconTintColor = MaterialTheme.colorScheme.primary,
                        onClick = { viewModel.setStudySubView("STUDY_MCQS", fromHome = false) },
                        progressText = "$solvedQuestions/$availableQuestions completed",
                        badgeText = "🔥 Popular"
                    )

                    // 2. Practice
                    StudyFeatureCard(
                        title = "Practice",
                        description = "Test yourself with instant explanations.",
                        actionText = "Practice Now",
                        icon = Icons.Default.Quiz,
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        iconTintColor = MaterialTheme.colorScheme.secondary,
                        onClick = { viewModel.navigateTo(Screen.PRACTICE) },
                        progressText = "$solvedQuestions/$availableQuestions completed",
                        badgeText = "⭐ Recommended"
                    )
                    
                    // 3. Smart Practice
                    StudyFeatureCard(
                        title = "Smart Practice",
                        description = "Practice saved and frequently incorrect questions.",
                        actionText = "Start Smart Practice",
                        icon = Icons.Default.Psychology,
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        iconTintColor = MaterialTheme.colorScheme.tertiary,
                        onClick = { viewModel.navigateTo(Screen.SMART_PRACTICE) },
                        badgeText = "🧠 Smart"
                    )

                    // 4. Saved Questions
                    StudyFeatureCard(
                        title = "Saved Questions",
                        description = "Review and manage your bookmarked MCQs.",
                        actionText = "Review Saved",
                        icon = Icons.Default.Bookmark,
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        iconTintColor = MaterialTheme.colorScheme.primary,
                        onClick = { showSavedQuestionsDialog = true },
                        progressText = "${bookmarkedQuestions.size} questions saved",
                        badgeText = "📌 Saved"
                    )

                    // 5. Study Notes
                    StudyFeatureCard(
                        title = "Study Notes",
                        description = "Read chapter-wise notes.",
                        actionText = "Read Notes",
                        icon = Icons.Default.Description,
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        iconTintColor = MaterialTheme.colorScheme.primary,
                        onClick = { viewModel.navigateTo(Screen.STUDY_NOTES) },
                        progressText = "$availableNotes chapters available",
                        badgeText = "🆕 New"
                    )

                    // 6. Current Affairs
                    StudyFeatureCard(
                        title = "Current Affairs",
                        description = "Stay updated with daily current affairs notes & news capsules.",
                        actionText = "Read Notes",
                        icon = Icons.Default.Newspaper,
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        iconTintColor = MaterialTheme.colorScheme.tertiary,
                        onClick = { viewModel.setStudySubView("CURRENT_AFFAIRS", fromHome = false) },
                        badgeText = "🆕 New"
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                }

                if (showSavedQuestionsDialog) {
                    SavedQuestionsDialog(
                        questions = bookmarkedQuestions,
                        language = language,
                        onDismiss = { showSavedQuestionsDialog = false },
                        onToggleBookmark = { q -> viewModel.toggleBookmarkQuestion(q) }
                    )
                }
            }
        }
    }
}

/* =====================================================================
   FEATURE CARD COMPONENT WITH SMOOTH PRESS ANIMATION
   ===================================================================== */
@Composable
fun StudyFeatureCard(
    title: String,
    description: String,
    actionText: String,
    icon: ImageVector,
    containerColor: Color,
    iconTintColor: Color,
    onClick: () -> Unit,
    progressText: String? = null,
    badgeText: String? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "cardScale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 6.dp
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = CircleShape,
                        color = containerColor,
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = iconTintColor,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )
                        
                        if (progressText != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = progressText,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                color = iconTintColor
                            )
                        }
                    }
                }
                
                if (badgeText != null) {
                    Surface(
                        color = containerColor.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = badgeText,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = iconTintColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onClick,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(42.dp),
                contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = iconTintColor,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = actionText,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/* =====================================================================
   TAB 1: STUDY MCQ INTERACTIVE MODULE
   ===================================================================== */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyMcqInteractiveTab(viewModel: JuktiViewModel) {
    val language by viewModel.language.collectAsState()
    val questionLanguage by viewModel.questionLanguage.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val isUserPremium by viewModel.isUserPremium.collectAsState()
    val questions by viewModel.questions.collectAsState()
    val allSubjectsChapters by viewModel.allSubjectsChapters.collectAsState()
    val isAssamese = language == AppLanguage.ASSAMESE
    val context = androidx.compose.ui.platform.LocalContext.current

    var isStudySessionStarted by remember { mutableStateOf(false) }
    var selectedSubjectTab by remember { mutableStateOf("All Subject") }
    var selectedChapters by remember { mutableStateOf<Set<String>>(emptySet()) }
    var chaptersMap by remember { mutableStateOf<Map<String, Set<String>>>(emptyMap()) }

    // Session State
    var currentQuestionIndex by remember { mutableStateOf(0) }
    var selectedOptionIndex by remember { mutableStateOf<Int?>(null) }
    
    // Remember the initial solved count when the session starts
    var sessionInitialSolvedCount by remember { mutableStateOf(userProfile?.totalSolved ?: 0) }
    var studiedQuestionsCountInSession by remember(userProfile?.id) { mutableStateOf(userProfile?.totalSolved ?: 0) }
    
    val learnedQuestionIds = remember { mutableStateOf(setOf<Long>()) }
    val markQuestionLearned: (Long) -> Unit = { questionId ->
        if (!learnedQuestionIds.value.contains(questionId)) {
            learnedQuestionIds.value = learnedQuestionIds.value + questionId
            studiedQuestionsCountInSession++
            viewModel.recordStudyProgress(0, 10)
            if (!isUserPremium && studiedQuestionsCountInSession >= 25) {
                viewModel.showPaywall()
            }
        }
    }

    var sessionTotalSeconds by remember { mutableStateOf(0) }
    var showLimitModal by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }
    var showSummaryDialog by remember { mutableStateOf(false) }
    var finalSessionQuestions by remember { mutableStateOf(0) }
    var finalSessionTime by remember { mutableStateOf(0) }

    // Live Timer Coroutine per Session
    LaunchedEffect(isStudySessionStarted) {
        if (isStudySessionStarted) {
            learnedQuestionIds.value = emptySet()
            sessionInitialSolvedCount = userProfile?.totalSolved ?: 0
            sessionTotalSeconds = 0
            while (true) {
                delay(1000L)
                sessionTotalSeconds++
                if (sessionTotalSeconds % 30 == 0 && studiedQuestionsCountInSession > sessionInitialSolvedCount) {
                    viewModel.recordStudyProgress(0, 30)
                }
            }
        }
    }

    var activeStudySessionQuestions by remember { mutableStateOf<List<QuestionEntity>>(emptyList()) }
    var lastStudyStartingQuestionId by rememberSaveable { mutableLongStateOf(-1L) }

    LaunchedEffect(isStudySessionStarted, selectedSubjectTab, selectedChapters) {
        if (isStudySessionStarted) {
            val filtered = questions.filter { q ->
                try {
                    if (q.isHidden) return@filter false
                    val matchSubject = when (selectedSubjectTab) {
                        "All Subject", "All Subjects" -> true
                        "General Knowledge" -> q.subject in listOf("General Knowledge", "Assam History", "Assam Geography", "Assamese Literature & Culture", "Current Affairs")
                        "General English" -> q.subject == "General English"
                        "General Mathematics", "Mathematics" -> q.subject in listOf("General Mathematics", "Mathematics", "Quantitative Aptitude")
                        "Reasoning" -> q.subject in listOf("Reasoning", "Logical Reasoning", "Logical Reasoning & Mental Ability", "Mental Ability", "Logical Aptitude", "Reasoning & Mental Ability")
                        "Transport Rule", "Transport Rules" -> q.subject.equals("Transport Rule", ignoreCase = true) || q.subject.equals("Transport Rules", ignoreCase = true) || q.subject.equals("Manual Entry", ignoreCase = true) || q.subject.contains("Manual", ignoreCase = true) || q.topic.contains("Transport Rule", ignoreCase = true) || q.topic.contains("Traffic Sign", ignoreCase = true) || q.topic.contains("Motor Vehicle", ignoreCase = true) || q.topic.contains("Driving Regulation", ignoreCase = true) || q.topic.contains("Vehicle Safety", ignoreCase = true)
                        else -> q.subject.equals(selectedSubjectTab, ignoreCase = true)
                    }
                    val matchChapter = if (selectedChapters.isEmpty()) {
                        true
                    } else {
                        val topicStr = q.topic ?: ""
                        val normTopic = com.example.data.repository.normalizeChapterName(topicStr)
                        if (selectedSubjectTab == "Reasoning") {
                            selectedChapters.any { ch ->
                                topicStr.contains(ch, ignoreCase = true) || 
                                ch.contains(topicStr, ignoreCase = true) ||
                                q.subject.contains(ch, ignoreCase = true) ||
                                (ch.contains("Coding", ignoreCase = true) && (topicStr.isBlank() || topicStr.contains("Code", ignoreCase = true) || topicStr.contains("Series", ignoreCase = true) || topicStr.contains("Analogy", ignoreCase = true))) ||
                                (ch.contains("Blood", ignoreCase = true) && (topicStr.contains("Blood", ignoreCase = true) || topicStr.contains("Direction", ignoreCase = true) || topicStr.contains("Relation", ignoreCase = true))) ||
                                (ch.contains("Seating", ignoreCase = true) && (topicStr.contains("Seat", ignoreCase = true) || topicStr.contains("Puzzle", ignoreCase = true) || topicStr.contains("Venn", ignoreCase = true))) ||
                                (ch.contains("Syllogism", ignoreCase = true) && (topicStr.contains("Syllogism", ignoreCase = true) || topicStr.contains("Statement", ignoreCase = true) || topicStr.contains("Assumption", ignoreCase = true)))
                            }
                        } else if (selectedSubjectTab == "General English") {
                            selectedChapters.any { ch ->
                                if (ch == "One-Word & Idioms" || ch == "One-Word & Idiom") {
                                    normTopic == "One-Word & Idioms" || normTopic == "One-Word & Idiom" || topicStr.contains("Idiom", ignoreCase = true) || topicStr.contains("One-Word", ignoreCase = true) || topicStr.contains("One Word", ignoreCase = true) || topicStr.contains("Substitution", ignoreCase = true) || topicStr.contains("Phrase", ignoreCase = true)
                                } else {
                                    normTopic.equals(ch, ignoreCase = true) || topicStr.contains(ch, ignoreCase = true) || ch.contains(topicStr, ignoreCase = true)
                                }
                            }
                        } else {
                            selectedChapters.any { ch ->
                                normTopic.equals(ch, ignoreCase = true) || 
                                topicStr.equals(ch, ignoreCase = true) ||
                                topicStr.contains(ch, ignoreCase = true) ||
                                ch.contains(topicStr, ignoreCase = true) ||
                                q.subject.contains(ch, ignoreCase = true)
                            }
                        }
                    }
                    matchSubject && matchChapter
                } catch (e: Exception) {
                    false
                }
            }.shuffled().toMutableList()

            if (filtered.size > 1 && filtered[0].id == lastStudyStartingQuestionId) {
                val temp = filtered[0]
                filtered[0] = filtered[1]
                filtered[1] = temp
            }
            if (filtered.isNotEmpty()) {
                lastStudyStartingQuestionId = filtered[0].id
            }
            activeStudySessionQuestions = filtered
            currentQuestionIndex = 0
            selectedOptionIndex = null
        } else {
            activeStudySessionQuestions = emptyList()
        }
    }

    val studyQuestionsList = if (isStudySessionStarted && activeStudySessionQuestions.isNotEmpty()) activeStudySessionQuestions else questions.filter { !it.isHidden }

    var showHideNotice by remember { mutableStateOf(false) }

    val currentQuestion = studyQuestionsList.getOrNull(currentQuestionIndex)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        if (!isStudySessionStarted) {
            // STEP 1: 5 SUBJECT BANNERS FOR STUDY MCQS
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Choose Subject to Study:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                val studyBanners = listOf(
                    StudyBannerConfig(
                        titleEn = "All Subject",
                        titleAs = "সকলো বিষয় (Mixed)",
                        subtitleEn = "Study chapter-wise mixed MCQs across all subjects",
                        subtitleAs = "সকলো বিষয়ৰ মিশ্ৰিত অধ্যায়ভিত্তিক প্ৰশ্ন পৰীক্ষা কৰক",
                        subjectKey = "All Subject",
                        icon = Icons.Default.Apps,
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        iconColor = MaterialTheme.colorScheme.primary
                    ),
                    StudyBannerConfig(
                        titleEn = "General Knowledge",
                        titleAs = "সাধাৰণ জ্ঞান (GK)",
                        subtitleEn = "History, Geography, Polity, Assam GK, Science & Current Affairs",
                        subtitleAs = "ইতিহাস, ভূগোল, ৰাজনীতি, অসম বিৱৰণ আৰু বিজ্ঞান",
                        subjectKey = "General Knowledge",
                        icon = Icons.Default.Public,
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                        iconColor = MaterialTheme.colorScheme.secondary
                    ),
                    StudyBannerConfig(
                        titleEn = "General English",
                        titleAs = "সাধাৰণ ইংৰাজী",
                        subtitleEn = "Grammar, Vocabulary, Synonyms, Antonyms & Comprehension",
                        subtitleAs = "ইংৰাজী ব্যাকৰণ, শব্দকোষ, বিপৰীত শব্দ আৰু সমাৰ্থক শব্দ",
                        subjectKey = "General English",
                        icon = Icons.Default.Translate,
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f),
                        iconColor = MaterialTheme.colorScheme.tertiary
                    ),
                    StudyBannerConfig(
                        titleEn = "Mathematics",
                        titleAs = "গণিত (Mathematics)",
                        subtitleEn = "Arithmetic, Quantitative Aptitude, Algebra & Geometry",
                        subtitleAs = "পাটিগণিত, বীজগণিত, জ্যামিতি আৰু সংখ্যা সংক্ৰান্তীয়",
                        subjectKey = "General Mathematics",
                        icon = Icons.Default.Calculate,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        iconColor = MaterialTheme.colorScheme.primary
                    ),
                    StudyBannerConfig(
                        titleEn = "Reasoning",
                        titleAs = "যুক্তিবিদ্যা (Reasoning)",
                        subtitleEn = "Logical Aptitude, Verbal & Non-Verbal Reasoning",
                        subtitleAs = "মানসিক দক্ষতা আৰু যুক্তিনিৰ্ভৰ প্ৰশ্নৱালী",
                        subjectKey = "Reasoning",
                        icon = Icons.Default.Psychology,
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        iconColor = MaterialTheme.colorScheme.secondary
                    ),
                    StudyBannerConfig(
                        titleEn = "Transport Rule",
                        titleAs = "পৰিবহন নিয়ম (Transport Rule)",
                        subtitleEn = "Traffic Signs, Motor Vehicle Act, Road Safety & Driving Rules",
                        subtitleAs = "যান-বাহন নিয়ম, মটৰ বাহন আইন, পথ সুৰক্ষা আৰু সংকেত",
                        subjectKey = "Transport Rule",
                        icon = Icons.Default.DirectionsCar,
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                        iconColor = MaterialTheme.colorScheme.secondary
                    )
                )

                studyBanners.forEach { banner ->
                    val availableChapters = remember(questions, allSubjectsChapters, banner.subjectKey) {
                        val set = mutableSetOf<String>()
                        when (banner.subjectKey) {
                            "All Subject", "All Subjects" -> {
                                questions.forEach { if (it.topic.isNotBlank()) set.add(com.example.data.repository.normalizeChapterName(it.topic)) }
                                allSubjectsChapters.forEach { if (it.chapter.isNotBlank()) set.add(com.example.data.repository.normalizeChapterName(it.chapter)) }
                            }
                            "General Knowledge" -> {
                                questions.filter { it.subject in listOf("General Knowledge", "Assam History", "Assam Geography", "Assamese Literature & Culture", "Current Affairs") }
                                    .forEach { if (it.topic.isNotBlank()) set.add(com.example.data.repository.normalizeChapterName(it.topic)) }
                                allSubjectsChapters.filter { it.subject in listOf("General Knowledge", "Assam History", "Assam Geography & Economy", "Assam Geography", "Indian Polity & Constitution", "Indian History & National Movement", "General Science", "Current Affairs & General Awareness") }
                                    .forEach { if (it.chapter.isNotBlank()) set.add(com.example.data.repository.normalizeChapterName(it.chapter)) }
                            }
                            "General English" -> {
                                set.add("Grammar & Sentence Correction")
                                set.add("Synonyms, Antonyms & Vocabulary")
                                set.add("One-Word & Idioms")
                                set.add("Reading Comprehension & Para Jumbles")
                                questions.filter { it.subject == "General English" }
                                    .forEach { if (it.topic.isNotBlank()) set.add(com.example.data.repository.normalizeChapterName(it.topic)) }
                                allSubjectsChapters.filter { it.subject == "General English" }
                                    .forEach { if (it.chapter.isNotBlank()) set.add(com.example.data.repository.normalizeChapterName(it.chapter)) }
                            }
                            "General Mathematics" -> {
                                set.add("Number System, LCM & HCF")
                                set.add("Percentage, Ratio & Proportion")
                                set.add("Profit, Loss, Discount & Simple/Compound Interest")
                                set.add("Time, Work, Speed, Distance & Mensuration")
                                questions.filter { it.subject in listOf("General Mathematics", "Mathematics", "Quantitative Aptitude") }
                                    .forEach { if (it.topic.isNotBlank()) set.add(com.example.data.repository.normalizeChapterName(it.topic)) }
                                allSubjectsChapters.filter { it.subject in listOf("General Mathematics", "Mathematics") }
                                    .forEach { if (it.chapter.isNotBlank()) set.add(com.example.data.repository.normalizeChapterName(it.chapter)) }
                            }
                            "Reasoning" -> {
                                set.add("Coding-Decoding, Series & Analogy")
                                set.add("Blood Relations & Direction Sense Test")
                                set.add("Seating Arrangement, Puzzles & Venn Diagrams")
                                set.add("Syllogism, Statements & Assumptions")
                                questions.filter { it.subject in listOf("Reasoning", "Logical Reasoning", "Logical Reasoning & Mental Ability", "Mental Ability", "Logical Aptitude", "Reasoning & Mental Ability") }
                                    .forEach { if (it.topic.isNotBlank()) set.add(com.example.data.repository.normalizeChapterName(it.topic)) }
                                allSubjectsChapters.filter { it.subject in listOf("Reasoning", "Logical Reasoning & Mental Ability") }
                                    .forEach { if (it.chapter.isNotBlank()) set.add(com.example.data.repository.normalizeChapterName(it.chapter)) }
                            }
                            "Transport Rule" -> {
                                set.add("Traffic Signs, Signals & Road Safety")
                                set.add("Motor Vehicles Act & Traffic Rules")
                                set.add("Driving Regulations, Licences & Permits")
                                set.add("Vehicle Safety, Violations & Penalties")
                                questions.filter { it.subject.equals("Transport Rule", ignoreCase = true) || it.subject.equals("Transport Rules", ignoreCase = true) || it.subject.equals("Manual Entry", ignoreCase = true) || it.subject.contains("Manual", ignoreCase = true) || it.topic.contains("Transport Rule", ignoreCase = true) || it.topic.contains("Traffic Sign", ignoreCase = true) || it.topic.contains("Motor Vehicle", ignoreCase = true) }
                                    .forEach { if (it.topic.isNotBlank()) set.add(com.example.data.repository.normalizeChapterName(it.topic)) }
                                allSubjectsChapters.filter { it.subject.equals("Transport Rule", ignoreCase = true) || it.subject.equals("Transport Rules", ignoreCase = true) || it.subject.equals("Manual Entry", ignoreCase = true) || it.subject.contains("Manual", ignoreCase = true) }
                                    .forEach { if (it.chapter.isNotBlank()) set.add(com.example.data.repository.normalizeChapterName(it.chapter)) }
                            }
                            else -> {
                                questions.filter { it.subject.equals(banner.subjectKey, ignoreCase = true) }
                                    .forEach { if (it.topic.isNotBlank()) set.add(com.example.data.repository.normalizeChapterName(it.topic)) }
                                allSubjectsChapters.filter { it.subject.equals(banner.subjectKey, ignoreCase = true) }
                                    .forEach { if (it.chapter.isNotBlank()) set.add(com.example.data.repository.normalizeChapterName(it.chapter)) }
                            }
                        }
                        set.remove("One-Word & Idiom")
                        set.remove("One-Word & Idiom/Phrase")
                        set.remove("Idioms, Phrases & One-Word Substitution")
                        set.remove("Idioms & Phrases")
                        set.remove("One-Word Substitution")
                        set.remove("One Word Substitution")
                        set.toList().sorted()
                    }

                    val totalCount = remember(questions, banner.subjectKey) {
                        when (banner.subjectKey) {
                            "All Subject", "All Subjects" -> questions.count { !it.isHidden }
                            "General Knowledge" -> questions.count { !it.isHidden && it.subject in listOf("General Knowledge", "Assam History", "Assam Geography", "Assamese Literature & Culture", "Current Affairs") }
                            "General English" -> questions.count { !it.isHidden && it.subject == "General English" }
                            "General Mathematics" -> questions.count { !it.isHidden && it.subject in listOf("General Mathematics", "Mathematics", "Quantitative Aptitude") }
                            "Reasoning" -> questions.count { !it.isHidden && it.subject in listOf("Reasoning", "Logical Reasoning", "Logical Reasoning & Mental Ability", "Mental Ability", "Logical Aptitude", "Reasoning & Mental Ability") }
                            "Transport Rule" -> questions.count { !it.isHidden && (it.subject.equals("Transport Rule", ignoreCase = true) || it.subject.equals("Transport Rules", ignoreCase = true) || it.subject.equals("Manual Entry", ignoreCase = true) || it.subject.contains("Manual", ignoreCase = true) || it.topic.contains("Transport Rule", ignoreCase = true) || it.topic.contains("Traffic Sign", ignoreCase = true) || it.topic.contains("Motor Vehicle", ignoreCase = true) || it.topic.contains("Driving Regulation", ignoreCase = true) || it.topic.contains("Vehicle Safety", ignoreCase = true)) }
                            else -> questions.count { !it.isHidden && it.subject.equals(banner.subjectKey, ignoreCase = true) }
                        }
                    }

                    val currentSelectedChapters = chaptersMap[banner.subjectKey] ?: emptySet()

                    StudySubjectBannerCard(
                        banner = banner,
                        availableChapters = availableChapters,
                        selectedChapters = currentSelectedChapters,
                        onChaptersChanged = { newSet ->
                            chaptersMap = chaptersMap + (banner.subjectKey to newSet)
                        },
                        totalQuestionsCount = totalCount,
                        actionButtonTextEn = "Start Study",
                        actionButtonTextAs = "অধ্যয়ন আৰম্ভ কৰক",
                        onStartClick = {
                            selectedSubjectTab = banner.subjectKey
                            selectedChapters = chaptersMap[banner.subjectKey] ?: emptySet()
                            currentQuestionIndex = 0
                            selectedOptionIndex = null
                            isStudySessionStarted = true
                        },
                        isAssamese = isAssamese
                    )
                }
            }
        } else {
            // STEP 2: ACTIVE STUDY QUESTION PAGE (Bigger View, NO Subject/Chapter Dropdowns!)
            // Header summary
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = when(selectedSubjectTab) {
                            "General Knowledge" -> "General Knowledge"
                            "General English" -> "General English"
                            "General Mathematics" -> "Mathematics"
                            "Reasoning" -> "Reasoning"
                            else -> "All Subjects"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = if (selectedChapters.isEmpty()) {
                            "All Chapters • ${studyQuestionsList.size} Questions"
                        } else {
                            "${selectedChapters.size} Chapters • ${studyQuestionsList.size} Questions"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Live Study Timer & Question Count Header Bar
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Live Timer
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Timer, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        val mins = sessionTotalSeconds / 60
                        val secs = sessionTotalSeconds % 60
                        val timeStr = String.format(Locale.US, "%02d:%02d", mins, secs)
                        Text(
                            text = timeStr,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Question Counter
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.MenuBook, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (studyQuestionsList.isNotEmpty()) "Q ${currentQuestionIndex + 1} / ${studyQuestionsList.size}" else "0 Qs",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Studied Total
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        val totalStudied = userProfile?.totalSolved ?: studiedQuestionsCountInSession
                        Text(
                            text = "Studied: $totalStudied",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

        Spacer(modifier = Modifier.height(24.dp))

        // Question Interactive Card View
        if (currentQuestion == null) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.SearchOff, contentDescription = null, modifier = Modifier.size(56.dp), tint = MaterialTheme.colorScheme.outline)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No questions found for selected chapter",
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = {
                        selectedSubjectTab = "All Subject"
                        selectedChapters = emptySet()
                    }) {
                        Text("View All Subjects")
                    }
                }
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    // Question Header Badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = currentQuestion.subject,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Surface(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = currentQuestion.topic,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
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
                            IconButton(onClick = { viewModel.toggleBookmarkQuestion(currentQuestion) }) {
                                Icon(
                                    imageVector = if (currentQuestion.isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                    contentDescription = "Bookmark",
                                    tint = if (currentQuestion.isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = {
                                viewModel.toggleHideQuestion(currentQuestion)
                                activeStudySessionQuestions = activeStudySessionQuestions.filter { it.id != currentQuestion.id }
                                showHideNotice = true
                            }) {
                                Icon(
                                    imageVector = Icons.Outlined.VisibilityOff,
                                    contentDescription = "Hide Question",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
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

                    Spacer(modifier = Modifier.height(24.dp))

                    // Options List - Directly highlights the correct answer
                    val options = listOf(
                        currentQuestion.optionAEn to currentQuestion.optionAAs,
                        currentQuestion.optionBEn to currentQuestion.optionBAs,
                        currentQuestion.optionCEn to currentQuestion.optionCAs,
                        currentQuestion.optionDEn to currentQuestion.optionDAs
                    )

                    options.forEachIndexed { optIndex, pair ->
                        val isCorrect = (optIndex == currentQuestion.correctOptionIndex)
                        val isSelected = (selectedOptionIndex == optIndex)
                        val optionLetter = ('A' + optIndex).toString()

                        // Card Color logic: Correct answer is ALWAYS directly highlighted in green
                        val backgroundColor = when {
                            isCorrect -> MaterialTheme.colorScheme.successContainer
                            isSelected && !isCorrect -> MaterialTheme.colorScheme.errorContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        }

                        val borderColor = when {
                            isCorrect -> MaterialTheme.colorScheme.success
                            isSelected && !isCorrect -> MaterialTheme.colorScheme.error
                            else -> null
                        }

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp)
                                .clickable {
                                    if (selectedOptionIndex == null) {
                                        selectedOptionIndex = optIndex
                                    }
                                },
                            shape = RoundedCornerShape(12.dp),
                            color = backgroundColor,
                            border = borderColor?.let { BorderStroke(1.5.dp, it) }
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = when {
                                        isCorrect -> MaterialTheme.colorScheme.success
                                        isSelected && !isCorrect -> MaterialTheme.colorScheme.error
                                        else -> MaterialTheme.colorScheme.primaryContainer
                                    },
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        if (isCorrect) {
                                            Icon(Icons.Default.Check, contentDescription = "Correct Answer", tint = Color.White, modifier = Modifier.size(18.dp))
                                        } else if (isSelected && !isCorrect) {
                                            Icon(Icons.Default.Close, contentDescription = "Incorrect Option", tint = Color.White, modifier = Modifier.size(18.dp))
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
                                    fontWeight = if (isCorrect) FontWeight.Bold else FontWeight.Normal,
                                    color = when {
                                        isCorrect -> MaterialTheme.colorScheme.onSuccessContainer
                                        isSelected && !isCorrect -> MaterialTheme.colorScheme.onErrorContainer
                                        else -> Color.Unspecified
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // DIRECT EXPLANATION & ANSWER BREAKDOWN (Directly visible without needing to tap)
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Lightbulb, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Correct Answer & Explanation:",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }

                                Surface(
                                    color = MaterialTheme.colorScheme.success,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "Option ${('A' + currentQuestion.correctOptionIndex)}",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            BilingualText(
                                textEn = currentQuestion.explanationEn,
                                textAs = currentQuestion.explanationAs,
                                language = questionLanguage,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Previous & Next Navigation Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Previous Button (<)
                        OutlinedButton(
                            onClick = {
                                if (currentQuestionIndex > 0) {
                                    currentQuestionIndex--
                                    selectedOptionIndex = null
                                }
                            },
                            enabled = currentQuestionIndex > 0,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Previous")
                        }

                        // Status Badge
                        Text(
                            text = "${currentQuestionIndex + 1} / ${studyQuestionsList.size}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.outline
                        )

                        // Next Button (>)
                        Button(
                            onClick = {
                                markQuestionLearned(currentQuestion.id)
                                if (currentQuestionIndex < studyQuestionsList.size - 1) {
                                    currentQuestionIndex++
                                    selectedOptionIndex = null
                                }
                            },
                            enabled = currentQuestionIndex < studyQuestionsList.size - 1,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Next")
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next", modifier = Modifier.size(18.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // End Learning Button
                    OutlinedButton(
                        onClick = {
                            markQuestionLearned(currentQuestion.id)
                            finalSessionTime = sessionTotalSeconds
                            finalSessionQuestions = studiedQuestionsCountInSession - sessionInitialSolvedCount
                            isStudySessionStarted = false
                            showSummaryDialog = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = "End Learning", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("End Learning", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Learning Summary Dialog
    if (showSummaryDialog) {
        AlertDialog(
            onDismissRequest = { 
                showSummaryDialog = false
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = { Text("Learning Session Summary", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = "Great job! Here is a summary of your study session:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Questions Learned:", fontWeight = FontWeight.Medium)
                        Text("$finalSessionQuestions", fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Time Spent:", fontWeight = FontWeight.Medium)
                        val m = finalSessionTime / 60
                        val s = finalSessionTime % 60
                        Text("${m} min ${s} sec", fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showSummaryDialog = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Done")
                }
            }
        )
    }

        // Free Plan Limit Dialog
        if (showLimitModal) {
            AlertDialog(
                onDismissRequest = { showLimitModal = false },
                icon = { Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(36.dp)) },
                title = {
                    Text(
                        text = "Free Daily Limit Reached",
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = "You have studied 25 questions in this session. Upgrade to Jukti Premium to unlock unlimited MCQ practice, custom tests, and detailed PDF study notes!"
                    )
                },
                confirmButton = {
                    Button(onClick = {
                        showLimitModal = false
                        viewModel.navigateTo(Screen.PREMIUM_PLANS)
                    }) {
                        Text("Upgrade to Premium")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLimitModal = false }) {
                        Text("Continue Free")
                    }
                }
            )
        }

        if (showHideNotice) {
            AlertDialog(
                onDismissRequest = { showHideNotice = false },
                icon = { Icon(Icons.Outlined.VisibilityOff, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                title = { Text("Question Hidden") },
                text = {
                    Text(
                        "This question has been hidden because you know it very well! It will no longer appear in your study or practice sessions. You can review or unhide it anytime under Settings."
                    )
                },
                confirmButton = {
                    TextButton(onClick = { showHideNotice = false }) {
                        Text("Got It")
                    }
                }
            )
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

/* =====================================================================
   TAB 2: PRACTICE MCQ MODULE
   ===================================================================== */
@Composable
fun PracticeMcqTab(viewModel: JuktiViewModel) {
    val language by viewModel.language.collectAsState()
    val questionLanguage by viewModel.questionLanguage.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val questions by viewModel.questions.collectAsState()
    val isAssamese = language == AppLanguage.ASSAMESE
    val context = androidx.compose.ui.platform.LocalContext.current

    var activePracticeMode by remember { mutableStateOf("Mixed") }
    var currentQuestionIndex by remember { mutableStateOf(0) }
    var selectedOptionIndex by remember { mutableStateOf<Int?>(null) }
    var isSubmitted by remember { mutableStateOf(false) }
    var scoreCount by remember { mutableStateOf(0) }
    var showReportDialog by remember { mutableStateOf(false) }

    val activeQuestions = remember(questions, activePracticeMode) {
        val nonHidden = questions.filter { !it.isHidden }
        if (activePracticeMode == "Shuffle") nonHidden.shuffled() else nonHidden
    }

    val currentQuestion = activeQuestions.getOrNull(currentQuestionIndex)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {


        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Timed Practice Quiz",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(20.dp)
                ) {
                    val count = userProfile?.totalSolved ?: 0
                    Text(
                        text = "Practiced: $count",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }

                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = "Q ${currentQuestionIndex + 1} / ${activeQuestions.size}",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("Mixed", "Shuffle", "Timed").forEach { mode ->
                    FilterChip(
                        selected = (activePracticeMode == mode),
                        onClick = {
                            activePracticeMode = mode
                            currentQuestionIndex = 0
                            selectedOptionIndex = null
                            isSubmitted = false
                        },
                        label = { Text(mode, fontSize = 12.sp) }
                    )
                }
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

        Spacer(modifier = Modifier.height(14.dp))

        if (currentQuestion == null) {
            com.example.ui.components.EmptyStateIllustration(
                type = com.example.ui.components.EmptyStateType.RHINO_BOOK,
                title = "No Questions",
                message = "No practice questions available.",
                modifier = Modifier.fillMaxWidth().padding(top = 40.dp)
            )
        } else {
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
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = currentQuestion.subject,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        Row {
                            IconButton(onClick = { showReportDialog = true }) {
                                Icon(
                                    imageVector = Icons.Default.Report,
                                    contentDescription = "Report Question",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                            IconButton(onClick = { viewModel.toggleBookmarkQuestion(currentQuestion) }) {
                                Icon(
                                    imageVector = if (currentQuestion.isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                    contentDescription = "Bookmark",
                                    tint = if (currentQuestion.isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = {
                                viewModel.toggleHideQuestion(currentQuestion)
                            }) {
                                Icon(
                                    imageVector = Icons.Outlined.VisibilityOff,
                                    contentDescription = "Hide Question",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

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

                    Spacer(modifier = Modifier.height(24.dp))

                    val options = listOf(
                        currentQuestion.optionAEn to currentQuestion.optionAAs,
                        currentQuestion.optionBEn to currentQuestion.optionBAs,
                        currentQuestion.optionCEn to currentQuestion.optionCAs,
                        currentQuestion.optionDEn to currentQuestion.optionDAs
                    )

                    options.forEachIndexed { optIndex, pair ->
                        val isSelected = (selectedOptionIndex == optIndex)
                        val isCorrect = (optIndex == currentQuestion.correctOptionIndex)

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    if (!isSubmitted) {
                                        selectedOptionIndex = optIndex
                                    }
                                },
                            shape = RoundedCornerShape(12.dp),
                            color = when {
                                isSubmitted && isCorrect -> MaterialTheme.colorScheme.successContainer
                                isSubmitted && isSelected && !isCorrect -> MaterialTheme.colorScheme.errorContainer
                                isSelected -> MaterialTheme.colorScheme.primaryContainer
                                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${('A' + optIndex)}. ",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                BilingualText(
                                    textEn = pair.first,
                                    textAs = pair.second,
                                    language = questionLanguage,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    if (!isSubmitted) {
                        Button(
                            onClick = {
                                if (selectedOptionIndex != null) {
                                    isSubmitted = true
                                    val isAnsCorrect = (selectedOptionIndex == currentQuestion.correctOptionIndex)
                                    if (isAnsCorrect) {
                                        scoreCount += 10
                                    }
                                    viewModel.submitQuestionAnswer(currentQuestion.id, isAnsCorrect, 10)
                                }
                            },
                            enabled = selectedOptionIndex != null,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Submit Answer")
                        }
                    } else {
                        Button(
                            onClick = {
                                if (currentQuestionIndex < activeQuestions.size - 1) {
                                    currentQuestionIndex++
                                    selectedOptionIndex = null
                                    isSubmitted = false
                                } else {
                                    viewModel.awardChapterCompletionXp()
                                    viewModel.navigateTo(Screen.HOME)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (currentQuestionIndex == activeQuestions.size - 1) ("Finish Practice") else ("Next Question"))
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

/* =====================================================================
   TAB 3: POMODORO CLOCK MODULE
   ===================================================================== */
@Composable
fun PomodoroClockTab(viewModel: JuktiViewModel) {
    val language by viewModel.language.collectAsState()
    val isAssamese = language == AppLanguage.ASSAMESE
    val context = androidx.compose.ui.platform.LocalContext.current

    var timerMode by remember { mutableStateOf("Study") } // "Study" (25m) or "Break" (5m)
    var totalTimeSeconds by remember { mutableStateOf(25 * 60) }
    var timeRemainingSeconds by remember { mutableStateOf(25 * 60) }
    var isTimerRunning by remember { mutableStateOf(false) }
    var completedSessions by remember { mutableStateOf(0) }

    LaunchedEffect(isTimerRunning, timeRemainingSeconds) {
        if (isTimerRunning && timeRemainingSeconds > 0) {
            delay(1000L)
            timeRemainingSeconds--
            if (timeRemainingSeconds % 60 == 0) {
                viewModel.recordStudyProgress(0, 60)
            }
        } else if (isTimerRunning && timeRemainingSeconds == 0) {
            isTimerRunning = false
            if (timerMode == "Study") {
                completedSessions++
                timerMode = "Break"
                totalTimeSeconds = 5 * 60
                timeRemainingSeconds = 5 * 60
            } else {
                timerMode = "Study"
                totalTimeSeconds = 25 * 60
                timeRemainingSeconds = 25 * 60
            }
        }
    }

    val progressRatio = if (totalTimeSeconds > 0) timeRemainingSeconds.toFloat() / totalTimeSeconds.toFloat() else 0f
    val minutes = timeRemainingSeconds / 60
    val seconds = timeRemainingSeconds % 60
    val formattedTime = String.format(Locale.US, "%02d:%02d", minutes, seconds)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {


        // Timer Mode Pills
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            FilterChip(
                selected = (timerMode == "Study"),
                onClick = {
                    timerMode = "Study"
                    totalTimeSeconds = 25 * 60
                    timeRemainingSeconds = 25 * 60
                    isTimerRunning = false
                },
                label = { Text("Study Mode (25m)") }
            )
            FilterChip(
                selected = (timerMode == "Break"),
                onClick = {
                    timerMode = "Break"
                    totalTimeSeconds = 5 * 60
                    timeRemainingSeconds = 5 * 60
                    isTimerRunning = false
                },
                label = { Text("Short Break (5m)") }
            )
        }

        Spacer(modifier = Modifier.height(30.dp))

        // Large Circular Clock Ring
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(220.dp)
        ) {
            CircularProgressIndicator(
                progress = progressRatio,
                modifier = Modifier.fillMaxSize(),
                strokeWidth = 12.dp,
                color = if (timerMode == "Study") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.success,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = formattedTime,
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = if (timerMode == "Study") ("FOCUSING") else ("REST TIME"),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Control Buttons
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(
                onClick = { isTimerRunning = !isTimerRunning },
                shape = RoundedCornerShape(16.dp),
                
            ) {
                Icon(if (isTimerRunning) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isTimerRunning) ("Pause") else ("Start Timer"))
            }

            OutlinedButton(
                onClick = {
                    isTimerRunning = false
                    timeRemainingSeconds = totalTimeSeconds
                },
                shape = RoundedCornerShape(16.dp),
                
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Reset")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Completed Sessions Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Completed Focus Sessions Today:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                Text(
                    text = "$completedSessions",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

/* =====================================================================
   TAB 4: EXAM PATTERN & CUTOFF MODULE
   ===================================================================== */
@Composable
fun ExamPatternAndCutoffTab(viewModel: JuktiViewModel) {
    val language by viewModel.language.collectAsState()
    val isAssamese = language == AppLanguage.ASSAMESE
    val context = androidx.compose.ui.platform.LocalContext.current

    var selectedExam by remember { mutableStateOf("ADRE") } // ADRE, APSC, Assam Police, TET

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {


        // Exam Selector Chips
        ScrollableTabRow(
            selectedTabIndex = listOf("ADRE", "APSC", "Assam Police", "TET").indexOf(selectedExam),
            edgePadding = 0.dp,
            divider = {}
        ) {
            listOf("ADRE", "APSC", "Assam Police", "TET").forEach { exam ->
                Tab(
                    selected = (selectedExam == exam),
                    onClick = { selectedExam = exam },
                    text = { Text(exam, fontWeight = FontWeight.Bold) }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        when (selectedExam) {
            "ADRE" -> AdreExamPatternCard(isAssamese)
            "APSC" -> ApscExamPatternCard(isAssamese)
            "Assam Police" -> AssamPoliceExamPatternCard(isAssamese)
            "TET" -> TetExamPatternCard(isAssamese)
        }
    }
}

@Composable
fun AdreExamPatternCard(isAssamese: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "ADRE Grade 3 & 4 Exam Pattern (2026)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Text("• Total Questions: 100 MCQs")
            Text("• Total Marks: 150 Marks")
            Text("• Duration: 90 Minutes (1.5 Hours)")
            Text("• Negative Marking: 0.25 Marks per wrong answer")
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Subject Marks Distribution:",
                fontWeight = FontWeight.Bold
            )
            Text("1. General Knowledge & Assam GK: 35 Qs (52.5 Marks)")
            Text("2. General English: 25 Qs (37.5 Marks)")
            Text("3. General Mathematics: 20 Qs (30 Marks)")
            Text("4. Logical Reasoning: 20 Qs (30 Marks)")

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            Text(
                text = "Expected Cutoff Marks (Grade 3):",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Unreserved (UR):"); Text("118 - 124 Marks", fontWeight = FontWeight.Bold)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("OBC / MOBC:"); Text("110 - 116 Marks", fontWeight = FontWeight.Bold)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("SC Category:"); Text("102 - 108 Marks", fontWeight = FontWeight.Bold)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("ST (P) / ST (H):"); Text("96 - 102 Marks", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ApscExamPatternCard(isAssamese: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "APSC CCE Prelims Exam Pattern",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Text("• Paper 1: General Studies 1 (200 Marks, 100 Qs)")
            Text("• Paper 2: GS Paper 2 - CSAT (200 Marks, 80 Qs - Qualifying 33%)")
            Text("• Duration: 2 Hours per Paper")
            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            Text(
                text = "Expected Cutoff Marks (GS Paper 1):",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("General (UR):"); Text("112 - 118 Marks", fontWeight = FontWeight.Bold)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("OBC / MOBC:"); Text("104 - 110 Marks", fontWeight = FontWeight.Bold)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("SC / ST:"); Text("95 - 102 Marks", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun AssamPoliceExamPatternCard(isAssamese: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Assam Police SI & AB/UB Constable Pattern",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Text("• Written Test: 100 Marks (100 Questions)")
            Text("• Physical Efficiency Test (PET): 40 Marks")
            Text("• Extra Qualification / NCC / Computer: 10 Marks")
            Text("• Total Merit Base: 150 Marks")
            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            Text(
                text = "Expected Cutoff (SI Male / Female):",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("UR Male:"); Text("74 - 78 / 100", fontWeight = FontWeight.Bold)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("UR Female:"); Text("68 - 72 / 100", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun QuestionStudyCard(
    question: QuestionEntity,
    language: AppLanguage,
    onBookmarkToggle: () -> Unit,
    onLikeToggle: () -> Unit,
    onReportClick: () -> Unit,
    onHideClick: (() -> Unit)? = null
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
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
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = question.subject,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Row {
                    IconButton(onClick = onReportClick) {
                        Icon(
                            imageVector = Icons.Default.Report,
                            contentDescription = "Report",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                    IconButton(onClick = onBookmarkToggle) {
                        Icon(
                            imageVector = if (question.isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = if (question.isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (onHideClick != null) {
                        IconButton(onClick = onHideClick) {
                            Icon(
                                imageVector = Icons.Outlined.VisibilityOff,
                                contentDescription = "Hide Question",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            com.example.ui.components.QuestionTypeBadge(
                questionType = question.questionType,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            BilingualText(
                textEn = question.questionEn,
                textAs = question.questionAs,
                language = language,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(onClick = { expanded = !expanded }) {
                Text(if (expanded) "Hide Answer" else "Show Answer & Explanation")
            }

            if (expanded) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "Correct Option: ${('A' + question.correctOptionIndex)}",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        BilingualText(
                            textEn = question.explanationEn,
                            textAs = question.explanationAs,
                            language = language,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TetExamPatternCard(isAssamese: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Assam Special TET Exam Pattern",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Text("• Total Questions: 150 MCQs")
            Text("• Total Marks: 150 Marks (No negative marking)")
            Text("• Duration: 150 Minutes")
            Text("• Minimum Passing Cutoff: 60% (90 Marks) for General, 55% (83.5 Marks) for Reserved categories")
        }
    }
}

/* =====================================================================
   CURRENT AFFAIRS STUDY NOTES ONE-PAGE DATA MODULE
   ===================================================================== */
@Composable
fun CurrentAffairsNotesTab(viewModel: JuktiViewModel) {
    val language by viewModel.language.collectAsState()
    val allNotes by viewModel.studyNotes.collectAsState()
    val isAssamese = language == AppLanguage.ASSAMESE
    val context = androidx.compose.ui.platform.LocalContext.current

    var selectedNote by remember { mutableStateOf<StudyNoteEntity?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    val categories = listOf("All", "Assam News", "National", "Schemes", "Sports")

    val currentAffairsNotes = remember(allNotes, searchQuery, selectedCategory) {
        val caNotes = allNotes.filter { it.subject.contains("Current Affairs", ignoreCase = true) }
        val finalNotes = caNotes

        finalNotes.filter { note ->
            val matchesCategory = when (selectedCategory) {
                "Assam News" -> note.topic.contains("Assam", ignoreCase = true) || note.titleEn.contains("Assam", ignoreCase = true)
                "National" -> note.topic.contains("National", ignoreCase = true) || note.titleEn.contains("National", ignoreCase = true)
                "Schemes" -> note.topic.contains("Scheme", ignoreCase = true) || note.titleEn.contains("Scheme", ignoreCase = true)
                "Sports" -> note.topic.contains("Sports", ignoreCase = true) || note.titleEn.contains("Sports", ignoreCase = true)
                else -> true
            }
            val matchesSearch = searchQuery.isEmpty() ||
                    note.titleEn.contains(searchQuery, ignoreCase = true) ||
                    note.titleAs.contains(searchQuery, ignoreCase = true) ||
                    note.topic.contains(searchQuery, ignoreCase = true)

            matchesCategory && matchesSearch
        }
    }

    val activeNote = selectedNote
    if (activeNote != null) {
        val note = activeNote
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = { selectedNote = null },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Back to List")
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(onClick = { viewModel.toggleBookmarkNote(note) }) {
                        Icon(
                            imageVector = if (note.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = if (note.isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Surface(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${note.readTimeMinutes} min read • ${note.topic}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            BilingualText(
                textEn = note.titleEn,
                textAs = note.titleAs,
                language = language,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    val contentEn = note.contentEn
                    val contentAs = note.contentAs
                    if (contentAs.isNotBlank() && isAssamese) {
                        Text(
                            text = contentAs,
                            style = MaterialTheme.typography.bodyLarge,
                            lineHeight = 24.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    } else {
                        Text(
                            text = contentEn,
                            style = MaterialTheme.typography.bodyLarge,
                            lineHeight = 24.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                SafeOutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = "Search current affairs topic or keyword...",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }

            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { category ->
                        FilterChip(
                            selected = (selectedCategory == category),
                            onClick = { selectedCategory = category },
                            label = { Text(category) },
                            shape = RoundedCornerShape(20.dp)
                        )
                    }
                }
            }

            if (currentAffairsNotes.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No current affairs study notes found.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(currentAffairsNotes, key = { it.id }) { note ->
                    Card(
                        onClick = { selectedNote = note },
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
                                Surface(
                                    color = MaterialTheme.colorScheme.tertiaryContainer,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = note.topic,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                }

                                Text(
                                    text = "${note.readTimeMinutes} min read",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = note.titleEn,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = note.contentEn.take(120) + "...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(onClick = { selectedNote = note }) {
                                    Text("Read Full Notes →", fontWeight = FontWeight.Bold)
                                }

                                IconButton(onClick = { viewModel.toggleBookmarkNote(note) }) {
                                    Icon(
                                        imageVector = if (note.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                        contentDescription = "Bookmark",
                                        tint = if (note.isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
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

