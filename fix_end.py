import re

def fix(filepath):
    with open(filepath, "r") as f:
        content = f.read()

    idx = content.find("private data class BannerConfig")
    if idx == -1:
        idx = content.find("private data class")
        
    if idx == -1: return
    
    before = content[:idx]
    after = content[idx:]
    
    # count braces in `before`
    # exclude comments and strings? The codebase doesn't have many block comments or strings with braces.
    open_count = before.count("{")
    close_count = before.count("}")
    
    diff = open_count - close_count
    
    if diff > 0:
        print(f"Adding {diff} braces")
        before += ("}\n" * diff)
        
    with open(filepath, "w") as f:
        f.write(before + after)

fix("app/src/main/java/com/example/ui/screens/PracticeScreen.kt")
