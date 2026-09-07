import re
import os

# 1. Update Entities.kt
entities_file = "app/src/main/java/com/example/data/local/Entities.kt"
with open(entities_file, "r") as f:
    entities_content = f.read()

if "GuidanceEntity" not in entities_content:
    entities_content += """
@Entity(tableName = "guidance")
data class GuidanceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val exam: String,
    val subject: String,
    val chapter: String,
    val pyqFocus: String = "",
    val focusTopics: String = "",
    val prepStrategy: String = "",
    val strengthWeakness: String = "",
    val firebaseId: String = "",
    val updatedAt: Long = 0L,
    val version: Int = 1
)
"""
    with open(entities_file, "w") as f:
        f.write(entities_content)
    print("Updated Entities.kt")

# 2. Update Daos.kt
daos_file = "app/src/main/java/com/example/data/local/Daos.kt"
with open(daos_file, "r") as f:
    daos_content = f.read()

if "GuidanceDao" not in daos_content:
    daos_content += """
@Dao
interface GuidanceDao {
    @Query("SELECT * FROM guidance")
    fun getAllGuidance(): kotlinx.coroutines.flow.Flow<List<GuidanceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGuidance(guidance: GuidanceEntity): Long

    @Update
    suspend fun updateGuidance(guidance: GuidanceEntity)

    @Delete
    suspend fun deleteGuidance(guidance: GuidanceEntity)
}
"""
    with open(daos_file, "w") as f:
        f.write(daos_content)
    print("Updated Daos.kt")

# 3. Update JuktiDatabase.kt
db_file = "app/src/main/java/com/example/data/local/JuktiDatabase.kt"
with open(db_file, "r") as f:
    db_content = f.read()

if "GuidanceEntity::class" not in db_content:
    db_content = db_content.replace("SubjectChapterEntity::class,", "SubjectChapterEntity::class,\n        GuidanceEntity::class,")
    
    # Add DAO
    db_content = db_content.replace("abstract fun subjectChapterDao(): SubjectChapterDao", "abstract fun subjectChapterDao(): SubjectChapterDao\n    abstract fun guidanceDao(): GuidanceDao")
    
    with open(db_file, "w") as f:
        f.write(db_content)
    print("Updated JuktiDatabase.kt")

# 4. Update JuktiRepository.kt
repo_file = "app/src/main/java/com/example/data/repository/JuktiRepository.kt"
with open(repo_file, "r") as f:
    repo_content = f.read()

if "fun saveGuidance" not in repo_content:
    repo_content = repo_content.replace("class JuktiRepository(", "class JuktiRepository(\n    private val guidanceDao: com.example.data.local.GuidanceDao,")
    
    new_repo_methods = """
    val allGuidance = guidanceDao.getAllGuidance()

    suspend fun saveGuidance(guidance: com.example.data.local.GuidanceEntity) {
        if (guidance.id == 0L) {
            guidanceDao.insertGuidance(guidance)
        } else {
            guidanceDao.updateGuidance(guidance)
        }
    }
"""
    repo_content = repo_content.replace("val activeSubjectChapterStats", new_repo_methods + "\n    val activeSubjectChapterStats")
    
    with open(repo_file, "w") as f:
        f.write(repo_content)
    print("Updated JuktiRepository.kt")

# 5. Update JuktiViewModel.kt
vm_file = "app/src/main/java/com/example/ui/viewmodel/JuktiViewModel.kt"
with open(vm_file, "r") as f:
    vm_content = f.read()

if "MANAGE_GUIDANCE" not in vm_content:
    vm_content = vm_content.replace("MANAGE_QBANK,", "MANAGE_QBANK,\n    MANAGE_GUIDANCE,")
    
    new_vm_methods = """
    val allGuidance = repository.allGuidance.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun saveGuidance(guidance: com.example.data.local.GuidanceEntity) {
        viewModelScope.launch {
            repository.saveGuidance(guidance)
        }
    }
"""
    vm_content = vm_content.replace("val allSubjectsChapters", new_vm_methods + "\n    val allSubjectsChapters")
    
    with open(vm_file, "w") as f:
        f.write(vm_content)
    print("Updated JuktiViewModel.kt")

# 6. Update MainActivity.kt
main_file = "app/src/main/java/com/example/MainActivity.kt"
with open(main_file, "r") as f:
    main_content = f.read()

if "MANAGE_GUIDANCE" not in main_content:
    main_content = main_content.replace("Screen.MANAGE_QBANK -> ManageQBankScreen(viewModel)", "Screen.MANAGE_QBANK -> ManageQBankScreen(viewModel)\n                                Screen.MANAGE_GUIDANCE -> com.example.ui.screens.ManageGuidanceScreen(viewModel)")
    
    with open(main_file, "w") as f:
        f.write(main_content)
    print("Updated MainActivity.kt")

# 7. Update WorkspaceScreen.kt
workspace_file = "app/src/main/java/com/example/ui/screens/WorkspaceScreen.kt"
with open(workspace_file, "r") as f:
    workspace_content = f.read()

if "MANAGE_GUIDANCE" not in workspace_content:
    workspace_content = workspace_content.replace(
        "WorkspaceBannerCard(\n            title = \"Manage Q-Bank\",",
        "WorkspaceBannerCard(\n            title = \"Manage Guidance\",\n            icon = Icons.Default.Explore,\n            onClick = { viewModel.navigateTo(com.example.ui.viewmodel.Screen.MANAGE_GUIDANCE) }\n        )\n        WorkspaceBannerCard(\n            title = \"Manage Q-Bank\","
    )
    with open(workspace_file, "w") as f:
        f.write(workspace_content)
    print("Updated WorkspaceScreen.kt")

print("Patch complete.")
