import re

def check_empty(filepath):
    with open(filepath, 'r') as f:
        lines = f.readlines()
        
    count = 0
    for i in range(len(lines)):
        if lines[i] == '\n':
            # find previous non-empty
            prev_indent = 0
            for j in range(i-1, -1, -1):
                if lines[j].strip():
                    prev_indent = len(lines[j]) - len(lines[j].lstrip())
                    break
            # find next non-empty
            next_indent = 0
            for j in range(i+1, len(lines)):
                if lines[j].strip():
                    next_indent = len(lines[j]) - len(lines[j].lstrip())
                    break
            
            if prev_indent >= 32 and next_indent <= 28:
                count += 1
                
    print(f"{filepath}: {count}")

check_empty("app/src/main/java/com/example/ui/screens/PracticeScreen.kt")
check_empty("app/src/main/java/com/example/ui/screens/McqStudyScreen.kt")
