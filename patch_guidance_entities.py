import re

entities_file = "app/src/main/java/com/example/data/local/Entities.kt"
with open(entities_file, "r") as f:
    content = f.read()

# Add the new entities if they don't exist
if "PyqFocusEntity" not in content:
    content += """
@Entity(tableName = "pyq_focus")
data class PyqFocusEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val exam: String,
    val subject: String,
    val chapter: String,
    val pyqCount: Int = 0,
    val examsCovered: Int = 0,
    val firebaseId: String = "",
    val updatedAt: Long = 0L
)

@Entity(tableName = "focus_topics")
data class FocusTopicEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val exam: String,
    val subject: String,
    val chapter: String,
    val topic: String,
    val priority: String = "Medium",
    val instruction: String,
    val firebaseId: String = "",
    val updatedAt: Long = 0L
)

@Entity(tableName = "prep_strategy")
data class PrepStrategyEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val exam: String,
    val content: String,
    val firebaseId: String = "",
    val updatedAt: Long = 0L
)

@Entity(tableName = "guidance_banner")
data class GuidanceBannerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val exam: String,
    val title: String,
    val description: String,
    val imageUrl: String = "",
    val displayOrder: Int = 0,
    val isActive: Boolean = true,
    val actionTarget: String = "",
    val firebaseId: String = "",
    val updatedAt: Long = 0L
)
"""
    with open(entities_file, "w") as f:
        f.write(content)
    print("Added new entities to Entities.kt")
else:
    print("Entities already exist.")
