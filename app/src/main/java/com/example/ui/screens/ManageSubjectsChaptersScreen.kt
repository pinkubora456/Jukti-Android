package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.SafeOutlinedTextField
import com.example.ui.viewmodel.JuktiViewModel
import com.example.ui.viewmodel.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageSubjectsChaptersScreen(viewModel: JuktiViewModel) {
    val context = LocalContext.current
    val allSubjectsChapters by viewModel.allSubjectsChapters.collectAsState()
    val activeStats by viewModel.activeSubjectChapterStats.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedSubjectForChapters by remember { mutableStateOf<String?>(null) }
    
    var showMergeDialog by remember { mutableStateOf(false) }
    var showRenameSubjectDialog by remember { mutableStateOf<String?>(null) }
    var showRenameChapterDialog by remember { mutableStateOf<String?>(null) }
    var showAddChapterDialog by remember { mutableStateOf<String?>(null) }

    // Combine static DB and active questions to get all subjects and chapters
    val combinedData = remember(allSubjectsChapters, activeStats) {
        val map = mutableMapOf<String, MutableMap<String, Int>>() // Subject -> { Chapter -> QuestionCount }
        
        // Load explicitly added ones first (default count 0)
        allSubjectsChapters.forEach { sc ->
            val subjMap = map.getOrPut(sc.subject) { mutableMapOf() }
            if (sc.chapter.isNotBlank()) {
                subjMap.putIfAbsent(sc.chapter, 0)
            }
        }
        
        // Load ones with active questions
        activeStats.forEach { stat ->
            val subjMap = map.getOrPut(stat.subject) { mutableMapOf() }
            if (stat.chapter.isNotBlank()) {
                subjMap[stat.chapter] = (subjMap[stat.chapter] ?: 0) + stat.questionCount
            }
        }
        
        map
    }

    Scaffold(
        topBar = {
            com.example.ui.components.JuktiTopAppBar(
                title = if (selectedSubjectForChapters == null) "Manage Subjects & Chapters" else selectedSubjectForChapters ?: "",
                onBackClick = { 
                    if (selectedSubjectForChapters != null) {
                        selectedSubjectForChapters = null
                        searchQuery = ""
                    } else {
                        viewModel.navigateTo(Screen.MANAGE_QBANK)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            SafeOutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (selectedSubjectForChapters == null) {
                // SUBJECT LIST VIEW
                val subjects = combinedData.entries.map {
                    SubjectSummary(
                        name = it.key,
                        chapterCount = it.value.keys.size,
                        totalQuestions = it.value.values.sum()
                    )
                }.filter { 
                    searchQuery.isBlank() || it.name.contains(searchQuery, ignoreCase = true) 
                }.sortedBy { it.name }

                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Subjects", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("${subjects.size} total", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    items(subjects) { subject ->
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable {
                                selectedSubjectForChapters = subject.name
                                searchQuery = ""
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(subject.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                    Text("${subject.chapterCount} Chapters • ${subject.totalQuestions} Questions", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    IconButton(onClick = { showRenameSubjectDialog = subject.name }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Rename Subject")
                                    }
                                    Button(onClick = { 
                                        selectedSubjectForChapters = subject.name
                                        searchQuery = ""
                                    }, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp), modifier = Modifier.height(36.dp)) {
                                        Text("Manage")
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // CHAPTER LIST VIEW
                val subjectName = selectedSubjectForChapters!!
                val chapters = combinedData[subjectName]?.entries?.map {
                    ChapterSummary(
                        name = it.key,
                        questionCount = it.value
                    )
                }?.filter {
                    searchQuery.isBlank() || it.name.contains(searchQuery, ignoreCase = true)
                }?.sortedByDescending { it.questionCount } ?: emptyList()

                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Chapters", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(onClick = { showAddChapterDialog = subjectName }, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp), modifier = Modifier.height(36.dp)) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Add")
                                }
                                Button(onClick = { showMergeDialog = true }, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp), modifier = Modifier.height(36.dp)) {
                                    Icon(Icons.Default.Merge, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Merge")
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    items(chapters) { chapter ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(chapter.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                                    Text(
                                        "${chapter.questionCount} Questions", 
                                        style = MaterialTheme.typography.bodySmall, 
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.clickable {
                                            viewModel.setSubjectFilter(subjectName)
                                            viewModel.setChapterFilter(chapter.name)
                                            viewModel.navigateTo(Screen.ALL_QUESTIONS)
                                        }
                                    )
                                }
                                IconButton(onClick = { showRenameChapterDialog = chapter.name }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Rename Chapter")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showRenameSubjectDialog != null) {
        var newName by remember { mutableStateOf(showRenameSubjectDialog!!) }
        AlertDialog(
            onDismissRequest = { showRenameSubjectDialog = null },
            title = { Text("Rename Subject") },
            text = {
                SafeOutlinedTextField(value = newName, onValueChange = { newName = it }, label = { Text("New Subject Name") }, modifier = Modifier.fillMaxWidth())
            },
            confirmButton = {
                Button(onClick = {
                    if (newName.isNotBlank() && newName != showRenameSubjectDialog) {
                        viewModel.renameSubject(showRenameSubjectDialog!!, newName)
                        if (selectedSubjectForChapters == showRenameSubjectDialog) {
                            selectedSubjectForChapters = newName
                        }
                        Toast.makeText(context, "Subject renamed", Toast.LENGTH_SHORT).show()
                    }
                    showRenameSubjectDialog = null
                }) { Text("Rename") }
            },
            dismissButton = { TextButton(onClick = { showRenameSubjectDialog = null }) { Text("Cancel") } }
        )
    }

    if (showRenameChapterDialog != null) {
        val subjectName = selectedSubjectForChapters!!
        var newName by remember { mutableStateOf(showRenameChapterDialog!!) }
        AlertDialog(
            onDismissRequest = { showRenameChapterDialog = null },
            title = { Text("Rename Chapter") },
            text = {
                SafeOutlinedTextField(value = newName, onValueChange = { newName = it }, label = { Text("New Chapter Name") }, modifier = Modifier.fillMaxWidth())
            },
            confirmButton = {
                Button(onClick = {
                    if (newName.isNotBlank() && newName != showRenameChapterDialog) {
                        viewModel.renameChapter(subjectName, showRenameChapterDialog!!, newName)
                        Toast.makeText(context, "Chapter renamed", Toast.LENGTH_SHORT).show()
                    }
                    showRenameChapterDialog = null
                }) { Text("Rename") }
            },
            dismissButton = { TextButton(onClick = { showRenameChapterDialog = null }) { Text("Cancel") } }
        )
    }

    if (showAddChapterDialog != null) {
        val subjectName = showAddChapterDialog!!
        var newName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddChapterDialog = null },
            title = { Text("Add Chapter") },
            text = {
                SafeOutlinedTextField(value = newName, onValueChange = { newName = it }, label = { Text("Chapter Name") }, modifier = Modifier.fillMaxWidth())
            },
            confirmButton = {
                Button(onClick = {
                    if (newName.isNotBlank()) {
                        viewModel.addSubjectChapter(subjectName, newName)
                        Toast.makeText(context, "Chapter added", Toast.LENGTH_SHORT).show()
                    }
                    showAddChapterDialog = null
                }) { Text("Add") }
            },
            dismissButton = { TextButton(onClick = { showAddChapterDialog = null }) { Text("Cancel") } }
        )
    }

    if (showMergeDialog && selectedSubjectForChapters != null) {
        val subjectName = selectedSubjectForChapters!!
        val chapterNames = combinedData[subjectName]?.keys?.sorted() ?: emptyList()
        MergeChaptersDialog(
            subjectName = subjectName,
            chapters = chapterNames,
            onDismiss = { showMergeDialog = false },
            onConfirm = { source, target ->
                viewModel.mergeChapter(subjectName, source, target)
                Toast.makeText(context, "Chapters merged successfully", Toast.LENGTH_SHORT).show()
                showMergeDialog = false
            }
        )
    }
}

data class SubjectSummary(val name: String, val chapterCount: Int, val totalQuestions: Int)
data class ChapterSummary(val name: String, val questionCount: Int)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MergeChaptersDialog(
    subjectName: String,
    chapters: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (source: String, target: String) -> Unit
) {
    var sourceChapter by remember { mutableStateOf("") }
    var targetChapter by remember { mutableStateOf("") }
    var sourceExpanded by remember { mutableStateOf(false) }
    var targetExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Merge Chapters") },
        text = {
            Column {
                Text("Subject: $subjectName", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(16.dp))
                
                ExposedDropdownMenuBox(
                    expanded = sourceExpanded,
                    onExpandedChange = { sourceExpanded = !sourceExpanded }
                ) {
                    SafeOutlinedTextField(
                        value = sourceChapter,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Source Chapter (will be removed)") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sourceExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = sourceExpanded,
                        onDismissRequest = { sourceExpanded = false }
                    ) {
                        chapters.filter { it != targetChapter }.forEach { selectionOption ->
                            DropdownMenuItem(
                                text = { Text(selectionOption) },
                                onClick = {
                                    sourceChapter = selectionOption
                                    sourceExpanded = false
                                }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                ExposedDropdownMenuBox(
                    expanded = targetExpanded,
                    onExpandedChange = { targetExpanded = !targetExpanded }
                ) {
                    SafeOutlinedTextField(
                        value = targetChapter,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Target Chapter (will keep questions)") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = targetExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = targetExpanded,
                        onDismissRequest = { targetExpanded = false }
                    ) {
                        chapters.filter { it != sourceChapter }.forEach { selectionOption ->
                            DropdownMenuItem(
                                text = { Text(selectionOption) },
                                onClick = {
                                    targetChapter = selectionOption
                                    targetExpanded = false
                                }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                if (sourceChapter.isNotBlank() && targetChapter.isNotBlank()) {
                    Text(
                        "Warning: All questions from \"$sourceChapter\" will be permanently moved to \"$targetChapter\". This cannot be easily undone.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(sourceChapter, targetChapter) },
                enabled = sourceChapter.isNotBlank() && targetChapter.isNotBlank() && sourceChapter != targetChapter
            ) {
                Text("Merge Chapters")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
