db_file = "app/src/main/java/com/example/data/local/JuktiDatabase.kt"
with open(db_file, "r") as f:
    content = f.read()

migration_code = """        val MIGRATION_41_42 = object : Migration(41, 42) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Destructive migration fallback handles table creation.
            }
        }
"""
content = content.replace("val MIGRATION_40_41 = object : Migration(40, 41) {", migration_code + "\n        val MIGRATION_40_41 = object : Migration(40, 41) {")

with open(db_file, "w") as f:
    f.write(content)
