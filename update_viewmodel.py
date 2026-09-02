import re

def fix_file(filepath):
    with open(filepath, "r") as f:
        content = f.read()

    # Find the filter block
    old_block = """    private val _selectedSubject = MutableStateFlow("All Subjects")
    val selectedSubject: StateFlow<String> = _selectedSubject.asStateFlow()

    private val _selectedDifficulty = MutableStateFlow("All")"""

    new_block = """    private val _selectedSubject = MutableStateFlow("All Subjects")
    val selectedSubject: StateFlow<String> = _selectedSubject.asStateFlow()

    private val _selectedExam = MutableStateFlow("All Exams")
    val selectedExam: StateFlow<String> = _selectedExam.asStateFlow()

    private val _selectedChapter = MutableStateFlow("All Chapters")
    val selectedChapter: StateFlow<String> = _selectedChapter.asStateFlow()

    private val _selectedDifficulty = MutableStateFlow("All")"""

    if old_block in content:
        content = content.replace(old_block, new_block)
        
    old_funcs = """    fun setSubjectFilter(subject: String) {
        _selectedSubject.value = subject
    }
    fun setDifficultyFilter(difficulty: String) {"""

    new_funcs = """    fun setSubjectFilter(subject: String) {
        _selectedSubject.value = subject
    }
    fun setExamFilter(exam: String) {
        _selectedExam.value = exam
    }
    fun setChapterFilter(chapter: String) {
        _selectedChapter.value = chapter
    }
    fun setDifficultyFilter(difficulty: String) {"""
    
    if old_funcs in content:
        content = content.replace(old_funcs, new_funcs)
        
    with open(filepath, "w") as f:
        f.write(content)
    print("Fixed ViewModel")

fix_file("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt")
