import re

with open("app/src/main/java/com/example/data/local/JuktiDatabase.kt", "r") as f:
    content = f.read()

migration = """
val MIGRATION_42_43 = object : androidx.room.migration.Migration(42, 43) {
    override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE prep_strategies ADD COLUMN subject TEXT DEFAULT NULL")
    }
}
"""

if "MIGRATION_42_43" not in content:
    content = content.replace("fun getDatabase(context: Context): JuktiDatabase {", migration + "\n\n        fun getDatabase(context: Context): JuktiDatabase {")
    content = content.replace(".fallbackToDestructiveMigration()", ".addMigrations(MIGRATION_42_43)\n                .fallbackToDestructiveMigration()")

with open("app/src/main/java/com/example/data/local/JuktiDatabase.kt", "w") as f:
    f.write(content)

