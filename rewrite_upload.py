import sys

content = """package com.example.ui.screens

import kotlinx.coroutines.launch
import com.example.ui.components.SafeOutlinedTextField
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.QuestionEntity
import com.example.ui.viewmodel.JuktiViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SingleQuestionUploadScreen(viewModel: JuktiViewModel) {
    val editingQuestion by viewModel.editingQuestionForUpload.collectAsState()
    
    val exams by viewModel.examsList.collectAsState()
    val allSubjectsChapters by viewModel.allSubjectsChapters.collectAsState()
    
    val rawSubjects = remember(allSubjectsChapters) {
        val list = allSubjectsChapters.map { it.subject }.distinct().sorted()
        var updated = list
        if (!updated.contains("Transport Rule")) {
            updated = (updated + "Transport Rule").sorted()
        }
        updated
    }
    val subjectsList: List<String> = rawSubjects
    
    var subject by remember { mutableStateOf("") }
    var chapter by remember { mutableStateOf("") }
    var difficulty by remember { mutableStateOf("Medium") }
    var questionFor by remember { mutableStateOf("Free") }
    var questionTag by remember { mutableStateOf("Expected") }
    
    val rawChapters = remember(allSubjectsChapters, subject) {
        if (subject.isBlank()) {
            emptyList()
        } else {
            val normCurrentSubject = com.example.data.repository.normalizeSubjectName(subject)
            val fromList = allSubjectsChapters.filter { it.subject == normCurrentSubject }.map { it.chapter }.distinct().sorted()
            if (normCurrentSubject == "Transport Rule" && fromList.isEmpty()) {
                listOf(
                    "Traffic Signs, Signals & Road Safety",
                    "Motor Vehicles Act & Traffic Rules",
                    "Vehicle Safety, Violations & Penalties"
                )
            } else {
                fromList
            }
        }
    }
    val chaptersList: List<String> = rawChapters
    
    var questionEnglish by remember { mutableStateOf("") }
    var questionAssamese by remember { mutableStateOf("") }
    
    var optionAEnglish by remember { mutableStateOf("") }
    var optionBEnglish by remember { mutableStateOf("") }
    var optionCEnglish by remember { mutableStateOf("") }
    var optionDEnglish by remember { mutableStateOf("") }
    
    var optionAAssamese by remember { mutableStateOf("") }
    var optionBAssamese by remember { mutableStateOf("") }
    var optionCAssamese by remember { mutableStateOf("") }
    var optionDAssamese by remember { mutableStateOf("") }
    
    var correctOption by remember { mutableStateOf("A") }
    var explanationEnglish by remember { mutableStateOf("") }
    var explanationAssamese by remember { mutableStateOf("") }
    
    val selectedExams = remember { mutableStateListOf<String>() }
    var duplicateError by remember { mutableStateOf<String?>(null) }
    var isDeploying by remember { mutableStateOf(false) }

    var subjectExpanded by remember { mutableStateOf(false) }
    var chapterExpanded by remember { mutableStateOf(false) }
    var difficultyExpanded by remember { mutableStateOf(false) }
    var questionForExpanded by remember { mutableStateOf(false) }
    var questionTagExpanded by remember { mutableStateOf(false) }
    var correctOptionExpanded by remember { mutableStateOf(false) }
    var targetExamDialogVisible by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(editingQuestion) {
        editingQuestion?.let { q ->
            subject = q.subject
            chapter = q.topic
            difficulty = q.difficulty
            questionFor = if (q.isPremium) "Premium" else "Free"
            if (q.questionType.startsWith("PYQ", ignoreCase = true)) {
                questionTag = "PYQ"
            } else {
                questionTag = q.questionType.ifBlank { "Expected" }
            }
            
            questionEnglish = q.questionEn
            questionAssamese = q.questionAs
            
            optionAEnglish = q.optionAEn
            optionBEnglish = q.optionBEn
            optionCEnglish = q.optionCEn
            optionDEnglish = q.optionDEn
            
            optionAAssamese = q.optionAAs
            optionBAssamese = q.optionBAs
            optionCAssamese = q.optionCAs
            optionDAssamese = q.optionDAs
            
            correctOption = when(q.correctOptionIndex) {
                0 -> "A"
                1 -> "B"
                2 -> "C"
                3 -> "D"
                else -> "A"
            }
            
            explanationEnglish = q.explanationEn
            explanationAssamese = q.explanationAs
            
            selectedExams.clear()
            if (q.examCategory.isNotBlank()) {
                selectedExams.addAll(q.examCategory.split(",").map { it.trim() })
            }
        }
    }

    if (duplicateError != null) {
        AlertDialog(
            onDismissRequest = { duplicateError = null },
            title = { Text("Duplicate Question") },
            text = {
                Text(
                    "A similar question already exists in the database:\\n\\n" +
                    "$duplicateError\\n\\n" +
                    "Import skipped to prevent duplicates."
                )
            },
            confirmButton = {
                TextButton(onClick = { duplicateError = null }) {
                    Text("OK")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            com.example.ui.components.JuktiTopAppBar(
                title = if (editingQuestion != null) "Edit Question" else "Manual Entry",
                onNavigationClick = { viewModel.navigateTo(com.example.ui.viewmodel.Screen.MANAGE_QBANK) },
                actions = {
                    IconButton(onClick = { viewModel.navigateTo(com.example.ui.viewmodel.Screen.MANAGE_SUBJECTS_CHAPTERS) }) {
                        Icon(Icons.Default.Category, contentDescription = "Manage Subjects & Chapters")
                    }
                }
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
                ExposedDropdownMenuBox(
                    expanded = subjectExpanded,
                    onExpandedChange = { subjectExpanded = it }
                ) {
                    SafeOutlinedTextField(
                        value = subject,
                        onValueChange = {},
                        label = { Text("Subject") },
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjectExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = subjectExpanded,
                        onDismissRequest = { subjectExpanded = false }
                    ) {
                        if (subjectsList.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("No subjects available") },
                                onClick = { subjectExpanded = false }
                            )
                        }
                        subjectsList.forEach { selSubject ->
                            DropdownMenuItem(
                                text = { Text(selSubject) },
                                onClick = {
                                    subject = selSubject
                                    chapter = ""
                                    subjectExpanded = false
                                }
                            )
                        }
                    }
                }
            }
            
            item {
                ExposedDropdownMenuBox(
                    expanded = chapterExpanded,
                    onExpandedChange = { chapterExpanded = it }
                ) {
                    SafeOutlinedTextField(
                        value = chapter,
                        onValueChange = {},
                        label = { Text("Chapter") },
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = chapterExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = chapterExpanded,
                        onDismissRequest = { chapterExpanded = false }
                    ) {
                        if (chaptersList.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("No chapters available") },
                                onClick = { chapterExpanded = false }
                            )
                        }
                        chaptersList.forEach { selChapter ->
                            DropdownMenuItem(
                                text = { Text(selChapter) },
                                onClick = {
                                    chapter = selChapter
                                    chapterExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            item {
                Box(modifier = Modifier.clickable { targetExamDialogVisible = true }) {
                    SafeOutlinedTextField(
                        value = if (selectedExams.isEmpty()) "Select Target Exams..." else selectedExams.joinToString(", "),
                        onValueChange = {},
                        label = { Text("Target Exams (Select Multiple)") },
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = false,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ExposedDropdownMenuBox(
                        expanded = difficultyExpanded,
                        onExpandedChange = { difficultyExpanded = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        SafeOutlinedTextField(
                            value = difficulty,
                            onValueChange = {},
                            label = { Text("Difficulty") },
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = difficultyExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = difficultyExpanded,
                            onDismissRequest = { difficultyExpanded = false }
                        ) {
                            listOf("Easy", "Medium", "Hard").forEach { selDifficulty ->
                                DropdownMenuItem(
                                    text = { Text(selDifficulty) },
                                    onClick = {
                                        difficulty = selDifficulty
                                        difficultyExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    ExposedDropdownMenuBox(
                        expanded = questionForExpanded,
                        onExpandedChange = { questionForExpanded = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        SafeOutlinedTextField(
                            value = questionFor,
                            onValueChange = {},
                            label = { Text("Access") },
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = questionForExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = questionForExpanded,
                            onDismissRequest = { questionForExpanded = false }
                        ) {
                            listOf("Free", "Premium").forEach { selFor ->
                                DropdownMenuItem(
                                    text = { Text(selFor) },
                                    onClick = {
                                        questionFor = selFor
                                        questionForExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            item {
                ExposedDropdownMenuBox(
                    expanded = questionTagExpanded,
                    onExpandedChange = { questionTagExpanded = it }
                ) {
                    SafeOutlinedTextField(
                        value = questionTag,
                        onValueChange = {},
                        label = { Text("Question Tag") },
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = questionTagExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = questionTagExpanded,
                        onDismissRequest = { questionTagExpanded = false }
                    ) {
                        listOf("Expected", "PYQ").forEach { tag ->
                            DropdownMenuItem(
                                text = { Text(tag) },
                                onClick = {
                                    questionTag = tag
                                    questionTagExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            item {
                Text("Question Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            item {
                SafeOutlinedTextField(
                    value = questionEnglish,
                    onValueChange = { questionEnglish = it },
                    label = { Text("Question (English) *") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
            item {
                SafeOutlinedTextField(
                    value = questionAssamese,
                    onValueChange = { questionAssamese = it },
                    label = { Text("Question (Assamese)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
            
            item {
                Text("Options", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SafeOutlinedTextField(value = optionAEnglish, onValueChange = { optionAEnglish = it }, label = { Text("Option A (En)") }, modifier = Modifier.weight(1f))
                    SafeOutlinedTextField(value = optionAAssamese, onValueChange = { optionAAssamese = it }, label = { Text("Option A (As)") }, modifier = Modifier.weight(1f))
                }
            }
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SafeOutlinedTextField(value = optionBEnglish, onValueChange = { optionBEnglish = it }, label = { Text("Option B (En)") }, modifier = Modifier.weight(1f))
                    SafeOutlinedTextField(value = optionBAssamese, onValueChange = { optionBAssamese = it }, label = { Text("Option B (As)") }, modifier = Modifier.weight(1f))
                }
            }
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SafeOutlinedTextField(value = optionCEnglish, onValueChange = { optionCEnglish = it }, label = { Text("Option C (En)") }, modifier = Modifier.weight(1f))
                    SafeOutlinedTextField(value = optionCAssamese, onValueChange = { optionCAssamese = it }, label = { Text("Option C (As)") }, modifier = Modifier.weight(1f))
                }
            }
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SafeOutlinedTextField(value = optionDEnglish, onValueChange = { optionDEnglish = it }, label = { Text("Option D (En)") }, modifier = Modifier.weight(1f))
                    SafeOutlinedTextField(value = optionDAssamese, onValueChange = { optionDAssamese = it }, label = { Text("Option D (As)") }, modifier = Modifier.weight(1f))
                }
            }

            item {
                ExposedDropdownMenuBox(
                    expanded = correctOptionExpanded,
                    onExpandedChange = { correctOptionExpanded = it }
                ) {
                    SafeOutlinedTextField(
                        value = correctOption,
                        onValueChange = {},
                        label = { Text("Correct Option") },
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = correctOptionExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = correctOptionExpanded,
                        onDismissRequest = { correctOptionExpanded = false }
                    ) {
                        listOf("A", "B", "C", "D").forEach { selectionOption ->
                            DropdownMenuItem(
                                text = { Text(selectionOption) },
                                onClick = {
                                    correctOption = selectionOption
                                    correctOptionExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            item {
                SafeOutlinedTextField(
                    value = explanationEnglish,
                    onValueChange = { explanationEnglish = it },
                    label = { Text("Explanation (English)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
            item {
                SafeOutlinedTextField(
                    value = explanationAssamese,
                    onValueChange = { explanationAssamese = it },
                    label = { Text("Explanation (Assamese)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
            item {
                Button(
                    onClick = {
                        if (subject.isNotBlank() && chapter.isNotBlank() && selectedExams.isNotEmpty() && questionEnglish.isNotBlank() && correctOption.isNotBlank()) {
                            isDeploying = true
                            
                            val duplicateKey = com.example.util.generateDuplicateKey(questionEnglish)
                            
                            scope.launch {
                                val duplicate = viewModel.getQuestionByDuplicateKey(duplicateKey)
                                if (duplicate != null && (editingQuestion == null || editingQuestion?.duplicateKey != duplicateKey)) {
                                    duplicateError = duplicate.questionEn
                                    isDeploying = false
                                    return@launch
                                }

                                val correctOptionIndex = when(correctOption) {
                                    "A" -> 0
                                    "B" -> 1
                                    "C" -> 2
                                    "D" -> 3
                                    else -> 0
                                }
                                
                                val finalQuestionTag = questionTag
                                
                                val normSubject = com.example.data.repository.normalizeSubjectName(subject)
                                val normChapter = com.example.data.repository.normalizeChapterName(chapter, normSubject)
                                
                                val newQuestion = QuestionEntity(
                                    id = editingQuestion?.id ?: 0L,
                                    updatedAt = System.currentTimeMillis(),
                                    subject = normSubject,
                                    topic = normChapter,
                                    difficulty = difficulty,
                                    questionEn = questionEnglish.trim(),
                                    questionAs = questionAssamese.trim(),
                                    optionAEn = optionAEnglish.trim(),
                                    optionBEn = optionBEnglish.trim(),
                                    optionCEn = optionCEnglish.trim(),
                                    optionDEn = optionDEnglish.trim(),
                                    optionAAs = optionAAssamese.trim(),
                                    optionBAs = optionBAssamese.trim(),
                                    optionCAs = optionCAssamese.trim(),
                                    optionDAs = optionDAssamese.trim(),
                                    correctOptionIndex = correctOptionIndex,
                                    explanationEn = explanationEnglish.trim(),
                                    explanationAs = explanationAssamese.trim(),
                                    examCategory = selectedExams.joinToString(", "),
                                    isPremium = questionFor.equals("Premium", ignoreCase = true),
                                    questionType = finalQuestionTag,
                                    duplicateKey = if (editingQuestion == null) duplicateKey else editingQuestion!!.duplicateKey,
                                    status = editingQuestion?.status ?: "ACTIVE"
                                )
                                
                                if (editingQuestion != null) {
                                    viewModel.updateQuestion(newQuestion)
                                    isDeploying = false
                                    Toast.makeText(context, "Question updated successfully!", Toast.LENGTH_SHORT).show()
                                    viewModel.startEditingQuestion(null)
                                    viewModel.navigateTo(com.example.ui.viewmodel.Screen.MANAGE_QBANK)
                                } else {
                                    viewModel.addQuestion(newQuestion) {
                                        isDeploying = false
                                        Toast.makeText(context, "Question deployed successfully!", Toast.LENGTH_SHORT).show()
                                        
                                        // Reset fields
                                        questionEnglish = ""
                                        questionAssamese = ""
                                        optionAEnglish = ""
                                        optionBEnglish = ""
                                        optionCEnglish = ""
                                        optionDEnglish = ""
                                        optionAAssamese = ""
                                        optionBAssamese = ""
                                        optionCAssamese = ""
                                        optionDAssamese = ""
                                        explanationEnglish = ""
                                        explanationAssamese = ""
                                    }
                                }
                            }
                        } else {
                            Toast.makeText(context, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    enabled = !isDeploying
                ) {
                    if (isDeploying) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (editingQuestion != null) "Updating..." else "Deploying...")
                    } else {
                        Text(if (editingQuestion != null) "Update Question" else "Deploy Question")
                    }
                }
            }
        }
    }

    if (targetExamDialogVisible) {
        AlertDialog(
            onDismissRequest = { targetExamDialogVisible = false },
            title = { Text("Select Target Exams") },
            text = {
                if (exams.isEmpty()) {
                    Text("No exams available. Please add exams in Manage Exams first.", color = MaterialTheme.colorScheme.error)
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                        items(exams) { exam ->
                            val isSelected = selectedExams.contains(exam.title)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (isSelected) {
                                            selectedExams.remove(exam.title)
                                        } else {
                                            selectedExams.add(exam.title)
                                        }
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
                }
            },
            confirmButton = {
                TextButton(onClick = { targetExamDialogVisible = false }) {
                    Text("Done")
                }
            }
        )
    }
}
"""

with open('app/src/main/java/com/example/ui/screens/SingleQuestionUploadScreen.kt', 'w') as f:
    f.write(content)
