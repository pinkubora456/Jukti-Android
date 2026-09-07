import re
files = [
    "app/src/main/java/com/example/data/repository/JuktiRepository.kt",
    "app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt"
]
for file_path in files:
    with open(file_path, "r") as f:
        content = f.read()

    # Repository
    if "JuktiRepository" in file_path:
        content = re.sub(r'suspend fun saveGuidance\(guidance: com\.example\.data\.local\.GuidanceEntity\) \{[\s\S]*?\}', '', content)

    # ViewModel
    if "JuktiViewModel" in file_path:
        content = re.sub(r'fun saveGuidance\(entity: com\.example\.data\.local\.GuidanceEntity\) \{[\s\S]*?\}', '', content)

    with open(file_path, "w") as f:
        f.write(content)
