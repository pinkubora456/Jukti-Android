sed -i '/val allSubjectsChapters/i \
    val activeSubjectChapterStats: Flow<List<SubjectChapterStat>> = questionDao.getSubjectChapterStats()\
' app/src/main/java/com/example/data/repository/JuktiRepository.kt
