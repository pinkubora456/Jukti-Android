sed -i '/abstract suspend fun deleteQuestionById(id: Long)/a \
\
    @Query("""\
        SELECT subject, topic as chapter, COUNT(*) as questionCount \
        FROM questions \
        GROUP BY subject, topic\
    """)\
    abstract fun getSubjectChapterStats(): Flow<List<SubjectChapterStat>>\
\
    @Query("""\
        UPDATE questions \
        SET topic = :newChapter \
        WHERE subject = :subject AND topic = :oldChapter\
    """)\
    abstract suspend fun mergeChapter(subject: String, oldChapter: String, newChapter: String)\
\
    @Query("""\
        UPDATE questions\
        SET subject = :newSubject\
        WHERE subject = :oldSubject\
    """)\
    abstract suspend fun renameSubject(oldSubject: String, newSubject: String)\
\
    @Query("""\
        UPDATE questions\
        SET topic = :newChapter\
        WHERE subject = :subject AND topic = :oldChapter\
    """)\
    abstract suspend fun renameChapter(subject: String, oldChapter: String, newChapter: String)\
' app/src/main/java/com/example/data/local/Daos.kt
