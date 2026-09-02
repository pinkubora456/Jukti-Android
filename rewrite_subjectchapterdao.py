import re

with open("app/src/main/java/com/example/data/local/Daos.kt", "r") as f:
    content = f.read()

correct_dao = """interface SubjectChapterDao {
    @Query("SELECT * FROM subjects_chapters ORDER BY subject ASC, chapter ASC")
    abstract fun getAllSubjectsChapters(): Flow<List<SubjectChapterEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertSubjectChapter(subjectChapter: SubjectChapterEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertAll(subjectsChapters: List<SubjectChapterEntity>)

    @Update
    abstract suspend fun updateSubjectChapter(subjectChapter: SubjectChapterEntity)

    @Query("UPDATE subjects_chapters SET subject = :newSubject WHERE subject = :oldSubject")
    abstract suspend fun renameSubject(oldSubject: String, newSubject: String)

    @Query("UPDATE subjects_chapters SET chapter = :newChapter WHERE subject = :subject AND chapter = :oldChapter")
    abstract suspend fun renameChapter(subject: String, oldChapter: String, newChapter: String)

    @Query("DELETE FROM subjects_chapters WHERE subject = :subject AND chapter = :chapter")
    abstract suspend fun deleteSubjectChapterByNames(subject: String, chapter: String)

    @Delete
    abstract suspend fun deleteSubjectChapter(subjectChapter: SubjectChapterEntity)

    @Query("DELETE FROM subjects_chapters")
    abstract suspend fun deleteAll()
}"""

content = re.sub(r'interface SubjectChapterDao \{.*?\n\}', correct_dao, content, flags=re.DOTALL)

with open("app/src/main/java/com/example/data/local/Daos.kt", "w") as f:
    f.write(content)
