with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "r") as f:
    vm = f.read()

vm = vm.replace(
    "                val googleAuthManager = GoogleAuthManager(getApplication())\n                googleAuthManager.signOut()",
    "                val googleAuthManager = GoogleAuthManager(getApplication())\n                googleAuthManager.signOut()\n                repository.clearPremiumCache()"
)

vm = vm.replace(
    "                if (currentProf.email.isNotBlank()) {\n                    UserSessionManager.unregisterSession(currentProf.email)\n                }",
    "                if (currentProf.email.isNotBlank()) {\n                    UserSessionManager.unregisterSession(currentProf.email)\n                }\n                repository.clearPremiumCache()"
)

with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "w") as f:
    f.write(vm)
