package com.example.ui.screens

import com.example.ui.components.SafeOutlinedTextField
import com.example.ui.components.BatchImportMockQuestionsDialog

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.local.MockTestEntity
import com.example.ui.viewmodel.JuktiViewModel
import com.example.ui.viewmodel.LocalMessageTranslator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMockScreen(viewModel: JuktiViewModel) {
    val context = LocalContext.current
    val isAdminOrOwner by viewModel.isAdminOrOwner.collectAsState()
    val mocks by viewModel.mockTests.collectAsState()
    val exams by viewModel.examsList.collectAsState()
    val allQuestions by viewModel.questions.collectAsState()

    var selectedMock by remember { mutableStateOf<MockTestEntity?>(null) }
    var mockToDelete by remember { mutableStateOf<MockTestEntity?>(null) }
    var showBatchImportDialog by remember { mutableStateOf(false) }

    // Form fields for editing
    var mockTitleEn by remember { mutableStateOf("") }
    var mockTitleAs by remember { mutableStateOf("") }
    val selectedExams = remember { mutableStateListOf<String>() }
    var examDialogVisible by remember { mutableStateOf(false) }

    var durationMinutes by remember { mutableStateOf("90") }
    
    var negativeMarking by remember { mutableStateOf("0.25 Marks") }
    var negativeMarkingExpanded by remember { mutableStateOf(false) }
    val negativeMarkingOptions = listOf("0.25 Marks", "0.5 Marks", "1.0 Marks", "None")

    var testType by remember { mutableStateOf("Full-Length") }
    var testTypeExpanded by remember { mutableStateOf(false) }
    val testTypeOptions = listOf("Full-Length", "Subject-wise", "Chapter-wise")

    var planType by remember { mutableStateOf("Free") }
    var planTypeExpanded by remember { mutableStateOf(false) }
    val planTypeOptions = listOf("Free", "Premium")

    var difficulty by remember { mutableStateOf("Medium") }
    var difficultyExpanded by remember { mutableStateOf(false) }
    val difficultyOptions = listOf("Easy", "Medium", "Hard")

    var selectedSubjectFilter by remember { mutableStateOf("All Subjects") }
    var subjectFilterExpanded by remember { mutableStateOf(false) }
    var questionSearchQuery by remember { mutableStateOf("") }
    val selectedQuestionIds = remember { mutableStateListOf<Long>() }
    val subjectMarks = remember { mutableStateMapOf<String, String>() }
    val individualQuestionMarks = remember { mutableStateMapOf<Long, String>() }

    val allSubjectsChapters by viewModel.allSubjectsChapters.collectAsState()
    val subjectsList = listOf("All Subjects") + allSubjectsChapters.map { it.subject }.distinct()

    val filteredQuestions = allQuestions.filter { q ->
        val matchesSubject = selectedSubjectFilter == "All Subjects" || q.subject.equals(selectedSubjectFilter, ignoreCase = true)
        val matchesSearch = questionSearchQuery.isBlank() || 
            q.questionEn.contains(questionSearchQuery, ignoreCase = true) ||
            q.subject.contains(questionSearchQuery, ignoreCase = true)
        matchesSubject && matchesSearch
    }

    // When a mock is selected, populate fields
    LaunchedEffect(selectedMock) {
        selectedMock?.let { mock ->
            mockTitleEn = mock.titleEn
            mockTitleAs = mock.titleAs
            selectedExams.clear()
            if (mock.category.isNotBlank()) {
                selectedExams.addAll(mock.category.split(",").map { it.trim() })
            }
            durationMinutes = mock.durationMinutes.toString()
            negativeMarking = mock.negativeMarking
            testType = mock.testType
            planType = if (mock.isPremium) "Premium" else "Free"
            difficulty = mock.difficulty.ifBlank { "Medium" }
            selectedQuestionIds.clear()
            if (mock.questionIds.isNotBlank()) {
                selectedQuestionIds.addAll(mock.questionIds.split(",").mapNotNull { it.trim().toLongOrNull() })
            }
            
            subjectMarks.clear()
            if (mock.subjectMarksJson.isNotBlank() && mock.subjectMarksJson != "{}") {
                try {
                    val sObj = org.json.JSONObject(mock.subjectMarksJson)
                    sObj.keys().forEach { k ->
                        val mVal = sObj.getDouble(k).toFloat()
                        val mStr = if (mVal % 1f == 0f) mVal.toInt().toString() else mVal.toString()
                        subjectMarks[k] = mStr
                    }
                } catch (e: Exception) {}
            }

            individualQuestionMarks.clear()
            if (mock.questionMarksJson.isNotBlank() && mock.questionMarksJson != "{}") {
                try {
                    val qMarks = org.json.JSONObject(mock.questionMarksJson)
                    val selectedQs = allQuestions.filter { selectedQuestionIds.contains(it.id) }
                    
                    selectedQs.forEach { q ->
                        val qIdStr = q.id.toString()
                        if (qMarks.has(qIdStr)) {
                            val markVal = qMarks.getDouble(qIdStr).toFloat()
                            val subjKey = getQuestionSubject(q)
                            val subjMarkStr = subjectMarks[subjKey] ?: "1"
                            val expectedDef = subjMarkStr.toFloatOrNull() ?: 1.0f
                            if (markVal > 0f && markVal != expectedDef) {
                                val markStr = if (markVal % 1f == 0f) markVal.toInt().toString() else markVal.toString()
                                individualQuestionMarks[q.id] = markStr
                            }
                        }
                    }
                } catch (e: Exception) {}
            }
        }
    }

    Scaffold(
        topBar = {
            com.example.ui.components.JuktiTopAppBar(
                title = if (selectedMock == null) "Edit Mock Tests" else "Editing: ${selectedMock?.titleEn}",
                onBackClick = {
                    if (selectedMock != null) {
                        selectedMock = null
                    } else {
                        viewModel.navigateTo(com.example.ui.viewmodel.Screen.MANAGE_MOCK)
                    }
                }
            )
        }
    ) { innerPadding ->
        if (selectedMock == null) {
            // List all mocks to select for editing
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text("Select a Mock Test to Edit:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                itemsIndexed(mocks, key = { index, mock -> if (mock.id != 0L) mock.id else "mock_${mock.titleEn}_$index" }) { _, mock ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedMock = mock },
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(mock.titleEn, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Exam: ${mock.category} | Type: ${mock.testType} | ${mock.durationMinutes} mins", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            IconButton(onClick = { selectedMock = mock }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
                            }
                            IconButton(onClick = {
                                mockToDelete = mock
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        } else {
            // Edit form for selected mock
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text("Mock Test Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                item {
                    SafeOutlinedTextField(
                        value = mockTitleEn,
                        onValueChange = { mockTitleEn = it },
                        label = { Text("Mock Test Title (English) *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                item {
                    SafeOutlinedTextField(
                        value = mockTitleAs,
                        onValueChange = { mockTitleAs = it },
                        label = { Text("Mock Test Title (Assamese)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                item {
                    Box(modifier = Modifier.fillMaxWidth().clickable { examDialogVisible = true }) {
                        SafeOutlinedTextField(
                            value = if (selectedExams.isEmpty()) "Select Target Exams..." else selectedExams.joinToString(", "),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Target Exam (Multiple/Single) *") },
                            trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = false,
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                disabledBorderColor = MaterialTheme.colorScheme.outline,
                                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SafeOutlinedTextField(
                            value = durationMinutes,
                            onValueChange = { durationMinutes = it },
                            label = { Text("Duration (Mins) *") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ExposedDropdownMenuBox(
                            expanded = negativeMarkingExpanded,
                            onExpandedChange = { negativeMarkingExpanded = !negativeMarkingExpanded },
                            modifier = Modifier.weight(1f)
                        ) {
                            SafeOutlinedTextField(
                                value = negativeMarking,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Negative Mark") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = negativeMarkingExpanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = negativeMarkingExpanded,
                                onDismissRequest = { negativeMarkingExpanded = false }
                            ) {
                                negativeMarkingOptions.forEach { opt ->
                                    DropdownMenuItem(
                                        text = { Text(opt) },
                                        onClick = {
                                            negativeMarking = opt
                                            negativeMarkingExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        ExposedDropdownMenuBox(
                            expanded = testTypeExpanded,
                            onExpandedChange = { testTypeExpanded = !testTypeExpanded },
                            modifier = Modifier.weight(1f)
                        ) {
                            SafeOutlinedTextField(
                                value = testType,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Test Type") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = testTypeExpanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = testTypeExpanded,
                                onDismissRequest = { testTypeExpanded = false }
                            ) {
                                testTypeOptions.forEach { opt ->
                                    DropdownMenuItem(
                                        text = { Text(opt) },
                                        onClick = {
                                            testType = opt
                                            testTypeExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ExposedDropdownMenuBox(
                            expanded = planTypeExpanded,
                            onExpandedChange = { planTypeExpanded = !planTypeExpanded },
                            modifier = Modifier.weight(1f)
                        ) {
                            SafeOutlinedTextField(
                                value = planType,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Plan") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = planTypeExpanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = planTypeExpanded,
                                onDismissRequest = { planTypeExpanded = false }
                            ) {
                                planTypeOptions.forEach { opt ->
                                    DropdownMenuItem(
                                        text = { Text(opt) },
                                        onClick = {
                                            planType = opt
                                            planTypeExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        ExposedDropdownMenuBox(
                            expanded = difficultyExpanded,
                            onExpandedChange = { difficultyExpanded = !difficultyExpanded },
                            modifier = Modifier.weight(1f)
                        ) {
                            SafeOutlinedTextField(
                                value = difficulty,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Difficulty") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = difficultyExpanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = difficultyExpanded,
                                onDismissRequest = { difficultyExpanded = false }
                            ) {
                                difficultyOptions.forEach { opt ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                val dotColor = when (opt) {
                                                    "Easy" -> MaterialTheme.colorScheme.primary
                                                    "Medium" -> MaterialTheme.colorScheme.tertiary
                                                    "Hard" -> MaterialTheme.colorScheme.error
                                                    else -> MaterialTheme.colorScheme.outline
                                                }
                                                Surface(
                                                    color = dotColor,
                                                    shape = RoundedCornerShape(4.dp),
                                                    modifier = Modifier.size(8.dp)
                                                ) {}
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(opt, fontWeight = if (difficulty == opt) FontWeight.Bold else FontWeight.Normal)
                                            }
                                        },
                                        onClick = {
                                            difficulty = opt
                                            difficultyExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Select Questions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        if (isAdminOrOwner) {
                            FilledTonalButton(
                                onClick = { showBatchImportDialog = true },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                                modifier = Modifier.defaultMinSize(minHeight = 42.dp)
                            ) {
                                Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Batch Import",
                                    maxLines = 1,
                                    softWrap = false,
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Total Questions Added:",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Medium
                            )
                            Surface(
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "${selectedQuestionIds.size}",
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ExposedDropdownMenuBox(
                            expanded = subjectFilterExpanded,
                            onExpandedChange = { subjectFilterExpanded = !subjectFilterExpanded },
                            modifier = Modifier.weight(1f)
                        ) {
                            SafeOutlinedTextField(
                                value = selectedSubjectFilter,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Filter Subject") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjectFilterExpanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = subjectFilterExpanded,
                                onDismissRequest = { subjectFilterExpanded = false }
                            ) {
                                subjectsList.forEach { subj ->
                                    DropdownMenuItem(
                                        text = { Text(subj) },
                                        onClick = {
                                            selectedSubjectFilter = subj
                                            subjectFilterExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        SafeOutlinedTextField(
                            value = questionSearchQuery,
                            onValueChange = { questionSearchQuery = it },
                            label = { Text("Search Q-Bank") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            trailingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
                        )
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth().padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            itemsIndexed(filteredQuestions, key = { index, q -> if (q.id != 0L) q.id else "q_${q.questionEn.hashCode()}_$index" }) { _, q ->
                                val isSelected = selectedQuestionIds.contains(q.id)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            if (isSelected) selectedQuestionIds.remove(q.id)
                                            else selectedQuestionIds.add(q.id)
                                        }
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = isSelected,
                                        onCheckedChange = { checked ->
                                            if (checked) {
                                                if (!selectedQuestionIds.contains(q.id)) selectedQuestionIds.add(q.id)
                                            } else {
                                                selectedQuestionIds.remove(q.id)
                                            }
                                        }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        com.example.ui.components.QuestionTypeBadge(
                                            questionType = q.questionType,
                                            modifier = Modifier.padding(bottom = 4.dp)
                                        )
                                        Text(q.questionEn, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, maxLines = 2)
                                        Text("[${q.subject}]", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    val activeSelectedQuestions = remember(testType, selectedMock?.subjectOrChapter, selectedQuestionIds.toList(), allQuestions) {
                        if (testType == "Subject-wise" && !(selectedMock?.subjectOrChapter.isNullOrBlank())) {
                            allQuestions.filter { it.subject.equals(selectedMock!!.subjectOrChapter, ignoreCase = true) }
                        } else if (testType == "Chapter-wise" && (selectedMock?.subjectOrChapter?.contains("||") == true)) {
                            val parts = selectedMock!!.subjectOrChapter.split("||")
                            allQuestions.filter { it.subject.equals(parts[0], ignoreCase = true) && it.topic.equals(parts.getOrElse(1) { "" }, ignoreCase = true) }
                        } else {
                            allQuestions.filter { selectedQuestionIds.contains(it.id) }
                        }
                    }

                    MarksConfigurationSection(
                        subjectMarks = subjectMarks,
                        onSubjectMarkChange = { subj, markStr -> subjectMarks[subj] = markStr },
                        selectedQuestions = activeSelectedQuestions,
                        individualMarks = individualQuestionMarks,
                        onIndividualMarkChange = { id, markStr -> individualQuestionMarks[id] = markStr },
                        onResetIndividualMark = { id -> individualQuestionMarks.remove(id) }
                    )
                }

                item {
                    Button(
                        onClick = {
                            if (mockTitleEn.isNotBlank() && selectedExams.isNotEmpty()) {
                                val duration = durationMinutes.toIntOrNull() ?: 90
                                
                                val activeSelectedQuestions = if (testType == "Subject-wise") {
                                    allQuestions.filter { it.subject.equals(selectedMock!!.subjectOrChapter, ignoreCase = true) }
                                } else if (testType == "Chapter-wise" && selectedMock!!.subjectOrChapter.contains("||")) {
                                    val parts = selectedMock!!.subjectOrChapter.split("||")
                                    allQuestions.filter { it.subject.equals(parts[0], ignoreCase = true) && it.topic.equals(parts.getOrElse(1) { "" }, ignoreCase = true) }
                                } else {
                                    allQuestions.filter { selectedQuestionIds.contains(it.id) }
                                }

                                val calculatedTotalMarks = calculateTotalMarksFromSubjectConfig(
                                    activeSelectedQuestions, subjectMarks, individualQuestionMarks
                                )
                                val subjMarksJson = calculateSubjectMarksJson(
                                    activeSelectedQuestions, subjectMarks
                                )
                                val qMarksJson = calculateMockQuestionMarksJson(
                                    activeSelectedQuestions, subjectMarks, individualQuestionMarks
                                )

                                val updatedMock = selectedMock!!.copy(
                                    titleEn = mockTitleEn.trim(),
                                    titleAs = mockTitleAs.trim().ifBlank { mockTitleEn.trim() },
                                    category = selectedExams.joinToString(", "),
                                    durationMinutes = duration,
                                    totalQuestions = activeSelectedQuestions.size.coerceAtLeast(1),
                                    totalMarks = calculatedTotalMarks,
                                    testType = testType,
                                    subjectOrChapter = selectedMock!!.subjectOrChapter,
                                    negativeMarking = negativeMarking,
                                    difficulty = difficulty,
                                    isPremium = planType.equals("Premium", ignoreCase = true),
                                    questionIds = selectedQuestionIds.joinToString(","),
                                    markPerQuestion = 1.0f,
                                    questionMarksJson = qMarksJson,
                                    subjectMarksJson = subjMarksJson
                                )

                                viewModel.updateMockTest(updatedMock) {
                                    Toast.makeText(context, "Mock Test updated successfully!", Toast.LENGTH_SHORT).show()
                                    selectedMock = null
                                }
                            } else {
                                Toast.makeText(context, "Please enter mock title and select target exam(s).", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp)
                    ) {
                        Text("Save Changes", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }

    if (examDialogVisible) {
        AlertDialog(
            onDismissRequest = { examDialogVisible = false },
            title = { Text("Select Target Exams") },
            text = {
                LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                    items(exams) { exam ->
                        val isSelected = selectedExams.contains(exam.title)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (isSelected) selectedExams.remove(exam.title)
                                    else selectedExams.add(exam.title)
                                }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = { checked ->
                                    if (checked) {
                                        if (!selectedExams.contains(exam.title)) selectedExams.add(exam.title)
                                    } else {
                                        selectedExams.remove(exam.title)
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(exam.title, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { examDialogVisible = false }) {
                    Text("Done")
                }
            }
        )
    }

    if (showBatchImportDialog) {
        BatchImportMockQuestionsDialog(
            viewModel = viewModel,
            defaultSubject = "General Studies",
            defaultChapter = "General",
            defaultExamCategory = if (selectedExams.isNotEmpty()) selectedExams.joinToString(", ") else "",
            isMockPremium = planType == "Premium",
            onDismiss = { showBatchImportDialog = false },
            onQuestionsImported = { assignedIds, _ ->
                assignedIds.forEach { id ->
                    if (!selectedQuestionIds.contains(id)) {
                        selectedQuestionIds.add(id)
                    }
                }
            }
        )
    }

    if (mockToDelete != null) {
        AlertDialog(
            onDismissRequest = { mockToDelete = null },
            title = { Text("Confirm Delete") },
            text = { Text("Are you sure you want to delete this mock test?") },
            confirmButton = {
                Button(onClick = {
                    viewModel.requestOrDeleteMock(mockToDelete!!) { _, message ->
                        Toast.makeText(context, LocalMessageTranslator.translateGeneralMessage(context, message), Toast.LENGTH_LONG).show()
                    }
                    mockToDelete = null
                }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { mockToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
