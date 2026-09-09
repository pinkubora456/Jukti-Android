import re

with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "r") as f:
    content = f.read()

new_func = """
    fun refreshGuidanceData() {
        viewModelScope.launch {
            repository.refreshGuidanceData()
        }
    }
"""

if "refreshGuidanceData" not in content:
    idx = content.find("init {")
    if idx != -1:
        # insert into init
        content = content.replace("init {", "init {\n        refreshGuidanceData()")
    content = content + new_func

with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "w") as f:
    f.write(content)
