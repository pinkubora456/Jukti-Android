package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ui.viewmodel.JuktiViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StorageManagementScreen(viewModel: JuktiViewModel) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Storage Management") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo(com.example.ui.viewmodel.Screen.SETTINGS) }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            Text("Question Cache Size: ~12 MB", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Number of Cached Questions: 1,450", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { /* clear cache logic */ }) {
                Text("Clear Question Cache")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Note: Saved questions, frequently incorrect questions, and important statistics will not be deleted.", style = MaterialTheme.typography.bodySmall)
        }
    }
}
