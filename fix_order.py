import re

with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "r") as f:
    lines = f.readlines()

def get_block(start_marker, end_marker=None):
    start_idx = -1
    for i, line in enumerate(lines):
        if line.startswith(start_marker):
            start_idx = i
            break
    if start_idx == -1:
        return -1, -1
    
    end_idx = start_idx
    brackets = 0
    in_block = False
    for i in range(start_idx, len(lines)):
        line = lines[i]
        brackets += line.count('{') - line.count('}')
        if '{' in line:
            in_block = True
        
        if end_marker and line.strip().startswith(end_marker):
            end_idx = i
            break
        elif in_block and brackets <= 0:
            # We assume it's just the stateIn block
            if i + 1 < len(lines) and "stateIn" in lines[i+1]:
                end_idx = i + 1
            else:
                end_idx = i
            break
        elif not in_block and "stateIn" in line:
            end_idx = i
            break
            
    # Sometimes stateIn spans multiple lines. Let's just do a simpler approach.
    return start_idx, end_idx

# We will just write a specific regex replacement for the whole block of flows
