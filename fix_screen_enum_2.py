with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "r") as f:
    content = f.read()

import re
content = re.sub(r'enum class Screen \{[\s]*SPLASH,', 'enum class Screen {\n    SPLASH,\n    GUIDANCE,\n    MANAGE_GUIDANCE,', content)

with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "w") as f:
    f.write(content)
