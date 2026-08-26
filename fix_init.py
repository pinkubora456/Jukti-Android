with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "r") as f:
    vm = f.read()

import re

# We want to replace the inserted block with just "    init {" for all but the first occurrence.
# Let's find the exact block to replace.

block_to_replace = """    private val networkMonitor = NetworkMonitor(application)
    val isConnected = networkMonitor.isConnected

    init {
        viewModelScope.launch {
            networkMonitor.isConnected.collect { online ->
                if (!online) {
                    repository.clearPremiumCache()
                    
                    if (_activeMockQuestions.value.any { it.isPremium }) {
                        _activeMockQuestions.value = emptyList()
                        _currentMockAttempt.value = null
                        _sessionMessage.value = "Premium Content is unavailable offline."
                        if (_currentScreen.value == Screen.MOCK_PLAYER || _currentScreen.value == Screen.MOCK_RESULT) {
                            navigateTo(Screen.HOME)
                        }
                    }
                    if (_selectedStudyNote.value?.isPremium == true) {
                        _selectedStudyNote.value = null
                        _sessionMessage.value = "Premium Content is unavailable offline."
                        if (_currentScreen.value == Screen.STUDY_NOTES) {
                            navigateTo(Screen.HOME)
                        }
                    }
                }
            }
        }
        viewModelScope.launch {
            effectiveEntitlement.collect { eff ->
                if (eff == null && userProfile.value?.isLoggedIn == true) {
                    // if entitlement becomes null but logged in?
                    // well eff isn't null if we just lose a plan, it'll just have different access.
                }
                // If they are on a premium screen and lose entitlement:
                val currentEff = eff ?: com.example.data.util.PlanValidityEngine.resolveEffectiveEntitlement(userEntitlements.value, plans.value, getTrustedTime())
                val isAdmin = isAdminOrOwner.value
                if (_activeMockQuestions.value.any { q -> q.isPremium && !com.example.data.util.PlanValidityEngine.isQuestionAccessible(q, currentEff, isAdmin) }) {
                     _activeMockQuestions.value = emptyList()
                     _currentMockAttempt.value = null
                     _sessionMessage.value = "Your Premium Entitlement has expired."
                     if (_currentScreen.value == Screen.MOCK_PLAYER || _currentScreen.value == Screen.MOCK_RESULT) {
                          navigateTo(Screen.HOME)
                     }
                }
                if (_selectedStudyNote.value?.isPremium == true && !com.example.data.util.PlanValidityEngine.isStudyNoteAccessible(_selectedStudyNote.value!!, currentEff, isAdmin)) {
                     _selectedStudyNote.value = null
                     _sessionMessage.value = "Your Premium Entitlement has expired."
                     if (_currentScreen.value == Screen.STUDY_NOTES) {
                          navigateTo(Screen.HOME)
                     }
                }
            }
        }"""

parts = vm.split(block_to_replace)

if len(parts) > 1:
    new_vm = parts[0] + block_to_replace
    for i in range(1, len(parts) - 1):
        new_vm += parts[i] + "    init {"
    new_vm += parts[-1]
    
    with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "w") as f:
        f.write(new_vm)
        print("Fixed duplicates")
else:
    print("Block not found exactly as specified.")

