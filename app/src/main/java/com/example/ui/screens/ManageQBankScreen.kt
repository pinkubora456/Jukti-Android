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
import com.example.ui.components.JuktiTopAppBar
import com.example.ui.viewmodel.JuktiViewModel

@Composable
fun ManageQBankScreen(viewModel: JuktiViewModel) {
    Scaffold(
        topBar = {
            JuktiTopAppBar(
                title = "Manage Q-Bank",
                onBackClick = { viewModel.navigateTo(com.example.ui.viewmodel.Screen.WORKSPACE) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            ManageQBankGrid(viewModel)
        }
    }
}

data class ManageQBankItem(val title: String, val icon: ImageVector, val onClick: () -> Unit = {})

@Composable
fun ManageQBankGrid(viewModel: JuktiViewModel) {
    val items = listOf(
        ManageQBankItem("Single Question Upload", Icons.Default.PostAdd) {
            viewModel.navigateTo(com.example.ui.viewmodel.Screen.SINGLE_QUESTION_UPLOAD)
        },
        ManageQBankItem("Batch Import Question", Icons.Default.UploadFile) {
            viewModel.navigateTo(com.example.ui.viewmodel.Screen.BATCH_IMPORT_QUESTION)
        },
        ManageQBankItem("Question Bank", Icons.Default.FormatListBulleted) {
            viewModel.navigateTo(com.example.ui.viewmodel.Screen.ALL_QUESTIONS)
        }
    )

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(items) { item ->
            ManageQBankBannerCard(item)
        }
    }
}

@Composable
fun ManageQBankBannerCard(item: ManageQBankItem) {
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
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
