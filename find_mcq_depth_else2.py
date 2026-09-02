def check():
    with open("app/src/main/java/com/example/ui/screens/McqStudyScreen.kt", "r") as f:
        lines = f.readlines()
        
    depth = 0
    in_else = False
    target_depth = 0
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
                
        if "} else {" in line and i > 800:
            in_else = True
            target_depth = depth
            print(f"else found at line {i+1} with target depth {target_depth}")
            
        if in_else and depth < target_depth and i > 823:
            print(f"else block ends at Line {i+1}! depth={depth}")
            break
            
check()
