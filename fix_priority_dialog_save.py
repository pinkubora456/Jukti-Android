import re

with open('app/src/main/java/com/example/ui/screens/ManageGuidanceScreen.kt', 'r') as f:
    content = f.read()

old_save_logic = """                    onClick = {
                        if (addSubject.isNotBlank() && addChapter.isNotBlank() && addTopic.isNotBlank()) {
                            onSaveFocusTopic(
                                FocusTopicEntity(
                                    exam = selectedExam,
                                    subject = addSubject.trim(),
                                    chapter = addChapter.trim(),
                                    topic = addTopic.trim(),
                                    priority = addPriority.trim().replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.ROOT) else it.toString() },
                                    instruction = addInstruction.trim(),
                                    updatedAt = System.currentTimeMillis()
                                )
                            )
                            showAddDialog = false
                        }
                    }"""

new_save_logic = """                    onClick = {
                        val trimmedSubj = addSubject.trim()
                        val trimmedChap = addChapter.trim()
                        val trimmedTopic = addTopic.trim()
                        
                        if (trimmedSubj.isBlank() || trimmedChap.isBlank() || trimmedTopic.isBlank()) {
                            Toast.makeText(context, "Subject, Chapter, and Topic are required", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        
                        if (!chaptersForSubj.contains(trimmedChap)) {
                            Toast.makeText(context, "Chapter does not belong to selected Subject", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        
                        val isDuplicate = allFocusTopics.any { 
                            it.exam.equals(selectedExam, ignoreCase = true) && 
                            it.subject.equals(trimmedSubj, ignoreCase = true) && 
                            it.chapter.equals(trimmedChap, ignoreCase = true) &&
                            it.topic.equals(trimmedTopic, ignoreCase = true)
                        }
                        
                        if (isDuplicate) {
                            Toast.makeText(context, "Priority Topic already exists", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        onSaveFocusTopic(
                            FocusTopicEntity(
                                exam = selectedExam,
                                subject = trimmedSubj,
                                chapter = trimmedChap,
                                topic = trimmedTopic,
                                priority = addPriority.trim().replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.ROOT) else it.toString() },
                                instruction = addInstruction.trim(),
                                updatedAt = System.currentTimeMillis()
                            )
                        )
                        showAddDialog = false
                    }"""

content = content.replace(old_save_logic, new_save_logic)

with open('app/src/main/java/com/example/ui/screens/ManageGuidanceScreen.kt', 'w') as f:
    f.write(content)
