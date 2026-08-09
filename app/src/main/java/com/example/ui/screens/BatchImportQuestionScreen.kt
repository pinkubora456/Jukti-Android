package com.example.ui.screens

import com.example.ui.components.SafeOutlinedTextField

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ui.viewmodel.JuktiViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatchImportQuestionScreen(viewModel: JuktiViewModel) {
    val selectedExams = remember { mutableStateListOf<String>() }
    var targetExamDialogVisible by remember { mutableStateOf(false) }
    val exams by viewModel.examsList.collectAsState()
    
    var questionForExpanded by remember { mutableStateOf(false) }
    var questionFor by remember { mutableStateOf("Free") }
    
    var pastedCsvData by remember { mutableStateOf("") }
    val context = androidx.compose.ui.platform.LocalContext.current
    var isImporting by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Batch Import Questions", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo(com.example.ui.viewmodel.Screen.MANAGE_QBANK) }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Box(modifier = Modifier.fillMaxWidth().clickable { targetExamDialogVisible = true }) {
                    SafeOutlinedTextField(
                        value = if (selectedExams.isEmpty()) "Select Target Exams..." else selectedExams.joinToString(", "),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Target Exams (Multiple)") },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = false,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }

            item {
                ExposedDropdownMenuBox(
                    expanded = questionForExpanded,
                    onExpandedChange = { questionForExpanded = !questionForExpanded }
                ) {
                    SafeOutlinedTextField(
                        value = questionFor,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Question For") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = questionForExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = questionForExpanded,
                        onDismissRequest = { questionForExpanded = false }
                    ) {
                        listOf("Free", "Premium").forEach { selectionOption ->
                            DropdownMenuItem(
                                text = { Text(selectionOption) },
                                onClick = {
                                    questionFor = selectionOption
                                    questionForExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Required CSV Format",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Question in English,Question in Assamese,a,a_as,b,b_as,c,c_as,d,d_as,correctAnswer,explanation,explanationAssamese,subject,topic,tags,difficulty",
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = "Example:\n\"Who was the first King of the Ahom Kingdom?\",\"আহোম ৰাজ্যৰ প্ৰথম ৰজা কোন আছিল?\",\"Sukaphaa\",\"চ্যুকাফা\",\"Sutephaa\",\"চ্যুটেফা\",\"Subinphaa\",\"চুবিনফা\",\"Sudangphaa\",\"চুডাংফা\",\"A\",\"Sukaphaa founded the Ahom Kingdom in medieval Assam.\",\"চ্যুকাফাই মধ্যযুগীয় অসমত আহোম ৰাজ্য প্ৰতিষ্ঠা কৰিছিল।\",\"Assam History\",\"Ahom Kingdom\",\"ADRE HS 2024\",\"Medium\"",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { /* Handle CSV Upload */ },
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Icon(Icons.Default.FileUpload, contentDescription = "Upload CSV", modifier = Modifier.padding(end = 8.dp))
                    Text("Select & Upload CSV")
                }
            }
            
            item {
                Text(
                    text = "OR",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    textAlign = TextAlign.Center
                )
            }
            
            item {
                SafeOutlinedTextField(
                    value = pastedCsvData,
                    onValueChange = { pastedCsvData = it },
                    label = { Text("Paste CSV Data Here") },
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    minLines = 5
                )
            }
            
            item {
                Button(
                    onClick = {
                        if (pastedCsvData.isNotBlank() && selectedExams.isNotEmpty()) {
                            isImporting = true
                            viewModel.importCsvQuestions(pastedCsvData, selectedExams.joinToString(", "), questionFor, "Expected") { count ->
                                isImporting = false
                                if (count > 0) {
                                    pastedCsvData = ""
                                    android.widget.Toast.makeText(context, "Successfully imported $count questions!", android.widget.Toast.LENGTH_SHORT).show()
                                } else {
                                    android.widget.Toast.makeText(context, "Failed to import questions. Check format.", android.widget.Toast.LENGTH_LONG).show()
                                }
                            }
                        } else {
                            android.widget.Toast.makeText(context, "Please select target exams and enter CSV data.", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    enabled = !isImporting
                ) {
                    if (isImporting) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Importing...")
                    } else {
                        Text("Import Pasted CSV")
                    }
                }
            }
            
            item {
                Text(
                    text = "Ensure your CSV matches the required format before uploading or pasting.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    if (targetExamDialogVisible) {
        AlertDialog(
            onDismissRequest = { targetExamDialogVisible = false },
            title = { Text("Select Target Exams") },
            text = {
                LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                    items(exams) { exam ->
                        val isSelected = selectedExams.contains(exam.title)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (isSelected) {
                                        selectedExams.remove(exam.title)
                                    } else {
                                        selectedExams.add(exam.title)
                                    }
                                }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = { checked ->
                                    if (checked) {
                                        if (!selectedExams.contains(exam.title)) selectedExams.add(exam.title)
                                    } else {
                                        selectedExams.remove(exam.title)
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(exam.title, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { targetExamDialogVisible = false }) {
                    Text("Done")
                }
            }
        )
    }
}
