import re

repo_file = "app/src/main/java/com/example/data/repository/JuktiRepository.kt"
with open(repo_file, "r") as f:
    content = f.read()

new_guidance = """
    val allPyqFocus = guidanceDao.getAllPyqFocus()
    val allFocusTopics = guidanceDao.getAllFocusTopics()
    val allPrepStrategies = guidanceDao.getAllPrepStrategies()
    val allGuidanceBanners = guidanceDao.getAllGuidanceBanners()

    suspend fun savePyqFocus(entity: com.example.data.local.PyqFocusEntity) {
        if (entity.id == 0L) guidanceDao.insertPyqFocus(entity) else guidanceDao.updatePyqFocus(entity)
    }
    suspend fun saveFocusTopic(entity: com.example.data.local.FocusTopicEntity) {
        if (entity.id == 0L) guidanceDao.insertFocusTopic(entity) else guidanceDao.updateFocusTopic(entity)
    }
    suspend fun deleteFocusTopic(entity: com.example.data.local.FocusTopicEntity) {
        guidanceDao.deleteFocusTopic(entity)
    }
    suspend fun savePrepStrategy(entity: com.example.data.local.PrepStrategyEntity) {
        if (entity.id == 0L) guidanceDao.insertPrepStrategy(entity) else guidanceDao.updatePrepStrategy(entity)
    }
    suspend fun saveGuidanceBanner(entity: com.example.data.local.GuidanceBannerEntity) {
        if (entity.id == 0L) guidanceDao.insertGuidanceBanner(entity) else guidanceDao.updateGuidanceBanner(entity)
    }
    suspend fun deleteGuidanceBanner(entity: com.example.data.local.GuidanceBannerEntity) {
        guidanceDao.deleteGuidanceBanner(entity)
    }
"""

content = re.sub(r'val allGuidance = guidanceDao\.getAllGuidance\(\)', new_guidance.strip(), content)

with open(repo_file, "w") as f:
    f.write(content)

