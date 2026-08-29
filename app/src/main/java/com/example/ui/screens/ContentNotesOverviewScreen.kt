package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.JuktiTopAppBar
import com.example.ui.viewmodel.JuktiViewModel
import com.example.ui.viewmodel.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContentNotesOverviewScreen(viewModel: JuktiViewModel) {
    val allNotes by viewModel.studyNotes.collectAsState()
    val allSubjectsChapters by viewModel.allSubjectsChapters.collectAsState()

    val subjectsList = remember(allSubjectsChapters) {
        allSubjectsChapters.map { it.subject }.distinct().sorted()
    }

    var selectedSubject by remember { mutableStateOf(subjectsList.firstOrNull() ?: "") }
    var expanded by remember { mutableStateOf(false) }

    if (selectedSubject.isNotEmpty() && !subjectsList.contains(selectedSubject)) {
        selectedSubject = subjectsList.firstOrNull() ?: ""
    }

    val chapterStats = remember(selectedSubject, allNotes, allSubjectsChapters) {
        val statsMap = mutableMapOf<String, Int>()
        allSubjectsChapters.filter { it.subject == selectedSubject }.forEach { sc ->
            statsMap[sc.chapter] = 0
        }
        val subjNotes = allNotes.filter { it.subject == selectedSubject }
        subjNotes.forEach { n ->
            statsMap[n.topic] = (statsMap[n.topic] ?: 0) + 1
        }
        statsMap.toList().sortedByDescending { it.second }
    }

    Scaffold(
        topBar = {
            JuktiTopAppBar(
                title = "Study Notes Overview",
                onBackClick = { viewModel.navigateTo(Screen.CONTENT_OVERVIEW) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it }
            ) {
                OutlinedTextField(
                    value = selectedSubject,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Select Subject") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    subjectsList.forEach { subj ->
                        DropdownMenuItem(
                            text = { Text(subj) },
                            onClick = {
                                selectedSubject = subj
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth().weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Chapter", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Text("Notes", fontWeight = FontWeight.Bold, modifier = Modifier.width(60.dp))
                    }
                    HorizontalDivider()
                    
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(chapterStats) { (chap, count) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    chap, 
                                    modifier = Modifier.weight(1f),
                                    maxLines = 2,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    "$count", 
                                    fontWeight = FontWeight.SemiBold, 
                                    modifier = Modifier.width(60.dp), 
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        }
                    }
                }
            }
        }
    }
}
