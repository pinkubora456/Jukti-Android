with open('app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt', 'r') as f:
    content = f.read()

import re

# Add state for multiple subjects
add_state = """    private val _selectedOverviewSubjects = MutableStateFlow<Set<String>>(setOf("All Subjects"))
    val selectedOverviewSubjects: StateFlow<Set<String>> = _selectedOverviewSubjects.asStateFlow()

    fun toggleOverviewSubject(subject: String, isChecked: Boolean) {
        val current = _selectedOverviewSubjects.value.toMutableSet()
        if (subject == "All Subjects") {
            if (isChecked) current.clear(); current.add("All Subjects")
        } else {
            current.remove("All Subjects")
            if (isChecked) current.add(subject) else current.remove(subject)
            if (current.isEmpty()) current.add("All Subjects")
        }
        _selectedOverviewSubjects.value = current
    }

"""

if "_selectedOverviewSubjects" not in content:
    content = content.replace('private val _selectedSubject = MutableStateFlow("All Subjects")', add_state + '    private val _selectedSubject = MutableStateFlow("All Subjects")')

with open('app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt', 'w') as f:
    f.write(content)
