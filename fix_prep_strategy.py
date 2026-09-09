import re

with open("app/src/main/java/com/example/data/local/Entities.kt", "r") as f:
    content = f.read()

# Replace PrepStrategyEntity
old_ent = """data class PrepStrategyEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val exam: String,
    val content: String,
    val firebaseId: String = "",
    val updatedAt: Long = 0L
)"""

new_ent = """data class PrepStrategyEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val exam: String,
    val subject: String? = null,
    val content: String,
    val firebaseId: String = "",
    val updatedAt: Long = 0L
)"""

content = content.replace(old_ent, new_ent)

with open("app/src/main/java/com/example/data/local/Entities.kt", "w") as f:
    f.write(content)

with open("app/src/main/java/com/example/data/repository/FirebaseRepository.kt", "r") as f:
    repo_content = f.read()

old_repo = """                        PrepStrategyEntity(
                            id = 0L,
                            exam = doc.getString("exam") ?: "",
                            content = doc.getString("content") ?: ""
                        )"""

new_repo = """                        PrepStrategyEntity(
                            id = 0L,
                            exam = doc.getString("exam") ?: "",
                            subject = doc.getString("subject"),
                            content = doc.getString("content") ?: ""
                        )"""

repo_content = repo_content.replace(old_repo, new_repo)

with open("app/src/main/java/com/example/data/repository/FirebaseRepository.kt", "w") as f:
    f.write(repo_content)

with open("app/src/main/java/com/example/data/util/GuidanceEngine.kt", "r") as f:
    engine_content = f.read()

old_engine = "val prepStrategyStr = allPrepStrategies.find { it.exam == exam }?.content ?: generateDynamicStrategy(exam, chapterPerformances)"
new_engine = """val subjectKey = if (subject != "All Subjects") subject else null
        val prepStrategyStr = allPrepStrategies.find { it.exam == exam && it.subject == subjectKey }?.content 
            ?: allPrepStrategies.find { it.exam == exam && it.subject == null }?.content 
            ?: generateDynamicStrategy(exam, chapterPerformances)"""

engine_content = engine_content.replace(old_engine, new_engine)

with open("app/src/main/java/com/example/data/util/GuidanceEngine.kt", "w") as f:
    f.write(engine_content)
