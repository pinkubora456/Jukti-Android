package com.example.ui.screens

import com.example.ui.components.SafeOutlinedTextField

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.QuestionEntity
import com.example.ui.components.EditQuestionDialog
import com.example.ui.viewmodel.JuktiViewModel
import com.example.ui.viewmodel.LocalMessageTranslator
import com.example.ui.viewmodel.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportedQuestionsScreen(viewModel: JuktiViewModel) {
    val context = LocalContext.current
    val reportedQuestions by viewModel.reportedQuestions.collectAsState()
    var editingQuestion by remember { mutableStateOf<QuestionEntity?>(null) }

    Scaffold(
        topBar = {
            com.example.ui.components.JuktiTopAppBar(
                title = "Reported Questions",
                subtitle = "Total Pending Reports: ${reportedQuestions.size}",
                onBackClick = { viewModel.navigateTo(Screen.WORKSPACE) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (reportedQuestions.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No reported questions.", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(reportedQuestions) { question ->
                        ReportedQuestionCard(
                            question = question,
                            onEdit = { editingQuestion = question },
                            onDelete = { 
                                viewModel.requestOrDeleteQuestion(question) { _, message ->
                                    Toast.makeText(context, LocalMessageTranslator.translateGeneralMessage(context, message), Toast.LENGTH_LONG).show()
                                }
                            },
                            onNoIssue = { viewModel.resolveReportedQuestion(question) }
                        )
                    }
                }
            }
        }
    }

    editingQuestion?.let { question ->
        EditQuestionDialog(
            question = question,
            onDismiss = { editingQuestion = null },
            onSave = { updated ->
                viewModel.updateQuestionAndResolve(updated)
                editingQuestion = null
            }
        )
    }
}

@Composable
fun ReportedQuestionCard(
    question: QuestionEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onNoIssue: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Q: ${question.questionEn}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit")
                }
                TextButton(onClick = onNoIssue, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)) {
                    Icon(Icons.Default.Check, contentDescription = "No Issue Found")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("No Issue")
                }
                TextButton(onClick = onDelete, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete")
                }
            }
        }
    }
}


