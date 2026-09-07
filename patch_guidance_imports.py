with open("app/src/main/java/com/example/ui/screens/GuidanceScreen.kt", "r") as f:
    content = f.read()

# Add image and other needed imports
if "import androidx.compose.ui.graphics.vector.ImageVector" not in content:
    content = content.replace("import androidx.compose.ui.unit.dp", "import androidx.compose.ui.unit.dp\nimport androidx.compose.ui.graphics.vector.ImageVector")

with open("app/src/main/java/com/example/ui/screens/GuidanceScreen.kt", "w") as f:
    f.write(content)
