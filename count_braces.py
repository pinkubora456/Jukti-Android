def check():
    with open("app/src/main/java/com/example/ui/screens/PracticeScreen.kt", "r") as f:
        lines = f.readlines()
        
    depth = 0
    for i, line in enumerate(lines):
        if "private data class BannerConfig" in line:
            print(f"Line {i+1} depth is {depth}")
            break
        # ignore strings
        l = line.replace('"', '')
        l = l.replace("'", '')
        depth += l.count('{')
        depth -= l.count('}')
        
check()
