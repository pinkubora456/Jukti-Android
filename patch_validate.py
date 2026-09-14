import re
path = "app/src/main/java/com/example/util/CsvQuestionParser.kt"
with open(path, "r") as f:
    content = f.read()

# Add contentType param
pattern_sig = r"(fun validateAndParseQuestions\([\s\S]*?existingQuestions: List<QuestionEntity> = emptyList\(\))"
replacement_sig = r"\1,\n        contentType: String = \"normal\""
if re.search(pattern_sig, content):
    content = re.sub(pattern_sig, replacement_sig, content)

with open(path, "w") as f:
    f.write(content)
print("Added contentType param")
