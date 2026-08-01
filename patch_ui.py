import re

with open("app/src/main/java/com/example/ui/screens/CreatePlanScreen.kt", "r") as f:
    lines = f.readlines()

start_idx = -1
end_idx = -1
for i, line in enumerate(lines):
    if 'Text("Validity", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)' in line:
        start_idx = i - 1  # include item {
    if 'FeatureSwitch(label = "Current Affairs"' in line:
        end_idx = i + 2 # include } after item {
        
if start_idx != -1 and end_idx != -1:
    new_ui = """
            item {
                Text("Plan Benefits & Features", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            item {
                OutlinedTextField(
                    value = customFeatureInput,
                    onValueChange = { customFeatureInput = it },
                    label = { Text("Add Benefit/Feature") },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { 
                            if(customFeatureInput.isNotBlank()) {
                                featuresList.add(customFeatureInput)
                                customFeatureInput = ""
                            }
                        }) {
                            Icon(Icons.Default.Upload, contentDescription = "Add Feature")
                        }
                    },
                    singleLine = true
                )
            }
            if (featuresList.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
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
                                    Text("• $feature", style = MaterialTheme.typography.bodyMedium)
                                    IconButton(onClick = { featuresList.removeAt(index) }) {
                                        Icon(androidx.compose.material.icons.filled.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            item {
                Text("Content", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.weight(1f)) {
                        ExamDropdownMenu(
                            label = "Select Content Type",
                            selectedOption = mockTestExam,
                            onOptionSelected = { mockTestExam = it }
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = mockTestLimit,
                            onValueChange = { mockTestLimit = it },
                            label = { Text("Limit/Details") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }
            }
            item {
                Button(
                    onClick = {
                        if (mockTestExam.isNotBlank() && mockTestLimit.isNotBlank()) {
                            val contentStr = "$mockTestExam: $mockTestLimit"
                            contentsList.add(contentStr)
                            // Also add to benefits so user sees it in the banner
                            featuresList.add(contentStr)
                            mockTestExam = ""
                            mockTestLimit = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Add Content & Benefit")
                }
            }
"""
    lines[start_idx:end_idx] = [new_ui]
    
with open("app/src/main/java/com/example/ui/screens/CreatePlanScreen.kt", "w") as f:
    f.writelines(lines)
