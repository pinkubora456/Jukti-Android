package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.viewmodel.JuktiViewModel
import com.example.ui.viewmodel.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkspaceScreen(viewModel: JuktiViewModel) {
    val isAdminOrOwner by viewModel.isAdminOrOwner.collectAsState()
    val isOwner by viewModel.isOwner.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
           WorkspaceBannerCard(
            title = { Text("Workspace", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
               WorkspaceBannerCard(
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
            if (isAdminOrOwner) {
                WorkspaceGrid(isOwner = isOwner, viewModel = viewModel)
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Access restricted to Admins & Owners.")
                }
            }
        }
    }
}

@Composable
fun WorkspaceGrid(isOwner: Boolean, viewModel: JuktiViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (isOwner) {
     
           WorkspaceBannerCard(
            title = "Owner Dashboard",
                icon = Icons.Default.Dashboard,
                onClick = { viewModel.navigateTo(com.example.ui.viewmodel.Screen.OWNER_DASHBOARD) }
            )
        }
        
 
            title = "Manage Q-Bank",
            icon = Icons.Default.LibraryBooks,
            onClick = { viewModel.navigateTo(com.example.ui.viewmodel.Screen.MANAGE_QBANK) }
        )
 
            title = "Manage Mocks",
            icon = Icons.Default.Timer,
            onClick = { viewModel.navigateTo(com.example.ui.viewmodel.Screen.MANAGE_MOCK) }
        )
 
            title = "Manage Plans",
            icon = Icons.Default.CardMembership,
            onClick = { viewModel.navigateTo(com.example.ui.viewmodel.Screen.MANAGE_PLAN) }
        )
 
            title = "Manage Exams",
            icon = Icons.Default.School,
            onClick = { viewModel.navigateTo(com.example.ui.viewmodel.Screen.MANAGE_EXAMS) }
        )
 
            title = "Manage User Log",
            icon = Icons.Default.People,
            onClick = { viewModel.navigateTo(com.example.ui.viewmodel.Screen.MANAGE_USER_LOG) }
        )
 
            title = "Reported Questions",
            icon = Icons.Default.BugReport,
            onClick = { viewModel.navigateTo(com.example.ui.viewmodel.Screen.REPORTED_QUESTIONS) }
        )
 
 
            title = "Manage Current Affairs",
            icon = Icons.Default.Newspaper,
            onClick = { viewModel.navigateTo(com.example.ui.viewmodel.Screen.MANAGE_CURRENT_AFFAIRS) }
        )
        WorkspaceBannerCard(
            title = "Manage Study Notes",
            icon = Icons.Default.MenuBook,
            onClick = { viewModel.navigateTo(com.example.ui.viewmodel.Screen.MANAGE_STUDY_NOTES) }
        )
 
            title = "Manage Subjects & Chapters",
            icon = Icons.Default.Category,
            onClick = { viewModel.navigateTo(com.example.ui.viewmodel.Screen.MANAGE_SUBJECTS_CHAPTERS) }
        )
 
            title = "Information Banners",
            icon = Icons.Default.ViewCarousel,
            onClick = { viewModel.navigateTo(Screen.MANAGE_BANNERS) }
        )
 
            title = "Exam Patern & Cutoff",
            icon = Icons.Default.Analytics,
            onClick = { viewModel.navigateTo(Screen.MANAGE_EXAM_PATTERN_CUTOFF) }
        )
 
            title = "Notification",
            icon = Icons.Default.Notifications,
            onClick = { viewModel.navigateTo(Screen.MANAGE_NOTIFICATIONS) }
        )

    }
}

@Composable
fun WorkspaceBannerCard(title: String, icon: ImageVector, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
