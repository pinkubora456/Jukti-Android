import re
with open("app/src/main/java/com/example/data/local/Daos.kt", "r") as f:
    content = f.read()

new_dao = """
@Dao
interface NotificationCategoryDao {
    @Query("SELECT * FROM notification_categories")
    fun getAllNotificationCategories(): Flow<List<NotificationCategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotificationCategory(category: NotificationCategoryEntity)

    @Delete
    suspend fun deleteNotificationCategory(category: NotificationCategoryEntity)
}
"""

content = content + "\n" + new_dao

with open("app/src/main/java/com/example/data/local/Daos.kt", "w") as f:
    f.write(content)
