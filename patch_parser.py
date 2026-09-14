import re
path = "app/src/main/java/com/example/util/CsvQuestionParser.kt"
with open(path, "r") as f:
    content = f.read()

# Add passages to BatchValidationResult
pattern_result = r"(data class BatchValidationResult\([\s\S]*?val duplicateInBatchRows: List<ParsedQuestionRow>)"
replacement_result = r"\1,\n    val passages: List<com.example.data.local.ReadingComprehensionPassageEntity> = emptyList()"
if re.search(pattern_result, content):
    content = re.sub(pattern_result, replacement_result, content)

with open(path, "w") as f:
    f.write(content)
print("Patched BatchValidationResult")
