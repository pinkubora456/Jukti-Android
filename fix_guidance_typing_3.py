import re

files_to_fix = [
    "app/src/main/java/com/example/ui/screens/GuidanceScreen.kt",
    "app/src/main/java/com/example/ui/screens/ManageGuidanceScreen.kt"
]

for file_path in files_to_fix:
    with open(file_path, "r") as f:
        content = f.read()

    # The issue is that allPyqFocus, allFocusTopics etc might still have issues
    content = content.replace("val allPyqFocus by viewModel.allPyqFocus.collectAsState(initial = emptyList())", "val allPyqFocus: List<PyqFocusEntity> by viewModel.allPyqFocus.collectAsState(initial = emptyList())")
    content = content.replace("val allFocusTopics by viewModel.allFocusTopics.collectAsState(initial = emptyList())", "val allFocusTopics: List<FocusTopicEntity> by viewModel.allFocusTopics.collectAsState(initial = emptyList())")
    content = content.replace("val allPrepStrategies by viewModel.allPrepStrategies.collectAsState(initial = emptyList())", "val allPrepStrategies: List<PrepStrategyEntity> by viewModel.allPrepStrategies.collectAsState(initial = emptyList())")
    content = content.replace("val allGuidanceBanners by viewModel.allGuidanceBanners.collectAsState(initial = emptyList())", "val allGuidanceBanners: List<GuidanceBannerEntity> by viewModel.allGuidanceBanners.collectAsState(initial = emptyList())")

    with open(file_path, "w") as f:
        f.write(content)
