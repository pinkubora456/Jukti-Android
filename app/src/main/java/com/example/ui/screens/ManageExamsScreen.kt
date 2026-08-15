package com.example.ui.screens

import com.example.ui.components.SafeOutlinedTextField

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.ExamEntity
import com.example.ui.viewmodel.JuktiViewModel
import com.example.ui.viewmodel.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageExamsScreen(viewModel: JuktiViewModel) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editingExam by remember { mutableStateOf<ExamEntity?>(null) }
    
    val examsList by viewModel.examsList.collectAsState()

    Scaffold(
        topBar = {
            com.example.ui.components.JuktiTopAppBar(
                title = "Manage Exams",
                onBackClick = { viewModel.navigateTo(Screen.WORKSPACE) },
                actions = {
                    IconButton(onClick = { viewModel.navigateTo(Screen.EXAM_INFO) }) {
                        Icon(Icons.Default.Info, contentDescription = "Exam Info & Cutoff")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Exam", tint = MaterialTheme.colorScheme.onPrimary)
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
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                itemsIndexed(examsList, key = { index, exam -> if (exam.firebaseId.isNotBlank()) exam.firebaseId else if (exam.id != 0L) exam.id.toString() else "exam_${exam.title}_$index" }) { _, exam ->
                    ExamCard(
                        exam = exam,
                        onEdit = { editingExam = exam },
                        onDelete = { viewModel.deleteExam(exam) },
                        onToggleStatus = {
                            val newStatus = if (exam.status == "Active") "Upcoming" else "Active"
                            viewModel.updateExam(exam.copy(status = newStatus))
                        }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        var name by remember { mutableStateOf("") }
        var desc by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add New Exam Category") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SafeOutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Exam Name (e.g. SSC CGL / Banking)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    SafeOutlinedTextField(
                        value = desc,
                        onValueChange = { desc = it },
                        label = { Text("Board / Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (name.isNotBlank()) {
                        viewModel.addExam(
                            title = name,
                            subtitle = desc.ifBlank { "Competitive Examination" },
                            status = "Active"
                        )
                        showAddDialog = false
                    }
                }) {
                    Text("Add Exam")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    editingExam?.let { exam ->
        var name by remember { mutableStateOf(exam.title) }
        var desc by remember { mutableStateOf(exam.subtitle) }

        AlertDialog(
            onDismissRequest = { editingExam = null },
            title = { Text("Edit Exam Category") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SafeOutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Exam Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    SafeOutlinedTextField(
                        value = desc,
                        onValueChange = { desc = it },
                        label = { Text("Board / Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (name.isNotBlank()) {
                        viewModel.updateExam(exam.copy(title = name, subtitle = desc))
                        editingExam = null
                    }
                }) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingExam = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ExamCard(exam: ExamEntity, onEdit: () -> Unit, onDelete: () -> Unit, onToggleStatus: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (exam.syncStatus == "PENDING") {
                        Icon(Icons.Default.Sync, contentDescription = "Syncing", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(
                        text = exam.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (exam.status == "Active") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = exam.status,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (exam.status == "Active") MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = exam.subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit")
                }
                Spacer(modifier = Modifier.width(4.dp))
                TextButton(onClick = onToggleStatus) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (exam.status == "Active") "Set Upcoming" else "Set Active")
                }
                Spacer(modifier = Modifier.width(4.dp))
                TextButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete")
                }
            }
        }
    }
}
