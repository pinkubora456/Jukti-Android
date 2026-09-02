import re

def fix_file(filepath):
    with open(filepath, 'r') as f:
        lines = f.readlines()
        
    # Revert fix_braces.py
    for i in range(len(lines)):
        match = re.match(r'^(\s*)\} else \{\s*$', lines[i])
        if match:
            lines[i] = " " * len(match.group(1)) + "else {\n"

    # Now, parse line by line
    stack = []
    out = []
    
    in_multiline_string = False
    
    for i, line in enumerate(lines):
        orig = line
        stripped = line.strip()
        
        # Check multiline string
        if '"""' in line:
            # count occurrences
            if line.count('"""') % 2 != 0:
                in_multiline_string = not in_multiline_string
                
        if in_multiline_string or stripped.startswith('//'):
            out.append(line)
            continue
            
        indent = len(line) - len(line.lstrip())
        
        # If line is completely empty, it could be a missing brace.
        # But we won't insert braces ON empty lines automatically yet.
        # We will check if the NEXT non-empty line forces us to close a block.
        if not stripped:
            out.append(line)
            continue
            
        # The line has some text.
        # Let's count how many '{' and '}' are on this line
        # We ignore strings like "{", "}"
        clean_line = re.sub(r'"([^"\\]|\\.)*"', '', line)
        clean_line = re.sub(r"'([^'\\]|\\.)*'", '', clean_line)
        clean_line = clean_line.split('//')[0]
        
        opens = clean_line.count('{')
        closes = clean_line.count('}')
        
        # If this line is dedented, we might need to insert missing '}' BEFORE this line
        # But we only insert if we know it was corrupted.
        # Corrupted lines lost "                            }" (28 spaces + })
        # So they were at indent >= 28, and their '}' is gone.
        # This means the current stack would have a block started at >= 28.
        # And the current line's indent is less than expected.
        
        while stack and indent < stack[-1] + 4:
            # We are dedented! The expected indent inside the block is stack[-1] + 4.
            # But the current line has less indent. 
            # This implies the block MUST be closed before this line.
            # Was this block started at indent >= 28? (or 24, since block + 4 = 28)
            # Actually, if the block was started at indent >= 24, its closing brace would be at >= 24.
            # 24 spaces + } is 25 chars. Wait, my sed removed 28 spaces + }.
            # So the closing brace was at exactly 28 spaces or more.
            # Which means the block was started at 28 spaces or more!
            block_indent = stack[-1]
            if block_indent >= 28:
                # Yes! A brace is missing!
                # We insert it right before the current line.
                # Actually, we should replace the preceding empty line if there is one.
                # Or just insert it.
                brace_line = " " * block_indent + "}\n"
                
                # Check if the last added line was empty, if so, replace it
                if out and out[-1] == "\n":
                    out[-1] = brace_line
                else:
                    out.append(brace_line)
                stack.pop()
            else:
                # If block_indent < 28, maybe the current line IS the closing brace?
                # E.g. line is "    }"
                # Then we will process it below.
                break
                
        # Now process the current line's own braces
        # Wait, what if the current line IS a closing brace, but we already closed it above?
        # That's why we only auto-close blocks >= 28!
        
        # For the current line:
        # A line can have multiple { and }. 
        # But for indentation purposes, we only care about the net change, or we push for each { and pop for each }.
        for char in clean_line:
            if char == '{':
                # what's the indent of this block? Usually the indent of the line.
                stack.append(indent)
            elif char == '}':
                if stack:
                    stack.pop()
                    
        out.append(line)
        
    while stack:
        block_indent = stack.pop()
        if block_indent >= 28:
            out.append(" " * block_indent + "}\n")
    with open(filepath, 'w') as f:
        f.writelines(out)
        
    print(f"Fixed {filepath}, remaining stack size: {len(stack)}")

fix_file("app/src/main/java/com/example/ui/screens/PracticeScreen.kt")
fix_file("app/src/main/java/com/example/ui/screens/McqStudyScreen.kt")
    while stack:
        block_indent = stack.pop()
        out.append(" " * block_indent + "}\n")

    while stack:
        block_indent = stack.pop()
        if block_indent >= 28:
            out.append(" " * block_indent + "}\n")
    with open(filepath, 'w') as f:
        f.writelines(out)
