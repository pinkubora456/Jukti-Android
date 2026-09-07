import glob
import re

for file_path in glob.glob("app/src/main/java/com/example/**/*.kt", recursive=True):
    with open(file_path, "r") as f:
        content = f.read()

    changed = False
    
    if "GuidanceEntity" in content:
        # Check Entities.kt
        if file_path.endswith("Entities.kt"):
            # Strip trailing GuidanceEntity fields that caused syntax errors
            content = re.sub(r'data class GuidanceEntity\([\s\S]*', '', content)
            changed = True
            
        # Check JuktiRepository
        if file_path.endswith("JuktiRepository.kt"):
            content = re.sub(r'fun saveGuidance\(entity: GuidanceEntity\)[\s\S]*?\}', '', content)
            changed = True

        # Check JuktiViewModel
        if file_path.endswith("JuktiViewModel.kt"):
            content = re.sub(r'fun saveGuidance\(entity: GuidanceEntity\)[\s\S]*?\}', '', content)
            changed = True

    if changed:
        with open(file_path, "w") as f:
            f.write(content)

