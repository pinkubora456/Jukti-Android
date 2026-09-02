def check():
    with open("app/src/main/java/com/example/ui/screens/McqStudyScreen.kt", "r") as f:
        lines = f.readlines()
        
    depth = 0
    for i, line in enumerate(lines):
        import re
        clean_line = re.sub(r'"([^"\\]|\\.)*"', '', line)
        clean_line = re.sub(r"'([^'\\]|\\.)*'", '', clean_line)
        clean_line = clean_line.split('//')[0]
        
        old_depth = depth
        for char in clean_line:
            if char == '{':
                depth += 1
            elif char == '}':
                depth -= 1
        
        if old_depth != depth and depth == 0:
            print(f"L{i+1} depth 0")
        if "fun StudyMcqInteractiveTab" in line:
            print(f"StudyMcqInteractiveTab at {i+1}, depth {depth}")
        if "fun PracticeMcqTab" in line:
            print(f"PracticeMcqTab at {i+1}, depth {depth}")
        if "fun PomodoroClockTab" in line:
            print(f"PomodoroClockTab at {i+1}, depth {depth}")
check()
