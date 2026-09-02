import re

# Fix JuktiViewModel.kt
with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "r") as f:
    vm_content = f.read()

old_vm = """    private val _selectedSubject = MutableStateFlow("All")
    val selectedSubject: StateFlow<String> = _selectedSubject.asStateFlow()

    private val _selectedDifficulty = MutableStateFlow("All")"""
new_vm = """    private val _selectedSubject = MutableStateFlow("All Subjects")
    val selectedSubject: StateFlow<String> = _selectedSubject.asStateFlow()

    private val _selectedExam = MutableStateFlow("All Exams")
    val selectedExam: StateFlow<String> = _selectedExam.asStateFlow()

    private val _selectedChapter = MutableStateFlow("All Chapters")
    val selectedChapter: StateFlow<String> = _selectedChapter.asStateFlow()

    private val _selectedDifficulty = MutableStateFlow("All")"""

if old_vm in vm_content:
    vm_content = vm_content.replace(old_vm, new_vm)
    with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "w") as f:
        f.write(vm_content)
    print("Fixed JuktiViewModel.kt")
else:
    print("Could not find old_vm in JuktiViewModel.kt")

# Fix AllQuestionsScreen.kt
with open("app/src/main/java/com/example/ui/screens/AllQuestionsScreen.kt", "r") as f:
    allq_content = f.read()

old_text = 'Text("You are about to move $selectedCount questions to:\n\nExam: $destExam\nSubject: $destSubj\nChapter/Topic: $destChap\n\nAre you sure?")'
old_text_broken = 'Text("You are about to move $selectedCount questions to:\nExam: $destExam\nSubject: $destSubj\nChapter/Topic: $destChap\nAre you sure?")'
old_text_broken2 = 'Text("You are about to move $selectedCount questions to:\n\nExam: $destExam\nSubject: $destSubj\nChapter/Topic: $destChap\n\nAre you sure?")'.replace('\\n', '\n')

new_text = 'Text("You are about to move $selectedCount questions to:\\n\\nExam: $destExam\\nSubject: $destSubj\\nChapter/Topic: $destChap\\n\\nAre you sure?")'

allq_content = allq_content.replace(old_text_broken2, new_text)
with open("app/src/main/java/com/example/ui/screens/AllQuestionsScreen.kt", "w") as f:
    f.write(allq_content)
print("Fixed AllQuestionsScreen.kt string")

# Fix ContentQuestionsOverviewScreen.kt line 100 which says:
# @Composable invocations can only happen from the context of a @Composable function
# Looking at ContentQuestionsOverviewScreen.kt, let's see what is on line 100.
