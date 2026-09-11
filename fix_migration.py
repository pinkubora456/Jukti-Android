with open('app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt', 'r') as f:
    content = f.read()

migration_code = """
    init {
        migrateQuestionTags()
        fetchData()
"""

def_code = """
    private fun migrateQuestionTags() {
        viewModelScope.launch {
            val allQs = repository.getAllQuestionsForExport()
            val qsToUpdate = allQs.filter { 
                it.questionType != "PYQ" && it.questionType != "Expected"
            }.map {
                it.copy(questionType = if (it.questionType.startsWith("PYQ", ignoreCase = true)) "PYQ" else "Expected")
            }
            if (qsToUpdate.isNotEmpty()) {
                repository.bulkEditQuestions(qsToUpdate, null, null, null, null, null)
            }
        }
    }
"""

if "migrateQuestionTags()" not in content:
    content = content.replace("init {\n        fetchData()", migration_code)
    # Add function before bulkEditQuestions
    content = content.replace("fun bulkEditQuestions(", def_code + "\n    fun bulkEditQuestions(")
    
with open('app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt', 'w') as f:
    f.write(content)
