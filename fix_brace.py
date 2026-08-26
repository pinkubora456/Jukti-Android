import re

with open("app/src/main/java/com/example/ui/screens/McqStudyScreen.kt", "r") as f:
    content = f.read()

target = """
                    Spacer(modifier = Modifier.height(32.dp))

                    // Navigation Row (< Previous  X/Y  Next >)
                    Row(
"""

replacement = """
                    } // Closing the premium check for options/explanation
                    
                    Spacer(modifier = Modifier.height(32.dp))

                    // Navigation Row (< Previous  X/Y  Next >)
                    Row(
"""

if target in content:
    content = content.replace(target, replacement)
else:
    print("Could not find the target!")

with open("app/src/main/java/com/example/ui/screens/McqStudyScreen.kt", "w") as f:
    f.write(content)
