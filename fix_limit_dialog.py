import re

with open("app/src/main/java/com/example/ui/screens/McqStudyScreen.kt", "r") as f:
    content = f.read()

limit_dialog_pattern = r'        // Free Plan Limit Dialog\n        if \(showLimitModal\) \{.*?\n        \}'
content = re.sub(limit_dialog_pattern, '', content, flags=re.DOTALL)

with open("app/src/main/java/com/example/ui/screens/McqStudyScreen.kt", "w") as f:
    f.write(content)
