import os
import re

def process_file(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()
    
    original_content = content
    
    # Replace `if (language == AppLanguage.ASSAMESE) "..." else "..."` with `"..."`
    # We can use regex to match the pattern:
    # `if\s*\([^)]*(isAssamese|language == AppLanguage\.ASSAMESE)\)\s*"[^"]*"\s*else\s*("[^"]*")` -> `\2`
    
    # Pattern 1: string literals
    pattern1 = re.compile(r'if\s*\([^)]*(?:isAssamese|language == AppLanguage\.ASSAMESE|appLanguage == AppLanguage\.ASSAMESE)\)\s*"[^"]*"\s*else\s*("[^"]*")')
    content = pattern1.sub(r'\1', content)
    
    # Pattern 2: `if (isAssamese) item.titleAs else item.titleEn`
    pattern2 = re.compile(r'if\s*\([^)]*(?:isAssamese|language == AppLanguage\.ASSAMESE)\)\s*([a-zA-Z0-9_\.]+)\s*else\s*([a-zA-Z0-9_\.]+)')
    content = pattern2.sub(r'\2', content)

    # Some multiline ones like `if (isAssamese) { ... } else { ... }` or `if (isAssamese) \n "..." else \n "..."`
    # we can try to catch them manually.

    if content != original_content:
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(content)
        print(f"Updated {filepath}")

for root, dirs, files in os.walk('app/src/main/java'):
    for file in files:
        if file.endswith('.kt'):
            process_file(os.path.join(root, file))
