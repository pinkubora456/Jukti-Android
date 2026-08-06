import re
with open("app/src/main/java/com/example/data/repository/JuktiRepository.kt", "r") as f:
    content = f.read()

content = content.replace("private val notificationDao: NotificationDao,", "private val notificationDao: NotificationDao,\n    private val notificationCategoryDao: NotificationCategoryDao,")

new_methods = """
    // Notification Categories
    val allNotificationCategories: Flow<List<NotificationCategoryEntity>> = notificationCategoryDao.getAllNotificationCategories()
    suspend fun insertNotificationCategory(category: NotificationCategoryEntity) = notificationCategoryDao.insertNotificationCategory(category)
    suspend fun deleteNotificationCategory(category: NotificationCategoryEntity) = notificationCategoryDao.deleteNotificationCategory(category)
"""

content = content.replace("    // --- Pending Requests ---", new_methods + "\n    // --- Pending Requests ---")

with open("app/src/main/java/com/example/data/repository/JuktiRepository.kt", "w") as f:
    f.write(content)
