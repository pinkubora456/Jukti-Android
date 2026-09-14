path = "app/src/main/java/com/example/data/local/Daos.kt"
with open(path, "r") as f:
    content = f.read()

passage_dao = """
@Dao
interface ReadingComprehensionPassageDao {
    @Query("SELECT * FROM rc_passages")
    fun getAllPassages(): kotlinx.coroutines.flow.Flow<List<ReadingComprehensionPassageEntity>>

    @Query("SELECT * FROM rc_passages WHERE passageId = :passageId LIMIT 1")
    suspend fun getPassageById(passageId: String): ReadingComprehensionPassageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPassage(passage: ReadingComprehensionPassageEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPassages(passages: List<ReadingComprehensionPassageEntity>)

    @Query("DELETE FROM rc_passages")
    suspend fun clearAll()
    
    @Query("SELECT * FROM rc_passages WHERE passageId = :passageId LIMIT 1")
    fun getPassageByIdFlow(passageId: String): kotlinx.coroutines.flow.Flow<ReadingComprehensionPassageEntity?>
}
"""

if "ReadingComprehensionPassageDao" not in content:
    content = content + "\n" + passage_dao
    with open(path, "w") as f:
        f.write(content)
    print("Added Passage Dao")
else:
    print("Dao exists")
