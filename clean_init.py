with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "r") as f:
    lines = f.readlines()

new_lines = []
skip = False
block_count = 0
skip_count = 0

for line in lines:
    if "private val networkMonitor = NetworkMonitor(application)" in line:
        block_count += 1
        if block_count > 1:
            skip = True
            skip_count = 43  # Adjust based on block size
            new_lines.append("    init {\n")
            continue
    
    if skip and skip_count > 0:
        skip_count -= 1
        continue
    
    skip = False
    new_lines.append(line)

with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "w") as f:
    f.writelines(new_lines)
