import re

with open("app/src/main/java/com/example/data/local/Entities.kt", "r") as f:
    content = f.read()

new_ent = """@Entity(tableName = "prep_strategies")
data class PrepStrategyEntity("""

content = content.replace("data class PrepStrategyEntity(", new_ent)

with open("app/src/main/java/com/example/data/local/Entities.kt", "w") as f:
    f.write(content)
