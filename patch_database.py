import re
with open("app/src/main/java/com/example/data/local/JuktiDatabase.kt", "r") as f:
    content = f.read()

content = content.replace("NotificationEntity::class,", "NotificationEntity::class,\n        NotificationCategoryEntity::class,")
content = content.replace("abstract fun notificationDao(): NotificationDao", "abstract fun notificationDao(): NotificationDao\n    abstract fun notificationCategoryDao(): NotificationCategoryDao")

# We should also add some default categories in the Database Callback.
# Let's check JuktiDatabase.Callback.
with open("app/src/main/java/com/example/data/local/JuktiDatabase.kt", "w") as f:
    f.write(content)
