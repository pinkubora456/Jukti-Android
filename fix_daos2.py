import re

with open("app/src/main/java/com/example/data/local/Daos.kt", "r") as f:
    content = f.read()

content = content.replace("DELETE FROM prep_strategies", "DELETE FROM prep_strategy")

with open("app/src/main/java/com/example/data/local/Daos.kt", "w") as f:
    f.write(content)

with open("app/src/main/java/com/example/data/local/Entities.kt", "r") as f:
    ent_content = f.read()

ent_content = ent_content.replace("@Entity(tableName = \"prep_strategy\")\n@Entity(tableName = \"prep_strategies\")", "@Entity(tableName = \"prep_strategy\")")

with open("app/src/main/java/com/example/data/local/Entities.kt", "w") as f:
    f.write(ent_content)

