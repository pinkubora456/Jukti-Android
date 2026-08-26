with open("app/src/main/java/com/example/data/repository/FirebaseRepository.kt", "r") as f:
    content = f.read()

# Revert java.util.Locale.ROOT back to Locale.ROOT
content = content.replace("java.util.Locale.ROOT", "Locale.ROOT")

# Add Locale import if missing
if "import java.util.Locale" not in content:
    content = content.replace("import java.util.UUID", "import java.util.UUID\nimport java.util.Locale")
    if "import java.util.Locale" not in content:
        # fallback
        content = "import java.util.Locale\n" + content

# Map import
if "import kotlinx.coroutines.flow.map" not in content:
    content = content.replace("import kotlinx.coroutines.flow.flow", "import kotlinx.coroutines.flow.flow\nimport kotlinx.coroutines.flow.map")
    
with open("app/src/main/java/com/example/data/repository/FirebaseRepository.kt", "w") as f:
    f.write(content)
