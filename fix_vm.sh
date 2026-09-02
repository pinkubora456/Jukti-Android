sed -i '/val allSubjectsChapters/i \
    val activeSubjectChapterStats = repository.activeSubjectChapterStats.stateIn(\
        scope = viewModelScope,\
        started = SharingStarted.WhileSubscribed(5000),\
        initialValue = emptyList()\
    )\
' app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt

sed -i '/fun deleteSubjectChapter/i \
    fun mergeChapter(subject: String, sourceChapter: String, targetChapter: String) {\
        viewModelScope.launch {\
            repository.mergeChapter(subject, sourceChapter, targetChapter)\
        }\
    }\
\
    fun renameSubject(oldSubject: String, newSubject: String) {\
        viewModelScope.launch {\
            repository.renameSubject(oldSubject, newSubject)\
        }\
    }\
\
    fun renameChapter(subject: String, oldChapter: String, newChapter: String) {\
        viewModelScope.launch {\
            repository.renameChapter(subject, oldChapter, newChapter)\
        }\
    }\
' app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt
