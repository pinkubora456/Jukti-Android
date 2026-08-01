package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.ExamUpdateEntity
import com.example.ui.viewmodel.JuktiViewModel
import com.example.ui.viewmodel.Screen
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageExamPatternCutoffScreen(viewModel: JuktiViewModel) {
    val context = LocalContext.current
    val examUpdates by viewModel.examUpdates.collectAsState()

    // Form states
    var examName by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Syllabus") }
    var titleEn by remember { mutableStateOf("") }
    var titleAs by remember { mutableStateOf("") }
    var detailEn by remember { mutableStateOf("") }
    var detailAs by remember { mutableStateOf("") }
    var officialLink by remember { mutableStateOf("https://assam.gov.in") }
    var isImportantNotice by remember { mutableStateOf(false) }

    // Editing state
    var editingItem by remember { mutableStateOf<ExamUpdateEntity?>(null) }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var examDropdownExpanded by remember { mutableStateOf(false) }

    val categories = listOf("Syllabus", "Pattern", "Cutoff", "Notification", "Admit Card")
    val popularExams = listOf("ADRE", "APSC", "Assam Police", "TET", "Other")

    // Populate form when editing an item
    LaunchedEffect(editingItem) {
        editingItem?.let {
            examName = it.examName
            category = it.category
            titleEn = it.titleEn
            titleAs = it.titleAs
            detailEn = it.detailEn
            detailAs = it.detailAs
            officialLink = it.officialLink
            isImportantNotice = it.isImportantNotice
        }
    }

    fun clearForm() {
        examName = ""
        category = "Syllabus"
        titleEn = ""
        titleAs = ""
        detailEn = ""
        detailAs = ""
        officialLink = "https://assam.gov.in"
        isImportantNotice = false
        editingItem = null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (editingItem != null) "Edit Exam Info" else "Manage Syllabus & Cutoffs", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateTo(Screen.WORKSPACE) },
                        modifier = Modifier.testTag("manage_exam_info_back_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Main content layout using Row for wide screens (desktop/tablet), or simple responsive Scroll
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Form Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = if (editingItem != null) "Edit Item: ${editingItem?.titleEn}" else "Add Syllabus, Pattern, or Cutoff",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Exam Name field with a simple suggestions system
                            Box(modifier = Modifier.weight(1f)) {
                                OutlinedTextField(
                                    value = examName,
                                    onValueChange = { examName = it },
                                    label = { Text("Exam Name") },
                                    modifier = Modifier.fillMaxWidth().testTag("exam_name_input"),
                                    singleLine = true,
                                    trailingIcon = {
                                        IconButton(onClick = { examDropdownExpanded = !examDropdownExpanded }) {
                                            Icon(Icons.Default.Add, contentDescription = "Select Popular")
                                        }
                                    }
                                )
                                DropdownMenu(
                                    expanded = examDropdownExpanded,
                                    onDismissRequest = { examDropdownExpanded = false }
                                ) {
                                    popularExams.forEach { exam ->
                                        DropdownMenuItem(
                                            text = { Text(exam) },
                                            onClick = {
                                                examName = if (exam == "Other") "" else exam
                                                examDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            // Category Selector
                            Box(modifier = Modifier.weight(1f)) {
                                OutlinedTextField(
                                    value = category,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Category") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { categoryDropdownExpanded = true }
                                        .testTag("category_selector"),
                                    enabled = false,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                                DropdownMenu(
                                    expanded = categoryDropdownExpanded,
                                    onDismissRequest = { categoryDropdownExpanded = false }
                                ) {
                                    categories.forEach { cat ->
                                        DropdownMenuItem(
                                            text = { Text(cat) },
                                            onClick = {
                                                category = cat
                                                categoryDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // English Title
                        OutlinedTextField(
                            value = titleEn,
                            onValueChange = { titleEn = it },
                            label = { Text("Title (English)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("title_en_input"),
                            singleLine = true
                        )

                        // Assamese Title
                        OutlinedTextField(
                            value = titleAs,
                            onValueChange = { titleAs = it },
                            label = { Text("Title (Assamese)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("title_as_input"),
                            singleLine = true
                        )

                        // English Detail
                        OutlinedTextField(
                            value = detailEn,
                            onValueChange = { detailEn = it },
                            label = { Text("Details / Content (English)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("detail_en_input"),
                            minLines = 3
                        )

                        // Assamese Detail
                        OutlinedTextField(
                            value = detailAs,
                            onValueChange = { detailAs = it },
                            label = { Text("Details / Content (Assamese)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("detail_as_input"),
                            minLines = 3
                        )

                        // Official Website Link
                        OutlinedTextField(
                            value = officialLink,
                            onValueChange = { officialLink = it },
                            label = { Text("Official Link / Reference URL") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("official_link_input"),
                            singleLine = true
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isImportantNotice,
                                onCheckedChange = { isImportantNotice = it },
                                modifier = Modifier.testTag("is_important_checkbox")
                            )
                            Text(
                                text = "Mark as Important Notice",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (editingItem != null) {
                                OutlinedButton(
                                    onClick = { clearForm() },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Clear, contentDescription = null)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Cancel")
                                }
                            }

                            Button(
                                onClick = {
                                    if (examName.isBlank() || titleEn.isBlank() || detailEn.isBlank()) {
                                        Toast.makeText(context, "Please fill in Exam Name, English Title, and Details", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }

                                    val currentDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())

                                    val itemToSave = ExamUpdateEntity(
                                        id = editingItem?.id ?: 0,
                                        examName = examName.trim(),
                                        category = category,
                                        titleEn = titleEn.trim(),
                                        titleAs = if (titleAs.isBlank()) titleEn.trim() else titleAs.trim(),
                                        updateDate = editingItem?.updateDate ?: currentDate,
                                        detailEn = detailEn.trim(),
                                        detailAs = if (detailAs.isBlank()) detailEn.trim() else detailAs.trim(),
                                        officialLink = officialLink.trim(),
                                        isImportantNotice = isImportantNotice
                                    )

                                    if (editingItem != null) {
                                        viewModel.updateExamUpdate(itemToSave)
                                        Toast.makeText(context, "Updated Successfully!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        viewModel.addExamUpdate(itemToSave)
                                        Toast.makeText(context, "Added Successfully!", Toast.LENGTH_SHORT).show()
                                    }
                                    clearForm()
                                },
                                modifier = Modifier
                                    .weight(1.5f)
                                    .testTag("save_exam_update_btn")
                            ) {
                                Icon(if (editingItem != null) Icons.Default.Save else Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (editingItem != null) "Update Info" else "Add Info")
                            }
                        }
                    }
                }

                Text(
                    text = "Existing Information & Syllabus Cards",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                if (examUpdates.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No entries found.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(examUpdates) { item ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = CardDefaults.outlinedCardBorder()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            SuggestionChip(
                                                onClick = {},
                                                label = { Text(item.examName) }
                                            )
                                            SuggestionChip(
                                                onClick = {},
                                                label = { Text(item.category) },
                                                colors = SuggestionChipDefaults.suggestionChipColors(
                                                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                                                )
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = item.titleEn,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = item.detailEn,
                                            style = MaterialTheme.typography.bodySmall,
                                            maxLines = 2,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Row {
                                        IconButton(
                                            onClick = { editingItem = item },
                                            modifier = Modifier.testTag("edit_update_${item.id}")
                                        ) {
                                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
                                        }
                                        IconButton(
                                            onClick = { viewModel.deleteExamUpdate(item) },
                                            modifier = Modifier.testTag("delete_update_${item.id}")
                                        ) {
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
    }
}
