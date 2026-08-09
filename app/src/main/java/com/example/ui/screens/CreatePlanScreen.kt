package com.example.ui.screens

import com.example.ui.components.SafeOutlinedTextField

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.example.ui.viewmodel.JuktiViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePlanScreen(viewModel: JuktiViewModel) {
    var planName by remember { mutableStateOf(TextFieldValue("")) }
    
    // Pricing
    var planPrice by remember { mutableStateOf(TextFieldValue("")) }
    var discount by remember { mutableStateOf(TextFieldValue("")) }
    var finalPrice by remember { mutableStateOf(TextFieldValue("")) }
    

    // Content & Benefits Lists
    val contentsList = remember { mutableStateListOf<String>() }
    val featuresList = remember { mutableStateListOf<String>() }
    
    val context = androidx.compose.ui.platform.LocalContext.current
    val examsList by viewModel.examsList.collectAsState()
    val examTitles = examsList.map { it.title }

    // State for Mock Test Benefit
    val mockTestExamsSelected = remember { mutableStateListOf<String>() }
    var mockTestLimitOption by remember { mutableStateOf("All") }
    var mockTestCustomLimit by remember { mutableStateOf(TextFieldValue("")) }

    // State for Questions Benefit
    val questionsExamsSelected = remember { mutableStateListOf<String>() }
    var questionsLimitOption by remember { mutableStateOf("All") }
    var questionsCustomLimit by remember { mutableStateOf(TextFieldValue("")) }

    // State for Study Notes Benefit
    val studyNotesExamsSelected = remember { mutableStateListOf<String>() }
    var studyNotesLimitOption by remember { mutableStateOf("All") }
    var studyNotesCustomLimit by remember { mutableStateOf(TextFieldValue("")) }

    // State for Current Affairs Benefit
    val currentAffairsExamsSelected = remember { mutableStateListOf<String>() }
    var currentAffairsLimitOption by remember { mutableStateOf("All") }
    var currentAffairsCustomLimit by remember { mutableStateOf(TextFieldValue("")) }

    // State for Analyze Page Benefit
    var isAnalyzePageEnabled by remember { mutableStateOf(true) }

    var customFeatureInput by remember { mutableStateOf(TextFieldValue("")) }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Plan", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo(com.example.ui.viewmodel.Screen.MANAGE_PLAN) }) {
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("Plan Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            item {
                SafeOutlinedTextField(
                    value = planName,
                    onValueChange = { planName = it },
                    label = { Text("Plan Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            item {
                Text("Pricing (Mandatory)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SafeOutlinedTextField(
                        value = planPrice,
                        onValueChange = { 
                            planPrice = it 
                            val price = it.text.toDoubleOrNull() ?: 0.0
                            val disc = discount.text.toDoubleOrNull() ?: 0.0
                            if (price >= 0 && disc in 0.0..100.0) {
                                finalPrice = TextFieldValue((price - (price * disc / 100)).toInt().toString())
                            }
                        },
                        label = { Text("Price (₹) *") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    SafeOutlinedTextField(
                        value = discount,
                        onValueChange = { 
                            discount = it 
                            val price = planPrice.text.toDoubleOrNull() ?: 0.0
                            val disc = it.text.toDoubleOrNull() ?: 0.0
                            if (price >= 0 && disc in 0.0..100.0) {
                                finalPrice = TextFieldValue((price - (price * disc / 100)).toInt().toString())
                            }
                        },
                        label = { Text("Discount % *") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            }
            item {
                SafeOutlinedTextField(
                    value = finalPrice,
                    onValueChange = { finalPrice = it },
                    label = { Text("Final Price (₹) *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            item {
                Text("Plan Benefits & Features", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            // 1. Mock Test Benefit Config
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Mock Test Benefit", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.weight(1.2f)) {
                                MultiSelectExamDropdownMenu(
                                    label = "Select Exams",
                                    selectedExams = mockTestExamsSelected,
                                    examOptions = examTitles
                                )
                            }
                            Box(modifier = Modifier.weight(0.8f)) {
                                GenericDropdownMenu(
                                    label = "Limit",
                                    selectedOption = mockTestLimitOption,
                                    options = listOf("All", "Custom"),
                                    onOptionSelected = { mockTestLimitOption = it }
                                )
                            }
                        }
                        if (mockTestLimitOption == "Custom") {
                            SafeOutlinedTextField(
                                value = mockTestCustomLimit,
                                onValueChange = { mockTestCustomLimit = it },
                                label = { Text("Limit Details (e.g. 10 Tests)") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }
                        Button(
                            onClick = {
                                val limit = if (mockTestLimitOption == "All") "All" else mockTestCustomLimit.text.ifBlank { "Custom" }
                                val examStr = if (mockTestExamsSelected.isEmpty()) "All Exams" else mockTestExamsSelected.joinToString(", ")
                                val featureStr = "Mock Test ($examStr): $limit"
                                featuresList.add(featureStr)
                                contentsList.add(featureStr)
                                if (mockTestLimitOption == "Custom") {
                                    mockTestCustomLimit = TextFieldValue("")
                                }
                            },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Add Mock Test")
                        }
                    }
                }
            }

            // 2. Questions Benefit Config
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Questions Benefit", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.weight(1.2f)) {
                                MultiSelectExamDropdownMenu(
                                    label = "Select Exams",
                                    selectedExams = questionsExamsSelected,
                                    examOptions = examTitles
                                )
                            }
                            Box(modifier = Modifier.weight(0.8f)) {
                                GenericDropdownMenu(
                                    label = "Limit",
                                    selectedOption = questionsLimitOption,
                                    options = listOf("All", "Custom"),
                                    onOptionSelected = { questionsLimitOption = it }
                                )
                            }
                        }
                        if (questionsLimitOption == "Custom") {
                            SafeOutlinedTextField(
                                value = questionsCustomLimit,
                                onValueChange = { questionsCustomLimit = it },
                                label = { Text("Limit Details (e.g. 500 Qs)") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }
                        Button(
                            onClick = {
                                val limit = if (questionsLimitOption == "All") "All" else questionsCustomLimit.text.ifBlank { "Custom" }
                                val examStr = if (questionsExamsSelected.isEmpty()) "All Exams" else questionsExamsSelected.joinToString(", ")
                                val featureStr = "Questions ($examStr): $limit"
                                featuresList.add(featureStr)
                                contentsList.add(featureStr)
                                if (questionsLimitOption == "Custom") {
                                    questionsCustomLimit = TextFieldValue("")
                                }
                            },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Add Questions")
                        }
                    }
                }
            }

            // 3. Study Notes Benefit Config
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Study Notes Benefit", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.weight(1.2f)) {
                                MultiSelectExamDropdownMenu(
                                    label = "Select Exams",
                                    selectedExams = studyNotesExamsSelected,
                                    examOptions = examTitles
                                )
                            }
                            Box(modifier = Modifier.weight(0.8f)) {
                                GenericDropdownMenu(
                                    label = "Limit",
                                    selectedOption = studyNotesLimitOption,
                                    options = listOf("All", "Custom"),
                                    onOptionSelected = { studyNotesLimitOption = it }
                                )
                            }
                        }
                        if (studyNotesLimitOption == "Custom") {
                            SafeOutlinedTextField(
                                value = studyNotesCustomLimit,
                                onValueChange = { studyNotesCustomLimit = it },
                                label = { Text("Limit Details (e.g. 50 Notes)") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }
                        Button(
                            onClick = {
                                val limit = if (studyNotesLimitOption == "All") "All" else studyNotesCustomLimit.text.ifBlank { "Custom" }
                                val examStr = if (studyNotesExamsSelected.isEmpty()) "All Exams" else studyNotesExamsSelected.joinToString(", ")
                                val featureStr = "Study Notes ($examStr): $limit"
                                featuresList.add(featureStr)
                                contentsList.add(featureStr)
                                if (studyNotesLimitOption == "Custom") {
                                    studyNotesCustomLimit = TextFieldValue("")
                                }
                            },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Add Study Notes")
                        }
                    }
                }
            }

            // 4. Current Affairs Benefit Config
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Current Affairs Benefit", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.weight(1.2f)) {
                                MultiSelectExamDropdownMenu(
                                    label = "Select Exams",
                                    selectedExams = currentAffairsExamsSelected,
                                    examOptions = examTitles
                                )
                            }
                            Box(modifier = Modifier.weight(0.8f)) {
                                GenericDropdownMenu(
                                    label = "Limit",
                                    selectedOption = currentAffairsLimitOption,
                                    options = listOf("All", "Custom"),
                                    onOptionSelected = { currentAffairsLimitOption = it }
                                )
                            }
                        }
                        if (currentAffairsLimitOption == "Custom") {
                            SafeOutlinedTextField(
                                value = currentAffairsCustomLimit,
                                onValueChange = { currentAffairsCustomLimit = it },
                                label = { Text("Limit Details (e.g. 12 Months)") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }
                        Button(
                            onClick = {
                                val limit = if (currentAffairsLimitOption == "All") "All" else currentAffairsCustomLimit.text.ifBlank { "Custom" }
                                val examStr = if (currentAffairsExamsSelected.isEmpty()) "All Exams" else currentAffairsExamsSelected.joinToString(", ")
                                val featureStr = "Current Affairs ($examStr): $limit"
                                featuresList.add(featureStr)
                                contentsList.add(featureStr)
                                if (currentAffairsLimitOption == "Custom") {
                                    currentAffairsCustomLimit = TextFieldValue("")
                                }
                            },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Add Current Affairs")
                        }
                    }
                }
            }

            // 5. Analyze Page Benefit Config
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Analyze Page Benefit", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Access to Analyze Page?", style = MaterialTheme.typography.bodyMedium)
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(if (isAnalyzePageEnabled) "Yes" else "No", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                Switch(
                                    checked = isAnalyzePageEnabled,
                                    onCheckedChange = { isAnalyzePageEnabled = it },
                                    colors = SwitchDefaults.colors(
                                        uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                                        uncheckedBorderColor = MaterialTheme.colorScheme.outline
                                    )
                                )
                            }
                        }
                        Button(
                            onClick = {
                                val featureStr = "Analyze Page Access: " + (if (isAnalyzePageEnabled) "Yes" else "No")
                                featuresList.add(featureStr)
                                contentsList.add(featureStr)
                            },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Add Analyze Page Option")
                        }
                    }
                }
            }

            // 6. Custom Feature / Benefit Config
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Custom Benefit / Feature", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        SafeOutlinedTextField(
                            value = customFeatureInput,
                            onValueChange = { customFeatureInput = it },
                            label = { Text("Enter Custom Benefit Text") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Button(
                            onClick = {
                                if (customFeatureInput.text.isNotBlank()) {
                                    featuresList.add(customFeatureInput.text.trim())
                                    contentsList.add(customFeatureInput.text.trim())
                                    customFeatureInput = TextFieldValue("")
                                }
                            },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Add Custom Benefit")
                        }
                    }
                }
            }

            // Added Benefits List View
            if (featuresList.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Added Benefits", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(8.dp))
                            featuresList.forEachIndexed { index, feature ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("• $feature", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                                    IconButton(onClick = { 
                                        featuresList.removeAt(index) 
                                        if (index < contentsList.size) {
                                            contentsList.removeAt(index)
                                        }
                                    }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = { /* Handle Image Upload */ },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Icon(Icons.Default.Upload, contentDescription = "Upload Photo")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Upload Photo for Direct Publish")
                }
            }

            item {
                Button(
                    onClick = {
                        if (planName.text.isNotBlank() && finalPrice.text.isNotBlank()) {
                            val newPlan = com.example.data.local.PlanEntity(
                                planName = planName.text,
                                planPrice = planPrice.text,
                                discount = discount.text,
                                finalPrice = finalPrice.text,
                                offerValidity = "", // Removed per user request
                                contents = contentsList.joinToString(separator = "|"),
                                features = featuresList.joinToString(separator = "|")
                            )
                            viewModel.requestOrCreatePlan(newPlan) { _, message ->
                                android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_LONG).show()
                                viewModel.navigateTo(com.example.ui.viewmodel.Screen.MANAGE_PLAN)
                            }
                        } else {
                            android.widget.Toast.makeText(context, "Please fill in plan name and price.", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                ) {
                    Text("Create Plan")
                }
            }
        }
    }
}

@Composable
fun FeatureSwitch(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
        Switch(
            checked = checked, 
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                uncheckedBorderColor = MaterialTheme.colorScheme.outline
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenericDropdownMenu(label: String, selectedOption: String, options: List<String>, onOptionSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        SafeOutlinedTextField(
            value = selectedOption,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiSelectExamDropdownMenu(
    label: String,
    selectedExams: MutableList<String>,
    examOptions: List<String>
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        SafeOutlinedTextField(
            value = if (selectedExams.isEmpty()) "All Exams" else selectedExams.joinToString(", "),
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = selectedExams.isEmpty(),
                            onCheckedChange = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("All Exams", fontWeight = FontWeight.Bold)
                    }
                },
                onClick = {
                    selectedExams.clear()
                }
            )
            examOptions.forEach { option ->
                val isSelected = selectedExams.contains(option)
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(option)
                        }
                    },
                    onClick = {
                        if (isSelected) {
                            selectedExams.remove(option)
                        } else {
                            selectedExams.add(option)
                        }
                    }
                )
            }
        }
    }
}
