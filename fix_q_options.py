import re

with open("app/src/main/java/com/example/ui/screens/McqStudyScreen.kt", "r") as f:
    content = f.read()

# We need to wrap the options and explanation block with an `if` check.
target = """
                    Spacer(modifier = Modifier.height(24.dp))

                    // Options List - Directly highlights the correct answer
"""

replacement = """
                    Spacer(modifier = Modifier.height(24.dp))

                    if (!currentQuestion.isPremium || isUserPremium || isAdminOrOwner) {
                    // Options List - Directly highlights the correct answer
"""

target2 = """
                    // Question Reporting and Stats Row
                    Row(
"""

replacement2 = """
                    }
                    
                    // Question Reporting and Stats Row
                    Row(
"""

content = content.replace(target, replacement)
content = content.replace(target2, replacement2)

with open("app/src/main/java/com/example/ui/screens/McqStudyScreen.kt", "w") as f:
    f.write(content)
