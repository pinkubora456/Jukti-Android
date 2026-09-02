def check():
    with open("app/src/main/java/com/example/ui/screens/McqStudyScreen.kt", "r") as f:
        lines = f.readlines()
        
    depth = 0
    in_tab = False
    for i, line in enumerate(lines):
        import re
        clean_line = re.sub(r'"([^"\\]|\\.)*"', '', line)
        clean_line = re.sub(r"'([^'\\]|\\.)*'", '', clean_line)
        clean_line = clean_line.split('//')[0]
        
        if "fun StudyMcqInteractiveTab" in line:
            in_tab = True
            
        if in_tab:
            for char in clean_line:
                if char == '{':
                    depth += 1
                elif char == '}':
                    depth -= 1
                    
            if depth <= 0:
                print(f"Line {i+1} drops to depth {depth}: {line.rstrip()}")
                
        if "fun PracticeMcqTab" in line:
            print(f"At PracticeMcqTab (Line {i+1}), depth is {depth}")
            break
            
check()
