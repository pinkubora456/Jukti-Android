def check():
    with open("app/src/main/java/com/example/ui/screens/PracticeScreen.kt", "r") as f:
        lines = f.readlines()
        
    stack = []
    for i, line in enumerate(lines):
        if "private data class BannerConfig" in line:
            print(f"Depth is {len(stack)}")
            for num, code in stack:
                print(f"{num}: {code.strip()}")
            break
            
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
                    
check()
