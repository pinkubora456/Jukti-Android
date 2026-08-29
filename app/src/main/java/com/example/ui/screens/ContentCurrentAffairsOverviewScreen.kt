package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.JuktiTopAppBar
import com.example.ui.viewmodel.JuktiViewModel
import com.example.ui.viewmodel.Screen

@Composable
fun ContentCurrentAffairsOverviewScreen(viewModel: JuktiViewModel) {
    val allNotes by viewModel.studyNotes.collectAsState()
    val allCurrentAffairs = remember(allNotes) { allNotes.filter { it.subject.contains("Current Affairs", ignoreCase = true) } }

    Scaffold(
        topBar = {
            JuktiTopAppBar(
                title = "Current Affairs Overview",
                onBackClick = { viewModel.navigateTo(Screen.CONTENT_OVERVIEW) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(
                "Total Current Affairs: ${allCurrentAffairs.size}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(allCurrentAffairs) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(item.titleEn, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Category: ${item.topic}", style = MaterialTheme.typography.bodySmall)
                            Text("Read Time: ${item.readTimeMinutes} min", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}
