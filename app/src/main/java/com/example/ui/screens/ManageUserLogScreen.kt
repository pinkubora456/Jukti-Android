package com.example.ui.screens

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageUserLogScreen(viewModel: JuktiViewModel) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    
    // Mock user data
    val mockUsers = remember {
        mutableStateListOf(
            UserLog("1", "John Doe", "john@example.com", "Free Plan", "Lifetime", false),
            UserLog("2", "Alice Smith", "alice@example.com", "Premium 6 Months", "Valid till Dec 2026", false),
            UserLog("3", "Bob Jones", "bob@example.com", "Premium 1 Year", "Valid till Jun 2027", true)
        )
    }

    val filteredUsers = mockUsers.filter {
        it.name.contains(searchQuery, ignoreCase = true) || it.email.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage User Log", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo(com.example.ui.viewmodel.Screen.WORKSPACE) }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search Bar
            OutlinedTextField(
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

            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(filteredUsers) { user ->
                    UserLogCard(
                        user = user,
                        onBlockUser = { 
                            viewModel.requestOrBlockUser(user.id, user.name, user.email) { isDirect, message ->
                                if (isDirect) {
                                    val index = mockUsers.indexOfFirst { it.id == user.id }
                                    if (index != -1) {
                                        mockUsers[index] = mockUsers[index].copy(isBlocked = !mockUsers[index].isBlocked)
                                    }
                                }
                                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                            }
                        },
                        onDeleteUser = {
                            viewModel.requestOrDeleteUser(user.id, user.name, user.email) { isDirect, message ->
                                if (isDirect) {
                                    mockUsers.removeAll { it.id == user.id }
                                }
                                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                            }
                        },
                        onChangePlan = { newPlan ->
                            viewModel.requestOrUpgradePlan(user.id, user.name, user.email, newPlan, user.validity) { isDirect, message ->
                                if (isDirect) {
                                    val index = mockUsers.indexOfFirst { it.id == user.id }
                                    if (index != -1) {
                                        mockUsers[index] = mockUsers[index].copy(currentPlan = newPlan)
                                    }
                                }
                                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                            }
                        },
                        onChangeValidity = { newValidity ->
                            viewModel.requestOrUpgradePlan(user.id, user.name, user.email, user.currentPlan, newValidity) { isDirect, message ->
                                if (isDirect) {
                                    val index = mockUsers.indexOfFirst { it.id == user.id }
                                    if (index != -1) {
                                        mockUsers[index] = mockUsers[index].copy(validity = newValidity)
                                    }
                                }
                                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                            }
                        }
                    )
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
    val isBlocked: Boolean
)

@Composable
fun UserLogCard(
    user: UserLog,
    onBlockUser: () -> Unit,
    onDeleteUser: () -> Unit,
    onChangePlan: (String) -> Unit,
    onChangeValidity: (String) -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showBlockDialog by remember { mutableStateOf(false) }
    var showChangePlanDialog by remember { mutableStateOf(false) }
    var showChangeValidityDialog by remember { mutableStateOf(false) }
    
    var newPlanInput by remember { mutableStateOf(user.currentPlan) }
    var newValidityInput by remember { mutableStateOf(user.validity) }

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
                Column {
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
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Divider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            
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
            title = { Text("${"${actionText.replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString() }}"} User") },
            text = { Text("Are you sure you want to $actionText ${user.name}?") },
            confirmButton = {
                TextButton(onClick = { 
                    onBlockUser()
                    showBlockDialog = false
                }) {
                    Text("${actionText.replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString() }}")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBlockDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showChangePlanDialog) {
        AlertDialog(
            onDismissRequest = { showChangePlanDialog = false },
            title = { Text("Change Plan") },
            text = { 
                Column {
                    Text("Select a new plan for ${user.name}:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newPlanInput,
                        onValueChange = { newPlanInput = it },
                        label = { Text("New Plan") }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { 
                    onChangePlan(newPlanInput)
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

    if (showChangeValidityDialog) {
        AlertDialog(
            onDismissRequest = { showChangeValidityDialog = false },
            title = { Text("Edit Plan Validity") },
            text = { 
                Column {
                    Text("Update validity for ${user.name}:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newValidityInput,
                        onValueChange = { newValidityInput = it },
                        label = { Text("New Validity") }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { 
                    onChangeValidity(newValidityInput)
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
}
