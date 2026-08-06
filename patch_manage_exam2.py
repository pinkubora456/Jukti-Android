with open("app/src/main/java/com/example/ui/screens/ManageExamPatternCutoffScreen.kt", "r") as f:
    content = f.read()

start_idx = content.find("// Category Selector")
# find the corresponding end of this box. Let's find "val titleEn" and cut before it.
end_idx = content.find("// English Title")

target_chunk = content[start_idx:end_idx]
print("Found chunk to replace:")
print(target_chunk)

replacement = """// Category Selector
                            ExposedDropdownMenuBox(
                                expanded = categoryDropdownExpanded,
                                onExpandedChange = { categoryDropdownExpanded = !categoryDropdownExpanded },
                                modifier = Modifier.weight(1f)
                            ) {
                                OutlinedTextField(
                                    value = category,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Category") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor()
                                        .testTag("category_selector")
                                )
                                ExposedDropdownMenu(
                                    expanded = categoryDropdownExpanded,
                                    onDismissRequest = { categoryDropdownExpanded = false }
                                ) {
                                    categories.forEach { cat ->
                                        DropdownMenuItem(
                                            text = { Text(cat) },
                                            onClick = {
                                                category = cat
                                                categoryDropdownExpanded = false
                                            },
                                            modifier = Modifier.testTag("category_item_$cat")
                                        )
                                    }
                                }
                            }
                        }

                        """

content = content.replace(target_chunk, replacement)

with open("app/src/main/java/com/example/ui/screens/ManageExamPatternCutoffScreen.kt", "w") as f:
    f.write(content)
print("Patch applied successfully.")
