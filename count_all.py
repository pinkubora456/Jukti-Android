def check():
    with open("app/src/main/java/com/example/ui/screens/PracticeScreen.kt", "r") as f:
        lines = f.readlines()
        
    stack = []
    for i, line in enumerate(lines):
        import re
        clean_line = re.sub(r'"([^"\\]|\\.)*"', '', line)
        clean_line = re.sub(r"'([^'\\]|\\.)*'", '', clean_line)
        clean_line = clean_line.split('//')[0]
        
        for char in clean_line:
            if char == '{':
                stack.append((i+1, line))
            elif char == '}':
                if stack:
                    stack.pop()
                else:
                    print(f"Error: Too many closing braces at line {i+1}!")
                    return
                    
    print(f"End of file depth is {len(stack)}")
    for num, code in stack:
        print(f"{num}: {code.strip()}")
                    
check()
