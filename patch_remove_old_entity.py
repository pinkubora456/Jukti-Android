with open("app/src/main/java/com/example/data/local/Entities.kt", "r") as f:
    content = f.read()

import re
content = re.sub(r'@Entity\(tableName = "guidance"\)[\s\S]*?data class GuidanceEntity\([\s\S]*?\)', '', content)

with open("app/src/main/java/com/example/data/local/Entities.kt", "w") as f:
    f.write(content)

# We also need to fix JuktiDatabase
with open("app/src/main/java/com/example/data/local/JuktiDatabase.kt", "r") as f:
    db_content = f.read()

db_content = db_content.replace("GuidanceEntity::class,", "")

with open("app/src/main/java/com/example/data/local/JuktiDatabase.kt", "w") as f:
    f.write(db_content)

print("Removed old GuidanceEntity")
