import re

def fix(filepath):
    with open(filepath, 'r') as f:
        lines = f.readlines()

    for i in range(len(lines)):
        match = re.match(r"^(\s+)else \{$", lines[i])
        if match:
            # Check if previous line ends with }
            # If not, add } else {
            prev = lines[i-1].strip()
            if not prev.endswith('}'):
                lines[i] = match.group(1) + "} else {\n"
                
    with open(filepath, 'w') as f:
        f.writelines(lines)
        
fix("app/src/main/java/com/example/ui/screens/McqStudyScreen.kt")
fix("app/src/main/java/com/example/ui/screens/PracticeScreen.kt")
