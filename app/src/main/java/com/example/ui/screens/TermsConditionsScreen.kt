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
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
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
import com.example.ui.components.SafeOutlinedTextField
import com.example.ui.viewmodel.JuktiViewModel
import com.example.ui.viewmodel.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsConditionsScreen(viewModel: JuktiViewModel) {
    val aboutConfig by viewModel.aboutConfig.collectAsState()
    val isAdminOrOwner by viewModel.isAdminOrOwner.collectAsState()
    val context = LocalContext.current

    var showEditDialog by remember { mutableStateOf(false) }
    var editedContent by remember { mutableStateOf("") }

    val defaultTermsText = """1. Acceptance of Terms
Welcome to Jukti. By downloading, installing, or using our mobile application, you agree to comply with and be bound by these Terms & Conditions. If you do not agree to these terms, please do not use the app.

2. Educational Purpose & Exam Scope
Jukti is an educational test preparation platform designed to assist aspirants preparing for APSC, ADRE, and other Assam competitive examinations. All mock tests, study notes, current affairs, and analytics are provided for practice and guidance purposes.

3. User Accounts & Responsibilities
You are responsible for maintaining the confidentiality of your login credentials and user profile. You agree to provide accurate information and not share your account access with third parties.

4. Intellectual Property Rights
All questions, study notes, graphics, user interface designs, and software code within Jukti are the intellectual property of Jukti and its creators. Unauthorized reproduction, distribution, or commercial exploitation is strictly prohibited.

5. Payments, Cancellation & Refund Policy
Please review our billing, cancellation, and refund guidelines below regarding Jukti Premium subscriptions and digital study plans:
• Plan & Subscription Pricing: Transparent pricing tiers are displayed for APSC, ADRE, and specialized test series packages.
• Payment Process: Payments are securely processed through authorized in-app payment gateways (such as Google Play Billing or secure online checkout).
• Subscription Duration: Subscription entitlements remain active for the specific exam cycle, monthly, or annual duration purchased.
• Renewal Terms: Subscriptions do not auto-renew unless explicitly authorized by the user during checkout.
• Cancellation Rules: Users may cancel active study plan subscriptions or clear app data anytime via the Profile or Settings screen.
• Refund Eligibility: Refund requests are evaluated and considered within 3 (three) days of purchase exclusively in cases of verified technical delivery failure or duplicate billing.
• Non-Refundable Conditions: Digital study notes, downloaded content, or mock tests heavily consumed or attempted after purchase are non-refundable.
• Legal Exceptions: Any mandatory refund exceptions required under applicable consumer protection laws will be honoured accordingly.
• Billing & Refund Support: For any billing discrepancies, payment issues, or refund inquiries, please contact our billing support at support@jukti.in.

6. Limitation of Liability
Jukti and its creators shall not be liable for any direct, indirect, incidental, or consequential damages arising from the use of our app or exam results.

7. Modifications to Terms
We reserve the right to modify these terms at any time. Continued use of the app following updates indicates your acceptance of the revised terms.

8. Contact & Support
For any questions regarding these Terms & Conditions or app services, contact us at:
Email: support@jukti.in"""

    val currentContent = if (aboutConfig.termsConditionsContent.isNotBlank()) {
        aboutConfig.termsConditionsContent
    } else {
        defaultTermsText
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Terms & Conditions", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateTo(Screen.MENU) },
                        modifier = Modifier.testTag("terms_conditions_back_btn")
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (isAdminOrOwner) {
                        IconButton(
                            onClick = {
                                editedContent = currentContent
                                showEditDialog = true
                            },
                            modifier = Modifier.testTag("edit_terms_conditions_btn")
                        ) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Terms & Conditions")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
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
            Surface(
                modifier = Modifier.size(72.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Text(
                text = "Terms of Service & Usage Guidelines",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = "Last updated: August 2026",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = currentContent,
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 22.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Start
                    )
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
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
                        text = "For support inquiries, contact support@jukti.in",
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
            title = { Text("Edit Terms & Conditions", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SafeOutlinedTextField(
                        value = editedContent,
                        onValueChange = { editedContent = it },
                        label = { Text("Terms & Conditions Content") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .testTag("terms_conditions_input"),
                        textStyle = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateAboutConfig(
                            aboutConfig.copy(termsConditionsContent = editedContent)
                        )
                        Toast.makeText(context, "Terms & Conditions updated successfully!", Toast.LENGTH_SHORT).show()
                        showEditDialog = false
                    },
                    modifier = Modifier.testTag("save_terms_conditions_btn")
                ) {
                    Text("Save Changes")
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
