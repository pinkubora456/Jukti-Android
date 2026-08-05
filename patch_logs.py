import re

filepath = 'app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt'
with open(filepath, 'r', encoding='utf-8') as f:
    content = f.read()

# I will just add logActivity to the prominent ones like addQuestion, updateAboutConfig, etc.
replacements = {
    "fun addQuestion(question: QuestionEntity, onComplete: (Long) -> Unit) {\n        viewModelScope.launch {\n            val id = repository.insertQuestion(question)": 
    "fun addQuestion(question: QuestionEntity, onComplete: (Long) -> Unit) {\n        logActivity(\"Added new question: ${question.questionEn.take(30)}...\")\n        viewModelScope.launch {\n            val id = repository.insertQuestion(question)",
    
    "fun updateAboutConfig(config: AboutConfigEntity) {\n        viewModelScope.launch {\n            repository.updateAboutConfig(config)":
    "fun updateAboutConfig(config: AboutConfigEntity) {\n        logActivity(\"Updated About/Config settings\")\n        viewModelScope.launch {\n            repository.updateAboutConfig(config)",
    
    "fun addMockTest(mock: MockTestEntity, onComplete: () -> Unit = {}) {\n        viewModelScope.launch {\n            repository.insertMockTest(mock)":
    "fun addMockTest(mock: MockTestEntity, onComplete: () -> Unit = {}) {\n        logActivity(\"Added mock test: ${mock.titleEn}\")\n        viewModelScope.launch {\n            repository.insertMockTest(mock)",
    
    "fun updateMockTest(mock: MockTestEntity, onComplete: () -> Unit = {}) {\n        viewModelScope.launch {\n            repository.updateMockTest(mock)":
    "fun updateMockTest(mock: MockTestEntity, onComplete: () -> Unit = {}) {\n        logActivity(\"Updated mock test: ${mock.titleEn}\")\n        viewModelScope.launch {\n            repository.updateMockTest(mock)",

    "fun addStudyNote(note: StudyNoteEntity, onComplete: () -> Unit = {}) {\n        viewModelScope.launch {\n            repository.insertStudyNote(note)":
    "fun addStudyNote(note: StudyNoteEntity, onComplete: () -> Unit = {}) {\n        logActivity(\"Added study note: ${note.titleEn}\")\n        viewModelScope.launch {\n            repository.insertStudyNote(note)",
    
    "fun updateStudyNote(note: StudyNoteEntity, onComplete: () -> Unit = {}) {\n        viewModelScope.launch {\n            repository.updateStudyNote(note)":
    "fun updateStudyNote(note: StudyNoteEntity, onComplete: () -> Unit = {}) {\n        logActivity(\"Updated study note: ${note.titleEn}\")\n        viewModelScope.launch {\n            repository.updateStudyNote(note)",

    "fun addPlan(plan: PlanEntity, onComplete: () -> Unit) {\n        viewModelScope.launch {\n            repository.insertPlan(plan)":
    "fun addPlan(plan: PlanEntity, onComplete: () -> Unit) {\n        logActivity(\"Added premium plan: ${plan.planName}\")\n        viewModelScope.launch {\n            repository.insertPlan(plan)",

    "fun addExam(title: String, subtitle: String, status: String = \"Active\") {\n        viewModelScope.launch {\n            repository.insertExam(ExamEntity(title = title, subtitle = subtitle, status = status))":
    "fun addExam(title: String, subtitle: String, status: String = \"Active\") {\n        logActivity(\"Added exam: $title\")\n        viewModelScope.launch {\n            repository.insertExam(ExamEntity(title = title, subtitle = subtitle, status = status))",

    "fun addExamUpdate(update: ExamUpdateEntity) {\n        viewModelScope.launch {\n            repository.insertExamUpdate(update)":
    "fun addExamUpdate(update: ExamUpdateEntity) {\n        logActivity(\"Added exam update: ${update.titleEn}\")\n        viewModelScope.launch {\n            repository.insertExamUpdate(update)",
    
    "fun addBanner(banner: BannerEntity) {\n        viewModelScope.launch {\n            repository.insertBanner(banner)":
    "fun addBanner(banner: BannerEntity) {\n        logActivity(\"Added banner: ${banner.titleEn}\")\n        viewModelScope.launch {\n            repository.insertBanner(banner)",
    
    "fun deleteQuestion(question: QuestionEntity) {\n        viewModelScope.launch {\n            repository.deleteQuestion(question)":
    "fun deleteQuestion(question: QuestionEntity) {\n        logActivity(\"Deleted question ID: ${question.id}\")\n        viewModelScope.launch {\n            repository.deleteQuestion(question)"
}

for k, v in replacements.items():
    content = content.replace(k, v)
    
with open(filepath, 'w', encoding='utf-8') as f:
    f.write(content)

