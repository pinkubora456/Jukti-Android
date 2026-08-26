import re

with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "r") as f:
    vm = f.read()

old_block = """    val smartPracticeQuestions: StateFlow<List<QuestionEntity>> = combine(
        userProfile.flatMapLatest { profile ->
            val uid = profile?.uid ?: FirebaseAuth.getInstance().currentUser?.uid
            if (uid.isNullOrBlank()) flowOf(emptyList())
            else repository.getSmartPracticeQuestions(uid)
        },
        effectiveEntitlement,
        isAdminOrOwner
    ) { questions, effective, isAdmin ->
        val eff = effective ?: com.example.data.util.PlanValidityEngine.resolveEffectiveEntitlement(userEntitlements.value, plans.value, getTrustedTime())
        questions.filter { q ->
            com.example.data.util.PlanValidityEngine.isQuestionAccessible(q, eff, isAdmin)
        }
    }"""

new_block = """    val smartPracticeQuestions: StateFlow<List<QuestionEntity>> = combine(
        userProfile.flatMapLatest { profile ->
            val uid = profile?.uid ?: FirebaseAuth.getInstance().currentUser?.uid
            if (uid.isNullOrBlank()) flowOf(emptyList())
            else repository.getSmartPracticeQuestions(uid)
        },
        effectiveEntitlement,
        isAdminOrOwner,
        networkMonitor.isConnected
    ) { questions, effective, isAdmin, isConnected ->
        val eff = effective ?: com.example.data.util.PlanValidityEngine.resolveEffectiveEntitlement(userEntitlements.value, plans.value, getTrustedTime())
        questions.filter { q ->
            !q.isPremium || (isConnected && com.example.data.util.PlanValidityEngine.isQuestionAccessible(q, eff, isAdmin))
        }
    }"""

vm = vm.replace(old_block, new_block)

with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "w") as f:
    f.write(vm)

