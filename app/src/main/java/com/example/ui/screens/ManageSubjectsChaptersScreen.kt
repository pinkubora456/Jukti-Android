package com.example.ui.screens

import com.example.ui.components.SafeOutlinedTextField

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.viewmodel.JuktiViewModel
import com.example.ui.viewmodel.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageSubjectsChaptersScreen(viewModel: JuktiViewModel) {
    val context = LocalContext.current
    val subjectsChapters by viewModel.allSubjectsChapters.collectAsState()

    var subject by remember { mutableStateOf("") }
    var chapter by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    val subjects = subjectsChapters.map { it.subject }.distinct()

    Scaffold(
        topBar = {
            com.example.ui.components.JuktiTopAppBar(
                title = "Manage Subjects & Chapters",
                onBackClick = { viewModel.navigateTo(Screen.WORKSPACE) }
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
                onExpandedChange = { expanded = !expanded }
            ) {
                SafeOutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Subject Name") },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    subjects.forEach { selectionOption ->
                        DropdownMenuItem(
                            text = { Text(selectionOption) },
                            onClick = {
                                subject = selectionOption
                                expanded = false
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            SafeOutlinedTextField(
                value = chapter,
                onValueChange = { chapter = it },
                label = { Text("Chapter Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    if (subject.isNotBlank() && chapter.isNotBlank()) {
                        viewModel.addSubjectChapter(subject.trim(), chapter.trim())
                        chapter = ""
                        Toast.makeText(context, "Added successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Please enter both Subject and Chapter", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add Chapter")
            }
            Spacer(modifier = Modifier.height(16.dp))
            
            val groupedSubjects = subjectsChapters.groupBy { it.subject }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                groupedSubjects.forEach { (subject, chapters) ->
                    item {
                        Text(
                            text = subject,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                        )
                    }
                    items(chapters) { item ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = item.chapter, style = MaterialTheme.typography.bodyMedium)
                                IconButton(onClick = { viewModel.deleteSubjectChapter(item) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
