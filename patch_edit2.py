import re

with open("app/src/main/java/com/example/ui/components/EditQuestionDialog.kt", "r") as f:
    content = f.read()

# Let's remove the garbage
content = re.sub(r'Row\(\s*modifier = Modifier\.fillMaxWidth\(\)\.horizontalScroll\(rememberScrollState\(\)\),\s*horizontalArrangement = Arrangement\.spacedBy\(8\.dp\)\s*\) \{\s*listOf\("A" to 0, "B" to 1, "C" to 2, "D" to 3\)\.forEach \{ \(label, idx\) ->\s*FilterChip\(\s*selected = correctIndex == idx,\s*onClick = \{ correctIndex = idx \},\s*label = \{ Text\("Option \$label"\) \}\s*\)\s*\}\s*\}\s*\/\/\s*horizontalArrangement = Arrangement\.SpaceBetween\s*\) \{\s*listOf\("A" to 0, "B" to 1, "C" to 2, "D" to 3\)\.forEach \{ \(label, idx\) ->\s*FilterChip\(\s*selected = correctIndex == idx,\s*onClick = \{ correctIndex = idx \},\s*label = \{ Text\("Option \$label"\) \}\s*\)\s*\}\s*\}', 
'''Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("A" to 0, "B" to 1, "C" to 2, "D" to 3).forEach { (label, idx) ->
                        FilterChip(
                            selected = correctIndex == idx,
                            onClick = { correctIndex = idx },
                            label = { Text("Option $label") }
                        )
                    }
                }''', content)

with open("app/src/main/java/com/example/ui/components/EditQuestionDialog.kt", "w") as f:
    f.write(content)

print("done")
