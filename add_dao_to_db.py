import re
path = "app/src/main/java/com/example/data/local/JuktiDatabase.kt"
with open(path, "r") as f:
    content = f.read()

dao_func = "abstract fun rcPassageDao(): ReadingComprehensionPassageDao"
if dao_func not in content:
    pattern = r"(abstract fun activityLogDao\(\): ActivityLogDao)"
    content = re.sub(pattern, r"\1\n    " + dao_func, content)
    
    # Also add MIGRATION_47_48 for the new table
    content = content.replace("version = 47", "version = 48")
    
    mig_str = """        val MIGRATION_47_48 = object : Migration(47, 48) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `rc_passages` (`passageId` TEXT NOT NULL, `passage` TEXT NOT NULL, `subject` TEXT NOT NULL, `chapter` TEXT NOT NULL, `topic` TEXT NOT NULL, `difficulty` TEXT NOT NULL, `updatedAt` INTEGER NOT NULL, `firebaseId` TEXT NOT NULL, PRIMARY KEY(`passageId`))")
            }
        }
"""
    mig_pattern = r"(val MIGRATION_46_47 = object : Migration\(46, 47\) \{[\s\S]*?\n        \})"
    if re.search(mig_pattern, content):
        content = re.sub(mig_pattern, r"\1\n" + mig_str, content)
        
    add_mig_pattern = r"(MIGRATION_45_46, MIGRATION_46_47\))"
    if re.search(add_mig_pattern, content):
        content = re.sub(add_mig_pattern, r"MIGRATION_45_46, MIGRATION_46_47, MIGRATION_47_48)", content)
        
    # Also need to add entity to @Database
    entity_pattern = r"EntitlementHistoryEntity::class,\n\s*GuidanceBannerEntity::class"
    if re.search(entity_pattern, content):
        content = re.sub(entity_pattern, r"EntitlementHistoryEntity::class,\n    GuidanceBannerEntity::class,\n    ReadingComprehensionPassageEntity::class", content)
    
    with open(path, "w") as f:
        f.write(content)
    print("Added Dao to Database")
else:
    print("Already in Database")
