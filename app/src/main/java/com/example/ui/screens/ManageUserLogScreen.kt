package com.example.ui.screens

import com.example.ui.components.SafeOutlinedTextField
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.viewmodel.JuktiViewModel
import com.example.ui.viewmodel.LocalMessageTranslator
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageUserLogScreen(viewModel: JuktiViewModel) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    
    val mockUsers = remember { mutableStateListOf<UserLog>() }
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(true) }
    val plans by viewModel.plans.collectAsState()

    LaunchedEffect(Unit) {
        isLoading = true
        val users = viewModel.fetchAllUsersDirect()
            .sortedByDescending { it.uid.isNotBlank() }
            .distinctBy { it.email.lowercase() }
        val userLogs = users.map { 
            val uid = it.email.replace("@", "_at_").replace(".", "_dot_")
            val entitlement = viewModel.fetchUserEntitlementDirect(it.email)
            val currentPlan = com.example.data.util.PlanValidityEngine.getEffectivePlanName(entitlement)
            val validity = com.example.data.util.PlanValidityEngine.getEffectiveValidityLabel(entitlement)
            val userRole = viewModel.getUserRole(it.email, it.role)

            UserLog(
                id = it.uid.ifBlank { uid },
                name = it.name,
                email = it.email,
                currentPlan = currentPlan,
                validity = validity,
                isBlocked = it.role == "BLOCKED",
                role = userRole
            )
        }
        mockUsers.clear()
        mockUsers.addAll(userLogs)
        isLoading = false
    }

    val filteredUsers = mockUsers.filter {
        it.name.contains(searchQuery, ignoreCase = true) || it.email.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            com.example.ui.components.JuktiTopAppBar(
                title = "Manage User Log",
                onBackClick = { viewModel.navigateTo(com.example.ui.viewmodel.Screen.WORKSPACE) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            SafeOutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Search by name or email...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(filteredUsers, key = { it.id }) { user ->
                        val canDeleteOrBan = viewModel.canActorDeleteOrBanUser(user.email, user.role.name)
                        UserLogCard(
                            user = user,
                            plans = plans,
                            canDeleteOrBan = canDeleteOrBan,
                            onBlockUser = { 
                                viewModel.requestOrBlockUser(user.id, user.name, user.email, user.role.name) { isDirect, message ->
                                    if (isDirect) {
                                        val index = mockUsers.indexOfFirst { it.id == user.id || it.email.equals(user.email, ignoreCase = true) }
                                        if (index != -1) {
                                            mockUsers[index] = mockUsers[index].copy(isBlocked = !mockUsers[index].isBlocked)
                                        }
                                    }
                                    Toast.makeText(context, LocalMessageTranslator.translateGeneralMessage(context, message), Toast.LENGTH_LONG).show()
                                }
                             },
                            onDeleteUser = {
                                viewModel.requestOrDeleteUser(user.id, user.name, user.email, user.role.name) { isDirect, message ->
                                    if (isDirect) {
                                        mockUsers.removeAll { it.id == user.id || it.email.equals(user.email, ignoreCase = true) }
                                    }
                                    Toast.makeText(context, LocalMessageTranslator.translateGeneralMessage(context, message), Toast.LENGTH_LONG).show()
                                }
                            },
                            onChangePlan = { newPlan, validity, valType, valVal, isLife ->
                                viewModel.requestOrUpgradePlan(
                                    userId = user.id,
                                    userName = user.name,
                                    userEmail = user.email,
                                    newPlanName = newPlan,
                                    validity = validity,
                                    validityType = valType,
                                    validityValue = valVal,
                                    isLifetime = isLife
                                ) { isDirect, message ->
                                    if (isDirect) {
                                        val index = mockUsers.indexOfFirst { it.id == user.id || it.email.equals(user.email, ignoreCase = true) }
                                        if (index != -1) {
                                            val finalPlanName = if (newPlan.equals("Free Plan", ignoreCase = true)) "Free Plan" else newPlan
                                            val finalValidity = if (finalPlanName == "Free Plan" || isLife) "Lifetime" else validity
                                            mockUsers[index] = mockUsers[index].copy(
                                                currentPlan = finalPlanName,
                                                validity = finalValidity
                                            )
                                        }
                                    }
                                    Toast.makeText(context, LocalMessageTranslator.translateGeneralMessage(context, message), Toast.LENGTH_LONG).show()
                                }
                            },
                            onChangeValidity = { newValidity, valType, valVal, isLife, explicitUntil ->
                                viewModel.requestOrUpgradePlan(
                                    userId = user.id,
                                    userName = user.name,
                                    userEmail = user.email,
                                    newPlanName = user.currentPlan,
                                    validity = newValidity,
                                    validityType = valType,
                                    validityValue = valVal,
                                    isLifetime = isLife,
                                    explicitValidUntil = explicitUntil
                                ) { isDirect, message ->
                                    if (isDirect) {
                                        val index = mockUsers.indexOfFirst { it.id == user.id || it.email.equals(user.email, ignoreCase = true) }
                                        if (index != -1) {
                                            val finalValidity = if (user.currentPlan.equals("Free Plan", ignoreCase = true) || isLife) "Lifetime" else newValidity
                                            mockUsers[index] = mockUsers[index].copy(validity = finalValidity)
                                        }
                                    }
                                    Toast.makeText(context, LocalMessageTranslator.translateGeneralMessage(context, message), Toast.LENGTH_LONG).show()
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

data class UserLog(
    val id: String,
    val name: String,
    val email: String,
    val currentPlan: String,
    val validity: String,
    val isBlocked: Boolean,
    val role: com.example.ui.viewmodel.UserRole = com.example.ui.viewmodel.UserRole.USER
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserLogCard(
    user: UserLog,
    plans: List<com.example.data.local.PlanEntity>,
    canDeleteOrBan: Boolean,
    onBlockUser: () -> Unit,
    onDeleteUser: () -> Unit,
    onChangePlan: (planName: String, validity: String, validityType: String, validityValue: Int, isLifetime: Boolean) -> Unit,
    onChangeValidity: (validity: String, validityType: String, validityValue: Int, isLifetime: Boolean, explicitValidUntil: Long) -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showBlockDialog by remember { mutableStateOf(false) }
    var showChangePlanDialog by remember { mutableStateOf(false) }
    var showChangeValidityDialog by remember { mutableStateOf(false) }
    
    var newPlanInput by remember { mutableStateOf(user.currentPlan) }
    var selectedPlanObj by remember { mutableStateOf<com.example.data.local.PlanEntity?>(null) }
    var newValidityInput by remember { mutableStateOf(user.validity) }
    var selectedCustomDateMillis by remember { mutableStateOf(0L) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = user.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = user.email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val (roleLabel, containerColor, contentColor) = when (user.role) {
                        com.example.ui.viewmodel.UserRole.OWNER -> Triple("OWNER 👑", MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer)
                        com.example.ui.viewmodel.UserRole.ADMIN -> Triple("ADMIN ⚡", MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer)
                        com.example.ui.viewmodel.UserRole.USER -> Triple("USER", MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = containerColor
                    ) {
                        Text(
                            text = roleLabel,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = contentColor,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (user.isBlocked) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.errorContainer
                        ) {
                            Text(
                                text = "Blocked",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Current Plan",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = user.currentPlan,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Validity",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = user.validity,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = { showChangePlanDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Change Plan",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = { showChangeValidityDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.EditCalendar,
                        contentDescription = "Edit Validity",
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }
                if (canDeleteOrBan) {
                    IconButton(onClick = { showBlockDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Block,
                            contentDescription = if (user.isBlocked) "Unblock User" else "Block User",
                            tint = if (user.isBlocked) Color.Gray else MaterialTheme.colorScheme.error
                        )
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete User",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
    
    // Dialogs
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete User") },
            text = { Text("Are you sure you want to delete ${user.name}? This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = { 
                    onDeleteUser()
                    showDeleteDialog = false
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showBlockDialog) {
        val actionText = if (user.isBlocked) "unblock" else "block"
        AlertDialog(
            onDismissRequest = { showBlockDialog = false },
            title = { Text("${actionText.replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString() }} User") },
            text = { Text("Are you sure you want to $actionText ${user.name}?") },
            confirmButton = {
                TextButton(onClick = { 
                    onBlockUser()
                    showBlockDialog = false
                }) {
                    Text(actionText.replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString() })
                }
            },
            dismissButton = {
                TextButton(onClick = { showBlockDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    LaunchedEffect(showChangePlanDialog) {
        if (showChangePlanDialog) {
            newPlanInput = user.currentPlan
            selectedPlanObj = plans.find { it.planName.equals(user.currentPlan, ignoreCase = true) }
        }
    }

    LaunchedEffect(showChangeValidityDialog) {
        if (showChangeValidityDialog) {
            newValidityInput = user.validity
            selectedCustomDateMillis = 0L
        }
    }

    if (showChangePlanDialog) {
        var expandedPlanDropdown by remember { mutableStateOf(false) }
        AlertDialog(
            onDismissRequest = { showChangePlanDialog = false },
            title = { Text("Change Plan") },
            text = { 
                Column {
                    Text("Select a new plan for ${user.name}:")
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    ExposedDropdownMenuBox(
                        expanded = expandedPlanDropdown,
                        onExpandedChange = { expandedPlanDropdown = !expandedPlanDropdown }
                    ) {
                        SafeOutlinedTextField(
                            value = newPlanInput,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Selected Plan") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPlanDropdown) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            singleLine = true
                        )
                        ExposedDropdownMenu(
                            expanded = expandedPlanDropdown,
                            onDismissRequest = { expandedPlanDropdown = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Free Plan (Lifetime)") },
                                onClick = {
                                    newPlanInput = "Free Plan"
                                    selectedPlanObj = null
                                    expandedPlanDropdown = false
                                }
                            )
                            if (plans.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("Basic Plan") },
                                    onClick = {
                                        newPlanInput = "Basic Plan"
                                        expandedPlanDropdown = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Premium Access") },
                                    onClick = {
                                        newPlanInput = "Premium Access"
                                        expandedPlanDropdown = false
                                    }
                                )
                            } else {
                                plans.forEach { plan ->
                                    val displayText = if (plan.isActive) plan.planName else "${plan.planName} (Archived)"
                                    DropdownMenuItem(
                                        text = { Text(displayText) },
                                        onClick = {
                                            newPlanInput = plan.planName
                                            selectedPlanObj = plan
                                            expandedPlanDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { 
                    if (newPlanInput.equals("Free Plan", ignoreCase = true)) {
                        onChangePlan("Free Plan", "Lifetime", "LIFETIME", 0, true)
                    } else {
                        val p = selectedPlanObj ?: plans.find { it.planName == newPlanInput }
                        val vLabel = p?.planValidity?.ifBlank { p.validityLabel } ?: "1 Month"
                        val vType = p?.validityType ?: "MONTHS"
                        val vVal = p?.validityValue ?: 1
                        val isLife = p?.isLifetime ?: false
                        onChangePlan(newPlanInput, vLabel, vType, vVal, isLife)
                    }
                    showChangePlanDialog = false
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showChangePlanDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    var showDatePicker by remember { mutableStateOf(false) }

    if (showChangeValidityDialog) {
        var expandedValidityDropdown by remember { mutableStateOf(false) }
        val validityOptions = listOf("1 Week", "1 Month", "6 Months", "1 Year", "Lifetime", "Custom Date")

        AlertDialog(
            onDismissRequest = { showChangeValidityDialog = false },
            title = { Text("Edit Plan Validity") },
            text = { 
                Column {
                    Text("Select validity preset or a custom date for ${user.name}:")
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    ExposedDropdownMenuBox(
                        expanded = expandedValidityDropdown,
                        onExpandedChange = { expandedValidityDropdown = !expandedValidityDropdown }
                    ) {
                        SafeOutlinedTextField(
                            value = newValidityInput,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Validity Period") },
                            trailingIcon = { 
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = { showDatePicker = true }) {
                                        Icon(Icons.Default.EditCalendar, contentDescription = "Select Date")
                                    }
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedValidityDropdown)
                                }
                            },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            singleLine = true
                        )
                        ExposedDropdownMenu(
                            expanded = expandedValidityDropdown,
                            onDismissRequest = { expandedValidityDropdown = false }
                        ) {
                            validityOptions.forEach { opt ->
                                DropdownMenuItem(
                                    text = { Text(opt) },
                                    onClick = {
                                        if (opt == "Custom Date") {
                                            showDatePicker = true
                                        } else {
                                            newValidityInput = opt
                                            selectedCustomDateMillis = 0L
                                        }
                                        expandedValidityDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { 
                    val (vType, vVal, isLife) = when (newValidityInput) {
                        "1 Week" -> Triple("DAYS", 7, false)
                        "1 Month" -> Triple("MONTHS", 1, false)
                        "6 Months" -> Triple("MONTHS", 6, false)
                        "1 Year" -> Triple("YEARS", 1, false)
                        "Lifetime" -> Triple("LIFETIME", 0, true)
                        else -> {
                            if (selectedCustomDateMillis > 0L) {
                                Triple("CUSTOM_DATE", 0, false)
                            } else {
                                val (t, v, l) = com.example.data.util.PlanValidityEngine.normalizeValidity(newValidityInput)
                                Triple(t, v, l == "Lifetime")
                            }
                        }
                    }
                    onChangeValidity(newValidityInput, vType, vVal, isLife, selectedCustomDateMillis)
                    showChangeValidityDialog = false
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showChangeValidityDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val selectedMillis = datePickerState.selectedDateMillis
                        if (selectedMillis != null) {
                            val formattedDate = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault()).format(java.util.Date(selectedMillis))
                            newValidityInput = formattedDate
                            selectedCustomDateMillis = selectedMillis
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
