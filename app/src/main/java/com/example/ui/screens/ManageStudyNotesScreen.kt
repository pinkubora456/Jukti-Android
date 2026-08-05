package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.StudyNoteEntity
import com.example.ui.viewmodel.JuktiViewModel
import com.example.ui.viewmodel.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageStudyNotesScreen(viewModel: JuktiViewModel) {
    val context = LocalContext.current
    val notes by viewModel.studyNotes.collectAsState()
    val exams by viewModel.examsList.collectAsState()
    val allSubjectsChapters by viewModel.allSubjectsChapters.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var editingNote by remember { mutableStateOf<StudyNoteEntity?>(null) }

    // Form fields
    var titleEn by remember { mutableStateOf("") }
    var titleAs by remember { mutableStateOf("") }
    var selectedExam by remember { mutableStateOf("ADRE 2.0") }
    var examDropdownExpanded by remember { mutableStateOf(false) }
    
    var noteType by remember { mutableStateOf("Free") }
    var typeDropdownExpanded by remember { mutableStateOf(false) }

    var subject by remember { mutableStateOf("Assam History") }
    var chapter by remember { mutableStateOf("Ahom Dynasty") }
    var chapterDropdownExpanded by remember { mutableStateOf(false) }
    val rawChapters = allSubjectsChapters.filter { it.subject == subject }.map { it.chapter }.distinct()
    val chaptersList: List<String> = if (rawChapters.isEmpty()) listOf("General") else rawChapters
    var subjectDropdownExpanded by remember { mutableStateOf(false) }

    var contentEn by remember { mutableStateOf("") }
    var contentAs by remember { mutableStateOf("") }

        val rawSubj = allSubjectsChapters.map { it.subject }.distinct()
    val subjectsList: List<String> = if (rawSubj.isEmpty()) listOf("Assam History", "Assam Geography", "Assam Culture", "Polity", "General Studies", "Quantitative Aptitude", "Logical Reasoning", "English") else rawSubj

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Study Notes", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo(Screen.WORKSPACE) }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    titleEn = ""
                    titleAs = ""
                    selectedExam = exams.firstOrNull()?.title ?: "ADRE 2.0"
                    noteType = "Free"
                    subject = "Assam History"
                    chapter = "Ahom Dynasty"
                    contentEn = ""
                    contentAs = ""
                    showAddDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Study Note", tint = MaterialTheme.colorScheme.onPrimary)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(notes) { note ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = if (note.isPremium) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = if (note.isPremium) "Premium" else "Free",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Row {
                                    IconButton(onClick = {
                                        editingNote = note
                                        titleEn = note.titleEn
                                        titleAs = note.titleAs
                                        noteType = if (note.isPremium) "Premium" else "Free"
                                        subject = note.subject
                                        chapter = note.topic
                                        contentEn = note.contentEn
                                        contentAs = note.contentAs
                                    }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                                    }
                                    IconButton(onClick = { viewModel.deleteStudyNote(note) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = note.titleEn,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            if (note.titleAs.isNotBlank()) {
                                Text(
                                    text = note.titleAs,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Subject: ${note.subject} • ${note.topic}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${note.readTimeMinutes} min read",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog || editingNote != null) {
        AlertDialog(
            onDismissRequest = {
                showAddDialog = false
                editingNote = null
            },
            title = { Text(if (editingNote == null) "Add Study Note" else "Edit Study Note", fontWeight = FontWeight.Bold) },
            text = {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 450.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        OutlinedTextField(
                            value = titleEn,
                            onValueChange = { titleEn = it },
                            label = { Text("Title of Study Note (English) *") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = titleAs,
                            onValueChange = { titleAs = it },
                            label = { Text("Title of Study Note (Assamese)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                    item {
                        ExposedDropdownMenuBox(
                            expanded = examDropdownExpanded,
                            onExpandedChange = { examDropdownExpanded = !examDropdownExpanded }
                        ) {
                            OutlinedTextField(
                                value = selectedExam,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Target Exam") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = examDropdownExpanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = examDropdownExpanded,
                                onDismissRequest = { examDropdownExpanded = false }
                            ) {
                                exams.forEach { exam ->
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
                    }
                    item {
                        ExposedDropdownMenuBox(
                            expanded = typeDropdownExpanded,
                            onExpandedChange = { typeDropdownExpanded = !typeDropdownExpanded }
                        ) {
                            OutlinedTextField(
                                value = noteType,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Type (Free / Premium)") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeDropdownExpanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = typeDropdownExpanded,
                                onDismissRequest = { typeDropdownExpanded = false }
                            ) {
                                listOf("Free", "Premium").forEach { typeOption ->
                                    DropdownMenuItem(
                                        text = { Text(typeOption) },
                                        onClick = {
                                            noteType = typeOption
                                            typeDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                    item {
                        ExposedDropdownMenuBox(
                            expanded = subjectDropdownExpanded,
                            onExpandedChange = { subjectDropdownExpanded = !subjectDropdownExpanded }
                        ) {
                            OutlinedTextField(
                                value = subject,
                                onValueChange = { subject = it },
                                label = { Text("Subject (Optional)") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjectDropdownExpanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = subjectDropdownExpanded,
                                onDismissRequest = { subjectDropdownExpanded = false }
                            ) {
                                subjectsList.forEach { subj ->
                                    DropdownMenuItem(
                                        text = { Text(subj) },
                                        onClick = {
                                            subject = subj
                                            subjectDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                    item {
                        ExposedDropdownMenuBox(
                            expanded = chapterDropdownExpanded,
                            onExpandedChange = { chapterDropdownExpanded = !chapterDropdownExpanded }
                        ) {
                            OutlinedTextField(
                                value = chapter,
                                onValueChange = { chapter = it },
                                label = { Text("Chapter / Topic (Optional)") },
                                modifier = Modifier.fillMaxWidth().menuAnchor(),
                                singleLine = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = chapterDropdownExpanded) }
                            )
                            ExposedDropdownMenu(
                                expanded = chapterDropdownExpanded,
                                onDismissRequest = { chapterDropdownExpanded = false }
                            ) {
                                chaptersList.forEach { chap ->
                                    DropdownMenuItem(
                                        text = { Text(chap) },
                                        onClick = {
                                            chapter = chap
                                            chapterDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                    item {
                        OutlinedTextField(
                            value = contentEn,
                            onValueChange = { contentEn = it },
                            label = { Text("Paste Content (English) *") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 4
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = contentAs,
                            onValueChange = { contentAs = it },
                            label = { Text("Paste Content (Assamese) - Optional") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 4
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (titleEn.isNotBlank() && contentEn.isNotBlank()) {
                            val newNote = StudyNoteEntity(
                                id = editingNote?.id ?: 0,
                                subject = subject.ifBlank { "General" },
                                topic = chapter.ifBlank { selectedExam },
                                titleEn = titleEn.trim(),
                                titleAs = titleAs.ifBlank { titleEn }.trim(),
                                contentEn = contentEn.trim(),
                                contentAs = contentAs.ifBlank { contentEn }.trim(),
                                isPremium = noteType.equals("Premium", ignoreCase = true),
                                readTimeMinutes = maxOf(3, contentEn.split("\\s+".toRegex()).size / 100)
                            )
                            if (editingNote == null) {
                                viewModel.addStudyNote(newNote) {
                                    Toast.makeText(context, "Study Note added successfully!", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                viewModel.updateStudyNote(newNote) {
                                    Toast.makeText(context, "Study Note updated successfully!", Toast.LENGTH_SHORT).show()
                                }
                            }
                            showAddDialog = false
                            editingNote = null
                        } else {
                            Toast.makeText(context, "Please enter Title and Content.", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text(if (editingNote == null) "Add Note" else "Save Changes")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showAddDialog = false
                    editingNote = null
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}
