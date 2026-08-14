package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Add
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
import com.example.data.local.QuestionEntity
import com.example.ui.viewmodel.JuktiViewModel
import com.example.ui.components.SafeOutlinedTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateMockScreen(viewModel: JuktiViewModel) {
    val context = LocalContext.current
    val exams by viewModel.examsList.collectAsState()
    val allSubjectsChapters by viewModel.allSubjectsChapters.collectAsState()
    val allQuestions by viewModel.questions.collectAsState()

    var mockTitleEn by remember { mutableStateOf("") }
    var mockTitleAs by remember { mutableStateOf("") }
    val selectedExams = remember { mutableStateListOf<String>() }
    var examDialogVisible by remember { mutableStateOf(false) }

    var durationMinutes by remember { mutableStateOf("90") }
    var markPerQuestion by remember { mutableStateOf("1.0") }
    
    var negativeMarking by remember { mutableStateOf("0.25 Marks") }
    var negativeMarkingExpanded by remember { mutableStateOf(false) }
    val negativeMarkingOptions = listOf("0.25 Marks", "0.5 Marks", "1.0 Marks", "None")

    var testType by remember { mutableStateOf("Full-Length") }
    var testTypeExpanded by remember { mutableStateOf(false) }
    val testTypeOptions = listOf("Full-Length", "Subject-wise", "Chapter-wise")

    var selectedMockSubject by remember { mutableStateOf("") }
    var mockSubjectExpanded by remember { mutableStateOf(false) }
    var selectedMockChapter by remember { mutableStateOf("") }
    var mockChapterExpanded by remember { mutableStateOf(false) }

    var planType by remember { mutableStateOf("Free") }
    var planTypeExpanded by remember { mutableStateOf(false) }
    val planTypeOptions = listOf("Free", "Premium")

    // Question Bank Selection state
    var selectedSubjectFilter by remember { mutableStateOf("All Subjects") }
    var subjectFilterExpanded by remember { mutableStateOf(false) }
    var questionSearchQuery by remember { mutableStateOf("") }
    val selectedQuestionIds = remember { mutableStateListOf<Long>() }

    // Add Question Dialog state
    var showAddQuestionDialog by remember { mutableStateOf(false) }
    var qSubject by remember { mutableStateOf("") }
    var qSubjectExpanded by remember { mutableStateOf(false) }
    var qChapterExpanded by remember { mutableStateOf(false) }
    val rawChapters = allSubjectsChapters.filter { it.subject == qSubject }.map { it.chapter }.distinct()
    val qChaptersList: List<String> = if (rawChapters.isEmpty()) listOf("General") else rawChapters
    var qChapter by remember { mutableStateOf("") }
    val qSelectedExams = remember { mutableStateListOf<String>() }
    var qTargetExamDialogVisible by remember { mutableStateOf(false) }

    var qDifficultyExpanded by remember { mutableStateOf(false) }
    var qDifficulty by remember { mutableStateOf("Medium") }

    var qQuestionForExpanded by remember { mutableStateOf(false) }
    var qQuestionFor by remember { mutableStateOf("Free") }

    var qQuestionTagExpanded by remember { mutableStateOf(false) }
    var qQuestionTag by remember { mutableStateOf("Expected") }

    var qPyqExamName by remember { mutableStateOf("") }
    var qPyqYear by remember { mutableStateOf("") }

    var qQuestionEnglish by remember { mutableStateOf("") }
    var qQuestionAssamese by remember { mutableStateOf("") }

    var qOptionAEnglish by remember { mutableStateOf("") }
    var qOptionBEnglish by remember { mutableStateOf("") }
    var qOptionCEnglish by remember { mutableStateOf("") }
    var qOptionDEnglish by remember { mutableStateOf("") }

    var qOptionAAssamese by remember { mutableStateOf("") }
    var qOptionBAssamese by remember { mutableStateOf("") }
    var qOptionCAssamese by remember { mutableStateOf("") }
    var qOptionDAssamese by remember { mutableStateOf("") }

    var qCorrectOptionExpanded by remember { mutableStateOf(false) }
    var qCorrectOption by remember { mutableStateOf("A") }

    var qExplanationEnglish by remember { mutableStateOf("") }
    var qExplanationAssamese by remember { mutableStateOf("") }

    var addToQBank by remember { mutableStateOf(true) }
    var isUploadingQ by remember { mutableStateOf(false) }

        val subjectsList = listOf("All Subjects") + allSubjectsChapters.map { it.subject }.distinct()

    val filteredQuestions = allQuestions.filter { q ->
        val matchesSubject = selectedSubjectFilter == "All Subjects" || q.subject.equals(selectedSubjectFilter, ignoreCase = true)
        val matchesSearch = questionSearchQuery.isBlank() || 
            q.questionEn.contains(questionSearchQuery, ignoreCase = true) ||
            q.subject.contains(questionSearchQuery, ignoreCase = true) ||
            q.topic.contains(questionSearchQuery, ignoreCase = true)
        matchesSubject && matchesSearch
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Mock Test", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo(com.example.ui.viewmodel.Screen.MANAGE_MOCK) }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
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
                    SafeOutlinedTextField(
                        value = markPerQuestion,
                        onValueChange = { markPerQuestion = it },
                        label = { Text("Mark / Q *") },
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
                    // Negative Marking Dropdown
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

                    // Test Type Dropdown
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
                // Plan Dropdown
                ExposedDropdownMenuBox(
                    expanded = planTypeExpanded,
                    onExpandedChange = { planTypeExpanded = !planTypeExpanded }
                ) {
                    SafeOutlinedTextField(
                        value = planType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Plan (Free/Premium)") },
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
            }

            if (testType == "Subject-wise" || testType == "Chapter-wise") {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val mockSubjectsList = allSubjectsChapters.map { it.subject }.distinct()
                        ExposedDropdownMenuBox(
                            expanded = mockSubjectExpanded,
                            onExpandedChange = { mockSubjectExpanded = !mockSubjectExpanded },
                            modifier = Modifier.weight(1f)
                        ) {
                            SafeOutlinedTextField(
                                value = selectedMockSubject,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Mock Subject") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = mockSubjectExpanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = mockSubjectExpanded,
                                onDismissRequest = { mockSubjectExpanded = false }
                            ) {
                                mockSubjectsList.forEach { subj ->
                                    DropdownMenuItem(
                                        text = { Text(subj) },
                                        onClick = {
                                            selectedMockSubject = subj
                                            selectedMockChapter = ""
                                            mockSubjectExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        if (testType == "Chapter-wise") {
                            val mockChaptersList = allSubjectsChapters.filter { it.subject == selectedMockSubject }.map { it.chapter }.distinct()
                            ExposedDropdownMenuBox(
                                expanded = mockChapterExpanded,
                                onExpandedChange = { mockChapterExpanded = !mockChapterExpanded },
                                modifier = Modifier.weight(1f)
                            ) {
                                SafeOutlinedTextField(
                                    value = selectedMockChapter,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Mock Chapter") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = mockChapterExpanded) },
                                    modifier = Modifier.menuAnchor().fillMaxWidth()
                                )
                                ExposedDropdownMenu(
                                    expanded = mockChapterExpanded,
                                    onDismissRequest = { mockChapterExpanded = false }
                                ) {
                                    mockChaptersList.forEach { chap ->
                                        DropdownMenuItem(
                                            text = { Text(chap) },
                                            onClick = {
                                                selectedMockChapter = chap
                                                mockChapterExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Select Questions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    OutlinedButton(onClick = { showAddQuestionDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add New Question")
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
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
                    // Subject Dropdown Filter
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
                        items(filteredQuestions, key = { it.id }) { q ->
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
                                    Text(q.questionEn, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, maxLines = 2)
                                    Text("[${q.subject}]", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        if (mockTitleEn.isBlank() || selectedExams.isEmpty()) {
                            Toast.makeText(context, "Please enter mock title and select target exam(s).", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        
                        var finalSubjectOrChapter = selectedSubjectFilter
                        var finalQuestionIds = selectedQuestionIds.toList()
                        
                        if (testType == "Subject-wise") {
                            if (selectedMockSubject.isBlank()) {
                                Toast.makeText(context, "Please select a Subject.", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            val questionsInSubject = allQuestions.filter { it.subject.equals(selectedMockSubject, ignoreCase = true) }
                            if (questionsInSubject.isEmpty()) {
                                Toast.makeText(context, "No questions found for the selected Subject.", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            finalSubjectOrChapter = selectedMockSubject
                            finalQuestionIds = emptyList() // Will be fetched dynamically
                        } else if (testType == "Chapter-wise") {
                            if (selectedMockSubject.isBlank() || selectedMockChapter.isBlank()) {
                                Toast.makeText(context, "Please select both Subject and Chapter.", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            val questionsInChapter = allQuestions.filter { 
                                it.subject.equals(selectedMockSubject, ignoreCase = true) && it.topic.equals(selectedMockChapter, ignoreCase = true)
                            }
                            if (questionsInChapter.isEmpty()) {
                                Toast.makeText(context, "No questions found for the selected Chapter.", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            finalSubjectOrChapter = "$selectedMockSubject||$selectedMockChapter"
                            finalQuestionIds = emptyList() // Will be fetched dynamically
                        } else {
                            // Full-Length mock requires manually selected questions or a default number
                            if (finalQuestionIds.isEmpty()) {
                                Toast.makeText(context, "Please add questions for this Full-Length mock.", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                        }

                        val duration = durationMinutes.toIntOrNull() ?: 90
                        val markPerQ = markPerQuestion.toFloatOrNull() ?: 1.0f
                        
                        // For Subject-wise and Chapter-wise, totalQ could be the total number of questions they want in the mock, or the actual number of questions in that category.
                        // Wait, they have a field for `totalQuestionsText` maybe? There is no field to specify how many questions to pick. Let's just use the count of questions available.
                        val totalQ = if (testType == "Subject-wise") {
                            allQuestions.count { it.subject.equals(selectedMockSubject, ignoreCase = true) }
                        } else if (testType == "Chapter-wise") {
                            allQuestions.count { it.subject.equals(selectedMockSubject, ignoreCase = true) && it.topic.equals(selectedMockChapter, ignoreCase = true) }
                        } else {
                            finalQuestionIds.size.coerceAtLeast(1)
                        }

                        val totalMarksInt = (totalQ * markPerQ).toInt()

                        val newMock = MockTestEntity(
                            titleEn = mockTitleEn.trim(),
                            titleAs = mockTitleAs.trim().ifBlank { mockTitleEn.trim() },
                            category = selectedExams.joinToString(", "),
                            durationMinutes = duration,
                            totalQuestions = totalQ,
                            totalMarks = totalMarksInt,
                            testType = testType,
                            subjectOrChapter = finalSubjectOrChapter,
                            negativeMarking = negativeMarking,
                            isPremium = planType.equals("Premium", ignoreCase = true),
                            questionIds = finalQuestionIds.joinToString(","),
                            markPerQuestion = markPerQ
                        )

                        viewModel.addMockTest(newMock) {
                            Toast.makeText(context, "Mock Test created successfully!", Toast.LENGTH_SHORT).show()
                            viewModel.navigateTo(com.example.ui.viewmodel.Screen.MANAGE_MOCK)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Text("Publish Mock Test", style = MaterialTheme.typography.titleMedium)
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

    if (showAddQuestionDialog) {
        AlertDialog(
            onDismissRequest = { showAddQuestionDialog = false },
            title = { Text("Add New Question", fontWeight = FontWeight.Bold) },
            text = {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 500.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        ExposedDropdownMenuBox(
                            expanded = qSubjectExpanded,
                            onExpandedChange = { qSubjectExpanded = !qSubjectExpanded }
                        ) {
                            SafeOutlinedTextField(
                                value = qSubject,
                                onValueChange = { qSubject = it },
                                label = { Text("Subject *") },
                                modifier = Modifier.fillMaxWidth().menuAnchor(),
                                singleLine = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = qSubjectExpanded) }
                            )
                            ExposedDropdownMenu(
                                expanded = qSubjectExpanded,
                                onDismissRequest = { qSubjectExpanded = false }
                            ) {
                                subjectsList.filter { it != "All Subjects" }.forEach { subj ->
                                    DropdownMenuItem(
                                        text = { Text(subj) },
                                        onClick = {
                                            qSubject = subj
                                            qSubjectExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                    item {
                        SafeOutlinedTextField(
                            value = qChapter,
                            onValueChange = { qChapter = it },
                            label = { Text("Chapter *") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                    item {
                        Box(modifier = Modifier.fillMaxWidth().clickable { qTargetExamDialogVisible = true }) {
                            SafeOutlinedTextField(
                                value = if (qSelectedExams.isEmpty()) "Select Target Exams..." else qSelectedExams.joinToString(", "),
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Target Exams (Multiple)") },
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
                        ExposedDropdownMenuBox(
                            expanded = qDifficultyExpanded,
                            onExpandedChange = { qDifficultyExpanded = !qDifficultyExpanded }
                        ) {
                            SafeOutlinedTextField(
                                value = qDifficulty,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Difficulty") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = qDifficultyExpanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = qDifficultyExpanded,
                                onDismissRequest = { qDifficultyExpanded = false }
                            ) {
                                listOf("Easy", "Medium", "Hard").forEach { selectionOption ->
                                    DropdownMenuItem(
                                        text = { Text(selectionOption) },
                                        onClick = {
                                            qDifficulty = selectionOption
                                            qDifficultyExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                    item {
                        ExposedDropdownMenuBox(
                            expanded = qQuestionForExpanded,
                            onExpandedChange = { qQuestionForExpanded = !qQuestionForExpanded }
                        ) {
                            SafeOutlinedTextField(
                                value = qQuestionFor,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Question For") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = qQuestionForExpanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = qQuestionForExpanded,
                                onDismissRequest = { qQuestionForExpanded = false }
                            ) {
                                listOf("Free", "Premium").forEach { selectionOption ->
                                    DropdownMenuItem(
                                        text = { Text(selectionOption) },
                                        onClick = {
                                            qQuestionFor = selectionOption
                                            qQuestionForExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                    item {
                        ExposedDropdownMenuBox(
                            expanded = qQuestionTagExpanded,
                            onExpandedChange = { qQuestionTagExpanded = !qQuestionTagExpanded }
                        ) {
                            SafeOutlinedTextField(
                                value = qQuestionTag,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Question Tag") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = qQuestionTagExpanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = qQuestionTagExpanded,
                                onDismissRequest = { qQuestionTagExpanded = false }
                            ) {
                                listOf("PYQ", "Expected").forEach { selectionOption ->
                                    DropdownMenuItem(
                                        text = { Text(selectionOption) },
                                        onClick = {
                                            qQuestionTag = selectionOption
                                            qQuestionTagExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                    if (qQuestionTag == "PYQ") {
                        item {
                            SafeOutlinedTextField(
                                value = qPyqExamName,
                                onValueChange = { qPyqExamName = it },
                                label = { Text("Exam Name (e.g., APSC CCE)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        item {
                            SafeOutlinedTextField(
                                value = qPyqYear,
                                onValueChange = { qPyqYear = it },
                                label = { Text("Year (e.g., 2023)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    item {
                        Text("Question Details", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    }
                    item {
                        SafeOutlinedTextField(
                            value = qQuestionEnglish,
                            onValueChange = { qQuestionEnglish = it },
                            label = { Text("Question in English *") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3
                        )
                    }
                    item {
                        SafeOutlinedTextField(
                            value = qQuestionAssamese,
                            onValueChange = { qQuestionAssamese = it },
                            label = { Text("Question in Assamese") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3
                        )
                    }
                    item {
                        Text("Options (English)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    }
                    item { SafeOutlinedTextField(value = qOptionAEnglish, onValueChange = { qOptionAEnglish = it }, label = { Text("Option A *") }, modifier = Modifier.fillMaxWidth()) }
                    item { SafeOutlinedTextField(value = qOptionBEnglish, onValueChange = { qOptionBEnglish = it }, label = { Text("Option B *") }, modifier = Modifier.fillMaxWidth()) }
                    item { SafeOutlinedTextField(value = qOptionCEnglish, onValueChange = { qOptionCEnglish = it }, label = { Text("Option C") }, modifier = Modifier.fillMaxWidth()) }
                    item { SafeOutlinedTextField(value = qOptionDEnglish, onValueChange = { qOptionDEnglish = it }, label = { Text("Option D") }, modifier = Modifier.fillMaxWidth()) }
                    
                    item {
                        Text("Options (Assamese)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    }
                    item { SafeOutlinedTextField(value = qOptionAAssamese, onValueChange = { qOptionAAssamese = it }, label = { Text("Option A") }, modifier = Modifier.fillMaxWidth()) }
                    item { SafeOutlinedTextField(value = qOptionBAssamese, onValueChange = { qOptionBAssamese = it }, label = { Text("Option B") }, modifier = Modifier.fillMaxWidth()) }
                    item { SafeOutlinedTextField(value = qOptionCAssamese, onValueChange = { qOptionCAssamese = it }, label = { Text("Option C") }, modifier = Modifier.fillMaxWidth()) }
                    item { SafeOutlinedTextField(value = qOptionDAssamese, onValueChange = { qOptionDAssamese = it }, label = { Text("Option D") }, modifier = Modifier.fillMaxWidth()) }

                    item {
                        ExposedDropdownMenuBox(
                            expanded = qCorrectOptionExpanded,
                            onExpandedChange = { qCorrectOptionExpanded = !qCorrectOptionExpanded }
                        ) {
                            SafeOutlinedTextField(
                                value = qCorrectOption,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Correct Option *") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = qCorrectOptionExpanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = qCorrectOptionExpanded,
                                onDismissRequest = { qCorrectOptionExpanded = false }
                            ) {
                                listOf("A", "B", "C", "D").forEach { selectionOption ->
                                    DropdownMenuItem(
                                        text = { Text(selectionOption) },
                                        onClick = {
                                            qCorrectOption = selectionOption
                                            qCorrectOptionExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                    item {
                        SafeOutlinedTextField(
                            value = qExplanationEnglish,
                            onValueChange = { qExplanationEnglish = it },
                            label = { Text("Explanation (English)") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2
                        )
                    }
                    item {
                        SafeOutlinedTextField(
                            value = qExplanationAssamese,
                            onValueChange = { qExplanationAssamese = it },
                            label = { Text("Explanation (Assamese)") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2
                        )
                    }
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { addToQBank = !addToQBank },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = addToQBank,
                                onCheckedChange = { addToQBank = it }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Also add this question to Question Bank", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (qSubject.isNotBlank() && qChapter.isNotBlank() && qQuestionEnglish.isNotBlank() && qOptionAEnglish.isNotBlank() && qOptionBEnglish.isNotBlank()) {
                            isUploadingQ = true
                            val correctOptionIndex = when(qCorrectOption) {
                                "A" -> 0
                                "B" -> 1
                                "C" -> 2
                                "D" -> 3
                                else -> 0
                            }
                            val finalQuestionTag = if (qQuestionTag == "PYQ") {
                                val examNameStr = qPyqExamName.trim()
                                val yearStr = qPyqYear.trim()
                                if (examNameStr.isNotBlank() || yearStr.isNotBlank()) "$examNameStr $yearStr".trim() else "PYQ"
                            } else {
                                "Expected"
                            }
                            val newQ = QuestionEntity(
                                subject = qSubject.trim(),
                                topic = qChapter.trim(),
                                difficulty = qDifficulty,
                                questionEn = qQuestionEnglish.trim(),
                                questionAs = qQuestionAssamese.trim(),
                                optionAEn = qOptionAEnglish.trim(),
                                optionBEn = qOptionBEnglish.trim(),
                                optionCEn = qOptionCEnglish.trim(),
                                optionDEn = qOptionDEnglish.trim(),
                                optionAAs = qOptionAAssamese.trim(),
                                optionBAs = qOptionBAssamese.trim(),
                                optionCAs = qOptionCAssamese.trim(),
                                optionDAs = qOptionDAssamese.trim(),
                                correctOptionIndex = correctOptionIndex,
                                explanationEn = qExplanationEnglish.trim(),
                                explanationAs = qExplanationAssamese.trim(),
                                examCategory = if (qSelectedExams.isNotEmpty()) qSelectedExams.joinToString(", ") else "ADRE",
                                isPremium = qQuestionFor.equals("Premium", ignoreCase = true),
                                questionType = finalQuestionTag
                            )
                            viewModel.addQuestion(newQ) { newId ->
                                isUploadingQ = false
                                Toast.makeText(context, "Question added & selected in mock test!", Toast.LENGTH_SHORT).show()
                                if (newId > 0 && !selectedQuestionIds.contains(newId)) {
                                    selectedQuestionIds.add(newId)
                                }
                                showAddQuestionDialog = false
                                // Reset form fields
                                qQuestionEnglish = ""
                                qQuestionAssamese = ""
                                qOptionAEnglish = ""
                                qOptionBEnglish = ""
                                qOptionCEnglish = ""
                                qOptionDEnglish = ""
                                qOptionAAssamese = ""
                                qOptionBAssamese = ""
                                qOptionCAssamese = ""
                                qOptionDAssamese = ""
                                qExplanationEnglish = ""
                                qExplanationAssamese = ""
                            }
                        } else {
                            Toast.makeText(context, "Please fill in all required fields (Subject, Chapter, Question, Option A, Option B)", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = !isUploadingQ
                ) {
                    if (isUploadingQ) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Adding...")
                    } else {
                        Text("Add Question")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddQuestionDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (qTargetExamDialogVisible) {
        AlertDialog(
            onDismissRequest = { qTargetExamDialogVisible = false },
            title = { Text("Select Target Exams") },
            text = {
                LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                    items(exams) { exam ->
                        val isSelected = qSelectedExams.contains(exam.title)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (isSelected) {
                                        qSelectedExams.remove(exam.title)
                                    } else {
                                        qSelectedExams.add(exam.title)
                                    }
                                }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = { checked ->
                                    if (checked) {
                                        if (!qSelectedExams.contains(exam.title)) qSelectedExams.add(exam.title)
                                    } else {
                                        qSelectedExams.remove(exam.title)
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
                Button(onClick = { qTargetExamDialogVisible = false }) {
                    Text("Done")
                }
            }
        )
    }
}
