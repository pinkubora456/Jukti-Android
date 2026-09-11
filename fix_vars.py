import re

with open('app/src/main/java/com/example/ui/screens/SingleQuestionUploadScreen.kt', 'r') as f:
    content = f.read()

# Fix the rawSubjects block
content = re.sub(
    r'if \(\!updated\.contains\("Transport Rule"\)\) \{\n\s*updated = \(updated \+ "Transport Rule"\)\.sorted\(\)\n\s*updated',
    'if (!updated.contains("Transport Rule")) {\n            updated = (updated + "Transport Rule").sorted()\n        }\n        updated',
    content
)

# Fix the rawChapters block
content = re.sub(
    r'if \(normCurrentSubject == "Transport Rule" && fromList\.isEmpty\(\)\) \{\n\s*listOf\(\n\s*"Traffic Signs, Signals & Road Safety",\n\s*"Motor Vehicles Act & Traffic Rules",\n\s*"Vehicle Safety, Violations & Penalties"\n\s*\)\n\s*fromList',
    'if (normCurrentSubject == "Transport Rule" && fromList.isEmpty()) {\n                listOf(\n                    "Traffic Signs, Signals & Road Safety",\n                    "Motor Vehicles Act & Traffic Rules",\n                    "Vehicle Safety, Violations & Penalties"\n                )\n            } else {\n                fromList\n            }',
    content
)

# Also fix the outer if
content = re.sub(
    r'if \(subject\.isBlank\(\)\) \{\n\s*emptyList\(\)',
    'if (subject.isBlank()) {\n            emptyList()\n        } else {',
    content
)
# Add the closing brace for the else block
content = re.sub(
    r'val chaptersList: List<String> = rawChapters',
    '        }\n    }\n    val chaptersList: List<String> = rawChapters',
    content
)


variables = """
    var subject by remember { mutableStateOf("") }
    var chapter by remember { mutableStateOf("") }
    var difficulty by remember { mutableStateOf("Medium") }
    var questionFor by remember { mutableStateOf("Free") }
    var questionTag by remember { mutableStateOf("Expected") }
    
    var questionEnglish by remember { mutableStateOf("") }
    var questionAssamese by remember { mutableStateOf("") }
    
    var optionAEnglish by remember { mutableStateOf("") }
    var optionBEnglish by remember { mutableStateOf("") }
    var optionCEnglish by remember { mutableStateOf("") }
    var optionDEnglish by remember { mutableStateOf("") }
    
    var optionAAssamese by remember { mutableStateOf("") }
    var optionBAssamese by remember { mutableStateOf("") }
    var optionCAssamese by remember { mutableStateOf("") }
    var optionDAssamese by remember { mutableStateOf("") }
    
    var correctOption by remember { mutableStateOf("A") }
    var explanationEnglish by remember { mutableStateOf("") }
    var explanationAssamese by remember { mutableStateOf("") }
    
    val selectedExams = remember { mutableStateListOf<String>() }
    var duplicateError by remember { mutableStateOf<String?>(null) }
    var isDeploying by remember { mutableStateOf(false) }

    var subjectExpanded by remember { mutableStateOf(false) }
    var chapterExpanded by remember { mutableStateOf(false) }
    var difficultyExpanded by remember { mutableStateOf(false) }
    var questionForExpanded by remember { mutableStateOf(false) }
    var questionTagExpanded by remember { mutableStateOf(false) }
    var correctOptionExpanded by remember { mutableStateOf(false) }
    var targetExamDialogVisible by remember { mutableStateOf(false) }
"""

# Insert variables after chaptersList
content = content.replace('val chaptersList: List<String> = rawChapters', 'val chaptersList: List<String> = rawChapters\n' + variables)

with open('app/src/main/java/com/example/ui/screens/SingleQuestionUploadScreen.kt', 'w') as f:
    f.write(content)
