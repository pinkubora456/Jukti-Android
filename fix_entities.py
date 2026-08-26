import re

with open("app/src/main/java/com/example/data/local/Entities.kt", "r") as f:
    text = f.read()

text = text.replace('val accessType: String = "FREE"\\)', 'val accessType: String = "FREE"\n)')

with open("app/src/main/java/com/example/data/local/Entities.kt", "w") as f:
    f.write(text)

with open("app/src/main/java/com/example/data/repository/FirebaseRepository.kt", "r") as f:
    text = f.read()

text = text.replace('kotlinx.coroutines.tasks.await', 'kotlinx.coroutines.tasks.await') # This is just a check
# Wait, why was await unresolved? Let's import it.
if 'import kotlinx.coroutines.tasks.await' not in text:
    text = text.replace('import kotlinx.coroutines.flow.*', 'import kotlinx.coroutines.flow.*\nimport kotlinx.coroutines.tasks.await')

text = text.replace('createdAt = (doc["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis()', '')

with open("app/src/main/java/com/example/data/repository/FirebaseRepository.kt", "w") as f:
    f.write(text)

