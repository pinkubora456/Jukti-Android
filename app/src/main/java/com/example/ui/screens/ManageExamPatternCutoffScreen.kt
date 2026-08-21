package com.example.ui.screens

import com.example.ui.components.SafeOutlinedTextField
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
    // Fallback screen showing choice dialog or direct navigation
    var showDialog by remember { mutableStateOf(true) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.navigateTo(Screen.WORKSPACE) },
            title = { Text("Exam Patterns, Syllabus & Cutoff") },
            text = { Text("Please choose an option:") },
            confirmButton = {
                TextButton(onClick = {
                    showDialog = false
                    viewModel.navigateTo(Screen.MANAGE_EXAM_PATTERN_CUTOFF_UPDATE)
                }) {
                    Text("Update (Add New)")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDialog = false
                    viewModel.navigateTo(Screen.MANAGE_EXAM_PATTERN_CUTOFF_VIEW)
                }) {
                    Text("View (All, Edit & Delete)")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateExamPatternCutoffScreen(viewModel: JuktiViewModel) {
    val context = LocalContext.current

    var examName by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Syllabus") }
    var titleEn by remember { mutableStateOf("") }
    var titleAs by remember { mutableStateOf("") }
    var detailEn by remember { mutableStateOf("") }
    var detailAs by remember { mutableStateOf("") }
    var officialLink by remember { mutableStateOf("https://assam.gov.in") }
    var isImportantNotice by remember { mutableStateOf(false) }

    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var examDropdownExpanded by remember { mutableStateOf(false) }

    val categories = listOf("Syllabus", "Pattern", "Cutoff", "Notification", "Admit Card")
    val examsList by viewModel.examsList.collectAsState()
    val popularExams = examsList.map { it.title } + "Other"

    Scaffold(
        topBar = {
            com.example.ui.components.JuktiTopAppBar(
                title = "Add New Exam Pattern, Syllabus & Cutoff",
                onBackClick = { viewModel.navigateTo(Screen.WORKSPACE) },
                backTestTag = "update_exam_info_back_btn"
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "New Exam Information Details",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Exam Name
                        Box(modifier = Modifier.weight(1f)) {
                            SafeOutlinedTextField(
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
                        ExposedDropdownMenuBox(
                            expanded = categoryDropdownExpanded,
                            onExpandedChange = { categoryDropdownExpanded = !categoryDropdownExpanded },
                            modifier = Modifier.weight(1f)
                        ) {
                            SafeOutlinedTextField(
                                value = category,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Category") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                                    .testTag("category_selector")
                            )
                            ExposedDropdownMenu(
                                expanded = categoryDropdownExpanded,
                                onDismissRequest = { categoryDropdownExpanded = false }
                            ) {
                                categories.forEach { cat ->
                                    DropdownMenuItem(
                                        text = { Text(cat) },
                                        onClick = {
                                            category = cat
                                            categoryDropdownExpanded = false
                                        },
                                        modifier = Modifier.testTag("category_item_$cat")
                                    )
                                }
                            }
                        }
                    }

                    // English Title
                    SafeOutlinedTextField(
                        value = titleEn,
                        onValueChange = { titleEn = it },
                        label = { Text("Title (English)") },
                        modifier = Modifier.fillMaxWidth().testTag("title_en_input"),
                        singleLine = true
                    )

                    // Assamese Title
                    SafeOutlinedTextField(
                        value = titleAs,
                        onValueChange = { titleAs = it },
                        label = { Text("Title (Assamese)") },
                        modifier = Modifier.fillMaxWidth().testTag("title_as_input"),
                        singleLine = true
                    )

                    // English Detail
                    SafeOutlinedTextField(
                        value = detailEn,
                        onValueChange = { detailEn = it },
                        label = { Text("Details / Content (English)") },
                        modifier = Modifier.fillMaxWidth().testTag("detail_en_input"),
                        minLines = 3
                    )

                    // Assamese Detail
                    SafeOutlinedTextField(
                        value = detailAs,
                        onValueChange = { detailAs = it },
                        label = { Text("Details / Content (Assamese)") },
                        modifier = Modifier.fillMaxWidth().testTag("detail_as_input"),
                        minLines = 3
                    )

                    // Official Website Link
                    SafeOutlinedTextField(
                        value = officialLink,
                        onValueChange = { officialLink = it },
                        label = { Text("Official Link / Reference URL") },
                        modifier = Modifier.fillMaxWidth().testTag("official_link_input"),
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

                    Button(
                        onClick = {
                            if (examName.isBlank() || titleEn.isBlank() || detailEn.isBlank()) {
                                Toast.makeText(context, "Please fill in Exam Name, English Title, and Details", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            val currentDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())

                            val newItem = ExamUpdateEntity(
                                id = 0,
                                examName = examName.trim(),
                                category = category,
                                titleEn = titleEn.trim(),
                                titleAs = if (titleAs.isBlank()) titleEn.trim() else titleAs.trim(),
                                updateDate = currentDate,
                                detailEn = detailEn.trim(),
                                detailAs = if (detailAs.isBlank()) detailEn.trim() else detailAs.trim(),
                                officialLink = officialLink.trim(),
                                isImportantNotice = isImportantNotice
                            )

                            viewModel.addExamUpdate(newItem)
                            Toast.makeText(context, "Added Successfully!", Toast.LENGTH_SHORT).show()
                            viewModel.navigateTo(Screen.MANAGE_EXAM_PATTERN_CUTOFF_VIEW)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("save_exam_update_btn")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Publish & Save Exam Update")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewExamPatternCutoffScreen(viewModel: JuktiViewModel) {
    val context = LocalContext.current
    val examUpdates by viewModel.examUpdates.collectAsState()

    var selectedFilterCategory by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }
    val filterCategories = listOf("All", "Syllabus", "Pattern", "Cutoff", "Notification", "Admit Card")
    var viewingItem by remember { mutableStateOf<ExamUpdateEntity?>(null) }
    var editingItem by remember { mutableStateOf<ExamUpdateEntity?>(null) }

    val filteredUpdates = remember(examUpdates, selectedFilterCategory, searchQuery) {
        examUpdates.filter { item ->
            val matchesCategory = selectedFilterCategory == "All" || item.category.equals(selectedFilterCategory, ignoreCase = true)
            val matchesSearch = searchQuery.isBlank() || 
                item.titleEn.contains(searchQuery, ignoreCase = true) ||
                item.examName.contains(searchQuery, ignoreCase = true) ||
                item.detailEn.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }

    Scaffold(
        topBar = {
            com.example.ui.components.JuktiTopAppBar(
                title = "View All Exam Updates (${filteredUpdates.size})",
                onBackClick = { viewModel.navigateTo(Screen.WORKSPACE) },
                backTestTag = "view_exam_info_back_btn"
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            // Search Bar
            SafeOutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search Syllabus, Pattern, Cutoff...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .testTag("search_exam_updates"),
                singleLine = true,
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear Search")
                        }
                    }
                }
            )

            // Filter Category Chips
            androidx.compose.foundation.lazy.LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filterCategories) { cat ->
                    FilterChip(
                        selected = selectedFilterCategory == cat,
                        onClick = { selectedFilterCategory = cat },
                        label = { Text(cat) },
                        modifier = Modifier.testTag("filter_chip_$cat")
                    )
                }
            }

            if (filteredUpdates.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No matching entries found.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredUpdates, key = { it.id }) { item ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewingItem = item },
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

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { editingItem = item },
                                        modifier = Modifier.testTag("edit_update_${item.id}")
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
                                    }
                                    IconButton(
                                        onClick = {
                                            viewModel.deleteExamUpdate(item)
                                            Toast.makeText(context, "Deleted successfully", Toast.LENGTH_SHORT).show()
                                        },
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

    // View Details Dialog
    viewingItem?.let { item ->
        AlertDialog(
            onDismissRequest = { viewingItem = null },
            title = {
                Column {
                    Text(item.examName, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    Text(item.titleEn, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SuggestionChip(onClick = {}, label = { Text("Category: ${item.category}") })
                    Text("Date: ${item.updateDate}", style = MaterialTheme.typography.bodySmall)
                    HorizontalDivider()
                    Text("English Details:", fontWeight = FontWeight.Bold)
                    Text(item.detailEn, style = MaterialTheme.typography.bodyMedium)
                    if (item.detailAs.isNotBlank() && item.detailAs != item.detailEn) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Assamese Details:", fontWeight = FontWeight.Bold)
                        Text(item.detailAs, style = MaterialTheme.typography.bodyMedium)
                    }
                    if (item.officialLink.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Reference / Link: ${item.officialLink}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    editingItem = item
                    viewingItem = null
                }) {
                    Text("Edit")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewingItem = null }) {
                    Text("Close")
                }
            }
        )
    }

    // Edit Dialog
    editingItem?.let { item ->
        var editExamName by remember { mutableStateOf(item.examName) }
        var editCategory by remember { mutableStateOf(item.category) }
        var editTitleEn by remember { mutableStateOf(item.titleEn) }
        var editDetailEn by remember { mutableStateOf(item.detailEn) }
        var editOfficialLink by remember { mutableStateOf(item.officialLink) }

        AlertDialog(
            onDismissRequest = { editingItem = null },
            title = { Text("Edit Exam Update") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SafeOutlinedTextField(
                        value = editExamName,
                        onValueChange = { editExamName = it },
                        label = { Text("Exam Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    SafeOutlinedTextField(
                        value = editTitleEn,
                        onValueChange = { editTitleEn = it },
                        label = { Text("Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    SafeOutlinedTextField(
                        value = editDetailEn,
                        onValueChange = { editDetailEn = it },
                        label = { Text("Details") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    SafeOutlinedTextField(
                        value = editOfficialLink,
                        onValueChange = { editOfficialLink = it },
                        label = { Text("Official Link") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val updated = item.copy(
                        examName = editExamName.trim(),
                        titleEn = editTitleEn.trim(),
                        detailEn = editDetailEn.trim(),
                        officialLink = editOfficialLink.trim()
                    )
                    viewModel.updateExamUpdate(updated)
                    Toast.makeText(context, "Updated Successfully!", Toast.LENGTH_SHORT).show()
                    editingItem = null
                }) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingItem = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
