import re

with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "r") as f:
    content = f.read()

target = """    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val smartPracticeQuestions: StateFlow<List<QuestionEntity>> = userProfile.flatMapLatest { profile ->
        val uid = profile?.uid ?: FirebaseAuth.getInstance().currentUser?.uid
        if (uid.isNullOrBlank()) flowOf(emptyList())
        else repository.getSmartPracticeQuestions(uid)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())"""

replacement = """    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val smartPracticeQuestions: StateFlow<List<QuestionEntity>> = combine(
        userProfile.flatMapLatest { profile ->
            val uid = profile?.uid ?: FirebaseAuth.getInstance().currentUser?.uid
            if (uid.isNullOrBlank()) flowOf(emptyList())
            else repository.getSmartPracticeQuestions(uid)
        },
        effectiveEntitlement,
        isAdminOrOwner
    ) { questions, effective, isAdmin ->
        questions.filter { q ->
            com.example.data.util.PlanValidityEngine.isQuestionAccessible(q, effective, isAdmin)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())"""

if target in content:
    content = content.replace(target, replacement)
    print("Patched smartPracticeQuestions")
else:
    print("Target smartPracticeQuestions not found")

with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "w") as f:
    f.write(content)

