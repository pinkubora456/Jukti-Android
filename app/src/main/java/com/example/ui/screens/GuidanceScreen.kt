package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.data.local.*
import com.example.ui.components.SafeOutlinedTextField
import com.example.ui.viewmodel.JuktiViewModel
import com.example.ui.viewmodel.Screen
import com.example.data.util.*

enum class GuidanceSectionType {
    NONE, PYQ_FOCUS, PRIORITY_TOPICS, STRENGTH_WEAKNESS, PREP_STRATEGY
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuidanceScreen(viewModel: JuktiViewModel) {
    var currentSection by rememberSaveable { mutableStateOf(GuidanceSectionType.NONE) }
    
    // Independent states for each section
    var pyqExam by rememberSaveable { mutableStateOf<String?>(null) }
    var pyqSubject by rememberSaveable { mutableStateOf("All Subjects") }

    var priorityExam by rememberSaveable { mutableStateOf<String?>(null) }
    var prioritySubject by rememberSaveable { mutableStateOf("All Subjects") }

    var strengthExam by rememberSaveable { mutableStateOf<String?>(null) }
    var strengthSubject by rememberSaveable { mutableStateOf("All Subjects") }

    var strategyExam by rememberSaveable { mutableStateOf<String?>(null) }
    var strategySubject by rememberSaveable { mutableStateOf("All Subjects") }

    val examsList by viewModel.examsList.collectAsState()
    val allPyqFocus by viewModel.allPyqFocus.collectAsState(initial = emptyList())
    val allPrepStrategies by viewModel.allPrepStrategies.collectAsState(initial = emptyList())
    val allQuestions by viewModel.questions.collectAsState()
    val userQuestionStates by viewModel.userQuestionStates.collectAsState(initial = emptyList())
    val allSubjectsChapters by viewModel.allSubjectsChapters.collectAsState()

    val effectiveEntitlement by viewModel.effectiveEntitlement.collectAsState()
    val isAdminOrOwner by viewModel.isAdminOrOwner.collectAsState()

    val isGuidanceUnlocked = isAdminOrOwner || (effectiveEntitlement?.isPremium == true && effectiveEntitlement?.guidanceEnabled == true)

    val accessibleExams = remember(examsList, effectiveEntitlement, isAdminOrOwner) {
        PlanValidityEngine.filterAccessibleGuidanceExams(examsList, effectiveEntitlement, isAdminOrOwner)
    }

    // Initialize default exams constrained strictly by allowed exams
    LaunchedEffect(accessibleExams) {
        if (accessibleExams.isNotEmpty()) {
            val validTitles = accessibleExams.map { it.title }
            if (pyqExam == null || !validTitles.contains(pyqExam)) pyqExam = accessibleExams.first().title
            if (priorityExam == null || !validTitles.contains(priorityExam)) priorityExam = accessibleExams.first().title
            if (strengthExam == null || !validTitles.contains(strengthExam)) strengthExam = accessibleExams.first().title
            if (strategyExam == null || !validTitles.contains(strategyExam)) strategyExam = accessibleExams.first().title
        } else {
            pyqExam = null
            priorityExam = null
            strengthExam = null
            strategyExam = null
        }
    }

    Crossfade(targetState = currentSection) { section ->
        when (section) {
            GuidanceSectionType.NONE -> {
                GuidanceLandingPage(
                    isGuidanceUnlocked = isGuidanceUnlocked,
                    effectivePlanName = effectiveEntitlement?.effectivePlanName ?: "Free Plan",
                    onBackClick = { viewModel.navigateTo(Screen.HOME) },
                    onSectionClick = { currentSection = it },
                    onUpgradeClick = { viewModel.navigateTo(Screen.PREMIUM_PLANS) }
                )
            }
            GuidanceSectionType.PYQ_FOCUS -> {
                GuidanceDetailScreen(
                    title = "PYQ Focus",
                    icon = Icons.Default.History,
                    selectedExam = pyqExam,
                    selectedSubject = pyqSubject,
                    accessibleExams = accessibleExams,
                    allPyqFocus = allPyqFocus,
                    allQuestions = allQuestions,
                    effectiveEntitlement = effectiveEntitlement,
                    isAdminOrOwner = isAdminOrOwner,
                    onExamChange = { pyqExam = it; pyqSubject = "All Subjects" },
                    onSubjectChange = { pyqSubject = it },
                    onBackClick = { currentSection = GuidanceSectionType.NONE },
                    onUpgradeClick = { viewModel.navigateTo(Screen.PREMIUM_PLANS) }
                ) { guidanceData ->
                    if (guidanceData.pyqFocus.isNotEmpty()) {
                        PyqFocusSection(guidanceData.pyqFocus) { chapter -> 
                            viewModel.startSmartPracticeSession(guidanceData.exam, if (pyqSubject != "All Subjects") pyqSubject else guidanceData.pyqFocus.firstOrNull { it.chapter == chapter }?.subject ?: "", chapter, "")
                        }
                    } else {
                        EmptyGuidanceSection("PYQ Focus", "No PYQ data available for this selection yet.", Icons.Default.History)
                    }
                }
            }
            GuidanceSectionType.PRIORITY_TOPICS -> {
                GuidanceDetailScreen(
                    title = "Priority Topics",
                    icon = Icons.Default.TrackChanges,
                    selectedExam = priorityExam,
                    selectedSubject = prioritySubject,
                    accessibleExams = accessibleExams,
                    allPyqFocus = allPyqFocus,
                    allQuestions = allQuestions,
                    userQuestionStates = userQuestionStates,
                    effectiveEntitlement = effectiveEntitlement,
                    isAdminOrOwner = isAdminOrOwner,
                    onExamChange = { priorityExam = it; prioritySubject = "All Subjects" },
                    onSubjectChange = { prioritySubject = it },
                    onBackClick = { currentSection = GuidanceSectionType.NONE },
                    onUpgradeClick = { viewModel.navigateTo(Screen.PREMIUM_PLANS) }
                ) { guidanceData ->
                    if (guidanceData.priorityTopics.isNotEmpty()) {
                        PriorityTopicsSection(guidanceData.priorityTopics) { subj, chapter ->
                            viewModel.startSmartPracticeSession(guidanceData.exam, subj, chapter, "")
                        }
                    } else {
                        EmptyGuidanceSection("Priority Topics", "No priority data available.", Icons.Default.TrackChanges)
                    }
                }
            }
            GuidanceSectionType.STRENGTH_WEAKNESS -> {
                GuidanceDetailScreen(
                    title = "Strength & Weakness",
                    icon = Icons.Default.FitnessCenter,
                    selectedExam = strengthExam,
                    selectedSubject = strengthSubject,
                    accessibleExams = accessibleExams,
                    allPyqFocus = allPyqFocus,
                    allQuestions = allQuestions,
                    userQuestionStates = userQuestionStates,
                    effectiveEntitlement = effectiveEntitlement,
                    isAdminOrOwner = isAdminOrOwner,
                    onExamChange = { strengthExam = it; strengthSubject = "All Subjects" },
                    onSubjectChange = { strengthSubject = it },
                    onBackClick = { currentSection = GuidanceSectionType.NONE },
                    onUpgradeClick = { viewModel.navigateTo(Screen.PREMIUM_PLANS) }
                ) { guidanceData ->
                    if (guidanceData.strengthWeakness.isNotEmpty()) {
                        StrengthWeaknessSection(guidanceData.strengthWeakness) { subj, chapter ->
                            viewModel.startSmartPracticeSession(guidanceData.exam, subj, chapter, "")
                        }
                    } else {
                        EmptyGuidanceSection("Strength & Weakness", "Not enough practice data yet. Practice more questions to unlock your Strength & Weakness analysis.", Icons.Default.FitnessCenter) {
                            viewModel.startSmartPracticeSession(guidanceData.exam, "", "", "")
                        }
                    }
                }
            }
            GuidanceSectionType.PREP_STRATEGY -> {
                GuidanceDetailScreen(
                    title = "Preparation Strategy",
                    icon = Icons.Default.Lightbulb,
                    selectedExam = strategyExam,
                    selectedSubject = strategySubject,
                    accessibleExams = accessibleExams,
                    allPyqFocus = allPyqFocus,
                    allQuestions = allQuestions,
                    userQuestionStates = userQuestionStates,
                    allPrepStrategies = allPrepStrategies,
                    effectiveEntitlement = effectiveEntitlement,
                    isAdminOrOwner = isAdminOrOwner,
                    onExamChange = { strategyExam = it; strategySubject = "All Subjects" },
                    onSubjectChange = { strategySubject = it },
                    onBackClick = { currentSection = GuidanceSectionType.NONE },
                    onUpgradeClick = { viewModel.navigateTo(Screen.PREMIUM_PLANS) }
                ) { guidanceData ->
                    if (guidanceData.preparationStrategy.isNotBlank()) {
                        PrepStrategySection(guidanceData.exam, guidanceData.preparationStrategy)
                    } else {
                        EmptyGuidanceSection("Preparation Strategy", "No preparation strategy available.", Icons.Default.Lightbulb)
                    }
                }
            }
        }
    }
}

@Composable
fun GuidanceLockedView(
    title: String = "Guidance Locked",
    message: String = "Personalized Guidance is an exclusive premium benefit included with supported subscription plans.",
    currentPlanName: String = "Free Plan",
    onUpgradeClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Guidance Locked",
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Current Plan: ",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = currentPlanName,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Button(
                    onClick = onUpgradeClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("View Supported Plans", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuidanceLandingPage(
    isGuidanceUnlocked: Boolean,
    effectivePlanName: String,
    onBackClick: () -> Unit,
    onSectionClick: (GuidanceSectionType) -> Unit,
    onUpgradeClick: () -> Unit
) {
    Scaffold(
        topBar = {
            com.example.ui.components.JuktiTopAppBar(
                title = "Guidance",
                onBackClick = onBackClick
            )
        }
    ) { paddingValues ->
        if (!isGuidanceUnlocked) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                GuidanceLockedView(
                    title = "Guidance Access Locked",
                    message = "Personalized Guidance (PYQ Focus, Priority Topics, Strength & Weakness, and Preparation Strategy) is automatically included with supported subscription plans.",
                    currentPlanName = effectivePlanName,
                    onUpgradeClick = onUpgradeClick
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    GuidanceBannerCard(
                        title = "PYQ Focus",
                        subtitle = "See previous exam question trends and important chapters",
                        icon = Icons.Default.History,
                        onClick = { onSectionClick(GuidanceSectionType.PYQ_FOCUS) }
                    )
                }
                item {
                    GuidanceBannerCard(
                        title = "Priority Topics",
                        subtitle = "Know what to study first",
                        icon = Icons.Default.TrackChanges,
                        onClick = { onSectionClick(GuidanceSectionType.PRIORITY_TOPICS) }
                    )
                }
                item {
                    GuidanceBannerCard(
                        title = "Strength & Weakness",
                        subtitle = "Understand your performance",
                        icon = Icons.Default.FitnessCenter,
                        onClick = { onSectionClick(GuidanceSectionType.STRENGTH_WEAKNESS) }
                    )
                }
                item {
                    GuidanceBannerCard(
                        title = "Preparation Strategy",
                        subtitle = "Know how to prepare effectively",
                        icon = Icons.Default.Lightbulb,
                        onClick = { onSectionClick(GuidanceSectionType.PREP_STRATEGY) }
                    )
                }
            }
        }
    }
}

@Composable
fun GuidanceBannerCard(title: String, subtitle: String, icon: ImageVector, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                    .padding(8.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Go",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuidanceDetailScreen(
    title: String,
    icon: ImageVector,
    selectedExam: String?,
    selectedSubject: String,
    accessibleExams: List<ExamEntity>,
    allPyqFocus: List<PyqFocusEntity>,
    allQuestions: List<QuestionEntity>,
    userQuestionStates: List<UserQuestionStateEntity> = emptyList(),
    allPrepStrategies: List<PrepStrategyEntity> = emptyList(),
    effectiveEntitlement: EffectiveUserEntitlement? = null,
    isAdminOrOwner: Boolean = false,
    onExamChange: (String) -> Unit,
    onSubjectChange: (String) -> Unit,
    onBackClick: () -> Unit,
    onUpgradeClick: () -> Unit,
    content: @Composable (GuidanceData) -> Unit
) {
    var examDropdownExpanded by remember { mutableStateOf(false) }
    var subjectDropdownExpanded by remember { mutableStateOf(false) }

    val isExamAllowed = selectedExam != null && (isAdminOrOwner || PlanValidityEngine.isGuidanceAccessibleForExam(selectedExam, effectiveEntitlement, isAdminOrOwner))

    val availableSubjects = remember(allPyqFocus, selectedExam, allQuestions) {
        val pyqSubjects = allPyqFocus.filter { it.exam == selectedExam }.map { it.subject }
        val qSubjects = allQuestions.filter { it.examCategory.contains(selectedExam ?: "", ignoreCase = true) }.map { it.subject }
        val subjects = (pyqSubjects + qSubjects).distinct().filter { it.isNotBlank() }.sorted()
        if (subjects.isEmpty()) listOf("All Subjects") else listOf("All Subjects") + subjects
    }

    val guidanceData = remember(selectedExam, selectedSubject, allPyqFocus, allQuestions, userQuestionStates, allPrepStrategies, effectiveEntitlement, isAdminOrOwner) {
        if (selectedExam == null) null
        else GuidanceEngine.calculateGuidance(
            exam = selectedExam,
            subject = if (selectedSubject == "All Subjects") null else selectedSubject,
            allPyqFocus = allPyqFocus,
            allQuestions = allQuestions,
            userStates = userQuestionStates,
            allPrepStrategies = allPrepStrategies,
            effectiveEntitlement = effectiveEntitlement,
            isAdminOrOwner = isAdminOrOwner
        )
    }

    Scaffold(
        topBar = {
            com.example.ui.components.JuktiTopAppBar(
                title = title,
                onBackClick = onBackClick
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Exam Selection
            Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                ExposedDropdownMenuBox(
                    expanded = examDropdownExpanded,
                    onExpandedChange = { examDropdownExpanded = !examDropdownExpanded }
                ) {
                    SafeOutlinedTextField(
                        value = selectedExam ?: "Select Exam",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Select Exam") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = examDropdownExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = examDropdownExpanded,
                        onDismissRequest = { examDropdownExpanded = false }
                    ) {
                        if (accessibleExams.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("No exam levels available") },
                                onClick = { examDropdownExpanded = false }
                            )
                        } else {
                            accessibleExams.forEach { exam ->
                                DropdownMenuItem(
                                    text = { Text(exam.title) },
                                    onClick = {
                                        onExamChange(exam.title)
                                        examDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
            
            // Subject Selection (only displayed when exam is selected and access is allowed)
            if (selectedExam != null && isExamAllowed && guidanceData?.isAccessDenied != true) {
                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                    ExposedDropdownMenuBox(
                        expanded = subjectDropdownExpanded,
                        onExpandedChange = { subjectDropdownExpanded = !subjectDropdownExpanded }
                    ) {
                        SafeOutlinedTextField(
                            value = selectedSubject,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Select Subject") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjectDropdownExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = subjectDropdownExpanded,
                            onDismissRequest = { subjectDropdownExpanded = false }
                        ) {
                            availableSubjects.forEach { subject ->
                                DropdownMenuItem(
                                    text = { Text(subject) },
                                    onClick = {
                                        onSubjectChange(subject)
                                        subjectDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            if (!isExamAllowed || guidanceData?.isAccessDenied == true) {
                GuidanceLockedView(
                    title = "Access Restricted",
                    message = guidanceData?.accessDeniedReason ?: "Your active plan does not include Guidance for '${selectedExam ?: "this exam"}'. Please upgrade your plan to unlock Guidance for this exam level.",
                    currentPlanName = effectiveEntitlement?.effectivePlanName ?: "Free Plan",
                    onUpgradeClick = onUpgradeClick
                )
            } else if (guidanceData != null) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    item {
                        content(guidanceData)
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Please select an exam to view guidance.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun EmptyGuidanceSection(title: String, message: String, icon: ImageVector, onAction: (() -> Unit)? = null) {
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        SectionHeader(title, icon)
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                if (onAction != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = onAction) {
                        Text("Start Practice")
                    }
                }
            }
        }
    }
}

@Composable
fun PyqFocusSection(pyqData: List<PyqFocusEntity>, onPractice: (String) -> Unit) {
    val groupedBySubject = pyqData.groupBy { it.subject }

    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        groupedBySubject.forEach { (subject, chapters) ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(subject, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                        Text("Chapter", modifier = Modifier.weight(2f), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                        Text("PYQs", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
                        Text("Exams", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
                        Text("Avg", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
                    }
                    HorizontalDivider()
                    
                    chapters.sortedByDescending { if (it.examsCovered > 0) it.pyqCount.toFloat() / it.examsCovered else 0f }.forEach { chapterData ->
                        val avg = if (chapterData.examsCovered > 0) chapterData.pyqCount.toFloat() / chapterData.examsCovered else 0f
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).clickable { onPractice(chapterData.chapter) }, 
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(chapterData.chapter, modifier = Modifier.weight(2f), style = MaterialTheme.typography.bodySmall)
                            Text("${chapterData.pyqCount}", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
                            Text("${chapterData.examsCovered}", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
                            Text(String.format("%.1f", avg), modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, color = if(avg >= 2f) Color(0xFFE53935) else MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PriorityTopicsSection(topics: List<PriorityTopicItem>, onPractice: (String, String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                topics.take(10).forEachIndexed { index, topic ->
                    val (statusColor, statusIcon) = when (topic.priorityLabel) {
                        "High Priority" -> Pair(Color(0xFFE53935), Icons.Default.Warning)
                        "Medium Priority" -> Pair(Color(0xFFFDD835), Icons.Default.TrendingDown)
                        "Maintain" -> Pair(Color(0xFF43A047), Icons.Default.Verified)
                        else -> Pair(Color(0xFF81C784), Icons.Default.Check)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(topic.priorityLabel.uppercase(), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = statusColor)
                            Text(topic.subject + " - " + topic.chapter, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text("PYQ Avg: ${String.format("%.1f", topic.pyqAvg)} | Your Accuracy: ${topic.accuracy?.let { String.format("%.0f%%", it) } ?: "N/A"}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(topic.reason, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        
                        TextButton(onClick = { onPractice(topic.subject, topic.chapter) }) {
                            Text("Practice")
                        }
                    }
                    if (index < topics.size - 1) HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                }
            }
        }
    }
}

@Composable
fun StrengthWeaknessSection(items: List<StrengthWeaknessItem>, onImprove: (String, String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items.take(10).forEachIndexed { index, item ->
                    val (statusColor, statusIcon) = when {
                        item.statusLabel == "Not Enough Data" -> Pair(MaterialTheme.colorScheme.onSurfaceVariant, Icons.Default.HelpOutline)
                        item.isImportant && item.isWeak -> Pair(Color(0xFFE53935), Icons.Default.Warning) // Red
                        item.isImportant && !item.isWeak -> Pair(Color(0xFF43A047), Icons.Default.Verified) // Green
                        !item.isImportant && item.isWeak -> Pair(Color(0xFFFDD835), Icons.Default.TrendingDown) // Yellow
                        else -> Pair(Color(0xFF43A047), Icons.Default.TrendingUp) // Green
                    }

                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.subject + " - " + item.chapter, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text("Avg PYQ: ${String.format("%.1f", item.pyqAvg)} | Accuracy: ${item.accuracy?.let { String.format("%.0f%%", it) } ?: "N/A"}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.background(statusColor.copy(alpha = 0.1f), RoundedCornerShape(16.dp)).padding(horizontal = 8.dp, vertical = 4.dp).clickable { onImprove(item.subject, item.chapter) }) {
                            Icon(statusIcon, contentDescription = null, tint = statusColor, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(item.statusLabel, color = statusColor, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                    }
                    if (index < items.size - 1) HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                }
            }
        }
    }
}

@Composable
fun PrepStrategySection(exam: String, content: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
            Text(
                text = content,
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
fun SectionHeader(title: String, icon: ImageVector) {
    Row(modifier = Modifier.padding(bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(8.dp))
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    }
}
