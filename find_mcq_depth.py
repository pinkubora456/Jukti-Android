def check():
    with open("app/src/main/java/com/example/ui/screens/McqStudyScreen.kt", "r") as f:
        lines = f.readlines()
        
    depth = 0
    for i, line in enumerate(lines):
        import re
        clean_line = re.sub(r'"([^"\\]|\\.)*"', '', line)
        clean_line = re.sub(r"'([^'\\]|\\.)*'", '', clean_line)
        clean_line = clean_line.split('//')[0]
        
        for char in clean_line:
            if char == '{':
                depth += 1
            elif char == '}':
                depth -= 1
                
        if i % 100 == 0:
            print(f"Line {i}: depth {depth}")
            
    print(f"End of file depth is {depth}")
                    
check()
