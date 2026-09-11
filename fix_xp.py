import re

with open('app/src/main/java/com/example/data/repository/JuktiRepository.kt', 'r') as f:
    content = f.read()

replacement = """        if (isCorrect) {
            if (!alreadyAttemptedToday) {
                xpToAward = 10 
            }
        }"""
content = re.sub(r'        if \(isCorrect\) \{\n            if \(!alreadyAttemptedToday\) \{\n                // Keep the original XP logic if possible, simplified\n                xpToAward = 5 \n                if \(state\.incorrectCount == 0 && state\.totalAttempts == 0\) \{\n                   xpToAward = 5 // First correct\n                \} else if \(state\.everGotWrong\) \{\n                    xpToAward = 8 // Correct after wrong\n                \}\n            \}\n        \}', replacement, content)

with open('app/src/main/java/com/example/data/repository/JuktiRepository.kt', 'w') as f:
    f.write(content)

