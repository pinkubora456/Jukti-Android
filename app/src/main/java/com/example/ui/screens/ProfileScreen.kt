package com.example.ui.screens

import com.example.ui.components.SafeOutlinedTextField

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(viewModel: JuktiViewModel) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val language by viewModel.language.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val isAdminOrOwner by viewModel.isAdminOrOwner.collectAsState()
    val isUserPremium by viewModel.isUserPremium.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    var showEditNameDialog by remember { mutableStateOf(false) }
    var quickNameInput by remember { mutableStateOf("") }

    var showTargetGoalDialog by remember { mutableStateOf(false) }
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
    var customGoalInput by remember { mutableStateOf("") }
    val exams by viewModel.examsList.collectAsState()

    val standardExamGoals = remember {
        listOf(
            "ADRE Grade III & IV",
            "APSC CCE Prelims / Mains",
            "Assam Police (SI / Constable)",
            "Assam TET / Teacher Recruitment",
            "APDCL & State Departments",
            "DHFWS / Health Sector Exams",
            "SSC (CGL / CHSL / GD)",
            "Banking (IBPS / SBI / RRB)",
            "Railways (RRB NTPC / Group D)",
            "UPSC Civil Services"
        )
    }

    val availableExamOptions = remember(exams) {
        (standardExamGoals + exams.map { it.title }).filter { it.isNotBlank() }.distinct()
    }

    if (showEditNameDialog) {
        AlertDialog(
            onDismissRequest = { showEditNameDialog = false },
            title = { Text("Edit Full Name", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Your name will be displayed across the app and on the leaderboard:")
                    SafeOutlinedTextField(
                        value = quickNameInput,
                        onValueChange = { quickNameInput = it },
                        label = { Text("Full Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmedName = quickNameInput.trim()
                        if (trimmedName.isNotBlank()) {
                            viewModel.updateUserName(trimmedName)
                            android.widget.Toast.makeText(context, "Name updated successfully!", android.widget.Toast.LENGTH_SHORT).show()
                        }
                        showEditNameDialog = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditNameDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showTargetGoalDialog) {
        AlertDialog(
            onDismissRequest = { showTargetGoalDialog = false },
            title = { Text("Select Target Exam Goals", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Select the exams you are preparing for:", style = MaterialTheme.typography.bodyMedium)

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        availableExamOptions.forEach { goal ->
                            val isSelected = selectedGoals.contains(goal)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    if (isSelected) {
                                        selectedGoals.remove(goal)
                                    } else {
                                        selectedGoals.add(goal)
                                    }
                                },
                                label = { Text(goal) },
                                leadingIcon = if (isSelected) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                } else null
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SafeOutlinedTextField(
                            value = customGoalInput,
                            onValueChange = { customGoalInput = it },
                            label = { Text("Add Other Exam Goal") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = {
                                val trimmed = customGoalInput.trim()
                                if (trimmed.isNotBlank() && !selectedGoals.contains(trimmed)) {
                                    selectedGoals.add(trimmed)
                                    customGoalInput = ""
                                }
                            }
                        ) {
                            Icon(Icons.Default.AddCircle, contentDescription = "Add Goal", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        userProfile?.let { prof ->
                            val newGoalsStr = if (selectedGoals.isEmpty()) "ADRE Grade III & IV" else selectedGoals.joinToString(", ")
                            val updated = prof.copy(examGoal = newGoalsStr)
                            viewModel.updateUserProfile(updated)
                            android.widget.Toast.makeText(context, "Target goals updated!", android.widget.Toast.LENGTH_SHORT).show()
                        }
                        showTargetGoalDialog = false
                    }
                ) {
                    Text("Save Goals")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTargetGoalDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showEditProfileDialog) {
        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = { Text("Edit Profile Details", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SafeOutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Full Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    SafeOutlinedTextField(
                        value = editMobile,
                        onValueChange = { editMobile = it },
                        label = { Text("Mobile Number (Optional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    SafeOutlinedTextField(
                        value = editDistrict,
                        onValueChange = { editDistrict = it },
                        label = { Text("District") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "Target Exam Goals:",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        availableExamOptions.forEach { goal ->
                            val isSelected = selectedGoals.contains(goal)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    if (isSelected) {
                                        selectedGoals.remove(goal)
                                    } else {
                                        selectedGoals.add(goal)
                                    }
                                },
                                label = { Text(goal) },
                                leadingIcon = if (isSelected) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                } else null
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SafeOutlinedTextField(
                            value = customGoalInput,
                            onValueChange = { customGoalInput = it },
                            label = { Text("Add Other Exam Goal") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = {
                                val trimmed = customGoalInput.trim()
                                if (trimmed.isNotBlank() && !selectedGoals.contains(trimmed)) {
                                    selectedGoals.add(trimmed)
                                    customGoalInput = ""
                                }
                            }
                        ) {
                            Icon(Icons.Default.AddCircle, contentDescription = "Add Goal", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        userProfile?.let { prof ->
                            val updated = prof.copy(
                                name = editName.trim().ifEmpty { prof.name },
                                mobile = editMobile.trim(),
                                district = editDistrict.trim(),
                                examGoal = if (selectedGoals.isEmpty()) "ADRE Grade III & IV" else selectedGoals.joinToString(", ")
                            )
                            viewModel.updateUserProfile(updated)
                            android.widget.Toast.makeText(context, "Profile updated successfully!", android.widget.Toast.LENGTH_SHORT).show()
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

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = displayName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            IconButton(
                onClick = {
                    quickNameInput = userProfile?.name ?: ""
                    showEditNameDialog = true
                },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Name",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

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
                ProfileDetailRow(
                    icon = Icons.Default.Person,
                    label = "Full Name",
                    value = userProfile?.name.takeIf { !it.isNullOrBlank() } ?: "Not Set",
                    onClick = {
                        quickNameInput = userProfile?.name ?: ""
                        showEditNameDialog = true
                    }
                )
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                ProfileDetailRow(icon = Icons.Default.Email, label = "Email ID", value = userProfile?.email ?: "scholar@jukti.in")
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                ProfileDetailRow(
                    icon = Icons.Default.Phone,
                    label = "Mobile Number",
                    value = userProfile?.mobile.takeIf { !it.isNullOrBlank() } ?: "Not Provided (Optional)",
                    onClick = {
                        editName = userProfile?.name ?: ""
                        editMobile = userProfile?.mobile ?: ""
                        editDistrict = userProfile?.district ?: ""
                        selectedGoals.clear()
                        selectedGoals.addAll((userProfile?.examGoal ?: "").split(",").map { it.trim() }.filter { it.isNotEmpty() })
                        showEditProfileDialog = true
                    }
                )
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                ProfileDetailRow(
                    icon = Icons.Default.LocationOn,
                    label = "District",
                    value = userProfile?.district.takeIf { !it.isNullOrBlank() } ?: "Kamrup Metropolitan",
                    onClick = {
                        editName = userProfile?.name ?: ""
                        editMobile = userProfile?.mobile ?: ""
                        editDistrict = userProfile?.district ?: ""
                        selectedGoals.clear()
                        selectedGoals.addAll((userProfile?.examGoal ?: "").split(",").map { it.trim() }.filter { it.isNotEmpty() })
                        showEditProfileDialog = true
                    }
                )
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                ProfileDetailRow(
                    icon = Icons.Default.Flag,
                    label = "Target Goal",
                    value = userProfile?.examGoal ?: "ADRE Grade III & IV",
                    onClick = {
                        selectedGoals.clear()
                        selectedGoals.addAll((userProfile?.examGoal ?: "").split(",").map { it.trim() }.filter { it.isNotEmpty() })
                        showTargetGoalDialog = true
                    }
                )
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                ProfileDetailRow(
                    icon = Icons.Default.WorkspacePremium,
                    label = "Subscription",
                    value = if (isAdminOrOwner) "Pass Pro Active ⚡ (Admin/Owner)" else if (isUserPremium) "Pass Pro Active ⚡" else "Free Plan",
                    onClick = { showMyPlanDialog = true }
                )
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
fun ProfileDetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = if (onClick != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
            if (onClick != null) {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit $label",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
