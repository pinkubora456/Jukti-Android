import re

vm_file = "app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt"
with open(vm_file, "r") as f:
    content = f.read()

# I see it still has `allGuidance = repository.allGuidance` at 269
# Let's replace it with the new properties

new_props = """
    val allPyqFocus = repository.allPyqFocus.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.Lazily, emptyList())
    val allFocusTopics = repository.allFocusTopics.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.Lazily, emptyList())
    val allPrepStrategies = repository.allPrepStrategies.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.Lazily, emptyList())
    val allGuidanceBanners = repository.allGuidanceBanners.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.Lazily, emptyList())

    fun savePyqFocus(entity: com.example.data.local.PyqFocusEntity) { viewModelScope.launch { repository.savePyqFocus(entity) } }
    fun saveFocusTopic(entity: com.example.data.local.FocusTopicEntity) { viewModelScope.launch { repository.saveFocusTopic(entity) } }
    fun deleteFocusTopic(entity: com.example.data.local.FocusTopicEntity) { viewModelScope.launch { repository.deleteFocusTopic(entity) } }
    fun savePrepStrategy(entity: com.example.data.local.PrepStrategyEntity) { viewModelScope.launch { repository.savePrepStrategy(entity) } }
    fun saveGuidanceBanner(entity: com.example.data.local.GuidanceBannerEntity) { viewModelScope.launch { repository.saveGuidanceBanner(entity) } }
    fun deleteGuidanceBanner(entity: com.example.data.local.GuidanceBannerEntity) { viewModelScope.launch { repository.deleteGuidanceBanner(entity) } }
"""

content = re.sub(r'val allGuidance = repository\.allGuidance\.stateIn.*?emptyList\(\)\)', new_props.strip(), content, flags=re.DOTALL)

with open(vm_file, "w") as f:
    f.write(content)

