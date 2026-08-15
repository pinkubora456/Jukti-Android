package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.BilingualText
import com.example.ui.components.SafeOutlinedTextField
import com.example.ui.viewmodel.AppLanguage
import com.example.ui.viewmodel.JuktiViewModel
import com.example.ui.viewmodel.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalSearchScreen(viewModel: JuktiViewModel) {
    val language by viewModel.language.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    val questions by viewModel.questions.collectAsState()
    val notes by viewModel.studyNotes.collectAsState()
    val mockTests by viewModel.mockTests.collectAsState()
    val examUpdates by viewModel.examUpdates.collectAsState()

    var query by remember { mutableStateOf("") }

    val matchedQuestions = remember(query, questions) {
        if (query.isBlank()) emptyList()
        else questions.filter { q ->
            q.questionEn.contains(query, ignoreCase = true) || q.questionAs.contains(query, ignoreCase = true) || q.subject.contains(query, ignoreCase = true)
        }
    }

    val matchedNotes = remember(query, notes) {
        if (query.isBlank()) emptyList()
        else notes.filter { n ->
            n.titleEn.contains(query, ignoreCase = true) || n.titleAs.contains(query, ignoreCase = true) || n.subject.contains(query, ignoreCase = true)
        }
    }

    val matchedMocks = remember(query, mockTests) {
        if (query.isBlank()) emptyList()
        else mockTests.filter { m ->
            m.titleEn.contains(query, ignoreCase = true) || m.titleAs.contains(query, ignoreCase = true) || m.category.contains(query, ignoreCase = true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        com.example.ui.components.JuktiTopAppBar(
            onBackClick = { viewModel.navigateTo(Screen.HOME) },
            title = {
                SafeOutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Search questions, notes, mocks...", style = MaterialTheme.typography.bodyMedium) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { query = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = null)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }
        )

        if (query.isBlank()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Type to search across Jukti app",
                    color = MaterialTheme.colorScheme.outline
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (matchedQuestions.isNotEmpty()) {
                    item {
                        Text("Questions (${matchedQuestions.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    items(matchedQuestions, key = { it.id }) { q ->
                        QuestionStudyCard(
                            question = q,
                            language = language,
                            onBookmarkToggle = { viewModel.toggleBookmarkQuestion(q) },
                            onLikeToggle = { viewModel.toggleLikeQuestion(q) },
                            onReportClick = { viewModel.reportQuestion(q); android.widget.Toast.makeText(context, "Question reported successfully", android.widget.Toast.LENGTH_SHORT).show() }
                        )
                    }
                }

                if (matchedNotes.isNotEmpty()) {
                    item {
                        Text("Study Notes (${matchedNotes.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    items(matchedNotes, key = { it.id }) { n ->
                        StudyNoteListItem(
                            note = n,
                            language = language,
                            onClick = { viewModel.selectStudyNote(n) },
                            onBookmarkToggle = { viewModel.toggleBookmarkNote(n) },
                            onDownloadToggle = { viewModel.toggleDownloadNote(n) }
                        )
                    }
                }

                if (matchedMocks.isNotEmpty()) {
                    item {
                        Text("Mock Tests (${matchedMocks.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    items(matchedMocks, key = { it.id }) { m ->
                        MockTestListItem(
                            mock = m,
                            language = language,
                            onStart = { viewModel.selectMockTest(m) }
                        )
                    }
                }
            }
        }
    }
}
