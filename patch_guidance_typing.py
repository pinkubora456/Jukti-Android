import re

files_to_fix = [
    "app/src/main/java/com/example/ui/screens/GuidanceScreen.kt",
    "app/src/main/java/com/example/ui/screens/ManageGuidanceScreen.kt"
]

for file_path in files_to_fix:
    with open(file_path, "r") as f:
        content = f.read()

    # The issue is that allPyqFocus, allFocusTopics etc are inferred as List<Any> or similar if types aren't explicit
    content = content.replace("val allPyqFocus by viewModel.allPyqFocus.collectAsState()", "val allPyqFocus by viewModel.allPyqFocus.collectAsState(initial = emptyList())")
    content = content.replace("val allFocusTopics by viewModel.allFocusTopics.collectAsState()", "val allFocusTopics by viewModel.allFocusTopics.collectAsState(initial = emptyList())")
    content = content.replace("val allPrepStrategies by viewModel.allPrepStrategies.collectAsState()", "val allPrepStrategies by viewModel.allPrepStrategies.collectAsState(initial = emptyList())")
    content = content.replace("val allGuidanceBanners by viewModel.allGuidanceBanners.collectAsState()", "val allGuidanceBanners by viewModel.allGuidanceBanners.collectAsState(initial = emptyList())")

    with open(file_path, "w") as f:
        f.write(content)
