package com.example.ui.screens

import com.example.ui.components.SafeOutlinedTextField

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.BorderStroke
import com.example.data.local.StudyNoteEntity
import com.example.ui.components.BilingualText
import com.example.ui.viewmodel.AppLanguage
import com.example.ui.viewmodel.JuktiViewModel
import com.example.ui.viewmodel.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyNotesScreen(viewModel: JuktiViewModel) {
    val studyNotesLanguage by viewModel.studyNotesLanguage.collectAsState()
    val isAssameseNoteContent = studyNotesLanguage == AppLanguage.ASSAMESE
    val notes by viewModel.studyNotes.collectAsState()
    val selectedNote by viewModel.selectedStudyNote.collectAsState()

    var noteSearchQuery by remember { mutableStateOf("") }
    var selectedSubjectFilter by remember { mutableStateOf("All") }
    val allSubjectsChapters by viewModel.allSubjectsChapters.collectAsState()
    
    val nonCurrentAffairsNotes = notes.filter { !it.subject.contains("Current Affairs", ignoreCase = true) }

    val rawSubj = allSubjectsChapters
        .map { it.subject }
        .filter { it.isNotBlank() && !it.contains("Current Affairs", ignoreCase = true) }
        .distinct()
        
    val subjects = listOf("All") + rawSubj

    val filteredNotes = nonCurrentAffairsNotes.filter { note ->
        (selectedSubjectFilter == "All" || note.subject == selectedSubjectFilter) &&
                (noteSearchQuery.isBlank() || note.titleEn.contains(noteSearchQuery, ignoreCase = true) || note.titleAs.contains(noteSearchQuery, ignoreCase = true))
    }

    if (selectedNote != null) {
        // Study Note Detail Reader View
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            com.example.ui.components.JuktiTopAppBar(
                title = {
                    BilingualText(
                        textEn = selectedNote!!.titleEn,
                        textAs = selectedNote!!.titleAs,
                        language = studyNotesLanguage,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                onBackClick = { viewModel.selectStudyNote(null) },
                actions = {
                    TextButton(onClick = { 
                        viewModel.toggleStudyNotesLanguage() 
                    }) {
                        Icon(Icons.Default.Translate, contentDescription = "Change Language", modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(if (studyNotesLanguage == AppLanguage.ENGLISH) "EN" else "অসমীয়া", fontWeight = FontWeight.Bold)
                    }
                    IconButton(onClick = { viewModel.toggleBookmarkNote(selectedNote!!) }) {
                        Icon(
                            imageVector = if (selectedNote!!.isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = "Bookmark"
                        )
                    }
                }
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "${selectedNote!!.subject} • ${selectedNote!!.readTimeMinutes} Mins Read",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                val contentEn = selectedNote!!.contentEn
                val contentAs = selectedNote!!.contentAs
                
                if (contentAs.isNotBlank() && isAssameseNoteContent) {
                    Text(
                        text = contentAs,
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.4f
                    )
                } else {
                    Text(
                        text = contentEn,
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.4f
                    )
                }
            }
        }
    } else {
        // Notes Directory List
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            com.example.ui.components.JuktiTopAppBar(
                title = "Study Notes & Revision Sheets",
                onBackClick = { viewModel.navigateTo(Screen.HOME) },
                actions = {
                    TextButton(onClick = { 
                        viewModel.toggleStudyNotesLanguage() 
                    }) {
                        Icon(Icons.Default.Translate, contentDescription = "Change Language", modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(if (studyNotesLanguage == AppLanguage.ENGLISH) "EN" else "অসমীয়া", fontWeight = FontWeight.Bold)
                    }
                }
            )

            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                SafeOutlinedTextField(
                    value = noteSearchQuery,
                    onValueChange = { noteSearchQuery = it },
                    placeholder = { Text("Search notes...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(subjects) { subject ->
                        FilterChip(
                            selected = (selectedSubjectFilter == subject),
                            onClick = { selectedSubjectFilter = subject },
                            label = { Text(subject) }
                        )
                    }
                }
            }

            if (filteredNotes.isEmpty()) {
                com.example.ui.components.EmptyStateIllustration(
                    type = com.example.ui.components.EmptyStateType.NOTEBOOK_GAMOSA,
                    title = "No Notes Found",
                    message = "Try clearing your search query or filters",
                    modifier = Modifier.fillMaxWidth().padding(top = 40.dp)
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(filteredNotes, key = { it.id }) { note ->
                        StudyNoteListItem(
                            note = note,
                            language = studyNotesLanguage,
                            onClick = { viewModel.selectStudyNote(note) },
                            onBookmarkToggle = { viewModel.toggleBookmarkNote(note) },
                            onDownloadToggle = { viewModel.toggleDownloadNote(note) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StudyNoteListItem(
    note: StudyNoteEntity,
    language: AppLanguage,
    onClick: () -> Unit,
    onBookmarkToggle: () -> Unit,
    onDownloadToggle: () -> Unit
) {
    Card(
        onClick = onClick,
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
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = note.subject,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
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

                Row {
                    IconButton(onClick = onDownloadToggle) {
                        Icon(
                            imageVector = if (note.isDownloaded) Icons.Filled.DownloadDone else Icons.Outlined.Download,
                            contentDescription = "Download Note",
                            tint = if (note.isDownloaded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onBookmarkToggle) {
                        Icon(
                            imageVector = if (note.isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = if (note.isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            BilingualText(
                textEn = note.titleEn,
                textAs = note.titleAs,
                language = language,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${note.readTimeMinutes} min read",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (note.isDownloaded) {
                    Text(
                        text = "• Saved Offline",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
