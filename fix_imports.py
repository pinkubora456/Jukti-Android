import re

with open("app/src/main/java/com/example/data/repository/FirebaseRepository.kt", "r") as f:
    content = f.read()

if "import kotlinx.coroutines.suspendCancellableCoroutine" not in content:
    content = content.replace("import kotlinx.coroutines.tasks.await", "import kotlinx.coroutines.tasks.await\nimport kotlinx.coroutines.suspendCancellableCoroutine\nimport kotlin.coroutines.resume")

with open("app/src/main/java/com/example/data/repository/FirebaseRepository.kt", "w") as f:
    f.write(content)
