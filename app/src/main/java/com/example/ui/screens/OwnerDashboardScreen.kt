package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.viewmodel.JuktiViewModel
import com.example.ui.viewmodel.Screen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerDashboardScreen(viewModel: JuktiViewModel) {
    var emailQuery by remember { mutableStateOf("") }
    var searchResult by remember { mutableStateOf<String?>(null) }
    var expandedAssignPlan by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            com.example.ui.components.JuktiTopAppBar(
                title = "Owner Dashboard",
                onBackClick = { viewModel.navigateTo(Screen.WORKSPACE) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Assign Plan Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clickable { expandedAssignPlan = !expandedAssignPlan }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CardMembership, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "Assign Plan to User",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Icon(
                            imageVector = if (expandedAssignPlan) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    if (expandedAssignPlan) {
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = emailQuery,
                            onValueChange = { emailQuery = it },
                            label = { Text("User Email") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                if (emailQuery.isNotBlank()) {
                                    scope.launch {
                                        val success = viewModel.grantPlanToUser(emailQuery, "Premium Pass", "1 Year")
                                        if (success) {
                                            searchResult = "Granted Premium Pass to $emailQuery"
                                        } else {
                                            searchResult = "Failed to grant Premium Pass"
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Grant Premium Pass")
                        }
                        searchResult?.let {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(it, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            Text("Management", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

            // Other Action Banners
            OwnerActionBanner(
                title = "Manage User Log",
                description = "View, block or delete users, change plans",
                icon = Icons.Default.People,
                onClick = { viewModel.navigateTo(Screen.MANAGE_USER_LOG) }
            )
            
            OwnerActionBanner(
                title = "Manage Admins",
                description = "Add or remove admin privileges",
                icon = Icons.Default.AdminPanelSettings,
                onClick = { viewModel.navigateTo(Screen.MANAGE_ADMIN) }
            )
            
            OwnerActionBanner(
                title = "Admin Activity Log",
                description = "Track actions performed by admins",
                icon = Icons.Default.List,
                onClick = { viewModel.navigateTo(Screen.ADMIN_ACTIVITY_LOG) }
            )
            
            OwnerActionBanner(
                title = "Export Reports",
                description = "Download app analytics and usage reports",
                icon = Icons.Default.Download,
                onClick = { viewModel.navigateTo(Screen.EXPORT_REPORTS) }
            )
        }
    }
}

@Composable
fun OwnerActionBanner(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
