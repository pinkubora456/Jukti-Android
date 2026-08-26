import re

with open("app/src/main/java/com/example/ui/screens/McqStudyScreen.kt", "r") as f:
    content = f.read()

# In Practice Screen, where Question text is displayed, let's look for "textEn = q.questionEn"
# Wait, let's just make the user unable to click the subject if they don't have premium?
