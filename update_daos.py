import re

with open("app/src/main/java/com/example/data/local/Daos.kt", "r") as f:
    content = f.read()

# Add deletePremium to QuestionDao
q_target = "    @Query(\"SELECT * FROM questions ORDER BY id DESC\")"
if q_target in content:
    content = content.replace(q_target, "    @Query(\"DELETE FROM questions WHERE isPremium = 1\")\n    suspend fun deletePremiumQuestions()\n\n" + q_target)

# Add deletePremium to MockTestDao
m_target = "    @Query(\"SELECT * FROM mock_tests ORDER BY createdAt DESC\")"
if m_target in content:
    content = content.replace(m_target, "    @Query(\"DELETE FROM mock_tests WHERE isPremium = 1\")\n    suspend fun deletePremiumMockTests()\n\n" + m_target)

# Add deletePremium to StudyNoteDao
s_target = "    @Query(\"SELECT * FROM study_notes ORDER BY createdAt DESC\")"
if s_target in content:
    content = content.replace(s_target, "    @Query(\"DELETE FROM study_notes WHERE isPremium = 1\")\n    suspend fun deletePremiumStudyNotes()\n\n" + s_target)

# Add deletePremium to ExamUpdateDao
e_target = "    @Query(\"SELECT * FROM exam_updates ORDER BY date DESC\")"
if e_target in content:
    content = content.replace(e_target, "    @Query(\"DELETE FROM exam_updates WHERE isPremium = 1\")\n    suspend fun deletePremiumExamUpdates()\n\n" + e_target)

with open("app/src/main/java/com/example/data/local/Daos.kt", "w") as f:
    f.write(content)
print("Updated Daos")
