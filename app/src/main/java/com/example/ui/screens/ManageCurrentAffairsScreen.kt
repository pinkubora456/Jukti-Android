package com.example.ui.screens

import com.example.ui.components.SafeOutlinedTextField

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
fun ManageCurrentAffairsScreen(viewModel: JuktiViewModel) {
    val context = LocalContext.current
    val allNotes by viewModel.studyNotes.collectAsState()
    val notes = remember(allNotes) { allNotes.filter { it.subject.contains("Current Affairs", ignoreCase = true) } }
    val exams by viewModel.examsList.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var editingNote by remember { mutableStateOf<StudyNoteEntity?>(null) }
    var currentAffairsToDelete by remember { mutableStateOf<StudyNoteEntity?>(null) }

    // Form fields
    var titleEn by remember { mutableStateOf("") }
    var titleAs by remember { mutableStateOf("") }
    val selectedExams = remember { mutableStateListOf<String>() }
    var examDropdownExpanded by remember { mutableStateOf(false) }
    
    var noteType by remember { mutableStateOf("Free") }
    var typeDropdownExpanded by remember { mutableStateOf(false) }

    var contentEn by remember { mutableStateOf("") }
    var contentAs by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            com.example.ui.components.JuktiTopAppBar(
                title = "Manage Current Affairs",
                onBackClick = { viewModel.navigateTo(Screen.WORKSPACE) }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    titleEn = ""
                    titleAs = ""
                    selectedExams.clear()
                    exams.firstOrNull()?.let { selectedExams.add(it.title) }
                    noteType = "Free"
                    contentEn = ""
                    contentAs = ""
                    showAddDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Current Affairs", tint = MaterialTheme.colorScheme.onPrimary)
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
                                        selectedExams.clear()
                                        if (note.topic.isNotBlank()) {
                                            note.topic.split(",").map { it.trim() }.filter { it.isNotBlank() }.forEach { selectedExams.add(it) }
                                        }
                                        noteType = if (note.isPremium) "Premium" else "Free"
                                        contentEn = note.contentEn
                                        contentAs = note.contentAs
                                    }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                                    }
                                    IconButton(onClick = { currentAffairsToDelete = note }) {
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
                                    text = if (note.topic.isNotBlank()) "Target: ${note.topic}" else "Current Affairs",
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
            title = { Text(if (editingNote == null) "Add Current Affairs" else "Edit Current Affairs", fontWeight = FontWeight.Bold) },
            text = {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 450.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        SafeOutlinedTextField(
                            value = titleEn,
                            onValueChange = { titleEn = it },
                            label = { Text("Title (English) *") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                    item {
                        SafeOutlinedTextField(
                            value = titleAs,
                            onValueChange = { titleAs = it },
                            label = { Text("Title (Assamese)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                    item {
                        ExposedDropdownMenuBox(
                            expanded = examDropdownExpanded,
                            onExpandedChange = { examDropdownExpanded = !examDropdownExpanded }
                        ) {
                            SafeOutlinedTextField(
                                value = if (selectedExams.isEmpty()) "Select Target Exams..." else selectedExams.joinToString(", "),
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Target Exams (Multiple)") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = examDropdownExpanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = examDropdownExpanded,
                                onDismissRequest = { examDropdownExpanded = false }
                            ) {
                                exams.forEach { exam ->
                                    val isSelected = selectedExams.contains(exam.title)
                                    DropdownMenuItem(
                                        text = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Checkbox(
                                                    checked = isSelected,
                                                    onCheckedChange = null
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(exam.title)
                                            }
                                        },
                                        onClick = {
                                            if (isSelected) {
                                                selectedExams.remove(exam.title)
                                            } else {
                                                selectedExams.add(exam.title)
                                            }
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
                            SafeOutlinedTextField(
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
                        SafeOutlinedTextField(
                            value = contentEn,
                            onValueChange = { contentEn = it },
                            label = { Text("Paste Content (English) *") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 4
                        )
                    }
                    item {
                        SafeOutlinedTextField(
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
                                subject = "Current Affairs",
                                topic = if (selectedExams.isNotEmpty()) selectedExams.joinToString(", ") else "General",
                                titleEn = titleEn.trim(),
                                titleAs = titleAs.ifBlank { titleEn }.trim(),
                                contentEn = contentEn.trim(),
                                contentAs = contentAs.ifBlank { contentEn }.trim(),
                                isPremium = noteType.equals("Premium", ignoreCase = true),
                                readTimeMinutes = maxOf(3, contentEn.split("\\s+".toRegex()).size / 100)
                            )
                            if (editingNote == null) {
                                viewModel.addStudyNote(newNote) {
                                    Toast.makeText(context, "Current Affairs added successfully!", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                viewModel.updateStudyNote(newNote) {
                                    Toast.makeText(context, "Current Affairs updated successfully!", Toast.LENGTH_SHORT).show()
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

    if (currentAffairsToDelete != null) {
        AlertDialog(
            onDismissRequest = { currentAffairsToDelete = null },
            title = { Text("Confirm Delete") },
            text = { Text("Are you sure you want to delete this current affairs note?") },
            confirmButton = {
                Button(onClick = {
                    viewModel.deleteStudyNote(currentAffairsToDelete!!)
                    currentAffairsToDelete = null
                }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { currentAffairsToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
