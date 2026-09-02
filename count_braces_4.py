def check():
    with open("app/src/main/java/com/example/ui/screens/PracticeScreen.kt", "r") as f:
        lines = f.readlines()
        
    stack = []
    for i, line in enumerate(lines[:1040]):
        import re
        clean_line = re.sub(r'"([^"\\]|\\.)*"', '', line)
        clean_line = re.sub(r"'([^'\\]|\\.)*'", '', clean_line)
        clean_line = clean_line.split('//')[0]
        
        for char in clean_line:
            if char == '{':
                stack.append((i+1, line.strip()))
            elif char == '}':
                if stack:
                    stack.pop()
                    
    print(f"At line 1040, depth is {len(stack)}")
    for num, code in stack:
        print(f"{num}: {code}")
                    
check()
