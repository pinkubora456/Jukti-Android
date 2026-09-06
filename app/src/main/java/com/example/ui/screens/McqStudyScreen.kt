package com.example.ui.screens

import com.example.ui.components.SafeOutlinedTextField

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
    isAssamese: Boolean,
    chapterCounts: Map<String, Int> = emptyMap()
) {
    var expanded by remember { mutableStateOf(false) }
    val isDark = isSystemInDarkTheme()
    val uniformContainerColor = MaterialTheme.colorScheme.primaryContainer
    val uniformPrimaryColor = MaterialTheme.colorScheme.primary
    val actualContainerColor = uniformContainerColor
    val actualTitleColor = MaterialTheme.colorScheme.onPrimaryContainer
    val actualSubtitleColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = actualContainerColor),
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
                    color = uniformPrimaryColor,
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
                            color = actualTitleColor
                        )
                        Surface(
                            color = uniformPrimaryColor,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "$totalQuestionsCount Qs",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                    Text(
                        text = banner.subtitleEn,
                        style = MaterialTheme.typography.bodySmall,
                        color = actualSubtitleColor
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
                    label = { Text("Select Chapters (Single/Multiple)", fontSize = 12.sp, color = actualSubtitleColor) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = if (isDark) MaterialTheme.colorScheme.surface else Color.White,
                        focusedContainerColor = if (isDark) MaterialTheme.colorScheme.surface else Color.White,
                        unfocusedBorderColor = if (isDark) MaterialTheme.colorScheme.outline.copy(alpha = 0.5f) else Color(0xFFC4C6D0),
                        focusedBorderColor = uniformPrimaryColor,
                        unfocusedLabelColor = actualSubtitleColor,
                        focusedLabelColor = uniformPrimaryColor,
                        unfocusedTextColor = actualTitleColor,
                        focusedTextColor = actualTitleColor,
                        unfocusedTrailingIconColor = actualSubtitleColor,
                        focusedTrailingIconColor = uniformPrimaryColor
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
                            val count = chapterCounts[chapter] ?: 0
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(
                                            checked = isChecked,
                                            onCheckedChange = null
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = "$chapter ($count Qs)")
                                    }
                                }
    ,
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
                            label = { Text(ch, style = MaterialTheme.typography.labelSmall, color = Color.White) },
                            colors = InputChipDefaults.inputChipColors(
                                selectedContainerColor = uniformPrimaryColor,
                                selectedLabelColor = Color.White,
                                selectedTrailingIconColor = Color.White
                            ),
                            trailingIcon = {
                                Icon(Icons.Default.Close, contentDescription = "Remove", modifier = Modifier.size(12.dp), tint = Color.White)
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
                colors = ButtonDefaults.buttonColors(containerColor = uniformPrimaryColor)
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
    val language = com.example.ui.viewmodel.AppLanguage.ENGLISH
    val questionLanguage by viewModel.questionLanguage.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val questions by viewModel.accessibleQuestions.collectAsState()
    val mockTests by viewModel.accessibleMockTests.collectAsState()
    val studyNotes by viewModel.accessibleStudyNotes.collectAsState()
    
    val bookmarkedIds by viewModel.bookmarkedIds.collectAsState()
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
            "STUDY_MCQS" -> null
            "POMODORO" -> "Stay focused with timed study sessions"
            "CURRENT_AFFAIRS" -> "Daily updated news capsules & study notes"
            else -> "Choose a module to start learning"
        }

        val isStudySessionActive by viewModel.isStudySessionActive.collectAsState()

        if (activeStudySubView != "STUDY_MCQS") {
            com.example.ui.components.JuktiTopAppBar(
                title = titleText,
                subtitle = subtitleText,
                onBackClick = {
                    if (isStudySessionActive) {
                        viewModel.requestEndStudySession()
                    } else if (activeStudySubView != null) {
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
                        val currentAffairsLanguage by viewModel.currentAffairsLanguage.collectAsState()
                        TextButton(onClick = { 
                            viewModel.toggleCurrentAffairsLanguage() 
                        }) {
                            Icon(Icons.Default.Translate, contentDescription = "Change Language", modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(if (currentAffairsLanguage == AppLanguage.ENGLISH) "EN" else "অসমীয়া", fontWeight = FontWeight.Bold)
                        }
                    }

                    if (activeStudySubView != "CURRENT_AFFAIRS") {
                        Surface(
                            color = androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer,
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
                                    tint = androidx.compose.material3.MaterialTheme.colorScheme.primary
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
        }

        // Body Content based on Active Sub-view
        when (activeStudySubView) {
            "STUDY_MCQS" -> {
                StudyMcqInteractiveTab(
                    viewModel = viewModel,
                    onBack = {
                        if (isStudySessionActive) {
                            viewModel.requestEndStudySession()
                        } else if (activeStudySubView != null) {
                            if (openedStudyDirectly) {
                                viewModel.setStudySubView(null)
                                viewModel.navigateTo(Screen.HOME)
                            } else {
                                viewModel.setStudySubView(null)
                            }
                        } else {
                            viewModel.navigateTo(Screen.HOME)
                        }
                    }
                )
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
                var showSavedQuestionsOptionsDialog by remember { mutableStateOf(false) }
                var showSavedQuestionsDialog by remember { mutableStateOf(false) }
                
                val accessibleCounts by viewModel.accessibleContentCounts.collectAsState()
                val availableQuestions = accessibleCounts.questionsCount
                val solvedQuestions = userProfile?.totalSolved ?: 0
                val availableNotes = accessibleCounts.studyNotesCount
                val availableCurrentAffairs = accessibleCounts.currentAffairsCount
                
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

                    // 1. Learn (Questions / MCQs)
                    if (searchQuery.isBlank() || "Learn".contains(searchQuery, ignoreCase = true) || "Master chapter-wise questions".contains(searchQuery, ignoreCase = true)) {
                        StudyFeatureCard(
                            title = "Learn",
                            description = "Master chapter-wise questions.",
                            actionText = "Start Learning",
                            icon = Icons.Default.AutoStories,
                            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer,
                            iconTintColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                            onClick = { viewModel.openStudyMcq(fromHome = false) },
                            progressText = "$solvedQuestions/$availableQuestions completed",
                            badgeText = "🔥 Popular"
                        )
                    }

                    // 2. Practice (Questions / MCQs)
                    if (searchQuery.isBlank() || "Practice".contains(searchQuery, ignoreCase = true) || "Test yourself with instant explanations".contains(searchQuery, ignoreCase = true)) {
                        StudyFeatureCard(
                            title = "Practice",
                            description = "Test yourself with instant explanations.",
                            actionText = "Practice Now",
                            icon = Icons.Default.Quiz,
                            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.secondaryContainer,
                            iconTintColor = androidx.compose.material3.MaterialTheme.colorScheme.secondary,
                            onClick = { viewModel.navigateTo(Screen.PRACTICE) },
                            progressText = "$solvedQuestions/$availableQuestions completed",
                            badgeText = "⭐ Recommended"
                        )
                    }
                    
                    // 3. Smart Practice
                    if (searchQuery.isBlank() || "Smart Practice".contains(searchQuery, ignoreCase = true) || "Practice saved and frequently incorrect questions".contains(searchQuery, ignoreCase = true)) {
                        StudyFeatureCard(
                            title = "Smart Practice",
                            description = "Practice saved and frequently incorrect questions.",
                            actionText = "Start Smart Practice",
                            icon = Icons.Default.Psychology,
                            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.tertiaryContainer,
                            iconTintColor = androidx.compose.material3.MaterialTheme.colorScheme.tertiary,
                            onClick = { viewModel.navigateTo(Screen.SMART_PRACTICE) },
                            badgeText = "🧠 Smart"
                        )
                    }

                    // 5. Saved Questions
                    if (searchQuery.isBlank() || "Saved Questions".contains(searchQuery, ignoreCase = true) || "Review and manage your bookmarked MCQs".contains(searchQuery, ignoreCase = true)) {
                        StudyFeatureCard(
                            title = "Saved Questions",
                            description = "Review and manage your bookmarked MCQs.",
                            actionText = "Review Saved",
                            icon = Icons.Default.Bookmark,
                            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer,
                            iconTintColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                            onClick = { showSavedQuestionsOptionsDialog = true },
                            progressText = "${bookmarkedQuestions.size} questions saved",
                            badgeText = "📌 Saved"
                        )
                    }

                    // 6. Study Notes
                    if (searchQuery.isBlank() || "Study Notes".contains(searchQuery, ignoreCase = true) || "Read chapter-wise notes".contains(searchQuery, ignoreCase = true)) {
                        StudyFeatureCard(
                            title = "Study Notes",
                            description = "Read chapter-wise notes.",
                            actionText = "Read Notes",
                            icon = Icons.Default.Description,
                            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer,
                            iconTintColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                            onClick = { viewModel.navigateTo(Screen.STUDY_NOTES) },
                            progressText = "$availableNotes study notes available",
                            badgeText = "🆕 New"
                        )
                    }

                    // 7. Current Affairs
                    if (searchQuery.isBlank() || "Current Affairs".contains(searchQuery, ignoreCase = true) || "Stay updated with daily current affairs notes".contains(searchQuery, ignoreCase = true)) {
                        StudyFeatureCard(
                            title = "Current Affairs",
                            description = "Stay updated with daily current affairs notes & news capsules.",
                            actionText = "Read Notes",
                            icon = Icons.Default.Newspaper,
                            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.tertiaryContainer,
                            iconTintColor = androidx.compose.material3.MaterialTheme.colorScheme.tertiary,
                            onClick = { viewModel.setStudySubView("CURRENT_AFFAIRS", fromHome = false) },
                            progressText = "$availableCurrentAffairs updates available",
                            badgeText = "🆕 New"
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }

                if (showSavedQuestionsOptionsDialog) {
                    val context = androidx.compose.ui.platform.LocalContext.current
                    SavedQuestionsOptionDialog(
                        savedCount = bookmarkedQuestions.size,
                        onDismiss = { showSavedQuestionsOptionsDialog = false },
                        onViewQuestions = {
                            showSavedQuestionsOptionsDialog = false
                            showSavedQuestionsDialog = true
                        },
                        onPracticeQuestions = {
                            showSavedQuestionsOptionsDialog = false
                            if (bookmarkedQuestions.isEmpty()) {
                                android.widget.Toast.makeText(context, "No saved questions to practice.", android.widget.Toast.LENGTH_SHORT).show()
                            } else {
                                viewModel.navigateTo(Screen.SAVED_PRACTICE)
                            }
                        }
                    )
                }

                if (showSavedQuestionsDialog) {
                    val isPremium by viewModel.isUserPremium.collectAsState()
                    val isAdmin by viewModel.isAdminOrOwner.collectAsState()
                    val context = androidx.compose.ui.platform.LocalContext.current
                    SavedQuestionsDialog(
                        questions = bookmarkedQuestions,
                        language = language,
                        isUserPremium = isPremium,
                        isAdminOrOwner = isAdmin,
                        onDismiss = { showSavedQuestionsDialog = false },
                        onToggleBookmark = { q -> viewModel.toggleBookmarkQuestion(q) },
                        onPracticeQuestion = { q ->
                            showSavedQuestionsDialog = false
                            viewModel.startPracticeForQuestion(q)
                            viewModel.setReturnToSavedQuestions(true)
                        },
                        onPracticeAll = {
                            showSavedQuestionsDialog = false
                            if (bookmarkedQuestions.isEmpty()) {
                                android.widget.Toast.makeText(context, "No saved questions to practice.", android.widget.Toast.LENGTH_SHORT).show()
                            } else {
                                viewModel.navigateTo(Screen.SAVED_PRACTICE)
                            }
                        }
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
fun StudyMcqInteractiveTab(
    viewModel: JuktiViewModel,
    onBack: () -> Unit = {}
) {
    val language = com.example.ui.viewmodel.AppLanguage.ENGLISH
    val questionLanguage by viewModel.questionLanguage.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val isUserPremium by viewModel.isUserPremium.collectAsState()
    val isAdminOrOwner by viewModel.isAdminOrOwner.collectAsState()
    val questions by viewModel.accessibleQuestions.collectAsState()
    val allSubjectsChapters by viewModel.allSubjectsChapters.collectAsState()
    val bookmarkedIds by viewModel.bookmarkedIds.collectAsState()
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
            viewModel.recordQuestionStudied(questionId, 10)

        }
    }

    var sessionTotalSeconds by remember { mutableStateOf(0) }

    var showReportDialog by remember { mutableStateOf(false) }
    var showSummaryDialog by remember { mutableStateOf(false) }
    var showEndLearningConfirmDialog by remember { mutableStateOf(false) }
    var finalSessionQuestions by remember { mutableStateOf(0) }
    var finalSessionTime by remember { mutableStateOf(0) }

    // System Back Button Intercept for Active Study Session
    BackHandler(enabled = isStudySessionStarted) {
        showEndLearningConfirmDialog = true
    }

    com.example.ui.components.StudyTimeTracker(
        isActive = isStudySessionStarted,
        onTimeAccumulated = { seconds ->
            viewModel.recordStudySession("LEARNING", seconds)
        }
    )

    val studySessionResetTrigger by viewModel.studySessionResetTrigger.collectAsState()
    LaunchedEffect(studySessionResetTrigger) {
        if (studySessionResetTrigger > 0) {
            isStudySessionStarted = false
            showEndLearningConfirmDialog = false
            showSummaryDialog = false
        }
    }

    val requestEndTrigger by viewModel.requestEndStudySessionTrigger.collectAsState()
    LaunchedEffect(requestEndTrigger) {
        if (requestEndTrigger > 0 && isStudySessionStarted) {
            showEndLearningConfirmDialog = true
        }
    }

    // Live Timer Coroutine & Bottom Bar State Sync per Session
    LaunchedEffect(isStudySessionStarted) {
        viewModel.setStudySessionActive(isStudySessionStarted)
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

    DisposableEffect(Unit) {
        onDispose {
            viewModel.setStudySessionActive(false)
        }
    }

    var activeStudySessionQuestions by remember { mutableStateOf<List<QuestionEntity>>(emptyList()) }
    var lastStudyStartingQuestionId by rememberSaveable { mutableLongStateOf(-1L) }

    // Asynchronously derive visible questions from questions state flow
    val visibleQuestions by produceState(
        initialValue = emptyList<QuestionEntity>(),
        questions
    ) {
        value = withContext(Dispatchers.Default) {
            questions
        }
    }

    // Filter questions by selected subject and chapters asynchronously on Dispatchers.Default
    val filteredStudyQuestions by produceState(
        initialValue = emptyList<QuestionEntity>(),
        visibleQuestions, selectedSubjectTab, selectedChapters
    ) {
        value = withContext(Dispatchers.Default) {
            val isAll = selectedSubjectTab.equals("All Subject", ignoreCase = true) || selectedSubjectTab.equals("All Subjects", ignoreCase = true) || selectedSubjectTab.isBlank()
            visibleQuestions.filter { q ->
                try {
                    val matchSubject = if (isAll) true else isQuestionInSubject(q, selectedSubjectTab)
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

    LaunchedEffect(isStudySessionStarted, selectedSubjectTab, selectedChapters, filteredStudyQuestions, visibleQuestions) {
        if (isStudySessionStarted) {
            val isAll = selectedSubjectTab.equals("All Subject", ignoreCase = true) || selectedSubjectTab.equals("All Subjects", ignoreCase = true) || selectedSubjectTab.isBlank()
            val subjectFiltered = visibleQuestions.filter { q ->
                val matchSubj = if (isAll) true else isQuestionInSubject(q, selectedSubjectTab)
                if (selectedChapters.isEmpty()) {
                    matchSubj
                } else {
                    val topicStr = q.topic ?: ""
                    val qSubject = q.subject ?: ""
                    val normTopic = com.example.data.repository.normalizeChapterName(topicStr, qSubject)

                    val matchCh = selectedChapters.any { rawCh ->
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
                    matchSubj && matchCh
                }
            }
            val scopeKey = com.example.data.repository.SessionDeckManager.buildScopeKey(selectedSubjectTab, selectedChapters)
            val deckResult = viewModel.getOrUpdateSessionDeck("LEARN", scopeKey, subjectFiltered)
            activeStudySessionQuestions = deckResult.orderedQuestions
            currentQuestionIndex = deckResult.currentIndex
        } else {
            activeStudySessionQuestions = emptyList()
        }
    }

    LaunchedEffect(isStudySessionStarted, currentQuestionIndex, activeStudySessionQuestions) {
        if (isStudySessionStarted && activeStudySessionQuestions.isNotEmpty()) {
            val scopeKey = com.example.data.repository.SessionDeckManager.buildScopeKey(selectedSubjectTab, selectedChapters)
            viewModel.saveSessionIndex("LEARN", scopeKey, currentQuestionIndex, activeStudySessionQuestions.size)
        }
    }

    val displayQuestions = when {
        isStudySessionStarted -> activeStudySessionQuestions
        filteredStudyQuestions.isNotEmpty() -> filteredStudyQuestions
        selectedSubjectTab.equals("All Subject", ignoreCase = true) || selectedSubjectTab.equals("All Subjects", ignoreCase = true) || selectedSubjectTab.isBlank() -> visibleQuestions
        else -> visibleQuestions.filter { isQuestionInSubject(it, selectedSubjectTab) }
    }

    val currentQuestion = displayQuestions.getOrNull(currentQuestionIndex)

    val predefinedStudy = listOf(
        StudyBannerConfig("General Knowledge", "সাধাৰণ জ্ঞান", "Assam history, geography, and more", "অসমৰ ইতিহাস, ভূগোল আৰু অন্যান্য", "General Knowledge", androidx.compose.material.icons.Icons.Default.Public, androidx.compose.ui.graphics.Color(0xFFE8F5E9), androidx.compose.ui.graphics.Color(0xFF2E7D32)),
        StudyBannerConfig("General English", "সাধাৰণ ইংৰাজী", "Grammar, vocabulary, and comprehension", "ব্যাকৰণ, শব্দভাণ্ডাৰ আৰু বুজাপৰা", "General English", androidx.compose.material.icons.Icons.Default.MenuBook, androidx.compose.ui.graphics.Color(0xFFE3F2FD), androidx.compose.ui.graphics.Color(0xFF1565C0)),
        StudyBannerConfig("General Mathematics", "সাধাৰণ গণিত", "Arithmetic, algebra, and geometry", "পাটিগণিত, বীজগণিত আৰু জ্যামিতি", "General Mathematics", androidx.compose.material.icons.Icons.Default.Calculate, androidx.compose.ui.graphics.Color(0xFFFFF3E0), androidx.compose.ui.graphics.Color(0xFFEF6C00)),
        StudyBannerConfig("Reasoning & Mental Ability", "যুক্তিবিদ্যা (Reasoning)", "Logical and analytical reasoning", "যৌক্তিক আৰু বিশ্লেষণাত্মক যুক্তি", "Reasoning & Mental Ability", androidx.compose.material.icons.Icons.Default.Psychology, androidx.compose.ui.graphics.Color(0xFFF3E5F5), androidx.compose.ui.graphics.Color(0xFF6A1B9A)),
        StudyBannerConfig("Transport & Motor Vehicle", "পৰিবহন আৰু মটৰ বাহন", "Motor vehicle act and traffic signs", "মটৰ বাহন আইন আৰু যান-বাহনৰ সংকেত", "Transport & Motor Vehicle", androidx.compose.material.icons.Icons.Default.Traffic, androidx.compose.ui.graphics.Color(0xFFFBE9E7), androidx.compose.ui.graphics.Color(0xFFD84315))
    )

    val existingStudyKeys = predefinedStudy.map { it.subjectKey.lowercase() }.toMutableSet()
    existingStudyKeys.add("all subjects")

    val dynamicStudySubjects = mutableSetOf<String>()
    allSubjectsChapters.forEach { if (it.subject.isNotBlank()) dynamicStudySubjects.add(it.subject) }
    questions.forEach { if (it.subject.isNotBlank()) dynamicStudySubjects.add(it.subject) }

    val dynamicStudyBanners = dynamicStudySubjects
        .filter { subj ->
            val norm = subj.lowercase()
            !existingStudyKeys.contains(norm) &&
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
            StudyBannerConfig(
                titleEn = subj,
                titleAs = subj,
                subtitleEn = "Study notes and questions for $subj",
                subtitleAs = "$subj ৰ অধ্যয়ন নোট আৰু প্ৰশ্ন",
                subjectKey = subj,
                icon = androidx.compose.material.icons.Icons.Default.Book,
                containerColor = androidx.compose.ui.graphics.Color(0xFFEFEBE9),
                iconColor = androidx.compose.ui.graphics.Color(0xFF4E342E)
            )
        }

    val studyBanners = predefinedStudy + dynamicStudyBanners + listOf(
        StudyBannerConfig("All Subjects", "সকলো বিষয়", "Mixed questions from all subjects", "সকলো বিষয়ৰ পৰা মিশ্ৰিত প্ৰশ্ন", "All Subjects", androidx.compose.material.icons.Icons.Default.AllInclusive, androidx.compose.ui.graphics.Color(0xFFFFF8E1), androidx.compose.ui.graphics.Color(0xFFFF8F00))
    )

    if (!isStudySessionStarted) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("top_bar_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = "Learn MCQs",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            androidx.compose.foundation.lazy.LazyColumn(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            items(studyBanners) { banner ->
                val bannerQuestions = remember(questions, banner.subjectKey) {
                    questions.filter { q -> isQuestionInSubject(q, banner.subjectKey) }
                }
                val totalCount = bannerQuestions.size
                
                val availableChapters = remember(banner.subjectKey, allSubjectsChapters, questions) {
                    if (banner.subjectKey == "All Subjects" || banner.subjectKey == "All Subject") {
                        val set = mutableSetOf<String>()
                        allSubjectsChapters.forEach { sc ->
                            if (sc.chapter.isNotBlank()) {
                                set.add("${sc.subject}: ${sc.chapter}")
                            }
                        }
                        bannerQuestions.forEach { q ->
                            if (!q.topic.isNullOrBlank() && !q.subject.isNullOrBlank()) {
                                set.add("${q.subject}: ${q.topic}")
                            }
                        }
                        set.toList().sorted()
                    } else {
                        val set = mutableSetOf<String>()
                        val filteredFromDb = allSubjectsChapters.filter { isQuestionSubjectMatch(it.subject, banner.subjectKey) }
                        filteredFromDb.forEach { if (it.chapter.isNotBlank()) set.add(it.chapter) }
                        bannerQuestions.forEach { q ->
                            if (!q.topic.isNullOrBlank()) set.add(q.topic)
                        }
                        set.toList().sorted()
                    }
                }
                
                val currentSelectedChapters = chaptersMap[banner.subjectKey] ?: emptySet()

                val chapterCounts = remember(bannerQuestions, availableChapters) {
                    availableChapters.associateWith { rawCh ->
                        val selSubj = if (rawCh.contains(": ")) rawCh.substringBefore(": ").trim() else ""
                        val ch = if (rawCh.contains(": ")) rawCh.substringAfter(": ").trim() else rawCh.trim()

                        bannerQuestions.count { q ->
                            val qSubj = q.subject ?: ""
                            val topicStr = q.topic ?: ""
                            val normTopic = com.example.data.repository.normalizeChapterName(topicStr, qSubj)

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
                }

                val displayedCount = if (currentSelectedChapters.isEmpty()) {
                    bannerQuestions.size
                } else {
                    currentSelectedChapters.sumOf { ch -> chapterCounts[ch] ?: 0 }
                }
                
                StudySubjectBannerCard(
                    banner = banner,
                    availableChapters = availableChapters,
                    selectedChapters = currentSelectedChapters,
                    onChaptersChanged = { newSet ->
                        chaptersMap = chaptersMap + (banner.subjectKey to newSet)
                    },
                    totalQuestionsCount = displayedCount,
                    actionButtonTextEn = "Start Study",
                    actionButtonTextAs = "অধ্যয়ন আৰম্ভ কৰক",
                    onStartClick = {
                        selectedSubjectTab = banner.subjectKey
                        selectedChapters = chaptersMap[banner.subjectKey] ?: emptySet()
                        currentQuestionIndex = 0
                        selectedOptionIndex = null
                        isStudySessionStarted = true
                    },
                    isAssamese = isAssamese,
                    chapterCounts = chapterCounts
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            val subjectTitle = when (selectedSubjectTab) {
                "General Knowledge" -> "General Knowledge"
                "General English" -> "General English"
                "General Mathematics" -> "Mathematics"
                "Reasoning", "Reasoning & Mental Ability" -> "Reasoning"
                "Reading Comprehension" -> "Reading Comprehension"
                "Basic Computer", "Computer Knowledge", "Computer" -> "Basic Computer"
                "Transport & Motor Vehicle", "Transport Rule", "Transport Rules" -> "Transport & Motor Vehicle"
                else -> if (selectedSubjectTab.isNotBlank()) selectedSubjectTab else "All Subjects"
            }

            // Compact Top Bar with Back Button + Subject Name + Question Info + Language Selector + Slim Progress
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 4.dp, end = 10.dp, top = 4.dp, bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { showEndLearningConfirmDialog = true },
                            modifier = Modifier.testTag("top_bar_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 4.dp)
                        ) {
                            Text(
                                text = subjectTitle,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (displayQuestions.isNotEmpty()) {
                                val pct = ((currentQuestionIndex + 1) * 100) / displayQuestions.size
                                Text(
                                    text = "Question ${currentQuestionIndex + 1} of ${displayQuestions.size} · $pct%",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Compact Language Selection Chips
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FilterChip(
                                selected = questionLanguage == AppLanguage.ENGLISH,
                                onClick = { viewModel.setQuestionLanguage(AppLanguage.ENGLISH) },
                                label = { Text("EN", fontSize = 11.sp, fontWeight = if (questionLanguage == AppLanguage.ENGLISH) FontWeight.Bold else FontWeight.Normal) },
                                modifier = Modifier.height(28.dp)
                            )
                            FilterChip(
                                selected = questionLanguage == AppLanguage.ASSAMESE,
                                onClick = { viewModel.setQuestionLanguage(AppLanguage.ASSAMESE) },
                                label = { Text("অসমীয়া", fontSize = 11.sp, fontWeight = if (questionLanguage == AppLanguage.ASSAMESE) FontWeight.Bold else FontWeight.Normal) },
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

                    // Slim 3dp Progress Bar
                    LinearProgressIndicator(
                        progress = { (currentQuestionIndex + 1).toFloat() / displayQuestions.size.coerceAtLeast(1) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
                    )
                }
            }

            // Scrollable Content Area - Space of page utilized properly
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
            // 2. QUESTION CARD VIEW
            if (currentQuestion == null) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        com.example.ui.components.EmptyStateIllustration(
                            type = com.example.ui.components.EmptyStateType.RHINO_BOOK,
                            title = "No Questions Found",
                            message = "No questions found for selected chapter or subject.",
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = {
                            isStudySessionStarted = false
                            selectedSubjectTab = "All Subject"
                            selectedChapters = emptySet()
                        }) {
                            Text("Back to Subjects")
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
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Question Header Metadata Row: Topic/Type Badges + Direct Bookmark & Report Icons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f, fill = false)
                            ) {
                                if (currentQuestion.topic.isNotBlank()) {
                                    val cleanTopic = if (currentQuestion.topic.contains(": ")) currentQuestion.topic.substringAfter(": ").trim() else currentQuestion.topic.trim()
                                    Surface(
                                        color = MaterialTheme.colorScheme.secondaryContainer,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = cleanTopic,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                                com.example.ui.components.QuestionTypeBadge(
                                    questionType = currentQuestion.questionType
                                )
                            }

                            // Directly Accessible Action Buttons (Bookmark & Report)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                IconButton(
                                    onClick = { viewModel.toggleBookmarkQuestion(currentQuestion) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = if (currentQuestion.id in bookmarkedIds) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                        contentDescription = "Bookmark Question",
                                        tint = if (currentQuestion.id in bookmarkedIds) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                IconButton(
                                    onClick = { showReportDialog = true },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Flag,
                                        contentDescription = "Report Question",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Question Text (Visually Dominant)
                        if (!viewModel.canAccessQuestion(currentQuestion)) {
                            Surface(
                                color = MaterialTheme.colorScheme.errorContainer,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(Icons.Default.Lock, contentDescription = "Premium Content", modifier = Modifier.size(44.dp), tint = MaterialTheme.colorScheme.error)
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text("Premium Question", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("This question is only available to Premium users.", textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onErrorContainer)
                                    Spacer(modifier = Modifier.height(14.dp))
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
                                style = MaterialTheme.typography.titleMedium.copy(lineHeight = 24.sp),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Options List (Pre-marked Correct Answer in Learn Mode)
                            val options = listOf(
                                currentQuestion.optionAEn to currentQuestion.optionAAs,
                                currentQuestion.optionBEn to currentQuestion.optionBAs,
                                currentQuestion.optionCEn to currentQuestion.optionCAs,
                                currentQuestion.optionDEn to currentQuestion.optionDAs
                            )

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                options.forEachIndexed { optIndex, pair ->
                                    val isCorrect = (optIndex == currentQuestion.correctOptionIndex)
                                    val optionLetter = ('A' + optIndex).toString()

                                    val backgroundColor = if (isCorrect) {
                                        MaterialTheme.colorScheme.successContainer.copy(alpha = 0.45f)
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                    }

                                    val borderColor = if (isCorrect) {
                                        MaterialTheme.colorScheme.success.copy(alpha = 0.8f)
                                    } else {
                                        MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                                    }

                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        color = backgroundColor,
                                        border = BorderStroke(1.5.dp, borderColor)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Surface(
                                                shape = CircleShape,
                                                color = if (isCorrect) MaterialTheme.colorScheme.success else MaterialTheme.colorScheme.surfaceVariant,
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    if (isCorrect) {
                                                        Icon(
                                                            Icons.Default.Check,
                                                            contentDescription = "Correct Option",
                                                            tint = Color.White,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                    } else {
                                                        Text(
                                                            text = optionLetter,
                                                            style = MaterialTheme.typography.labelMedium,
                                                            fontWeight = FontWeight.Bold,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
                                                color = if (isCorrect) MaterialTheme.colorScheme.onSuccessContainer else MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Explanation Card (IMMEDIATELY VISIBLE in Learn Mode)
                            if (currentQuestion.explanationEn.isNotBlank() || currentQuestion.explanationAs.isNotBlank()) {
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth(),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "💡 Explanation",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        BilingualText(
                                            textEn = currentQuestion.explanationEn,
                                            textAs = currentQuestion.explanationAs,
                                            language = questionLanguage,
                                            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                            }

                            // Previous / Next Navigation Controls
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
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f).padding(end = 6.dp)
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Question", modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Previous")
                                }

                                val isLastQuestion = (currentQuestionIndex == displayQuestions.size - 1)
                                Button(
                                    onClick = {
                                        markQuestionLearned(currentQuestion.id)
                                        if (!isLastQuestion) {
                                            currentQuestionIndex++
                                        } else {
                                            finalSessionTime = sessionTotalSeconds
                                            finalSessionQuestions = learnedQuestionIds.value.size
                                            isStudySessionStarted = false
                                            showSummaryDialog = true
                                        }
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f).padding(start = 6.dp)
                                ) {
                                    Text(if (isLastQuestion) "Finish Learning" else "Next")
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = if (isLastQuestion) Icons.Default.CheckCircle else Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Subtle End Learning text button
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                TextButton(
                                    onClick = { showEndLearningConfirmDialog = true },
                                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                ) {
                                    Text(
                                        text = "End Learning",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.SemiBold,
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
    }

    // Learning Summary Dialog
    if (showSummaryDialog) {
        AlertDialog(
            onDismissRequest = { 
                showSummaryDialog = false
                isStudySessionStarted = false
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = { Text("Learning Session Summary 📚", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Great job! Here is what you achieved in this study session:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Questions Studied:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                Text("$finalSessionQuestions", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = androidx.compose.material3.MaterialTheme.colorScheme.primary)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("XP Earned:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                Text("+${finalSessionQuestions * 5} XP", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = androidx.compose.material3.MaterialTheme.colorScheme.primary)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Time Spent:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                val m = finalSessionTime / 60
                                val s = finalSessionTime % 60
                                val timeStr = if (m > 0) "${m}m ${s}s" else "${s}s"
                                Text(timeStr, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            }
                            if (finalSessionQuestions > 0 && finalSessionTime > 0) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Avg Time / Question:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                    Text("${(finalSessionTime.toFloat() / finalSessionQuestions).toInt()}s", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
    
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { 
                        showSummaryDialog = false
                        isStudySessionStarted = false
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Done")
                }
            }
        )
    }




        
    // End Learning Confirmation Dialog
    if (showEndLearningConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showEndLearningConfirmDialog = false },
            title = { Text("End Learning?", fontWeight = FontWeight.Bold) },
            text = { Text("Your learning progress will be saved.") },
            confirmButton = {
                Button(
                    onClick = { showEndLearningConfirmDialog = false }
                ) {
                    Text("Continue Learning")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showEndLearningConfirmDialog = false
                        markQuestionLearned(currentQuestion?.id ?: 0L)
                        finalSessionTime = sessionTotalSeconds
                        finalSessionQuestions = learnedQuestionIds.value.size
                        isStudySessionStarted = false
                        showSummaryDialog = true
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(
                        text = "End Learning",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
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
/* =====================================================================
   TAB 2: PRACTICE MCQ MODULE
   ===================================================================== */
@Composable
fun PracticeMcqTab(viewModel: JuktiViewModel) {
    val language = com.example.ui.viewmodel.AppLanguage.ENGLISH
    val questionLanguage by viewModel.questionLanguage.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val questions by viewModel.accessibleQuestions.collectAsState()
    val bookmarkedIds by viewModel.bookmarkedIds.collectAsState()
    val isAssamese = language == AppLanguage.ASSAMESE
    val context = androidx.compose.ui.platform.LocalContext.current

    var activePracticeMode by remember { mutableStateOf("Mixed") }
    var currentQuestionIndex by remember { mutableStateOf(0) }
    var selectedOptionIndex by remember { mutableStateOf<Int?>(null) }
    var isSubmitted by remember { mutableStateOf(false) }
    var scoreCount by remember { mutableStateOf(0) }
    var showReportDialog by remember { mutableStateOf(false) }

    val activeQuestions = remember(questions, activePracticeMode) {
        if (activePracticeMode == "Shuffle") questions.shuffled() else questions
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
                color = androidx.compose.material3.MaterialTheme.colorScheme.primary
            )

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Surface(
                    color = androidx.compose.material3.MaterialTheme.colorScheme.secondaryContainer,
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
                    color = androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer,
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
                        selectedContainerColor = androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer,
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
                            color = androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer,
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
                                    imageVector = if (currentQuestion.id in bookmarkedIds) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                    contentDescription = "Bookmark",
                                    tint = if (currentQuestion.id in bookmarkedIds) androidx.compose.material3.MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
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
                            color = if (isSubmitted) {
                                if (isCorrect) MaterialTheme.colorScheme.primaryContainer else if (isSelected) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surface
                            } else {
                                if (isSelected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface
                            },
                            border = BorderStroke(1.dp, if (isSelected || (isSubmitted && isCorrect)) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                val letter = ('A' + optIndex).toString()
                                Text(letter, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(end = 12.dp))
                                BilingualText(textEn = pair.first, textAs = pair.second, language = language)
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    if (!isSubmitted) {
                        Button(
                            onClick = {
                                if (selectedOptionIndex != null) {
                                    isSubmitted = true
                                    val isAnsCorrect = (selectedOptionIndex == currentQuestion?.correctOptionIndex)
                                    if (isAnsCorrect) {
                                        scoreCount += 10
                                    }
                                    if (currentQuestion != null) {
                                        viewModel.submitQuestionAnswer(currentQuestion.id, isAnsCorrect, 10)
                                    }
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
                            Text(if (currentQuestionIndex == activeQuestions.size - 1) "Finish Practice" else "Next Question")
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

/* =====================================================================
   TAB 3: POMODORO CLOCK MODULE
   ===================================================================== */
@Composable
fun PomodoroClockTab(viewModel: JuktiViewModel) {
    val language = com.example.ui.viewmodel.AppLanguage.ENGLISH
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
                color = if (timerMode == "Study") androidx.compose.material3.MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.success,
                trackColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = formattedTime,
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.primary
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
            colors = CardDefaults.cardColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = androidx.compose.material3.MaterialTheme.colorScheme.secondary)
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
                    color = androidx.compose.material3.MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}


@Composable
fun QuestionStudyCard(
    question: QuestionEntity,
    language: AppLanguage,
    bookmarkedIds: Set<Long>,
    isUserPremium: Boolean = false,
    isAdminOrOwner: Boolean = false,
    onUnlockClick: () -> Unit = {},
    onBookmarkToggle: () -> Unit,
    onLikeToggle: () -> Unit,
    onReportClick: () -> Unit
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
                    color = androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer,
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
                            imageVector = if (question.id in bookmarkedIds) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = if (question.id in bookmarkedIds) androidx.compose.material3.MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
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
                    color = androidx.compose.material3.MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "Correct Option: ${('A' + question.correctOptionIndex)}",
                            fontWeight = FontWeight.Bold,
                            color = androidx.compose.material3.MaterialTheme.colorScheme.primary
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

/* =====================================================================
   CURRENT AFFAIRS STUDY NOTES ONE-PAGE DATA MODULE
   ===================================================================== */
@Composable
fun CurrentAffairsNotesTab(viewModel: JuktiViewModel) {
    val currentAffairsLanguage by viewModel.currentAffairsLanguage.collectAsState()
    val bookmarkedIds by viewModel.bookmarkedIds.collectAsState()
    val allNotes by viewModel.accessibleStudyNotes.collectAsState()
    val isAssamese = currentAffairsLanguage == AppLanguage.ASSAMESE
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
                            imageVector = if (note.id in bookmarkedIds) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = if (note.id in bookmarkedIds) androidx.compose.material3.MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Surface(
                color = androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(16.dp), tint = androidx.compose.material3.MaterialTheme.colorScheme.primary)
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
                language = currentAffairsLanguage,
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
                        onClick = { viewModel.selectStudyNote(note) },
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
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Surface(
                                        color = androidx.compose.material3.MaterialTheme.colorScheme.tertiaryContainer,
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
                                    if (note.isPremium) {
                                        Surface(
                                            color = MaterialTheme.colorScheme.errorContainer,
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Star,
                                                    contentDescription = "Premium",
                                                    tint = MaterialTheme.colorScheme.error,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text(
                                                    text = "PREMIUM",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.error
                                                )
                
            
        
    
                                            }
                                        }
                                    }
                                }
                                Text(
                                    text = "${note.readTimeMinutes} min read",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                            }
                            Spacer(modifier = Modifier.height(10.dp))

                            BilingualText(
                                textEn = note.titleEn,
                                textAs = note.titleAs,
                                language = currentAffairsLanguage,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            val snippetEn = note.contentEn.take(120) + "..."
                            val snippetAs = if (note.contentAs.isNotBlank()) note.contentAs.take(120) + "..." else snippetEn
                            Text(
                                text = if (isAssamese && note.contentAs.isNotBlank()) snippetAs else snippetEn,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(onClick = { viewModel.selectStudyNote(note) }) {
                                    Text("Read Full Notes →", fontWeight = FontWeight.Bold)
    
                                }
                                IconButton(onClick = { viewModel.toggleBookmarkNote(note) }) {
                                    Icon(
                                        imageVector = if (note.id in bookmarkedIds) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                        contentDescription = "Bookmark",
                                        tint = if (note.id in bookmarkedIds) androidx.compose.material3.MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
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

fun isQuestionInSubject(q: com.example.data.local.QuestionEntity, subjectKey: String): Boolean {
    val key = subjectKey.trim()
    if (key.equals("All Subject", ignoreCase = true) || key.equals("All Subjects", ignoreCase = true) || key.isBlank()) {
        return true
    }
    val qSubj = q.subject.trim()
    val qTopic = (q.topic ?: "").trim()

    return when {
        key.equals("General Knowledge", ignoreCase = true) || key.equals("GK", ignoreCase = true) -> {
            qSubj.equals("General Knowledge", ignoreCase = true) ||
            qSubj.equals("GK", ignoreCase = true) ||
            qSubj.contains("GK", ignoreCase = true) ||
            qSubj.contains("Knowledge", ignoreCase = true) ||
            qSubj.contains("History", ignoreCase = true) ||
            qSubj.contains("Geography", ignoreCase = true) ||
            qSubj.contains("Polity", ignoreCase = true) ||
            qSubj.contains("Economy", ignoreCase = true) ||
            qSubj.contains("Culture", ignoreCase = true) ||
            qSubj.contains("Current Affairs", ignoreCase = true) ||
            qSubj.contains("General Studies", ignoreCase = true) ||
            qSubj in listOf("Assam History", "Assam Geography", "Assamese Literature & Culture", "Indian History", "Indian Polity", "Indian Economy", "Current Affairs", "General Studies", "GS", "History", "Geography", "Polity", "Economy")
        }
        key.equals("General English", ignoreCase = true) || key.equals("English", ignoreCase = true) -> {
            qSubj.equals("General English", ignoreCase = true) ||
            qSubj.equals("English", ignoreCase = true) ||
            qSubj.contains("English", ignoreCase = true) ||
            qSubj.contains("Grammar", ignoreCase = true) ||
            qSubj.contains("Vocabulary", ignoreCase = true) ||
            qSubj.contains("Comprehension", ignoreCase = true)
        }
        key.equals("General Mathematics", ignoreCase = true) || key.equals("Mathematics", ignoreCase = true) || key.equals("Maths", ignoreCase = true) || key.equals("Quantitative Aptitude", ignoreCase = true) -> {
            qSubj in listOf("General Mathematics", "Mathematics", "Maths", "Quantitative Aptitude", "Arithmetic", "Elementary Mathematics") ||
            qSubj.contains("Math", ignoreCase = true) ||
            qSubj.contains("Aptitude", ignoreCase = true) ||
            qSubj.contains("Arithmetic", ignoreCase = true)
        }
        key.equals("Reasoning", ignoreCase = true) || key.equals("Reasoning & Mental Ability", ignoreCase = true) || key.equals("Logical Reasoning", ignoreCase = true) || key.equals("Mental Ability", ignoreCase = true) -> {
            qSubj in listOf("Reasoning", "Logical Reasoning", "Logical Reasoning & Mental Ability", "Mental Ability", "Logical Aptitude", "Reasoning & Mental Ability", "Analytical Reasoning") ||
            qSubj.contains("Reasoning", ignoreCase = true) ||
            qSubj.contains("Mental Ability", ignoreCase = true)
        }
        key.equals("Basic Computer", ignoreCase = true) || key.equals("Computer Knowledge", ignoreCase = true) || key.equals("Computer", ignoreCase = true) || key.equals("Computer Awareness", ignoreCase = true) -> {
            qSubj in listOf("Basic Computer", "Computer Knowledge", "Computer", "Computer Awareness", "Computer Science", "Information Technology", "IT") ||
            qSubj.contains("Computer", ignoreCase = true) ||
            qSubj.contains("Information Technology", ignoreCase = true) ||
            qTopic.contains("Computer", ignoreCase = true) ||
            qTopic.contains("MS Office", ignoreCase = true) ||
            qTopic.contains("Operating System", ignoreCase = true) ||
            qTopic.contains("Internet", ignoreCase = true) ||
            qTopic.contains("Hardware", ignoreCase = true)
        }
        key.equals("Transport & Motor Vehicle", ignoreCase = true) || key.equals("Transport Rule", ignoreCase = true) || key.equals("Transport Rules", ignoreCase = true) -> {
            qSubj.equals("Transport & Motor Vehicle", ignoreCase = true) ||
            qSubj.equals("Transport Rule", ignoreCase = true) ||
            qSubj.equals("Transport Rules", ignoreCase = true) ||
            qSubj.contains("Transport", ignoreCase = true) ||
            qSubj.contains("Motor Vehicle", ignoreCase = true) ||
            qTopic.contains("Transport Rule", ignoreCase = true) ||
            qTopic.contains("Traffic Sign", ignoreCase = true) ||
            qTopic.contains("Motor Vehicle", ignoreCase = true) ||
            qTopic.contains("Driving Regulation", ignoreCase = true) ||
            qTopic.contains("Vehicle Safety", ignoreCase = true)
        }
        else -> {
            if (qSubj.isBlank()) {
                false
            } else {
                qSubj.equals(key, ignoreCase = true) ||
                qSubj.contains(key, ignoreCase = true) ||
                (key.length >= 3 && key.contains(qSubj, ignoreCase = true))
            }
        }
    }
}

fun isQuestionSubjectMatch(qSubject: String, subjectKey: String): Boolean {
    val dummy = com.example.data.local.QuestionEntity(
        subject = qSubject, topic = "", difficulty = "", questionEn = "", questionAs = "",
        optionAEn = "", optionBEn = "", optionCEn = "", optionDEn = "", optionAAs = "",
        optionBAs = "", optionCAs = "", optionDAs = "", correctOptionIndex = 0,
        explanationEn = "", explanationAs = ""
    )
    return isQuestionInSubject(dummy, subjectKey)
}
