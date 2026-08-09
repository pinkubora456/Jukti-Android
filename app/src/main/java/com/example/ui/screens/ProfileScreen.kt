package com.example.ui.screens

import com.example.ui.components.SafeOutlinedTextField

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.viewmodel.AppLanguage
import com.example.ui.viewmodel.JuktiViewModel
import com.example.ui.viewmodel.Screen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(viewModel: JuktiViewModel) {
    val language by viewModel.language.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val isAdminOrOwner by viewModel.isAdminOrOwner.collectAsState()
    val isUserPremium by viewModel.isUserPremium.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showMyPlanDialog by remember { mutableStateOf(false) }

    var editName by remember { mutableStateOf(userProfile?.name ?: "") }
    var editMobile by remember { mutableStateOf(userProfile?.mobile ?: "") }
    var editDistrict by remember { mutableStateOf(userProfile?.district ?: "") }
    val selectedGoals = remember {
        mutableStateListOf<String>().apply {
            addAll((userProfile?.examGoal ?: "").split(",").map { it.trim() }.filter { it.isNotEmpty() })
        }
    }
    var goalExpanded by remember { mutableStateOf(false) }
    val exams by viewModel.examsList.collectAsState()

    if (showEditProfileDialog) {
        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = { Text("Edit Profile") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SafeOutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Name") },
                        singleLine = true
                    )
                    SafeOutlinedTextField(
                        value = editMobile,
                        onValueChange = { editMobile = it },
                        label = { Text("Mobile Number (Optional)") },
                        singleLine = true
                    )
                    SafeOutlinedTextField(
                        value = editDistrict,
                        onValueChange = { editDistrict = it },
                        label = { Text("District") },
                        singleLine = true
                    )
                    ExposedDropdownMenuBox(
                        expanded = goalExpanded,
                        onExpandedChange = { goalExpanded = !goalExpanded }
                    ) {
                        SafeOutlinedTextField(
                            value = if (selectedGoals.isEmpty()) "Select Target Exam Goals..." else selectedGoals.joinToString(", "),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Target Exam Goals (Multiple)") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = goalExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            singleLine = true
                        )
                        ExposedDropdownMenu(
                            expanded = goalExpanded,
                            onDismissRequest = { goalExpanded = false }
                        ) {
                            exams.forEach { exam ->
                                val isSelected = selectedGoals.contains(exam.title)
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Checkbox(
                                                checked = isSelected,
                                                onCheckedChange = null
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(exam.title)
                                        }
                                    },
                                    onClick = {
                                        if (isSelected) {
                                            selectedGoals.remove(exam.title)
                                        } else {
                                            selectedGoals.add(exam.title)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        userProfile?.let { prof ->
                            val updated = prof.copy(
                                name = editName,
                                mobile = editMobile,
                                district = editDistrict,
                                examGoal = selectedGoals.joinToString(", ")
                            )
                            coroutineScope.launch {
                                viewModel.repository.updateUserProfile(updated)
                            }
                        }
                        showEditProfileDialog = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showMyPlanDialog) {
        val isPassPro = isUserPremium
        AlertDialog(
            onDismissRequest = { showMyPlanDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text("My Plan Details", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isPassPro) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = if (isAdminOrOwner) "Pass Pro Active ⚡ (Admin/Owner)" else if (isPassPro) "Pass Pro Active ⚡" else "Free Plan 🌟",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isPassPro) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isPassPro) "Unlimited access to all mock tests, study notes & practice modules." else "Basic access to practice questions and daily updates.",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    Text(
                        text = "Plan Features Included:",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelLarge
                    )

                    val features = if (isPassPro) listOf(
                        "• Full length mock tests & detailed solutions",
                        "• Chapter-wise MCQs & study notes",
                        "• Exam alerts & syllabus updates",
                        "• Unlimited practice & leaderboard analytics",
                        "• Ad-free learning & priority support"
                    ) else listOf(
                        "• Basic practice questions",
                        "• Limited study notes access",
                        "• Daily exam updates",
                        "⚡ Upgrade to Pass Pro for full access"
                    )

                    features.forEach { feature ->
                        Text(text = feature, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showMyPlanDialog = false
                        viewModel.navigateTo(Screen.PREMIUM_PLANS)
                    }
                ) {
                    Text(if (isPassPro) "View All Plans" else "Upgrade to Pass Pro")
                }
            },
            dismissButton = {
                TextButton(onClick = { showMyPlanDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Profile", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo(Screen.HOME) }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(80.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = userProfile?.name?.take(1)?.uppercase() ?: "J",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        val roleSuffix = when (userProfile?.role?.uppercase()) {
            "OWNER" -> " (Owner)"
            "ADMIN" -> " (Admin)"
            else -> ""
        }
        val displayName = "${userProfile?.name ?: "Assam Scholar"}$roleSuffix"

        Text(
            text = displayName,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = userProfile?.email ?: "scholar@jukti.in",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                ProfileDetailRow(icon = Icons.Default.Email, label = "Email ID", value = userProfile?.email ?: "scholar@jukti.in")
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                ProfileDetailRow(icon = Icons.Default.Phone, label = "Mobile Number", value = userProfile?.mobile.takeIf { !it.isNullOrBlank() } ?: "Not Provided (Optional)")
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                ProfileDetailRow(icon = Icons.Default.LocationOn, label = "District", value = userProfile?.district.takeIf { !it.isNullOrBlank() } ?: "Kamrup Metropolitan")
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                ProfileDetailRow(icon = Icons.Default.Flag, label = "Target Goal", value = userProfile?.examGoal ?: "ADRE & APSC")
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                ProfileDetailRow(icon = Icons.Default.WorkspacePremium, label = "Subscription", value = if (isAdminOrOwner) "Pass Pro Active ⚡ (Admin/Owner)" else if (isUserPremium) "Pass Pro Active ⚡" else "Free Plan")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                editName = userProfile?.name ?: ""
                editMobile = userProfile?.mobile ?: ""
                editDistrict = userProfile?.district ?: ""
                selectedGoals.clear()
                selectedGoals.addAll((userProfile?.examGoal ?: "").split(",").map { it.trim() }.filter { it.isNotEmpty() })
                showEditProfileDialog = true
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Edit, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Edit Profile Details")
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            onClick = { showMyPlanDialog = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(8.dp))
            Text("My Plan Details")
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            onClick = {
                viewModel.logout()
            },
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.ExitToApp, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Sign Out")
        }
    }
}
}

@Composable
fun ProfileDetailRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}
