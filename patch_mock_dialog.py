import re
path = "app/src/main/java/com/example/ui/components/BatchImportMockQuestionsDialog.kt"
with open(path, "r") as f:
    content = f.read()

content = re.sub(
    r"(CsvQuestionParser\.validateAndParseQuestions\([\s\S]*?existingQuestions = allExistingQuestions)",
    r"\1,\n                                contentType = \"Normal MCQ\"",
    content
)

with open(path, "w") as f:
    f.write(content)
