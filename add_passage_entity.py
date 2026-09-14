path = "app/src/main/java/com/example/data/local/Entities.kt"
with open(path, "r") as f:
    content = f.read()

passage_entity = """
@Entity(tableName = "rc_passages")
data class ReadingComprehensionPassageEntity(
    @PrimaryKey val passageId: String,
    val passage: String,
    val subject: String,
    val chapter: String,
    val topic: String,
    val difficulty: String,
    val updatedAt: Long = 0L,
    val firebaseId: String = ""
)
"""
if "rc_passages" not in content:
    content = content + "\n" + passage_entity
    with open(path, "w") as f:
        f.write(content)
    print("Added ReadingComprehensionPassageEntity")
else:
    print("Already exists")
