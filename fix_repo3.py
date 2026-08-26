with open("app/src/main/java/com/example/data/repository/FirebaseRepository.kt", "r") as f:
    content = f.read()

if "import kotlinx.coroutines.flow.map" not in content:
    content = content.replace("import kotlinx.coroutines.flow.Flow\n", "import kotlinx.coroutines.flow.Flow\nimport kotlinx.coroutines.flow.map\n")
    
with open("app/src/main/java/com/example/data/repository/FirebaseRepository.kt", "w") as f:
    f.write(content)
