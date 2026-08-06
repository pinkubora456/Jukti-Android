import re
with open("app/src/main/java/com/example/ui/screens/ManageNotificationsScreen.kt", "r") as f:
    content = f.read()

# I will find the `if (showCategoryManageDialog) {` and move it.
# Everything from `        if (showCategoryManageDialog) {` to `    }\n}\n\n@Composable\nfun SentNotificationCard` is the dialog code (plus the closing brace).

start = content.find("        if (showCategoryManageDialog) {")
end = content.find("@Composable\nfun SentNotificationCard")

dialog_code_with_braces = content[start:end]

# It looks like:
#        if (showCategoryManageDialog) {
#            ...
#        }
#    }
#}
#

# I will replace `dialog_code_with_braces` with:
new_end = """
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
"""

content = content.replace(dialog_code_with_braces, new_end + "\n\n")

with open("app/src/main/java/com/example/ui/screens/ManageNotificationsScreen.kt", "w") as f:
    f.write(content)
