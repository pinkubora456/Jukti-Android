import re

with open('app/src/main/java/com/example/ui/screens/AllQuestionsScreen.kt', 'r') as f:
    content = f.read()

import re
matches = re.findall(r'if \(showMoveDialog.*?\).*?\n    }', content, re.DOTALL)
for m in matches:
    print(m)
