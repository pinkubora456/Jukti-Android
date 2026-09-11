with open('app/src/main/java/com/example/data/repository/JuktiRepository.kt', 'r') as f:
    lines = f.readlines()

new_lines = []
for line in lines:
    if "val baseExam = if (targetPyqExamName" in line:
        continue
    new_lines.append(line)

with open('app/src/main/java/com/example/data/repository/JuktiRepository.kt', 'w') as f:
    f.writelines(new_lines)
