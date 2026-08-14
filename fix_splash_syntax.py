import re

with open('app/src/main/java/com/example/ui/screens/SplashScreen.kt', 'r') as f:
    content = f.read()

content = content.replace("import androidx.compose.ui.unit.sppackage", "package")
if not content.startswith("package com.example.ui.screens"):
    content = re.sub(r'^.*package com.example.ui.screens', 'package com.example.ui.screens', content, flags=re.DOTALL)

if "import androidx.compose.ui.unit.sp" not in content:
    content = content.replace("import androidx.compose.ui.unit.dp", "import androidx.compose.ui.unit.dp\nimport androidx.compose.ui.unit.sp")

with open('app/src/main/java/com/example/ui/screens/SplashScreen.kt', 'w') as f:
    f.write(content)
