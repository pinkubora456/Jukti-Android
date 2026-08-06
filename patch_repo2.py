import re
with open("app/src/main/java/com/example/data/repository/JuktiRepository.kt", "r") as f:
    content = f.read()

new_methods = """
    // Notification Categories
    val allNotificationCategories = notificationCategoryDao.getAllNotificationCategories()
    suspend fun insertNotificationCategory(category: NotificationCategoryEntity) = notificationCategoryDao.insertNotificationCategory(category)
    suspend fun deleteNotificationCategory(category: NotificationCategoryEntity) = notificationCategoryDao.deleteNotificationCategory(category)
"""

content = content.replace("    // Pending Requests", new_methods + "\n    // Pending Requests")
content = content.replace("import com.example.data.local.NotificationEntity", "import com.example.data.local.NotificationEntity\nimport com.example.data.local.NotificationCategoryEntity")

with open("app/src/main/java/com/example/data/repository/JuktiRepository.kt", "w") as f:
    f.write(content)
