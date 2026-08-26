import re

with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "r") as f:
    text = f.read()

mockTests_decl = """    val mockTests: StateFlow<List<MockTestEntity>> = combine(
        repository.allMockTests, effectiveEntitlement, isAdminOrOwner, networkMonitor.isConnected
    ) { list, effective, isAdmin, isConnected ->
        val eff = effective ?: com.example.data.util.PlanValidityEngine.resolveEffectiveEntitlement(userEntitlements.value, plans.value, getTrustedTime())
        list.map { m ->
            if (!m.isPremium || (isConnected && com.example.data.util.PlanValidityEngine.isMockTestAccessible(m, eff, isAdmin))) m
            else m.copy(
                titleEn = "Premium Content 🔒", titleAs = "প্ৰিমিয়াম সমল 🔒"
            )
        }
    }.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )"""

text = text.replace(mockTests_decl + "\n\n", "")

accessibleMockTests_decl = """    val accessibleMockTests: StateFlow<List<MockTestEntity>>"""
text = text.replace(accessibleMockTests_decl, mockTests_decl + "\n\n" + accessibleMockTests_decl)

with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "w") as f:
    f.write(text)

