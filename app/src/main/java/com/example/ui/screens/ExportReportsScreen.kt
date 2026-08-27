package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.viewmodel.JuktiViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportReportsScreen(viewModel: JuktiViewModel) {
    val isAdminOrOwner by viewModel.isAdminOrOwner.collectAsState()

    Scaffold(
        topBar = {
            com.example.ui.components.JuktiTopAppBar(
                title = "Export Reports",
                onBackClick = { viewModel.navigateTo(com.example.ui.viewmodel.Screen.OWNER_DASHBOARD) }
            )
        }
    ) { innerPadding ->
        if (!isAdminOrOwner) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Access Denied",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Access Restricted",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Only authorized Owner and Admin users may access administrative data exports.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        } else {
            ExportReportsContent(viewModel, Modifier.padding(innerPadding))
        }
    }
}

@Composable
fun ExportReportsContent(viewModel: JuktiViewModel, modifier: Modifier = Modifier) {
    val context = LocalContext.current

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Assessment,
                        contentDescription = "Reports Header",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Administrative Reports & Exports",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Generate authoritative CSV reports for auditing, financial reconciliation, and content management.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        // 1. User Report
        item {
            ReportCategoryCard(
                title = "1. User Report",
                badge = "Accounts & Activity",
                description = "Account details, registration date, active plans, exam targets, accuracy %, questions attempted, and mock test metrics.",
                icon = Icons.Default.People,
                actions = listOf(
                    ReportAction("Export User Report", Icons.Default.Download) {
                        viewModel.exportUsersCsv(context)
                    }
                )
            )
        }

        // 2. Purchase Report
        item {
            ReportCategoryCard(
                title = "2. Purchase & Refund Report",
                badge = "Transactions & Audit",
                description = "Individual sales transactions, masked purchase tokens, Google Play order IDs, verification statuses, and standalone refund history.",
                icon = Icons.Default.ReceiptLong,
                actions = listOf(
                    ReportAction("Export Purchase Report", Icons.Default.Download) {
                        viewModel.exportPurchasesCsv(context)
                    },
                    ReportAction("Export Refund Report", Icons.Default.History) {
                        viewModel.exportRefundsCsv(context)
                    }
                )
            )
        }

        // 3. Question Report
        item {
            ReportCategoryCard(
                title = "3. Question Report",
                badge = "Content Management",
                description = "Complete Question Bank export (including Assamese translations and solutions) and Mock Test-wise ordered question composition.",
                icon = Icons.Default.MenuBook,
                actions = listOf(
                    ReportAction("Full Question Bank Export", Icons.Default.Download) {
                        viewModel.exportFullQuestionBankCsv(context)
                    },
                    ReportAction("Mock Test-wise Questions Export", Icons.Default.Quiz) {
                        viewModel.exportMockTestQuestionsCsv(context)
                    }
                )
            )
        }

        // 4. Revenue Report
        item {
            ReportCategoryCard(
                title = "4. Revenue & Settlement Report",
                badge = "Financial Reconciliation",
                description = "Gross transaction revenue, refund deductions, estimated net revenue, and Google Play financial reconciliation tracking.",
                icon = Icons.Default.AccountBalanceWallet,
                actions = listOf(
                    ReportAction("Export Revenue & Settlement", Icons.Default.Download) {
                        viewModel.exportRevenueCsv(context)
                    }
                )
            )
        }
    }
}

data class ReportAction(
    val label: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

@Composable
fun ReportCategoryCard(
    title: String,
    badge: String,
    description: String,
    icon: ImageVector,
    actions: List<ReportAction>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Text(
                            text = badge,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                actions.forEach { action ->
                    Button(
                        onClick = action.onClick,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = action.icon,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = action.label)
                    }
                }
            }
        }
    }
}
