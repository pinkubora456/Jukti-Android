db_file = "app/src/main/java/com/example/data/local/JuktiDatabase.kt"
with open(db_file, "r") as f:
    content = f.read()

# Make sure all entities are registered
content = content.replace("GuidanceEntity::class,", "") # Remove old if exists
content = content.replace("SubjectChapterEntity::class,", "SubjectChapterEntity::class,\n        PyqFocusEntity::class,\n        FocusTopicEntity::class,\n        PrepStrategyEntity::class,\n        GuidanceBannerEntity::class,")

# Bump version number from 41 to 42
content = content.replace("version = 41,", "version = 42,")
content = content.replace("version = 40,", "version = 42,")

# Add MIGRATION_41_42
content = content.replace("MIGRATION_40_41)", "MIGRATION_40_41, MIGRATION_41_42)")

with open(db_file, "w") as f:
    f.write(content)

print("Updated JuktiDatabase.kt")
