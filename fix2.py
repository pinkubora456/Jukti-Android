with open('app/src/main/java/com/example/ui/screens/GuidanceScreen.kt', 'r') as f:
    content = f.read()

content = content.replace("else 0f                        val (baseColor, bgColor) = when {", "else 0f\n                        val (baseColor, bgColor) = when {")

with open('app/src/main/java/com/example/ui/screens/GuidanceScreen.kt', 'w') as f:
    f.write(content)
