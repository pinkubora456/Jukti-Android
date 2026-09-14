import re
path = "app/src/main/java/com/example/ui/screens/BatchImportQuestionScreen.kt"
with open(path, "r") as f:
    content = f.read()

pattern = r"(onClick = \{\n                                        questionFor = \"Premium\"\n                                        runValidation\(isPrem = true\)\n                                        questionTypeExpanded = false\n                                    \}\n                                \)\n                            \}\n                        \})"

replacement = r"""\1

                        // Section: Content Type (Dropdown)
                        var contentTypeExpanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = contentTypeExpanded,
                            onExpandedChange = { contentTypeExpanded = !contentTypeExpanded }
                        ) {
                            SafeOutlinedTextField(
                                value = selectedContentType,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Content Type") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = contentTypeExpanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = contentTypeExpanded,
                                onDismissRequest = { contentTypeExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Normal MCQ") },
                                    onClick = {
                                        selectedContentType = "Normal MCQ"
                                        runValidation(contentType = "Normal MCQ")
                                        contentTypeExpanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Reading Comprehension") },
                                    onClick = {
                                        selectedContentType = "Reading Comprehension"
                                        runValidation(contentType = "Reading Comprehension")
                                        contentTypeExpanded = false
                                    }
                                )
                            }
                        }"""

if re.search(pattern, content):
    content = re.sub(pattern, replacement, content)
    print("Replaced successfully")
else:
    print("Not found")

with open(path, "w") as f:
    f.write(content)
