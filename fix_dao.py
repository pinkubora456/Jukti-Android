import re

with open("app/src/main/java/com/example/data/local/Daos.kt", "r") as f:
    daos = f.read()

old_insertQ = """    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestion(question: QuestionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(questions: List<QuestionEntity>)"""

new_insertQ = """    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestionInternal(question: QuestionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllInternal(questions: List<QuestionEntity>)
    
    @Transaction
    suspend fun insertQuestion(question: QuestionEntity): Long {
        if (question.isPremium) return -1L // Prevent inserting Premium questions into Room
        return insertQuestionInternal(question)
    }

    @Transaction
    suspend fun insertAll(questions: List<QuestionEntity>) {
        val freeQuestions = questions.filter { !it.isPremium }
        if (freeQuestions.isNotEmpty()) {
            insertAllInternal(freeQuestions)
        }
    }"""

daos = daos.replace(old_insertQ, new_insertQ)

old_insertM = """    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMockTest(test: MockTestEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tests: List<MockTestEntity>)"""

new_insertM = """    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMockTestInternal(test: MockTestEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllMockTestsInternal(tests: List<MockTestEntity>)

    @Transaction
    suspend fun insertMockTest(test: MockTestEntity): Long {
        if (test.isPremium) return -1L
        return insertMockTestInternal(test)
    }

    @Transaction
    suspend fun insertAll(tests: List<MockTestEntity>) {
        val freeTests = tests.filter { !it.isPremium }
        if (freeTests.isNotEmpty()) {
            insertAllMockTestsInternal(freeTests)
        }
    }"""

daos = daos.replace(old_insertM, new_insertM)

old_insertN = """    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: StudyNoteEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(notes: List<StudyNoteEntity>)"""

new_insertN = """    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNoteInternal(note: StudyNoteEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllNotesInternal(notes: List<StudyNoteEntity>)

    @Transaction
    suspend fun insertNote(note: StudyNoteEntity): Long {
        if (note.isPremium) return -1L
        return insertNoteInternal(note)
    }

    @Transaction
    suspend fun insertAll(notes: List<StudyNoteEntity>) {
        val freeNotes = notes.filter { !it.isPremium }
        if (freeNotes.isNotEmpty()) {
            insertAllNotesInternal(freeNotes)
        }
    }"""

daos = daos.replace(old_insertN, new_insertN)

with open("app/src/main/java/com/example/data/local/Daos.kt", "w") as f:
    f.write(daos)

