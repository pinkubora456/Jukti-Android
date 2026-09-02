sed -i '/suspend fun deleteSubjectChapter/i \
    suspend fun mergeChapter(subject: String, sourceChapter: String, targetChapter: String) {\
        withContext(Dispatchers.IO) {\
            questionDao.mergeChapter(subject, sourceChapter, targetChapter)\
        }\
    }\
\
    suspend fun renameSubject(oldSubject: String, newSubject: String) {\
        withContext(Dispatchers.IO) {\
            questionDao.renameSubject(oldSubject, newSubject)\
        }\
    }\
\
    suspend fun renameChapter(subject: String, oldChapter: String, newChapter: String) {\
        withContext(Dispatchers.IO) {\
            questionDao.renameChapter(subject, oldChapter, newChapter)\
        }\
    }\
' app/src/main/java/com/example/data/repository/JuktiRepository.kt
