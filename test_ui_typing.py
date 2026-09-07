import glob
for file_path in ["app/src/main/java/com/example/ui/screens/ManageGuidanceScreen.kt", "app/src/main/java/com/example/ui/screens/GuidanceScreen.kt"]:
    with open(file_path, "r") as f:
        content = f.read()
    print(f"{file_path} pyq typing: {'List<PyqFocusEntity>' in content}")
