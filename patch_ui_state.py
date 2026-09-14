import re
path = "app/src/main/java/com/example/ui/screens/BatchImportQuestionScreen.kt"
with open(path, "r") as f:
    content = f.read()

# 1. Add selectedContentType
state_pattern = r"(var questionFor by remember \{ mutableStateOf\(\"Premium\"\) \} // Free or Premium)"
state_repl = r"\1\n    var selectedContentType by remember { mutableStateOf(\"Normal MCQ\") }"
content = re.sub(state_pattern, state_repl, content)

# 2. Update runValidation signature and validateAndParseQuestions call
val_pattern = r"(fun runValidation\([\s\S]*?isPrem: Boolean = questionFor\.equals\(\"Premium\", ignoreCase = true\)\n    \) \{[\s\S]*?CsvQuestionParser\.validateAndParseQuestions\([\s\S]*?existingQuestions = allExistingQuestions)"
val_repl = r"fun runValidation(\n        text: String = csvInputText,\n        exams: String = selectedExams.joinToString(\", \"),\n        isPrem: Boolean = questionFor.equals(\"Premium\", ignoreCase = true),\n        contentType: String = selectedContentType\n    ) {\n        if (text.isNotBlank()) {\n            isValidating = true\n            coroutineScope.launch {\n                val result = withContext(Dispatchers.Default) {\n                    CsvQuestionParser.validateAndParseQuestions(\n                        csvText = text,\n                        defaultSubject = \"General Studies\",\n                        defaultChapter = \"General\",\n                        defaultExamCategory = exams,\n                        isPremium = isPrem,\n                        existingQuestions = allExistingQuestions,\n                        contentType = contentType"
if re.search(r"fun runValidation\([\s\S]*?CsvQuestionParser\.validateAndParseQuestions\([\s\S]*?existingQuestions = allExistingQuestions", content):
    content = re.sub(r"fun runValidation\([\s\S]*?CsvQuestionParser\.validateAndParseQuestions\([\s\S]*?existingQuestions = allExistingQuestions", val_repl, content)

with open(path, "w") as f:
    f.write(content)
