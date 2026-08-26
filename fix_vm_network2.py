with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "r") as f:
    vm = f.read()

# Replace the old block we just inserted
old_block = """
        viewModelScope.launch {
            networkMonitor.isConnected.collect { online ->
                if (!online) {
                    repository.clearPremiumCache()
                    val currentAttempt = _currentMockAttempt.value
                    if (currentAttempt != null) {
                        val activeMockId = currentAttempt.mockTestId
                        val isStillAccessible = mockTests.value.any { it.id == activeMockId }
                        if (!isStillAccessible) {
                            _activeMockQuestions.value = emptyList()
                            _currentMockAttempt.value = null
                            _sessionMessage.value = "Your Premium Mock Test was ended because internet connection was lost."
                            navigateTo(Screen.HOME)
                        }
                    }
                }
            }
        }
"""

new_block = """
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
                }
            }
        }
"""
if old_block in vm:
    vm = vm.replace(old_block, new_block)

# Also check effectiveEntitlement changes!
entitlement_block = """
        viewModelScope.launch {
            effectiveEntitlement.collect { eff ->
                if (eff == null && userProfile.value?.isLoggedIn == true) {
                    // if entitlement becomes null but logged in?
                    // well eff isn't null if we just lose a plan, it'll just have different access.
                }
                // If they are on a premium screen and lose entitlement:
                if (_activeMockQuestions.value.any { q -> q.isPremium && !com.example.data.util.PlanValidityEngine.isQuestionAccessible(q, eff ?: com.example.data.util.PlanValidityEngine.resolveEffectiveEntitlement(userEntitlements.value, plans.value, getTrustedTime()), isAdminOrOwner.value) }) {
                     _activeMockQuestions.value = emptyList()
                     _currentMockAttempt.value = null
                     _sessionMessage.value = "Your Premium Entitlement has expired."
                     if (_currentScreen.value == Screen.MOCK_PLAYER || _currentScreen.value == Screen.MOCK_RESULT) {
                          navigateTo(Screen.HOME)
                     }
                }
            }
        }
"""
# insert right after network monitor launch
vm = vm.replace(new_block, new_block + entitlement_block)

with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "w") as f:
    f.write(vm)
