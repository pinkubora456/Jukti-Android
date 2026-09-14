import re
path = "app/src/main/java/com/example/ui/screens/BatchImportQuestionScreen.kt"
with open(path, "r") as f:
    content = f.read()

pattern = r"(                                isImporting = true\n                                viewModel\.batchImportQuestionsToQBank\(questionsToImport\) \{ importedCount, message ->)"

replacement = r"""                                isImporting = true
                                val passagesToImport = validationResult?.passages?.filter { p ->
                                    questionsToImport.any { q -> q.passageId == p.id }
                                } ?: emptyList()
                                viewModel.batchImportQuestionsToQBank(questionsToImport, passagesToImport) { importedCount, message ->"""

if re.search(pattern, content):
    content = re.sub(pattern, replacement, content)
    print("Replaced UI call")
else:
    print("Pattern not found in UI call")

with open(path, "w") as f:
    f.write(content)
