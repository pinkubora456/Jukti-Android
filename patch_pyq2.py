import re

with open('app/src/main/java/com/example/ui/screens/ManageGuidanceScreen.kt', 'r') as f:
    content = f.read()

replacement = """@Composable
fun PyqFocusRowItem(
    item: PyqFocusEntity,
    onSave: (PyqFocusEntity) -> Unit,
    onDelete: () -> Unit
) {
    var pyqCountStr by remember(item.pyqCount) { mutableStateOf(item.pyqCount.toString()) }
    var examsCoveredStr by remember(item.examsCovered) { mutableStateOf(item.examsCovered.toString()) }

    val isModified = pyqCountStr != item.pyqCount.toString() || examsCoveredStr != item.examsCovered.toString()

    val avg = if ((examsCoveredStr.toIntOrNull() ?: 0) > 0) {
        (pyqCountStr.toFloatOrNull() ?: 0f) / (examsCoveredStr.toFloatOrNull() ?: 1f)
    } else 0f

    val (impLabel, impColor) = when {
        avg >= 2.0f -> Pair("High", Color(0xFFE53935))
        avg >= 1.0f -> Pair("Med", Color(0xFFFB8C00))
        else -> Pair("Low", Color(0xFF757575))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Column(modifier = Modifier.weight(2.2f)) {
                Text(
                    text = item.chapter,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = item.subject,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            SafeOutlinedTextField(
                value = pyqCountStr,
                onValueChange = { pyqCountStr = it },
                modifier = Modifier.weight(1.1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            SafeOutlinedTextField(
                value = examsCoveredStr,
                onValueChange = { examsCoveredStr = it },
                modifier = Modifier.weight(1.1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Column(
                modifier = Modifier.weight(1.3f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = String.format(java.util.Locale.US, "%.1f", avg),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = impColor.copy(alpha = 0.15f),
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Text(
                        text = impLabel,
                        color = impColor,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.width(36.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val context = androidx.compose.ui.platform.LocalContext.current
                if (isModified) {
                    IconButton(
                        onClick = {
                            val newCount = pyqCountStr.toIntOrNull() ?: 0
                            val newCovered = examsCoveredStr.toIntOrNull() ?: 0
                            onSave(item.copy(pyqCount = newCount, examsCovered = newCovered, updatedAt = System.currentTimeMillis()))
                            Toast.makeText(context, "Changes Saved to Cloud", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Save", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    }
                } else {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}"""

# Find the start and end of the function
start_idx = content.find("@Composable\nfun PyqFocusRowItem(")
if start_idx != -1:
    end_idx = content.find("/* ==========================================================================\n   SECTION 2: PRIORITY TOPICS MANAGEMENT", start_idx)
    if end_idx != -1:
        # We need to preserve any newlines before the section comment
        new_content = content[:start_idx] + replacement + "\n\n" + content[end_idx:]
        with open('app/src/main/java/com/example/ui/screens/ManageGuidanceScreen.kt', 'w') as f:
            f.write(new_content)
            print("Successfully replaced PyqFocusRowItem")
    else:
        print("Could not find end of function")
else:
    print("Could not find start of function")
