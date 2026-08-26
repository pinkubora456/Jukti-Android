import re

with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "r") as f:
    content = f.read()

target = """    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
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
    content = content.replace(target, "")
else:
    print("Could not find smartPracticeQuestions")

target_dest = """    val accessibleContentCounts: StateFlow<com.example.data.util.PlanAccessibleContentCounts> = combine("""

replacement_dest = target + "\n\n" + target_dest

if target_dest in content:
    content = content.replace(target_dest, replacement_dest)
    with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "w") as f:
        f.write(content)
    print("Moved smartPracticeQuestions")
else:
    print("Could not find accessibleContentCounts")

