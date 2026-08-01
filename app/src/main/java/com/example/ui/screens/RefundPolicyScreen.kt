package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.AppLanguage
import com.example.ui.viewmodel.JuktiViewModel
import com.example.ui.viewmodel.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RefundPolicyScreen(viewModel: JuktiViewModel) {
    val language by viewModel.language.collectAsState()
    val isAssamese = language == AppLanguage.ASSAMESE
    val isAdminOrOwner by viewModel.isAdminOrOwner.collectAsState()
    val aboutConfig by viewModel.aboutConfig.collectAsState()
    val context = LocalContext.current

    var showEditDialog by remember { mutableStateOf(false) }
    var policyEn by remember { mutableStateOf("") }
    var policyAs by remember { mutableStateOf("") }

    // Synchronize initial dialog state when open
    LaunchedEffect(showEditDialog) {
        if (showEditDialog) {
            policyEn = aboutConfig.refundPolicyEn
            policyAs = aboutConfig.refundPolicyAs
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isAssamese) "ৰিফাণ্ড পলিচি (Refund Policy)" else "Refund Policy", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateTo(Screen.MENU) },
                        modifier = Modifier.testTag("refund_policy_back_btn")
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (isAdminOrOwner) {
                        IconButton(
                            onClick = { showEditDialog = true },
                            modifier = Modifier.testTag("refund_policy_edit_btn")
                        ) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Refund Policy")
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Receipt Icon Top illustration
            Surface(
                modifier = Modifier.size(72.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.ReceiptLong,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Text(
                text = if (isAssamese) "ক্ৰয় আৰু ৰিফাণ্ডৰ নিয়মাৱলী" else "Purchase & Refund Guidelines",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            // Content Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isAssamese) aboutConfig.refundPolicyAs else aboutConfig.refundPolicyEn,
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 22.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Justify
                    )
                }
            }

            // Footer / Support
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = if (isAssamese) "কিবা অসুবিধা পালে support@jukti.in ত যোগাযোগ কৰক।" else "For any payment discrepancy, email support@jukti.in",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Refund Policy", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = policyEn,
                        onValueChange = { policyEn = it },
                        label = { Text("Policy Content (English) *") },
                        modifier = Modifier.fillMaxWidth().testTag("refund_policy_input_en"),
                        minLines = 4
                    )
                    OutlinedTextField(
                        value = policyAs,
                        onValueChange = { policyAs = it },
                        label = { Text("Policy Content (Assamese) *") },
                        modifier = Modifier.fillMaxWidth().testTag("refund_policy_input_as"),
                        minLines = 4
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (policyEn.isBlank() || policyAs.isBlank()) {
                            Toast.makeText(context, "Fields cannot be blank", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.updateAboutConfig(
                                aboutConfig.copy(
                                    refundPolicyEn = policyEn.trim(),
                                    refundPolicyAs = policyAs.trim()
                                )
                            )
                            Toast.makeText(context, "Refund policy updated!", Toast.LENGTH_SHORT).show()
                            showEditDialog = false
                        }
                    },
                    modifier = Modifier.testTag("refund_policy_save_btn")
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
