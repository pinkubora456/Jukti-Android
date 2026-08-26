import re

with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "r") as f:
    lines = f.readlines()

new_lines = []
for line in lines:
    if line.strip() == ")" and (len(new_lines) > 0 and "Guest Mode" in new_lines[-1]):
        print("Removed orphan after Guest Mode")
        continue
    new_lines.append(line)

final_lines = []
for i, line in enumerate(new_lines):
    if line.strip() == ")" and "emptyList()" in new_lines[i-1] and "StateFlow<List<MockTestEntity>>" in "".join(new_lines[i-15:i]):
        print("Removed orphan after MockTestEntity stateIn")
        continue
    final_lines.append(line)

with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "w") as f:
    f.writelines(final_lines)
