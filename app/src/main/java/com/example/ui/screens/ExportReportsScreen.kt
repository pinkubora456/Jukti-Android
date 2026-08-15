package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.viewmodel.JuktiViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportReportsScreen(viewModel: JuktiViewModel) {
    Scaffold(
        topBar = {
            com.example.ui.components.JuktiTopAppBar(
                title = "Export Reports",
                onBackClick = { viewModel.navigateTo(com.example.ui.viewmodel.Screen.OWNER_DASHBOARD) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            ExportReportsGrid(viewModel)
        }
    }
}

data class ExportReportsItem(val title: String, val icon: ImageVector, val onClick: () -> Unit = {})

@Composable
fun ExportReportsGrid(viewModel: JuktiViewModel) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val items = listOf(
        ExportReportsItem("User Report", Icons.Default.PeopleOutline, { viewModel.exportUsersCsv(context) }),
        ExportReportsItem("Purchases & Revenue", Icons.Default.AttachMoney, { viewModel.exportPurchasesCsv(context) }),
        ExportReportsItem("Mocks Report", Icons.Default.Quiz, { viewModel.exportMocksCsv(context) }),
        ExportReportsItem("Questions Report", Icons.Default.QuestionAnswer, { viewModel.exportQuestionsCsv(context) })
    )

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(items) { item ->
            ExportReportsBannerCard(item)
        }
    }
}

@Composable
fun ExportReportsBannerCard(item: ExportReportsItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { item.onClick() },
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
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = Icons.Default.Download,
                contentDescription = "Download",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
