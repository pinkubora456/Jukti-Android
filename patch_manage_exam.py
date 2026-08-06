with open("app/src/main/java/com/example/ui/screens/ManageExamPatternCutoffScreen.kt", "r") as f:
    content = f.read()

old_category_selector = """                            // Category Selector
                             Box(modifier = Modifier.weight(1f)) {
                                 OutlinedTextField(
                                     value = category,
                                     onValueChange = {},
                                     readOnly = true,
                                     label = { Text("Category") },
                                     modifier = Modifier
                                         .fillMaxWidth()
                                         .testTag("category_selector")
                                 )
                                 Box(
                                     modifier = Modifier
                                         .matchParentSize()
                                         .clickable { categoryDropdownExpanded = true }
                                 )
                                 DropdownMenu(
                                     expanded = categoryDropdownExpanded,
                                     onDismissRequest = { categoryDropdownExpanded = false }
                                 ) {
                                     categories.forEach { cat ->
                                         DropdownMenuItem(
                                             text = { Text(cat) },
                                             onClick = {
                                                 category = cat
                                                 categoryDropdownExpanded = false
                                             }
                                         )
                                     }
                                 }
                             }"""

new_category_selector = """                            // Category Selector
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
                            }"""

if old_category_selector in content:
    content = content.replace(old_category_selector, new_category_selector)
    with open("app/src/main/java/com/example/ui/screens/ManageExamPatternCutoffScreen.kt", "w") as f:
        f.write(content)
    print("Successfully patched ManageExamPatternCutoffScreen.kt")
else:
    print("Error: old_category_selector not found in file")
