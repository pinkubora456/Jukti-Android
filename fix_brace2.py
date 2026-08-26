import re

with open("app/src/main/java/com/example/ui/screens/McqStudyScreen.kt", "r") as f:
    content = f.read()

target = """
                    Spacer(modifier = Modifier.height(18.dp))

                    // Previous & Next Navigation Buttons
                    Row(
"""

replacement = """
                    } // Closing the premium check for options/explanation
                    
                    Spacer(modifier = Modifier.height(18.dp))

                    // Previous & Next Navigation Buttons
                    Row(
"""

if target in content:
    content = content.replace(target, replacement)
else:
    print("Could not find the target 2!")

# We also added an extra `}` at the end of the file earlier, we should remove it.
content = content.rstrip()
if content.endswith("}"):
    content = content[:-1]

with open("app/src/main/java/com/example/ui/screens/McqStudyScreen.kt", "w") as f:
    f.write(content)
