with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "r") as f:
    vm = f.read()

old_block = """
                    if (_activeMockQuestions.value.any { it.isPremium }) {
                        _activeMockQuestions.value = emptyList()
                        _currentMockAttempt.value = null
                        _sessionMessage.value = "Premium Content is unavailable offline."
                        if (_currentScreen.value == Screen.MOCK_PLAYER || _currentScreen.value == Screen.MOCK_RESULT) {
                            navigateTo(Screen.HOME)
                        }
                    }
"""

new_block = """
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
"""

vm = vm.replace(old_block, new_block)

old_block2 = """
                if (_activeMockQuestions.value.any { q -> q.isPremium && !com.example.data.util.PlanValidityEngine.isQuestionAccessible(q, eff ?: com.example.data.util.PlanValidityEngine.resolveEffectiveEntitlement(userEntitlements.value, plans.value, getTrustedTime()), isAdminOrOwner.value) }) {
                     _activeMockQuestions.value = emptyList()
                     _currentMockAttempt.value = null
                     _sessionMessage.value = "Your Premium Entitlement has expired."
                     if (_currentScreen.value == Screen.MOCK_PLAYER || _currentScreen.value == Screen.MOCK_RESULT) {
                          navigateTo(Screen.HOME)
                     }
                }
"""

new_block2 = """
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
"""

vm = vm.replace(old_block2, new_block2)

with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "w") as f:
    f.write(vm)
