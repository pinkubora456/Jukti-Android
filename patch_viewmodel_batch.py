import re
path = "app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt"
with open(path, "r") as f:
    content = f.read()

pattern = r"(fun batchImportQuestionsToQBank\([\s\S]*?questionsToInsert: List<QuestionEntity>,\n        onComplete: \(Int, String\) -> Unit\n    \) \{[\s\S]*?try \{)"

replacement = r"""fun batchImportQuestionsToQBank(
        questionsToInsert: List<QuestionEntity>,
        passagesToInsert: List<com.example.data.local.ReadingComprehensionPassageEntity> = emptyList(),
        onComplete: (Int, String) -> Unit
    ) {
        viewModelScope.launch {
            if (!isAdminOrOwner.value) {
                onComplete(0, "Unauthorized: Only Admin/Owner can batch import questions.")
                return@launch
            }
            if (questionsToInsert.isEmpty()) {
                onComplete(0, "No questions to import.")
                return@launch
            }
            try {
                if (passagesToInsert.isNotEmpty()) {
                    repository.bulkInsertPassages(passagesToInsert)
                }"""

if re.search(pattern, content):
    content = re.sub(pattern, replacement, content)
    print("Replaced in viewmodel!")
else:
    print("Pattern not found in viewmodel")

with open(path, "w") as f:
    f.write(content)
