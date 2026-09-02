sed -i '/abstract suspend fun deleteSubjectChapterByNames/i \
    @Query("UPDATE subjects_chapters SET subject = :newSubject WHERE subject = :oldSubject")\
    abstract suspend fun renameSubject(oldSubject: String, newSubject: String)\
\
    @Query("UPDATE subjects_chapters SET chapter = :newChapter WHERE subject = :subject AND chapter = :oldChapter")\
    abstract suspend fun renameChapter(subject: String, oldChapter: String, newChapter: String)\
' app/src/main/java/com/example/data/local/Daos.kt
