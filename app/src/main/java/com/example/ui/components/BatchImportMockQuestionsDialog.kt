package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.QuestionEntity
import com.example.ui.viewmodel.JuktiViewModel
import com.example.util.BatchValidationResult
import com.example.util.CsvQuestionParser
import com.example.util.ParsedQuestionRow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatchImportMockQuestionsDialog(
    viewModel: JuktiViewModel,
    defaultSubject: String = "General Studies",
    defaultChapter: String = "General",
    defaultExamCategory: String = "",
    isMockPremium: Boolean = false,
    isGeneralQBankImport: Boolean = false,
    onDismiss: () -> Unit,
    onQuestionsImported: (List<Long>, Int) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val allExistingQuestions by viewModel.questions.collectAsState()
    val examsList by viewModel.examsList.collectAsState()

    val selectedExams = remember {
        mutableStateListOf<String>().apply {
            if (defaultExamCategory.isNotBlank()) {
                addAll(defaultExamCategory.split(",").map { it.trim() }.filter { it.isNotEmpty() })
            }
        }
    }

    var questionFor by remember {
        mutableStateOf("Premium")
    }

    var targetExamDialogVisible by remember { mutableStateOf(false) }
    var questionForExpanded by remember { mutableStateOf(false) }

    var csvInputText by remember { mutableStateOf("") }
    var selectedFileName by remember { mutableStateOf<String?>(null) }
    var selectedInputTab by remember { mutableIntStateOf(0) } // 0 = Paste CSV, 1 = Upload File
    var selectedPreviewTab by remember { mutableIntStateOf(0) } // 0 = Valid, 1 = Invalid

    var addToQuestionBank by remember { mutableStateOf(true) }
    
    // Force addToQuestionBank to true if this is a general Q-Bank import
    if (isGeneralQBankImport) {
        addToQuestionBank = true
    }
    var isProcessing by remember { mutableStateOf(false) }
    var validationResult by remember { mutableStateOf<BatchValidationResult?>(null) }
    var showTemplateDialog by remember { mutableStateOf(false) }

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
                        selectedFileName = uri.lastPathSegment ?: "questions.csv"
                        validationResult = CsvQuestionParser.validateAndParseQuestions(
                            csvText = content,
                            defaultSubject = defaultSubject,
                            defaultChapter = defaultChapter,
                            defaultExamCategory = selectedExams.joinToString(", "),
                            isPremium = questionFor.equals("Premium", ignoreCase = true),
                            existingQuestions = allExistingQuestions
                        )
                        Toast.makeText(context, "CSV file loaded successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Selected file is empty.", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Failed to read file: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // Trigger validation when input text changes manually
    fun runValidation(
        text: String = csvInputText,
        targetExamsStr: String = selectedExams.joinToString(", "),
        isPrem: Boolean = questionFor.equals("Premium", ignoreCase = true)
    ) {
        csvInputText = text
        if (text.isNotBlank()) {
            validationResult = CsvQuestionParser.validateAndParseQuestions(
                csvText = text,
                defaultSubject = defaultSubject,
                defaultChapter = defaultChapter,
                defaultExamCategory = targetExamsStr,
                isPremium = isPrem,
                existingQuestions = allExistingQuestions
            )
        } else {
            validationResult = null
        }
    }

    Dialog(
        onDismissRequest = { if (!isProcessing) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 24.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.CloudUpload,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Batch Import Questions",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = if (isGeneralQBankImport) "Import multiple questions at once directly into Question Bank." else "Import multiple questions at once for this mock test.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        enabled = !isProcessing
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Input Mode Tabs
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
                                Icon(Icons.Default.EditNote, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Paste CSV Data", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    )
                    Tab(
                        selected = selectedInputTab == 1,
                        onClick = { selectedInputTab = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Upload CSV File", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Content Area inside LazyColumn
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Batch Configuration Settings Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Tune,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Batch Import Settings",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    // Target Exams (Multiple) Box
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { targetExamDialogVisible = true }
                                    ) {
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
                                        Box(
                                            modifier = Modifier
                                                .matchParentSize()
                                                .clickable { targetExamDialogVisible = true }
                                        )
                                    }

                                    // Question For (Free / Premium) Dropdown
                                    ExposedDropdownMenuBox(
                                        expanded = questionForExpanded,
                                        onExpandedChange = { questionForExpanded = !questionForExpanded },
                                        modifier = Modifier.fillMaxWidth()
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
                                            listOf("Free", "Premium").forEach { option ->
                                                DropdownMenuItem(
                                                    text = { Text(option) },
                                                    onClick = {
                                                        questionFor = option
                                                        questionForExpanded = false
                                                        runValidation(isPrem = option.equals("Premium", ignoreCase = true))
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Upload / Paste section
                    item {
                        if (selectedInputTab == 1) {
                            // File Picker Card
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                                shape = RoundedCornerShape(14.dp),
                                border = CardDefaults.outlinedCardBorder()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        Icons.Default.InsertDriveFile,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(40.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = selectedFileName ?: "No CSV file selected yet",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (selectedFileName != null) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selectedFileName != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Button(
                                            onClick = {
                                                filePickerLauncher.launch("text/*")
                                            },
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(if (selectedFileName == null) "Select CSV File" else "Change File")
                                        }

                                        OutlinedButton(
                                            onClick = { showTemplateDialog = true },
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("View Template")
                                        }
                                    }
                                }
                            }
                        } else {
                            // Paste Text Area
                            Column {
                                SafeOutlinedTextField(
                                    value = csvInputText,
                                    onValueChange = { runValidation(it) },
                                    label = { Text("Paste CSV Content Here") },
                                    placeholder = { Text(CsvQuestionParser.SAMPLE_CSV_HEADER) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(160.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TextButton(onClick = { showTemplateDialog = true }) {
                                        Icon(Icons.Default.FormatQuote, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("View / Copy Sample CSV", style = MaterialTheme.typography.bodySmall)
                                    }

                                    if (csvInputText.isNotBlank()) {
                                        TextButton(onClick = { runValidation("") }) {
                                            Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Clear", style = MaterialTheme.typography.bodySmall)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Validation Summary Banner & Stats
                    if (validationResult != null) {
                        val res = validationResult!!
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "Validation Summary",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        // Total Found
                                        StatChip(
                                            label = "Total",
                                            count = res.totalRows,
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.weight(1f)
                                        )

                                        // Valid Questions
                                        StatChip(
                                            label = "Valid",
                                            count = res.validCount,
                                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                            contentColor = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.weight(1f)
                                        )

                                        // Invalid Questions
                                        StatChip(
                                            label = "Invalid",
                                            count = res.invalidCount,
                                            containerColor = if (res.invalidCount > 0) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant,
                                            contentColor = if (res.invalidCount > 0) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.weight(1f)
                                        )

                                        // Existing in Q-Bank
                                        if (res.duplicateInQBankRows.isNotEmpty()) {
                                            StatChip(
                                                label = "In Q-Bank",
                                                count = res.duplicateInQBankRows.size,
                                                containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f),
                                                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Preview Tabs (Valid vs Invalid)
                        item {
                            TabRow(
                                selectedTabIndex = selectedPreviewTab,
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                modifier = Modifier.clip(RoundedCornerShape(10.dp))
                            ) {
                                Tab(
                                    selected = selectedPreviewTab == 0,
                                    onClick = { selectedPreviewTab = 0 },
                                    text = {
                                        Text(
                                            text = "Valid Questions (${res.validCount})",
                                            fontWeight = if (selectedPreviewTab == 0) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                )
                                Tab(
                                    selected = selectedPreviewTab == 1,
                                    onClick = { selectedPreviewTab = 1 },
                                    text = {
                                        Text(
                                            text = "Invalid Rows (${res.invalidCount})",
                                            color = if (res.invalidCount > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                                            fontWeight = if (selectedPreviewTab == 1) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                )
                            }
                        }

                        // Questions Preview List
                        if (selectedPreviewTab == 0) {
                            if (res.validRows.isEmpty()) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 24.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "No valid questions found in this batch.",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            } else {
                                items(res.validRows) { itemRow ->
                                    ValidQuestionCard(itemRow = itemRow)
                                }
                            }
                        } else {
                            if (res.invalidRows.isEmpty()) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 24.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "No invalid rows! All questions in this batch are valid.",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            } else {
                                items(res.invalidRows) { itemRow ->
                                    InvalidQuestionCard(itemRow = itemRow)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Question Bank Option Card
                if (!isGeneralQBankImport) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { addToQuestionBank = !addToQuestionBank },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = addToQuestionBank,
                                onCheckedChange = { addToQuestionBank = it }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Add these questions to Question Bank",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = if (addToQuestionBank)
                                        "Questions will be added to this Mock Test AND saved in Question Bank."
                                    else
                                        "Questions will be used ONLY for this Mock Test (not saved to Question Bank).",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Bottom Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        enabled = !isProcessing,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Cancel")
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    val validCount = validationResult?.validCount ?: 0
                    Button(
                        onClick = {
                            val res = validationResult
                            if (res == null || res.validRows.isEmpty()) {
                                Toast.makeText(context, "No valid questions to import.", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            isProcessing = true
                            // Separate new questions to insert from existing questions to reuse
                            val newQuestionsToInsert = mutableListOf<QuestionEntity>()
                            val reusableExistingIds = mutableListOf<Long>()

                            for (row in res.validRows) {
                                if (row.isExistingInQBank && row.existingQBankId != null) {
                                    reusableExistingIds.add(row.existingQBankId)
                                } else if (row.question != null) {
                                    newQuestionsToInsert.add(row.question)
                                }
                            }

                            viewModel.batchImportQuestionsForMock(
                                questionsToInsert = newQuestionsToInsert,
                                reusableExistingIds = reusableExistingIds,
                                addToQuestionBank = addToQuestionBank
                            ) { assignedIds, newQBankCount, message ->
                                isProcessing = false
                                if (assignedIds.isNotEmpty()) {
                                    val successMsg = if (isGeneralQBankImport) {
                                        "Imported ${newQuestionsToInsert.size} questions to Question Bank"
                                    } else {
                                        "Imported ${assignedIds.size} questions to Mock Test" +
                                                if (newQBankCount > 0) " ($newQBankCount saved to Question Bank)" else ""
                                    }
                                    Toast.makeText(context, successMsg, Toast.LENGTH_LONG).show()
                                    onQuestionsImported(assignedIds, newQBankCount)
                                    onDismiss()
                                } else {
                                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                }
                            }
                        },
                        enabled = validCount > 0 && !isProcessing,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        if (isProcessing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Importing...")
                        } else {
                            Icon(Icons.Default.AddCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            val buttonText = if (isGeneralQBankImport) {
                                if (validCount > 0) "Import $validCount Questions" else "Import Questions"
                            } else {
                                if (validCount > 0) "Add $validCount Questions to Mock" else "Add Questions to Mock"
                            }
                            Text(buttonText)
                        }
                    }
                }
            }
        }
    }

    // Template Dialog
    if (showTemplateDialog) {
        val sampleText = CsvQuestionParser.getSampleCsvTemplate()
        AlertDialog(
            onDismissRequest = { showTemplateDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Description, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("CSV Format & Sample", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Your CSV file should have the following columns:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = CsvQuestionParser.SAMPLE_CSV_HEADER,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Sample Rows:",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 160.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        LazyColumn {
                            item {
                                Text(
                                    text = sampleText,
                                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
                                )
                            }
                        }
                    }
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
                    Text("Copy Template")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTemplateDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Target Exams Selection Dialog
    if (targetExamDialogVisible) {
        AlertDialog(
            onDismissRequest = { targetExamDialogVisible = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Assignment, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Select Target Exams", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                if (examsList.isEmpty()) {
                    Text("No exams available. Please add exams in Manage Exams first.", color = MaterialTheme.colorScheme.error)
                } else {
                    val availableExams = examsList.map { it.title }.distinct()
                    LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                        items(availableExams) { examTitle ->
                            val isSelected = selectedExams.contains(examTitle)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (isSelected) {
                                            selectedExams.remove(examTitle)
                                        } else {
                                            selectedExams.add(examTitle)
                                        }
                                        runValidation(targetExamsStr = selectedExams.joinToString(", "))
                                    }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = { checked ->
                                    if (checked) {
                                        if (!selectedExams.contains(examTitle)) selectedExams.add(examTitle)
                                    } else {
                                        selectedExams.remove(examTitle)
                                    }
                                    runValidation(targetExamsStr = selectedExams.joinToString(", "))
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(examTitle, style = MaterialTheme.typography.bodyLarge)
                        }
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

@Composable
private fun StatChip(
    label: String,
    count: Int,
    containerColor: androidx.compose.ui.graphics.Color,
    contentColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor.copy(alpha = 0.85f),
                maxLines = 1
            )
        }
    }
}

@Composable
private fun ValidQuestionCard(itemRow: ParsedQuestionRow) {
    val q = itemRow.question ?: return
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Row number and tags
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Row #${itemRow.rowNumber}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

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
                    Surface(
                        color = if (q.isPremium) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = if (q.isPremium) "Premium" else "Free",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (q.isPremium) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    if (itemRow.isExistingInQBank) {
                        Surface(
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "In Q-Bank",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
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

            Spacer(modifier = Modifier.height(6.dp))

            // Question Text
            Text(
                text = q.questionEn,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            if (q.questionAs.isNotBlank()) {
                Text(
                    text = q.questionAs,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Options Preview Grid
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                OptionPreviewRow("A", q.optionAEn, q.correctOptionIndex == 0)
                OptionPreviewRow("B", q.optionBEn, q.correctOptionIndex == 1)
                if (q.optionCEn.isNotBlank()) OptionPreviewRow("C", q.optionCEn, q.correctOptionIndex == 2)
                if (q.optionDEn.isNotBlank()) OptionPreviewRow("D", q.optionDEn, q.correctOptionIndex == 3)
            }
        }
    }
}

@Composable
private fun OptionPreviewRow(optionKey: String, optionText: String, isCorrect: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(
                if (isCorrect) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                else MaterialTheme.colorScheme.surface
            )
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$optionKey.",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = if (isCorrect) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = optionText,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (isCorrect) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isCorrect) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (isCorrect) {
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = "Correct Answer",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
private fun InvalidQuestionCard(itemRow: ParsedQuestionRow) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Row #${itemRow.rowNumber} (Failed Validation)",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            }

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

            // Error Pills
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
