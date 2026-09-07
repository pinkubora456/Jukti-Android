import re

vm_file = "app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt"
with open(vm_file, "r") as f:
    content = f.read()

# Add explicit flow types
replacements = {
    "val allPyqFocus = repository.allPyqFocus.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.Lazily, emptyList())": "val allPyqFocus: StateFlow<List<com.example.data.local.PyqFocusEntity>> = repository.allPyqFocus.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.Lazily, emptyList())",
    "val allFocusTopics = repository.allFocusTopics.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.Lazily, emptyList())": "val allFocusTopics: StateFlow<List<com.example.data.local.FocusTopicEntity>> = repository.allFocusTopics.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.Lazily, emptyList())",
    "val allPrepStrategies = repository.allPrepStrategies.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.Lazily, emptyList())": "val allPrepStrategies: StateFlow<List<com.example.data.local.PrepStrategyEntity>> = repository.allPrepStrategies.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.Lazily, emptyList())",
    "val allGuidanceBanners = repository.allGuidanceBanners.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.Lazily, emptyList())": "val allGuidanceBanners: StateFlow<List<com.example.data.local.GuidanceBannerEntity>> = repository.allGuidanceBanners.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.Lazily, emptyList())"
}

for old, new in replacements.items():
    content = content.replace(old, new)

with open(vm_file, "w") as f:
    f.write(content)
