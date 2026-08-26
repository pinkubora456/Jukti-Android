with open("app/src/main/java/com/example/data/local/Daos.kt", "r") as f:
    daos = f.read()

daos = daos.replace(
    "@Query(\"SELECT * FROM study_notes ORDER BY id DESC\")\n    fun getAllNotes()",
    "@Query(\"DELETE FROM study_notes WHERE isPremium = 1\")\n    suspend fun deletePremiumStudyNotes()\n\n    @Query(\"SELECT * FROM study_notes ORDER BY id DESC\")\n    fun getAllNotes()"
)

with open("app/src/main/java/com/example/data/local/Daos.kt", "w") as f:
    f.write(daos)
