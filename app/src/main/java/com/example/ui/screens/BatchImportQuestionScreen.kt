package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ui.viewmodel.JuktiViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatchImportQuestionScreen(viewModel: JuktiViewModel) {
    var showDialog by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            com.example.ui.components.JuktiTopAppBar(
                title = "Batch Import Questions",
                onBackClick = { viewModel.navigateTo(com.example.ui.viewmodel.Screen.MANAGE_QBANK) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (showDialog) {
                com.example.ui.components.BatchImportMockQuestionsDialog(
                    viewModel = viewModel,
                    isGeneralQBankImport = true,
                    onDismiss = {
                        showDialog = false
                        viewModel.navigateTo(com.example.ui.viewmodel.Screen.MANAGE_QBANK)
                    },
                    onQuestionsImported = { _, _ ->
                        showDialog = false
                        viewModel.navigateTo(com.example.ui.viewmodel.Screen.MANAGE_QBANK)
                    }
                )
            } else {
                Button(onClick = { showDialog = true }) {
                    Text("Open CSV Importer")
                }
            }
        }
    }
}
