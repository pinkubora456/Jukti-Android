import re
with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

content = content.replace("if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {", "if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {")

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
