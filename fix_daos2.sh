sed -i '/abstract suspend fun deleteSubjectChapter/i \
    @Query("DELETE FROM subjects_chapters WHERE subject = :subject AND chapter = :chapter")\
    abstract suspend fun deleteSubjectChapterByNames(subject: String, chapter: String)\
' app/src/main/java/com/example/data/local/Daos.kt
