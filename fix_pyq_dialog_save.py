import re

with open('app/src/main/java/com/example/ui/screens/ManageGuidanceScreen.kt', 'r') as f:
    content = f.read()

old_save_logic = """                    onClick = {
                        if (addSubject.isNotBlank() && addChapter.isNotBlank()) {
                            val pyqCount = addPyqCountStr.toIntOrNull() ?: 0
                            val examsCovered = addExamsCoveredStr.toIntOrNull() ?: 1
                            onSavePyq(
                                PyqFocusEntity(
                                    exam = selectedExam,
                                    subject = addSubject.trim(),
                                    chapter = addChapter.trim(),
                                    pyqCount = pyqCount,
                                    examsCovered = examsCovered,
                                    updatedAt = System.currentTimeMillis()
                                )
                            )
                            showAddDialog = false
                        }
                    }"""

new_save_logic = """                    onClick = {
                        val trimmedSubj = addSubject.trim()
                        val trimmedChap = addChapter.trim()
                        
                        if (trimmedSubj.isBlank() || trimmedChap.isBlank()) {
                            Toast.makeText(context, "Subject and Chapter are required", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        
                        if (!chaptersForSubj.contains(trimmedChap)) {
                            Toast.makeText(context, "Chapter does not belong to selected Subject", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        
                        val isDuplicate = allPyqFocus.any { 
                            it.exam.equals(selectedExam, ignoreCase = true) && 
                            it.subject.equals(trimmedSubj, ignoreCase = true) && 
                            it.chapter.equals(trimmedChap, ignoreCase = true) 
                        }
                        
                        if (isDuplicate) {
                            Toast.makeText(context, "PYQ Focus for this Chapter already exists", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        val pyqCount = addPyqCountStr.toIntOrNull() ?: 0
                        val examsCovered = addExamsCoveredStr.toIntOrNull() ?: 1
                        onSavePyq(
                            PyqFocusEntity(
                                exam = selectedExam,
                                subject = trimmedSubj,
                                chapter = trimmedChap,
                                pyqCount = pyqCount,
                                examsCovered = examsCovered,
                                updatedAt = System.currentTimeMillis()
                            )
                        )
                        showAddDialog = false
                    }"""

content = content.replace(old_save_logic, new_save_logic)

with open('app/src/main/java/com/example/ui/screens/ManageGuidanceScreen.kt', 'w') as f:
    f.write(content)
