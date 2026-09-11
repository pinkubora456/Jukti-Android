with open('app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt', 'r') as f:
    content = f.read()

import re

# Fix submitQuestionAnswer
submit_regex = r'fun submitQuestionAnswer\(questionId: Long, isCorrect: Boolean, timeSpentSec: Int = 10\) \{\n        viewModelScope\.launch \{\n            val userId = userProfile\.value\?\.uid \?: FirebaseAuth\.getInstance\(\)\.currentUser\?\.uid \?: return@launch\n            val today = java\.text\.SimpleDateFormat\("yyyy-MM-dd", java\.util\.Locale\.US\)\.format\(java\.util\.Date\(\)\)\n            repository\.recordQuestionAnswer\(userId, questionId\.toString\(\), isCorrect, timeSpentSec, today\)\n        \}\n    \}'

submit_replacement = r'fun submitQuestionAnswer(questionId: Long, isCorrect: Boolean, timeSpentSec: Int = 10) {\n        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {\n            val userId = userProfile.value?.uid ?: FirebaseAuth.getInstance().currentUser?.uid ?: return@launch\n            val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())\n            repository.recordQuestionAnswer(userId, questionId.toString(), isCorrect, timeSpentSec, today)\n        }\n    }'

content = re.sub(submit_regex, submit_replacement, content)

# Fix awardCorrectAnswerXp
award_regex = r'fun awardCorrectAnswerXp\(\) \{\n        viewModelScope\.launch \{\n            repository\.awardXp\(10, 0\)\n        \}\n    \}'
award_replacement = r'fun awardCorrectAnswerXp() {\n        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {\n            repository.awardXp(10, 0)\n        }\n    }'
content = re.sub(award_regex, award_replacement, content)

with open('app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt', 'w') as f:
    f.write(content)
