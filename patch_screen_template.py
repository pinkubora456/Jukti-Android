import re
path = "app/src/main/java/com/example/ui/screens/BatchImportQuestionScreen.kt"
with open(path, "r") as f:
    content = f.read()

# Replace CsvQuestionParser.getSampleCsvTemplate() with CsvQuestionParser.getSampleCsvTemplate(selectedContentType)
content = content.replace("CsvQuestionParser.getSampleCsvTemplate()", "CsvQuestionParser.getSampleCsvTemplate(selectedContentType)")

# Additionally, the "Format Guide" should show different instructions.
# Let's find: "Your CSV file can use either the full 19-column schema or the simplified 7-column schema:"
content = content.replace(
    'Text(\n                        text = "Your CSV file can use either the full 19-column schema or the simplified 7-column schema:",\n                        style = MaterialTheme.typography.bodySmall\n                    )',
    'Text(\n                        text = if (selectedContentType == "Reading Comprehension") "For Reading Comprehension, ensure you provide passageId. The passage text is only needed in the first row." else "Your CSV file can use either the full 19-column schema or the simplified 7-column schema:",\n                        style = MaterialTheme.typography.bodySmall\n                    )'
)

with open(path, "w") as f:
    f.write(content)

