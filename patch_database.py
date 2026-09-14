import re

path = "app/src/main/java/com/example/data/local/JuktiDatabase.kt"
with open(path, "r") as f:
    content = f.read()

# Update version from 46 to 47
content = content.replace("version = 46", "version = 47")

# Add Migration 46 to 47
migration_str = """        val MIGRATION_46_47 = object : Migration(46, 47) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `questions` ADD COLUMN `contentType` TEXT NOT NULL DEFAULT 'normal'")
                db.execSQL("ALTER TABLE `questions` ADD COLUMN `passageId` TEXT NOT NULL DEFAULT ''")
            }
        }
"""

# Find where MIGRATION_45_46 is defined and insert after
pattern_migration = r"(val MIGRATION_45_46 = object : Migration\(45, 46\) \{[\s\S]*?\n        \})"
if re.search(pattern_migration, content):
    content = re.sub(pattern_migration, r"\1\n" + migration_str, content)
else:
    print("Could not find MIGRATION_45_46")

# Add to migrations list
pattern_add = r"(\.addMigrations\(MIGRATION_42_43, MIGRATION_43_44, MIGRATION_44_45, MIGRATION_45_46\))"
if re.search(pattern_add, content):
    content = re.sub(pattern_add, r".addMigrations(MIGRATION_42_43, MIGRATION_43_44, MIGRATION_44_45, MIGRATION_45_46, MIGRATION_46_47)", content)
else:
    print("Could not find .addMigrations")

with open(path, "w") as f:
    f.write(content)
print("Patched JuktiDatabase.kt successfully")

