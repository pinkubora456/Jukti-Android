package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.FaqEntity
import com.example.ui.components.JuktiTopAppBar
import com.example.ui.components.SafeOutlinedTextField
import com.example.ui.viewmodel.AppLanguage
import com.example.ui.viewmodel.JuktiViewModel
import com.example.ui.viewmodel.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FaqScreen(viewModel: JuktiViewModel) {
    val language = com.example.ui.viewmodel.AppLanguage.ENGLISH
    val isAssamese = language == AppLanguage.ASSAMESE
    val isAdminOrOwner by viewModel.isAdminOrOwner.collectAsState()
    val faqs by viewModel.faqs.collectAsState()
    val context = LocalContext.current

    var searchQuery by remember { mutableStateOf("") }
    var editingFaq by remember { mutableStateOf<FaqEntity?>(null) }
    var showFaqDialog by remember { mutableStateOf(false) }
    var faqQuestionEn by remember { mutableStateOf("") }
    var faqQuestionAs by remember { mutableStateOf("") }
    var faqAnswerEn by remember { mutableStateOf("") }
    var faqAnswerAs by remember { mutableStateOf("") }
    var faqToDelete by remember { mutableStateOf<FaqEntity?>(null) }

    val filteredFaqs = remember(faqs, searchQuery, isAssamese) {
        if (searchQuery.isBlank()) {
            faqs
        } else {
            val q = searchQuery.trim().lowercase()
            faqs.filter { faq ->
                faq.questionEn.lowercase().contains(q) ||
                faq.questionAs.lowercase().contains(q) ||
                faq.answerEn.lowercase().contains(q) ||
                faq.answerAs.lowercase().contains(q)
            }
        }
    }

    Scaffold(
        topBar = {
            JuktiTopAppBar(
                title = "Frequently Asked Questions",
                onBackClick = { viewModel.navigateTo(Screen.HELP_SUPPORT) },
                actions = {
                    if (isAdminOrOwner) {
                        IconButton(
                            onClick = {
                                editingFaq = null
                                faqQuestionEn = ""
                                faqQuestionAs = ""
                                faqAnswerEn = ""
                                faqAnswerAs = ""
                                showFaqDialog = true
                            },
                            modifier = Modifier.testTag("add_faq_top_btn")
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Add FAQ")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Search field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("faq_search_input"),
                placeholder = { Text("Search FAQ topics, tests, subscriptions...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Header summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "All FAQs (${filteredFaqs.size})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                if (isAdminOrOwner) {
                    TextButton(
                        onClick = {
                            editingFaq = null
                            faqQuestionEn = ""
                            faqQuestionAs = ""
                            faqAnswerEn = ""
                            faqAnswerAs = ""
                            showFaqDialog = true
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+ Add FAQ", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (filteredFaqs.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (searchQuery.isNotBlank()) "No FAQs matching \"$searchQuery\"" else "No FAQs available yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredFaqs, key = { it.id }) { faq ->
                        val question = if (isAssamese && faq.questionAs.isNotBlank()) faq.questionAs else faq.questionEn
                        val answer = if (isAssamese && faq.answerAs.isNotBlank()) faq.answerAs else faq.answerEn

                        var expanded by remember { mutableStateOf(false) }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp).animateContentSize()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { expanded = !expanded },
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = question,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Icon(
                                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = null
                                    )
                                }
                                if (expanded) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    HorizontalDivider()
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = answer,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        lineHeight = 20.sp
                                    )

                                    if (isAdminOrOwner) {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.End,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            TextButton(
                                                onClick = {
                                                    editingFaq = faq
                                                    faqQuestionEn = faq.questionEn
                                                    faqQuestionAs = faq.questionAs
                                                    faqAnswerEn = faq.answerEn
                                                    faqAnswerAs = faq.answerAs
                                                    showFaqDialog = true
                                                }
                                            ) {
                                                Icon(Icons.Default.Edit, contentDescription = "Edit FAQ", modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Edit")
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            TextButton(
                                                onClick = { faqToDelete = faq },
                                                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = "Delete FAQ", modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Delete")
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

    // Add / Edit Dialog
    if (showFaqDialog) {
        AlertDialog(
            onDismissRequest = { showFaqDialog = false },
            title = { Text(if (editingFaq != null) "Edit FAQ" else "Add FAQ", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SafeOutlinedTextField(
                        value = faqQuestionEn,
                        onValueChange = { faqQuestionEn = it },
                        label = { Text("Question (English) *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    SafeOutlinedTextField(
                        value = faqQuestionAs,
                        onValueChange = { faqQuestionAs = it },
                        label = { Text("Question (Assamese)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    SafeOutlinedTextField(
                        value = faqAnswerEn,
                        onValueChange = { faqAnswerEn = it },
                        label = { Text("Answer (English) *") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                    SafeOutlinedTextField(
                        value = faqAnswerAs,
                        onValueChange = { faqAnswerAs = it },
                        label = { Text("Answer (Assamese)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (faqQuestionEn.isBlank() || faqAnswerEn.isBlank()) {
                            android.widget.Toast.makeText(context, "English question and answer are required", android.widget.Toast.LENGTH_SHORT).show()
                        } else {
                            if (editingFaq != null) {
                                viewModel.updateFaq(
                                    editingFaq!!.copy(
                                        questionEn = faqQuestionEn.trim(),
                                        questionAs = faqQuestionAs.trim(),
                                        answerEn = faqAnswerEn.trim(),
                                        answerAs = faqAnswerAs.trim()
                                    )
                                )
                                android.widget.Toast.makeText(context, "FAQ updated successfully!", android.widget.Toast.LENGTH_SHORT).show()
                            } else {
                                viewModel.addFaq(
                                    questionEn = faqQuestionEn.trim(),
                                    questionAs = faqQuestionAs.trim(),
                                    answerEn = faqAnswerEn.trim(),
                                    answerAs = faqAnswerAs.trim()
                                )
                                android.widget.Toast.makeText(context, "FAQ added successfully!", android.widget.Toast.LENGTH_SHORT).show()
                            }
                            showFaqDialog = false
                        }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showFaqDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete Confirmation Dialog
    if (faqToDelete != null) {
        AlertDialog(
            onDismissRequest = { faqToDelete = null },
            title = { Text("Delete FAQ", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete this FAQ?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteFaq(faqToDelete!!)
                        android.widget.Toast.makeText(context, "FAQ deleted", android.widget.Toast.LENGTH_SHORT).show()
                        faqToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { faqToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
