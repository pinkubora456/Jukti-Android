import re

def fix(filepath):
    with open(filepath, "r") as f:
        lines = f.readlines()
        
    for i in range(len(lines)):
        line = lines[i]
        
        # fix floating comma
        if re.match(r'^\s*,$', line):
            lines[i] = "                                        },\n"
            
        # fix missing braces for empty lines that were deleted
        # How to know? The compiler errors told us:
        # 1178: Syntax error: Unexpected tokens (use ';' to separate expressions on the same line).
        
    with open(filepath, "w") as f:
        f.writelines(lines)

fix("app/src/main/java/com/example/ui/screens/PracticeScreen.kt")
