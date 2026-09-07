with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "r") as f:
    vm_content = f.read()

import re
# Fix duplicate MANAGE_GUIDANCE
vm_content = re.sub(r'MANAGE_GUIDANCE,[\s]*HOME,', 'HOME,', vm_content)

with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "w") as f:
    f.write(vm_content)

with open("app/src/main/java/com/example/data/local/Entities.kt", "r") as f:
    e_content = f.read()

e_content = re.sub(r'data class GuidanceEntity\([\s\S]*', '', e_content)

with open("app/src/main/java/com/example/data/local/Entities.kt", "w") as f:
    f.write(e_content)

with open("app/src/main/java/com/example/data/repository/JuktiRepository.kt", "r") as f:
    r_content = f.read()

r_content = re.sub(r'fun saveGuidance\(entity: GuidanceEntity\)[\s\S]*?\}', '', r_content)

with open("app/src/main/java/com/example/data/repository/JuktiRepository.kt", "w") as f:
    f.write(r_content)
