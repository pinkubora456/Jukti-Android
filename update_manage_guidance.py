import re

with open("app/src/main/java/com/example/ui/screens/ManageGuidanceScreen.kt", "r") as f:
    content = f.read()

migration_btn = """
                Button(
                    onClick = {
                        viewModel.migrateGuidanceToFirestore()
                    },
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                ) {
                    Text("Migrate Local Guidance to Firestore")
                }
"""

if "migrateGuidanceToFirestore" not in content:
    target = "Text(\"Manage Guidance\", style = MaterialTheme.typography.titleLarge)"
    content = content.replace(target, target + migration_btn)

with open("app/src/main/java/com/example/ui/screens/ManageGuidanceScreen.kt", "w") as f:
    f.write(content)

with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "r") as f:
    vm_content = f.read()

vm_mig = """
    fun migrateGuidanceToFirestore() {
        viewModelScope.launch {
            repository.migrateGuidanceToFirestore()
        }
    }
"""

if "migrateGuidanceToFirestore" not in vm_content:
    vm_content += vm_mig

with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "w") as f:
    f.write(vm_content)

with open("app/src/main/java/com/example/data/repository/JuktiRepository.kt", "r") as f:
    repo_content = f.read()

repo_mig = """
    suspend fun migrateGuidanceToFirestore() {
        val pyqs = guidanceDao.getAllPyqFocus().firstOrNull() ?: emptyList()
        val strats = guidanceDao.getAllPrepStrategies().firstOrNull() ?: emptyList()
        firebaseRepository.uploadGuidanceData(pyqs, strats)
    }
"""

if "migrateGuidanceToFirestore" not in repo_content:
    repo_content += repo_mig

with open("app/src/main/java/com/example/data/repository/JuktiRepository.kt", "w") as f:
    f.write(repo_content)

