import os

screen_code = """
package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.local.*
import com.example.ui.components.SafeOutlinedTextField
import com.example.ui.viewmodel.JuktiViewModel
import com.example.ui.viewmodel.Screen
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageGuidanceScreen(viewModel: JuktiViewModel) {
    val examsList by viewModel.examsList.collectAsState()
    val allSubjectsChapters by viewModel.allSubjectsChapters.collectAsState()
    
    val allPyqFocus by viewModel.allPyqFocus.collectAsState()
    val allFocusTopics by viewModel.allFocusTopics.collectAsState()
    val allPrepStrategies by viewModel.allPrepStrategies.collectAsState()
    val allGuidanceBanners by viewModel.allGuidanceBanners.collectAsState()

    var selectedExam by remember { mutableStateOf<String?>(null) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }

    var examDropdownExpanded by remember { mutableStateOf(false) }

    val categories = listOf(
        "Guidance Banners" to Icons.Default.ViewCarousel,
        "PYQ Focus" to Icons.Default.History,
        "Focus Topics" to Icons.Default.TrackChanges,
        "Preparation Strategy" to Icons.Default.Lightbulb
    )

    Scaffold(
        topBar = {
            com.example.ui.components.JuktiTopAppBar(
                title = if (selectedCategory != null) "Manage ${selectedCategory}" else "Manage Guidance",
                onBackClick = {
                    if (selectedCategory != null) {
                        selectedCategory = null
                    } else {
                        viewModel.navigateTo(Screen.WORKSPACE)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (selectedCategory == null) {
                // LEVEL 1: Select Exam and Category
                Text("1. Select Exam", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                
                ExposedDropdownMenuBox(
                    expanded = examDropdownExpanded,
                    onExpandedChange = { examDropdownExpanded = !examDropdownExpanded }
                ) {
                    SafeOutlinedTextField(
                        value = selectedExam ?: "Select Exam",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Exam") },
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
                                    selectedExam = exam.title
                                    examDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                if (selectedExam != null) {
                    Text(
                        text = "2. Select Guidance Category",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    
                    categories.forEach { (catName, icon) ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedCategory = catName },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(catName, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            } else {
                // LEVEL 2: Inside a specific Category
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Exam: ${selectedExam!!}", fontWeight = FontWeight.Bold)
                    }
                }

                when (selectedCategory) {
                    "Guidance Banners" -> ManageGuidanceBanners(
                        exam = selectedExam!!, 
                        banners = allGuidanceBanners.filter { it.exam == selectedExam },
                        onSave = { viewModel.saveGuidanceBanner(it) },
                        onDelete = { viewModel.deleteGuidanceBanner(it) }
                    )
                    "PYQ Focus" -> ManagePyqFocus(
                        exam = selectedExam!!,
                        allSubjectsChapters = allSubjectsChapters,
                        pyqFocusData = allPyqFocus.filter { it.exam == selectedExam },
                        onSave = { viewModel.savePyqFocus(it) }
                    )
                    "Focus Topics" -> ManageFocusTopics(
                        exam = selectedExam!!,
                        allSubjectsChapters = allSubjectsChapters,
                        focusTopics = allFocusTopics.filter { it.exam == selectedExam },
                        onSave = { viewModel.saveFocusTopic(it) },
                        onDelete = { viewModel.deleteFocusTopic(it) }
                    )
                    "Preparation Strategy" -> ManagePrepStrategy(
                        exam = selectedExam!!,
                        strategy = allPrepStrategies.find { it.exam == selectedExam },
                        onSave = { viewModel.savePrepStrategy(it) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagePyqFocus(
    exam: String,
    allSubjectsChapters: List<SubjectChapterEntity>,
    pyqFocusData: List<PyqFocusEntity>,
    onSave: (PyqFocusEntity) -> Unit
) {
    var selectedSubject by remember { mutableStateOf<String?>(null) }
    var subjectDropdownExpanded by remember { mutableStateOf(false) }

    val uniqueSubjects = remember(allSubjectsChapters) {
        allSubjectsChapters.map { it.subject }.distinct().sorted()
    }

    val chaptersForSubject = remember(allSubjectsChapters, selectedSubject) {
        if (selectedSubject == null) emptyList()
        else allSubjectsChapters.filter { it.subject == selectedSubject }.map { it.chapter }.distinct().sorted()
    }

    ExposedDropdownMenuBox(
        expanded = subjectDropdownExpanded,
        onExpandedChange = { subjectDropdownExpanded = !subjectDropdownExpanded }
    ) {
        SafeOutlinedTextField(
            value = selectedSubject ?: "Select Subject",
            onValueChange = {},
            readOnly = true,
            label = { Text("Subject") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjectDropdownExpanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = subjectDropdownExpanded,
            onDismissRequest = { subjectDropdownExpanded = false }
        ) {
            uniqueSubjects.forEach { subject ->
                DropdownMenuItem(
                    text = { Text(subject) },
                    onClick = {
                        selectedSubject = subject
                        subjectDropdownExpanded = false
                    }
                )
            }
        }
    }

    if (selectedSubject != null) {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Chapter", modifier = Modifier.weight(2f), fontWeight = FontWeight.Bold)
                    Text("PYQ", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                    Text("Exams", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                    Text("Avg", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                }
            }

            items(chaptersForSubject) { chapter ->
                val currentData = pyqFocusData.find { it.subject == selectedSubject && it.chapter == chapter }
                    ?: PyqFocusEntity(exam = exam, subject = selectedSubject!!, chapter = chapter)

                var pyqCountStr by remember(currentData) { mutableStateOf(currentData.pyqCount.toString()) }
                var examsCoveredStr by remember(currentData) { mutableStateOf(currentData.examsCovered.toString()) }

                val pyqCount = pyqCountStr.toIntOrNull() ?: 0
                val examsCovered = examsCoveredStr.toIntOrNull() ?: 0
                val avg = if (examsCovered > 0) pyqCount.toFloat() / examsCovered else 0f

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(chapter, modifier = Modifier.weight(2f), style = MaterialTheme.typography.bodyMedium)
                        
                        SafeOutlinedTextField(
                            value = pyqCountStr,
                            onValueChange = { 
                                pyqCountStr = it 
                                onSave(currentData.copy(pyqCount = it.toIntOrNull() ?: 0, examsCovered = examsCoveredStr.toIntOrNull() ?: 0, updatedAt = System.currentTimeMillis()))
                            },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        
                        SafeOutlinedTextField(
                            value = examsCoveredStr,
                            onValueChange = { 
                                examsCoveredStr = it
                                onSave(currentData.copy(pyqCount = pyqCountStr.toIntOrNull() ?: 0, examsCovered = it.toIntOrNull() ?: 0, updatedAt = System.currentTimeMillis()))
                            },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        
                        Text(String.format("%.1f", avg), modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageFocusTopics(
    exam: String,
    allSubjectsChapters: List<SubjectChapterEntity>,
    focusTopics: List<FocusTopicEntity>,
    onSave: (FocusTopicEntity) -> Unit,
    onDelete: (FocusTopicEntity) -> Unit
) {
    var selectedSubject by remember { mutableStateOf<String?>(null) }
    var selectedChapter by remember { mutableStateOf<String?>(null) }
    
    var subjectDropdownExpanded by remember { mutableStateOf(false) }
    var chapterDropdownExpanded by remember { mutableStateOf(false) }

    var topicName by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("Medium") }
    var instruction by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }

    val uniqueSubjects = remember(allSubjectsChapters) {
        allSubjectsChapters.map { it.subject }.distinct().sorted()
    }

    val chaptersForSubject = remember(allSubjectsChapters, selectedSubject) {
        if (selectedSubject == null) emptyList()
        else allSubjectsChapters.filter { it.subject == selectedSubject }.map { it.chapter }.distinct().sorted()
    }

    val topicsForChapter = remember(focusTopics, selectedSubject, selectedChapter) {
        focusTopics.filter { it.subject == selectedSubject && it.chapter == selectedChapter }
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ExposedDropdownMenuBox(
            expanded = subjectDropdownExpanded,
            onExpandedChange = { subjectDropdownExpanded = !subjectDropdownExpanded }
        ) {
            SafeOutlinedTextField(
                value = selectedSubject ?: "Select Subject",
                onValueChange = {},
                readOnly = true,
                label = { Text("Subject") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjectDropdownExpanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = subjectDropdownExpanded,
                onDismissRequest = { subjectDropdownExpanded = false }
            ) {
                uniqueSubjects.forEach { subject ->
                    DropdownMenuItem(
                        text = { Text(subject) },
                        onClick = {
                            selectedSubject = subject
                            selectedChapter = null // Reset chapter
                            subjectDropdownExpanded = false
                        }
                    )
                }
            }
        }

        if (selectedSubject != null) {
            ExposedDropdownMenuBox(
                expanded = chapterDropdownExpanded,
                onExpandedChange = { chapterDropdownExpanded = !chapterDropdownExpanded }
            ) {
                SafeOutlinedTextField(
                    value = selectedChapter ?: "Select Chapter",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Chapter") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = chapterDropdownExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = chapterDropdownExpanded,
                    onDismissRequest = { chapterDropdownExpanded = false }
                ) {
                    chaptersForSubject.forEach { chapter ->
                        DropdownMenuItem(
                            text = { Text(chapter) },
                            onClick = {
                                selectedChapter = chapter
                                chapterDropdownExpanded = false
                            }
                        )
                    }
                }
            }
        }

        if (selectedChapter != null) {
            Button(
                onClick = { showAddDialog = true },
                modifier = Modifier.align(Alignment.End)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Topic")
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(topicsForChapter) { topic ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(topic.topic, fontWeight = FontWeight.Bold)
                                Text("Priority: ${topic.priority}", style = MaterialTheme.typography.bodySmall)
                                if (topic.instruction.isNotBlank()) {
                                    Text(topic.instruction, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            IconButton(onClick = { onDelete(topic) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Focus Topic") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SafeOutlinedTextField(
                        value = topicName,
                        onValueChange = { topicName = it },
                        label = { Text("Topic Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    SafeOutlinedTextField(
                        value = priority,
                        onValueChange = { priority = it },
                        label = { Text("Priority (High, Medium, Low)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    SafeOutlinedTextField(
                        value = instruction,
                        onValueChange = { instruction = it },
                        label = { Text("Guidance/Instruction (Optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (topicName.isNotBlank() && selectedSubject != null && selectedChapter != null) {
                        onSave(FocusTopicEntity(
                            exam = exam,
                            subject = selectedSubject!!,
                            chapter = selectedChapter!!,
                            topic = topicName,
                            priority = priority,
                            instruction = instruction,
                            updatedAt = System.currentTimeMillis()
                        ))
                        topicName = ""
                        instruction = ""
                        priority = "Medium"
                        showAddDialog = false
                    }
                }) {
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
fun ManagePrepStrategy(
    exam: String,
    strategy: PrepStrategyEntity?,
    onSave: (PrepStrategyEntity) -> Unit
) {
    var content by remember(strategy) { mutableStateOf(strategy?.content ?: "") }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Overall Preparation Strategy for $exam", fontWeight = FontWeight.Bold)
        
        SafeOutlinedTextField(
            value = content,
            onValueChange = { content = it },
            label = { Text("Strategy Content (Leave empty to hide from users)") },
            modifier = Modifier.fillMaxWidth().height(250.dp),
            minLines = 8
        )

        Button(
            onClick = {
                onSave(
                    (strategy ?: PrepStrategyEntity(exam = exam, content = "")).copy(
                        content = content,
                        updatedAt = System.currentTimeMillis()
                    )
                )
            },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Save Strategy")
        }
    }
}

@Composable
fun ManageGuidanceBanners(
    exam: String,
    banners: List<GuidanceBannerEntity>,
    onSave: (GuidanceBannerEntity) -> Unit,
    onDelete: (GuidanceBannerEntity) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var actionTarget by remember { mutableStateOf("") }
    
    Button(
        onClick = { showAddDialog = true },
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(Icons.Default.Add, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Add Banner")
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 16.dp)) {
        items(banners) { banner ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(banner.title, fontWeight = FontWeight.Bold)
                        Text(banner.description, style = MaterialTheme.typography.bodySmall)
                        if (banner.actionTarget.isNotBlank()) {
                            Text("Links to: ${banner.actionTarget}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    IconButton(onClick = { onDelete(banner) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Guidance Banner") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SafeOutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    SafeOutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    SafeOutlinedTextField(
                        value = actionTarget,
                        onValueChange = { actionTarget = it },
                        label = { Text("Action Target (e.g. PYQ Focus)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (title.isNotBlank()) {
                        onSave(GuidanceBannerEntity(
                            exam = exam,
                            title = title,
                            description = description,
                            actionTarget = actionTarget,
                            updatedAt = System.currentTimeMillis()
                        ))
                        title = ""
                        description = ""
                        actionTarget = ""
                        showAddDialog = false
                    }
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
            }
        )
    }
}
"""

with open("app/src/main/java/com/example/ui/screens/ManageGuidanceScreen.kt", "w") as f:
    f.write(screen_code)

print("Updated ManageGuidanceScreen.kt with new requirements.")
