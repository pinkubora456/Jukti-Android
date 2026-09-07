with open("app/src/main/java/com/example/data/repository/JuktiRepository.kt", "r") as f:
    repo_content = f.read()

import re
repo_content = re.sub(r'fun saveGuidance\(entity: GuidanceEntity\)[\s\S]*?\}', '', repo_content)

with open("app/src/main/java/com/example/data/repository/JuktiRepository.kt", "w") as f:
    f.write(repo_content)
