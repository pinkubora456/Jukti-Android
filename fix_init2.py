import re

with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "r") as f:
    vm = f.read()

bad_block = """    init {
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

vm = vm.replace(bad_block, "    init {")

with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "w") as f:
    f.write(vm)

