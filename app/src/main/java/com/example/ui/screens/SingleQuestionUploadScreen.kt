package com.example.ui.screens

import com.example.ui.components.SafeOutlinedTextField

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
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
    var subject by remember { mutableStateOf("") }
    var chapter by remember { mutableStateOf("") }
    val selectedExams = remember { mutableStateListOf<String>() }
    var targetExamDialogVisible by remember { mutableStateOf(false) }
    val exams by viewModel.examsList.collectAsState()
    val allSubjectsChapters by viewModel.allSubjectsChapters.collectAsState()
    val rawSubjects = allSubjectsChapters.map { it.subject }.filter { it.isNotBlank() }.distinct()
    val subjectsList: List<String> = rawSubjects
    var subjectExpanded by remember { mutableStateOf(false) }
    var chapterExpanded by remember { mutableStateOf(false) }
    val rawChapters = allSubjectsChapters.filter { it.subject == subject }.map { it.chapter }.filter { it.isNotBlank() }.distinct()
    val chaptersList: List<String> = rawChapters
    
    var difficultyExpanded by remember { mutableStateOf(false) }
    var difficulty by remember { mutableStateOf("Medium") }
    
    var questionForExpanded by remember { mutableStateOf(false) }
    var questionFor by remember { mutableStateOf("Free") }
    
    var questionTagExpanded by remember { mutableStateOf(false) }
    var questionTag by remember { mutableStateOf("Expected") }
    
    var pyqExamName by remember { mutableStateOf("") }
    var pyqYear by remember { mutableStateOf("") }
    
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
    
    var correctOptionExpanded by remember { mutableStateOf(false) }
    var correctOption by remember { mutableStateOf("A") }
    
    var explanationEnglish by remember { mutableStateOf("") }
    var explanationAssamese by remember { mutableStateOf("") }

    val context = LocalContext.current
    var isDeploying by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            com.example.ui.components.JuktiTopAppBar(
                title = "Upload Single Question",
                onBackClick = { viewModel.navigateTo(com.example.ui.viewmodel.Screen.MANAGE_QBANK) }
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
                    onExpandedChange = { subjectExpanded = !subjectExpanded }
                ) {
                    SafeOutlinedTextField(
                        value = subject,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Subject") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjectExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = subjectExpanded,
                        onDismissRequest = { subjectExpanded = false }
                    ) {
                        if (subjectsList.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("No subjects available in workspace") },
                                onClick = { subjectExpanded = false }
                            )
                        } else {
                            subjectsList.forEach { selSubject ->
                                DropdownMenuItem(
                                    text = { Text(selSubject) },
                                    onClick = {
                                        subject = selSubject
                                        chapter = "" // reset chapter on subject change
                                        subjectExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
            item {
                ExposedDropdownMenuBox(
                    expanded = chapterExpanded,
                    onExpandedChange = { chapterExpanded = !chapterExpanded }
                ) {
                    SafeOutlinedTextField(
                        value = chapter,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Chapter") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = chapterExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = chapterExpanded,
                        onDismissRequest = { chapterExpanded = false }
                    ) {
                        if (chaptersList.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text(if (subject.isBlank()) "Select a subject first" else "No chapters available") },
                                onClick = { chapterExpanded = false }
                            )
                        } else {
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
            }
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { targetExamDialogVisible = true }
                ) {
                    SafeOutlinedTextField(
                        value = if (selectedExams.isEmpty()) "Select Target Exams..." else selectedExams.joinToString(", "),
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
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { targetExamDialogVisible = true }
                    )
                }
            }
            item {
                ExposedDropdownMenuBox(
                    expanded = difficultyExpanded,
                    onExpandedChange = { difficultyExpanded = !difficultyExpanded }
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
                        listOf("Easy", "Medium", "Hard").forEach { selectionOption ->
                            DropdownMenuItem(
                                text = { Text(selectionOption) },
                                onClick = {
                                    difficulty = selectionOption
                                    difficultyExpanded = false
                                }
                            )
                        }
                    }
                }
            }
            item {
                ExposedDropdownMenuBox(
                    expanded = questionForExpanded,
                    onExpandedChange = { questionForExpanded = !questionForExpanded }
                ) {
                    SafeOutlinedTextField(
                        value = questionFor,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Question For") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = questionForExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = questionForExpanded,
                        onDismissRequest = { questionForExpanded = false }
                    ) {
                        listOf("Free", "Premium").forEach { selectionOption ->
                            DropdownMenuItem(
                                text = { Text(selectionOption) },
                                onClick = {
                                    questionFor = selectionOption
                                    questionForExpanded = false
                                }
                            )
                        }
                    }
                }
            }
            item {
                ExposedDropdownMenuBox(
                    expanded = questionTagExpanded,
                    onExpandedChange = { questionTagExpanded = !questionTagExpanded }
                ) {
                    SafeOutlinedTextField(
                        value = questionTag,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Question Tag") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = questionTagExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = questionTagExpanded,
                        onDismissRequest = { questionTagExpanded = false }
                    ) {
                        listOf("PYQ", "Expected").forEach { selectionOption ->
                            DropdownMenuItem(
                                text = { Text(selectionOption) },
                                onClick = {
                                    questionTag = selectionOption
                                    questionTagExpanded = false
                                }
                            )
                        }
                    }
                }
            }
            if (questionTag == "PYQ") {
                item {
                    SafeOutlinedTextField(
                        value = pyqExamName,
                        onValueChange = { pyqExamName = it },
                        label = { Text("Exam Name (e.g., APSC CCE)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    SafeOutlinedTextField(
                        value = pyqYear,
                        onValueChange = { pyqYear = it },
                        label = { Text("Year (e.g., 2023)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            item {
                Text("Question Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            item {
                SafeOutlinedTextField(
                    value = questionEnglish,
                    onValueChange = { questionEnglish = it },
                    label = { Text("Question in English") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
            item {
                SafeOutlinedTextField(
                    value = questionAssamese,
                    onValueChange = { questionAssamese = it },
                    label = { Text("Question in Assamese") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
            item {
                OutlinedButton(onClick = { /* Handle photo upload */ }, modifier = Modifier.fillMaxWidth()) {
                    Text("Upload Question Photo (Optional)")
                }
            }
            item {
                Text("Options (English)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            }
            item {
                SafeOutlinedTextField(value = optionAEnglish, onValueChange = { optionAEnglish = it }, label = { Text("Option A") }, modifier = Modifier.fillMaxWidth())
            }
            item {
                SafeOutlinedTextField(value = optionBEnglish, onValueChange = { optionBEnglish = it }, label = { Text("Option B") }, modifier = Modifier.fillMaxWidth())
            }
            item {
                SafeOutlinedTextField(value = optionCEnglish, onValueChange = { optionCEnglish = it }, label = { Text("Option C") }, modifier = Modifier.fillMaxWidth())
            }
            item {
                SafeOutlinedTextField(value = optionDEnglish, onValueChange = { optionDEnglish = it }, label = { Text("Option D") }, modifier = Modifier.fillMaxWidth())
            }
            item {
                Text("Options (Assamese)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            }
            item {
                SafeOutlinedTextField(value = optionAAssamese, onValueChange = { optionAAssamese = it }, label = { Text("Option A") }, modifier = Modifier.fillMaxWidth())
            }
            item {
                SafeOutlinedTextField(value = optionBAssamese, onValueChange = { optionBAssamese = it }, label = { Text("Option B") }, modifier = Modifier.fillMaxWidth())
            }
            item {
                SafeOutlinedTextField(value = optionCAssamese, onValueChange = { optionCAssamese = it }, label = { Text("Option C") }, modifier = Modifier.fillMaxWidth())
            }
            item {
                SafeOutlinedTextField(value = optionDAssamese, onValueChange = { optionDAssamese = it }, label = { Text("Option D") }, modifier = Modifier.fillMaxWidth())
            }
            item {
                ExposedDropdownMenuBox(
                    expanded = correctOptionExpanded,
                    onExpandedChange = { correctOptionExpanded = !correctOptionExpanded }
                ) {
                    SafeOutlinedTextField(
                        value = correctOption,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Correct Option") },
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
                            
                            val correctOptionIndex = when(correctOption) {
                                "A" -> 0
                                "B" -> 1
                                "C" -> 2
                                "D" -> 3
                                else -> 0
                            }
                            
                            val finalQuestionTag = if (questionTag == "PYQ") {
                                val examNameStr = pyqExamName.trim()
                                val yearStr = pyqYear.trim()
                                if (examNameStr.isNotBlank() || yearStr.isNotBlank()) "$examNameStr $yearStr".trim() else "PYQ"
                            } else {
                                "Expected"
                            }
                            
                            val newQuestion = QuestionEntity(
                                subject = subject.trim(),
                                topic = chapter.trim(),
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
                                questionType = finalQuestionTag
                            )
                            
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
                                // Keep context like subject, chapter, exam intact for fast subsequent inserts
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
                        Text("Deploying...")
                    } else {
                        Text("Deploy Question")
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
                Button(onClick = { targetExamDialogVisible = false }) {
                    Text("Done")
                }
            }
        )
    }
}
