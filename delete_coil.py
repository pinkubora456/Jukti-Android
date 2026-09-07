with open("app/src/main/java/com/example/ui/screens/GuidanceScreen.kt", "r") as f:
    content = f.read()

content = content.replace("import coil.compose.AsyncImage\n", "")

with open("app/src/main/java/com/example/ui/screens/GuidanceScreen.kt", "w") as f:
    f.write(content)
