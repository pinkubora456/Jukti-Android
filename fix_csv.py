with open('app/src/main/java/com/example/util/CsvQuestionParser.kt', 'r') as f:
    content = f.read()

import re

# We can find where questionType is being assigned.
# Usually it's something like val questionType = columns[x]

def replace_qtype(m):
    return """val rawType = """ + m.group(1) + """
            val questionType = if (rawType.startsWith("PYQ", ignoreCase = true)) "PYQ" else "Expected\""""

content = re.sub(r'val questionType\s*=\s*(.*?)\n', replace_qtype, content)

with open('app/src/main/java/com/example/util/CsvQuestionParser.kt', 'w') as f:
    f.write(content)
