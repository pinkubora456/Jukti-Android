with open('app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt', 'r') as f:
    content = f.read()

import re

# Update setSubjectFilter
replacement = """    fun setSubjectFilter(subject: String) {
        _selectedSubject.value = subject
        if (subject == "All Subjects") {
            _selectedOverviewSubjects.value = setOf("All Subjects")
        } else {
            _selectedOverviewSubjects.value = setOf(subject)
        }
    }"""
content = re.sub(r'    fun setSubjectFilter\(subject: String\) \{\n        _selectedSubject\.value = subject\n    \}', replacement, content)

with open('app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt', 'w') as f:
    f.write(content)
