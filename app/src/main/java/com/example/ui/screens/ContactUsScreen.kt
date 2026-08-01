package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
    val context = androidx.compose.ui.platform.LocalContext.current

    var showEditDialog by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var telegram by remember { mutableStateOf("") }
    var whatsapp by remember { mutableStateOf("") }

    LaunchedEffect(showEditDialog) {
        if (showEditDialog) {
            email = aboutConfig.contactEmail
            phone = aboutConfig.contactPhone
            telegram = aboutConfig.contactTelegram
            whatsapp = aboutConfig.contactWhatsapp
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isAssamese) "যোগাযোগ আৰু সহায়" else "Contact Us", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo(Screen.MENU) }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
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
                text = if (isAssamese) "দ্ৰুত সহায় চেনেলসমূহ" else "Direct Support Channels",
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
                    title = "Helpline",
                    sub = aboutConfig.contactPhone,
                    icon = Icons.Default.Phone,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ContactChannelCard(
                    title = "Telegram",
                    sub = aboutConfig.contactTelegram,
                    icon = Icons.Default.Send,
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
            Text(
                text = if (isAssamese) "প্ৰায়ে সোধা প্ৰশ্নসমূহ (FAQ)" else "Frequently Asked Questions",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            FaqItemCard(
                q = if (isAssamese) "মক টেষ্টসমূহ কিদৰে দিয়া হয়?" else "How do I take full-length mock tests?",
                a = if (isAssamese) "মক টেষ্ট মেনুলৈ গৈ যিকোনো পৰীক্ষা চয়ন কৰক। তাত নিৰ্ধাৰিত সময় আৰু নিগেティブ মাৰ্কিং ব্যৱস্থা থাকিব।" else "Navigate to the Mock Tests tab, pick your exam (APSC, ADRE, Police), and click 'Start Test'. Timer and negative marking rules apply."
            )

            FaqItemCard(
                q = if (isAssamese) "অফলাইনত অধ্যয়ন কৰিব পাৰিমনে?" else "Can I study offline without internet?",
                a = if (isAssamese) "হয়, এবাৰ ডাউন্মলোড কৰা প্ৰশ্ন আৰু নোটছসমূহ অফলাইনত পঢ়িব পাৰিব।" else "Yes! Loaded study notes, downloaded e-books, and saved offline practice sets can be accessed anytime without internet."
            )

            FaqItemCard(
                q = if (isAssamese) "ম'বাইল নম্বৰ সলনি কিদৰে কৰিব?" else "How to report a wrong question answer?",
                a = if (isAssamese) "প্ৰশ্নটোৰ তলত থকা ফ্ল্যাগ/ৰিপোৰ্ট আইকনটো টিপি আমালৈ জনাওক, আমাৰ ছাবজেক্ট এক্সপাৰ্টসকলে ১২ ঘণ্টাৰ ভিতৰত সংশোধন কৰিব।" else "Tap the 'Report Question' flag icon inside any MCQ screen. Our Assam subject experts verify and correct reports within 12 hours."
            )
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
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Support *") },
                        modifier = Modifier.fillMaxWidth().testTag("contact_email_input"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Helpline Phone *") },
                        modifier = Modifier.fillMaxWidth().testTag("contact_phone_input"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = telegram,
                        onValueChange = { telegram = it },
                        label = { Text("Telegram Link/Handle *") },
                        modifier = Modifier.fillMaxWidth().testTag("contact_telegram_input"),
                        singleLine = true
                    )
                    OutlinedTextField(
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
                        if (email.isBlank() || phone.isBlank() || telegram.isBlank() || whatsapp.isBlank()) {
                            android.widget.Toast.makeText(context, "All contact fields are required", android.widget.Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.updateAboutConfig(
                                aboutConfig.copy(
                                    contactEmail = email.trim(),
                                    contactPhone = phone.trim(),
                                    contactTelegram = telegram.trim(),
                                    contactWhatsapp = whatsapp.trim()
                                )
                            )
                            android.widget.Toast.makeText(context, "Contact details updated successfully!", android.widget.Toast.LENGTH_SHORT).show()
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
private fun FaqItemCard(q: String, a: String) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = q, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
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
            }
        }
    }
}
