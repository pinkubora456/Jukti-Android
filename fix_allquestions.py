with open('app/src/main/java/com/example/ui/screens/AllQuestionsScreen.kt', 'r') as f:
    content = f.read()

import re

# Update selectedSubject
content = content.replace("val selectedSubject by viewModel.selectedSubject.collectAsState()", "val selectedOverviewSubjects by viewModel.selectedOverviewSubjects.collectAsState()")

# Update remember keys
content = content.replace("selectedSubject, selectedChapter,", "selectedOverviewSubjects, selectedChapter,")

# Update matchesSubject
match_regex = r'val matchesSubject = selectedSubject == "All Subjects" \|\|\s*normSubj\.equals\(selectedSubject, ignoreCase = true\) \|\|\s*q\.subject\.equals\(selectedSubject, ignoreCase = true\)'
match_replacement = r'val matchesSubject = selectedOverviewSubjects.contains("All Subjects") ||\n                  selectedOverviewSubjects.contains(normSubj) ||\n                  selectedOverviewSubjects.contains(q.subject)'
content = re.sub(match_regex, match_replacement, content)

# Update if statement
if_regex = r'selectedSubject != "All Subjects"'
if_replacement = r'!selectedOverviewSubjects.contains("All Subjects")'
content = content.replace(if_regex, if_replacement)

# Update Text
text_regex = r'Text\("Subject: \$selectedSubject", style = MaterialTheme\.typography\.bodySmall, fontWeight = FontWeight\.Bold\)'
text_replacement = r'Text("Subjects: ${selectedOverviewSubjects.size} Selected", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)'
content = re.sub(text_regex, text_replacement, content)

with open('app/src/main/java/com/example/ui/screens/AllQuestionsScreen.kt', 'w') as f:
    f.write(content)
