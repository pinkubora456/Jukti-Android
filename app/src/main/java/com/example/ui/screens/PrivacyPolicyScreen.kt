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
import androidx.compose.material.icons.filled.Security
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
import com.example.ui.viewmodel.JuktiViewModel
import com.example.ui.viewmodel.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(viewModel: JuktiViewModel) {
    val aboutConfig by viewModel.aboutConfig.collectAsState()
    val isAdminOrOwner by viewModel.isAdminOrOwner.collectAsState()
    val context = LocalContext.current

    var showEditDialog by remember { mutableStateOf(false) }
    var editedContent by remember { mutableStateOf("") }

    val defaultPrivacyText = """1. Introduction & Overview
Jukti ("we", "our", or "us") is committed to protecting your privacy. This Privacy Policy explains how we collect, use, disclose, and safeguard your information when you use our mobile application for APSC, ADRE, and Assam competitive exam preparation.

2. User & Account Data Collection
We collect basic profile information when you register or sign in, including your name, email address, and preferred study language (English / Assamese). This data is used to personalize your learning experience, track exam preparation progress, and maintain your account state.

3. Authentication
Authentication is securely handled via standard Firebase Authentication and Google Identity Services. We do not store your raw passwords on our local servers; credentials are verified securely through encrypted authentication providers.

4. Firebase Data Usage
We utilize Google Firebase services (Firestore, Realtime Database, and Cloud Storage) to sync educational content, mock test questions, current affairs, user rankings, and announcements. Firebase securely processes data in transit using industry-standard SSL encryption.

5. Practice and Performance Data
To provide meaningful analytics, speed metrics, accuracy scores, and leaderboard rankings, we record your mock test answers, bookmarked questions, hidden/mastered questions, and study session progress locally on your device and synchronized securely when online.

6. Subscription & Payment-Related Data
When you subscribe to Jukti Premium plans, payment transactions are processed securely through authorized third-party payment gateways. We do not store sensitive credit card or banking credentials on our app. We only retain transaction status identifiers, active subscription duration, and entitlement details.

7. Data Storage & Security
Your local app data is securely stored using Android's local Room database architecture with encrypted shared preferences. We implement robust administrative, technical, and physical security safeguards to protect your personal information against unauthorized access.

8. Data Retention
We retain your personal profile and academic progress data as long as your account remains active. If you initiate an account deletion, your personal data and associated progress records are permanently cleared from local and cloud databases.

9. Account & Data Deletion
You have full control over your data. You can clear your speed and accuracy progress data or permanently delete your user account directly from the App Settings screen at any time.

10. Third-Party Services
Our application integrates with trusted third-party services such as Google Firebase and Google Play Billing. These services operate under their respective privacy policies and security standards.

11. User Rights
You possess the right to access, update, or request deletion of your personal profile data. You may review or modify your account information directly within your Profile screen.

12. Contact Information
If you have questions, concerns, or requests regarding this Privacy Policy or data practices, please reach out to our privacy support team at:
Email: support@jukti.in"""

    val currentContent = if (aboutConfig.privacyPolicyContent.isNotBlank()) {
        aboutConfig.privacyPolicyContent
    } else {
        defaultPrivacyText
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Privacy Policy", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateTo(Screen.MENU) },
                        modifier = Modifier.testTag("privacy_policy_back_btn")
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
                            modifier = Modifier.testTag("edit_privacy_policy_btn")
                        ) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Privacy Policy")
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
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Text(
                text = "Privacy & Data Protection",
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
                        text = "For privacy inquiries, contact support@jukti.in",
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
            title = { Text("Edit Privacy Policy", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = editedContent,
                        onValueChange = { editedContent = it },
                        label = { Text("Privacy Policy Content") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .testTag("privacy_policy_input"),
                        textStyle = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateAboutConfig(
                            aboutConfig.copy(privacyPolicyContent = editedContent)
                        )
                        Toast.makeText(context, "Privacy Policy updated successfully!", Toast.LENGTH_SHORT).show()
                        showEditDialog = false
                    },
                    modifier = Modifier.testTag("save_privacy_policy_btn")
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
