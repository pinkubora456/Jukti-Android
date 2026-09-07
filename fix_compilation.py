with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "r") as f:
    vm_content = f.read()

import re
# Check if GUIDANCE is correctly added in enum Screen
if "GUIDANCE" not in vm_content:
    vm_content = vm_content.replace("SPLASH,", "SPLASH, GUIDANCE,")

# Replace saveGuidance signature to remove it (leftover)
vm_content = re.sub(r'fun saveGuidance\(entity: GuidanceEntity\) \{[\s\S]*?\}', '', vm_content)

with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "w") as f:
    f.write(vm_content)

with open("app/src/main/java/com/example/data/local/Entities.kt", "r") as f:
    e_content = f.read()

# Fix syntax errors in Entities.kt
e_content = re.sub(r'data class GuidanceEntity\([\s\S]*?\}', '', e_content)
e_content = re.sub(r'data class GuidanceEntity\([\s\S]*?val updatedAt: Long = 0L,', '', e_content)

# just to be safe, I will re-create Entities.kt and strip GuidanceEntity completely
import glob
for file_path in glob.glob("app/src/main/java/com/example/data/local/Entities.kt"):
    with open(file_path, "r") as ef:
        content = ef.read()
    # It seems my previous replace might have left some dangling lines. Let's fix that.
    content = re.sub(r'@Entity\(tableName = "guidance"\)[\s\S]*?data class GuidanceEntity\([\s\S]*?val updatedAt: Long = 0L,', '', content)
    content = content.replace("    val pyqFocus: String = \"\",", "")
    content = content.replace("    val focusTopics: String = \"\",", "")
    content = content.replace("    val prepStrategy: String = \"\",", "")
    content = content.replace("    val strengthWeakness: String = \"\",", "")
    
    with open(file_path, "w") as ef:
        ef.write(content)

