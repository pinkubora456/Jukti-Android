import re
with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "r") as f:
    content = f.read()

new_properties = """
    val allNotificationCategories = repository.allNotificationCategories.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    fun addNotificationCategory(name: String) {
        viewModelScope.launch {
            repository.insertNotificationCategory(NotificationCategoryEntity(name = name))
        }
    }

    fun deleteNotificationCategory(category: NotificationCategoryEntity) {
        viewModelScope.launch {
            repository.deleteNotificationCategory(category)
        }
    }
"""

content = content.replace("    val notifications = repository.allNotifications.stateIn(", new_properties + "\n    val notifications = repository.allNotifications.stateIn(")
content = content.replace("import com.example.data.local.NotificationEntity", "import com.example.data.local.NotificationEntity\nimport com.example.data.local.NotificationCategoryEntity")

with open("app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt", "w") as f:
    f.write(content)
