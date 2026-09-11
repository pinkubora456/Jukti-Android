with open('app/src/main/java/com/example/data/local/JuktiDatabase.kt', 'r') as f:
    content = f.read()

import re

# Update version
content = content.replace("version = 44,", "version = 45,")

# Add to addMigrations
content = content.replace(".addMigrations(MIGRATION_42_43, MIGRATION_43_44)", ".addMigrations(MIGRATION_42_43, MIGRATION_43_44, MIGRATION_44_45)")

with open('app/src/main/java/com/example/data/local/JuktiDatabase.kt', 'w') as f:
    f.write(content)
