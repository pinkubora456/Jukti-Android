with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "r") as f:
    vm = f.read()

import_statement = "import com.example.util.NetworkMonitor\n"
if import_statement not in vm:
    vm = vm.replace("import android.app.Application", import_statement + "import android.app.Application")

init_block = """
    private val networkMonitor = NetworkMonitor(application)
    val isConnected = networkMonitor.isConnected

    init {
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
vm = vm.replace("    init {", init_block)

with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "w") as f:
    f.write(vm)
