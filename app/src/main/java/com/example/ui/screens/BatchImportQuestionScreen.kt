package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.QuestionEntity
import com.example.data.repository.normalizeChapterName
import com.example.data.repository.normalizeSubjectName
import com.example.ui.components.JuktiTopAppBar
import com.example.ui.components.SafeOutlinedTextField
import com.example.ui.viewmodel.JuktiViewModel
import com.example.ui.viewmodel.Screen
import com.example.util.BatchValidationResult
import com.example.util.CsvQuestionParser
import com.example.util.ParsedQuestionRow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun BatchImportQuestionScreen(viewModel: JuktiViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val allExistingQuestions by viewModel.questions.collectAsState()
    val examsList by viewModel.examsList.collectAsState()
    val allSubjectsChapters by viewModel.allSubjectsChapters.collectAsState()

    // Available exams for selection
    val availableExams = remember(examsList) {
        examsList.map { it.title }.filter { it.isNotBlank() }.distinct()
    }

    // Options state
    var questionFor by remember { mutableStateOf("Free") } // Free or Premium
    val selectedExams = remember { mutableStateListOf<String>() }

    // Individual question selection and customization state
    val selectedRowNumbers = remember { mutableStateListOf<Int>() }
    val individualQuestionOverrides = remember { mutableStateMapOf<Int, Boolean>() } // rowNumber -> isPremium override

    var showFormatGuideDialog by remember { mutableStateOf(false) }

    // Input state
    var selectedInputTab by remember { mutableIntStateOf(0) } // 0 = Upload File, 1 = Paste CSV
    var csvInputText by remember { mutableStateOf("") }
    var selectedFileName by remember { mutableStateOf<String?>(null) }
    var selectedFileSize by remember { mutableStateOf<String?>(null) }
    var validationResult by remember { mutableStateOf<BatchValidationResult?>(null) }

    // Preview state
    var selectedPreviewTab by remember { mutableIntStateOf(0) } // 0 = Ready to Import, 1 = Duplicates, 2 = Invalid

    // Import progress and success dialog
    var isImporting by remember { mutableStateOf(false) }
    var importSuccessSummary by remember { mutableStateOf<Pair<Int, Int>?>(null) } // <importedCount, skippedCount>

    // Function to re-run validation
    fun runValidation(
        text: String = csvInputText,
        exams: String = selectedExams.joinToString(", "),
        isPrem: Boolean = questionFor.equals("Premium", ignoreCase = true)
    ) {
        if (text.isNotBlank()) {
            validationResult = CsvQuestionParser.validateAndParseQuestions(
                csvText = text,
                defaultSubject = "General Studies",
                defaultChapter = "General",
                defaultExamCategory = exams,
                isPremium = isPrem,
                existingQuestions = allExistingQuestions
            )
        } else {
            validationResult = null
        }
    }

    // File picker launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                try {
                    val content = withContext(Dispatchers.IO) {
                        context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                    }
                    if (!content.isNullOrBlank()) {
                        csvInputText = content
                        val fileName = uri.lastPathSegment ?: "questions.csv"
                        selectedFileName = fileName
                        selectedFileSize = "${(content.length / 1024).coerceAtLeast(1)} KB"
                        runValidation(text = content)
                        Toast.makeText(context, "CSV file loaded successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Selected CSV file is empty.", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Failed to read file: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // Calculate final questions to import
    val res = validationResult
    val validRows = res?.validRows ?: emptyList()
    val duplicateInQBankRows = res?.duplicateInQBankRows ?: emptyList()
    val invalidRows = res?.invalidRows ?: emptyList()

    // Automatically exclude questions already existing in Question Bank
    val validNonDuplicateRows = remember(res) {
        res?.validRows?.filter { !it.isExistingInQBank } ?: emptyList()
    }

    // When new validation results arrive, auto-select all non-duplicate valid rows
    LaunchedEffect(res) {
        selectedRowNumbers.clear()
        selectedRowNumbers.addAll(validNonDuplicateRows.map { it.rowNumber })
        individualQuestionOverrides.clear()
    }

    val questionsToImport: List<QuestionEntity> = remember(
        validNonDuplicateRows,
        selectedRowNumbers.toList(),
        individualQuestionOverrides.toMap()
    ) {
        validNonDuplicateRows
            .filter { selectedRowNumbers.contains(it.rowNumber) }
            .mapNotNull { row ->
                val baseQ = row.question ?: return@mapNotNull null
                val customPrem = individualQuestionOverrides[row.rowNumber]
                if (customPrem != null) {
                    baseQ.copy(isPremium = customPrem)
                } else {
                    baseQ
                }
            }
    }

    // Duplicate questions in Question Bank are automatically skipped
    val skippedCount = duplicateInQBankRows.size
    val readyToImportCount = questionsToImport.size

    Scaffold(
        modifier = Modifier.testTag("batch_import_question_screen").imePadding(),
        topBar = {
            JuktiTopAppBar(
                title = "Batch Import Questions",
                subtitle = "Question Bank",
                onBackClick = { viewModel.navigateTo(Screen.MANAGE_QBANK) },
                actions = {
                    IconButton(
                        onClick = { showFormatGuideDialog = true },
                        modifier = Modifier.testTag("btn_format_guide_topbar")
                    ) {
                        Icon(Icons.Default.HelpOutline, contentDescription = "CSV Format Guide")
                    }
                }
            )
        },
        bottomBar = {
            if (res != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    tonalElevation = 8.dp,
                    shadowElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "$readyToImportCount of ${validNonDuplicateRows.size} selected to import",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (readyToImportCount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (skippedCount > 0) {
                                Text(
                                    text = "$skippedCount duplicate questions in Q-Bank automatically skipped",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Button(
                            onClick = {
                                if (questionsToImport.isEmpty()) {
                                    Toast.makeText(context, "Please select at least 1 question to import.", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                isImporting = true
                                viewModel.batchImportQuestionsToQBank(questionsToImport) { importedCount, message ->
                                    isImporting = false
                                    if (importedCount > 0) {
                                        importSuccessSummary = Pair(importedCount, skippedCount)
                                    } else {
                                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                    }
                                }
                            },
                            enabled = readyToImportCount > 0 && !isImporting,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("btn_import_to_qbank")
                        ) {
                            if (isImporting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Importing...")
                            } else {
                                Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Import ($readyToImportCount) to Question Bank")
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Info & Template Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.UploadFile,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Question Bank Batch Importer",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Bulk import MCQs with bilingual text, options, explanations and exam tags.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { showFormatGuideDialog = true },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(vertical = 8.dp, horizontal = 12.dp)
                            ) {
                                Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Format Guide", style = MaterialTheme.typography.labelMedium)
                            }

                            OutlinedButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Sample CSV Template", CsvQuestionParser.getSampleCsvTemplate())
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "Sample CSV copied to clipboard!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(vertical = 8.dp, horizontal = 12.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Copy Sample CSV", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
            }

            // Targeted Exam (multiple selection) & Question Type (Free or Premium)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Section: Targeted Exam (Dropdown multiple selection)
                        var targetExamExpanded by remember { mutableStateOf(false) }

                        ExposedDropdownMenuBox(
                            expanded = targetExamExpanded,
                            onExpandedChange = { targetExamExpanded = !targetExamExpanded }
                        ) {
                            SafeOutlinedTextField(
                                value = if (selectedExams.isEmpty()) "Select Target Exam(s)" else selectedExams.joinToString(", "),
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Targeted Exam (Multiple)") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = targetExamExpanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = targetExamExpanded,
                                onDismissRequest = { targetExamExpanded = false }
                            ) {
                                availableExams.forEach { examTitle ->
                                    val isSelected = selectedExams.contains(examTitle)
                                    DropdownMenuItem(
                                        text = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Checkbox(checked = isSelected, onCheckedChange = null)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(examTitle)
                                            }
                                        },
                                        onClick = {
                                            if (isSelected) selectedExams.remove(examTitle)
                                            else selectedExams.add(examTitle)
                                            runValidation(exams = selectedExams.joinToString(", "))
                                        }
                                    )
                                }
                            }
                        }

                        // Section: Question Type (Dropdown)
                        var questionTypeExpanded by remember { mutableStateOf(false) }

                        ExposedDropdownMenuBox(
                            expanded = questionTypeExpanded,
                            onExpandedChange = { questionTypeExpanded = !questionTypeExpanded }
                        ) {
                            SafeOutlinedTextField(
                                value = questionFor,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Question Type") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = questionTypeExpanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = questionTypeExpanded,
                                onDismissRequest = { questionTypeExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Free") },
                                    onClick = {
                                        questionFor = "Free"
                                        runValidation(isPrem = false)
                                        questionTypeExpanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Premium") },
                                    onClick = {
                                        questionFor = "Premium"
                                        runValidation(isPrem = true)
                                        questionTypeExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Input Method Tabs (Upload CSV File vs Paste CSV Text)
            item {
                TabRow(
                    selectedTabIndex = selectedInputTab,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = selectedInputTab == 0,
                        onClick = { selectedInputTab = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Upload CSV File", fontWeight = FontWeight.SemiBold)
                            }
                        },
                        modifier = Modifier.testTag("tab_upload_csv_file")
                    )
                    Tab(
                        selected = selectedInputTab == 1,
                        onClick = { selectedInputTab = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.EditNote, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Paste CSV Text", fontWeight = FontWeight.SemiBold)
                            }
                        },
                        modifier = Modifier.testTag("tab_paste_csv_text")
                    )
                }
            }

            // Tab 0: Upload File Card
            if (selectedInputTab == 0) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (selectedFileName == null) {
                                Icon(
                                    Icons.Default.CloudUpload,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(48.dp)
                                )
                                Text(
                                    text = "Select a .CSV file from your device",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Supports standard comma-separated and quoted values",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Button(
                                    onClick = { filePickerLauncher.launch("*/*") },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.testTag("btn_select_csv_file")
                                ) {
                                    Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Browse CSV File")
                                }
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.size(44.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                Icons.Default.InsertDriveFile,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = selectedFileName ?: "questions.csv",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = selectedFileSize ?: "CSV Data Loaded",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            selectedFileName = null
                                            selectedFileSize = null
                                            csvInputText = ""
                                            validationResult = null
                                        }
                                    ) {
                                        Icon(Icons.Default.DeleteOutline, contentDescription = "Remove File", tint = MaterialTheme.colorScheme.error)
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    OutlinedButton(
                                        onClick = { filePickerLauncher.launch("*/*") },
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Choose Different File")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Tab 1: Paste CSV Text
            if (selectedInputTab == 1) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = csvInputText,
                            onValueChange = { newText ->
                                csvInputText = newText
                                runValidation(text = newText)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 160.dp, max = 280.dp)
                                .testTag("tf_paste_csv_data"),
                            placeholder = {
                                Text(
                                    "Paste CSV content here...\ne.g.\nstatement,statementAssamese,a,a_as,b,b_as,c,c_as,d,d_as,correctAnswer,explanation,explanationAssamese,subject,topic,tags,difficulty\n\"Who was the first King of the Ahom Kingdom?\",\"আহোম ৰাজ্যৰ প্ৰথম ৰজা কোন আছিল?\",\"Sukaphaa\",\"চ্যুকাফা\",\"Sutephaa\",\"চ্যুটেফা\",\"Subinphaa\",\"চুবিনফা\",\"Sudangphaa\",\"চুডাংফা\",\"A\",\"Sukaphaa founded the Ahom Kingdom in medieval Assam.\",\"চ্যুকাফাই মধ্যযুগীয় অসমত আহোম ৰাজ্য প্ৰতিষ্ঠা কৰিছিল।\",\"Assam History\",\"Ahom Kingdom\",\"ADRE HS 2024\",\"Medium\"",
                                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            },
                            textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (csvInputText.isBlank()) "0 characters"
                                else "${csvInputText.lines().count { it.isNotBlank() }} lines • ${csvInputText.length} chars",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                TextButton(
                                    onClick = {
                                        val sample = CsvQuestionParser.getSampleCsvTemplate()
                                        csvInputText = sample
                                        runValidation(text = sample)
                                    }
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Load Sample Rows")
                                }

                                if (csvInputText.isNotBlank()) {
                                    TextButton(
                                        onClick = {
                                            csvInputText = ""
                                            validationResult = null
                                        }
                                    ) {
                                        Text("Clear", color = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Real-Time Stats Bar
            if (res != null) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Validation & Batch Summary",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            BatchStatCard(
                                label = "Total Parsed",
                                count = res.totalRows,
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f)
                            )
                            BatchStatCard(
                                label = "Selected",
                                count = readyToImportCount,
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.weight(1f)
                            )
                            if (duplicateInQBankRows.isNotEmpty()) {
                                BatchStatCard(
                                    label = "In Q-Bank (Auto-Skip)",
                                    count = duplicateInQBankRows.size,
                                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (invalidRows.isNotEmpty()) {
                                BatchStatCard(
                                    label = "Invalid Rows",
                                    count = invalidRows.size,
                                    containerColor = MaterialTheme.colorScheme.errorContainer,
                                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        // Preview Category Tabs
                        val previewTabs = mutableListOf<String>()
                        previewTabs.add("Ready (${validNonDuplicateRows.size})")
                        if (duplicateInQBankRows.isNotEmpty()) {
                            previewTabs.add("In Q-Bank (${duplicateInQBankRows.size})")
                        }
                        if (invalidRows.isNotEmpty()) {
                            previewTabs.add("Invalid (${invalidRows.size})")
                        }

                        if (previewTabs.size > 1) {
                            ScrollableTabRow(
                                selectedTabIndex = selectedPreviewTab.coerceIn(0, previewTabs.size - 1),
                                edgePadding = 0.dp,
                                containerColor = Color.Transparent,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                previewTabs.forEachIndexed { index, title ->
                                    Tab(
                                        selected = selectedPreviewTab == index,
                                        onClick = { selectedPreviewTab = index },
                                        text = { Text(title, fontWeight = FontWeight.SemiBold) }
                                    )
                                }
                            }
                        }
                    }
                }

                // Preview Content
                when (selectedPreviewTab) {
                    0 -> {
                        // Ready Questions
                        if (validNonDuplicateRows.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (validRows.isEmpty()) "No valid questions parsed yet."
                                        else "All valid questions in this batch already exist in Question Bank and are automatically skipped.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        } else {
                            // Individual Selection Action Bar (Select All / Deselect All)
                            item {
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val allSelected = selectedRowNumbers.size == validNonDuplicateRows.size
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.clickable {
                                                if (allSelected) {
                                                    selectedRowNumbers.clear()
                                                } else {
                                                    selectedRowNumbers.clear()
                                                    selectedRowNumbers.addAll(validNonDuplicateRows.map { it.rowNumber })
                                                }
                                            }
                                        ) {
                                            Checkbox(
                                                checked = allSelected,
                                                onCheckedChange = { checked ->
                                                    if (checked) {
                                                        selectedRowNumbers.clear()
                                                        selectedRowNumbers.addAll(validNonDuplicateRows.map { it.rowNumber })
                                                    } else {
                                                        selectedRowNumbers.clear()
                                                    }
                                                }
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "${selectedRowNumbers.size} of ${validNonDuplicateRows.size} Selected",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            if (selectedRowNumbers.isNotEmpty()) {
                                                TextButton(
                                                    onClick = { selectedRowNumbers.clear() },
                                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                                                ) {
                                                    Text("Deselect All", style = MaterialTheme.typography.labelSmall)
                                                }
                                            }
                                            if (selectedRowNumbers.size < validNonDuplicateRows.size) {
                                                TextButton(
                                                    onClick = {
                                                        selectedRowNumbers.clear()
                                                        selectedRowNumbers.addAll(validNonDuplicateRows.map { it.rowNumber })
                                                    },
                                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                                                ) {
                                                    Text("Select All", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            items(validNonDuplicateRows, key = { it.rowNumber }) { row ->
                                val isSelected = selectedRowNumbers.contains(row.rowNumber)
                                val currentIsPremium = individualQuestionOverrides[row.rowNumber] ?: (row.question?.isPremium ?: false)
                                QBankValidQuestionCard(
                                    itemRow = row,
                                    isSelected = isSelected,
                                    onToggleSelect = {
                                        if (isSelected) selectedRowNumbers.remove(row.rowNumber)
                                        else selectedRowNumbers.add(row.rowNumber)
                                    },
                                    isPremium = currentIsPremium,
                                    onTogglePremium = {
                                        individualQuestionOverrides[row.rowNumber] = !currentIsPremium
                                    }
                                )
                            }
                        }
                    }
                    1 -> {
                        // Duplicates (Already in Question Bank)
                        if (duplicateInQBankRows.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("No duplicate questions found.", style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        } else {
                            item {
                                Surface(
                                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.Info,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.tertiary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "${duplicateInQBankRows.size} questions already exist in your Question Bank and will be automatically skipped.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onTertiaryContainer
                                        )
                                    }
                                }
                            }

                            items(duplicateInQBankRows) { row ->
                                QBankDuplicateQuestionCard(itemRow = row)
                            }
                        }
                    }
                    else -> {
                        // Invalid Rows
                        if (invalidRows.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("No invalid rows! All questions in this batch are valid.", color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        } else {
                            items(invalidRows) { row ->
                                QBankInvalidQuestionCard(itemRow = row)
                            }
                        }
                    }
                }
            }

            // Bottom spacing
            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }

    // Format Guide & Sample Template Dialog
    if (showFormatGuideDialog) {
        val sampleText = CsvQuestionParser.getSampleCsvTemplate()
        AlertDialog(
            onDismissRequest = { showFormatGuideDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.HelpOutline, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("CSV Format Guide", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Your CSV file can use either the full 19-column schema or the simplified 7-column schema:",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "Standard Format (17 columns):",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = CsvQuestionParser.SAMPLE_CSV_HEADER,
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "Simplified Format (7 columns):",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Question, Option A, Option B, Option C, Option D, Correct Answer (A/B/C/D), Subject",
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Text(
                        text = "Tips: Values with commas or quotes should be wrapped in double quotes. Correct answer must be A, B, C, or D.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Sample CSV Template", sampleText)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Sample template copied to clipboard!", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copy Sample CSV")
                }
            },
            dismissButton = {
                TextButton(onClick = { showFormatGuideDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Success Summary Dialog
    importSuccessSummary?.let { (importedCount, skipped) ->
        AlertDialog(
            onDismissRequest = {
                importSuccessSummary = null
                viewModel.navigateTo(Screen.MANAGE_QBANK)
            },
            icon = {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            },
            title = {
                Text(
                    text = "Import Successful!",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Successfully added $importedCount questions directly to your Question Bank.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (skipped > 0) {
                        Text(
                            text = "$skipped duplicate questions already in Question Bank were safely skipped.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        importSuccessSummary = null
                        viewModel.navigateTo(Screen.ALL_QUESTIONS)
                    }
                ) {
                    Icon(Icons.Default.FormatListBulleted, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("View Question Bank")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        importSuccessSummary = null
                        csvInputText = ""
                        selectedFileName = null
                        validationResult = null
                    }
                ) {
                    Text("Import More")
                }
            }
        )
    }
}

@Composable
private fun BatchStatCard(
    label: String,
    count: Int,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor.copy(alpha = 0.85f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun QBankValidQuestionCard(
    itemRow: ParsedQuestionRow,
    isSelected: Boolean,
    onToggleSelect: () -> Unit,
    isPremium: Boolean,
    onTogglePremium: () -> Unit
) {
    val q = itemRow.question ?: return
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleSelect() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        ),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            if (isSelected) 1.5.dp else 1.dp,
            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header with Checkbox for individual selection, Row # and Metadata Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { onToggleSelect() },
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Row #${itemRow.rowNumber}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (q.examCategory.isNotBlank()) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = q.examCategory,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Individual Option: Tap badge to toggle Free / Premium for this question
                    Surface(
                        color = if (isPremium) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.clickable { onTogglePremium() }
                    ) {
                        Text(
                            text = if (isPremium) "⭐ Premium" else "🆓 Free",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isPremium) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "${q.subject} • ${q.topic}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Question Statement
            Text(
                text = q.questionEn,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            if (q.questionAs.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = q.questionAs,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Options Preview
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                QBankOptionRow("A", q.optionAEn, q.optionAAs, q.correctOptionIndex == 0)
                QBankOptionRow("B", q.optionBEn, q.optionBAs, q.correctOptionIndex == 1)
                if (q.optionCEn.isNotBlank()) QBankOptionRow("C", q.optionCEn, q.optionCAs, q.correctOptionIndex == 2)
                if (q.optionDEn.isNotBlank()) QBankOptionRow("D", q.optionDEn, q.optionDAs, q.correctOptionIndex == 3)
            }

            // Explanation Preview if present
            if (q.explanationEn.isNotBlank() || q.explanationAs.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "💡 ${q.explanationEn.ifBlank { q.explanationAs }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun QBankOptionRow(optionKey: String, optionEn: String, optionAs: String, isCorrect: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(
                if (isCorrect) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                else MaterialTheme.colorScheme.surface
            )
            .padding(horizontal = 8.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$optionKey.",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = if (isCorrect) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(6.dp))
        val textDisplay = if (optionAs.isNotBlank() && optionAs != optionEn) "$optionEn ($optionAs)" else optionEn
        Text(
            text = textDisplay,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (isCorrect) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isCorrect) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        if (isCorrect) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = "Correct Answer",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun QBankDuplicateQuestionCard(itemRow: ParsedQuestionRow) {
    val q = itemRow.question ?: return
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.25f)),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Row #${itemRow.rowNumber} (Existing Question)",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.tertiary
                )
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "Automatically Skipped",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = q.questionEn,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Already found in Question Bank (ID: #${itemRow.existingQBankId ?: "N/A"}).",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun QBankInvalidQuestionCard(itemRow: ParsedQuestionRow) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "Row #${itemRow.rowNumber} (Validation Failed)",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )

            if (itemRow.rawPreview.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = itemRow.rawPreview,
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                itemRow.errorReasons.forEach { error ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = error,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
