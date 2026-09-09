import re

with open('app/src/main/java/com/example/ui/screens/AllQuestionsScreen.kt', 'r') as f:
    content = f.read()

# Fix lines 464-469
content = content.replace("""        )
    }


        )
    }""", """        )
    }""")

# Fix lines 619-624
broken_text = '''                Text("Update $selectedCount Questions?

Subject: $destSubj
Chapter: $destChap

These changes will be applied to all $selectedCount selected questions.")'''
fixed_text = '                Text("Update $selectedCount Questions?\\n\\nSubject: $destSubj\\nChapter: $destChap\\n\\nThese changes will be applied to all $selectedCount selected questions.")'
content = content.replace(broken_text, fixed_text)

with open('app/src/main/java/com/example/ui/screens/AllQuestionsScreen.kt', 'w') as f:
    f.write(content)
