import re
with open("app/src/main/java/com/example/ui/screens/ManageNotificationsScreen.kt", "r") as f:
    content = f.read()

# Replace val categories = listOf(...) with state collection
# Also add imports
imports = """
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import com.example.data.local.NotificationCategoryEntity
"""

content = content.replace("import com.example.data.local.NotificationEntity", "import com.example.data.local.NotificationEntity" + imports)

state_declaration = """
    val notifications by viewModel.notifications.collectAsState()
    val notificationCategories by viewModel.allNotificationCategories.collectAsState()

    var showCategoryManageDialog by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }
"""

content = content.replace("    val notifications by viewModel.notifications.collectAsState()", state_declaration)
content = content.replace("    val categories = listOf(\"General\", \"Mock Test\", \"Exam Update\", \"Study Note\", \"Announcement\")", "")

# The Dropdown
old_dropdown = """
                    // Category Selector Dropdown
                    ExposedDropdownMenuBox(
                        expanded = dropdownExpanded,
                        onExpandedChange = { dropdownExpanded = !dropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = category,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Category") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                                .testTag("notif_category_dropdown")
                        )
                        ExposedDropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false }
                        ) {
                            categories.forEach { selectionOption ->
                                DropdownMenuItem(
                                    text = { Text(selectionOption) },
                                    onClick = {
                                        category = selectionOption
                                        dropdownExpanded = false
                                    },
                                    modifier = Modifier.testTag("notif_cat_item_$selectionOption")
                                )
                            }
                        }
                    }
"""

new_dropdown = """
                    // Category Selector Dropdown
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ExposedDropdownMenuBox(
                            expanded = dropdownExpanded,
                            onExpandedChange = { dropdownExpanded = !dropdownExpanded },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = category,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Category") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                                    .testTag("notif_category_dropdown")
                            )
                            ExposedDropdownMenu(
                                expanded = dropdownExpanded,
                                onDismissRequest = { dropdownExpanded = false }
                            ) {
                                notificationCategories.forEach { selectionOption ->
                                    DropdownMenuItem(
                                        text = { Text(selectionOption.name) },
                                        onClick = {
                                            category = selectionOption.name
                                            dropdownExpanded = false
                                        },
                                        modifier = Modifier.testTag("notif_cat_item_${selectionOption.name}")
                                    )
                                }
                            }
                        }
                        
                        IconButton(
                            onClick = { showCategoryManageDialog = true },
                            modifier = Modifier
                                .size(56.dp)
                                .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(12.dp))
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = "Manage Categories", tint = MaterialTheme.colorScheme.onSecondaryContainer)
                        }
                    }
"""

content = content.replace(old_dropdown, new_dropdown)

dialog_code = """
        if (showCategoryManageDialog) {
            AlertDialog(
                onDismissRequest = { showCategoryManageDialog = false },
                title = { Text("Manage Categories") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = newCategoryName,
                            onValueChange = { newCategoryName = it },
                            label = { Text("New Category Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                if (newCategoryName.isNotBlank()) {
                                    viewModel.addNotificationCategory(newCategoryName.trim())
                                    newCategoryName = ""
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Category")
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                            items(notificationCategories) { cat ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(cat.name)
                                    IconButton(onClick = {
                                        viewModel.deleteNotificationCategory(cat)
                                        if (category == cat.name) {
                                            category = notificationCategories.firstOrNull { it.id != cat.id }?.name ?: "General"
                                        }
                                    }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete Category", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showCategoryManageDialog = false }) {
                        Text("Close")
                    }
                }
            )
        }
    }
}
"""

# replace the last `    }\n}` with `    }\n` + dialog_code
content = content.replace("    }\n}\n\n@Composable\nfun SentNotificationCard", "    }\n" + dialog_code + "\n\n@Composable\nfun SentNotificationCard")

with open("app/src/main/java/com/example/ui/screens/ManageNotificationsScreen.kt", "w") as f:
    f.write(content)
