sed -i '/questionDao.renameSubject(oldSubject, newSubject)/a \
            subjectChapterDao.renameSubject(oldSubject, newSubject)\
' app/src/main/java/com/example/data/repository/JuktiRepository.kt

sed -i '/questionDao.renameChapter(subject, oldChapter, newChapter)/a \
            subjectChapterDao.renameChapter(subject, oldChapter, newChapter)\
' app/src/main/java/com/example/data/repository/JuktiRepository.kt
