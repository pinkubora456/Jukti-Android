package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.local.*
import com.example.data.util.GuidanceEngine
import com.example.ui.components.SafeOutlinedTextField
import com.example.ui.viewmodel.JuktiViewModel
import com.example.ui.viewmodel.Screen
import java.util.Locale

enum class ManageGuidanceSection {
    LANDING,
    PYQ_FOCUS,
    PRIORITY_TOPICS,
    STRENGTH_WEAKNESS,
    PREP_STRATEGY
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageGuidanceScreen(viewModel: JuktiViewModel, onBackClick: () -> Unit = {}) {
    var currentSection by rememberSaveable { mutableStateOf(ManageGuidanceSection.LANDING) }
    val context = LocalContext.current

    val examsList by viewModel.examsList.collectAsState()
    val allSubjectsChapters by viewModel.allSubjectsChapters.collectAsState()
    val allQuestions by viewModel.questions.collectAsState()
    val allPyqFocus by viewModel.allPyqFocus.collectAsState(initial = emptyList())
    val allFocusTopics by viewModel.allFocusTopics.collectAsState(initial = emptyList())
    val allPrepStrategies by viewModel.allPrepStrategies.collectAsState(initial = emptyList())

    // Independent Exam & Subject states for each of the 4 sections
    var pyqExam by rememberSaveable { mutableStateOf<String?>(null) }
    var pyqSubject by rememberSaveable { mutableStateOf("All Subjects") }

    var priorityExam by rememberSaveable { mutableStateOf<String?>(null) }
    var prioritySubject by rememberSaveable { mutableStateOf("All Subjects") }

    var strengthExam by rememberSaveable { mutableStateOf<String?>(null) }
    var strengthSubject by rememberSaveable { mutableStateOf("All Subjects") }

    var strategyExam by rememberSaveable { mutableStateOf<String?>(null) }
    var strategySubject by rememberSaveable { mutableStateOf("All Subjects") }

    // Initialize default exams once exams list is loaded
    LaunchedEffect(examsList) {
        if (examsList.isNotEmpty()) {
            val firstExam = examsList.first().title
            if (pyqExam == null) pyqExam = firstExam
            if (priorityExam == null) priorityExam = firstExam
            if (strengthExam == null) strengthExam = firstExam
            if (strategyExam == null) strategyExam = firstExam
        }
    }

    val screenTitle = when (currentSection) {
        ManageGuidanceSection.LANDING -> "Manage Guidance"
        ManageGuidanceSection.PYQ_FOCUS -> "Manage PYQ Focus"
        ManageGuidanceSection.PRIORITY_TOPICS -> "Manage Priority Topics"
        ManageGuidanceSection.STRENGTH_WEAKNESS -> "Strength & Weakness Configuration"
        ManageGuidanceSection.PREP_STRATEGY -> "Manage Preparation Strategy"
    }

    Scaffold(
        topBar = {
            com.example.ui.components.JuktiTopAppBar(
                title = screenTitle,
                onBackClick = {
                    if (currentSection != ManageGuidanceSection.LANDING) {
                        currentSection = ManageGuidanceSection.LANDING
                    } else {
                        viewModel.navigateTo(Screen.WORKSPACE)
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (currentSection) {
                ManageGuidanceSection.LANDING -> {
                    ManageGuidanceLanding(
                        onNavigate = { currentSection = it },
                        onSyncCloud = {
                            viewModel.migrateGuidanceToFirestore()
                            Toast.makeText(context, "Published Guidance data to Firestore", Toast.LENGTH_SHORT).show()
                        },
                        onRefreshCloud = {
                            viewModel.refreshGuidanceData()
                            Toast.makeText(context, "Refreshed Guidance data from Cloud", Toast.LENGTH_SHORT).show()
                        }
                    )
                }

                ManageGuidanceSection.PYQ_FOCUS -> {
                    ManagePyqFocusScreen(
                        selectedExam = pyqExam,
                        selectedSubject = pyqSubject,
                        examsList = examsList,
                        allSubjectsChapters = allSubjectsChapters,
                        allQuestions = allQuestions,
                        allPyqFocus = allPyqFocus,
                        onExamChange = { pyqExam = it; pyqSubject = "All Subjects" },
                        onSubjectChange = { pyqSubject = it },
                        onSavePyq = { viewModel.savePyqFocus(it) },
                        onDeletePyq = { viewModel.deletePyqFocus(it) }
                    )
                }

                ManageGuidanceSection.PRIORITY_TOPICS -> {
                    ManagePriorityTopicsScreen(
                        selectedExam = priorityExam,
                        selectedSubject = prioritySubject,
                        examsList = examsList,
                        allSubjectsChapters = allSubjectsChapters,
                        allPyqFocus = allPyqFocus,
                        allFocusTopics = allFocusTopics,
                        onExamChange = { priorityExam = it; prioritySubject = "All Subjects" },
                        onSubjectChange = { prioritySubject = it },
                        onSaveFocusTopic = { viewModel.saveFocusTopic(it) },
                        onDeleteFocusTopic = { viewModel.deleteFocusTopic(it) }
                    )
                }

                ManageGuidanceSection.STRENGTH_WEAKNESS -> {
                    ManageStrengthWeaknessScreen(
                        selectedExam = strengthExam,
                        selectedSubject = strengthSubject,
                        examsList = examsList,
                        onExamChange = { strengthExam = it },
                        onSubjectChange = { strengthSubject = it }
                    )
                }

                ManageGuidanceSection.PREP_STRATEGY -> {
                    ManagePrepStrategyScreen(
                        selectedExam = strategyExam,
                        selectedSubject = strategySubject,
                        examsList = examsList,
                        allSubjectsChapters = allSubjectsChapters,
                        allQuestions = allQuestions,
                        allPyqFocus = allPyqFocus,
                        allPrepStrategies = allPrepStrategies,
                        onExamChange = { strategyExam = it; strategySubject = "All Subjects" },
                        onSubjectChange = { strategySubject = it },
                        onSaveStrategy = { viewModel.savePrepStrategy(it) },
                        onDeleteStrategy = { viewModel.deletePrepStrategy(it) }
                    )
                }
            }
        }
    }
}

@Composable
fun ManageGuidanceLanding(
    onNavigate: (ManageGuidanceSection) -> Unit,
    onSyncCloud: () -> Unit,
    onRefreshCloud: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            ManageSectionBannerCard(
                title = "PYQ Focus",
                subtitle = "View entries, Add PYQ Focus Entry, Edit, Delete",
                icon = Icons.Default.History,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                onClick = { onNavigate(ManageGuidanceSection.PYQ_FOCUS) }
            )
        }
        item {
            ManageSectionBannerCard(
                title = "Priority Topics",
                subtitle = "View entries, Add, Edit, Delete",
                icon = Icons.Default.TrackChanges,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                onClick = { onNavigate(ManageGuidanceSection.PRIORITY_TOPICS) }
            )
        }
        item {
            ManageSectionBannerCard(
                title = "Strength & Weakness",
                subtitle = "Configure accuracy & importance thresholds",
                icon = Icons.Default.FitnessCenter,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                onClick = { onNavigate(ManageGuidanceSection.STRENGTH_WEAKNESS) }
            )
        }
        item {
            ManageSectionBannerCard(
                title = "Preparation Strategy",
                subtitle = "View entries, Add, Edit, Delete preparation strategies",
                icon = Icons.Default.Lightbulb,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                onClick = { onNavigate(ManageGuidanceSection.PREP_STRATEGY) }
            )
        }
        
        // Guidance Data Sync
        item {
            Card(
                modifier = Modifier.fillMaxWidth().clickable {
                    onSyncCloud()
                    onRefreshCloud()
                },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudSync,
                        contentDescription = "Guidance Data Sync",
                        modifier = Modifier
                            .size(44.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                            .padding(10.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Guidance Data Sync",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "One centralized Firestore sync option",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ManageSectionBannerCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    containerColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = containerColor),
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
                    .size(44.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                    .padding(10.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Open",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/* ==========================================================================
   SECTION 1: PYQ FOCUS MANAGEMENT
   ========================================================================== */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagePyqFocusScreen(
    selectedExam: String?,
    selectedSubject: String,
    examsList: List<ExamEntity>,
    allSubjectsChapters: List<SubjectChapterEntity>,
    allQuestions: List<QuestionEntity>,
    allPyqFocus: List<PyqFocusEntity>,
    onExamChange: (String) -> Unit,
    onSubjectChange: (String) -> Unit,
    onSavePyq: (PyqFocusEntity) -> Unit,
    onDeletePyq: (PyqFocusEntity) -> Unit
) {
    var examDropdownExpanded by remember { mutableStateOf(false) }
    var subjectDropdownExpanded by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Available subjects for selected exam
    val availableSubjects = remember(selectedExam, allSubjectsChapters, allPyqFocus, allQuestions) {
        val chapterSubjs = allSubjectsChapters.map { it.subject }
        val pyqSubjs = allPyqFocus.filter { 
            selectedExam == null || it.exam.equals(selectedExam, ignoreCase = true) || it.exam.contains(selectedExam, ignoreCase = true) 
        }.map { it.subject }
        val qSubjs = allQuestions.filter { 
            selectedExam == null || it.examCategory.contains(selectedExam, ignoreCase = true) || selectedExam.contains(it.examCategory, ignoreCase = true)
        }.map { it.subject }
        val combined = (chapterSubjs + pyqSubjs + qSubjs).filter { it.isNotBlank() }.distinct().sorted()
        listOf("All Subjects") + combined
    }

    // Filter PYQ Focus records matching selected exam
    val examPyqs = remember(allPyqFocus, selectedExam) {
        if (selectedExam == null) emptyList()
        else allPyqFocus.filter { 
            it.exam.equals(selectedExam, ignoreCase = true) || 
            it.exam.contains(selectedExam, ignoreCase = true) || 
            selectedExam.contains(it.exam, ignoreCase = true)
        }
    }

    // Chapters to display based on subject selection
    val displayedPyqs = remember(examPyqs, selectedSubject) {
        if (selectedSubject == "All Subjects") {
            examPyqs
        } else {
            examPyqs.filter { it.subject.equals(selectedSubject, ignoreCase = true) }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Independent Exam Selector
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
                examsList.forEach { exam ->
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

        // Independent Subject Selector
        if (selectedExam != null) {
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

            // Action Row: Add Chapter & Auto-Scan
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (selectedSubject == "All Subjects") "All Chapters (${displayedPyqs.size})" else "$selectedSubject Chapters",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            // Auto-scan questions tagged with PYQ for this exam
                            val pyqQuestions = allQuestions.filter { 
                                (it.examCategory.contains(selectedExam, ignoreCase = true) || selectedExam.contains(it.examCategory, ignoreCase = true) || it.examCategory.isEmpty()) &&
                                it.questionType.equals("PYQ", ignoreCase = true)
                            }
                            if (pyqQuestions.isEmpty()) {
                                Toast.makeText(context, "No PYQ-tagged questions found in Q-Bank for $selectedExam", Toast.LENGTH_SHORT).show()
                            } else {
                                val grouped = pyqQuestions.groupBy { Pair(it.subject, it.topic) }
                                var addedCount = 0
                                grouped.forEach { (key, qList) ->
                                    val subj = key.first
                                    val chap = key.second
                                    if (subj.isNotBlank() && chap.isNotBlank()) {
                                        val existing = examPyqs.find { it.subject.equals(subj, ignoreCase = true) && it.chapter.equals(chap, ignoreCase = true) }
                                        val entity = existing?.copy(pyqCount = qList.size, updatedAt = System.currentTimeMillis())
                                            ?: PyqFocusEntity(exam = selectedExam, subject = subj, chapter = chap, pyqCount = qList.size, examsCovered = 1, updatedAt = System.currentTimeMillis())
                                        onSavePyq(entity)
                                        addedCount++
                                    }
                                }
                                Toast.makeText(context, "Scanned $addedCount chapters from Q-Bank PYQ questions!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Auto-Scan Q-Bank", style = MaterialTheme.typography.labelSmall)
                    }

                    Button(
                        onClick = { showAddDialog = true },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add PYQ Entry", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            // PYQ Chapters List
            if (displayedPyqs.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(36.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "No PYQ records for this selection yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Tap 'Auto-Scan Q-Bank' to generate from existing questions or 'Add PYQ Entry' to enter manually.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 60.dp)
                ) {
                    // Header Row
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Chapter & Subject", modifier = Modifier.weight(2.2f), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                            Text("PYQs", modifier = Modifier.weight(1.1f), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium, textAlign = TextAlign.Center)
                            Text("Exams", modifier = Modifier.weight(1.1f), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium, textAlign = TextAlign.Center)
                            Text("Avg/Imp", modifier = Modifier.weight(1.3f), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium, textAlign = TextAlign.Center)
                            Spacer(modifier = Modifier.width(36.dp))
                        }
                    }

                    items(displayedPyqs) { pyqItem ->
                        PyqFocusRowItem(
                            item = pyqItem,
                            onSave = onSavePyq,
                            onDelete = { onDeletePyq(pyqItem) }
                        )
                    }
                }
            }
        }
    }

    // Add PYQ Entry Dialog
    if (showAddDialog && selectedExam != null) {
        var addSubject by remember { mutableStateOf(if (selectedSubject != "All Subjects") selectedSubject else availableSubjects.getOrNull(1) ?: "") }
        var addChapter by remember { mutableStateOf("") }
        var addPyqCountStr by remember { mutableStateOf("5") }
        var addExamsCoveredStr by remember { mutableStateOf("2") }

        val chaptersForSubj = remember(addSubject, allSubjectsChapters) {
            allSubjectsChapters.filter { it.subject.equals(addSubject, ignoreCase = true) }.map { it.chapter }.distinct().sorted()
        }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add PYQ Focus Entry") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    var subjectExpanded by remember { mutableStateOf(false) }
                    var chapterExpanded by remember { mutableStateOf(false) }

                    val allRawSubjects = remember(allSubjectsChapters) {
                        allSubjectsChapters.map { it.subject }.distinct().sorted()
                    }

                    ExposedDropdownMenuBox(
                        expanded = subjectExpanded,
                        onExpandedChange = { subjectExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = addSubject,
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Subject Name") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjectExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                        )
                        ExposedDropdownMenu(
                            expanded = subjectExpanded,
                            onDismissRequest = { subjectExpanded = false }
                        ) {
                            allRawSubjects.forEach { subj ->
                                DropdownMenuItem(
                                    text = { Text(subj) },
                                    onClick = { 
                                        if (addSubject != subj) {
                                            addSubject = subj
                                            addChapter = ""
                                        }
                                        subjectExpanded = false 
                                    }
                                )
                            }
                        }
                    }

                    ExposedDropdownMenuBox(
                        expanded = chapterExpanded,
                        onExpandedChange = { chapterExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = addChapter,
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Chapter / Topic Name") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = chapterExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                        )
                        ExposedDropdownMenu(
                            expanded = chapterExpanded,
                            onDismissRequest = { chapterExpanded = false }
                        ) {
                            chaptersForSubj.forEach { chap ->
                                DropdownMenuItem(
                                    text = { Text(chap) },
                                    onClick = { addChapter = chap; chapterExpanded = false }
                                )
                            }
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SafeOutlinedTextField(
                            value = addPyqCountStr,
                            onValueChange = { addPyqCountStr = it },
                            label = { Text("Total PYQs") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        SafeOutlinedTextField(
                            value = addExamsCoveredStr,
                            onValueChange = { addExamsCoveredStr = it },
                            label = { Text("Exams Covered") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmedSubj = addSubject.trim()
                        val trimmedChap = addChapter.trim()
                        
                        if (trimmedSubj.isBlank() || trimmedChap.isBlank()) {
                            Toast.makeText(context, "Subject and Chapter are required", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        
                        if (!chaptersForSubj.contains(trimmedChap)) {
                            Toast.makeText(context, "Chapter does not belong to selected Subject", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        
                        val isDuplicate = allPyqFocus.any { 
                            it.exam.equals(selectedExam, ignoreCase = true) && 
                            it.subject.equals(trimmedSubj, ignoreCase = true) && 
                            it.chapter.equals(trimmedChap, ignoreCase = true) 
                        }
                        
                        if (isDuplicate) {
                            Toast.makeText(context, "PYQ Focus for this Chapter already exists", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        val pyqCount = addPyqCountStr.toIntOrNull() ?: 0
                        val examsCovered = addExamsCoveredStr.toIntOrNull() ?: 1
                        onSavePyq(
                            PyqFocusEntity(
                                exam = selectedExam,
                                subject = trimmedSubj,
                                chapter = trimmedChap,
                                pyqCount = pyqCount,
                                examsCovered = examsCovered,
                                updatedAt = System.currentTimeMillis()
                            )
                        )
                        showAddDialog = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun PyqFocusRowItem(
    item: PyqFocusEntity,
    onSave: (PyqFocusEntity) -> Unit,
    onDelete: () -> Unit
) {
    var pyqCountStr by remember(item.pyqCount) { mutableStateOf(item.pyqCount.toString()) }
    var examsCoveredStr by remember(item.examsCovered) { mutableStateOf(item.examsCovered.toString()) }

    val isModified = pyqCountStr != item.pyqCount.toString() || examsCoveredStr != item.examsCovered.toString()

    val avg = if ((examsCoveredStr.toIntOrNull() ?: 0) > 0) {
        (pyqCountStr.toFloatOrNull() ?: 0f) / (examsCoveredStr.toFloatOrNull() ?: 1f)
    } else 0f

    val (impLabel, impColor) = when {
        avg >= 2.0f -> Pair("High", Color(0xFFE53935))
        avg >= 1.0f -> Pair("Med", Color(0xFFFB8C00))
        else -> Pair("Low", Color(0xFF757575))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Column(modifier = Modifier.weight(2.2f)) {
                Text(
                    text = item.chapter,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = item.subject,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            SafeOutlinedTextField(
                value = pyqCountStr,
                onValueChange = { pyqCountStr = it },
                modifier = Modifier.weight(1.1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            SafeOutlinedTextField(
                value = examsCoveredStr,
                onValueChange = { examsCoveredStr = it },
                modifier = Modifier.weight(1.1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Column(
                modifier = Modifier.weight(1.3f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = String.format(java.util.Locale.US, "%.1f", avg),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = impColor.copy(alpha = 0.15f),
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Text(
                        text = impLabel,
                        color = impColor,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.width(36.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val context = androidx.compose.ui.platform.LocalContext.current
                if (isModified) {
                    IconButton(
                        onClick = {
                            val newCount = pyqCountStr.toIntOrNull() ?: 0
                            val newCovered = examsCoveredStr.toIntOrNull() ?: 0
                            onSave(item.copy(pyqCount = newCount, examsCovered = newCovered, updatedAt = System.currentTimeMillis()))
                            Toast.makeText(context, "Changes Saved to Cloud", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Save", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    }
                } else {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

/* ==========================================================================
   SECTION 2: PRIORITY TOPICS MANAGEMENT
   ========================================================================== */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagePriorityTopicsScreen(
    selectedExam: String?,
    selectedSubject: String,
    examsList: List<ExamEntity>,
    allSubjectsChapters: List<SubjectChapterEntity>,
    allPyqFocus: List<PyqFocusEntity>,
    allFocusTopics: List<FocusTopicEntity>,
    onExamChange: (String) -> Unit,
    onSubjectChange: (String) -> Unit,
    onSaveFocusTopic: (FocusTopicEntity) -> Unit,
    onDeleteFocusTopic: (FocusTopicEntity) -> Unit
) {
    var examDropdownExpanded by remember { mutableStateOf(false) }
    var subjectDropdownExpanded by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }

    val availableSubjects = remember(selectedExam, allSubjectsChapters, allPyqFocus) {
        val chapterSubjs = allSubjectsChapters.map { it.subject }
        val pyqSubjs = allPyqFocus.filter { 
            selectedExam == null || it.exam.equals(selectedExam, ignoreCase = true) || it.exam.contains(selectedExam, ignoreCase = true) 
        }.map { it.subject }
        val combined = (chapterSubjs + pyqSubjs).filter { it.isNotBlank() }.distinct().sorted()
        listOf("All Subjects") + combined
    }

    val examTopics = remember(allFocusTopics, selectedExam) {
        if (selectedExam == null) emptyList()
        else allFocusTopics.filter { 
            it.exam.equals(selectedExam, ignoreCase = true) || 
            it.exam.contains(selectedExam, ignoreCase = true) || 
            selectedExam.contains(it.exam, ignoreCase = true)
        }
    }

    val displayedTopics = remember(examTopics, selectedSubject) {
        if (selectedSubject == "All Subjects") examTopics
        else examTopics.filter { it.subject.equals(selectedSubject, ignoreCase = true) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Exam Selector
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
                examsList.forEach { exam ->
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

        // Subject Selector
        if (selectedExam != null) {
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

            // Info & Add Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Configured Priority Topics (${displayedTopics.size})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Button(
                    onClick = { showAddDialog = true },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Priority Topic", style = MaterialTheme.typography.labelSmall)
                }
            }

            if (displayedTopics.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.TrackChanges, contentDescription = null, modifier = Modifier.size(36.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "No manual priority overrides set for this selection.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "The system automatically computes topic priority using PYQ frequency + individual user accuracy. You can optionally add custom priority tags and study recommendations above.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 60.dp)
                ) {
                    items(displayedTopics) { topic ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(topic.topic, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        val badgeColor = when (topic.priority) {
                                            "High" -> Color(0xFFE53935)
                                            "Medium" -> Color(0xFFFB8C00)
                                            else -> Color(0xFF43A047)
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = badgeColor.copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = "${topic.priority} Priority",
                                                color = badgeColor,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        "${topic.subject} • ${topic.chapter}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    if (topic.instruction.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            topic.instruction,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                IconButton(onClick = { onDeleteFocusTopic(topic) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Priority Topic Dialog
    if (showAddDialog && selectedExam != null) {
        var addSubject by remember { mutableStateOf(if (selectedSubject != "All Subjects") selectedSubject else availableSubjects.getOrNull(1) ?: "") }
        var addChapter by remember { mutableStateOf("") }
        var addTopic by remember { mutableStateOf("") }
        var addPriority by remember { mutableStateOf("High") }
        var addInstruction by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Priority Topic") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    var subjectExpanded by remember { mutableStateOf(false) }
                    var chapterExpanded by remember { mutableStateOf(false) }

                    val allRawSubjects = remember(allSubjectsChapters) {
                        allSubjectsChapters.map { it.subject }.distinct().sorted()
                    }
                    val chaptersForSubj = remember(addSubject, allSubjectsChapters) {
                        allSubjectsChapters.filter { it.subject.equals(addSubject, ignoreCase = true) }.map { it.chapter }.distinct().sorted()
                    }

                    ExposedDropdownMenuBox(
                        expanded = subjectExpanded,
                        onExpandedChange = { subjectExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = addSubject,
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Subject") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjectExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                        )
                        ExposedDropdownMenu(
                            expanded = subjectExpanded,
                            onDismissRequest = { subjectExpanded = false }
                        ) {
                            allRawSubjects.forEach { subj ->
                                DropdownMenuItem(
                                    text = { Text(subj) },
                                    onClick = { 
                                        if (addSubject != subj) {
                                            addSubject = subj
                                            addChapter = ""
                                        }
                                        subjectExpanded = false 
                                    }
                                )
                            }
                        }
                    }

                    ExposedDropdownMenuBox(
                        expanded = chapterExpanded,
                        onExpandedChange = { chapterExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = addChapter,
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Chapter") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = chapterExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                        )
                        ExposedDropdownMenu(
                            expanded = chapterExpanded,
                            onDismissRequest = { chapterExpanded = false }
                        ) {
                            chaptersForSubj.forEach { chap ->
                                DropdownMenuItem(
                                    text = { Text(chap) },
                                    onClick = { addChapter = chap; chapterExpanded = false }
                                )
                            }
                        }
                    }
                    SafeOutlinedTextField(
                        value = addTopic,
                        onValueChange = { addTopic = it },
                        label = { Text("Topic Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    SafeOutlinedTextField(
                        value = addPriority,
                        onValueChange = { addPriority = it },
                        label = { Text("Priority Level (High, Medium, Low)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    SafeOutlinedTextField(
                        value = addInstruction,
                        onValueChange = { addInstruction = it },
                        label = { Text("Recommendation Note (Optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (addSubject.isNotBlank() && addChapter.isNotBlank() && addTopic.isNotBlank()) {
                            onSaveFocusTopic(
                                FocusTopicEntity(
                                    exam = selectedExam,
                                    subject = addSubject.trim(),
                                    chapter = addChapter.trim(),
                                    topic = addTopic.trim(),
                                    priority = addPriority.trim(),
                                    instruction = addInstruction.trim(),
                                    updatedAt = System.currentTimeMillis()
                                )
                            )
                            showAddDialog = false
                        }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
            }
        )
    }
}

/* ==========================================================================
   SECTION 3: STRENGTH & WEAKNESS CONFIGURATION
   ========================================================================== */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageStrengthWeaknessScreen(
    selectedExam: String?,
    selectedSubject: String,
    examsList: List<ExamEntity>,
    onExamChange: (String) -> Unit,
    onSubjectChange: (String) -> Unit
) {
    val context = LocalContext.current

    var minAttemptsStr by remember { mutableStateOf(GuidanceEngine.minAttemptsRequired.toString()) }
    var weakThresholdStr by remember { mutableStateOf(String.format(Locale.US, "%.0f", GuidanceEngine.weakAccuracyThreshold)) }
    var strongThresholdStr by remember { mutableStateOf(String.format(Locale.US, "%.0f", GuidanceEngine.strongAccuracyThreshold)) }
    var highPyqAvgStr by remember { mutableStateOf(String.format(Locale.US, "%.1f", GuidanceEngine.highImportancePyqAvg)) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 60.dp)
    ) {
        // System Explanation Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.FitnessCenter,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Automated Performance Engine",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "Strength & Weakness uses actual user practice data (attempts, correct answers, accuracy, and chapter PYQ frequency). No fake or manual student scores are entered here. You can calibrate the system evaluation thresholds below.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Calibrate Thresholds Form
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Analysis Threshold Settings",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    SafeOutlinedTextField(
                        value = minAttemptsStr,
                        onValueChange = { minAttemptsStr = it },
                        label = { Text("Minimum Attempts Required") },
                        supportingText = { Text("Topics with fewer attempts are tagged 'Not Enough Data'") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    SafeOutlinedTextField(
                        value = weakThresholdStr,
                        onValueChange = { weakThresholdStr = it },
                        label = { Text("Weak Topic Threshold (Accuracy %)") },
                        supportingText = { Text("Accuracy below this marks a topic as Weak") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    SafeOutlinedTextField(
                        value = strongThresholdStr,
                        onValueChange = { strongThresholdStr = it },
                        label = { Text("Strong Topic Threshold (Accuracy %)") },
                        supportingText = { Text("Accuracy at or above this marks a topic as Strong") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    SafeOutlinedTextField(
                        value = highPyqAvgStr,
                        onValueChange = { highPyqAvgStr = it },
                        label = { Text("High Importance PYQ Threshold (Avg PYQ)") },
                        supportingText = { Text("Avg PYQ per exam at or above this marks High Importance") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            val minAtt = minAttemptsStr.toIntOrNull() ?: 10
                            val weakThresh = weakThresholdStr.toFloatOrNull() ?: 60f
                            val strongThresh = strongThresholdStr.toFloatOrNull() ?: 80f
                            val highPyq = highPyqAvgStr.toFloatOrNull() ?: 1.5f

                            GuidanceEngine.setThresholds(minAtt, weakThresh, strongThresh, highPyq)
                            Toast.makeText(context, "Analysis thresholds updated successfully", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save Thresholds")
                    }
                }
            }
        }

        // Classification Legend Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Classification Matrix Rules",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "• 🔴 Weak & Important: High PYQ (≥ $highPyqAvgStr) + Low Accuracy (< $weakThresholdStr%)\n" +
                        "• 🟢 Strong & Important: High PYQ (≥ $highPyqAvgStr) + High Accuracy (≥ $strongThresholdStr%)\n" +
                        "• 🟡 Weak & Less Important: Low PYQ (< $highPyqAvgStr) + Low Accuracy (< $weakThresholdStr%)\n" +
                        "• 🟢 Strong: Low PYQ (< $highPyqAvgStr) + High Accuracy (≥ $strongThresholdStr%)\n" +
                        "• ⚪ Not Enough Data: Student attempts < $minAttemptsStr",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/* ==========================================================================
   SECTION 4: PREPARATION STRATEGY MANAGEMENT
   ========================================================================== */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagePrepStrategyScreen(
    selectedExam: String?,
    selectedSubject: String,
    examsList: List<ExamEntity>,
    allSubjectsChapters: List<SubjectChapterEntity>,
    allQuestions: List<QuestionEntity> = emptyList(),
    allPyqFocus: List<PyqFocusEntity> = emptyList(),
    allPrepStrategies: List<PrepStrategyEntity>,
    onExamChange: (String) -> Unit,
    onSubjectChange: (String) -> Unit,
    onSaveStrategy: (PrepStrategyEntity) -> Unit,
    onDeleteStrategy: (PrepStrategyEntity) -> Unit
) {
    var examDropdownExpanded by remember { mutableStateOf(false) }
    var subjectDropdownExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val availableSubjects = remember(selectedExam, allSubjectsChapters, allQuestions, allPyqFocus, allPrepStrategies) {
        val chapterSubjs = allSubjectsChapters.map { it.subject }
        val pyqSubjs = allPyqFocus.filter { 
            selectedExam == null || it.exam.equals(selectedExam, ignoreCase = true) || it.exam.contains(selectedExam, ignoreCase = true) || selectedExam.contains(it.exam, ignoreCase = true)
        }.map { it.subject }
        val qSubjs = allQuestions.filter { 
            selectedExam == null || it.examCategory.contains(selectedExam, ignoreCase = true) || selectedExam.contains(it.examCategory, ignoreCase = true)
        }.map { it.subject }
        val stratSubjs = allPrepStrategies.filter {
            selectedExam == null || it.exam.equals(selectedExam, ignoreCase = true) || selectedExam.contains(it.exam, ignoreCase = true) || selectedExam.contains(it.exam, ignoreCase = true)
        }.mapNotNull { it.subject }
        val combined = (chapterSubjs + pyqSubjs + qSubjs + stratSubjs).filter { it.isNotBlank() }.distinct().sorted()
        listOf("All Subjects") + combined
    }

    val examStrategies = remember(allPrepStrategies, selectedExam) {
        if (selectedExam == null) emptyList()
        else allPrepStrategies.filter { 
            it.exam.equals(selectedExam, ignoreCase = true) || 
            it.exam.contains(selectedExam, ignoreCase = true) || 
            selectedExam.contains(it.exam, ignoreCase = true)
        }
    }

    // Find current strategy for (selectedExam, selectedSubject)
    val subjectKey = if (selectedSubject != "All Subjects") selectedSubject else null
    val currentStrategy = remember(examStrategies, selectedExam, subjectKey) {
        if (selectedExam == null) null
        else examStrategies.find { 
            ((subjectKey == null && (it.subject == null || it.subject == "All Subjects")) || (subjectKey != null && it.subject.equals(subjectKey, ignoreCase = true)))
        }
    }

    var strategyContent by remember(currentStrategy, selectedExam, selectedSubject) {
        mutableStateOf(currentStrategy?.content ?: "")
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Exam Selector
        item {
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
                    examsList.forEach { exam ->
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

        if (selectedExam != null) {
            // Scope / Subject Selector
            item {
                ExposedDropdownMenuBox(
                    expanded = subjectDropdownExpanded,
                    onExpandedChange = { subjectDropdownExpanded = !subjectDropdownExpanded }
                ) {
                    SafeOutlinedTextField(
                        value = if (selectedSubject == "All Subjects") "All Subjects (Overall Exam Strategy)" else selectedSubject,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Strategy Scope") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjectDropdownExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = subjectDropdownExpanded,
                        onDismissRequest = { subjectDropdownExpanded = false }
                    ) {
                        availableSubjects.forEach { subject ->
                            DropdownMenuItem(
                                text = { Text(if (subject == "All Subjects") "All Subjects (Overall Exam Strategy)" else subject) },
                                onClick = {
                                    onSubjectChange(subject)
                                    subjectDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Quick select chip row for existing published strategies for this exam
            if (examStrategies.isNotEmpty()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Published Strategies (${examStrategies.size}):",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(examStrategies) { strat ->
                                val label = if (strat.subject.isNullOrBlank() || strat.subject == "All Subjects") "Overall Exam" else strat.subject
                                val isSelected = (subjectKey == null && (strat.subject.isNullOrBlank() || strat.subject == "All Subjects")) ||
                                        (subjectKey != null && strat.subject.equals(subjectKey, ignoreCase = true))
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        onSubjectChange(if (strat.subject.isNullOrBlank()) "All Subjects" else strat.subject)
                                    },
                                    label = { Text(label) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = if (isSelected) Icons.Default.Check else Icons.Default.Lightbulb,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Status Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (currentStrategy != null) 
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        else 
                            MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (currentStrategy != null) Icons.Default.CheckCircle else Icons.Default.Info,
                            contentDescription = null,
                            tint = if (currentStrategy != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (currentStrategy != null) 
                                    "Custom Strategy Published" 
                                else 
                                    "Dynamic Auto-Strategy Active",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (currentStrategy != null)
                                    "Scope: ${if (selectedSubject == "All Subjects") "Overall Exam" else selectedSubject} (${strategyContent.length} chars). Editing and saving will update this live."
                                else
                                    "No custom strategy published yet for ${if (selectedSubject == "All Subjects") "Overall Exam" else selectedSubject}. System dynamically derives advice from mock accuracy. Enter custom guidelines below to publish.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Quick Template Buttons
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            strategyContent = "Overall 3-Phase Preparation Strategy for $selectedExam:\n\n" +
                                    "Phase 1: Foundation Building (Weeks 1-3)\n" +
                                    "• Complete core concepts from high-weightage chapters.\n" +
                                    "• Clear grammar rules and fundamental formulas.\n\n" +
                                    "Phase 2: PYQ Drill & Topic Practice (Weeks 4-6)\n" +
                                    "• Solve past 5 years' question papers chapter-wise.\n" +
                                    "• Target weak areas identified in Strength & Weakness analysis.\n\n" +
                                    "Phase 3: Full-Length Mocks & Revision (Weeks 7-8)\n" +
                                    "• Attempt 2 full-length timed mocks weekly.\n" +
                                    "• Revise bookmarked questions and error log daily."
                        },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text("Roadmap Template", style = MaterialTheme.typography.labelSmall)
                    }

                    OutlinedButton(
                        onClick = {
                            val subjName = if (selectedSubject != "All Subjects") selectedSubject else "Subject"
                            strategyContent = "Subject Preparation Strategy for $subjName ($selectedExam):\n\n" +
                                    "1. High-Priority Focus: Prioritize chapters with highest PYQ frequency.\n" +
                                    "2. Daily Practice: Dedicate at least 30 minutes to MCQs from this subject.\n" +
                                    "3. Error Analysis: Re-attempt all incorrect questions within 48 hours.\n" +
                                    "4. Formula & Notes Revision: Maintain concise formula sheets for weekly revision.\n" +
                                    "5. Speed & Accuracy: Aim for >80% accuracy under timed conditions."
                        },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text("Subject Template", style = MaterialTheme.typography.labelSmall)
                    }

                    if (strategyContent.isNotBlank()) {
                        IconButton(
                            onClick = { strategyContent = "" },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                Icons.Default.Clear,
                                contentDescription = "Clear Text",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Content Editor
            item {
                SafeOutlinedTextField(
                    value = strategyContent,
                    onValueChange = { strategyContent = it },
                    label = { Text("Preparation Strategy Content") },
                    supportingText = { 
                        Text("Scope: ${if (selectedSubject == "All Subjects") "Overall Exam Strategy" else selectedSubject} • ${strategyContent.length} characters") 
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 180.dp, max = 320.dp),
                    minLines = 8
                )
            }

            // Save & Delete Buttons
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentStrategy != null) {
                        TextButton(
                            onClick = {
                                onDeleteStrategy(currentStrategy)
                                strategyContent = ""
                                Toast.makeText(context, "Strategy removed. System will use dynamic strategy.", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Clear Strategy", color = MaterialTheme.colorScheme.error)
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    Button(
                        enabled = strategyContent.isNotBlank(),
                        onClick = {
                            val entity = (currentStrategy ?: PrepStrategyEntity(
                                exam = selectedExam,
                                subject = subjectKey,
                                content = ""
                            )).copy(
                                exam = selectedExam,
                                subject = subjectKey,
                                content = strategyContent.trim(),
                                updatedAt = System.currentTimeMillis()
                            )
                            onSaveStrategy(entity)
                            Toast.makeText(context, "Preparation strategy published to Firestore!", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (currentStrategy != null) "Update & Publish" else "Save & Publish")
                    }
                }
            }

            // List of all published strategies for this exam
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "All Published Strategies ($selectedExam)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${examStrategies.size} entries",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (examStrategies.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.MenuBook,
                                contentDescription = null,
                                modifier = Modifier.size(32.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No custom preparation strategies created for $selectedExam yet.",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(examStrategies) { strat ->
                    val scopeName = if (strat.subject.isNullOrBlank() || strat.subject == "All Subjects") "Overall Exam Strategy" else "Subject: ${strat.subject}"
                    val isEditingThis = (subjectKey == null && (strat.subject.isNullOrBlank() || strat.subject == "All Subjects")) ||
                            (subjectKey != null && strat.subject.equals(subjectKey, ignoreCase = true))

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSubjectChange(if (strat.subject.isNullOrBlank()) "All Subjects" else strat.subject)
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isEditingThis)
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                            else
                                MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lightbulb,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = scopeName,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = {
                                            onSubjectChange(if (strat.subject.isNullOrBlank()) "All Subjects" else strat.subject)
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Edit,
                                            contentDescription = "Edit",
                                            modifier = Modifier.size(18.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            onDeleteStrategy(strat)
                                            if (isEditingThis) strategyContent = ""
                                            Toast.makeText(context, "Deleted strategy for $scopeName", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            modifier = Modifier.size(18.dp),
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = strat.content.replace("\n", " ").take(140) + if (strat.content.length > 140) "..." else "",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
