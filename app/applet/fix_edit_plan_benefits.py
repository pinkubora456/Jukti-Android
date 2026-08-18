
file_path = '/app/applet/app/src/main/java/com/example/ui/screens/EditPlanScreen.kt'
with open(file_path, 'r') as f:
    content = f.read()

insertion = """
                SafeOutlinedTextField(
                    value = examTarget,
                    onValueChange = { examTarget = it },
                    label = { Text("Exam Target") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Text("Benefits", style = MaterialTheme.typography.titleSmall)
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    SafeOutlinedTextField(
                        value = newBenefit,
                        onValueChange = { newBenefit = it },
                        label = { Text("Add Benefit") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    IconButton(onClick = {
                        if (newBenefit.isNotBlank()) {
                            benefits.add(newBenefit.trim())
                            newBenefit = ""
                        }
                    }) {
                        Icon(Icons.Default.Edit, contentDescription = "Add Benefit")
                    }
                }
                benefits.forEachIndexed { index, benefit ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(benefit)
                        IconButton(onClick = { benefits.removeAt(index) }) {
                            Icon(Icons.Default.Close, contentDescription = "Remove Benefit")
                        }
                    }
                }
                Row(
"""

import re
pattern = re.compile(r'SafeOutlinedTextField\(\s+value = examTarget,.*?modifier = Modifier\.fillMaxWidth\(\),\s+singleLine = true\s+\)\s+Row\(', re.DOTALL)
new_content = pattern.sub(insertion.strip() + '\n                Row(', content)

with open(file_path, 'w') as f:
    f.write(new_content)
