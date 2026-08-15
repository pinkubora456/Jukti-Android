package com.example.ui.screens

import com.example.ui.components.SafeOutlinedTextField

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.animation.animateContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.FaqEntity
import com.example.ui.viewmodel.AppLanguage
import com.example.ui.viewmodel.JuktiViewModel
import com.example.ui.viewmodel.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactUsScreen(viewModel: JuktiViewModel) {
    val language by viewModel.language.collectAsState()
    val isAssamese = language == AppLanguage.ASSAMESE
    val isAdminOrOwner by viewModel.isAdminOrOwner.collectAsState()
    val aboutConfig by viewModel.aboutConfig.collectAsState()
    val faqs by viewModel.faqs.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    var showEditDialog by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var whatsapp by remember { mutableStateOf("") }

    // FAQ editing state
    var editingFaq by remember { mutableStateOf<FaqEntity?>(null) }
    var showFaqDialog by remember { mutableStateOf(false) }
    var faqQuestionEn by remember { mutableStateOf("") }
    var faqQuestionAs by remember { mutableStateOf("") }
    var faqAnswerEn by remember { mutableStateOf("") }
    var faqAnswerAs by remember { mutableStateOf("") }
    var faqToDelete by remember { mutableStateOf<FaqEntity?>(null) }

    LaunchedEffect(showEditDialog) {
        if (showEditDialog) {
            email = aboutConfig.contactEmail
            whatsapp = aboutConfig.contactWhatsapp
        }
    }

    Scaffold(
        topBar = {
            com.example.ui.components.JuktiTopAppBar(
                title = "Contact Us",
                onBackClick = { viewModel.navigateTo(Screen.MENU) },
                actions = {
                    if (isAdminOrOwner) {
                        IconButton(
                            onClick = { showEditDialog = true },
                            modifier = Modifier.testTag("edit_contact_us_btn")
                        ) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Contacts")
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Support Channels Banner
            Text(
                text = "Direct Support Channels",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ContactChannelCard(
                    title = "Email Support",
                    sub = aboutConfig.contactEmail,
                    icon = Icons.Default.Email,
                    modifier = Modifier.weight(1f)
                )
                ContactChannelCard(
                    title = "WhatsApp",
                    sub = aboutConfig.contactWhatsapp,
                    icon = Icons.Default.Forum,
                    modifier = Modifier.weight(1f)
                )
            }

            // Frequently Asked Questions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Frequently Asked Questions",
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
                        },
                        modifier = Modifier.testTag("add_faq_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add FAQ", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+ Add FAQ", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (faqs.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "No FAQs available.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                faqs.forEach { faq ->
                    val question = if (isAssamese && faq.questionAs.isNotBlank()) faq.questionAs else faq.questionEn
                    val answer = if (isAssamese && faq.answerAs.isNotBlank()) faq.answerAs else faq.answerEn

                    FaqItemCard(
                        q = question,
                        a = answer,
                        isAdminOrOwner = isAdminOrOwner,
                        onEdit = {
                            editingFaq = faq
                            faqQuestionEn = faq.questionEn
                            faqQuestionAs = faq.questionAs
                            faqAnswerEn = faq.answerEn
                            faqAnswerAs = faq.answerAs
                            showFaqDialog = true
                        },
                        onDelete = {
                            faqToDelete = faq
                        }
                    )
                }
            }
        }
    }

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Contact Information", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SafeOutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Support *") },
                        modifier = Modifier.fillMaxWidth().testTag("contact_email_input"),
                        singleLine = true
                    )
                    SafeOutlinedTextField(
                        value = whatsapp,
                        onValueChange = { whatsapp = it },
                        label = { Text("WhatsApp Community Info *") },
                        modifier = Modifier.fillMaxWidth().testTag("contact_whatsapp_input"),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmedEmail = email.trim()
                        val trimmedWhatsapp = whatsapp.trim()
                        if (trimmedEmail.isBlank() || trimmedWhatsapp.isBlank()) {
                            android.widget.Toast.makeText(context, "Email and WhatsApp fields are required", android.widget.Toast.LENGTH_SHORT).show()
                        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
                            android.widget.Toast.makeText(context, "Please enter a valid support email address (e.g. support@jukti.in)", android.widget.Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.updateAboutConfig(
                                aboutConfig.copy(
                                    contactEmail = trimmedEmail,
                                    contactWhatsapp = trimmedWhatsapp
                                )
                            ) { success, msg ->
                                val confirmText = if (success) "Contact details saved & synced to Firebase!" else "Saved locally. Firebase sync queued: $msg"
                                android.widget.Toast.makeText(context, confirmText, android.widget.Toast.LENGTH_LONG).show()
                            }
                            showEditDialog = false
                        }
                    },
                    modifier = Modifier.testTag("save_contacts_btn")
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Add / Edit FAQ Dialog
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
                        modifier = Modifier.fillMaxWidth().testTag("faq_q_en_input")
                    )
                    SafeOutlinedTextField(
                        value = faqQuestionAs,
                        onValueChange = { faqQuestionAs = it },
                        label = { Text("Question (Assamese)") },
                        modifier = Modifier.fillMaxWidth().testTag("faq_q_as_input")
                    )
                    SafeOutlinedTextField(
                        value = faqAnswerEn,
                        onValueChange = { faqAnswerEn = it },
                        label = { Text("Answer (English) *") },
                        modifier = Modifier.fillMaxWidth().testTag("faq_a_en_input"),
                        minLines = 3
                    )
                    SafeOutlinedTextField(
                        value = faqAnswerAs,
                        onValueChange = { faqAnswerAs = it },
                        label = { Text("Answer (Assamese)") },
                        modifier = Modifier.fillMaxWidth().testTag("faq_a_as_input"),
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
                    },
                    modifier = Modifier.testTag("save_faq_btn")
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

    // Delete FAQ Confirmation Dialog
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
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("confirm_delete_faq_btn")
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

@Composable
private fun ContactChannelCard(
    title: String,
    sub: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            Text(text = sub, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun FaqItemCard(
    q: String,
    a: String,
    isAdminOrOwner: Boolean = false,
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
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
                    text = q,
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
                Text(text = a, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                if (isAdminOrOwner) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = onEdit, modifier = Modifier.testTag("edit_faq_item_btn")) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit FAQ", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Edit")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(
                            onClick = onDelete,
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.testTag("delete_faq_item_btn")
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete FAQ", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Delete")
                        }
                    }
                }
            }
        }
    }
}
