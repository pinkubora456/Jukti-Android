import re
with open("app/src/main/java/com/example/data/local/Entities.kt", "r") as f:
    content = f.read()

new_entity = """
@Entity(tableName = "notification_categories")
data class NotificationCategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String
)
"""
content = content + new_entity

with open("app/src/main/java/com/example/data/local/Entities.kt", "w") as f:
    f.write(content)
