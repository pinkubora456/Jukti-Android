with open('app/src/main/java/com/example/ui/screens/ContentQuestionsOverviewScreen.kt', 'r') as f:
    content = f.read()

content = content.replace('viewModel.setSubjectFilter(subjectsList.first())', '')
content = content.replace('viewModel.setSubjectFilter(selectedSubject)', '')

with open('app/src/main/java/com/example/ui/screens/ContentQuestionsOverviewScreen.kt', 'w') as f:
    f.write(content)
