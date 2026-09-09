import re

with open("app/src/main/java/com/example/data/local/Daos.kt", "r") as f:
    content = f.read()

new_daos = """
    @Query("DELETE FROM pyq_focus")
    abstract suspend fun deleteAllPyqFocus()

    @Query("DELETE FROM prep_strategies")
    abstract suspend fun deleteAllPrepStrategies()
"""

if "deleteAllPyqFocus" not in content:
    content = content.replace("interface GuidanceDao {", "interface GuidanceDao {\n" + new_daos)

with open("app/src/main/java/com/example/data/local/Daos.kt", "w") as f:
    f.write(content)
