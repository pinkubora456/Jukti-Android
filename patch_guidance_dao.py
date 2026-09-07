import re

daos_file = "app/src/main/java/com/example/data/local/Daos.kt"
with open(daos_file, "r") as f:
    content = f.read()

# I will replace the existing GuidanceDao completely
if "interface GuidanceDao" in content:
    # We will remove the old GuidanceDao and replace it.
    content = re.sub(r'@Dao\s*interface GuidanceDao \{[\s\S]*?\}', '', content)

new_dao = """
@Dao
interface GuidanceDao {
    // PYQ Focus
    @Query("SELECT * FROM pyq_focus")
    fun getAllPyqFocus(): kotlinx.coroutines.flow.Flow<List<PyqFocusEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPyqFocus(entity: PyqFocusEntity): Long
    
    @Update
    suspend fun updatePyqFocus(entity: PyqFocusEntity)

    // Focus Topics
    @Query("SELECT * FROM focus_topics")
    fun getAllFocusTopics(): kotlinx.coroutines.flow.Flow<List<FocusTopicEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFocusTopic(entity: FocusTopicEntity): Long
    
    @Update
    suspend fun updateFocusTopic(entity: FocusTopicEntity)
    
    @Delete
    suspend fun deleteFocusTopic(entity: FocusTopicEntity)

    // Prep Strategy
    @Query("SELECT * FROM prep_strategy")
    fun getAllPrepStrategies(): kotlinx.coroutines.flow.Flow<List<PrepStrategyEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrepStrategy(entity: PrepStrategyEntity): Long
    
    @Update
    suspend fun updatePrepStrategy(entity: PrepStrategyEntity)

    // Guidance Banners
    @Query("SELECT * FROM guidance_banner")
    fun getAllGuidanceBanners(): kotlinx.coroutines.flow.Flow<List<GuidanceBannerEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGuidanceBanner(entity: GuidanceBannerEntity): Long
    
    @Update
    suspend fun updateGuidanceBanner(entity: GuidanceBannerEntity)
    
    @Delete
    suspend fun deleteGuidanceBanner(entity: GuidanceBannerEntity)
}
"""

content += new_dao

with open(daos_file, "w") as f:
    f.write(content)
print("Updated GuidanceDao in Daos.kt")
