import re

def fix():
    with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "r") as f:
        content = f.read()
        
    old = """    fun bulkMoveQuestions(
        questionIds: List<Long>,
        targetExam: String,
        targetSubject: String,
        targetChapter: String,
        onComplete: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val (success, msg) = repository.bulkMoveQuestions(questionIds, targetExam, targetSubject, targetChapter)"""
            
    new = """    fun bulkMoveQuestions(
        questionsToUpdate: List<QuestionEntity>,
        targetExam: String,
        targetSubject: String,
        targetChapter: String,
        onComplete: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val (success, msg) = repository.bulkMoveQuestions(questionsToUpdate, targetExam, targetSubject, targetChapter)"""
            
    if old in content:
        content = content.replace(old, new)
        with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "w") as f:
            f.write(content)
        print("Fixed VM")
    else:
        print("Old not found in VM")

fix()
